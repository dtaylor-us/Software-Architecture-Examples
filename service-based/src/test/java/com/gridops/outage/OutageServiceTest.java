package com.gridops.outage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OutageService can be tested independently; no other services or HTTP required.
 */
class OutageServiceTest {

    private OutageService outageService;

    @BeforeEach
    void setUp() {
        outageService = new OutageServiceImpl(new InMemoryOutageRepository());
    }

    @Test
    void reportOutage_createsRecord_and_returnsIt() {
        OutageService.ReportOutageCommand cmd = new OutageService.ReportOutageCommand(
                "TRANSFORMER-01",
                "Overheating",
                Instant.now(),
                "HIGH"
        );
        OutageService.OutageRecord record = outageService.reportOutage(cmd);

        assertThat(record.id()).startsWith("OUT-");
        assertThat(record.assetId()).isEqualTo("TRANSFORMER-01");
        assertThat(record.description()).isEqualTo("Overheating");
        assertThat(record.severity()).isEqualTo("HIGH");
        assertThat(record.status()).isEqualTo("ACTIVE");
    }

    @Test
    void getOutage_returnsEmpty_whenNotFound() {
        assertThat(outageService.getOutage("OUT-nonexistent")).isEmpty();
    }

    @Test
    void listActiveOutages_returnsOnlyActive() {
        outageService.reportOutage(new OutageService.ReportOutageCommand("A1", "D1", Instant.now(), "LOW"));
        outageService.reportOutage(new OutageService.ReportOutageCommand("A2", "D2", Instant.now(), "MEDIUM"));

        List<OutageService.OutageRecord> active = outageService.listActiveOutages();
        assertThat(active).hasSize(2);
    }

    @Test
    void listByAsset_filtersByAssetId() {
        outageService.reportOutage(new OutageService.ReportOutageCommand("ASSET-X", "Out1", Instant.now(), "LOW"));
        outageService.reportOutage(new OutageService.ReportOutageCommand("ASSET-Y", "Out2", Instant.now(), "LOW"));
        outageService.reportOutage(new OutageService.ReportOutageCommand("ASSET-X", "Out3", Instant.now(), "MEDIUM"));

        List<OutageService.OutageRecord> forX = outageService.listByAsset("ASSET-X");
        assertThat(forX).hasSize(2);
    }
}
