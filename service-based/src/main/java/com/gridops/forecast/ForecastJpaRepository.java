package com.gridops.forecast;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

interface ForecastJpaRepository extends JpaRepository<ForecastEntity, String> {

    List<ForecastEntity> findAllByZoneId(String zoneId);

    @Query("SELECT f FROM ForecastEntity f WHERE f.zoneId = :zoneId AND f.at >= :from AND f.at < :to")
    List<ForecastEntity> findByZoneIdAndTimeWindow(@Param("zoneId") String zoneId, @Param("from") Instant from, @Param("to") Instant to);
}
