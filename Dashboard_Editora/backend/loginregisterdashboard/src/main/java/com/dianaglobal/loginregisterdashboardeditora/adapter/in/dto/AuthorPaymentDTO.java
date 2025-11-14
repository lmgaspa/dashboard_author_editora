package com.dianaglobal.loginregisterdashboardeditora.adapter.in.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * DTO para representar um pagamento de um autor.
 */
public record AuthorPaymentDTO(
        Long paymentId,           // ID do pagamento no banco do e-commerce
        Long orderId,             // ID do pedido associado
        Long authorId,            // ID do autor
        String bookTitle,         // Título do livro
        String bookId,            // ID do livro
        BigDecimal amount,        // Valor do pagamento
        String status,            // Status do pagamento (ex: "PAID", "PENDING", etc.)
        OffsetDateTime paidAt,    // Data/hora do pagamento
        String provider,          // Provedor do pagamento (ex: "EFI_PIX")
        String externalId         // ID externo (ex: txid do Pix)
) {}

