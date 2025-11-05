package com.dianaglobal.loginregisterdashboardeditora.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "confirm_resend_throttle", indexes = {
    @Index(name = "idx_crt_user_id", columnList = "user_id"),
    @Index(name = "idx_crt_email_hash", columnList = "email_hash"),
    @Index(name = "idx_crt_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
public class ConfirmResendThrottleEntity {

    @Id
    @Column(name = "id", length = 100)
    private String id;                 // userId + ":" + yyyy-MM-dd (chave do dia)

    @Column(name = "user_id", nullable = false, length = 20)
    private String userId;

    @Column(name = "email_hash")
    private String emailHash;          // opcional (anti-enumeração)

    @Column(name = "attempts_today", nullable = false)
    private int attemptsToday;

    @Column(name = "last_sent_at")
    private Instant lastSentAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;         // TTL será gerenciado via scheduled task
}
