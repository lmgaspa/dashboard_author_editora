package com.dianaglobal.paineldoauthorbackend.adapter.in.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para resposta do perfil do usuário.
 * Retorna informações do usuário autenticado, incluindo authorId.
 */
public record ProfileResponseDTO(
        @JsonProperty("id")
        String id,  // Format: "admin-1", "user-1", "user-2", etc.
        
        @JsonProperty("name")
        String name,
        
        @JsonProperty("email")
        String email,
        
        @JsonProperty("authProvider")
        String authProvider,   // new field
        
        @JsonProperty("passwordSet")
        boolean passwordSet,   // new field
        
        @JsonProperty("profilePhotoUrl")
        String profilePhotoUrl, // URL da foto de perfil (pode ser null)
        
        @JsonProperty("authorId")  // Serializado como "authorId" no JSON
        String authorId,       // ID do autor no e-commerce (pode ser null)
        
        @JsonProperty("ecommerceUrl")
        String ecommerceUrl   // URL do e-commerce (pode ser null)
) {}
