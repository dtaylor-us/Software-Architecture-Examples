package com.gridops.pipeline.pipeline;

import com.gridops.pipeline.domain.TelemetryPipelineContext;

/**
 * Contract for a single stage in the GridOps telemetry processing pipeline.
 *
 * <p>Each stage receives the shared {@link TelemetryPipelineContext}, mutates it
 * (adds parsed fields, enrichment data, persisted ID, etc.), and returns control
 * to the pipeline runner.  A stage signals failure via
 * {@link TelemetryPipelineContext#addValidationError} and a deliberate drop via
 * {@link TelemetryPipelineContext#markFiltered}.
 *
 * <p>Stages are ordered by Spring's {@code @Order} annotation; the pipeline
 * runner collects all beans implementing this interface and executes them in
 * that declared order.
 */
public interface PipelineStage {

    /**
     * Execute this stage's logic against {@code context}.
     *
     * @param context the mutable event context flowing through the pipeline
     */
    void process(TelemetryPipelineContext context);

    /** Human-readable stage name used in logging and diagnostics. */
    String name();
}
