package com.dianaglobal.paineldoauthorbackend.adapter.out.persistence;

import com.dianaglobal.paineldoauthorbackend.adapter.out.persistence.entity.OrderShippingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaOrderShippingRepository extends JpaRepository<OrderShippingEntity, UUID> {
    
    /**
     * Busca shipping por order_id e author_id.
     */
    Optional<OrderShippingEntity> findByOrderIdAndAuthorId(Long orderId, String authorId);
    
    /**
     * Lista todos os shippings de um autor.
     */
    List<OrderShippingEntity> findByAuthorIdOrderByCreatedAtDesc(String authorId);
    
    /**
     * Lista shippings por status.
     */
    List<OrderShippingEntity> findByAuthorIdAndStatusEnvioOrderByCreatedAtDesc(
        String authorId, 
        com.dianaglobal.paineldoauthorbackend.domain.model.ShippingStatus statusEnvio
    );
}

