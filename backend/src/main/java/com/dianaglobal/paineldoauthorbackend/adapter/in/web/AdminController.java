package com.dianaglobal.paineldoauthorbackend.adapter.in.web;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dianaglobal.paineldoauthorbackend.adapter.out.mail.DeleteAccountEmailService;
import com.dianaglobal.paineldoauthorbackend.adapter.out.persistence.EmailChangeTokenRepository;
import com.dianaglobal.paineldoauthorbackend.adapter.out.persistence.PasswordResetTokenRepository;
import com.dianaglobal.paineldoauthorbackend.application.port.out.UserRepositoryPort;
import com.dianaglobal.paineldoauthorbackend.application.service.AuthorStatsService;
import com.dianaglobal.paineldoauthorbackend.application.service.UserIdGeneratorService;
import com.dianaglobal.paineldoauthorbackend.config.ApiPaths;
import com.dianaglobal.paineldoauthorbackend.domain.model.Role;
import com.dianaglobal.paineldoauthorbackend.domain.model.User;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(ApiPaths.ADMIN_BASE)
@RequiredArgsConstructor
public class AdminController {

    private final UserRepositoryPort userRepositoryPort;
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailChangeTokenRepository emailChangeTokenRepository;
    private final UserIdGeneratorService userIdGeneratorService;
    private final com.dianaglobal.paineldoauthorbackend.application.event.UserConfirmedListener userConfirmedListener;
    private final DeleteAccountEmailService deleteAccountEmailService;
    private final AuthorStatsService authorStatsService;
    
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
                            user.getAuthProvider(),
                            user.getAuthorId(),
                            user.getEcommerceUrl(),
                            user.getEcommerceDbUrl(),
                            user.getEcommerceDbUsername(),
                            user.getEcommerceDbPassword(),  // Retornar password persistido para admin editar
                            user.getProfilePhotoUrl()
                    ))
                    .collect(Collectors.toList());
            
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
            // Usuários criados por admin são sempre confirmados (não há mais fluxo de confirmação)
            boolean emailConfirmed = request.emailConfirmed() == null || Boolean.TRUE.equals(request.emailConfirmed());
            newUser.setEmailConfirmed(emailConfirmed);
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
            
            // Admin pode definir author_id (opcional)
            if (request.authorId() != null && !request.authorId().trim().isEmpty()) {
                newUser.setAuthorId(request.authorId().trim());
            }
            
            // Admin pode definir ecommerce_url (opcional - URL base do e-commerce do autor)
            if (request.ecommerceUrl() != null && !request.ecommerceUrl().trim().isEmpty()) {
                newUser.setEcommerceUrl(request.ecommerceUrl().trim());
            }
            
            // Admin pode definir credenciais do banco do e-commerce (opcional)
            if (request.ecommerceDbUrl() != null && !request.ecommerceDbUrl().trim().isEmpty()) {
                newUser.setEcommerceDbUrl(request.ecommerceDbUrl().trim());
            }
            if (request.ecommerceDbUsername() != null && !request.ecommerceDbUsername().trim().isEmpty()) {
                newUser.setEcommerceDbUsername(request.ecommerceDbUsername().trim());
            }
            if (request.ecommerceDbPassword() != null && !request.ecommerceDbPassword().trim().isEmpty()) {
                newUser.setEcommerceDbPassword(request.ecommerceDbPassword().trim());
            }

            userRepositoryPort.save(newUser);

            // Sempre enviar welcome email quando admin cria usuário (independente de emailConfirmed)
            log.info("[ADMIN CREATE USER] Sending welcome email to {} (name: {}, emailConfirmed: {})", 
                    normalizedEmail, newUser.getName(), newUser.isEmailConfirmed());
            try {
                userConfirmedListener.onUserConfirmed(newUser, request.password());
                log.info("[ADMIN CREATE USER] ✅ Welcome email successfully sent to {}", normalizedEmail);
            } catch (Exception e) {
                log.error("[ADMIN CREATE USER] ❌ Failed to send welcome email to {}: {}", normalizedEmail, e.getMessage(), e);
                // Não falhar a criação do usuário se o email falhar, apenas logar o erro
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new CreateUserResponse(
                            "Usuário criado com sucesso",
                            newUser.getId(),
                            newUser.getName(),
                            newUser.getEmail(),
                    newUser.getRole().name(),
                    newUser.isEmailConfirmed(),
                    newUser.getAuthorId(),
                    newUser.getEcommerceUrl(),
                    newUser.getEcommerceDbUrl(),
                    newUser.getEcommerceDbUsername(),
                    newUser.getEcommerceDbPassword()  // Retornar password persistido para admin visualizar
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
            // Buscar todos os admins usando o repositório
            var admins = userRepositoryPort.findAllByRole(Role.ADMIN);
            
            List<UserListResponse> adminList = admins.stream()
                    .map(admin -> new UserListResponse(
                            admin.getId(),
                            admin.getName(),
                            admin.getEmail(),
                            admin.getRole().name(),
                            admin.isEmailConfirmed(),
                            admin.getAuthProvider(),
                            admin.getAuthorId(),
                            admin.getEcommerceUrl(),
                            admin.getEcommerceDbUrl(),
                            admin.getEcommerceDbUsername(),
                            admin.getEcommerceDbPassword(),  // Retornar password persistido para admin editar
                            admin.getProfilePhotoUrl()
                    ))
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(new UsersListResponse(
                    "Lista de administradores",
                    adminList.size(),
                    adminList
            ));
        } catch (Exception e) {
            log.error("Erro ao listar administradores: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erro ao buscar informações dos administradores: " + e.getMessage()));
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
        } catch (DataAccessException dae) {
            log.error("Erro de acesso ao banco ao verificar status: {}", dae.getMessage(), dae);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erro ao acessar o banco de dados: " + dae.getMessage()));
        } catch (Exception e) {
            log.error("Erro inesperado ao verificar status do banco: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erro ao verificar banco de dados: " + e.getMessage()));
        }
    }

    @PutMapping("/users/{identifier}/confirm-email")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<?> confirmUserEmail(
            @PathVariable String identifier
    ) {
        try {
            // Tentar encontrar o usuário por ID ou email
            User user;
            var userById = userRepositoryPort.findById(identifier);
            if (userById.isPresent()) {
                user = userById.get();
            } else {
                String normalizedEmail = identifier.trim().toLowerCase();
                user = userRepositoryPort.findByEmail(normalizedEmail)
                        .orElseThrow(() -> new IllegalArgumentException("User not found"));
            }

            if (user.isEmailConfirmed()) {
                return ResponseEntity.ok(new MessageResponse(
                        String.format("Email do usuário %s já está confirmado.", user.getEmail())
                ));
            }

            user.setEmailConfirmed(true);
            userRepositoryPort.save(user);

            log.info("[CONFIRM EMAIL] Email confirmed for user: {} ({})", user.getEmail(), user.getId());

            return ResponseEntity.ok(new MessageResponse(
                    String.format("Email do usuário %s confirmado com sucesso.", user.getEmail())
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Usuário não encontrado."));
        } catch (Exception e) {
            log.error("[CONFIRM EMAIL ERROR] Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erro ao confirmar email: " + e.getMessage()));
        }
    }

    @PutMapping("/users/{identifier}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<?> updateUser(
            @PathVariable String identifier,
            @RequestBody @Valid UpdateUserRequest request
    ) {
        try {
            // Tentar encontrar o usuário por ID ou email
            User user;
            var userById = userRepositoryPort.findById(identifier);
            if (userById.isPresent()) {
                user = userById.get();
            } else {
                String normalizedEmail = identifier.trim().toLowerCase();
                user = userRepositoryPort.findByEmail(normalizedEmail)
                        .orElseThrow(() -> new IllegalArgumentException("User not found"));
            }

            // Atualizar campos se fornecidos
            boolean changed = false;
            
            if (request.name() != null && !request.name().trim().isEmpty()) {
                user.setName(request.name().trim());
                changed = true;
            }
            
            if (request.role() != null && !request.role().isBlank()) {
                try {
                    Role newRole = Role.valueOf(request.role().toUpperCase());
                    user.setRole(newRole);
                    changed = true;
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new MessageResponse("Role inválido. Use 'USER' ou 'ADMIN'."));
                }
            }
            
            // author_id pode ser atualizado (incluindo null para remover)
            if (request.authorId() != null) {
                // Se for string vazia, trata como null (remove author_id)
                String newAuthorId = request.authorId().trim().isEmpty() ? null : request.authorId().trim();
                user.setAuthorId(newAuthorId);
                changed = true;
            }
            
            // ecommerce_url pode ser atualizado (incluindo null para remover)
            if (request.ecommerceUrl() != null) {
                // Se for string vazia, trata como null (remove ecommerce_url)
                String newEcommerceUrl = request.ecommerceUrl().trim().isEmpty() ? null : request.ecommerceUrl().trim();
                user.setEcommerceUrl(newEcommerceUrl);
                changed = true;
            }
            
            // Credenciais do banco do e-commerce podem ser atualizadas
            if (request.ecommerceDbUrl() != null) {
                String newDbUrl = request.ecommerceDbUrl().trim().isEmpty() ? null : request.ecommerceDbUrl().trim();
                user.setEcommerceDbUrl(newDbUrl);
                changed = true;
            }
            if (request.ecommerceDbUsername() != null) {
                String newDbUsername = request.ecommerceDbUsername().trim().isEmpty() ? null : request.ecommerceDbUsername().trim();
                user.setEcommerceDbUsername(newDbUsername);
                changed = true;
            }
            // ecommerceDbPassword pode ser atualizado
            // IMPORTANTE: Se vier string vazia, remove o password
            // Se vier null no request, NÃO altera o password atual (mantém o que está no banco)
            // Se vier um valor não vazio, atualiza com o novo password
            if (request.ecommerceDbPassword() != null) {
                String newDbPassword = request.ecommerceDbPassword().trim().isEmpty() ? null : request.ecommerceDbPassword().trim();
                // Só marca como changed se realmente mudou
                if (!java.util.Objects.equals(user.getEcommerceDbPassword(), newDbPassword)) {
                    user.setEcommerceDbPassword(newDbPassword);
                    changed = true;
                }
            }
            // Se request.ecommerceDbPassword() for null, mantém o password atual do banco (não altera)

            // profile_photo_url pode ser atualizado (incluindo null para remover)
            if (request.profilePhotoUrl() != null) {
                String newPhotoUrl = request.profilePhotoUrl().trim().isEmpty() ? null : request.profilePhotoUrl().trim();
                user.setProfilePhotoUrl(newPhotoUrl);
                changed = true;
            }
            
            // password pode ser atualizado pelo admin (sem precisar da senha atual)
            // Admin pode mudar a senha de qualquer usuário livremente
            if (request.password() != null && !request.password().trim().isEmpty()) {
                String newPassword = request.password().trim();
                // Validar força da senha
                if (newPassword.length() < 8) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new MessageResponse("Password must be at least 8 characters long"));
                }
                boolean hasUpper = newPassword.chars().anyMatch(Character::isUpperCase);
                boolean hasLower = newPassword.chars().anyMatch(Character::isLowerCase);
                boolean hasDigit = newPassword.chars().anyMatch(Character::isDigit);
                if (!hasUpper) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new MessageResponse("Password must include at least 1 uppercase letter"));
                }
                if (!hasLower) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new MessageResponse("Password must include at least 1 lowercase letter"));
                }
                if (!hasDigit) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new MessageResponse("Password must include at least 1 digit"));
                }
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setPasswordSet(true);
                changed = true;
                log.info("[UPDATE USER] Admin changed password for user: {} ({})", user.getEmail(), user.getId());
            }

            if (changed) {
                userRepositoryPort.save(user);
                log.info("[UPDATE USER] User updated: {} ({})", user.getEmail(), user.getId());
            }

            // Retornar usuário atualizado
            // IMPORTANTE: Retornar o password persistido do banco para o admin poder visualizar/editar
            // Se o password foi alterado no request, retornar o novo valor salvo
            // Se não foi alterado, retornar o valor atual do banco
            String passwordToReturn = user.getEcommerceDbPassword();
            
            var updatedUser = new UserListResponse(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getRole().name(),
                    user.isEmailConfirmed(),
                    user.getAuthProvider(),
                    user.getAuthorId(),
                    user.getEcommerceUrl(),
                    user.getEcommerceDbUrl(),
                    user.getEcommerceDbUsername(),
                    passwordToReturn,  // Retornar password persistido para admin editar
                    user.getProfilePhotoUrl()
            );

            return ResponseEntity.ok(updatedUser);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Usuário não encontrado."));
        } catch (Exception e) {
            log.error("[UPDATE USER ERROR] Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erro ao atualizar usuário: " + e.getMessage()));
        }
    }

    @GetMapping("/users/by-author/{authorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUsersByAuthorId(@PathVariable String authorId) {
        try {
            var users = userRepositoryPort.findAllByAuthorId(authorId);
            
            List<UserListResponse> userList = users.stream()
                    .map(user -> new UserListResponse(
                            user.getId(),
                            user.getName(),
                            user.getEmail(),
                            user.getRole().name(),
                            user.isEmailConfirmed(),
                            user.getAuthProvider(),
                            user.getAuthorId(),
                            user.getEcommerceUrl(),
                            user.getEcommerceDbUrl(),
                            user.getEcommerceDbUsername(),
                            user.getEcommerceDbPassword(),  // Retornar password persistido para admin editar
                            user.getProfilePhotoUrl()
                    ))
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(new UsersListResponse(
                    "Usuários encontrados para author_id: " + authorId,
                    userList.size(),
                    userList
            ));

        } catch (Exception e) {
            log.error("Erro ao buscar usuários por author_id: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erro ao buscar usuários: " + e.getMessage()));
        }
    }

    /**
     * Busca estatísticas completas de um autor no e-commerce
     * Usa o author_id do usuário logado ou fornecido
     */
    /**
     * Busca estatísticas completas de um autor no e-commerce
     * Requer que o usuário tenha as credenciais do banco configuradas
     */
    @GetMapping("/user/{identifier}/author-stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserAuthorStats(@PathVariable String identifier) {
        try {
            // Buscar usuário por ID ou email
            User user;
            var userById = userRepositoryPort.findById(identifier);
            if (userById.isPresent()) {
                user = userById.get();
            } else {
                String normalizedEmail = identifier.trim().toLowerCase();
                user = userRepositoryPort.findByEmail(normalizedEmail)
                        .orElseThrow(() -> new IllegalArgumentException("User not found"));
            }

            if (user.getAuthorId() == null || user.getAuthorId().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("Usuário não possui author_id configurado"));
            }

            if (user.getEcommerceDbUrl() == null || user.getEcommerceDbUrl().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("Usuário não possui credenciais do banco do e-commerce configuradas"));
            }

            Long authorId;
            try {
                authorId = Long.parseLong(user.getAuthorId());
            } catch (NumberFormatException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("author_id inválido: " + user.getAuthorId()));
            }
            
            var statsOpt = authorStatsService.getAuthorStats(
                    authorId,
                    user.getEcommerceDbUrl(),
                    user.getEcommerceDbUsername(),
                    user.getEcommerceDbPassword()
            );
            
            if (statsOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new MessageResponse("Autor não encontrado ou sem dados no e-commerce"));
            }

            var stats = statsOpt.get();
            var recentSalesOpt = authorStatsService.getRecentSales(
                    authorId,
                    user.getEcommerceDbUrl(),
                    user.getEcommerceDbUsername(),
                    user.getEcommerceDbPassword()
            );
        
            var response = new AuthorStatsResponse(
                    stats.getAuthorId(),
                    stats.getAuthorName(),
                    stats.getEmail(),
                    stats.getTotalBooks(),
                    stats.getCompletedOrders(),
                    stats.getTotalRevenue(),
                    stats.getTotalPayouts(),
                    stats.getTotalPaid(),
                    stats.getHasPaymentAccount(),
                    recentSalesOpt.map(rs -> rs.recentOrders()).orElse(0L),
                    recentSalesOpt.map(rs -> rs.recentRevenue()).orElse(java.math.BigDecimal.ZERO)
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Usuário não encontrado"));
        } catch (Exception e) {
            log.error("[USER AUTHOR STATS] Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erro ao buscar estatísticas: " + e.getMessage()));
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

            // ❌ BLOQUEAR: Não permitir deletar usuários com role ADMIN
            if (userToDelete.getRole() == com.dianaglobal.paineldoauthorbackend.domain.model.Role.ADMIN) {
                log.warn("[DELETE USER ERROR {}] Attempt to delete ADMIN user: {} ({})", 
                        requestId, userToDelete.getEmail(), userId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse("Não é possível deletar usuários administradores."));
            }

            // Limpar tokens relacionados antes de deletar o usuário
            log.info("[DELETE USER {}] Cleaning related tokens for user: {}", requestId, userId);
            try {
                passwordResetTokenRepository.deleteByUserId(userId);
                emailChangeTokenRepository.deleteByUserId(userId);
                log.info("[DELETE USER {}] All tokens cleaned successfully", requestId);
            } catch (Exception e) {
                log.warn("[DELETE USER {}] Warning: Some tokens could not be deleted: {}", requestId, e.getMessage());
                // Continuar com a deleção mesmo se alguns tokens não forem deletados
            }

            // Enviar email de notificação de deleção ANTES de deletar o usuário
            log.info("[DELETE USER {}] Attempting to send delete account email to {} (name: {})", 
                    requestId, userToDelete.getEmail(), userToDelete.getName());
            try {
                deleteAccountEmailService.send(userToDelete.getEmail(), userToDelete.getName());
                log.info("[DELETE USER {}] ✅ Delete account email successfully sent to {}", requestId, userToDelete.getEmail());
            } catch (org.springframework.mail.MailSendException e) {
                log.error("[DELETE USER {}] ❌ MailSendException when sending delete account email to {}: {}", 
                        requestId, userToDelete.getEmail(), e.getMessage(), e);
                // Continuar com a deleção mesmo se o email falhar
            } catch (Exception e) {
                log.error("[DELETE USER {}] ❌ Unexpected error sending delete account email to {}: {} - {}", 
                        requestId, userToDelete.getEmail(), e.getClass().getSimpleName(), e.getMessage(), e);
                // Continuar com a deleção mesmo se o email falhar
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
            String role,             // Admin pode definir role (USER ou ADMIN)
            String authorId,         // ID do autor no sistema externo (opcional)
            String ecommerceUrl,     // URL base do e-commerce do autor (opcional)
            String ecommerceDbUrl,   // JDBC URL do banco do e-commerce (opcional)
            String ecommerceDbUsername, // Username do banco do e-commerce (opcional)
            String ecommerceDbPassword  // Password do banco do e-commerce (opcional)
    ) {}

    public record CreateUserResponse(
            String message,
            String id,  // Format: "user-1", "user-2", etc.
            String name,
            String email,
            String role,
            Boolean emailConfirmed,
            String authorId,        // ID do autor no sistema externo (pode ser null)
            String ecommerceUrl,    // URL base do e-commerce do autor (pode ser null)
            String ecommerceDbUrl,  // JDBC URL do banco do e-commerce (pode ser null)
            String ecommerceDbUsername, // Username do banco (pode ser null)
            String ecommerceDbPassword  // Password do banco (persistido, retornado para admin visualizar)
    ) {}

    public record UserListResponse(
            String id,
            String name,
            String email,
            String role,
            Boolean emailConfirmed,
            String authProvider,
            String authorId,        // ID do autor no sistema externo (pode ser null)
            String ecommerceUrl,    // URL base do e-commerce do autor (pode ser null)
            String ecommerceDbUrl,  // JDBC URL do banco (pode ser null)
            String ecommerceDbUsername, // Username do banco (pode ser null)
            String ecommerceDbPassword,  // Password do banco (persistido, retornado para admin editar)
            String profilePhotoUrl  // URL da foto de perfil (pode ser null)
    ) {}

    public record UsersListResponse(
            String message,
            Integer total,
            List<UserListResponse> users
    ) {}

    public record UpdateUserRequest(
            String name,         // Opcional: atualizar nome
            String role,         // Opcional: atualizar role (USER ou ADMIN)
            String password,     // Opcional: atualizar senha do usuário (admin pode mudar livremente, validação de força aplicada)
            String authorId,     // Opcional: atualizar author_id (pode ser null ou string vazia para remover)
            String ecommerceUrl, // Opcional: atualizar ecommerce_url (pode ser null ou string vazia para remover)
            String ecommerceDbUrl,     // Opcional: atualizar URL do banco
            String ecommerceDbUsername, // Opcional: atualizar username do banco
            String ecommerceDbPassword,  // Opcional: atualizar password do banco
            String profilePhotoUrl  // Opcional: atualizar foto de perfil (pode ser null ou string vazia para remover)
    ) {}

    public record AuthorStatsResponse(
            Long authorId,
            String authorName,
            String email,
            Long totalBooks,
            Long completedOrders,
            java.math.BigDecimal totalRevenue,
            Long totalPayouts,
            java.math.BigDecimal totalPaid,
            Boolean hasPaymentAccount,
            Long recentOrders,           // Últimos 30 dias
            java.math.BigDecimal recentRevenue  // Últimos 30 dias
    ) {}
}

