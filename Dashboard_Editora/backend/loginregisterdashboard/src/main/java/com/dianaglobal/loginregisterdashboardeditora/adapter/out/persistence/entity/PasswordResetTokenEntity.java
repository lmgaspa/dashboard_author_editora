package com.dianaglobal.loginregisterdashboardeditora.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens", indexes = {
    @Index(name = "idx_prt_user_id", columnList = "user_id"),
    @Index(name = "idx_prt_token_hash", columnList = "token_hash", unique = true),
    @Index(name = "idx_prt_expires_at", columnList = "expires_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetTokenEntity {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @Column(name = "user_id", nullable = false, length = 20)
    private String userId;

    /** SHA-256 (URL-safe) do token em texto puro */
    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    /** Expira exatamente neste horário */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
