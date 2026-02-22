package com.gridops.outage;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@Repository
@Primary
public class JpaOutageRepository implements OutageRepository {

    private final OutageJpaRepository jpa;

    public JpaOutageRepository(OutageJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public OutageService.OutageRecord save(OutageService.OutageRecord record) {
        OutageEntity entity = OutageEntity.from(record);
        return jpa.save(entity).toRecord();
    }

    @Override
    public Optional<OutageService.OutageRecord> findById(String id) {
        return jpa.findById(id).map(OutageEntity::toRecord);
    }

    @Override
    public List<OutageService.OutageRecord> findAllActive() {
        return jpa.findAllByStatus("ACTIVE").stream()
                .map(OutageEntity::toRecord)
                .toList();
    }

    @Override
    public List<OutageService.OutageRecord> findByAssetId(String assetId) {
        return jpa.findAllByAssetId(assetId).stream()
                .map(OutageEntity::toRecord)
                .toList();
    }
}
