package com.dianaglobal.paineldoauthorbackend.application.port.out;

import com.dianaglobal.paineldoauthorbackend.domain.model.Role;
import com.dianaglobal.paineldoauthorbackend.domain.model.User;

import java.util.List;
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
    // NEW: listar todos os usuários
    List<User> findAll();
    // NEW: listar usuários por role
    List<User> findAllByRole(Role role);
    // NEW: listar usuários por author_id
    List<User> findAllByAuthorId(String authorId);
}
