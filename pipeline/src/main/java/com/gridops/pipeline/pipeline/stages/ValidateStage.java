package com.gridops.pipeline.pipeline.stages;

import com.gridops.pipeline.domain.TelemetryPipelineContext;
import com.gridops.pipeline.pipeline.PipelineStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Stage 4 – Validate.
 *
 * <p>Enforces business rules that must hold before the event is enriched or
 * persisted.  Any violation calls {@link TelemetryPipelineContext#addValidationError},
 * which halts the main pipeline while still allowing the PublishSummary stage
 * to report the outcome.
 *
 * <p>Rules checked:
 * <ul>
 *   <li>Device ID must be present.</li>
 *   <li>Timestamp must be present and must not be in the future.</li>
 *   <li>Value must be non-negative.</li>
 * </ul>
 */
@Component
@Order(4)
public class ValidateStage implements PipelineStage {

    private static final Logger log = LoggerFactory.getLogger(ValidateStage.class);

    @Override
    public void process(TelemetryPipelineContext context) {
        log.debug("[Validate] Validating event");

        if (context.getDeviceId() == null || context.getDeviceId().isBlank()) {
            context.addValidationError("deviceId is required");
        }

        if (context.getTimestamp() == null) {
            context.addValidationError("timestamp is required");
        } else if (context.getTimestamp().isAfter(Instant.now())) {
            context.addValidationError("timestamp must not be in the future");
        }

        if (context.getValue() < 0) {
            context.addValidationError("value must be non-negative, got: " + context.getValue());
        }

        if (context.isValid()) {
            log.debug("[Validate] Event passed all checks");
        } else {
            log.warn("[Validate] Event failed validation: {}", context.getValidationErrors());
        }
    }

    @Override
    public String name() {
        return "Validate";
    }
}
