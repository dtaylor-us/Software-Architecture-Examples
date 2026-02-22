package com.gridops.api;

import com.gridops.audit.AuditService;
import com.gridops.outage.OutageService;
import com.gridops.alert.AlertService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

/**
 * REST API for outage operations. Orchestrates OutageService, AlertService, and AuditService in-process.
 */
@RestController
@RequestMapping("/api/outages")
public class OutageController {

    private final OutageService outageService;
    private final AlertService alertService;
    private final AuditService auditService;

    public OutageController(OutageService outageService, AlertService alertService, AuditService auditService) {
        this.outageService = outageService;
        this.alertService = alertService;
        this.auditService = auditService;
    }

    @PostMapping
    public ResponseEntity<OutageService.OutageRecord> reportOutage(@Valid @RequestBody ReportOutageRequest req) {
        OutageService.ReportOutageCommand cmd = new OutageService.ReportOutageCommand(
                req.assetId(),
                req.description(),
                req.startTime() != null ? req.startTime() : Instant.now(),
                req.severity() != null ? req.severity() : "MEDIUM"
        );
        OutageService.OutageRecord record = outageService.reportOutage(cmd);
        alertService.raiseAlert(new AlertService.RaiseAlertCommand(
                "OUTAGE",
                "Outage reported: " + record.description(),
                record.severity(),
                record.id()
        ));
        auditService.record(new AuditService.AuditCommand(
                "api",
                "REPORT_OUTAGE",
                "Outage",
                record.id(),
                "asset=" + record.assetId()
        ));
        return ResponseEntity.ok(record);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OutageService.OutageRecord> getOutage(@PathVariable String id) {
        return outageService.getOutage(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/active")
    public List<OutageService.OutageRecord> listActive() {
        return outageService.listActiveOutages();
    }

    @GetMapping
    public List<OutageService.OutageRecord> listByAsset(@RequestParam @NotBlank String assetId) {
        return outageService.listByAsset(assetId);
    }

    public record ReportOutageRequest(
            @NotBlank String assetId,
            @NotBlank String description,
            Instant startTime,
            String severity
    ) {}
}
