package com.dianaglobal.loginregisterdashboardeditora.adapter.in.web;

import com.dianaglobal.loginregisterdashboardeditora.adapter.out.persistence.AccountConfirmationTokenRepository;
import com.dianaglobal.loginregisterdashboardeditora.adapter.out.persistence.EmailChangeTokenRepository;
import com.dianaglobal.loginregisterdashboardeditora.adapter.out.persistence.PasswordResetTokenRepository;
import com.dianaglobal.loginregisterdashboardeditora.adapter.out.persistence.RefreshTokenRepository;
import com.dianaglobal.loginregisterdashboardeditora.application.port.out.UserRepositoryPort;
import com.dianaglobal.loginregisterdashboardeditora.application.service.AccountConfirmationService;
import com.dianaglobal.loginregisterdashboardeditora.application.service.UserIdGeneratorService;
import com.dianaglobal.loginregisterdashboardeditora.config.ApiPaths;
import com.dianaglobal.loginregisterdashboardeditora.domain.model.Role;
import com.dianaglobal.loginregisterdashboardeditora.domain.model.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(ApiPaths.ADMIN_BASE)
@RequiredArgsConstructor
public class AdminController {

    private final UserRepositoryPort userRepositoryPort;
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final AccountConfirmationService accountConfirmationService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final AccountConfirmationTokenRepository accountConfirmationTokenRepository;
    private final EmailChangeTokenRepository emailChangeTokenRepository;
    private final UserIdGeneratorService userIdGeneratorService;
    
    @Value("${application.frontend.base-url}")
    private String frontendBaseUrl;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getDashboard(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Not authenticated"));
        }

        var admin = userRepositoryPort.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));

        DashboardResponse dashboard = new DashboardResponse(
                "Painel Administrativo",
                admin.getName(),
                admin.getEmail(),
                "Bem-vindo ao painel de administração"
        );

        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            var users = userRepositoryPort.findAll();
            
            List<UserListResponse> userList = users.stream()
                    .map(user -> new UserListResponse(
                            user.getId(),
                            user.getName(),
                            user.getEmail(),
                            user.getRole().name(),
                            user.isEmailConfirmed(),
                            user.getAuthProvider()
                    ))
                    .toList();
            
            return ResponseEntity.ok(new UsersListResponse(
                    "Lista de usuários",
                    userList.size(),
                    userList
            ));
        } catch (Exception e) {
            log.error("Erro ao listar usuários: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erro ao listar usuários: " + e.getMessage()));
        }
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<?> createUser(
            @RequestBody @Valid CreateUserRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            final String normalizedEmail = request.email().trim().toLowerCase();

            // Verificar se email já existe
            var existing = userRepositoryPort.findByEmail(normalizedEmail);
            if (existing.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new MessageResponse("Este e-mail já está cadastrado."));
            }

            // Criar novo usuário
            User newUser = new User();
            // Gera ID no formato "user-1", "user-2", etc.
            newUser.setId(userIdGeneratorService.generateNextUserId());
            newUser.setName(request.name());
            newUser.setEmail(normalizedEmail);
            newUser.setPassword(passwordEncoder.encode(request.password()));
            newUser.setPasswordSet(true); // Admin define senha, então passwordSet = true
            // Admin pode criar já confirmado (default: false se não especificado)
            newUser.setEmailConfirmed(request.emailConfirmed() != null ? request.emailConfirmed() : false);
            newUser.setAuthProvider("LOCAL");
            // Admin pode definir role (default: USER se não especificado)
            Role userRole = Role.USER;
            if (request.role() != null && !request.role().isBlank()) {
                try {
                    userRole = Role.valueOf(request.role().toUpperCase());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new MessageResponse("Role inválido. Use 'USER' ou 'ADMIN'."));
                }
            }
            newUser.setRole(userRole);

            userRepositoryPort.save(newUser);

            // Se não estiver confirmado, enviar email de confirmação
            if (!newUser.isEmailConfirmed()) {
                accountConfirmationService.requestConfirmation(
                        normalizedEmail,
                        frontendBaseUrl
                );
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new CreateUserResponse(
                            "Usuário criado com sucesso",
                            newUser.getId(),
                            newUser.getName(),
                            newUser.getEmail(),
                            newUser.getRole().name(),
                            newUser.isEmailConfirmed()
                    )
            );

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erro ao criar usuário: " + ex.getMessage()));
        }
    }

    @GetMapping("/admin-info")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAdminInfo() {
        try {
            // Buscar todos os admins (sem created_at pois não existe na tabela)
            List<Map<String, Object>> admins = jdbcTemplate.queryForList(
                "SELECT id, name, email, role, email_confirmed, auth_provider " +
                "FROM users " +
                "WHERE role = 'ADMIN' " +
                "ORDER BY email"
            );

            return ResponseEntity.ok(new AdminInfoResponse(
                "Informações dos administradores",
                admins.size(),
                admins
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erro ao buscar informações do admin: " + e.getMessage()));
        }
    }

    @GetMapping("/database/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getDatabaseStatus() {
        try {
            // Verificar tabelas
            List<Map<String, Object>> tables = jdbcTemplate.queryForList(
                "SELECT table_name, table_type " +
                "FROM information_schema.tables " +
                "WHERE table_schema = 'public' " +
                "ORDER BY table_name"
            );

            // Contar registros nas tabelas principais
            Long usersCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users", Long.class
            );
            Long adminCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE role = 'ADMIN'", Long.class
            );

            // Verificar versão do Flyway
            List<Map<String, Object>> flywayVersions = jdbcTemplate.queryForList(
                "SELECT version, description, installed_on, success " +
                "FROM flyway_schema_history " +
                "ORDER BY installed_rank DESC " +
                "LIMIT 5"
            );

            DatabaseStatusResponse status = new DatabaseStatusResponse(
                "Banco de dados conectado com sucesso",
                tables.size(),
                tables,
                usersCount,
                adminCount,
                flywayVersions
            );

            return ResponseEntity.ok(status);
        } catch (Exception e) {
            e.printStackTrace(); // Log do erro
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erro ao verificar banco de dados: " + e.getMessage()));
        }
    }

    @DeleteMapping("/users/{identifier}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(
            @PathVariable String identifier,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            String requestId = UUID.randomUUID().toString();
            log.info("[DELETE USER REQUEST {}] Attempt to delete user: {}", requestId, identifier);

            // Verificar se o admin não está tentando deletar a si mesmo
            String adminEmail = userDetails.getUsername();
            
            // Tentar encontrar o usuário por ID ou email
            User userToDelete;
            String userId;
            
            // Tentar como ID primeiro (admin-1, user-1, etc.)
            var userById = userRepositoryPort.findById(identifier);
            if (userById.isPresent()) {
                userToDelete = userById.get();
                userId = userToDelete.getId();
            } else {
                // Se não for ID, tentar como email
                String normalizedEmail = identifier.trim().toLowerCase();
                userToDelete = userRepositoryPort.findByEmail(normalizedEmail)
                        .orElseThrow(() -> new IllegalArgumentException("User not found"));
                userId = userToDelete.getId();
            }

            // Verificar se o admin está tentando deletar a si mesmo
            if (userToDelete.getEmail().equalsIgnoreCase(adminEmail)) {
                log.warn("[DELETE USER ERROR {}] Admin trying to delete themselves: {}", requestId, adminEmail);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse("Você não pode deletar sua própria conta."));
            }

            // Limpar tokens relacionados antes de deletar o usuário
            log.info("[DELETE USER {}] Cleaning related tokens for user: {}", requestId, userId);
            try {
                // Refresh tokens usa email, não userId
                refreshTokenRepository.deleteByEmail(userToDelete.getEmail());
                passwordResetTokenRepository.deleteByUserId(userId);
                accountConfirmationTokenRepository.deleteByUserId(userId);
                emailChangeTokenRepository.deleteByUserId(userId);
                log.info("[DELETE USER {}] All tokens cleaned successfully", requestId);
            } catch (Exception e) {
                log.warn("[DELETE USER {}] Warning: Some tokens could not be deleted: {}", requestId, e.getMessage());
                // Continuar com a deleção mesmo se alguns tokens não forem deletados
            }

            // Deletar o usuário
            userRepositoryPort.deleteById(userId);
            
            log.info("[DELETE USER SUCCESS {}] User deleted successfully: {} ({})", 
                    requestId, userToDelete.getEmail(), userId);

            return ResponseEntity.ok(new MessageResponse(
                    String.format("Usuário %s deletado com sucesso.", userToDelete.getEmail())
            ));

        } catch (IllegalArgumentException e) {
            log.warn("[DELETE USER ERROR] User not found: {}", identifier);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Usuário não encontrado."));
        } catch (Exception e) {
            log.error("[DELETE USER ERROR] Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erro ao deletar usuário: " + e.getMessage()));
        }
    }

    public record DashboardResponse(
            String panel,
            String adminName,
            String adminEmail,
            String message
    ) {}

    public record MessageResponse(String message) {}

    public record DatabaseStatusResponse(
            String message,
            Integer totalTables,
            List<Map<String, Object>> tables,
            Long totalUsers,
            Long totalAdmins,
            List<Map<String, Object>> flywayVersions
    ) {}

    public record AdminInfoResponse(
            String message,
            Integer totalAdmins,
            List<Map<String, Object>> admins
    ) {}

    public record CreateUserRequest(
            @NotBlank(message = "Nome é obrigatório")
            String name,
            @NotBlank(message = "Email é obrigatório")
            @Email(message = "Email inválido")
            @Pattern(
                    regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
                    message = "Email deve conter um domínio válido"
            )
            String email,
            @NotBlank(message = "Senha é obrigatória")
            String password,
            Boolean emailConfirmed,  // Admin pode criar já confirmado
            String role              // Admin pode definir role (USER ou ADMIN)
    ) {}

    public record CreateUserResponse(
            String message,
            String id,  // Format: "user-1", "user-2", etc.
            String name,
            String email,
            String role,
            Boolean emailConfirmed
    ) {}

    public record UserListResponse(
            String id,
            String name,
            String email,
            String role,
            Boolean emailConfirmed,
            String authProvider
    ) {}

    public record UsersListResponse(
            String message,
            Integer total,
            List<UserListResponse> users
    ) {}
}

