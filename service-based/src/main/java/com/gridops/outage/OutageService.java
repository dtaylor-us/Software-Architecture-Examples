package com.gridops.outage;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Internal interface for the Outage service. Callers in this deployable use this interface;
 * implementation is not exposed outside the service boundary.
 */
public interface OutageService {

    OutageRecord reportOutage(ReportOutageCommand command);

    Optional<OutageRecord> getOutage(String outageId);

    List<OutageRecord> listActiveOutages();

    List<OutageRecord> listByAsset(String assetId);

    record ReportOutageCommand(String assetId, String description, Instant startTime, String severity) {}

    record OutageRecord(String id, String assetId, String description, Instant startTime, Instant endTime, String severity, String status) {}
}
