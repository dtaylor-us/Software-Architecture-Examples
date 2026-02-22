package com.gridops.pricing;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/** In-memory implementation for unit tests; app uses {@link JpaPriceRuleRepository}. */
public class InMemoryPriceRuleRepository implements PriceRuleRepository {

    private final List<PricingService.PriceRule> store = new CopyOnWriteArrayList<>();

    @Override
    public PricingService.PriceRule save(PricingService.PriceRule rule) {
        store.add(rule);
        return rule;
    }

    @Override
    public List<PricingService.PriceRule> findByZoneId(String zoneId) {
        return store.stream()
                .filter(r -> zoneId.equals(r.zoneId()))
                .toList();
    }

    @Override
    public Optional<Double> findEffectivePriceForZoneAt(String zoneId, Instant at) {
        return store.stream()
                .filter(r -> zoneId.equals(r.zoneId())
                        && !at.isBefore(r.effectiveFrom())
                        && at.isBefore(r.effectiveTo()))
                .findFirst()
                .map(PricingService.PriceRule::pricePerMwh);
    }
}
