package com.dianaglobal.loginregisterdashboardeditora.adapter.in.dto.pagamentos;

/**
 * DTO para o funil de vendas do autor.
 * Métricas simples e fáceis de entender para um escritor leigo.
 */
public record FunilVendasDTO(
        long totalPedidos,              // Total de pedidos (todos os status)
        long pedidosConfirmados,        // Pedidos pagos (status CONFIRMED)
        long pedidosEmAndamento,        // Pedidos NEW/WAITING
        long pedidosCancelados,         // Pedidos cancelados/expirados
        double taxaConversao,           // Porcentagem de pedidos confirmados (0-100)
        double valorTotalPedidos,       // Valor total de todos os pedidos
        double valorConfirmado,         // Valor dos pedidos confirmados
        double valorEmAndamento         // Valor dos pedidos em andamento
) {}

