package com.dianaglobal.paineldoauthorbackend.adapter.in.dto;

public record ProfileResponseDTO(
        String id,  // Format: "admin-1", "user-1", "user-2", etc.
        String name,
        String email,
        String authProvider,   // new field
        boolean passwordSet,   // new field
        String profilePhotoUrl // URL da foto de perfil (pode ser null)
) {}
