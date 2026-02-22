package com.gridops.pricing;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Internal interface for the Pricing service. Owns price rules and computed prices.
 */
public interface PricingService {

    PriceRule createRule(CreatePriceRuleCommand command);

    Optional<Double> getPriceForZoneAt(String zoneId, Instant at);

    List<PriceRule> listRulesForZone(String zoneId);

    record CreatePriceRuleCommand(String zoneId, Instant effectiveFrom, Instant effectiveTo, double pricePerMwh) {}

    record PriceRule(String id, String zoneId, Instant effectiveFrom, Instant effectiveTo, double pricePerMwh) {}
}
