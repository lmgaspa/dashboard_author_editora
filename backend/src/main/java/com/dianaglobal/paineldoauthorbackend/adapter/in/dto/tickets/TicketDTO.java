package com.dianaglobal.paineldoauthorbackend.adapter.in.dto.tickets;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO para ticket (autor vê - SEM prioridade).
 */
public record TicketDTO(
    UUID id,
    String ticketNumber,
    String title,
    String description,
    String category,
    String status,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    // PRIORIDADE NÃO INCLUÍDA (apenas admin vê)
    List<MessageDTO> messages
) {}



