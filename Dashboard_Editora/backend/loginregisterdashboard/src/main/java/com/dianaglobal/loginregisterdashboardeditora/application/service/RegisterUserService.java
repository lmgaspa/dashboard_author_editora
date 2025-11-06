// src/main/java/com/dianaglobal/loginregister/application/service/RegisterUserService.java
package com.dianaglobal.loginregisterdashboardeditora.application.service;

import com.dianaglobal.loginregisterdashboardeditora.application.port.in.RegisterUserUseCase;
import com.dianaglobal.loginregisterdashboardeditora.application.port.out.UserRepositoryPort;
import com.dianaglobal.loginregisterdashboardeditora.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoder encoder;

    @Override
    public void register(String name, String email, String password) {
        validatePasswordStrength(password);

        final String normalizedEmail = email == null ? null : email.trim().toLowerCase();
        if (normalizedEmail == null || normalizedEmail.isBlank()) {
            throw new IllegalArgumentException("E-mail is required.");
        }

        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new DuplicateKeyException("E-mail is already registered");
        }

        // Nota: Este método não é mais usado para registro público
        // Usuários devem ser criados via /api/v1/admin/users
        // Se necessário, injetar UserIdGeneratorService aqui
        throw new UnsupportedOperationException("Use /api/v1/admin/users to create users");
    }

    @Override
    public User registerOauthUser(String name, String email, String googleSub) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("E-mail is required.");
        }
        final String normalizedEmail = email.trim().toLowerCase();

        var existing = userRepository.findByEmail(normalizedEmail);
        if (existing.isPresent()) {
            User u = existing.get();
            // Garante confirmação para contas OAuth
            if (!u.isEmailConfirmed()) {
                u.setEmailConfirmed(true);
            }
            // Marca origin OAuth (idempotente)
            if (u.getAuthProvider() == null) {
                u.setAuthProvider("GOOGLE");
            }
            // Se estiver usando providerId, setar aqui (campo comentado no model)
            // if (u.getProviderId() == null) u.setProviderId(googleSub);

            userRepository.save(u);
            log.info("[OAUTH GOOGLE] Linked existing user {} as GOOGLE {}", normalizedEmail, googleSub);
            return u;
        }

        // Não existe -> não cria mais automaticamente
        // Admin deve criar o usuário primeiro
        log.warn("[OAUTH GOOGLE] User not found, cannot create automatically: {}", normalizedEmail);
        throw new IllegalArgumentException("User not found. Please contact administrator to create your account.");
    }

    // --- helpers ---
    private static void validatePasswordStrength(String pwd) {
        if (pwd == null || pwd.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
        boolean hasUpper = pwd.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = pwd.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = pwd.chars().anyMatch(Character::isDigit);

        if (!hasUpper) throw new IllegalArgumentException("Password must include at least 1 uppercase letter");
        if (!hasLower) throw new IllegalArgumentException("Password must include at least 1 lowercase letter");
        if (!hasDigit) throw new IllegalArgumentException("Password must include at least 1 digit");
    }
}
