package com.dianaglobal.paineldoauthorbackend.adapter.in.dto.pagamentos;

/**
 * DTO para o resumo de pagamentos de um autor.
 * Focado em informações claras para um escritor leigo.
 */
public record PagamentosAutorResumoDTO(
        long autorId,
        String nomeAutor,
        double valorVendasConfirmadas,  // Soma de pedidos CONFIRMED do autor
        double valorJaRecebido,          // Por enquanto pode ser 0.0 (placeholder para futuro)
        double valorAReceber             // valorVendasConfirmadas - valorJaRecebido (mínimo 0)
) {}

