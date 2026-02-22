package com.gridops.audit;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AuditServiceImpl implements AuditService {

    private final AuditEntryRepository repository;

    public AuditServiceImpl(AuditEntryRepository repository) {
        this.repository = repository;
    }

    @Override
    public AuditEntry record(AuditCommand command) {
        String id = "AUD-" + UUID.randomUUID().toString().substring(0, 8);
        AuditEntry entry = new AuditEntry(
                id,
                command.actor(),
                command.action(),
                command.targetType(),
                command.targetId(),
                command.details(),
                Instant.now()
        );
        return repository.save(entry);
    }

    @Override
    public List<AuditEntry> listByActor(String actor) {
        return repository.findByActor(actor);
    }

    @Override
    public List<AuditEntry> listByAction(String action) {
        return repository.findByAction(action);
    }

    @Override
    public List<AuditEntry> listRecent(int limit) {
        return repository.findRecent(limit);
    }
}
