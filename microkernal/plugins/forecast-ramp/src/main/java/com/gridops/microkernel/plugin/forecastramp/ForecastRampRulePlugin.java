package com.gridops.microkernel.plugin.forecastramp;

import com.gridops.microkernel.core.alert.Alert;
import com.gridops.microkernel.core.contract.RulePlugin;
import com.gridops.microkernel.core.event.GridOpsEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Fires when forecast load ramp (delta) exceeds a threshold (e.g. rapid load increase).
 */
public final class ForecastRampRulePlugin implements RulePlugin {

    public static final String ID = "forecast-ramp";
    private static final double DEFAULT_RAMP_THRESHOLD_MW = 500.0;

    private final double rampThresholdMw;

    public ForecastRampRulePlugin() {
        this(DEFAULT_RAMP_THRESHOLD_MW);
    }

    public ForecastRampRulePlugin(double rampThresholdMw) {
        this.rampThresholdMw = rampThresholdMw;
    }

    @Override
    public String id() { return ID; }

    @Override
    public String name() { return "Forecast Ramp Rule"; }

    @Override
    public List<Alert> evaluate(GridOpsEvent event) {
        List<Alert> alerts = new ArrayList<>();
        if (!"forecast-ramp".equals(event.getEventType()) && !"load-forecast".equals(event.getEventType())) {
            return alerts;
        }
        Double rampMw = event.getPayloadValue("rampMw", Double.class);
        if (rampMw == null) rampMw = event.getPayloadValue("deltaLoadMw", Double.class);
        if (rampMw == null) return alerts;
        if (rampMw >= rampThresholdMw) {
            alerts.add(new Alert(
                ID,
                "forecast-ramp-rule",
                "MEDIUM",
                "Forecast ramp exceeds threshold: " + rampMw + " MW >= " + rampThresholdMw + " MW",
                Instant.now()));
        }
        return alerts;
    }
}
