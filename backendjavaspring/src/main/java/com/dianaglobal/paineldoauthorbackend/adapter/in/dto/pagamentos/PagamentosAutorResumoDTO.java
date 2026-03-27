package com.dianaglobal.paineldoauthorbackend.adapter.in.dto.pagamentos;

/**
 * DTO para o resumo de pagamentos de um autor.
 * Focado em informações claras para um escritor leigo.
 * 
 * IMPORTANTE: Todos os valores são REAIS (após taxas e margens), não valores brutos.
 * Vêm de payment_payouts.amount_net, não de orders.total.
 */
public record PagamentosAutorResumoDTO(
        long autorId,
        String nomeAutor,
        double valorVendasConfirmadas,  // Soma de payment_payouts.amount_net com status='CONFIRMED' (valor REAL recebido)
        double valorJaRecebido,          // Soma de payment_payouts.amount_net com status='CONFIRMED' (valor REAL confirmado)
        double valorAReceber             // Soma de payment_payouts.amount_net com status='SENT' (valor REAL enviado mas pendente)
) {}

