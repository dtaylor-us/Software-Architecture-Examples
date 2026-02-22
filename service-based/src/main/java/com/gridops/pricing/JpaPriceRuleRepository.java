package com.gridops.pricing;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
@Primary
public class JpaPriceRuleRepository implements PriceRuleRepository {

    private final PriceRuleJpaRepository jpa;

    public JpaPriceRuleRepository(PriceRuleJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public PricingService.PriceRule save(PricingService.PriceRule rule) {
        PriceRuleEntity entity = PriceRuleEntity.from(rule);
        return jpa.save(entity).toRule();
    }

    @Override
    public List<PricingService.PriceRule> findByZoneId(String zoneId) {
        return jpa.findAllByZoneId(zoneId).stream()
                .map(PriceRuleEntity::toRule)
                .toList();
    }

    @Override
    public Optional<Double> findEffectivePriceForZoneAt(String zoneId, Instant at) {
        return jpa.findEffectiveForZoneAt(zoneId, at)
                .map(PriceRuleEntity::getPricePerMwh);
    }
}
