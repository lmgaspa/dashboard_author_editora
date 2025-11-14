package com.dianaglobal.loginregisterdashboardeditora.adapter.in.dto.pagamentos;

import java.util.List;

/**
 * DTO principal para o painel de pagamentos do autor.
 * Combina o resumo, o funil de vendas e a lista de vendas recentes.
 */
public record PainelPagamentosAutorDTO(
        PagamentosAutorResumoDTO resumo,
        FunilVendasDTO funilVendas,
        List<VendaRecenteDTO> vendasRecentes
) {}

