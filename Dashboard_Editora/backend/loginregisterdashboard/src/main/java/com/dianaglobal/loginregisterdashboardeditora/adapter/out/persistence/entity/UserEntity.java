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
    @Index(name = "idx_user_email", columnList = "email", unique = true),
    @Index(name = "idx_user_author_id", columnList = "author_id")
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

    @Column(name = "author_id", length = 255)
    private String authorId;  // ID do autor no sistema externo (pode ser null)

    @Column(name = "ecommerce_url", length = 500)
    private String ecommerceUrl;  // URL base do e-commerce do autor (cada autor tem seu próprio e-commerce)

    @Column(name = "ecommerce_db_url", length = 500)
    private String ecommerceDbUrl;  // JDBC URL do banco do e-commerce

    @Column(name = "ecommerce_db_username", length = 255)
    private String ecommerceDbUsername;  // Username do banco do e-commerce

    @Column(name = "ecommerce_db_password", length = 500)
    private String ecommerceDbPassword;  // Password do banco do e-commerce

    @Column(name = "profile_photo_url", length = 500)
    private String profilePhotoUrl;  // URL da foto de perfil do usuário (pode ser null)

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
                .authorId(d.getAuthorId())
                .ecommerceUrl(d.getEcommerceUrl())
                .ecommerceDbUrl(d.getEcommerceDbUrl())
                .ecommerceDbUsername(d.getEcommerceDbUsername())
                .ecommerceDbPassword(d.getEcommerceDbPassword())
                .profilePhotoUrl(d.getProfilePhotoUrl())
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
                .authorId(e.getAuthorId())
                .ecommerceUrl(e.getEcommerceUrl())
                .ecommerceDbUrl(e.getEcommerceDbUrl())
                .ecommerceDbUsername(e.getEcommerceDbUsername())
                .ecommerceDbPassword(e.getEcommerceDbPassword())
                .profilePhotoUrl(e.getProfilePhotoUrl())
                .build();
    }
}
