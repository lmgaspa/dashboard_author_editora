package com.dianaglobal.paineldoauthorbackend.adapter.in.dto.entregas;

/**
 * DTO para item de um pedido (para entrega).
 */
public record ItemEntregaDTO(
        String bookId,
        String titulo,
        Integer quantidade,
        java.math.BigDecimal preco
) {}

