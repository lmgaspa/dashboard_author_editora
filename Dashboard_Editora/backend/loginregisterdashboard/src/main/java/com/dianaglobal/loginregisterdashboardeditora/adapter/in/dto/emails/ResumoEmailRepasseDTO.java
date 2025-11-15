package com.dianaglobal.loginregisterdashboardeditora.adapter.in.dto.emails;

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
        String mensagemErro
) {}

