package com.gridops.pipeline.pipeline.stages;

import com.gridops.pipeline.domain.TelemetryEventType;
import com.gridops.pipeline.domain.TelemetryPipelineContext;
import com.gridops.pipeline.pipeline.PipelineStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Stage 6 – Filter.
 *
 * <p>Silently drops events that should not be persisted or published.
 * Filtered events are not errors; they are legitimate no-ops.
 * The stage calls {@link TelemetryPipelineContext#markFiltered} with
 * a human-readable reason.
 *
 * <p>Current filter rules:
 * <ul>
 *   <li>UNKNOWN event type — unrecognised source, cannot be processed.</li>
 *   <li>Zero-value events — no measurable activity, treated as heartbeats.</li>
 * </ul>
 */
@Component
@Order(6)
public class FilterStage implements PipelineStage {

    private static final Logger log = LoggerFactory.getLogger(FilterStage.class);

    @Override
    public void process(TelemetryPipelineContext context) {
        log.debug("[Filter] Applying filter rules");

        if (context.getEventType() == TelemetryEventType.UNKNOWN) {
            context.markFiltered("UNKNOWN event type — no applicable processor");
            log.info("[Filter] Dropped event: {}", context.getFilterReason());
            return;
        }

        if (context.getValue() == 0.0) {
            context.markFiltered("Zero-value event — treated as a heartbeat, not persisted");
            log.info("[Filter] Dropped event: {}", context.getFilterReason());
        }
    }

    @Override
    public String name() {
        return "Filter";
    }
}
