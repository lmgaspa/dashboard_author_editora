package com.dianaglobal.loginregisterdashboardeditora.adapter.in.web;

import com.dianaglobal.loginregisterdashboardeditora.adapter.in.dto.ProfileResponseDTO;
import com.dianaglobal.loginregisterdashboardeditora.adapter.out.mail.DeleteAccountEmailService;
import com.dianaglobal.loginregisterdashboardeditora.adapter.out.persistence.EmailChangeTokenRepository;
import com.dianaglobal.loginregisterdashboardeditora.adapter.out.persistence.PasswordResetTokenRepository;
import com.dianaglobal.loginregisterdashboardeditora.application.port.out.UserRepositoryPort;
import com.dianaglobal.loginregisterdashboardeditora.config.ApiPaths;
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
    private final DeleteAccountEmailService deleteAccountEmailService;

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
                user.isPasswordSet()
        );

        return ResponseEntity.ok(profile);
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
            deleteAccountEmailService.send(user.getEmail(), user.getName());
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
}

