// src/main/java/com/dianaglobal/paineldoauthor/domain/model/User.java
package com.dianaglobal.paineldoauthorbackend.domain.model;

import lombok.*;

// src/main/java/com/dianaglobal/paineldoauthor/domain/model/User.java
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class User {
    private String id;  // Format: "admin-1", "user-1", "user-2", etc.
    private String name;
    private String email;
    private String password;          // pode ficar null
    private boolean emailConfirmed;

    // NOVOS:
    private String authProvider;      // ex.: "LOCAL" ou outros provedores futuros
    private boolean passwordSet;      // true quando o usuário definiu senha conscientemente
    
    @Builder.Default
    private Role role = Role.USER;   // Role padrão é USER
    
    // ID do autor no sistema externo (pode ser null)
    private String authorId;
    
    // URL base do e-commerce do autor (cada autor tem seu próprio e-commerce)
    private String ecommerceUrl;
    
    // Credenciais do banco de dados do e-commerce (cada autor tem seu próprio banco)
    private String ecommerceDbUrl;      // JDBC URL do banco do e-commerce
    private String ecommerceDbUsername; // Username do banco do e-commerce
    private String ecommerceDbPassword; // Password do banco do e-commerce (será criptografado no futuro)
    
    // URL da foto de perfil do usuário (pode ser null)
    private String profilePhotoUrl;
}

//