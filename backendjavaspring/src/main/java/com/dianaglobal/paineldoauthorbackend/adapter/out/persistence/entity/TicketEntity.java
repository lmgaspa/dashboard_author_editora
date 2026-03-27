package com.dianaglobal.paineldoauthorbackend.adapter.out.persistence.entity;

import com.dianaglobal.paineldoauthorbackend.domain.model.Ticket;
import com.dianaglobal.paineldoauthorbackend.domain.model.TicketCategory;
import com.dianaglobal.paineldoauthorbackend.domain.model.TicketPriority;
import com.dianaglobal.paineldoauthorbackend.domain.model.TicketStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tickets", indexes = {
    @Index(name = "idx_tickets_created_by", columnList = "created_by_user_id"),
    @Index(name = "idx_tickets_assigned_to", columnList = "assigned_to_user_id"),
    @Index(name = "idx_tickets_author_id", columnList = "author_id"),
    @Index(name = "idx_tickets_status", columnList = "status"),
    @Index(name = "idx_tickets_category", columnList = "category"),
    @Index(name = "idx_tickets_priority", columnList = "priority"),
    @Index(name = "idx_tickets_created_at", columnList = "created_at"),
    @Index(name = "idx_tickets_ticket_number", columnList = "ticket_number", unique = true),
    @Index(name = "idx_tickets_related_charge", columnList = "related_charge_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketEntity {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @Column(name = "ticket_number", nullable = false, unique = true, length = 20)
    private String ticketNumber;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_by_user_id", nullable = false, length = 20)
    private String createdByUserId;

    @Column(name = "assigned_to_user_id", length = 20)
    private String assignedToUserId;

    @Column(name = "author_id", length = 255)
    private String authorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private TicketCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    @Builder.Default
    private TicketPriority priority = TicketPriority.MEDIUM;

    @Column(name = "priority_reason", columnDefinition = "TEXT")
    private String priorityReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TicketStatus status = TicketStatus.OPEN;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "related_charge_id", columnDefinition = "UUID")
    private UUID relatedChargeId;

    // ---------- Mapeamentos domínio <-> entidade ----------

    public static TicketEntity fromDomain(Ticket d) {
        if (d == null) return null;
        return TicketEntity.builder()
                .id(d.getId())
                .ticketNumber(d.getTicketNumber())
                .title(d.getTitle())
                .description(d.getDescription())
                .createdByUserId(d.getCreatedByUserId())
                .assignedToUserId(d.getAssignedToUserId())
                .authorId(d.getAuthorId())
                .category(d.getCategory())
                .priority(d.getPriority() != null ? d.getPriority() : TicketPriority.MEDIUM)
                .priorityReason(d.getPriorityReason())
                .status(d.getStatus() != null ? d.getStatus() : TicketStatus.OPEN)
                .createdAt(d.getCreatedAt() != null ? d.getCreatedAt() : Instant.now())
                .updatedAt(d.getUpdatedAt() != null ? d.getUpdatedAt() : Instant.now())
                .resolvedAt(d.getResolvedAt())
                .closedAt(d.getClosedAt())
                .relatedChargeId(d.getRelatedChargeId())
                .build();
    }

    public static Ticket toDomain(TicketEntity e) {
        if (e == null) return null;
        return Ticket.builder()
                .id(e.getId())
                .ticketNumber(e.getTicketNumber())
                .title(e.getTitle())
                .description(e.getDescription())
                .createdByUserId(e.getCreatedByUserId())
                .assignedToUserId(e.getAssignedToUserId())
                .authorId(e.getAuthorId())
                .category(e.getCategory())
                .priority(e.getPriority())
                .priorityReason(e.getPriorityReason())
                .status(e.getStatus())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .resolvedAt(e.getResolvedAt())
                .closedAt(e.getClosedAt())
                .relatedChargeId(e.getRelatedChargeId())
                .build();
    }
}



