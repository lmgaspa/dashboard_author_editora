package com.dianaglobal.loginregisterdashboardeditora.application.service;

import com.dianaglobal.loginregisterdashboardeditora.adapter.out.persistence.RefreshTokenRepository;
import com.dianaglobal.loginregisterdashboardeditora.adapter.out.persistence.entity.RefreshTokenEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // <-- adicione

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository repository;

    public RefreshTokenEntity create(String email) {
        RefreshTokenEntity token = RefreshTokenEntity.builder()
                .id(UUID.randomUUID())
                .email(email)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusSeconds(7 * 24 * 60 * 60)) // 7 dias
                .revoked(false)
                .build();
        return repository.save(token);
    }

    public boolean validate(String token) {
        return repository.findByToken(token)
                .map(t -> !t.isRevoked() && t.getExpiryDate().isAfter(Instant.now()))
                .orElse(false);
    }

    public String getEmailByToken(String token) {
        return repository.findByToken(token)
                .map(RefreshTokenEntity::getEmail)
                .orElseThrow(() -> new RuntimeException("Token not found"));
    }

    public void revokeToken(String token) {
        repository.findByToken(token).ifPresent(t -> {
            t.setRevoked(true);
            repository.save(t);
        });
    }

    // ✅ NOVO: rotação simples (atômica) — NÃO quebra código existente
    @Transactional
    public RefreshTokenEntity rotate(String email, String oldToken) {
        revokeToken(oldToken);
        return create(email); // retorna RefreshTokenEntity -> já possui getToken()
    }
}
