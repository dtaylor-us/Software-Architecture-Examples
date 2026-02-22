package com.gridops.forecast;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ForecastService tested independently.
 */
class ForecastServiceTest {

    private ForecastService forecastService;

    @BeforeEach
    void setUp() {
        forecastService = new ForecastServiceImpl(new InMemoryForecastRepository());
    }

    @Test
    void createForecast_returnsRecordWithId() {
        ForecastService.CreateForecastCommand cmd = new ForecastService.CreateForecastCommand(
                "ZONE-NORTH",
                Instant.now(),
                150.5,
                "DAY-AHEAD"
        );
        ForecastService.ForecastRecord record = forecastService.createForecast(cmd);

        assertThat(record.id()).startsWith("FC-");
        assertThat(record.zoneId()).isEqualTo("ZONE-NORTH");
        assertThat(record.mwValue()).isEqualTo(150.5);
        assertThat(record.horizon()).isEqualTo("DAY-AHEAD");
    }

    @Test
    void listByZone_returnsOnlyThatZone() {
        forecastService.createForecast(new ForecastService.CreateForecastCommand("Z-A", Instant.now(), 100, "DA"));
        forecastService.createForecast(new ForecastService.CreateForecastCommand("Z-B", Instant.now(), 200, "DA"));
        forecastService.createForecast(new ForecastService.CreateForecastCommand("Z-A", Instant.now(), 110, "DA"));

        List<ForecastService.ForecastRecord> forA = forecastService.listByZone("Z-A");
        assertThat(forA).hasSize(2);
    }

    @Test
    void getForecastsForWindow_filtersByTime() {
        Instant base = Instant.parse("2025-02-20T12:00:00Z");
        forecastService.createForecast(new ForecastService.CreateForecastCommand("Z1", base, 100, "DA"));
        forecastService.createForecast(new ForecastService.CreateForecastCommand("Z1", base.plusSeconds(3600), 120, "DA"));
        forecastService.createForecast(new ForecastService.CreateForecastCommand("Z1", base.plusSeconds(7200), 130, "DA"));

        List<ForecastService.ForecastRecord> window = forecastService.getForecastsForWindow(
                "Z1", base, base.plusSeconds(4000));
        assertThat(window).hasSize(2);
    }
}
