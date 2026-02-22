package com.gridops.pricing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

interface PriceRuleJpaRepository extends JpaRepository<PriceRuleEntity, String> {

    List<PriceRuleEntity> findAllByZoneId(String zoneId);

    @Query("SELECT r FROM PriceRuleEntity r WHERE r.zoneId = :zoneId AND :at >= r.effectiveFrom AND :at < r.effectiveTo")
    Optional<PriceRuleEntity> findEffectiveForZoneAt(@Param("zoneId") String zoneId, @Param("at") Instant at);
}
