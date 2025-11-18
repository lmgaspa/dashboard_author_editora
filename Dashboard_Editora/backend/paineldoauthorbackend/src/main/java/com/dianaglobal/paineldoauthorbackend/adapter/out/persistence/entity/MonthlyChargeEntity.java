package com.dianaglobal.paineldoauthorbackend.adapter.out.persistence.entity;

import com.dianaglobal.paineldoauthorbackend.domain.model.ChargeStatus;
import com.dianaglobal.paineldoauthorbackend.domain.model.MonthlyCharge;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "monthly_charges", indexes = {
    @Index(name = "idx_charge_author_status", columnList = "author_id, status"),
    @Index(name = "idx_charge_due_date", columnList = "due_date"),
    @Index(name = "idx_charge_status", columnList = "status"),
    @Index(name = "idx_charge_created_by", columnList = "created_by_user_id"),
    @Index(name = "idx_charge_month_year", columnList = "charge_month, charge_year")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyChargeEntity {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @Column(name = "author_id", nullable = false, length = 255)
    private String authorId;

    @Column(name = "created_by_user_id", nullable = false, length = 20)
    private String createdByUserId;

    @Column(name = "charge_month", nullable = false)
    private Integer chargeMonth;

    @Column(name = "charge_year", nullable = false)
    private Integer chargeYear;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "charge_date", nullable = false)
    private LocalDate chargeDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ChargeStatus status = ChargeStatus.PENDING;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "confirmed_by_user_id", length = 20)
    private String confirmedByUserId;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "pix_code", columnDefinition = "TEXT")
    private String pixCode;

    @Column(name = "pix_expires_at")
    private Instant pixExpiresAt;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // ---------- Mapeamentos domínio <-> entidade ----------

    public static MonthlyChargeEntity fromDomain(MonthlyCharge d) {
        if (d == null) return null;
        return MonthlyChargeEntity.builder()
                .id(d.getId())
                .authorId(d.getAuthorId())
                .createdByUserId(d.getCreatedByUserId())
                .chargeMonth(d.getChargeMonth())
                .chargeYear(d.getChargeYear())
                .amount(d.getAmount())
                .dueDate(d.getDueDate())
                .chargeDate(d.getChargeDate())
                .status(d.getStatus() != null ? d.getStatus() : ChargeStatus.PENDING)
                .paidAt(d.getPaidAt())
                .confirmedByUserId(d.getConfirmedByUserId())
                .confirmedAt(d.getConfirmedAt())
                .pixCode(d.getPixCode())
                .pixExpiresAt(d.getPixExpiresAt())
                .createdAt(d.getCreatedAt() != null ? d.getCreatedAt() : Instant.now())
                .updatedAt(d.getUpdatedAt() != null ? d.getUpdatedAt() : Instant.now())
                .notes(d.getNotes())
                .build();
    }

    public static MonthlyCharge toDomain(MonthlyChargeEntity e) {
        if (e == null) return null;
        return MonthlyCharge.builder()
                .id(e.getId())
                .authorId(e.getAuthorId())
                .createdByUserId(e.getCreatedByUserId())
                .chargeMonth(e.getChargeMonth())
                .chargeYear(e.getChargeYear())
                .amount(e.getAmount())
                .dueDate(e.getDueDate())
                .chargeDate(e.getChargeDate())
                .status(e.getStatus())
                .paidAt(e.getPaidAt())
                .confirmedByUserId(e.getConfirmedByUserId())
                .confirmedAt(e.getConfirmedAt())
                .pixCode(e.getPixCode())
                .pixExpiresAt(e.getPixExpiresAt())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .notes(e.getNotes())
                .build();
    }
}



