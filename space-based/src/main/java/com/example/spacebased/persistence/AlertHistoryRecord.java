package com.example.spacebased.persistence;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/** DTO for alert records in the Redis queue and for persistence to DB. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertHistoryRecord {

    private String alertId;
    private String nodeId;
    private String type;
    private BigDecimal priceMwh;
    private BigDecimal thresholdOrAverage;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "UTC")
    private Instant raisedAt;
}
