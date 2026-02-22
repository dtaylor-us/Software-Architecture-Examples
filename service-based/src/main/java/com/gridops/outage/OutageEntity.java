package com.gridops.outage;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "outages")
public class OutageEntity {

    @Id
    private String id;
    @Column(nullable = false)
    private String assetId;
    @Column(nullable = false, length = 2000)
    private String description;
    @Column(nullable = false)
    private Instant startTime;
    private Instant endTime;
    @Column(nullable = false, length = 32)
    private String severity;
    @Column(nullable = false, length = 32)
    private String status;

    protected OutageEntity() {}

    public OutageEntity(String id, String assetId, String description, Instant startTime, Instant endTime, String severity, String status) {
        this.id = id;
        this.assetId = assetId;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.severity = severity;
        this.status = status;
    }

    public static OutageEntity from(OutageService.OutageRecord r) {
        return new OutageEntity(r.id(), r.assetId(), r.description(), r.startTime(), r.endTime(), r.severity(), r.status());
    }

    public OutageService.OutageRecord toRecord() {
        return new OutageService.OutageRecord(id, assetId, description, startTime, endTime, severity, status);
    }

    public String getId() { return id; }
    public String getAssetId() { return assetId; }
    public String getDescription() { return description; }
    public Instant getStartTime() { return startTime; }
    public Instant getEndTime() { return endTime; }
    public String getSeverity() { return severity; }
    public String getStatus() { return status; }
}
