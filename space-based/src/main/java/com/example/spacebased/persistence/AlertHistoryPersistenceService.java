package com.example.spacebased.persistence;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Saves one alert history entity in its own transaction so duplicate key errors
 * only roll back that insert, not the whole batch.
 */
@Service
public class AlertHistoryPersistenceService {

    private final AlertHistoryRepository repository;

    public AlertHistoryPersistenceService(AlertHistoryRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveOne(AlertHistoryEntity entity) {
        if (repository.existsByAlertId(entity.getAlertId())) {
            return; // already persisted (e.g. by another instance or duplicate in queue)
        }
        repository.save(entity);
    }
}
