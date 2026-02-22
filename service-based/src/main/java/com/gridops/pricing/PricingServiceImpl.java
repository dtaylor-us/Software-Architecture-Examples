package com.gridops.pricing;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PricingServiceImpl implements PricingService {

    private final PriceRuleRepository repository;

    public PricingServiceImpl(PriceRuleRepository repository) {
        this.repository = repository;
    }

    @Override
    public PriceRule createRule(CreatePriceRuleCommand command) {
        String id = "PR-" + UUID.randomUUID().toString().substring(0, 8);
        PriceRule rule = new PriceRule(
                id,
                command.zoneId(),
                command.effectiveFrom(),
                command.effectiveTo(),
                command.pricePerMwh()
        );
        return repository.save(rule);
    }

    @Override
    public Optional<Double> getPriceForZoneAt(String zoneId, Instant at) {
        return repository.findEffectivePriceForZoneAt(zoneId, at);
    }

    @Override
    public List<PriceRule> listRulesForZone(String zoneId) {
        return repository.findByZoneId(zoneId);
    }
}
