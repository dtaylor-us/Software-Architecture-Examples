package com.gridops.audit;

import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Primary
public class JpaAuditEntryRepository implements AuditEntryRepository {

    private final AuditEntryJpaRepository jpa;

    public JpaAuditEntryRepository(AuditEntryJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public AuditService.AuditEntry save(AuditService.AuditEntry entry) {
        AuditEntryEntity entity = AuditEntryEntity.from(entry);
        return jpa.save(entity).toEntry();
    }

    @Override
    public List<AuditService.AuditEntry> findByActor(String actor) {
        return jpa.findAllByActor(actor).stream()
                .map(AuditEntryEntity::toEntry)
                .toList();
    }

    @Override
    public List<AuditService.AuditEntry> findByAction(String action) {
        return jpa.findAllByAction(action).stream()
                .map(AuditEntryEntity::toEntry)
                .toList();
    }

    @Override
    public List<AuditService.AuditEntry> findRecent(int limit) {
        return jpa.findAllByOrderByAtDesc(PageRequest.of(0, limit)).stream()
                .map(AuditEntryEntity::toEntry)
                .toList();
    }
}
