// src/main/java/com/dianaglobal/loginregister/adapter/out/persistence/entity/EmailChangeTokenEntity.java
package com.dianaglobal.loginregisterdashboardeditora.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "email_change_tokens", indexes = {
    @Index(name = "idx_ect_user_id", columnList = "user_id"),
    @Index(name = "idx_ect_token_hash", columnList = "token_hash", unique = true),
    @Index(name = "idx_ect_new_email", columnList = "new_email_normalized"),
    @Index(name = "idx_ect_expires_at", columnList = "expires_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailChangeTokenEntity {
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @Column(name = "user_id", nullable = false, length = 20)
    private String userId;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "new_email_normalized", nullable = false)
    private String newEmailNormalized;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "consumed_at")
    private Instant consumedAt;

    @Column(name = "valid")
    private Boolean valid; // usar Boolean para null-safety (TRUE/FALSE)

    public boolean isValid() {
        return Boolean.TRUE.equals(valid);
    }
}

