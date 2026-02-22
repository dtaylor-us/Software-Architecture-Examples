package com.gridops.pipeline.pipeline;

import com.gridops.pipeline.domain.TelemetryEventType;
import com.gridops.pipeline.domain.TelemetryPipelineContext;
import com.gridops.pipeline.repository.TelemetryEventRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the full {@link TelemetryPipeline}.
 * Uses an in-memory H2 database (configured by {@code application.yml}).
 */
@SpringBootTest
class TelemetryPipelineTest {

    @Autowired
    TelemetryPipeline pipeline;

    @Autowired
    TelemetryEventRepository repository;

    @AfterEach
    void cleanUp() {
        repository.deleteAll();
    }

    @Test
    void processesJsonOutageEventEndToEnd() {
        String raw = "{\"type\":\"OUTAGE\",\"deviceId\":\"DEV-001\",\"timestamp\":\"2024-01-15T10:30:00Z\",\"value\":1500.0,\"region\":\"WEST\"}";

        TelemetryPipelineContext ctx = pipeline.process(raw);

        assertThat(ctx.isValid()).isTrue();
        assertThat(ctx.isFiltered()).isFalse();
        assertThat(ctx.getEventType()).isEqualTo(TelemetryEventType.OUTAGE);
        assertThat(ctx.getDeviceId()).isEqualTo("DEV-001");
        assertThat(ctx.getRegion()).isEqualTo("WEST");
        assertThat(ctx.getValue()).isEqualTo(1500.0);
        assertThat(ctx.getPersistedId()).isNotNull();
        assertThat(ctx.getMetadata()).containsKey("gridZone");
        assertThat(ctx.getMetadata()).containsKey("outageSeverity");
    }

    @Test
    void processesPipeDelimitedForecastEvent() {
        String raw = "FORECAST|METER-42|2024-01-15T10:30:00Z|250.5|EAST";

        TelemetryPipelineContext ctx = pipeline.process(raw);

        assertThat(ctx.isValid()).isTrue();
        assertThat(ctx.isFiltered()).isFalse();
        assertThat(ctx.getEventType()).isEqualTo(TelemetryEventType.FORECAST);
        assertThat(ctx.getDeviceId()).isEqualTo("METER-42");
        assertThat(ctx.getRegion()).isEqualTo("EAST");
        assertThat(ctx.getPersistedId()).isNotNull();
    }

    @Test
    void normalizedRegionAbbreviationInPipeDelimited() {
        String raw = "PRICE|NODE-99|2024-01-15T10:30:00Z|45.75|W";

        TelemetryPipelineContext ctx = pipeline.process(raw);

        assertThat(ctx.isValid()).isTrue();
        assertThat(ctx.getRegion()).isEqualTo("WEST");
    }

    @Test
    void invalidInputHaltsBeforePersist() {
        TelemetryPipelineContext ctx = pipeline.process("");

        assertThat(ctx.isValid()).isFalse();
        assertThat(ctx.getPersistedId()).isNull();
        assertThat(ctx.getValidationErrors()).isNotEmpty();
    }

    @Test
    void zeroValueEventIsFiltered() {
        String raw = "OUTAGE|DEV-001|2024-01-15T10:30:00Z|0.0|WEST";

        TelemetryPipelineContext ctx = pipeline.process(raw);

        assertThat(ctx.isFiltered()).isTrue();
        assertThat(ctx.getPersistedId()).isNull();
    }

    @Test
    void unknownTypeEventIsFiltered() {
        String raw = "{\"type\":\"VOLTAGE\",\"deviceId\":\"DEV-X\",\"timestamp\":\"2024-01-15T10:30:00Z\",\"value\":10.0}";

        TelemetryPipelineContext ctx = pipeline.process(raw);

        // UNKNOWN type passes validation but is dropped by the filter stage
        assertThat(ctx.isFiltered()).isTrue();
        assertThat(ctx.getPersistedId()).isNull();
    }

    @Test
    void defaultUnitAssignedForForecast() {
        String raw = "FORECAST|M-01|2024-01-15T10:30:00Z|100.0|NORTH";

        TelemetryPipelineContext ctx = pipeline.process(raw);

        assertThat(ctx.getUnit()).isEqualTo("MW");
    }
}
