package com.gridops.pipeline.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * JPA entity that represents a successfully processed telemetry event
 * stored in the {@code telemetry_events} table.
 */
@Entity
@Table(name = "telemetry_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelemetryEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TelemetryEventType type;

    private String deviceId;
    private String region;
    private Instant timestamp;

    @Column(name = "event_value")
    private double value;

    private String unit;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    @Column(name = "raw_input", length = 2000)
    private String rawInput;
}
