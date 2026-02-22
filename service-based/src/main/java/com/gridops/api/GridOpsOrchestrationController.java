package com.gridops.api;

import com.gridops.alert.AlertService;
import com.gridops.audit.AuditService;
import com.gridops.forecast.ForecastService;
import com.gridops.outage.OutageService;
import com.gridops.pricing.PricingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Orchestrated flow: combines multiple services in-process to demonstrate
 * service-based boundaries while keeping a single deployable.
 */
@RestController
@RequestMapping("/api/orchestrate")
public class GridOpsOrchestrationController {

    private final OutageService outageService;
    private final ForecastService forecastService;
    private final PricingService pricingService;
    private final AlertService alertService;
    private final AuditService auditService;

    public GridOpsOrchestrationController(OutageService outageService,
                                          ForecastService forecastService,
                                          PricingService pricingService,
                                          AlertService alertService,
                                          AuditService auditService) {
        this.outageService = outageService;
        this.forecastService = forecastService;
        this.pricingService = pricingService;
        this.alertService = alertService;
        this.auditService = auditService;
    }

    /**
     * Sample flow: report outage, raise alert, audit, and return a summary.
     * All calls are in-process (no HTTP between services).
     */
    @PostMapping("/outage-with-alert")
    public ResponseEntity<Map<String, Object>> reportOutageWithAlert(
            @RequestParam String assetId,
            @RequestParam String description,
            @RequestParam(defaultValue = "MEDIUM") String severity) {

        OutageService.ReportOutageCommand cmd = new OutageService.ReportOutageCommand(
                assetId, description, Instant.now(), severity);
        OutageService.OutageRecord outage = outageService.reportOutage(cmd);

        AlertService.AlertRecord alert = alertService.raiseAlert(
                new AlertService.RaiseAlertCommand("OUTAGE", "Outage: " + description, severity, outage.id()));

        auditService.record(new AuditService.AuditCommand(
                "orchestrate", "REPORT_OUTAGE", "Outage", outage.id(), "asset=" + assetId));

        Map<String, Object> summary = new HashMap<>();
        summary.put("outage", outage);
        summary.put("alert", alert);
        summary.put("message", "Outage reported, alert raised, and audit recorded (in-process).");
        return ResponseEntity.ok(summary);
    }

    /**
     * Dashboard-style summary from all services (in-process reads).
     */
    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        Map<String, Object> out = new HashMap<>();
        out.put("activeOutages", outageService.listActiveOutages().size());
        out.put("activeAlerts", alertService.listActiveAlerts().size());
        out.put("recentAuditCount", auditService.listRecent(5).size());
        out.put("timestamp", Instant.now().toString());
        return out;
    }
}
