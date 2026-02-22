package com.gridops.microkernel.core.contract;

import com.gridops.microkernel.core.alert.Alert;
import com.gridops.microkernel.core.event.GridOpsEvent;

import java.util.List;

/**
 * Versioned plugin contract (v1). All rule strategy plugins implement this.
 * The microkernel discovers and invokes plugins without knowing their domain logic.
 */
public interface RulePlugin {

    /** Contract version for compatibility checks. */
    String CONTRACT_VERSION = "1";

    /** Unique plugin identifier (e.g. "price-spike", "forecast-ramp"). */
    String id();

    /** Human-readable name. */
    String name();

    /** Contract version this plugin implements. */
    default String contractVersion() {
        return CONTRACT_VERSION;
    }

    /**
     * Evaluate the event and return zero or more alerts.
     * Must not throw; return empty list on no match or error.
     */
    List<Alert> evaluate(GridOpsEvent event);

    /** Lifecycle: called when plugin is registered. Default no-op. */
    default void onLoad() {}

    /** Lifecycle: called when plugin is unregistered. Default no-op. */
    default void onUnload() {}
}
