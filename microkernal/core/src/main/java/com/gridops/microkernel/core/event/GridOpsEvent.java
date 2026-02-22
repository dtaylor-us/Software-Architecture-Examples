package com.gridops.microkernel.core.event;

import java.time.Instant;
import java.util.Map;

/**
 * Normalized GridOps event consumed by the Alert Rule Engine.
 * Domain-agnostic structure keeps the core thin; plugins interpret fields as needed.
 */
public final class GridOpsEvent {

    private final String eventId;
    private final String eventType;
    private final Instant timestamp;
    private final Map<String, Object> payload;

    public GridOpsEvent(String eventId, String eventType, Instant timestamp, Map<String, Object> payload) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
        this.payload = payload != null ? Map.copyOf(payload) : Map.of();
    }

    public String getEventId() { return eventId; }
    public String getEventType() { return eventType; }
    public Instant getTimestamp() { return timestamp; }
    public Map<String, Object> getPayload() { return payload; }

    @SuppressWarnings("unchecked")
    public <T> T getPayloadValue(String key, Class<T> type) {
        Object v = payload.get(key);
        if (v == null) return null;
        if (type.isInstance(v)) return (T) v;
        if (type == Double.class && v instanceof Number) return (T) Double.valueOf(((Number) v).doubleValue());
        if (type == Long.class && v instanceof Number) return (T) Long.valueOf(((Number) v).longValue());
        return null;
    }
}
