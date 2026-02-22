package com.gridops.forecast;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** In-memory implementation for unit tests; app uses {@link JpaForecastRepository}. */
public class InMemoryForecastRepository implements ForecastRepository {

    private final List<ForecastService.ForecastRecord> store = new CopyOnWriteArrayList<>();

    @Override
    public ForecastService.ForecastRecord save(ForecastService.ForecastRecord record) {
        store.add(record);
        return record;
    }

    @Override
    public List<ForecastService.ForecastRecord> findByZoneId(String zoneId) {
        return store.stream()
                .filter(f -> zoneId.equals(f.zoneId()))
                .toList();
    }

    @Override
    public List<ForecastService.ForecastRecord> findByZoneIdAndTimeWindow(String zoneId, Instant from, Instant to) {
        return store.stream()
                .filter(f -> zoneId.equals(f.zoneId()) && !f.at().isBefore(from) && f.at().isBefore(to))
                .toList();
    }
}
