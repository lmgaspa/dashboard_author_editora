package com.dianaglobal.paineldoauthorbackend.adapter.in.dto.emails;

import java.util.List;

/**
 * DTO principal para o painel de e-mails do autor.
 * Consolida e-mails de clientes (orders) e e-mails de repasse (payout_email).
 */
public record PainelEmailsAutorDTO(
        List<ResumoEmailClienteDTO> emailsClientes,
        List<ResumoEmailRepasseDTO> emailsRepasse
) {}

