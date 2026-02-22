package com.gridops.microkernel.core.engine;

import com.gridops.microkernel.core.alert.Alert;
import com.gridops.microkernel.core.contract.RulePlugin;
import com.gridops.microkernel.core.event.GridOpsEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AlertRuleEngineTest {

    private AlertRuleEngine engine;

    @BeforeEach
    void setUp() {
        engine = new AlertRuleEngine();
    }

    @Test
    void evaluate_emptyPlugins_returnsNoAlerts() {
        GridOpsEvent event = new GridOpsEvent("e1", "price", Instant.now(), Map.of("price", 100.0));
        AlertRuleEngine.EngineResult result = engine.evaluate(event);
        assertEquals("e1", result.getEventId());
        assertTrue(result.getAlerts().isEmpty());
        assertTrue(result.getPluginsFired().isEmpty());
    }

    @Test
    void evaluate_withPlugin_aggregatesAlertsAndPluginsFired() {
        engine.registerPlugin(new RulePlugin() {
            @Override public String id() { return "test-plugin"; }
            @Override public String name() { return "Test"; }
            @Override
            public List<Alert> evaluate(GridOpsEvent ev) {
                return List.of(
                    new Alert("test-plugin", "r1", "HIGH", "Test alert", Instant.now())
                );
            }
        });
        GridOpsEvent event = new GridOpsEvent("e1", "x", Instant.now(), Map.of());
        AlertRuleEngine.EngineResult result = engine.evaluate(event);
        assertEquals(1, result.getAlerts().size());
        assertEquals("Test alert", result.getAlerts().get(0).getMessage());
        assertEquals(1, result.getPluginsFired().size());
        assertEquals("test-plugin", result.getPluginsFired().get(0));
    }

    @Test
    void getPlugins_returnsRegisteredPlugins() {
        RulePlugin p = new RulePlugin() {
            @Override public String id() { return "p1"; }
            @Override public String name() { return "P1"; }
            @Override public List<Alert> evaluate(GridOpsEvent ev) { return List.of(); }
        };
        engine.registerPlugin(p);
        List<RulePlugin> list = engine.getPlugins();
        assertEquals(1, list.size());
        assertEquals("p1", list.get(0).id());
    }
}
