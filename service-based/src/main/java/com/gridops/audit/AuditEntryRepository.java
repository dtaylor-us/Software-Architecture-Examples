package com.gridops.audit;

import java.util.List;

/**
 * Data layer for the Audit service. Owned by this service only.
 */
public interface AuditEntryRepository {

    AuditService.AuditEntry save(AuditService.AuditEntry entry);

    List<AuditService.AuditEntry> findByActor(String actor);

    List<AuditService.AuditEntry> findByAction(String action);

    List<AuditService.AuditEntry> findRecent(int limit);
}
