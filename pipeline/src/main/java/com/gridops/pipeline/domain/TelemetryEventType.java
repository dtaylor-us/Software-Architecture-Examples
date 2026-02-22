package com.gridops.pipeline.domain;

/**
 * Supported telemetry event types produced by the GridOps grid.
 */
public enum TelemetryEventType {
    /** Power outage reported by a grid device. */
    OUTAGE,
    /** Energy demand/generation forecast submitted by a forecasting system. */
    FORECAST,
    /** Real-time electricity price update from a market node. */
    PRICE,
    /** Type could not be determined from the raw input. */
    UNKNOWN
}
