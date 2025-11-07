// src/main/java/com/dianaglobal/loginregister/application/event/WelcomeEmailOnConfirm.java
package com.dianaglobal.loginregisterdashboardeditora.application.event;

import com.dianaglobal.loginregisterdashboardeditora.adapter.out.mail.WelcomeEmailService;
import com.dianaglobal.loginregisterdashboardeditora.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WelcomeEmailOnConfirm implements UserConfirmedListener {

    private final WelcomeEmailService welcomeEmailService;

    @Override
    public void onUserConfirmed(User user, String plaintextPassword) {
        log.info("[WELCOME EMAIL] Preparing to send welcome email to {} (name: {})", user.getEmail(), user.getName());
        try {
            welcomeEmailService.send(user.getEmail(), user.getName(), plaintextPassword);
            log.info("[WELCOME EMAIL] ✅ Successfully sent welcome email to {}", user.getEmail());
        } catch (Exception e) {
            log.error("[WELCOME EMAIL] ❌ Failed to send welcome email to {}: {}", user.getEmail(), e.getMessage(), e);
            throw e; // Re-throw para que o AdminController possa capturar e logar também
        }
    }
}
