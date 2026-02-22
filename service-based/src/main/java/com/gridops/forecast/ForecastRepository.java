package com.gridops.forecast;

import java.time.Instant;
import java.util.List;

/**
 * Data layer for the Forecast service. Owned by this service only.
 */
public interface ForecastRepository {

    ForecastService.ForecastRecord save(ForecastService.ForecastRecord record);

    List<ForecastService.ForecastRecord> findByZoneId(String zoneId);

    List<ForecastService.ForecastRecord> findByZoneIdAndTimeWindow(String zoneId, Instant from, Instant to);
}
