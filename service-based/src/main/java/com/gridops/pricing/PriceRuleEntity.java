package com.gridops.pricing;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "price_rules")
public class PriceRuleEntity {

    @Id
    private String id;
    @Column(nullable = false, length = 64)
    private String zoneId;
    @Column(nullable = false)
    private Instant effectiveFrom;
    @Column(nullable = false)
    private Instant effectiveTo;
    @Column(nullable = false)
    private double pricePerMwh;

    protected PriceRuleEntity() {}

    public PriceRuleEntity(String id, String zoneId, Instant effectiveFrom, Instant effectiveTo, double pricePerMwh) {
        this.id = id;
        this.zoneId = zoneId;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.pricePerMwh = pricePerMwh;
    }

    public static PriceRuleEntity from(PricingService.PriceRule r) {
        return new PriceRuleEntity(r.id(), r.zoneId(), r.effectiveFrom(), r.effectiveTo(), r.pricePerMwh());
    }

    public PricingService.PriceRule toRule() {
        return new PricingService.PriceRule(id, zoneId, effectiveFrom, effectiveTo, pricePerMwh);
    }

    public String getId() { return id; }
    public String getZoneId() { return zoneId; }
    public Instant getEffectiveFrom() { return effectiveFrom; }
    public Instant getEffectiveTo() { return effectiveTo; }
    public double getPricePerMwh() { return pricePerMwh; }
}
