package com.gridops.outage;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface OutageJpaRepository extends JpaRepository<OutageEntity, String> {

    List<OutageEntity> findAllByStatus(String status);

    List<OutageEntity> findAllByAssetId(String assetId);
}
