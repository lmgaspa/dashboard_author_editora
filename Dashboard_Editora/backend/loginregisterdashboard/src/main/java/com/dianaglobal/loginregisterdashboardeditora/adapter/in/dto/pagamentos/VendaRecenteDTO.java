package com.dianaglobal.loginregisterdashboardeditora.adapter.in.dto.pagamentos;

import java.time.OffsetDateTime;

/**
 * DTO para representar uma venda recente do autor.
 * Dados simples e fáceis de entender para um escritor leigo.
 */
public record VendaRecenteDTO(
        long pedidoId,
        OffsetDateTime dataPedido,
        String tituloLivro,
        int quantidade,
        double valorTotal,      // quantidade * preço do item
        String statusLegivel    // "Pago" / "Em andamento" / "Cancelado"
) {}

