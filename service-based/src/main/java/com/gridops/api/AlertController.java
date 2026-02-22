package com.gridops.api;

import com.gridops.alert.AlertService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @PostMapping
    public AlertService.AlertRecord raiseAlert(@Valid @RequestBody RaiseAlertRequest req) {
        return alertService.raiseAlert(new AlertService.RaiseAlertCommand(
                req.type(),
                req.message(),
                req.severity() != null ? req.severity() : "MEDIUM",
                req.sourceId()
        ));
    }

    @GetMapping("/active")
    public List<AlertService.AlertRecord> listActive() {
        return alertService.listActiveAlerts();
    }

    @GetMapping
    public List<AlertService.AlertRecord> listBySeverity(@RequestParam @NotBlank String severity) {
        return alertService.listBySeverity(severity);
    }

    public record RaiseAlertRequest(@NotBlank String type, @NotBlank String message, String severity, String sourceId) {}
}
