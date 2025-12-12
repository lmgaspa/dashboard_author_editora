package com.dianaglobal.paineldoauthorbackend.adapter.out.persistence.entity;

import com.dianaglobal.paineldoauthorbackend.domain.model.OrderShipping;
import com.dianaglobal.paineldoauthorbackend.domain.model.ShippingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA Entity para rastreamento de envio de pedidos.
 */
@Entity
@Table(name = "order_shipping", indexes = {
    @Index(name = "idx_shipping_order_id", columnList = "order_id"),
    @Index(name = "idx_shipping_author_id", columnList = "author_id"),
    @Index(name = "idx_shipping_status", columnList = "status_envio"),
    @Index(name = "idx_shipping_enviado", columnList = "enviado"),
    @Index(name = "idx_shipping_created_at", columnList = "created_at")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_order_author", columnNames = {"order_id", "author_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderShippingEntity {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "author_id", nullable = false, length = 255)
    private String authorId;

    @Column(name = "enviado", nullable = false)
    @Builder.Default
    private Boolean enviado = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_envio", nullable = false, length = 20)
    @Builder.Default
    private ShippingStatus statusEnvio = ShippingStatus.AGUARDANDO;

    @Column(name = "codigo_rastreamento", length = 255)
    private String codigoRastreamento;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @Column(name = "enviado_at")
    private Instant enviadoAt;

    // ---------- Mapeamentos domínio <-> entidade ----------

    public static OrderShippingEntity fromDomain(OrderShipping d) {
        if (d == null) {
            return null;
        }
        return OrderShippingEntity.builder()
                .id(d.getId())
                .orderId(d.getOrderId())
                .authorId(d.getAuthorId())
                .enviado(d.getEnviado())
                .statusEnvio(d.getStatusEnvio())
                .codigoRastreamento(d.getCodigoRastreamento())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .enviadoAt(d.getEnviadoAt())
                .build();
    }

    public static OrderShipping toDomain(OrderShippingEntity e) {
        if (e == null) {
            return null;
        }
        return OrderShipping.builder()
                .id(e.getId())
                .orderId(e.getOrderId())
                .authorId(e.getAuthorId())
                .enviado(e.getEnviado())
                .statusEnvio(e.getStatusEnvio())
                .codigoRastreamento(e.getCodigoRastreamento())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .enviadoAt(e.getEnviadoAt())
                .build();
    }
}

