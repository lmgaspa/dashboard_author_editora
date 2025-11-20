package com.dianaglobal.paineldoauthorbackend.adapter.in.dto.emails;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO para resumo de e-mails de repasse.
 * Representa um e-mail de repasse enviado ao autor.
 */
public record ResumoEmailRepasseDTO(
        Long id,
        Long pedidoId,
        Long repasseId,
        String emailDestinatario,
        String tipoEmail,
        String status,
        Instant enviadoEm,
        String mensagemErro,
        BigDecimal valorRepassado,
        CouponInfoPayoutDTO cupom  // Informações de cupom do pedido
) {}

