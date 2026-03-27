package com.dianaglobal.paineldoauthorbackend.adapter.in.web;

import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.AuthorPaymentDTO;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.PaymentSummaryDTO;
import com.dianaglobal.paineldoauthorbackend.application.port.out.UserRepositoryPort;
import com.dianaglobal.paineldoauthorbackend.application.service.CurrentAuthorService;
import com.dianaglobal.paineldoauthorbackend.application.service.PaymentQueryService;
import com.dianaglobal.paineldoauthorbackend.config.ApiPaths;
import com.dianaglobal.paineldoauthorbackend.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controller para endpoints de pagamentos do admin.
 * Permite que admins vejam pagamentos de qualquer autor.
 */
@Slf4j
@RestController
@RequestMapping(ApiPaths.API_V1_BASE + "/admin/payments")
@RequiredArgsConstructor
public class AdminPaymentsController {

    private final CurrentAuthorService currentAuthorService;
    private final PaymentQueryService paymentQueryService;
    private final UserRepositoryPort userRepositoryPort;

    public record MessageResponse(String message) {}

    /**
     * GET /api/v1/admin/payments/author/{authorId}/summary
     * Retorna resumo de pagamentos de um autor específico (apenas admin).
     */
    @GetMapping("/author/{authorId}/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAuthorPaymentSummary(
            @PathVariable Long authorId,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        try {
            // Verificar se o usuário atual é admin (já garantido pelo @PreAuthorize, mas validamos aqui também)
            if (!currentAuthorService.isCurrentUserAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse("Acesso negado: apenas administradores"));
            }

            // Obter usuário admin para pegar credenciais do banco
            // Admins podem usar suas próprias credenciais ou buscar um usuário associado ao autor
            // Por enquanto, vamos buscar um usuário que tenha este author_id para pegar as credenciais
            var usersWithAuthorId = userRepositoryPort.findAllByAuthorId(authorId.toString());
            
            if (usersWithAuthorId.isEmpty()) {
                // Se não houver usuário com este author_id, tentar usar as credenciais do admin atual
                // Mas isso pode não funcionar se cada autor tem seu próprio banco
                Optional<User> currentUserOpt = currentAuthorService.getCurrentUser();
                if (currentUserOpt.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(new MessageResponse("Usuário não encontrado"));
                }

                User adminUser = currentUserOpt.get();
                if (adminUser.getEcommerceDbUrl() == null || adminUser.getEcommerceDbUrl().trim().isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new MessageResponse("Nenhum usuário encontrado para este autor e credenciais do banco não configuradas"));
                }

                // Tentar usar credenciais do admin (assumindo que é um banco compartilhado ou admin tem acesso)
                Optional<PaymentSummaryDTO> summaryOpt = paymentQueryService.getPaymentSummary(
                        authorId,
                        adminUser.getEcommerceDbUrl(),
                        adminUser.getEcommerceDbUsername(),
                        adminUser.getEcommerceDbPassword(),
                        limit
                );

                if (summaryOpt.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new MessageResponse("Autor não encontrado ou sem dados no e-commerce"));
                }

                return ResponseEntity.ok(summaryOpt.get());
            }

            // Usar credenciais do primeiro usuário associado ao autor
            User userWithAuthor = usersWithAuthorId.get(0);
            if (userWithAuthor.getEcommerceDbUrl() == null || userWithAuthor.getEcommerceDbUrl().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("Credenciais do banco do e-commerce não configuradas para este autor"));
            }

            // Buscar resumo de pagamentos
            Optional<PaymentSummaryDTO> summaryOpt = paymentQueryService.getPaymentSummary(
                    authorId,
                    userWithAuthor.getEcommerceDbUrl(),
                    userWithAuthor.getEcommerceDbUsername(),
                    userWithAuthor.getEcommerceDbPassword(),
                    limit
            );

            if (summaryOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new MessageResponse("Autor não encontrado ou sem dados no e-commerce"));
            }

            return ResponseEntity.ok(summaryOpt.get());

        } catch (Exception e) {
            log.error("[ADMIN PAYMENTS] Error fetching payment summary for author {}: {}", authorId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erro ao buscar pagamentos: " + e.getMessage()));
        }
    }

    /**
     * GET /api/v1/admin/payments/author/{authorId}/details
     * Retorna lista detalhada de pagamentos de um autor específico (apenas admin).
     */
    @GetMapping("/author/{authorId}/details")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAuthorPaymentDetails(
            @PathVariable Long authorId,
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "50") int limit
    ) {
        try {
            // Verificar se o usuário atual é admin
            if (!currentAuthorService.isCurrentUserAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse("Acesso negado: apenas administradores"));
            }

            // Buscar usuário associado ao autor para obter credenciais
            var usersWithAuthorId = userRepositoryPort.findAllByAuthorId(authorId.toString());
            
            if (usersWithAuthorId.isEmpty()) {
                // Tentar usar credenciais do admin atual
                Optional<User> currentUserOpt = currentAuthorService.getCurrentUser();
                if (currentUserOpt.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(new MessageResponse("Usuário não encontrado"));
                }

                User adminUser = currentUserOpt.get();
                if (adminUser.getEcommerceDbUrl() == null || adminUser.getEcommerceDbUrl().trim().isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new MessageResponse("Nenhum usuário encontrado para este autor e credenciais do banco não configuradas"));
                }

                List<AuthorPaymentDTO> payments = paymentQueryService.listPaymentsForAuthor(
                        authorId,
                        adminUser.getEcommerceDbUrl(),
                        adminUser.getEcommerceDbUsername(),
                        adminUser.getEcommerceDbPassword(),
                        offset,
                        limit
                );

                return ResponseEntity.ok(payments);
            }

            // Usar credenciais do usuário associado ao autor
            User userWithAuthor = usersWithAuthorId.get(0);
            if (userWithAuthor.getEcommerceDbUrl() == null || userWithAuthor.getEcommerceDbUrl().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("Credenciais do banco do e-commerce não configuradas para este autor"));
            }

            // Buscar lista de pagamentos
            List<AuthorPaymentDTO> payments = paymentQueryService.listPaymentsForAuthor(
                    authorId,
                    userWithAuthor.getEcommerceDbUrl(),
                    userWithAuthor.getEcommerceDbUsername(),
                    userWithAuthor.getEcommerceDbPassword(),
                    offset,
                    limit
            );

            return ResponseEntity.ok(payments);

        } catch (Exception e) {
            log.error("[ADMIN PAYMENTS] Error fetching payment details for author {}: {}", authorId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erro ao buscar pagamentos: " + e.getMessage()));
        }
    }
}

