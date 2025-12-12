package com.dianaglobal.paineldoauthorbackend.application.service;

import com.dianaglobal.paineldoauthorbackend.application.port.out.UserRepositoryPort;
import com.dianaglobal.paineldoauthorbackend.domain.model.Role;
import com.dianaglobal.paineldoauthorbackend.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Serviço para obter o author_id do usuário autenticado.
 * Centraliza a lógica de mapeamento user -> author.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CurrentAuthorService {

    private final UserRepositoryPort userRepositoryPort;

    /**
     * Obtém o author_id do usuário atualmente autenticado.
     * 
     * @return Optional com o author_id (Long) se o usuário tiver um autor associado,
     *         Optional.empty() se for admin ou não tiver author_id
     */
    public Optional<Long> getCurrentAuthorId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            log.debug("[CURRENT AUTHOR] No authentication found");
            return Optional.empty();
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        try {
            Optional<User> userOpt = userRepositoryPort.findByEmail(email);
            if (userOpt.isEmpty()) {
                log.warn("[CURRENT AUTHOR] User not found for email: {}", email);
                return Optional.empty();
            }

            User user = userOpt.get();

            // Admin users não têm author_id ou podem ter author_id = null
            // Para admins, retornamos empty para indicar que podem acessar qualquer autor
            if (user.getRole() == Role.ADMIN) {
                log.debug("[CURRENT AUTHOR] User {} is ADMIN, returning empty", email);
                return Optional.empty();
            }

            // USER role: deve ter author_id
            String authorIdStr = user.getAuthorId();
            if (authorIdStr == null || authorIdStr.trim().isEmpty()) {
                log.debug("[CURRENT AUTHOR] User {} has no author_id", email);
                return Optional.empty();
            }

            try {
                Long authorId = Long.parseLong(authorIdStr.trim());
                log.debug("[CURRENT AUTHOR] Found author_id {} for user {}", authorId, email);
                return Optional.of(authorId);
            } catch (NumberFormatException e) {
                log.warn("[CURRENT AUTHOR] Invalid author_id format for user {}: {}", email, authorIdStr);
                return Optional.empty();
            }

        } catch (Exception e) {
            log.error("[CURRENT AUTHOR] Error getting author_id for user {}: {}", email, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Verifica se o usuário atual é admin.
     */
    public boolean isCurrentUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return false;
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        try {
            Optional<User> userOpt = userRepositoryPort.findByEmail(email);
            if (userOpt.isEmpty()) {
                return false;
            }

            return userOpt.get().getRole() == Role.ADMIN;
        } catch (Exception e) {
            log.error("[CURRENT AUTHOR] Error checking admin status: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Obtém o User atual (para acessar credenciais do banco, etc).
     */
    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return Optional.empty();
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        return userRepositoryPort.findByEmail(email);
    }
}

