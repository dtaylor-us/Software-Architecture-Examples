package com.gridops.microkernel.host;

import com.gridops.microkernel.core.contract.RulePlugin;
import com.gridops.microkernel.core.discovery.PluginDiscovery;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Registry of valid plugin types that can be added at runtime.
 * Built from ServiceLoader: maps plugin id -> factory for creating new instances.
 */
public final class PluginRegistry {

    private final Map<String, Supplier<RulePlugin>> factories;

    public PluginRegistry() {
        this.factories = buildFactories();
    }

    private static Map<String, Supplier<RulePlugin>> buildFactories() {
        Map<String, Supplier<RulePlugin>> map = new HashMap<>();
        for (RulePlugin p : PluginDiscovery.discoverFromServiceLoader()) {
            if (!map.containsKey(p.id())) {
                Class<? extends RulePlugin> clazz = p.getClass();
                map.put(p.id(), () -> newInstance(clazz));
            }
        }
        return Collections.unmodifiableMap(map);
    }

    private static RulePlugin newInstance(Class<? extends RulePlugin> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot instantiate plugin " + clazz.getName(), e);
        }
    }

    /** Returns true if pluginId is a known (valid) plugin type. */
    public boolean isValidPluginId(String pluginId) {
        return pluginId != null && !pluginId.isBlank() && factories.containsKey(pluginId);
    }

    /** Returns the set of valid plugin ids that can be added. */
    public Set<String> getValidPluginIds() {
        return factories.keySet();
    }

    /** Creates a new plugin instance for the given id. Caller must validate id first. */
    public RulePlugin createPlugin(String pluginId) {
        Supplier<RulePlugin> factory = factories.get(pluginId);
        if (factory == null) {
            throw new IllegalArgumentException("Unknown plugin id: " + pluginId);
        }
        return factory.get();
    }
}
