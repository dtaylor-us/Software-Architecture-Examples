package com.gridops.audit;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface AuditEntryJpaRepository extends JpaRepository<AuditEntryEntity, String> {

    List<AuditEntryEntity> findAllByActor(String actor);

    List<AuditEntryEntity> findAllByAction(String action);

    List<AuditEntryEntity> findAllByOrderByAtDesc(Pageable pageable);
}
