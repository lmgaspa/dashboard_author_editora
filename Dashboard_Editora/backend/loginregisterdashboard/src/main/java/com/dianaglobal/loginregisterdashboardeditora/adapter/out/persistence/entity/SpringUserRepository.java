// src/main/java/com/dianaglobal/loginregister/adapter/out/persistence/entity/SpringUserRepository.java
package com.dianaglobal.loginregisterdashboardeditora.adapter.out.persistence.entity;

import com.dianaglobal.loginregisterdashboardeditora.domain.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringUserRepository extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByEmail(String email);
    List<UserEntity> findByRole(Role role);
}
