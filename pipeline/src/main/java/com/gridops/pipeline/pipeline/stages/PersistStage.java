package com.gridops.pipeline.pipeline.stages;

import com.gridops.pipeline.domain.TelemetryEventEntity;
import com.gridops.pipeline.domain.TelemetryPipelineContext;
import com.gridops.pipeline.pipeline.PipelineStage;
import com.gridops.pipeline.repository.TelemetryEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Stage 7 – Persist.
 *
 * <p>Saves the normalized and enriched event to the {@code telemetry_events}
 * table via Spring Data JPA and writes the generated primary key back to the
 * context for use by the final stage.
 */
@Component
@Order(7)
public class PersistStage implements PipelineStage {

    private static final Logger log = LoggerFactory.getLogger(PersistStage.class);

    private final TelemetryEventRepository repository;

    public PersistStage(TelemetryEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public void process(TelemetryPipelineContext context) {
        log.debug("[Persist] Saving event to database");

        TelemetryEventEntity entity = TelemetryEventEntity.builder()
                .type(context.getEventType())
                .deviceId(context.getDeviceId())
                .region(context.getRegion())
                .timestamp(context.getTimestamp())
                .value(context.getValue())
                .unit(context.getUnit())
                .processedAt(Instant.now())
                .rawInput(context.getRawInput())
                .build();

        TelemetryEventEntity saved = repository.save(entity);
        context.setPersistedId(saved.getId());

        log.info("[Persist] Event persisted with id={}", saved.getId());
    }

    @Override
    public String name() {
        return "Persist";
    }
}
