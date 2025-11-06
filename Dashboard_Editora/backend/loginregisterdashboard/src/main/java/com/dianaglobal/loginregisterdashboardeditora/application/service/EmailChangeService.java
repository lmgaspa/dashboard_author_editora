// src/main/java/com/dianaglobal/loginregister/application/service/EmailChangeService.java
package com.dianaglobal.loginregisterdashboardeditora.application.service;

import com.dianaglobal.loginregisterdashboardeditora.adapter.out.mail.EmailChangeAlertOldEmailService;
import com.dianaglobal.loginregisterdashboardeditora.adapter.out.mail.EmailChangeChangedEmailService;
import com.dianaglobal.loginregisterdashboardeditora.adapter.out.mail.EmailChangeConfirmNewEmailService;
import com.dianaglobal.loginregisterdashboardeditora.application.port.out.UserRepositoryPort;
import com.dianaglobal.loginregisterdashboardeditora.domain.model.User;
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

    // Serviços de e-mail divididos em 3 arquivos
    private final EmailChangeConfirmNewEmailService confirmNewEmailService;
    private final EmailChangeChangedEmailService changedEmailService;
    private final EmailChangeAlertOldEmailService alertOldEmailService;

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
        confirmNewEmailService.sendConfirmNew(newEmailNormalized, user.getName(), link, ttlMinutes);

        // opcional: alerta para o e-mail antigo
        try {
            if (user.getEmail() != null && !user.getEmail().isBlank()) {
                String support = (frontendBaseUrl == null || frontendBaseUrl.isBlank())
                        ? "https://www.dianaglobal.com.br/support"
                        : (frontendBaseUrl.endsWith("/") ? frontendBaseUrl + "support" : frontendBaseUrl + "/support");
                alertOldEmailService.sendAlertOld(user.getEmail(), user.getName(), support);
            }
        } catch (Exception ex) {
            log.warn("[EMAIL-CHANGE] alert-old send warn: {}", ex.getMessage());
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
            changedEmailService.sendChanged(newEmail, user.getName());
        } catch (Exception ex) {
            log.warn("[EMAIL-CHANGE] changed-mail warn: {}", ex.getMessage());
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
