package com.dianaglobal.paineldoauthorbackend.adapter.in.dto.tickets;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO para mensagem de ticket.
 */
public record MessageDTO(
    UUID id,
    String sentByUserId,
    String sentByName,
    String message,
    boolean isInternalNote,           // Só admin vê se for true
    OffsetDateTime createdAt,
    OffsetDateTime readAt
) {}



