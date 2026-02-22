package com.gridops.pipeline.pipeline.stages;

import com.gridops.pipeline.domain.TelemetryPipelineContext;
import com.gridops.pipeline.domain.TelemetryEventType;
import com.gridops.pipeline.pipeline.PipelineStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Stage 5 – Enrich.
 *
 * <p>Attaches additional context that was not present in the raw event:
 * <ul>
 *   <li>Pipeline source/version metadata.</li>
 *   <li>Processing timestamp.</li>
 *   <li>Grid zone derived from the normalized region.</li>
 *   <li>Event-type-specific severity or category labels.</li>
 * </ul>
 *
 * <p>All enrichment data is stored in {@link TelemetryPipelineContext#getMetadata()}.
 * In a production system this stage would call external reference-data services.
 */
@Component
@Order(5)
public class EnrichStage implements PipelineStage {

    private static final Logger log = LoggerFactory.getLogger(EnrichStage.class);

    @Override
    public void process(TelemetryPipelineContext context) {
        log.debug("[Enrich] Enriching event");

        context.getMetadata().put("source", "GridOps-Pipeline");
        context.getMetadata().put("pipelineVersion", "1.0");
        context.getMetadata().put("processedAt", Instant.now().toString());

        enrichGridZone(context);
        enrichEventCategory(context);

        log.debug("[Enrich] Metadata keys: {}", context.getMetadata().keySet());
    }

    private void enrichGridZone(TelemetryPipelineContext context) {
        String region = context.getRegion();
        if (region == null) return;
        String zone = switch (region) {
            case "WEST"      -> "CAISO";
            case "EAST"      -> "PJM";
            case "NORTH"     -> "MISO-N";
            case "SOUTH"     -> "MISO-S";
            case "NORTHWEST" -> "BPA";
            case "SOUTHEAST" -> "SOCO";
            default          -> "UNKNOWN-ZONE";
        };
        context.getMetadata().put("gridZone", zone);
    }

    private void enrichEventCategory(TelemetryPipelineContext context) {
        if (context.getEventType() == TelemetryEventType.OUTAGE) {
            String severity = context.getValue() >= 1000 ? "MAJOR" : context.getValue() >= 100 ? "MINOR" : "MICRO";
            context.getMetadata().put("outageSeverity", severity);
        } else if (context.getEventType() == TelemetryEventType.PRICE) {
            String category = context.getValue() >= 100 ? "HIGH" : context.getValue() >= 50 ? "MEDIUM" : "LOW";
            context.getMetadata().put("priceCategory", category);
        } else if (context.getEventType() == TelemetryEventType.FORECAST) {
            String trend = context.getValue() >= 500 ? "PEAK" : context.getValue() >= 200 ? "SHOULDER" : "OFF_PEAK";
            context.getMetadata().put("forecastBand", trend);
        }
    }

    @Override
    public String name() {
        return "Enrich";
    }
}
