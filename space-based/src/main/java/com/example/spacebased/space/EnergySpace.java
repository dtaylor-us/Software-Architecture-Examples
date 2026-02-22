package com.example.spacebased.space;

import com.example.spacebased.config.SpaceProperties;
import com.example.spacebased.dto.ActiveAlert;
import com.example.spacebased.dto.PriceUpdate;
import com.example.spacebased.persistence.AlertHistoryRecord;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * The "space" in Space-Based Architecture: Redis holds hot state (prices, rolling window, active alerts).
 * API nodes are stateless; all shared state lives here. Partitioning is by nodeId for scale.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EnergySpace {

    private final RedisTemplate<String, Object> redis;
    private final SpaceProperties props;
    private final ObjectMapper objectMapper;

    private static final String ALERT_TYPE_SPIKE = "SPIKE";

    /**
     * Write bulk price updates into the space: latest price + rolling window per node.
     * Evaluate spike condition and create/refresh alerts with TTL (reservation pattern).
     */
    public List<ActiveAlert> writePriceUpdates(List<PriceUpdate> updates) {
        if (updates == null || updates.isEmpty()) {
            return List.of();
        }
        long windowMs = (long) props.getPriceWindowMinutes() * 60 * 1000;
        double thresholdMultiplier = props.getSpikeThresholdMultiplier();
        int ttlSec = props.getAlertTtlSeconds();
        List<ActiveAlert> raised = new ArrayList<>();

        for (PriceUpdate u : updates) {
            String nodeId = u.getNodeId();
            long ts = u.getTimestampMs() > 0 ? u.getTimestampMs() : System.currentTimeMillis();
            BigDecimal price = u.getPriceMwh();

            String priceKey = SpaceKeys.latestPrice(nodeId);
            String windowKey = SpaceKeys.priceWindow(nodeId);

            redis.opsForValue().set(priceKey, price.toPlainString());
            redis.opsForZSet().add(windowKey, price.toPlainString() + ":" + ts, ts);
            // Trim window to last N minutes
            long cutoff = ts - windowMs;
            redis.opsForZSet().removeRangeByScore(windowKey, Double.NEGATIVE_INFINITY, cutoff);

            // Spike evaluation: current price > threshold * rolling average
            Double avg = rollingAverage(windowKey, nodeId);
            if (avg != null && price.doubleValue() > avg * thresholdMultiplier) {
                String alertId = nodeId + ":" + ALERT_TYPE_SPIKE + ":" + ts;
                ActiveAlert alert = ActiveAlert.builder()
                    .alertId(alertId)
                    .nodeId(nodeId)
                    .type(ALERT_TYPE_SPIKE)
                    .priceMwh(price)
                    .thresholdOrAverage(BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP))
                    .raisedAt(Instant.ofEpochMilli(ts))
                    .ttlSeconds(ttlSec)
                    .build();
                putAlertWithTtl(alert, ttlSec);
                raised.add(alert);
            }
        }
        return raised;
    }

    private Double rollingAverage(String windowKey, String nodeId) {
        Set<ZSetOperations.TypedTuple<Object>> tuples = redis.opsForZSet().rangeWithScores(windowKey, 0, -1);
        if (tuples == null || tuples.size() < 2) return null;
        double sum = 0;
        int n = 0;
        for (ZSetOperations.TypedTuple<Object> t : tuples) {
            if (t.getValue() == null) continue;
            String v = t.getValue().toString();
            int colon = v.indexOf(':');
            if (colon > 0) {
                try {
                    sum += Double.parseDouble(v.substring(0, colon));
                    n++;
                } catch (NumberFormatException ignored) {}
            }
        }
        return n == 0 ? null : sum / n;
    }

    private void putAlertWithTtl(ActiveAlert alert, int ttlSeconds) {
        String key = SpaceKeys.alert(alert.getAlertId());
        try {
            redis.opsForValue().set(key, objectMapper.writeValueAsString(alert), ttlSeconds, TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Serialization error", e);
        }
        redis.opsForSet().add(SpaceKeys.activeAlertIds(), alert.getAlertId());
        redis.opsForSet().add(SpaceKeys.nodeAlerts(alert.getNodeId()), alert.getAlertId());
        redis.expire(SpaceKeys.nodeAlerts(alert.getNodeId()), ttlSeconds, TimeUnit.SECONDS);
        // Enqueue for async persistence (worker will persist and not depend on TTL)
        enqueueForPersistence(alert);
    }

    private void enqueueForPersistence(ActiveAlert alert) {
        AlertHistoryRecord record = AlertHistoryRecord.builder()
            .alertId(alert.getAlertId())
            .nodeId(alert.getNodeId())
            .type(alert.getType())
            .priceMwh(alert.getPriceMwh())
            .thresholdOrAverage(alert.getThresholdOrAverage())
            .raisedAt(alert.getRaisedAt())
            .build();
        try {
            redis.opsForList().rightPush(SpaceKeys.alertHistoryQueue(), objectMapper.writeValueAsString(record));
        } catch (JsonProcessingException e) {
            log.warn("Failed to enqueue alert for persistence: {}", e.getMessage());
        }
    }

    /**
     * List all active alerts (keys still in the space with TTL). Expired alerts disappear automatically.
     */
    @SuppressWarnings("unchecked")
    public List<ActiveAlert> getActiveAlerts() {
        Set<Object> ids = redis.opsForSet().members(SpaceKeys.activeAlertIds());
        if (ids == null || ids.isEmpty()) return List.of();

        List<ActiveAlert> result = new ArrayList<>();
        for (Object idObj : ids) {
            String id = idObj.toString();
            String key = SpaceKeys.alert(id);
            Object raw = redis.opsForValue().get(key);
            if (raw == null) {
                redis.opsForSet().remove(SpaceKeys.activeAlertIds(), id);
                continue;
            }
            try {
                ActiveAlert a = objectMapper.readValue(raw.toString(), ActiveAlert.class);
                result.add(a);
            } catch (JsonProcessingException e) {
                log.debug("Could not deserialize alert {}: {}", id, e.getMessage());
            }
        }
        return result.stream()
            .sorted(Comparator.comparing(ActiveAlert::getRaisedAt).reversed())
            .collect(Collectors.toList());
    }

    /**
     * Pop a batch of alert history records from the queue for persistence (used by background worker).
     */
    public List<AlertHistoryRecord> pollAlertHistoryQueue(int maxCount) {
        List<AlertHistoryRecord> batch = new ArrayList<>();
        String queueKey = SpaceKeys.alertHistoryQueue();
        for (int i = 0; i < maxCount; i++) {
            Object raw = redis.opsForList().leftPop(queueKey);
            if (raw == null) break;
            try {
                AlertHistoryRecord record = raw instanceof String
                    ? objectMapper.readValue((String) raw, AlertHistoryRecord.class)
                    : objectMapper.convertValue(raw, AlertHistoryRecord.class);
                batch.add(record);
            } catch (Exception e) {
                log.warn("Invalid record in alert history queue: {}", e.getMessage());
            }
        }
        return batch;
    }
}
