package com.gridops.pipeline.pipeline.stages;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gridops.pipeline.domain.TelemetryEventType;
import com.gridops.pipeline.domain.TelemetryPipelineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ParseStage} — no Spring context required.
 */
class ParseStageTest {

    private ParseStage stage;

    @BeforeEach
    void setUp() {
        stage = new ParseStage(new ObjectMapper());
    }

    @Test
    void parsesJsonOutageEvent() {
        TelemetryPipelineContext ctx = ctx(
                "{\"type\":\"OUTAGE\",\"deviceId\":\"DEV-001\",\"timestamp\":\"2024-01-15T10:30:00Z\",\"value\":1500.0,\"region\":\"WEST\"}");
        stage.process(ctx);

        assertThat(ctx.isValid()).isTrue();
        assertThat(ctx.getEventType()).isEqualTo(TelemetryEventType.OUTAGE);
        assertThat(ctx.getDeviceId()).isEqualTo("DEV-001");
        assertThat(ctx.getValue()).isEqualTo(1500.0);
        assertThat(ctx.getRegion()).isEqualTo("WEST");
    }

    @Test
    void parsesJsonPriceEvent() {
        TelemetryPipelineContext ctx = ctx(
                "{\"type\":\"PRICE\",\"deviceId\":\"NODE-99\",\"timestamp\":\"2024-06-01T08:00:00Z\",\"value\":45.75,\"unit\":\"$/MWh\",\"region\":\"EAST\"}");
        stage.process(ctx);

        assertThat(ctx.getEventType()).isEqualTo(TelemetryEventType.PRICE);
        assertThat(ctx.getUnit()).isEqualTo("$/MWh");
    }

    @Test
    void parsesPipeDelimitedForecastEvent() {
        TelemetryPipelineContext ctx = ctx("FORECAST|METER-42|2024-01-15T10:30:00Z|250.5|EAST");
        stage.process(ctx);

        assertThat(ctx.isValid()).isTrue();
        assertThat(ctx.getEventType()).isEqualTo(TelemetryEventType.FORECAST);
        assertThat(ctx.getDeviceId()).isEqualTo("METER-42");
        assertThat(ctx.getValue()).isEqualTo(250.5);
        assertThat(ctx.getRegion()).isEqualTo("EAST");
    }

    @Test
    void parsesPipeDelimitedWithoutRegion() {
        TelemetryPipelineContext ctx = ctx("OUTAGE|DEV-002|2024-01-15T10:30:00Z|800.0");
        stage.process(ctx);

        assertThat(ctx.isValid()).isTrue();
        assertThat(ctx.getRegion()).isNull();
    }

    @Test
    void setsUnknownTypeForUnrecognisedJsonType() {
        TelemetryPipelineContext ctx = ctx(
                "{\"type\":\"VOLTAGE\",\"deviceId\":\"D\",\"timestamp\":\"2024-01-15T10:30:00Z\",\"value\":1.0}");
        stage.process(ctx);

        assertThat(ctx.getEventType()).isEqualTo(TelemetryEventType.UNKNOWN);
    }

    @Test
    void invalidatesOnTooFewPipeFields() {
        TelemetryPipelineContext ctx = ctx("OUTAGE|DEV-001|2024-01-15T10:30:00Z");
        stage.process(ctx);

        assertThat(ctx.isValid()).isFalse();
        assertThat(ctx.getValidationErrors()).isNotEmpty();
    }

    @Test
    void invalidatesOnBadTimestamp() {
        TelemetryPipelineContext ctx = ctx("OUTAGE|DEV-001|not-a-date|100.0|WEST");
        stage.process(ctx);

        assertThat(ctx.isValid()).isFalse();
    }

    private TelemetryPipelineContext ctx(String raw) {
        return new TelemetryPipelineContext(raw);
    }
}
