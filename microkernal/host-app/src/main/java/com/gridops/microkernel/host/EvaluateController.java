package com.gridops.microkernel.host;

import com.gridops.microkernel.core.alert.Alert;
import com.gridops.microkernel.core.contract.RulePlugin;
import com.gridops.microkernel.core.engine.AlertRuleEngine;
import com.gridops.microkernel.core.event.GridOpsEvent;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST API: evaluate events, list/add/remove plugins.
 */
@RestController
@RequestMapping("/api")
public class EvaluateController {

    private final AlertRuleEngine engine;
    private final PluginRegistry pluginRegistry;

    public EvaluateController(AlertRuleEngine engine, PluginRegistry pluginRegistry) {
        this.engine = engine;
        this.pluginRegistry = pluginRegistry;
    }

    @PostMapping("/evaluate")
    public ResponseEntity<EvaluateResponse> evaluate(@RequestBody EvaluateRequest request) {
        String eventId = request.getEventId() != null ? request.getEventId() : "evt-" + System.currentTimeMillis();
        String eventType = request.getEventType() != null ? request.getEventType() : "unknown";
        Instant ts = parseTimestamp(request.getTimestamp());
        Map<String, Object> payload = request.getPayload() != null ? request.getPayload() : Map.of();

        GridOpsEvent event = new GridOpsEvent(eventId, eventType, ts, payload);
        AlertRuleEngine.EngineResult result = engine.evaluate(event);

        List<AlertDto> alertDtos = result.getAlerts().stream()
            .map(a -> new AlertDto(
                a.getPluginId(),
                a.getRuleId(),
                a.getSeverity(),
                a.getMessage(),
                a.getRaisedAt().toString()))
            .collect(Collectors.toList());

        return ResponseEntity.ok(new EvaluateResponse(
            result.getEventId(),
            alertDtos,
            result.getPluginsFired()));
    }

    @GetMapping("/plugins")
    public ResponseEntity<PluginsResponse> listPlugins() {
        List<PluginInfo> infos = engine.getPlugins().stream()
            .map(p -> new PluginInfo(p.id(), p.name(), p.contractVersion()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(new PluginsResponse(infos));
    }

    /**
     * Add a plugin at runtime by id. Validates that the id is a known plugin type
     * and that it is not already registered (one instance per id).
     */
    @PostMapping("/plugins")
    public ResponseEntity<?> addPlugin(@RequestBody AddPluginRequest request) {
        String pluginId = request != null ? request.getPluginId() : null;
        if (pluginId == null || pluginId.isBlank()) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("pluginId is required"));
        }
        if (!pluginRegistry.isValidPluginId(pluginId)) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse(
                    "Invalid plugin id: '" + pluginId + "'. Valid ids: " + pluginRegistry.getValidPluginIds()));
        }
        if (engine.isRegistered(pluginId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("Plugin already registered: " + pluginId));
        }
        RulePlugin plugin = pluginRegistry.createPlugin(pluginId);
        engine.registerPlugin(plugin);
        PluginInfo info = new PluginInfo(plugin.id(), plugin.name(), plugin.contractVersion());
        return ResponseEntity.status(HttpStatus.CREATED).body(info);
    }

    /**
     * Remove a plugin at runtime by id. Validates that the plugin is currently registered.
     */
    @DeleteMapping("/plugins/{pluginId}")
    public ResponseEntity<?> removePlugin(@PathVariable("pluginId") String pluginId) {
        if (pluginId == null || pluginId.isBlank()) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("pluginId is required"));
        }
        if (!engine.unregisterPluginById(pluginId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("Plugin not registered: " + pluginId));
        }
        return ResponseEntity.noContent().build();
    }

    private static Instant parseTimestamp(String s) {
        if (s == null || s.isBlank()) return Instant.now();
        try {
            return Instant.parse(s);
        } catch (Exception e) {
            return Instant.now();
        }
    }

    public static final class EvaluateResponse {
        private final String eventId;
        private final List<AlertDto> alerts;
        private final List<String> pluginsFired;

        public EvaluateResponse(String eventId, List<AlertDto> alerts, List<String> pluginsFired) {
            this.eventId = eventId;
            this.alerts = alerts;
            this.pluginsFired = pluginsFired;
        }
        public String getEventId() { return eventId; }
        public List<AlertDto> getAlerts() { return alerts; }
        public List<String> getPluginsFired() { return pluginsFired; }
    }

    public static final class AlertDto {
        private final String pluginId;
        private final String ruleId;
        private final String severity;
        private final String message;
        private final String raisedAt;

        public AlertDto(String pluginId, String ruleId, String severity, String message, String raisedAt) {
            this.pluginId = pluginId;
            this.ruleId = ruleId;
            this.severity = severity;
            this.message = message;
            this.raisedAt = raisedAt;
        }
        public String getPluginId() { return pluginId; }
        public String getRuleId() { return ruleId; }
        public String getSeverity() { return severity; }
        public String getMessage() { return message; }
        public String getRaisedAt() { return raisedAt; }
    }

    public static final class PluginInfo {
        private final String id;
        private final String name;
        private final String contractVersion;

        public PluginInfo(String id, String name, String contractVersion) {
            this.id = id;
            this.name = name;
            this.contractVersion = contractVersion;
        }
        public String getId() { return id; }
        public String getName() { return name; }
        public String getContractVersion() { return contractVersion; }
    }

    public static final class PluginsResponse {
        private final List<PluginInfo> plugins;

        public PluginsResponse(List<PluginInfo> plugins) {
            this.plugins = plugins;
        }
        public List<PluginInfo> getPlugins() { return plugins; }
    }

    public static final class AddPluginRequest {
        private String pluginId;
        public String getPluginId() { return pluginId; }
        public void setPluginId(String pluginId) { this.pluginId = pluginId; }
    }

    public static final class ErrorResponse {
        private final String error;
        public ErrorResponse(String error) { this.error = error; }
        public String getError() { return error; }
    }
}
