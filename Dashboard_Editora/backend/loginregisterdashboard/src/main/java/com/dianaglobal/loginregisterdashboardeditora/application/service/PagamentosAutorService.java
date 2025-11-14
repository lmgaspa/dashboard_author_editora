package com.dianaglobal.loginregisterdashboardeditora.application.service;

import com.dianaglobal.loginregisterdashboardeditora.adapter.in.dto.pagamentos.PainelPagamentosAutorDTO;

/**
 * Service para montar o painel de pagamentos do autor.
 * Focado em informações claras e simples para um escritor leigo.
 */
public interface PagamentosAutorService {
    
    /**
     * Monta o painel simples de pagamentos para um autor.
     * Busca dados do banco de dados do e-commerce usando as credenciais fornecidas.
     * 
     * @param autorId ID do autor no banco do e-commerce
     * @param dbUrl URL de conexão JDBC do banco do e-commerce
     * @param dbUsername Username do banco do e-commerce
     * @param dbPassword Password do banco do e-commerce
     * @return Painel completo com resumo e vendas recentes, ou null se não encontrar dados
     */
    PainelPagamentosAutorDTO montarPainelPagamentosAutor(
            long autorId,
            String dbUrl,
            String dbUsername,
            String dbPassword
    );
}

