package com.gridops.audit;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** In-memory implementation for unit tests; app uses {@link JpaAuditEntryRepository}. */
public class InMemoryAuditEntryRepository implements AuditEntryRepository {

    private final List<AuditService.AuditEntry> store = new CopyOnWriteArrayList<>();

    @Override
    public AuditService.AuditEntry save(AuditService.AuditEntry entry) {
        store.add(entry);
        return entry;
    }

    @Override
    public List<AuditService.AuditEntry> findByActor(String actor) {
        return store.stream()
                .filter(e -> actor.equals(e.actor()))
                .toList();
    }

    @Override
    public List<AuditService.AuditEntry> findByAction(String action) {
        return store.stream()
                .filter(e -> action.equals(e.action()))
                .toList();
    }

    @Override
    public List<AuditService.AuditEntry> findRecent(int limit) {
        return store.stream()
                .sorted(Comparator.comparing(AuditService.AuditEntry::at).reversed())
                .limit(limit)
                .toList();
    }
}
