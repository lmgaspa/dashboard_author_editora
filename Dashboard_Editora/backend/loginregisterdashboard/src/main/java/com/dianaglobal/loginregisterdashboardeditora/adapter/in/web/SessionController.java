package com.dianaglobal.loginregisterdashboardeditora.adapter.in.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dianaglobal.loginregisterdashboardeditora.adapter.in.dto.ApiError;
import com.dianaglobal.loginregisterdashboardeditora.adapter.in.dto.login.LoginRequest;
import com.dianaglobal.loginregisterdashboardeditora.adapter.in.dto.login.LoginResponse;
import com.dianaglobal.loginregisterdashboardeditora.adapter.in.dto.password.ForgotPasswordRequest;
import com.dianaglobal.loginregisterdashboardeditora.adapter.in.dto.password.ResetPasswordRequest;
import com.dianaglobal.loginregisterdashboardeditora.application.port.out.UserRepositoryPort;
import com.dianaglobal.loginregisterdashboardeditora.application.service.JwtService;
import com.dianaglobal.loginregisterdashboardeditora.application.service.PasswordResetService;
import com.dianaglobal.loginregisterdashboardeditora.config.ApiPaths;
import com.dianaglobal.loginregisterdashboardeditora.domain.model.User;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
@RequestMapping(ApiPaths.AUTH_BASE) // "/api/v1/auth"
@RequiredArgsConstructor
@Validated
public class SessionController {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PasswordResetService passwordResetService;

    // URL base do frontend (pra montar links nos e-mails de confirmação / reset)
    @Value("${application.frontend.base-url}")
    private String frontendBaseUrl;

    // ------------------------------------------------------------------------------------
    // LOGIN COM SENHA LOCAL
    // ------------------------------------------------------------------------------------
    @PostMapping(value = "/login", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> login(
            @RequestBody @Valid LoginRequest request
    ) {

        var userOpt = userRepositoryPort.findByEmail(request.email().trim().toLowerCase());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Invalid credentials"));
        }
        User user = userOpt.get();

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Invalid credentials"));
        }

        // e-mail não confirmado => erro tipado EMAIL_UNCONFIRMED
        if (!user.isEmailConfirmed()) {
            var body = ApiError.builder()
                    .error("EMAIL_UNCONFIRMED")
                    .message("Unconfirmed email.")
                    .build();
            return ResponseEntity.status(HttpStatus.CONFLICT).body(body); // 409
        }

        // Gerar apenas JWT access token
        String access = jwtService.generateToken(user.getEmail());

        // Criar objeto UserInfo para a resposta
        var userInfo = new LoginResponse.UserInfo(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(), // ADMIN ou USER
                "LOCAL",
                true
        );

        return ResponseEntity.ok(new LoginResponse(access, userInfo));
    }

    // ------------------------------------------------------------------------------------
    // REGISTRO (DESABILITADO - Use /api/v1/admin/users)
    // ------------------------------------------------------------------------------------
    public record RegisterRequest(
            @NotBlank String name,
            @NotBlank
            @Email(message = "Invalid e-mail")
            @Pattern(
                    regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
                    message = "E-mail must contain a valid domain"
            )
            String email,
            @NotBlank String password
    ) {}

    @PostMapping(value = "/register", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest body) {
        try {
            final String normalizedEmail = body.email().trim().toLowerCase();

            // 1. já existe?
            var existing = userRepositoryPort.findByEmail(normalizedEmail);
            if (existing.isPresent()) {
                // conflito: e-mail já cadastrado
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new MessageResponse("This e-mail is already registered."));
            }

            // 2. Este endpoint requer ADMIN agora - não deve criar usuários aqui
            // Use /api/v1/admin/users para criar usuários
            throw new UnsupportedOperationException("Direct user registration is disabled. Use /api/v1/admin/users");

        } catch (IllegalArgumentException ex) {
            // validação de domínio, etc.
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(ex.getMessage()));
        } catch (RuntimeException ex) {
            log.error("[REGISTER] error: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Could not register now."));
        }
    }


    // ------------------------------------------------------------------------------------
    // ESQUECI MINHA SENHA / RESET DE SENHA POR TOKEN
    // ------------------------------------------------------------------------------------
    @PostMapping(value = "/forgot-password", consumes = "application/json")
    public ResponseEntity<?> forgotPassword(@RequestBody @Valid ForgotPasswordRequest req) {
        // dispara e-mail com link /reset-password?token=...
        passwordResetService.requestReset(req.email(), frontendBaseUrl);
        return ResponseEntity.noContent().build(); // 204
    }

    @PostMapping(value = "/reset-password", consumes = "application/json")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid ResetPasswordRequest req) {
        // valida token e troca a senha
        passwordResetService.resetPassword(req.token(), req.newPassword());
        return ResponseEntity.ok().build();
    }

    // ------------------------------------------------------------------------------------
    // LOGOUT
    // ------------------------------------------------------------------------------------
    @PostMapping(value = "/logout", produces = "application/json")
    public ResponseEntity<MessageResponse> logout() {
        // Logout simplificado - frontend deve remover o token JWT localmente
        return ResponseEntity.ok(new MessageResponse("Logged out successfully"));
    }

    // ------------------------------------------------------------------------------------
    // Pequeno DTO "genérico" de mensagem
    // ------------------------------------------------------------------------------------
    public record MessageResponse(String message) {}
}
