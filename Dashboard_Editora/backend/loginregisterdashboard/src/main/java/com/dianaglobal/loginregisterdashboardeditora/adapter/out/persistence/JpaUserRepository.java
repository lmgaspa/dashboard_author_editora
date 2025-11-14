package com.dianaglobal.loginregisterdashboardeditora.adapter.out.persistence;

import com.dianaglobal.loginregisterdashboardeditora.adapter.out.persistence.entity.SpringUserRepository;
import com.dianaglobal.loginregisterdashboardeditora.adapter.out.persistence.entity.UserEntity;
import com.dianaglobal.loginregisterdashboardeditora.application.port.out.UserRepositoryPort;
import com.dianaglobal.loginregisterdashboardeditora.domain.model.Role;
import com.dianaglobal.loginregisterdashboardeditora.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaUserRepository implements UserRepositoryPort {

    private final SpringUserRepository repository;

    @Override
    public void save(User user) {
        repository.save(UserEntity.fromDomain(user));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email).map(UserEntity::toDomain);
    }

    @Override
    public Optional<User> findById(String id) {
        return repository.findById(id).map(UserEntity::toDomain);
    }

    @Override
    public void updatePassword(String userId, String encodedPassword) {
        var ent = repository.findById(userId).orElseThrow();
        ent.setPassword(encodedPassword);
        repository.save(ent);
    }

    @Override
    public void markEmailConfirmed(String userId) {
        var ent = repository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        if (!ent.isEmailConfirmed()) {
            ent.setEmailConfirmed(true);
            repository.save(ent);
        }
    }

    @Override
    public void deleteById(String id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("User not found: " + id);
        }
        repository.deleteById(id);
    }

    @Override
    public List<User> findAll() {
        return repository.findAll().stream()
                .map(UserEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findAllByRole(Role role) {
        return repository.findByRole(role).stream()
                .map(UserEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> findAllByAuthorId(String authorId) {
        return repository.findByAuthorId(authorId).stream()
                .map(UserEntity::toDomain)
                .collect(Collectors.toList());
    }
}
