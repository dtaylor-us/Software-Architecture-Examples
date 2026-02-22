package com.gridops.api;

import com.gridops.pricing.PricingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pricing")
public class PricingController {

    private final PricingService pricingService;

    public PricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }

    @PostMapping("/rules")
    public ResponseEntity<PricingService.PriceRule> createRule(@Valid @RequestBody CreatePriceRuleRequest req) {
        PricingService.CreatePriceRuleCommand cmd = new PricingService.CreatePriceRuleCommand(
                req.zoneId(),
                req.effectiveFrom(),
                req.effectiveTo(),
                req.pricePerMwh()
        );
        return ResponseEntity.ok(pricingService.createRule(cmd));
    }

    @GetMapping("/rules")
    public List<PricingService.PriceRule> listRules(@RequestParam @NotBlank String zoneId) {
        return pricingService.listRulesForZone(zoneId);
    }

    @GetMapping("/price")
    public ResponseEntity<Map<String, Object>> getPrice(
            @RequestParam @NotBlank String zoneId,
            @RequestParam Instant at) {
        return pricingService.getPriceForZoneAt(zoneId, at)
                .map(price -> ResponseEntity.ok(Map.<String, Object>of(
                        "zoneId", zoneId,
                        "at", at.toString(),
                        "pricePerMwh", price
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    public record CreatePriceRuleRequest(String zoneId, Instant effectiveFrom, Instant effectiveTo, double pricePerMwh) {}
}
