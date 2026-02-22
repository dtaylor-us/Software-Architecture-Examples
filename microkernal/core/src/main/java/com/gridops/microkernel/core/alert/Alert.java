package com.gridops.microkernel.core.alert;

import java.time.Instant;

/**
 * Single alert produced by a rule plugin. Immutable.
 */
public final class Alert {

    private final String pluginId;
    private final String ruleId;
    private final String severity;
    private final String message;
    private final Instant raisedAt;

    public Alert(String pluginId, String ruleId, String severity, String message, Instant raisedAt) {
        this.pluginId = pluginId;
        this.ruleId = ruleId;
        this.severity = severity;
        this.message = message;
        this.raisedAt = raisedAt != null ? raisedAt : Instant.now();
    }

    public String getPluginId() { return pluginId; }
    public String getRuleId() { return ruleId; }
    public String getSeverity() { return severity; }
    public String getMessage() { return message; }
    public Instant getRaisedAt() { return raisedAt; }
}
