package com.gridops.pipeline.pipeline.stages;

import com.gridops.pipeline.domain.TelemetryEventType;
import com.gridops.pipeline.domain.TelemetryPipelineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link NormalizeStage}.
 */
class NormalizeStageTest {

    private NormalizeStage stage;

    @BeforeEach
    void setUp() {
        stage = new NormalizeStage();
    }

    @Test
    void expandsRegionAbbreviation() {
        TelemetryPipelineContext ctx = ctxWith(TelemetryEventType.OUTAGE, "W", 100.0, null);
        stage.process(ctx);

        assertThat(ctx.getRegion()).isEqualTo("WEST");
    }

    @Test
    void upperCasesRegion() {
        TelemetryPipelineContext ctx = ctxWith(TelemetryEventType.PRICE, "east", 50.0, null);
        stage.process(ctx);

        assertThat(ctx.getRegion()).isEqualTo("EAST");
    }

    @Test
    void roundsValueToTwoDecimalPlaces() {
        TelemetryPipelineContext ctx = ctxWith(TelemetryEventType.PRICE, "NORTH", 45.7777, null);
        stage.process(ctx);

        assertThat(ctx.getValue()).isEqualTo(45.78);
    }

    @Test
    void assignsDefaultUnitWhenAbsent() {
        TelemetryPipelineContext ctx = ctxWith(TelemetryEventType.FORECAST, "SOUTH", 200.0, null);
        stage.process(ctx);

        assertThat(ctx.getUnit()).isEqualTo("MW");
    }

    @Test
    void doesNotOverrideExistingUnit() {
        TelemetryPipelineContext ctx = ctxWith(TelemetryEventType.PRICE, "WEST", 60.0, "EUR/MWh");
        stage.process(ctx);

        assertThat(ctx.getUnit()).isEqualTo("EUR/MWh");
    }

    @Test
    void upperCasesDeviceId() {
        TelemetryPipelineContext ctx = ctxWith(TelemetryEventType.OUTAGE, "WEST", 100.0, null);
        ctx.setDeviceId("dev-001");
        stage.process(ctx);

        assertThat(ctx.getDeviceId()).isEqualTo("DEV-001");
    }

    private TelemetryPipelineContext ctxWith(TelemetryEventType type, String region, double value, String unit) {
        TelemetryPipelineContext ctx = new TelemetryPipelineContext("raw");
        ctx.setEventType(type);
        ctx.setRegion(region);
        ctx.setValue(value);
        ctx.setUnit(unit);
        ctx.setDeviceId("dev-001");
        return ctx;
    }
}
