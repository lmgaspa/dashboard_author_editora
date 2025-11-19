package com.dianaglobal.paineldoauthorbackend.application.service;

import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.emails.PainelEmailsAutorDTO;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.pagamentos.PainelPagamentosAutorDTO;

/**
 * Serviço para exportação de dados em diferentes formatos (PDF, CSV).
 */
public interface ExportService {
    
    /**
     * Exporta dados de pagamentos em PDF.
     */
    byte[] exportPaymentsToPdf(PainelPagamentosAutorDTO painel, String authorName);
    
    /**
     * Exporta dados de pagamentos em CSV.
     */
    byte[] exportPaymentsToCsv(PainelPagamentosAutorDTO painel, String authorName);
    
    /**
     * Exporta dados de emails em PDF.
     */
    byte[] exportEmailsToPdf(PainelEmailsAutorDTO painel, String authorName);
    
    /**
     * Exporta dados de emails em CSV.
     */
    byte[] exportEmailsToCsv(PainelEmailsAutorDTO painel, String authorName);
}

