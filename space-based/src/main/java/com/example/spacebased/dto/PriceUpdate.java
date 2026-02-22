package com.example.spacebased.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceUpdate {

    @NotBlank
    private String nodeId;

    @NotNull
    @Positive
    private BigDecimal priceMwh;

    private long timestampMs;
}
