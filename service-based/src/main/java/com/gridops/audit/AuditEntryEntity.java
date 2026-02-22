package com.gridops.audit;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "audit_entries")
public class AuditEntryEntity {

    @Id
    private String id;
    @Column(nullable = false, length = 128)
    private String actor;
    @Column(nullable = false, length = 128)
    private String action;
    @Column(length = 64)
    private String targetType;
    @Column(length = 64)
    private String targetId;
    @Column(length = 2000)
    private String details;
    @Column(nullable = false)
    private Instant at;

    protected AuditEntryEntity() {}

    public AuditEntryEntity(String id, String actor, String action, String targetType, String targetId, String details, Instant at) {
        this.id = id;
        this.actor = actor;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.details = details;
        this.at = at;
    }

    public static AuditEntryEntity from(AuditService.AuditEntry e) {
        return new AuditEntryEntity(e.id(), e.actor(), e.action(), e.targetType(), e.targetId(), e.details(), e.at());
    }

    public AuditService.AuditEntry toEntry() {
        return new AuditService.AuditEntry(id, actor, action, targetType, targetId, details, at);
    }

    public String getId() { return id; }
    public String getActor() { return actor; }
    public String getAction() { return action; }
    public String getTargetType() { return targetType; }
    public String getTargetId() { return targetId; }
    public String getDetails() { return details; }
    public Instant getAt() { return at; }
}
