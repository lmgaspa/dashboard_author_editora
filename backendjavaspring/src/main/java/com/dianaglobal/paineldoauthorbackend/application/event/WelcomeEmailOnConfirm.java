package com.dianaglobal.paineldoauthorbackend.application.event;

import com.dianaglobal.paineldoauthorbackend.adapter.out.messaging.EventPublisher;
import com.dianaglobal.paineldoauthorbackend.adapter.out.messaging.event.AuthEmailEvent;
import com.dianaglobal.paineldoauthorbackend.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WelcomeEmailOnConfirm implements UserConfirmedListener {

    private final EventPublisher eventPublisher;

    @Override
    public void onUserConfirmed(User user, String plaintextPassword) {
        log.info("[WELCOME EMAIL] Publishing auth.welcome event for {}", user.getEmail());
        AuthEmailEvent event = new AuthEmailEvent(
                "auth.welcome",
                user.getName(),
                user.getEmail(),
                null, null,
                plaintextPassword,
                null
        );
        eventPublisher.publishAuthEvent("auth.welcome", event);
    }
}
