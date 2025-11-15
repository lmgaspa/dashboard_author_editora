package com.dianaglobal.loginregisterdashboardeditora.adapter.in.dto.emails;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO para resumo de e-mails de clientes.
 * Agrupa informações de pedidos por e-mail do cliente.
 */
public record ResumoEmailClienteDTO(
        String email,
        long totalPedidos,
        long totalPedidosConfirmados,
        BigDecimal valorTotalConfirmado,
        Instant primeiroPedidoEm,
        Instant ultimoPedidoEm
) {}

