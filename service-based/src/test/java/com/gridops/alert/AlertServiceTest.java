package com.gridops.alert;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AlertService tested independently.
 */
class AlertServiceTest {

    private AlertService alertService;

    @BeforeEach
    void setUp() {
        alertService = new AlertServiceImpl(new InMemoryAlertRepository());
    }

    @Test
    void raiseAlert_returnsRecordWithId() {
        AlertService.RaiseAlertCommand cmd = new AlertService.RaiseAlertCommand(
                "PRICE_SPIKE",
                "Price exceeded 200 $/MWh",
                "HIGH",
                "ZONE-1"
        );
        AlertService.AlertRecord record = alertService.raiseAlert(cmd);

        assertThat(record.id()).startsWith("ALT-");
        assertThat(record.type()).isEqualTo("PRICE_SPIKE");
        assertThat(record.message()).contains("200");
        assertThat(record.severity()).isEqualTo("HIGH");
        assertThat(record.status()).isEqualTo("ACTIVE");
    }

    @Test
    void listActiveAlerts_returnsOnlyActive() {
        alertService.raiseAlert(new AlertService.RaiseAlertCommand("T1", "M1", "LOW", "S1"));
        alertService.raiseAlert(new AlertService.RaiseAlertCommand("T2", "M2", "MEDIUM", "S2"));

        List<AlertService.AlertRecord> active = alertService.listActiveAlerts();
        assertThat(active).hasSize(2);
    }

    @Test
    void listBySeverity_filtersCorrectly() {
        alertService.raiseAlert(new AlertService.RaiseAlertCommand("A", "M", "HIGH", "S1"));
        alertService.raiseAlert(new AlertService.RaiseAlertCommand("B", "M", "LOW", "S2"));
        alertService.raiseAlert(new AlertService.RaiseAlertCommand("C", "M", "HIGH", "S3"));

        List<AlertService.AlertRecord> high = alertService.listBySeverity("HIGH");
        assertThat(high).hasSize(2);
    }
}
