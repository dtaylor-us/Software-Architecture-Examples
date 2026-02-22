package com.gridops.pipeline.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Mutable context object that travels through every pipeline stage.
 *
 * <p>Each stage may read from and write to this context.  Stages signal
 * a hard failure by calling {@link #addValidationError} (sets {@code valid=false})
 * or a soft skip by calling {@link #markFiltered}.
 */
@Getter
@Setter
public class TelemetryPipelineContext {

    // ── raw input ────────────────────────────────────────────────────────────
    private String rawInput;

    // ── parsed fields ────────────────────────────────────────────────────────
    private TelemetryEventType eventType = TelemetryEventType.UNKNOWN;
    private String deviceId;
    private String region;
    private Instant timestamp;
    private double value;
    private String unit;

    // ── enrichment bag ───────────────────────────────────────────────────────
    private final Map<String, Object> metadata = new LinkedHashMap<>();

    // ── pipeline control ─────────────────────────────────────────────────────
    private boolean valid = true;
    private final List<String> validationErrors = new ArrayList<>();
    private boolean filtered = false;
    private String filterReason;

    // ── persistence result ───────────────────────────────────────────────────
    private Long persistedId;

    public TelemetryPipelineContext(String rawInput) {
        this.rawInput = rawInput;
    }

    /** Mark this context invalid and record the reason. */
    public void addValidationError(String error) {
        this.validationErrors.add(error);
        this.valid = false;
    }

    /** Mark this event as filtered (silently dropped — not an error). */
    public void markFiltered(String reason) {
        this.filtered = true;
        this.filterReason = reason;
    }
}
