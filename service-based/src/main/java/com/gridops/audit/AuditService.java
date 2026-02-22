package com.gridops.audit;

import java.time.Instant;
import java.util.List;

/**
 * Internal interface for the Audit service. Owns audit log entries (data ownership).
 */
public interface AuditService {

    AuditEntry record(AuditCommand command);

    List<AuditEntry> listByActor(String actor);

    List<AuditEntry> listByAction(String action);

    List<AuditEntry> listRecent(int limit);

    record AuditCommand(String actor, String action, String targetType, String targetId, String details) {}

    record AuditEntry(String id, String actor, String action, String targetType, String targetId, String details, Instant at) {}
}
