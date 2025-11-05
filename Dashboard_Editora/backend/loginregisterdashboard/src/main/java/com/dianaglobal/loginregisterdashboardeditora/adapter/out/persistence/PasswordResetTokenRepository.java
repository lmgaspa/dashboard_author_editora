// src/main/java/com/dianaglobal/loginregister/adapter/out/persistence/PasswordResetTokenRepository.java
package com.dianaglobal.loginregisterdashboardeditora.adapter.out.persistence;

import com.dianaglobal.loginregisterdashboardeditora.adapter.out.persistence.entity.PasswordResetTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, UUID> {
    Optional<PasswordResetTokenEntity> findByTokenHashAndUsedAtIsNullAndExpiresAtAfter(String tokenHash, Instant now);
    void deleteByUserId(String userId);
}
