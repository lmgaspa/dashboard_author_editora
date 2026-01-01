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

    @Column(name = "notes")
    private String notes;

    @Column(name = "txid")
    private String txid;

    @Column(name = "location_id")
    private String locationId;

    // ---------- Mapeamentos domínio <-> entidade ----------

    public MonthlyCharge toDomain() {
        return MonthlyCharge.builder()
                .id(this.id)
                .authorId(this.authorId)
                .createdByUserId(this.createdByUserId)
                .chargeMonth(this.chargeMonth)
                .chargeYear(this.chargeYear)
                .amount(this.amount)
                .dueDate(this.dueDate)
                .chargeDate(this.chargeDate)
                .status(this.status)
                .paidAt(this.paidAt)
                .confirmedByUserId(this.confirmedByUserId)
                .confirmedAt(this.confirmedAt)
                .pixCode(this.pixCode)
                .pixExpiresAt(this.pixExpiresAt)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .notes(this.notes)
                .txid(this.txid)
                .locationId(this.locationId)
                .build();
    }

    public static MonthlyChargeEntity fromDomain(MonthlyCharge domain) {
        return MonthlyChargeEntity.builder()
                .id(domain.getId())
                .authorId(domain.getAuthorId())
                .createdByUserId(domain.getCreatedByUserId())
                .chargeMonth(domain.getChargeMonth())
                .chargeYear(domain.getChargeYear())
                .amount(domain.getAmount())
                .dueDate(domain.getDueDate())
                .chargeDate(domain.getChargeDate())
                .status(domain.getStatus())
                .paidAt(domain.getPaidAt())
                .confirmedByUserId(domain.getConfirmedByUserId())
                .confirmedAt(domain.getConfirmedAt())
                .pixCode(domain.getPixCode())
                .pixExpiresAt(domain.getPixExpiresAt())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .notes(domain.getNotes())
                .txid(domain.getTxid())
                .locationId(domain.getLocationId())
                .build();
    }
}
