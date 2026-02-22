package com.gridops.pipeline.pipeline;

import com.gridops.pipeline.domain.TelemetryPipelineContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Assembles and executes the ordered list of {@link PipelineStage} beans.
 *
 * <p>Spring injects all {@link PipelineStage} implementations in the order
 * declared by their {@code @Order} annotations, producing the processing chain:
 * <pre>
 *   Ingest(1) → Parse(2) → Normalize(3) → Validate(4) →
 *   Enrich(5) → Filter(6) → Persist(7) → PublishSummary(8)
 * </pre>
 *
 * <p>The runner stops the main chain as soon as the context is invalid or
 * filtered, but <em>always</em> invokes the final {@code PublishSummary} stage
 * so that every ingest attempt produces an observable outcome.
 */
@Component
public class TelemetryPipeline {

    private static final Logger log = LoggerFactory.getLogger(TelemetryPipeline.class);

    private final List<PipelineStage> stages;

    public TelemetryPipeline(List<PipelineStage> stages) {
        this.stages = stages;
    }

    /**
     * Process a single raw input string through all pipeline stages.
     *
     * @param rawInput the raw event string (JSON blob or pipe-delimited line)
     * @return the context after all applicable stages have run
     */
    public TelemetryPipelineContext process(String rawInput) {
        TelemetryPipelineContext context = new TelemetryPipelineContext(rawInput);

        log.info("[Pipeline] Starting — input length={}", rawInput == null ? 0 : rawInput.length());

        List<PipelineStage> mainStages = stages.subList(0, stages.size() - 1);
        PipelineStage summaryStage    = stages.get(stages.size() - 1);

        for (PipelineStage stage : mainStages) {
            if (!context.isValid() || context.isFiltered()) {
                log.debug("[Pipeline] Skipping '{}' (halted)", stage.name());
                continue;
            }
            log.debug("[Pipeline] → {}", stage.name());
            stage.process(context);
        }

        // PublishSummary always runs
        log.debug("[Pipeline] → {}", summaryStage.name());
        summaryStage.process(context);

        log.info("[Pipeline] Done — valid={} filtered={} persistedId={}",
                context.isValid(), context.isFiltered(), context.getPersistedId());

        return context;
    }
}
