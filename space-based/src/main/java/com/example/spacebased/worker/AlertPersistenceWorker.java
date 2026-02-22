package com.example.spacebased.worker;

import com.example.spacebased.config.PersistenceProperties;
import com.example.spacebased.persistence.AlertHistoryEntity;
import com.example.spacebased.persistence.AlertHistoryPersistenceService;
import com.example.spacebased.persistence.AlertHistoryRecord;
import com.example.spacebased.persistence.AlertHistoryRepository;
import com.example.spacebased.space.EnergySpace;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Background worker: drains the Redis alert-history queue and persists to the DB.
 * Decouples write path (API -> space) from durable storage; avoids blocking the space on DB latency.
 * Skips duplicates (same alert_id already persisted). Each insert runs in its own transaction so
 * a duplicate never marks the whole batch rollback-only.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AlertPersistenceWorker {

    private final EnergySpace energySpace;
    private final AlertHistoryRepository repository;
    private final AlertHistoryPersistenceService persistenceService;
    private final PersistenceProperties persistenceProperties;

    @Scheduled(fixedDelayString = "${app.persistence.poll-interval-ms}")
    public void flushAlertHistory() {
        int batchSize = persistenceProperties.getBatchSize();
        List<AlertHistoryRecord> batch = energySpace.pollAlertHistoryQueue(batchSize);
        if (batch.isEmpty()) return;

        List<AlertHistoryEntity> entities = batch.stream()
            .map(AlertHistoryEntity::fromRecord)
            .toList();
        List<String> alertIds = entities.stream().map(AlertHistoryEntity::getAlertId).toList();
        Set<String> existing = repository.findAlertIdsByAlertIdIn(alertIds).stream().collect(Collectors.toSet());
        List<AlertHistoryEntity> toSave = entities.stream()
            .filter(e -> !existing.contains(e.getAlertId()))
            .toList();
        if (toSave.isEmpty()) {
            log.trace("Batch had only duplicate alert_ids, nothing to persist");
            return;
        }
        int saved = 0;
        for (AlertHistoryEntity entity : toSave) {
            try {
                persistenceService.saveOne(entity);
                saved++;
            } catch (DataIntegrityViolationException ignored) {
                log.trace("Skipped duplicate alert_id: {}", entity.getAlertId());
            }
        }
        log.debug("Persisted {} alert history records (skipped {} duplicates)", saved, entities.size() - saved);
    }
}
