package com.gridops.pipeline.pipeline.stages;

import com.gridops.pipeline.domain.TelemetryPipelineContext;
import com.gridops.pipeline.pipeline.PipelineStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Stage 3 – Normalize.
 *
 * <p>Applies canonical transformations so that downstream stages work with
 * consistent, well-formed data regardless of how the producer formatted it:
 * <ul>
 *   <li>Region names are upper-cased and common abbreviations are expanded.</li>
 *   <li>Numeric values are rounded to two decimal places.</li>
 *   <li>Default units are assigned when the producer omitted them.</li>
 *   <li>DeviceId is upper-cased and trimmed.</li>
 * </ul>
 */
@Component
@Order(3)
public class NormalizeStage implements PipelineStage {

    private static final Logger log = LoggerFactory.getLogger(NormalizeStage.class);

    private static final Map<String, String> REGION_ALIASES = Map.of(
            "W", "WEST",
            "E", "EAST",
            "N", "NORTH",
            "S", "SOUTH",
            "NW", "NORTHWEST",
            "SE", "SOUTHEAST"
    );

    private static final Map<String, String> DEFAULT_UNITS = Map.of(
            "OUTAGE",   "count",
            "FORECAST", "MW",
            "PRICE",    "$/MWh"
    );

    @Override
    public void process(TelemetryPipelineContext context) {
        log.debug("[Normalize] Normalizing event");

        // Normalize region
        if (context.getRegion() != null) {
            String upper = context.getRegion().toUpperCase().strip();
            context.setRegion(REGION_ALIASES.getOrDefault(upper, upper));
        }

        // Normalize deviceId
        if (context.getDeviceId() != null) {
            context.setDeviceId(context.getDeviceId().toUpperCase().strip());
        }

        // Round value to 2 d.p.
        context.setValue(
                BigDecimal.valueOf(context.getValue())
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue()
        );

        // Assign default unit when absent
        if (context.getUnit() == null || context.getUnit().isBlank()) {
            String defaultUnit = DEFAULT_UNITS.get(context.getEventType().name());
            if (defaultUnit != null) {
                context.setUnit(defaultUnit);
            }
        }

        log.debug("[Normalize] region={}, value={}, unit={}", context.getRegion(), context.getValue(), context.getUnit());
    }

    @Override
    public String name() {
        return "Normalize";
    }
}
