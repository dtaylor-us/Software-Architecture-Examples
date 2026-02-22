package com.gridops.pipeline.pipeline.stages;

import com.gridops.pipeline.domain.TelemetryEventType;
import com.gridops.pipeline.domain.TelemetryPipelineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ValidateStage}.
 */
class ValidateStageTest {

    private ValidateStage stage;

    @BeforeEach
    void setUp() {
        stage = new ValidateStage();
    }

    @Test
    void passesValidOutageEvent() {
        TelemetryPipelineContext ctx = validCtx();
        stage.process(ctx);

        assertThat(ctx.isValid()).isTrue();
        assertThat(ctx.getValidationErrors()).isEmpty();
    }

    @Test
    void invalidatesMissingDeviceId() {
        TelemetryPipelineContext ctx = validCtx();
        ctx.setDeviceId(null);
        stage.process(ctx);

        assertThat(ctx.isValid()).isFalse();
        assertThat(ctx.getValidationErrors()).anyMatch(e -> e.contains("deviceId"));
    }

    @Test
    void invalidatesMissingTimestamp() {
        TelemetryPipelineContext ctx = validCtx();
        ctx.setTimestamp(null);
        stage.process(ctx);

        assertThat(ctx.isValid()).isFalse();
        assertThat(ctx.getValidationErrors()).anyMatch(e -> e.contains("timestamp"));
    }

    @Test
    void invalidatesFutureTimestamp() {
        TelemetryPipelineContext ctx = validCtx();
        ctx.setTimestamp(Instant.now().plus(1, ChronoUnit.HOURS));
        stage.process(ctx);

        assertThat(ctx.isValid()).isFalse();
        assertThat(ctx.getValidationErrors()).anyMatch(e -> e.contains("future"));
    }

    @Test
    void invalidatesNegativeValue() {
        TelemetryPipelineContext ctx = validCtx();
        ctx.setValue(-1.0);
        stage.process(ctx);

        assertThat(ctx.isValid()).isFalse();
        assertThat(ctx.getValidationErrors()).anyMatch(e -> e.contains("non-negative"));
    }

    private TelemetryPipelineContext validCtx() {
        TelemetryPipelineContext ctx = new TelemetryPipelineContext("raw");
        ctx.setEventType(TelemetryEventType.OUTAGE);
        ctx.setDeviceId("DEV-001");
        ctx.setTimestamp(Instant.now().minus(1, ChronoUnit.MINUTES));
        ctx.setValue(100.0);
        return ctx;
    }
}
