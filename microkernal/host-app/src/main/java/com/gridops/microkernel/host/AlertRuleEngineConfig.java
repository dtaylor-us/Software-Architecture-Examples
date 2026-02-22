package com.gridops.microkernel.host;

import com.gridops.microkernel.core.contract.RulePlugin;
import com.gridops.microkernel.core.discovery.PluginDiscovery;
import com.gridops.microkernel.core.engine.AlertRuleEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configures the Alert Rule Engine and discovers plugins.
 * Uses ServiceLoader by default; all plugin JARs on classpath are picked up via META-INF/services.
 */
@Configuration
public class AlertRuleEngineConfig {

    @Bean
    public PluginRegistry pluginRegistry() {
        return new PluginRegistry();
    }

    @Bean
    public AlertRuleEngine alertRuleEngine() {
        AlertRuleEngine engine = new AlertRuleEngine();
        List<RulePlugin> plugins = PluginDiscovery.discoverFromServiceLoader();
        plugins.forEach(engine::registerPlugin);
        return engine;
    }
}
