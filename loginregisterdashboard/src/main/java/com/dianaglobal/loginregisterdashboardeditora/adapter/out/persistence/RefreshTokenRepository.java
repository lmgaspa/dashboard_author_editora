package com.dianaglobal.loginregisterdashboardeditora.adapter.out.persistence;

import com.dianaglobal.loginregisterdashboardeditora.adapter.out.persistence.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {
    Optional<RefreshTokenEntity> findByToken(String token);
    void deleteByToken(String token);
    void deleteByEmail(String email);
}
