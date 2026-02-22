package com.gridops.microkernel.core.contract;

import com.gridops.microkernel.core.alert.Alert;
import com.gridops.microkernel.core.event.GridOpsEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Contract tests for RulePlugin (v1). Plugins must satisfy these behaviors;
 * run the same tests in each plugin module to ensure compatibility.
 */
public abstract class RulePluginContractTest {

    /** Subclass returns the plugin under test. */
    protected abstract RulePlugin createPlugin();

    @Test
    void id_isNonBlank() {
        RulePlugin plugin = createPlugin();
        assertNotNull(plugin.id());
        assertFalse(plugin.id().isBlank());
    }

    @Test
    void name_isNonBlank() {
        RulePlugin plugin = createPlugin();
        assertNotNull(plugin.name());
        assertFalse(plugin.name().isBlank());
    }

    @Test
    void contractVersion_isV1() {
        RulePlugin plugin = createPlugin();
        assertEquals("1", plugin.contractVersion());
    }

    @Test
    void evaluate_neverReturnsNull() {
        RulePlugin plugin = createPlugin();
        GridOpsEvent event = new GridOpsEvent("e1", "test", Instant.now(), Map.of());
        List<Alert> result = plugin.evaluate(event);
        assertNotNull(result);
    }

    @Test
    void evaluate_doesNotThrow() {
        RulePlugin plugin = createPlugin();
        GridOpsEvent event = new GridOpsEvent("e1", "test", Instant.now(), Map.of("key", "value"));
        assertDoesNotThrow(() -> plugin.evaluate(event));
    }

    @Test
    void alerts_fromEvaluate_havePluginId() {
        RulePlugin plugin = createPlugin();
        GridOpsEvent event = eventThatMayTrigger(plugin);
        List<Alert> alerts = plugin.evaluate(event);
        for (Alert a : alerts) {
            assertEquals(plugin.id(), a.getPluginId());
            assertNotNull(a.getRuleId());
            assertNotNull(a.getSeverity());
            assertNotNull(a.getMessage());
        }
    }

    /** Override to provide an event that might trigger the plugin; default minimal event. */
    protected GridOpsEvent eventThatMayTrigger(RulePlugin plugin) {
        return new GridOpsEvent("e1", "test", Instant.now(), Map.of());
    }
}
