package com.example.spacebased.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveAlert {

    private String alertId;
    private String nodeId;
    private String type;           // e.g. SPIKE, CONGESTION
    private BigDecimal priceMwh;
    private BigDecimal thresholdOrAverage;
    private Instant raisedAt;
    private long ttlSeconds;
}
