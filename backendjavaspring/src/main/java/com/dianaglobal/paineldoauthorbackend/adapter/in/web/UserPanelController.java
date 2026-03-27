package com.dianaglobal.paineldoauthorbackend.adapter.in.web;

import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.ProfileResponseDTO;
import com.dianaglobal.paineldoauthorbackend.adapter.out.messaging.EventPublisher;
import com.dianaglobal.paineldoauthorbackend.adapter.out.messaging.event.AuthEmailEvent;
import com.dianaglobal.paineldoauthorbackend.adapter.out.persistence.EmailChangeTokenRepository;
import com.dianaglobal.paineldoauthorbackend.adapter.out.persistence.PasswordResetTokenRepository;
import com.dianaglobal.paineldoauthorbackend.application.port.out.UserRepositoryPort;
import com.dianaglobal.paineldoauthorbackend.config.ApiPaths;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(ApiPaths.USER_PANEL_BASE)
@RequiredArgsConstructor
public class UserPanelController {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailChangeTokenRepository emailChangeTokenRepository;
    private final EventPublisher eventPublisher;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getDashboard(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Not authenticated"));
        }

        var user = userRepositoryPort.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String provider = user.getAuthProvider();
        if (provider == null || provider.trim().isEmpty()) {
            provider = "LOCAL";
        }

        UserDashboardResponse dashboard = new UserDashboardResponse(
                "Painel do Usuário",
                user.getName(),
                user.getEmail(),
                provider,
                user.isPasswordSet(),
                "Bem-vindo ao seu painel pessoal"
        );

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getUserProfile(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Not authenticated"));
        }

        var user = userRepositoryPort.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String provider = user.getAuthProvider();
        if (provider == null || provider.trim().isEmpty()) {
            provider = "LOCAL";
        }

        var profile = new ProfileResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                provider,
                user.isPasswordSet(),
                user.getProfilePhotoUrl(),
                user.getAuthorId(),
                user.getEcommerceUrl()
        );

        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Transactional
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid UpdateProfileRequest request
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Not authenticated"));
        }

        try {
            var user = userRepositoryPort.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            // Atualizar nome se fornecido
            if (request.name() != null && !request.name().trim().isEmpty()) {
                String newName = request.name().trim();
                log.info("[UPDATE PROFILE] Updating name for user {}: {} -> {}", user.getEmail(), user.getName(), newName);
                user.setName(newName);
            }

            // Atualizar foto de perfil se fornecida (pode ser null para remover)
            if (request.profilePhotoUrl() != null) {
                String newPhotoUrl = request.profilePhotoUrl().trim().isEmpty() ? null : request.profilePhotoUrl().trim();
                log.info("[UPDATE PROFILE] Updating profile photo for user {}: {} -> {}", 
                        user.getEmail(), user.getProfilePhotoUrl(), newPhotoUrl);
                user.setProfilePhotoUrl(newPhotoUrl);
            }

            // Salvar alterações
            userRepositoryPort.save(user);

            log.info("[UPDATE PROFILE] ✅ Profile updated successfully for user: {}", user.getEmail());

            // Retornar perfil atualizado
            String provider = user.getAuthProvider();
            if (provider == null || provider.trim().isEmpty()) {
                provider = "LOCAL";
            }

            var updatedProfile = new ProfileResponseDTO(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    provider,
                    user.isPasswordSet(),
                    user.getProfilePhotoUrl(),
                    user.getAuthorId(),
                    user.getEcommerceUrl()
            );

            return ResponseEntity.ok(updatedProfile);

        } catch (IllegalArgumentException e) {
            log.error("[UPDATE PROFILE] ❌ User not found: {}", userDetails.getUsername());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("User not found"));
        } catch (Exception e) {
            log.error("[UPDATE PROFILE] ❌ Error updating profile: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erro ao atualizar perfil: " + e.getMessage()));
        }
    }

    @DeleteMapping("/account")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Transactional
    public ResponseEntity<MessageResponse> deleteOwnAccount(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Not authenticated"));
        }

        String requestId = UUID.randomUUID().toString().substring(0, 8);
        String userEmail = userDetails.getUsername();
        
        log.info("[DELETE OWN ACCOUNT REQUEST {}] User attempting to delete their own account: {}", requestId, userEmail);

        var userOpt = userRepositoryPort.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            log.warn("[DELETE OWN ACCOUNT ERROR {}] User not found: {}", requestId, userEmail);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("User not found"));
        }

        var user = userOpt.get();
        String userId = user.getId();

        // Limpar tokens relacionados antes de deletar o usuário
        log.info("[DELETE OWN ACCOUNT {}] Cleaning related tokens for user: {}", requestId, userId);
        try {
            passwordResetTokenRepository.deleteByUserId(userId);
            emailChangeTokenRepository.deleteByUserId(userId);
            log.info("[DELETE OWN ACCOUNT {}] All tokens cleaned successfully", requestId);
        } catch (Exception e) {
            log.warn("[DELETE OWN ACCOUNT {}] Warning: Some tokens could not be deleted: {}", requestId, e.getMessage());
            // Continuar com a deleção mesmo se alguns tokens não forem deletados
        }

        // Enviar email de notificação de deleção antes de deletar o usuário
        log.info("[DELETE OWN ACCOUNT {}] Sending delete account email to {} (name: {})", requestId, user.getEmail(), user.getName());
        try {
            eventPublisher.publishAuthEvent("auth.account.delete", new AuthEmailEvent(
                    "auth.account.delete", user.getName(), user.getEmail(), null, null, null, null));
            log.info("[DELETE OWN ACCOUNT {}] ✅ Delete account email successfully sent to {}", requestId, user.getEmail());
        } catch (Exception e) {
            log.error("[DELETE OWN ACCOUNT {}] ❌ Failed to send delete account email to {}: {}", requestId, user.getEmail(), e.getMessage(), e);
            // Continuar com a deleção mesmo se o email falhar
        }

        // Deletar o usuário
        userRepositoryPort.deleteById(userId);
        
        log.info("[DELETE OWN ACCOUNT SUCCESS {}] User deleted their own account successfully: {} ({})", 
                requestId, user.getEmail(), userId);

        return ResponseEntity.ok(new MessageResponse(
                "Sua conta foi deletada com sucesso."
        ));
    }

    public record UserDashboardResponse(
            String panel,
            String userName,
            String userEmail,
            String authProvider,
            boolean passwordSet,
            String message
    ) {}

    public record MessageResponse(String message) {}

    public record UpdateProfileRequest(
            String name,  // Nome do usuário (opcional, mas se fornecido deve ser válido)
            String profilePhotoUrl  // URL da foto de perfil (opcional, pode ser null ou string vazia para remover)
    ) {}
}

