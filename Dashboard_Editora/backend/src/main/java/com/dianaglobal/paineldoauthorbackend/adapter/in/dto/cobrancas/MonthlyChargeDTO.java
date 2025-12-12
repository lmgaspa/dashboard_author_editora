package com.dianaglobal.paineldoauthorbackend.adapter.in.dto.cobrancas;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO para cobrança mensal (resposta).
 */
public record MonthlyChargeDTO(
    UUID id,
    String authorId,
    String authorName,
    Integer chargeMonth,
    Integer chargeYear,
    BigDecimal amount,
    LocalDate dueDate,
    LocalDate chargeDate,
    String status,                    // 'PENDING', 'PAID', 'OVERDUE', 'CANCELLED'
    OffsetDateTime paidAt,
    String confirmedByAdminName,
    OffsetDateTime confirmedAt,
    String pixCode,
    OffsetDateTime pixExpiresAt,
    long daysOverdue,                 // Dias de atraso (se OVERDUE)
    boolean hasOpenTicket             // Se já existe ticket aberto
) {}



