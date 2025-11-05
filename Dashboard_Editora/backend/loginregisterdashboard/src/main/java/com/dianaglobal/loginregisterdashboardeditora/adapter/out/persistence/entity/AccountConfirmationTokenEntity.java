package com.dianaglobal.loginregisterdashboardeditora.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "account_confirmation_tokens", indexes = {
    @Index(name = "idx_act_user_id", columnList = "user_id"),
    @Index(name = "idx_act_token_hash", columnList = "token_hash", unique = true),
    @Index(name = "idx_act_expires_at", columnList = "expires_at")
})
@Data
@NoArgsConstructor
public class AccountConfirmationTokenEntity {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @Column(name = "user_id", nullable = false, length = 20)
    private String userId;

    /** Salve apenas o hash do token (ex.: SHA-256 Base64Url) */
    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** Expiração absoluta */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /** Uso único */
    @Column(name = "consumed_at")
    private Instant consumedAt;

    /** Revogação/validade atual */
    @Column(name = "valid", nullable = false)
    private boolean valid = true;
}
