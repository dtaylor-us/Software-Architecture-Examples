package com.gridops.alert;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AlertServiceImpl implements AlertService {

    private final AlertRepository repository;

    public AlertServiceImpl(AlertRepository repository) {
        this.repository = repository;
    }

    @Override
    public AlertRecord raiseAlert(RaiseAlertCommand command) {
        String id = "ALT-" + UUID.randomUUID().toString().substring(0, 8);
        AlertRecord record = new AlertRecord(
                id,
                command.type(),
                command.message(),
                command.severity(),
                command.sourceId(),
                Instant.now(),
                "ACTIVE"
        );
        return repository.save(record);
    }

    @Override
    public List<AlertRecord> listActiveAlerts() {
        return repository.findAllActive();
    }

    @Override
    public List<AlertRecord> listBySeverity(String severity) {
        return repository.findBySeverity(severity);
    }
}
