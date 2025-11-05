package com.dianaglobal.loginregisterdashboardeditora.adapter.out.persistence;

import com.dianaglobal.loginregisterdashboardeditora.adapter.out.persistence.entity.EmailChangeTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmailChangeTokenRepository extends JpaRepository<EmailChangeTokenEntity, UUID> {
    List<EmailChangeTokenEntity> findAllByUserIdAndValidTrue(String userId);
    Optional<EmailChangeTokenEntity> findByTokenHash(String tokenHash);
    void deleteByUserId(String userId);
}
