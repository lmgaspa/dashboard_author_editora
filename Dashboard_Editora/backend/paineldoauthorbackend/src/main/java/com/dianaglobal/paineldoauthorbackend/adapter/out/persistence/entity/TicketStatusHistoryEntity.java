package com.dianaglobal.paineldoauthorbackend.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidade para histórico de mudanças de status de tickets (auditoria).
 */
@Entity
@Table(name = "ticket_status_history", indexes = {
    @Index(name = "idx_history_ticket_id", columnList = "ticket_id, created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketStatusHistoryEntity {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @Column(name = "ticket_id", nullable = false, columnDefinition = "UUID")
    private UUID ticketId;

    @Column(name = "old_status", length = 20)
    private String oldStatus;

    @Column(name = "new_status", nullable = false, length = 20)
    private String newStatus;

    @Column(name = "changed_by_user_id", nullable = false, length = 20)
    private String changedByUserId;

    @Column(name = "change_reason", columnDefinition = "TEXT")
    private String changeReason;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}

