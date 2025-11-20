package com.dianaglobal.paineldoauthorbackend.application.port.out;

import com.dianaglobal.paineldoauthorbackend.domain.model.OrderShipping;

import java.util.List;
import java.util.Optional;

/**
 * Port para repositório de shipping de pedidos.
 */
public interface OrderShippingRepositoryPort {
    
    /**
     * Salva ou atualiza um shipping.
     */
    OrderShipping save(OrderShipping shipping);
    
    /**
     * Busca shipping por order_id e author_id.
     */
    Optional<OrderShipping> findByOrderIdAndAuthorId(Long orderId, String authorId);
    
    /**
     * Lista todos os shippings de um autor.
     */
    List<OrderShipping> findByAuthorId(String authorId);
    
    /**
     * Lista shippings por status.
     */
    List<OrderShipping> findByAuthorIdAndStatusEnvio(String authorId, com.dianaglobal.paineldoauthorbackend.domain.model.ShippingStatus statusEnvio);
}

