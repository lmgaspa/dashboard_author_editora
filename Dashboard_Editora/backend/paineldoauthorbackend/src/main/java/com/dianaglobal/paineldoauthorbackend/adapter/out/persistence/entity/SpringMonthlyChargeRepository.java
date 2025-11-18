package com.dianaglobal.paineldoauthorbackend.adapter.out.persistence.entity;

import com.dianaglobal.paineldoauthorbackend.domain.model.ChargeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringMonthlyChargeRepository extends JpaRepository<MonthlyChargeEntity, UUID> {
    Optional<MonthlyChargeEntity> findByAuthorIdAndChargeMonthAndChargeYear(String authorId, Integer month, Integer year);
    List<MonthlyChargeEntity> findByAuthorId(String authorId);
    List<MonthlyChargeEntity> findByAuthorIdAndStatus(String authorId, ChargeStatus status);
    List<MonthlyChargeEntity> findByStatus(ChargeStatus status);
    List<MonthlyChargeEntity> findByDueDateBefore(LocalDate date);
    List<MonthlyChargeEntity> findByStatusAndDueDateBefore(ChargeStatus status, LocalDate date);
}



