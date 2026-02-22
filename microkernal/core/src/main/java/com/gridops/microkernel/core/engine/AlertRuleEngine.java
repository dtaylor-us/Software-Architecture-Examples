package com.gridops.microkernel.core.engine;

import com.gridops.microkernel.core.alert.Alert;
import com.gridops.microkernel.core.contract.RulePlugin;
import com.gridops.microkernel.core.event.GridOpsEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Microkernel: orchestrates plugin discovery, lifecycle, and execution.
 * Aggregates alerts from all registered plugins.
 */
public final class AlertRuleEngine {

    private final List<RulePlugin> plugins = new CopyOnWriteArrayList<>();

    public void registerPlugin(RulePlugin plugin) {
        if (plugin == null) return;
        plugins.add(plugin);
        plugin.onLoad();
    }

    public void unregisterPlugin(RulePlugin plugin) {
        if (plugin == null) return;
        plugins.remove(plugin);
        plugin.onUnload();
    }

    /**
     * Unregister the plugin with the given id, if present.
     * @return true if a plugin was found and unregistered, false otherwise
     */
    public boolean unregisterPluginById(String pluginId) {
        if (pluginId == null || pluginId.isBlank()) return false;
        RulePlugin found = plugins.stream()
            .filter(p -> pluginId.equals(p.id()))
            .findFirst()
            .orElse(null);
        if (found != null) {
            unregisterPlugin(found);
            return true;
        }
        return false;
    }

    /** @return true if a plugin with this id is currently registered */
    public boolean isRegistered(String pluginId) {
        if (pluginId == null || pluginId.isBlank()) return false;
        return plugins.stream().anyMatch(p -> pluginId.equals(p.id()));
    }

    public List<RulePlugin> getPlugins() {
        return List.copyOf(plugins);
    }

    /**
     * Evaluate event against all registered plugins and aggregate alerts.
     */
    public EngineResult evaluate(GridOpsEvent event) {
        List<Alert> allAlerts = new ArrayList<>();
        List<String> pluginsFired = new ArrayList<>();

        for (RulePlugin plugin : plugins) {
            try {
                List<Alert> alerts = plugin.evaluate(event);
                if (alerts != null && !alerts.isEmpty()) {
                    allAlerts.addAll(alerts);
                    pluginsFired.add(plugin.id());
                }
            } catch (Exception e) {
                // Isolate plugin failure; could log and optionally add a system alert
                allAlerts.add(new com.gridops.microkernel.core.alert.Alert(
                    plugin.id(), "engine", "ERROR",
                    "Plugin evaluation failed: " + e.getMessage(),
                    java.time.Instant.now()));
                pluginsFired.add(plugin.id());
            }
        }

        return new EngineResult(event.getEventId(), allAlerts, pluginsFired);
    }

    public static final class EngineResult {
        private final String eventId;
        private final List<Alert> alerts;
        private final List<String> pluginsFired;

        public EngineResult(String eventId, List<Alert> alerts, List<String> pluginsFired) {
            this.eventId = eventId;
            this.alerts = List.copyOf(alerts);
            this.pluginsFired = List.copyOf(pluginsFired);
        }

        public String getEventId() { return eventId; }
        public List<Alert> getAlerts() { return alerts; }
        public List<String> getPluginsFired() { return pluginsFired; }
    }
}
