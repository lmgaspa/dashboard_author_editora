package com.dianaglobal.paineldoauthorbackend.application.service;

import com.dianaglobal.paineldoauthorbackend.adapter.out.mail.BillingEmailService;
import com.dianaglobal.paineldoauthorbackend.application.port.out.UserRepositoryPort;
import com.dianaglobal.paineldoauthorbackend.domain.model.ChargeStatus;
import com.dianaglobal.paineldoauthorbackend.domain.model.MonthlyCharge;
import com.dianaglobal.paineldoauthorbackend.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingJob {

    private final UserRepositoryPort userRepository;
    private final MonthlyChargeService chargeService;
    private final BillingEmailService emailService;

    @Value("${billing.day:15}")
    private int billingDay;

    @Value("${billing.amount:50.00}")
    private BigDecimal billingAmount;

    @Scheduled(cron = "0 0 8 * * *") // Daily at 8 AM
    @Transactional
    public void generateMonthlyCharges() {
        LocalDate today = LocalDate.now();

        // Check if today is the billing day
        if (today.getDayOfMonth() != billingDay) {
            return;
        }

        log.info("[BILLING JOB] Starting monthly billing generation for {}/{}", today.getMonthValue(), today.getYear());

        // Find all active authors (Users with Role USER/ADMIN and valid authorId)
        // Note: Ideally we should filter by users who have a contract or "active"
        // status,
        // but for now we assume all users with authorId should be billed.
        // We iterate all users to find authors. Optimally this should be a DB query.
        List<User> allUsers = userRepository.findAll();

        int generatedCount = 0;
        int errorCount = 0;

        for (User user : allUsers) {
            // Skip users without authorId or with role ADMIN who might not be paying
            // authors (adjust as needed)
            if (user.getAuthorId() == null || user.getAuthorId().trim().isEmpty()) {
                continue;
            }

            try {
                generateChargeForUser(user, today);
                generatedCount++;
            } catch (Exception e) {
                log.error("[BILLING JOB] Error generating charge for user {}: {}", user.getEmail(), e.getMessage());
                errorCount++;
            }
        }

        log.info("[BILLING JOB] Finished. Generated: {}, Errors: {}", generatedCount, errorCount);
    }

    private void generateChargeForUser(User user, LocalDate today) {
        // Due date = Day 20 of current month
        LocalDate dueDate = today.withDayOfMonth(20);

        // Create charge domain object
        MonthlyCharge charge = MonthlyCharge.builder()
                .id(UUID.randomUUID())
                .authorId(user.getAuthorId())
                .createdByUserId("system")
                .chargeMonth(today.getMonthValue())
                .chargeYear(today.getYear())
                .amount(billingAmount)
                .dueDate(dueDate)
                .chargeDate(today)
                .status(ChargeStatus.PENDING)
                .build();

        try {
            // Attempt to create charge (will call EFI to generate PIX)
            // If already exists, service throws exception (handled by caller)
            MonthlyCharge createdCharge = chargeService.createCharge(charge);

            // Send email
            emailService.sendBillingEmail(user, createdCharge);

        } catch (IllegalArgumentException e) {
            log.info("[BILLING JOB] Charge already exists for user {} ({})", user.getEmail(), e.getMessage());
        }
    }
}
