package com.gridops.pipeline.pipeline.stages;

import com.gridops.pipeline.domain.TelemetryPipelineContext;
import com.gridops.pipeline.pipeline.PipelineStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Stage 1 – Ingest.
 *
 * <p>Accepts raw input (string line or JSON blob) and performs basic
 * entry-gate checks: null/blank guard and whitespace trimming.
 * This stage is intentionally thin — real-world implementations would
 * apply rate-limiting, authentication, and source tagging here.
 */
@Component
@Order(1)
public class IngestStage implements PipelineStage {

    private static final Logger log = LoggerFactory.getLogger(IngestStage.class);

    @Override
    public void process(TelemetryPipelineContext context) {
        log.debug("[Ingest] Receiving raw input");

        if (context.getRawInput() == null || context.getRawInput().isBlank()) {
            context.addValidationError("Raw input must not be null or blank");
            log.warn("[Ingest] Rejected: empty input");
            return;
        }

        context.setRawInput(context.getRawInput().strip());
        log.debug("[Ingest] Accepted input (length={})", context.getRawInput().length());
    }

    @Override
    public String name() {
        return "Ingest";
    }
}
