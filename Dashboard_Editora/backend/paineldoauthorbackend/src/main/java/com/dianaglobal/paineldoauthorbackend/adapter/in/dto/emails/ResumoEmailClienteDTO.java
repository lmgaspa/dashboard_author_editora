package com.dianaglobal.paineldoauthorbackend.adapter.in.dto.emails;

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
        BigDecimal valorRepassado,  // Valor real repassado (amount_net), não valor bruto
        Instant primeiroPedidoEm,
        Instant ultimoPedidoEm,
        CouponInfoClienteDTO cupom  // Informações agregadas de cupons
) {}

