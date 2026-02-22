package com.example.spacebased.api;

import com.example.spacebased.dto.ActiveAlert;
import com.example.spacebased.dto.PriceUpdate;
import com.example.spacebased.space.EnergySpace;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Stateless API: all hot state lives in Redis (the "space").
 * POST /price-updates accepts bulk updates; GET /active-alerts reads from the space.
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
public class PriceUpdateController {

    private final EnergySpace energySpace;

    @PostMapping("/price-updates")
    public ResponseEntity<BulkPriceResponse> postPriceUpdates(@Valid @RequestBody List<PriceUpdate> updates) {
        if (updates == null || updates.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<ActiveAlert> raised = energySpace.writePriceUpdates(updates);
        return ResponseEntity.ok(BulkPriceResponse.builder()
            .accepted(updates.size())
            .alertsRaised(raised.size())
            .alerts(raised)
            .build());
    }

    @GetMapping("/active-alerts")
    public ResponseEntity<List<ActiveAlert>> getActiveAlerts() {
        return ResponseEntity.ok(energySpace.getActiveAlerts());
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "arch", "space-based"));
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BulkPriceResponse {
        private int accepted;
        private int alertsRaised;
        private List<ActiveAlert> alerts;
    }
}
