package com.example.spacebased.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AlertHistoryRepository extends JpaRepository<AlertHistoryEntity, Long> {

    List<AlertHistoryEntity> findByNodeIdOrderByRaisedAtDesc(String nodeId, org.springframework.data.domain.Pageable pageable);

    List<AlertHistoryEntity> findByRaisedAtBetweenOrderByRaisedAtDesc(Instant from, Instant to, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT e.alertId FROM AlertHistoryEntity e WHERE e.alertId IN :ids")
    List<String> findAlertIdsByAlertIdIn(@Param("ids") List<String> ids);

    boolean existsByAlertId(String alertId);
}
