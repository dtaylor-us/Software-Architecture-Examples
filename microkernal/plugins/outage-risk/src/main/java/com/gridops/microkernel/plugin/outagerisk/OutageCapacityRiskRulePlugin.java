package com.gridops.microkernel.plugin.outagerisk;

import com.gridops.microkernel.core.alert.Alert;
import com.gridops.microkernel.core.contract.RulePlugin;
import com.gridops.microkernel.core.event.GridOpsEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Fires when reserve margin or outage-related capacity risk falls below threshold.
 */
public final class OutageCapacityRiskRulePlugin implements RulePlugin {

    public static final String ID = "outage-capacity-risk";
    private static final double DEFAULT_MIN_RESERVE_MARGIN_PCT = 15.0;

    private final double minReserveMarginPct;

    public OutageCapacityRiskRulePlugin() {
        this(DEFAULT_MIN_RESERVE_MARGIN_PCT);
    }

    public OutageCapacityRiskRulePlugin(double minReserveMarginPct) {
        this.minReserveMarginPct = minReserveMarginPct;
    }

    @Override
    public String id() { return ID; }

    @Override
    public String name() { return "Outage Capacity Risk Rule"; }

    @Override
    public List<Alert> evaluate(GridOpsEvent event) {
        List<Alert> alerts = new ArrayList<>();
        if (!"outage-risk".equals(event.getEventType()) && !"capacity".equals(event.getEventType())) {
            return alerts;
        }
        Double reserveMarginPct = event.getPayloadValue("reserveMarginPct", Double.class);
        if (reserveMarginPct == null) reserveMarginPct = event.getPayloadValue("reserveMargin", Double.class);
        if (reserveMarginPct == null) return alerts;
        if (reserveMarginPct < minReserveMarginPct) {
            alerts.add(new Alert(
                ID,
                "outage-capacity-risk-rule",
                "HIGH",
                "Reserve margin below threshold: " + reserveMarginPct + "% < " + minReserveMarginPct + "%",
                Instant.now()));
        }
        return alerts;
    }
}
