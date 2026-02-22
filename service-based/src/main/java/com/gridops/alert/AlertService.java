package com.gridops.alert;

import java.time.Instant;
import java.util.List;

/**
 * Internal interface for the Alert service. Owns alert lifecycle and delivery.
 */
public interface AlertService {

    AlertRecord raiseAlert(RaiseAlertCommand command);

    List<AlertRecord> listActiveAlerts();

    List<AlertRecord> listBySeverity(String severity);

    record RaiseAlertCommand(String type, String message, String severity, String sourceId) {}

    record AlertRecord(String id, String type, String message, String severity, String sourceId, Instant raisedAt, String status) {}
}
