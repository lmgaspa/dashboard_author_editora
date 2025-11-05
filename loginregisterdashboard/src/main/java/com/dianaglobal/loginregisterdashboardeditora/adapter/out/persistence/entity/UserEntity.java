// src/main/java/com/dianaglobal/loginregister/adapter/out/persistence/entity/UserEntity.java
package com.dianaglobal.loginregisterdashboardeditora.adapter.out.persistence.entity;

import com.dianaglobal.loginregisterdashboardeditora.domain.model.Role;
import com.dianaglobal.loginregisterdashboardeditora.domain.model.User;
import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email", unique = true)
})
public class UserEntity {

    @Id
    @Column(name = "id", length = 20)
    private String id;  // Format: "admin-1", "user-1", "user-2", etc.

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "email_confirmed", nullable = false)
    @Builder.Default
    private boolean emailConfirmed = false;

    @Column(name = "auth_provider", length = 50)
    @Builder.Default
    private String authProvider = "LOCAL"; // default para novos registros locais

    @Column(name = "password_set", nullable = false)
    @Builder.Default
    private boolean passwordSet = false;   // default para novos registros

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.USER;

    // ---------- Mapeamentos domínio <-> entidade ----------

    public static UserEntity fromDomain(User d) {
        if (d == null) return null;
        return UserEntity.builder()
                .id(d.getId())
                .name(d.getName())
                .email(d.getEmail())
                .password(d.getPassword())
                .emailConfirmed(d.isEmailConfirmed())
                .authProvider(d.getAuthProvider())
                .passwordSet(d.isPasswordSet())
                .role(d.getRole() != null ? d.getRole() : Role.USER)
                .build();
    }

    public static User toDomain(UserEntity e) {
        if (e == null) return null;
        return User.builder()
                .id(e.getId())
                .name(e.getName())
                .email(e.getEmail())
                .password(e.getPassword())
                .emailConfirmed(e.isEmailConfirmed())
                .authProvider(e.getAuthProvider())
                .passwordSet(e.isPasswordSet())
                .role(e.getRole())
                .build();
    }
}
