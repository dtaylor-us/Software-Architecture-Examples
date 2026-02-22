package com.gridops.microkernel.plugin.outagerisk;

import com.gridops.microkernel.core.alert.Alert;
import com.gridops.microkernel.core.contract.RulePlugin;
import com.gridops.microkernel.core.contract.RulePluginContractTest;
import com.gridops.microkernel.core.event.GridOpsEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OutageCapacityRiskRulePluginTest extends RulePluginContractTest {

    @Override
    protected RulePlugin createPlugin() {
        return new OutageCapacityRiskRulePlugin();
    }

    @Override
    protected GridOpsEvent eventThatMayTrigger(RulePlugin plugin) {
        return new GridOpsEvent("e1", "outage-risk", Instant.now(), Map.of("reserveMarginPct", 10.0));
    }

    @Test
    void evaluate_reserveAboveThreshold_noAlert() {
        OutageCapacityRiskRulePlugin plugin = new OutageCapacityRiskRulePlugin(15.0);
        GridOpsEvent event = new GridOpsEvent("e1", "outage-risk", Instant.now(), Map.of("reserveMarginPct", 20.0));
        List<Alert> alerts = plugin.evaluate(event);
        assertTrue(alerts.isEmpty());
    }

    @Test
    void evaluate_reserveBelowThreshold_firesAlert() {
        OutageCapacityRiskRulePlugin plugin = new OutageCapacityRiskRulePlugin(15.0);
        GridOpsEvent event = new GridOpsEvent("e1", "outage-risk", Instant.now(), Map.of("reserveMarginPct", 12.0));
        List<Alert> alerts = plugin.evaluate(event);
        assertEquals(1, alerts.size());
        assertTrue(alerts.get(0).getMessage().contains("12"));
        assertEquals(OutageCapacityRiskRulePlugin.ID, alerts.get(0).getPluginId());
    }
}
