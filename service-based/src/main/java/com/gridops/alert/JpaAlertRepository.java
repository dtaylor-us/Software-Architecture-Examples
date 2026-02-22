package com.gridops.alert;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Primary
public class JpaAlertRepository implements AlertRepository {

    private final AlertJpaRepository jpa;

    public JpaAlertRepository(AlertJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public AlertService.AlertRecord save(AlertService.AlertRecord record) {
        AlertEntity entity = AlertEntity.from(record);
        return jpa.save(entity).toRecord();
    }

    @Override
    public List<AlertService.AlertRecord> findAllActive() {
        return jpa.findAllByStatus("ACTIVE").stream()
                .map(AlertEntity::toRecord)
                .toList();
    }

    @Override
    public List<AlertService.AlertRecord> findBySeverity(String severity) {
        return jpa.findAllBySeverity(severity).stream()
                .map(AlertEntity::toRecord)
                .toList();
    }
}
