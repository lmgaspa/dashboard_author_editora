package com.dianaglobal.paineldoauthorbackend.adapter.out.persistence;

import com.dianaglobal.paineldoauthorbackend.adapter.out.persistence.entity.OrderShippingEntity;
import com.dianaglobal.paineldoauthorbackend.application.port.out.OrderShippingRepositoryPort;
import com.dianaglobal.paineldoauthorbackend.domain.model.OrderShipping;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderShippingRepositoryAdapter implements OrderShippingRepositoryPort {

    private final JpaOrderShippingRepository jpaRepository;

    @Override
    public OrderShipping save(OrderShipping shipping) {
        OrderShippingEntity entity = OrderShippingEntity.fromDomain(shipping);
        if (entity.getId() == null) {
            entity.setId(java.util.UUID.randomUUID());
        }
        entity.setUpdatedAt(java.time.Instant.now());
        OrderShippingEntity saved = jpaRepository.save(entity);
        return OrderShippingEntity.toDomain(saved);
    }

    @Override
    public Optional<OrderShipping> findByOrderIdAndAuthorId(Long orderId, String authorId) {
        return jpaRepository.findByOrderIdAndAuthorId(orderId, authorId)
                .map(OrderShippingEntity::toDomain);
    }

    @Override
    public List<OrderShipping> findByAuthorId(String authorId) {
        return jpaRepository.findByAuthorIdOrderByCreatedAtDesc(authorId).stream()
                .map(OrderShippingEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderShipping> findByAuthorIdAndStatusEnvio(String authorId, com.dianaglobal.paineldoauthorbackend.domain.model.ShippingStatus statusEnvio) {
        return jpaRepository.findByAuthorIdAndStatusEnvioOrderByCreatedAtDesc(authorId, statusEnvio).stream()
                .map(OrderShippingEntity::toDomain)
                .collect(Collectors.toList());
    }
}

