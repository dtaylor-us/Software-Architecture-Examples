package com.gridops.pipeline.pipeline.stages;

import com.gridops.pipeline.domain.TelemetryPipelineContext;
import com.gridops.pipeline.pipeline.PipelineStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Stage 8 – PublishSummary.
 *
 * <p>Always runs — even when upstream stages mark the context invalid or
 * filtered — so that every ingest attempt produces an observable outcome.
 *
 * <p>In this educational implementation the summary is published as a Spring
 * {@link ApplicationEvent} ({@link TelemetrySummaryEvent}) and logged at INFO
 * level.  A production system would fan the summary out to a message broker
 * (Kafka, EventBridge, etc.) or a metrics sink.
 */
@Component
@Order(8)
public class PublishSummaryStage implements PipelineStage {

    private static final Logger log = LoggerFactory.getLogger(PublishSummaryStage.class);

    private final ApplicationEventPublisher eventPublisher;

    public PublishSummaryStage(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void process(TelemetryPipelineContext context) {
        TelemetrySummaryEvent summary = new TelemetrySummaryEvent(
                this,
                context.getEventType().name(),
                context.getDeviceId(),
                context.isValid(),
                context.isFiltered(),
                context.getPersistedId(),
                context.getValidationErrors().isEmpty() ? null : String.join("; ", context.getValidationErrors()),
                context.getFilterReason()
        );

        eventPublisher.publishEvent(summary);

        log.info("[PublishSummary] type={} device={} valid={} filtered={} persistedId={}",
                context.getEventType(), context.getDeviceId(),
                context.isValid(), context.isFiltered(), context.getPersistedId());
    }

    @Override
    public String name() {
        return "PublishSummary";
    }

    /**
     * Spring application event carrying the processing outcome for a single
     * telemetry event.  Listeners can react to successes, failures, or filtered
     * events without coupling directly to the pipeline.
     */
    public static class TelemetrySummaryEvent extends org.springframework.context.ApplicationEvent {

        private final String eventType;
        private final String deviceId;
        private final boolean valid;
        private final boolean filtered;
        private final Long persistedId;
        private final String validationErrors;
        private final String filterReason;

        public TelemetrySummaryEvent(Object source, String eventType, String deviceId,
                                     boolean valid, boolean filtered, Long persistedId,
                                     String validationErrors, String filterReason) {
            super(source);
            this.eventType = eventType;
            this.deviceId = deviceId;
            this.valid = valid;
            this.filtered = filtered;
            this.persistedId = persistedId;
            this.validationErrors = validationErrors;
            this.filterReason = filterReason;
        }

        public String getEventType()       { return eventType; }
        public String getDeviceId()        { return deviceId; }
        public boolean isValid()           { return valid; }
        public boolean isFiltered()        { return filtered; }
        public Long getPersistedId()       { return persistedId; }
        public String getValidationErrors(){ return validationErrors; }
        public String getFilterReason()    { return filterReason; }
    }
}
