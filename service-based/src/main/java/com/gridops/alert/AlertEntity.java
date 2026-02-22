package com.gridops.alert;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "alerts")
public class AlertEntity {

    @Id
    private String id;
    @Column(nullable = false, length = 64)
    private String type;
    @Column(nullable = false, length = 2000)
    private String message;
    @Column(nullable = false, length = 32)
    private String severity;
    @Column(length = 64)
    private String sourceId;
    @Column(nullable = false)
    private Instant raisedAt;
    @Column(nullable = false, length = 32)
    private String status;

    protected AlertEntity() {}

    public AlertEntity(String id, String type, String message, String severity, String sourceId, Instant raisedAt, String status) {
        this.id = id;
        this.type = type;
        this.message = message;
        this.severity = severity;
        this.sourceId = sourceId;
        this.raisedAt = raisedAt;
        this.status = status;
    }

    public static AlertEntity from(AlertService.AlertRecord r) {
        return new AlertEntity(r.id(), r.type(), r.message(), r.severity(), r.sourceId(), r.raisedAt(), r.status());
    }

    public AlertService.AlertRecord toRecord() {
        return new AlertService.AlertRecord(id, type, message, severity, sourceId, raisedAt, status);
    }

    public String getId() { return id; }
    public String getType() { return type; }
    public String getMessage() { return message; }
    public String getSeverity() { return severity; }
    public String getSourceId() { return sourceId; }
    public Instant getRaisedAt() { return raisedAt; }
    public String getStatus() { return status; }
}
