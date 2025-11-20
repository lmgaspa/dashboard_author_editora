package com.dianaglobal.paineldoauthorbackend.adapter.in.dto.emails;

import java.math.BigDecimal;

/**
 * DTO para informações de cupom em e-mails de clientes.
 * Agrega informações de cupons usados por um cliente.
 */
public record CouponInfoClienteDTO(
        long pedidosComCupom,        // Quantidade de pedidos confirmados que tiveram cupom
        BigDecimal totalDesconto     // Soma total de descontos aplicados
) {}

