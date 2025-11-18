package com.dianaglobal.paineldoauthorbackend.application.service;

import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.emails.PainelEmailsAutorDTO;

/**
 * Service para gerenciar e-mails do autor.
 * Consolida e-mails de clientes (orders) e e-mails de repasse (payout_email).
 */
public interface EmailsAutorService {

    /**
     * Monta o painel de e-mails para um autor.
     * 
     * @param autorId ID do autor
     * @param dbUrl URL do banco de dados do e-commerce
     * @param dbUsername Username do banco de dados do e-commerce
     * @param dbPassword Senha do banco de dados do e-commerce
     * @return Painel com e-mails de clientes e e-mails de repasse
     */
    PainelEmailsAutorDTO montarPainelEmailsAutor(
            long autorId,
            String dbUrl,
            String dbUsername,
            String dbPassword
    );
}

