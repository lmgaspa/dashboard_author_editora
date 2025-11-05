package com.dianaglobal.loginregisterdashboardeditora.adapter.out.persistence;

import com.dianaglobal.loginregisterdashboardeditora.adapter.out.persistence.entity.AccountConfirmationTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountConfirmationTokenRepository extends JpaRepository<AccountConfirmationTokenEntity, UUID> {

    /** Busca pelo hash do token; regras de expiração/uso único são tratadas no service. */
    Optional<AccountConfirmationTokenEntity> findByTokenHash(String tokenHash);

    /** Lista tokens ainda válidos de um usuário (para revogar em massa antes de emitir novo). */
    List<AccountConfirmationTokenEntity> findAllByUserIdAndValidTrue(String userId);

    /** Se você quiser realmente apagar tokens antigos de um usuário. (Opcional/unused) */
    void deleteByUserId(String userId);
}
