package com.dianaglobal.paineldoauthorbackend.application.service;

import com.dianaglobal.paineldoauthorbackend.domain.model.MonthlyCharge;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MonthlyChargeService {
    MonthlyCharge createCharge(MonthlyCharge charge);
    Optional<MonthlyCharge> findById(UUID id);
    List<MonthlyCharge> findAllByAuthorId(String authorId);
    List<MonthlyCharge> findAll();
    MonthlyCharge confirmPayment(UUID chargeId, String adminUserId, String notes);
    List<MonthlyCharge> findOverdueCharges();
    void markAsOverdue(UUID chargeId);
    String generatePixCode(MonthlyCharge charge);
}



