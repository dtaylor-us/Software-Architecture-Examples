package com.gridops.alert;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** In-memory implementation for unit tests; app uses {@link JpaAlertRepository}. */
public class InMemoryAlertRepository implements AlertRepository {

    private final List<AlertService.AlertRecord> store = new CopyOnWriteArrayList<>();

    @Override
    public AlertService.AlertRecord save(AlertService.AlertRecord record) {
        store.add(record);
        return record;
    }

    @Override
    public List<AlertService.AlertRecord> findAllActive() {
        return store.stream()
                .filter(a -> "ACTIVE".equals(a.status()))
                .toList();
    }

    @Override
    public List<AlertService.AlertRecord> findBySeverity(String severity) {
        return store.stream()
                .filter(a -> severity.equals(a.severity()))
                .toList();
    }
}
