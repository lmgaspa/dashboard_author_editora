package com.dianaglobal.paineldoauthorbackend.application.service;

import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.cobrancas.MonthlyChargeDTO;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.entregas.EntregaDTO;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.emails.PainelEmailsAutorDTO;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.pagamentos.PainelPagamentosAutorDTO;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.tickets.TicketDTO;
import com.dianaglobal.paineldoauthorbackend.adapter.in.web.AdminController.AuthorStatsResponse;

import java.util.List;

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
    
    /**
     * Exporta entregas em PDF.
     */
    byte[] exportEntregasToPdf(List<EntregaDTO> entregas, String authorName);
    
    /**
     * Exporta entregas em CSV.
     */
    byte[] exportEntregasToCsv(List<EntregaDTO> entregas, String authorName);
    
    /**
     * Exporta cobranças em PDF.
     */
    byte[] exportCobrancasToPdf(List<MonthlyChargeDTO> cobrancas, String authorName);
    
    /**
     * Exporta cobranças em CSV.
     */
    byte[] exportCobrancasToCsv(List<MonthlyChargeDTO> cobrancas, String authorName);
    
    /**
     * Exporta métricas/estatísticas em PDF.
     */
    byte[] exportMetricasToPdf(AuthorStatsResponse metricas, String authorName);
    
    /**
     * Exporta métricas/estatísticas em CSV.
     */
    byte[] exportMetricasToCsv(AuthorStatsResponse metricas, String authorName);
    
    /**
     * Exporta tickets em PDF.
     */
    byte[] exportTicketsToPdf(List<TicketDTO> tickets, String authorName);
    
    /**
     * Exporta tickets em CSV.
     */
    byte[] exportTicketsToCsv(List<TicketDTO> tickets, String authorName);
}

