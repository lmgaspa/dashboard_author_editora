// src/main/java/com/dianaglobal/paineldoauthor/adapter/out/persistence/entity/SpringUserRepository.java
package com.dianaglobal.paineldoauthorbackend.adapter.out.persistence.entity;

import com.dianaglobal.paineldoauthorbackend.domain.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringUserRepository extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByEmail(String email);
    List<UserEntity> findByRole(Role role);
    List<UserEntity> findByAuthorId(String authorId);  // Buscar usuários por author_id
}
