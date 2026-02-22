package com.gridops.pipeline.api;

import com.gridops.pipeline.domain.TelemetryEventEntity;
import com.gridops.pipeline.domain.TelemetryEventType;
import com.gridops.pipeline.domain.TelemetryPipelineContext;
import com.gridops.pipeline.pipeline.TelemetryPipeline;
import com.gridops.pipeline.repository.TelemetryEventRepository;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST façade that exposes the GridOps telemetry pipeline over HTTP.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code POST /api/telemetry/ingest}       — submit one raw event (string line or JSON blob)</li>
 *   <li>{@code POST /api/telemetry/ingest/batch}  — submit multiple raw events</li>
 *   <li>{@code GET  /api/telemetry/events}         — list all persisted events</li>
 *   <li>{@code GET  /api/telemetry/events/{type}}  — list events by type</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/telemetry")
@Validated
public class TelemetryController {

    private final TelemetryPipeline pipeline;
    private final TelemetryEventRepository repository;

    public TelemetryController(TelemetryPipeline pipeline, TelemetryEventRepository repository) {
        this.pipeline = pipeline;
        this.repository = repository;
    }

    /**
     * Ingest a single raw telemetry event.
     *
     * @param rawInput the raw event as a string body (JSON blob or pipe-delimited line)
     * @return 200 OK with pipeline result on success; 422 if invalid/filtered
     */
    @PostMapping(value = "/ingest", consumes = "text/plain")
    public ResponseEntity<PipelineResult> ingest(@RequestBody @NotBlank String rawInput) {
        TelemetryPipelineContext context = pipeline.process(rawInput);
        PipelineResult result = PipelineResult.from(context);
        return context.isValid() && !context.isFiltered()
                ? ResponseEntity.ok(result)
                : ResponseEntity.unprocessableEntity().body(result);
    }

    /**
     * Ingest a batch of raw telemetry events.
     *
     * @param inputs list of raw event strings
     * @return list of pipeline results in the same order as inputs
     */
    @PostMapping("/ingest/batch")
    public ResponseEntity<List<PipelineResult>> ingestBatch(@RequestBody List<String> inputs) {
        List<PipelineResult> results = inputs.stream()
                .map(pipeline::process)
                .map(PipelineResult::from)
                .toList();
        return ResponseEntity.ok(results);
    }

    /** List all persisted telemetry events. */
    @GetMapping("/events")
    public ResponseEntity<List<TelemetryEventEntity>> getEvents() {
        return ResponseEntity.ok(repository.findAll());
    }

    /** List persisted telemetry events filtered by type. */
    @GetMapping("/events/{type}")
    public ResponseEntity<List<TelemetryEventEntity>> getEventsByType(
            @PathVariable TelemetryEventType type) {
        return ResponseEntity.ok(repository.findByType(type));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Response DTO
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Summary of a single pipeline run returned to the caller.
     */
    public record PipelineResult(
            boolean success,
            boolean filtered,
            String eventType,
            String deviceId,
            Long persistedId,
            List<String> validationErrors,
            String filterReason
    ) {
        static PipelineResult from(TelemetryPipelineContext ctx) {
            return new PipelineResult(
                    ctx.isValid() && !ctx.isFiltered(),
                    ctx.isFiltered(),
                    ctx.getEventType().name(),
                    ctx.getDeviceId(),
                    ctx.getPersistedId(),
                    ctx.getValidationErrors(),
                    ctx.getFilterReason()
            );
        }
    }
}
