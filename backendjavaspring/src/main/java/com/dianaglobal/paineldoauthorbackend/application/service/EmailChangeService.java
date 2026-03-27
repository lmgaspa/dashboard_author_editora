// src/main/java/com/dianaglobal/paineldoauthor/application/service/EmailChangeService.java
package com.dianaglobal.paineldoauthorbackend.application.service;

import com.dianaglobal.paineldoauthorbackend.adapter.out.messaging.EventPublisher;
import com.dianaglobal.paineldoauthorbackend.adapter.out.messaging.event.AuthEmailEvent;
import com.dianaglobal.paineldoauthorbackend.application.port.out.UserRepositoryPort;
import com.dianaglobal.paineldoauthorbackend.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailChangeService {

    private final UserRepositoryPort userRepository;
    private final EmailChangeTokenService tokenService;

    private final EventPublisher eventPublisher;

    @Value("${application.email-change.minutes:30}")
    private int ttlMinutes;

    public void requestChange(String userId, String newEmailNormalized, String frontendBaseUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Invalida tokens antigos pendentes
        tokenService.invalidateAllFor(userId);

        // Gera novo token usando o serviço
        String rawToken = tokenService.issue(userId, newEmailNormalized, Duration.ofMinutes(ttlMinutes));

        // link para o NOVO e-mail
        String link = buildConfirmLink(frontendBaseUrl, rawToken);

        // envia confirmação para o novo e-mail
        eventPublisher.publishAuthEvent("auth.email.change.confirm", new AuthEmailEvent(
                "auth.email.change.confirm", user.getName(), newEmailNormalized, null, link, null, null));

        // opcional: alerta para o e-mail antigo
        try {
            if (user.getEmail() != null && !user.getEmail().isBlank()) {
                String support = (frontendBaseUrl == null || frontendBaseUrl.isBlank())
                        ? "https://www.dianaglobal.com.br/support"
                        : (frontendBaseUrl.endsWith("/") ? frontendBaseUrl + "support" : frontendBaseUrl + "/support");
                eventPublisher.publishAuthEvent("auth.email.change.alert", new AuthEmailEvent(
                        "auth.email.change.alert", user.getName(), user.getEmail(), null, null, null, support));
            }
        } catch (Exception ex) {
            log.warn("[EMAIL-CHANGE] alert-old event warn: {}", ex.getMessage());
        }
    }

    public void confirm(String rawToken) {
        // Consome o token usando o serviço (valida expiração, uso único, etc)
        EmailChangeTokenService.Payload payload = tokenService.consume(rawToken);

        String userId = payload.userId();
        String newEmail = payload.newEmail();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String oldEmail = user.getEmail();
        user.setEmail(newEmail);
        user.setEmailConfirmed(true);
        userRepository.save(user);

        // Invalida outros tokens pendentes do usuário
        tokenService.invalidateAllFor(userId);

        try {
            eventPublisher.publishAuthEvent("auth.email.changed", new AuthEmailEvent(
                    "auth.email.changed", user.getName(), newEmail, null, null, null, null));
        } catch (Exception ex) {
            log.warn("[EMAIL-CHANGE] changed-mail event warn: {}", ex.getMessage());
        }

        log.info("[EMAIL-CHANGE] user {} changed e-mail {} -> {}", userId, oldEmail, newEmail);
    }


    private static String buildConfirmLink(String frontendBaseUrl, String token) {
        String base = (frontendBaseUrl == null || frontendBaseUrl.isBlank())
                ? "https://www.dianaglobal.com.br"
                : frontendBaseUrl.trim();
        String path = "email-change/confirm?token=" + token;
        return base.endsWith("/") ? base + path : base + "/" + path;
    }
}
