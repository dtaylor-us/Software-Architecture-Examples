package com.gridops.forecast;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
@Primary
public class JpaForecastRepository implements ForecastRepository {

    private final ForecastJpaRepository jpa;

    public JpaForecastRepository(ForecastJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public ForecastService.ForecastRecord save(ForecastService.ForecastRecord record) {
        ForecastEntity entity = ForecastEntity.from(record);
        return jpa.save(entity).toRecord();
    }

    @Override
    public List<ForecastService.ForecastRecord> findByZoneId(String zoneId) {
        return jpa.findAllByZoneId(zoneId).stream()
                .map(ForecastEntity::toRecord)
                .toList();
    }

    @Override
    public List<ForecastService.ForecastRecord> findByZoneIdAndTimeWindow(String zoneId, Instant from, Instant to) {
        return jpa.findByZoneIdAndTimeWindow(zoneId, from, to).stream()
                .map(ForecastEntity::toRecord)
                .toList();
    }
}
