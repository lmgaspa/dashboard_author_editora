package com.dianaglobal.paineldoauthorbackend.domain.model;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain model para rastreamento de envio de pedidos.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderShipping {
    private UUID id;
    private Long orderId;                    // ID do pedido no e-commerce
    private String authorId;                  // author_id do autor responsável
    private Boolean enviado;                  // Se o livro foi enviado (sim/não)
    private ShippingStatus statusEnvio;       // Status do envio
    private String codigoRastreamento;       // Código de rastreamento dos Correios
    private Instant createdAt;
    private Instant updatedAt;
    private Instant enviadoAt;               // Quando foi marcado como enviado
}

