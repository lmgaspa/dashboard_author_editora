package com.dianaglobal.paineldoauthorbackend.domain.model;

/**
 * Status de envio de um pedido.
 */
public enum ShippingStatus {
    ENVIADO,            // Livro foi enviado
    AGUARDANDO,         // Aguardando envio
    RECUSADO,           // Envio recusado/cancelado
    ENTREGUE            // Entregue (recebido pelo cliente) - anteriormente ENVIO_CONFIRMADO
}

