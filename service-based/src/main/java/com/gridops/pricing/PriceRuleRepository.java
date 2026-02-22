package com.gridops.pricing;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Data layer for the Pricing service. Owned by this service only.
 */
public interface PriceRuleRepository {

    PricingService.PriceRule save(PricingService.PriceRule rule);

    List<PricingService.PriceRule> findByZoneId(String zoneId);

    Optional<Double> findEffectivePriceForZoneAt(String zoneId, Instant at);
}
