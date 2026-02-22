package com.gridops.forecast;

import java.time.Instant;
import java.util.List;

/**
 * Internal interface for the Forecast service. Owns load/demand forecasts.
 */
public interface ForecastService {

    ForecastRecord createForecast(CreateForecastCommand command);

    List<ForecastRecord> getForecastsForWindow(String zoneId, Instant from, Instant to);

    List<ForecastRecord> listByZone(String zoneId);

    record CreateForecastCommand(String zoneId, Instant at, double mwValue, String horizon) {}

    record ForecastRecord(String id, String zoneId, Instant at, double mwValue, String horizon) {}
}
