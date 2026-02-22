package com.gridops.alert;

import java.util.List;

/**
 * Data layer for the Alert service. Owned by this service only.
 */
public interface AlertRepository {

    AlertService.AlertRecord save(AlertService.AlertRecord record);

    List<AlertService.AlertRecord> findAllActive();

    List<AlertService.AlertRecord> findBySeverity(String severity);
}
