package com.gridops.microkernel.plugin.pricespike;

import com.gridops.microkernel.core.alert.Alert;
import com.gridops.microkernel.core.contract.RulePlugin;
import com.gridops.microkernel.core.contract.RulePluginContractTest;
import com.gridops.microkernel.core.event.GridOpsEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PriceSpikeRulePluginTest extends RulePluginContractTest {

    @Override
    protected RulePlugin createPlugin() {
        return new PriceSpikeRulePlugin();
    }

    @Override
    protected GridOpsEvent eventThatMayTrigger(RulePlugin plugin) {
        return new GridOpsEvent("e1", "price", Instant.now(), Map.of("price", 200.0));
    }

    @Test
    void evaluate_noPrice_noAlert() {
        PriceSpikeRulePlugin plugin = new PriceSpikeRulePlugin(150.0);
        GridOpsEvent event = new GridOpsEvent("e1", "other", Instant.now(), Map.of("other", 1));
        List<Alert> alerts = plugin.evaluate(event);
        assertTrue(alerts.isEmpty());
    }

    @Test
    void evaluate_priceBelowThreshold_noAlert() {
        PriceSpikeRulePlugin plugin = new PriceSpikeRulePlugin(150.0);
        GridOpsEvent event = new GridOpsEvent("e1", "price", Instant.now(), Map.of("price", 100.0));
        List<Alert> alerts = plugin.evaluate(event);
        assertTrue(alerts.isEmpty());
    }

    @Test
    void evaluate_priceAtOrAboveThreshold_firesAlert() {
        PriceSpikeRulePlugin plugin = new PriceSpikeRulePlugin(150.0);
        GridOpsEvent event = new GridOpsEvent("e1", "price", Instant.now(), Map.of("price", 150.0));
        List<Alert> alerts = plugin.evaluate(event);
        assertEquals(1, alerts.size());
        assertTrue(alerts.get(0).getMessage().contains("150"));
        assertEquals(PriceSpikeRulePlugin.ID, alerts.get(0).getPluginId());
    }
}
