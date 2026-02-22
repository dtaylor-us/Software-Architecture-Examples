package com.gridops.microkernel.core.discovery;

import com.gridops.microkernel.core.contract.RulePlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Discovers RulePlugin implementations via Java ServiceLoader (META-INF/services).
 *
 * Tradeoffs:
 * - ServiceLoader: zero config in code, add JAR to classpath + META-INF/services file;
 *   good for optional plugins and true "drop-in" deployment. Discovery is lazy and
 *   classpath-based; harder to test in isolation and to support dynamic load/unload.
 * - Explicit registry: host app (or config) explicitly registers plugin instances;
 *   full control over which plugins run, easier testing and version gating. No
 *   META-INF needed; plugins can be Spring beans or constructed from config.
 *
 * This class supports ServiceLoader-based discovery; host-app can also use
 * explicit registration (e.g. inject plugin beans into the engine).
 */
public final class PluginDiscovery {

    public static List<RulePlugin> discoverFromServiceLoader() {
        List<RulePlugin> found = new ArrayList<>();
        ServiceLoader<RulePlugin> loader = ServiceLoader.load(RulePlugin.class);
        for (RulePlugin plugin : loader) {
            found.add(plugin);
        }
        return found;
    }
}
