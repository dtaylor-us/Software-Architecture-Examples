package com.gridops.pipeline.repository;

import com.gridops.pipeline.domain.TelemetryEventEntity;
import com.gridops.pipeline.domain.TelemetryEventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository for persisted telemetry events.
 */
public interface TelemetryEventRepository extends JpaRepository<TelemetryEventEntity, Long> {

    /** Returns all events of the given type. */
    List<TelemetryEventEntity> findByType(TelemetryEventType type);

    /** Returns all events from the given grid region. */
    List<TelemetryEventEntity> findByRegion(String region);
}
