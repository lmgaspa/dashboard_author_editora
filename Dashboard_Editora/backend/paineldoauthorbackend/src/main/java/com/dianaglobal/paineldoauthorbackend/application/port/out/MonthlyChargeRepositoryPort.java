package com.dianaglobal.paineldoauthorbackend.application.port.out;

import com.dianaglobal.paineldoauthorbackend.domain.model.ChargeStatus;
import com.dianaglobal.paineldoauthorbackend.domain.model.MonthlyCharge;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MonthlyChargeRepositoryPort {
    void save(MonthlyCharge charge);
    Optional<MonthlyCharge> findById(UUID id);
    Optional<MonthlyCharge> findByAuthorIdAndMonthAndYear(String authorId, Integer month, Integer year);
    List<MonthlyCharge> findAllByAuthorId(String authorId);
    List<MonthlyCharge> findAllByAuthorIdAndStatus(String authorId, ChargeStatus status);
    List<MonthlyCharge> findAllByStatus(ChargeStatus status);
    List<MonthlyCharge> findAllByDueDateBefore(LocalDate date);
    List<MonthlyCharge> findAllByStatusAndDueDateBefore(ChargeStatus status, LocalDate date);
}



