package com.gridops.api;

import com.gridops.audit.AuditService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @PostMapping
    public AuditService.AuditEntry record(@Valid @RequestBody AuditRequest req) {
        return auditService.record(new AuditService.AuditCommand(
                req.actor(),
                req.action(),
                req.targetType(),
                req.targetId(),
                req.details()
        ));
    }

    @GetMapping("/actor/{actor}")
    public List<AuditService.AuditEntry> listByActor(@PathVariable String actor) {
        return auditService.listByActor(actor);
    }

    @GetMapping("/action/{action}")
    public List<AuditService.AuditEntry> listByAction(@PathVariable String action) {
        return auditService.listByAction(action);
    }

    @GetMapping("/recent")
    public List<AuditService.AuditEntry> listRecent(@RequestParam(defaultValue = "20") int limit) {
        return auditService.listRecent(limit);
    }

    public record AuditRequest(@NotBlank String actor, @NotBlank String action, String targetType, String targetId, String details) {}
}
