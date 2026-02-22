package com.gridops.outage;

import java.util.List;
import java.util.Optional;

/**
 * Data layer for the Outage service. Owned by this service; no other service accesses it.
 * Implementation can be in-memory (demo) or a dedicated DB/schema when evolving to microservices.
 */
public interface OutageRepository {

    OutageService.OutageRecord save(OutageService.OutageRecord record);

    Optional<OutageService.OutageRecord> findById(String id);

    List<OutageService.OutageRecord> findAllActive();

    List<OutageService.OutageRecord> findByAssetId(String assetId);
}
