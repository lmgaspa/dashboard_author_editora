package com.dianaglobal.paineldoauthorbackend.adapter.in.dto.tickets;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO para ticket (admin vê - COM prioridade).
 */
public record TicketAdminDTO(
    UUID id,
    String ticketNumber,
    String title,
    String description,
    String category,
    String status,
    String priority,                  // ✅ ADMIN VÊ
    String priorityReason,            // ✅ ADMIN VÊ
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    String authorName,
    String assignedToAdminName,
    UUID relatedChargeId,
    List<MessageDTO> messages
) {}



