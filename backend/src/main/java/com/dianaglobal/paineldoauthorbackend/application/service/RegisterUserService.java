// src/main/java/com/dianaglobal/paineldoauthor/application/service/RegisterUserService.java
package com.dianaglobal.paineldoauthorbackend.application.service;

import com.dianaglobal.paineldoauthorbackend.application.port.in.RegisterUserUseCase;
import com.dianaglobal.paineldoauthorbackend.application.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepositoryPort userRepository;

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
