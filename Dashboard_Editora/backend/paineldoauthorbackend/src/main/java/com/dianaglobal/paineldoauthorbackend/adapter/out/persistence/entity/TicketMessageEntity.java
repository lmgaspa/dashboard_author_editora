package com.dianaglobal.paineldoauthorbackend.adapter.out.persistence.entity;

import com.dianaglobal.paineldoauthorbackend.domain.model.TicketMessage;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ticket_messages", indexes = {
    @Index(name = "idx_messages_ticket_id", columnList = "ticket_id, created_at"),
    @Index(name = "idx_messages_sent_by", columnList = "sent_by_user_id"),
    @Index(name = "idx_messages_read_at", columnList = "read_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketMessageEntity {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @Column(name = "ticket_id", nullable = false, columnDefinition = "UUID")
    private UUID ticketId;

    @Column(name = "sent_by_user_id", nullable = false, length = 20)
    private String sentByUserId;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "is_internal_note", nullable = false)
    @Builder.Default
    private boolean isInternalNote = false;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "read_at")
    private Instant readAt;

    // ---------- Mapeamentos domínio <-> entidade ----------

    public static TicketMessageEntity fromDomain(TicketMessage d) {
        if (d == null) return null;
        return TicketMessageEntity.builder()
                .id(d.getId())
                .ticketId(d.getTicketId())
                .sentByUserId(d.getSentByUserId())
                .message(d.getMessage())
                .isInternalNote(d.isInternalNote())
                .createdAt(d.getCreatedAt() != null ? d.getCreatedAt() : Instant.now())
                .readAt(d.getReadAt())
                .build();
    }

    public static TicketMessage toDomain(TicketMessageEntity e) {
        if (e == null) return null;
        return TicketMessage.builder()
                .id(e.getId())
                .ticketId(e.getTicketId())
                .sentByUserId(e.getSentByUserId())
                .message(e.getMessage())
                .isInternalNote(e.isInternalNote())
                .createdAt(e.getCreatedAt())
                .readAt(e.getReadAt())
                .build();
    }
}



