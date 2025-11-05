package com.dianaglobal.loginregisterdashboardeditora.adapter.in.web;

import com.dianaglobal.loginregisterdashboardeditora.adapter.in.dto.ProfileResponseDTO;
import com.dianaglobal.loginregisterdashboardeditora.application.port.out.UserRepositoryPort;
import com.dianaglobal.loginregisterdashboardeditora.config.ApiPaths;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiPaths.USER_PANEL_BASE)
@RequiredArgsConstructor
public class UserPanelController {

    private final UserRepositoryPort userRepositoryPort;

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

