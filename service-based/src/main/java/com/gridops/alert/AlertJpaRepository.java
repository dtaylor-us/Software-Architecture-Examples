package com.gridops.alert;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface AlertJpaRepository extends JpaRepository<AlertEntity, String> {

    List<AlertEntity> findAllByStatus(String status);

    List<AlertEntity> findAllBySeverity(String severity);
}
