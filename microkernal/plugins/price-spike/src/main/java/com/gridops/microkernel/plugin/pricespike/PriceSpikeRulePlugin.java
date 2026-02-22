package com.gridops.microkernel.plugin.pricespike;

import com.gridops.microkernel.core.alert.Alert;
import com.gridops.microkernel.core.contract.RulePlugin;
import com.gridops.microkernel.core.event.GridOpsEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Fires when price exceeds a threshold (e.g. price spike in energy market).
 */
public final class PriceSpikeRulePlugin implements RulePlugin {

    public static final String ID = "price-spike";
    private static final double DEFAULT_THRESHOLD = 150.0;

    private final double threshold;

    public PriceSpikeRulePlugin() {
        this(DEFAULT_THRESHOLD);
    }

    public PriceSpikeRulePlugin(double threshold) {
        this.threshold = threshold;
    }

    @Override
    public String id() { return ID; }

    @Override
    public String name() { return "Price Spike Rule"; }

    @Override
    public List<Alert> evaluate(GridOpsEvent event) {
        List<Alert> alerts = new ArrayList<>();
        Double price = event.getPayloadValue("price", Double.class);
        if (price == null) return alerts;
        if (price >= threshold) {
            alerts.add(new Alert(
                ID,
                "price-spike-rule",
                "HIGH",
                "Price spike detected: " + price + " >= " + threshold,
                Instant.now()));
        }
        return alerts;
    }
}
