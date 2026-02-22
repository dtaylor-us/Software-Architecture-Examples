package com.gridops.audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AuditService tested independently.
 */
class AuditServiceTest {

    private AuditService auditService;

    @BeforeEach
    void setUp() {
        auditService = new AuditServiceImpl(new InMemoryAuditEntryRepository());
    }

    @Test
    void record_returnsEntryWithId() {
        AuditService.AuditCommand cmd = new AuditService.AuditCommand(
                "operator-1",
                "REPORT_OUTAGE",
                "Outage",
                "OUT-123",
                "asset=TRANSFORMER-01"
        );
        AuditService.AuditEntry entry = auditService.record(cmd);

        assertThat(entry.id()).startsWith("AUD-");
        assertThat(entry.actor()).isEqualTo("operator-1");
        assertThat(entry.action()).isEqualTo("REPORT_OUTAGE");
        assertThat(entry.targetId()).isEqualTo("OUT-123");
        assertThat(entry.at()).isNotNull();
    }

    @Test
    void listByActor_filtersCorrectly() {
        auditService.record(new AuditService.AuditCommand("alice", "ACTION_A", "T", "1", "d"));
        auditService.record(new AuditService.AuditCommand("bob", "ACTION_B", "T", "2", "d"));
        auditService.record(new AuditService.AuditCommand("alice", "ACTION_C", "T", "3", "d"));

        List<AuditService.AuditEntry> alice = auditService.listByActor("alice");
        assertThat(alice).hasSize(2);
    }

    @Test
    void listRecent_returnsOrderedByNewestFirst() {
        auditService.record(new AuditService.AuditCommand("u", "A", "T", "1", "d"));
        auditService.record(new AuditService.AuditCommand("u", "A", "T", "2", "d"));
        auditService.record(new AuditService.AuditCommand("u", "A", "T", "3", "d"));

        List<AuditService.AuditEntry> recent = auditService.listRecent(2);
        assertThat(recent).hasSize(2);
    }
}
