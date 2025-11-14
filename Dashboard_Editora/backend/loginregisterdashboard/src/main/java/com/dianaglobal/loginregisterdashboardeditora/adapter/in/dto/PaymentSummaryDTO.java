package com.dianaglobal.loginregisterdashboardeditora.adapter.in.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO para resumo de pagamentos de um autor.
 */
public record PaymentSummaryDTO(
        Long authorId,
        String authorName,
        BigDecimal totalReceived,    // Total recebido
        Long totalPayments,          // Quantidade total de pagamentos
        BigDecimal recentReceived,   // Total recebido recentemente (últimos 30 dias)
        Long recentPayments,         // Quantidade de pagamentos recentes
        List<AuthorPaymentDTO> payments  // Lista de pagamentos (paginada)
) {}

