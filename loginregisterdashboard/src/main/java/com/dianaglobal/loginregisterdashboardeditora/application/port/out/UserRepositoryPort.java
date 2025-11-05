package com.dianaglobal.loginregisterdashboardeditora.application.port.out;

import com.dianaglobal.loginregisterdashboardeditora.domain.model.User;

import java.util.Optional;

public interface UserRepositoryPort {
    void save(User user);
    void updatePassword(String userId, String encodedPassword);

    Optional<User> findById(String id);
    Optional<User> findByEmail(String email);
    // NEW: marcar e-mail como confirmado
    void markEmailConfirmed(String userId);
    // NEW: deletar usuário
    void deleteById(String id);
}
