package com.gridops.pricing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PricingService tested independently.
 */
class PricingServiceTest {

    private PricingService pricingService;

    @BeforeEach
    void setUp() {
        pricingService = new PricingServiceImpl(new InMemoryPriceRuleRepository());
    }

    @Test
    void createRule_returnsRuleWithId() {
        Instant from = Instant.parse("2025-02-20T00:00:00Z");
        Instant to = Instant.parse("2025-02-21T00:00:00Z");
        PricingService.CreatePriceRuleCommand cmd = new PricingService.CreatePriceRuleCommand(
                "ZONE-1", from, to, 45.50
        );
        PricingService.PriceRule rule = pricingService.createRule(cmd);

        assertThat(rule.id()).startsWith("PR-");
        assertThat(rule.zoneId()).isEqualTo("ZONE-1");
        assertThat(rule.pricePerMwh()).isEqualTo(45.50);
    }

    @Test
    void getPriceForZoneAt_returnsPriceWhenRuleCoversTime() {
        Instant from = Instant.parse("2025-02-20T00:00:00Z");
        Instant to = Instant.parse("2025-02-21T00:00:00Z");
        pricingService.createRule(new PricingService.CreatePriceRuleCommand("Z1", from, to, 100.0));

        assertThat(pricingService.getPriceForZoneAt("Z1", Instant.parse("2025-02-20T12:00:00Z")))
                .hasValue(100.0);
    }

    @Test
    void getPriceForZoneAt_returnsEmptyWhenNoRule() {
        assertThat(pricingService.getPriceForZoneAt("UNKNOWN", Instant.now())).isEmpty();
    }

    @Test
    void listRulesForZone_filtersByZone() {
        Instant from = Instant.now();
        Instant to = from.plusSeconds(86400);
        pricingService.createRule(new PricingService.CreatePriceRuleCommand("Z-A", from, to, 10));
        pricingService.createRule(new PricingService.CreatePriceRuleCommand("Z-B", from, to, 20));
        pricingService.createRule(new PricingService.CreatePriceRuleCommand("Z-A", from, to, 15));

        List<PricingService.PriceRule> forA = pricingService.listRulesForZone("Z-A");
        assertThat(forA).hasSize(2);
    }
}
