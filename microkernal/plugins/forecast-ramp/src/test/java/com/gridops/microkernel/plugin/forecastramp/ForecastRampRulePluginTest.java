package com.gridops.microkernel.plugin.forecastramp;

import com.gridops.microkernel.core.alert.Alert;
import com.gridops.microkernel.core.contract.RulePlugin;
import com.gridops.microkernel.core.contract.RulePluginContractTest;
import com.gridops.microkernel.core.event.GridOpsEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ForecastRampRulePluginTest extends RulePluginContractTest {

    @Override
    protected RulePlugin createPlugin() {
        return new ForecastRampRulePlugin();
    }

    @Override
    protected GridOpsEvent eventThatMayTrigger(RulePlugin plugin) {
        return new GridOpsEvent("e1", "forecast-ramp", Instant.now(), Map.of("rampMw", 600.0));
    }

    @Test
    void evaluate_rampBelowThreshold_noAlert() {
        ForecastRampRulePlugin plugin = new ForecastRampRulePlugin(500.0);
        GridOpsEvent event = new GridOpsEvent("e1", "forecast-ramp", Instant.now(), Map.of("rampMw", 300.0));
        List<Alert> alerts = plugin.evaluate(event);
        assertTrue(alerts.isEmpty());
    }

    @Test
    void evaluate_rampAtOrAboveThreshold_firesAlert() {
        ForecastRampRulePlugin plugin = new ForecastRampRulePlugin(500.0);
        GridOpsEvent event = new GridOpsEvent("e1", "forecast-ramp", Instant.now(), Map.of("rampMw", 500.0));
        List<Alert> alerts = plugin.evaluate(event);
        assertEquals(1, alerts.size());
        assertTrue(alerts.get(0).getMessage().contains("500"));
        assertEquals(ForecastRampRulePlugin.ID, alerts.get(0).getPluginId());
    }
}
