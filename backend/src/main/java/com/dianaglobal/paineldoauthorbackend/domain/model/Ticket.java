package com.dianaglobal.paineldoauthorbackend.domain.model;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain model para ticket.
 * Autor escreve o que quiser, sistema classifica prioridade automaticamente.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {
    private UUID id;
    private String ticketNumber;                // "TKT-2024-001234"
    private String title;                       // Título do ticket
    private String description;                // Descrição (autor escreve o que quiser)
    private String createdByUserId;            // FK para users.id (autor)
    private String assignedToUserId;           // FK para users.id (admin responsável)
    private String authorId;                    // author_id do criador
    private TicketCategory category;           // Categoria
    private TicketPriority priority;           // Prioridade (APENAS ADMIN VÊ - sistema classifica)
    private String priorityReason;             // Por que foi classificada assim (automático)
    private TicketStatus status;               // Status do ticket
    private Instant createdAt;                 // Data de criação
    private Instant updatedAt;                  // Data de atualização
    private Instant resolvedAt;                // Data de resolução
    private Instant closedAt;                  // Data de fechamento
    private UUID relatedChargeId;              // FK para monthly_charges.id (opcional)
}



