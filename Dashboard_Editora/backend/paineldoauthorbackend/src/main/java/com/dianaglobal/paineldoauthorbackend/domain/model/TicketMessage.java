package com.dianaglobal.paineldoauthorbackend.domain.model;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain model para mensagem de ticket.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketMessage {
    private UUID id;
    private UUID ticketId;                     // FK para tickets.id
    private String sentByUserId;               // FK para users.id
    private String message;                    // Conteúdo da mensagem
    private boolean isInternalNote;            // Nota interna (só admin vê)
    private Instant createdAt;                 // Data de criação
    private Instant readAt;                    // Quando foi lido pelo destinatário
}



