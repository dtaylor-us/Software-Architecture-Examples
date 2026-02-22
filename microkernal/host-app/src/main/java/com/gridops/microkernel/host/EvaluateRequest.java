package com.gridops.microkernel.host;

import java.time.Instant;
import java.util.Map;

/**
 * REST request body for POST /evaluate.
 */
public class EvaluateRequest {

    private String eventId;
    private String eventType;
    private String timestamp;  // ISO-8601 or null for now
    private Map<String, Object> payload;

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }
}
