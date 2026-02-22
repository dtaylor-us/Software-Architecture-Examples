package com.example.spacebased.persistence;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "alert_history", indexes = {
    @Index(name = "idx_alert_history_node_raised", columnList = "node_id, raised_at"),
    @Index(name = "idx_alert_history_raised", columnList = "raised_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 256)
    private String alertId;

    @Column(name = "node_id", nullable = false, length = 64)
    private String nodeId;

    @Column(nullable = false, length = 32)
    private String type;

    @Column(name = "price_mwh", precision = 19, scale = 4, nullable = false)
    private BigDecimal priceMwh;

    @Column(name = "threshold_or_average", precision = 19, scale = 4)
    private BigDecimal thresholdOrAverage;

    @Column(name = "raised_at", nullable = false)
    private Instant raisedAt;

    @Column(name = "persisted_at", nullable = false)
    private Instant persistedAt;

    public static AlertHistoryEntity fromRecord(AlertHistoryRecord record) {
        return AlertHistoryEntity.builder()
            .alertId(record.getAlertId())
            .nodeId(record.getNodeId())
            .type(record.getType())
            .priceMwh(record.getPriceMwh())
            .thresholdOrAverage(record.getThresholdOrAverage())
            .raisedAt(record.getRaisedAt())
            .persistedAt(Instant.now())
            .build();
    }
}
