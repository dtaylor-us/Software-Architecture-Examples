package com.gridops.outage;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/** In-memory implementation for unit tests; app uses {@link JpaOutageRepository}. */
public class InMemoryOutageRepository implements OutageRepository {

    private final Map<String, OutageService.OutageRecord> store = new ConcurrentHashMap<>();

    @Override
    public OutageService.OutageRecord save(OutageService.OutageRecord record) {
        store.put(record.id(), record);
        return record;
    }

    @Override
    public Optional<OutageService.OutageRecord> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<OutageService.OutageRecord> findAllActive() {
        return store.values().stream()
                .filter(o -> "ACTIVE".equals(o.status()))
                .collect(Collectors.toList());
    }

    @Override
    public List<OutageService.OutageRecord> findByAssetId(String assetId) {
        return store.values().stream()
                .filter(o -> assetId.equals(o.assetId()))
                .collect(Collectors.toList());
    }
}
