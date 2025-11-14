package com.dianaglobal.loginregisterdashboardeditora.adapter.in.web;

import com.dianaglobal.loginregisterdashboardeditora.adapter.in.dto.AuthorPaymentDTO;
import com.dianaglobal.loginregisterdashboardeditora.adapter.in.dto.PaymentSummaryDTO;
import com.dianaglobal.loginregisterdashboardeditora.application.service.CurrentAuthorService;
import com.dianaglobal.loginregisterdashboardeditora.application.service.PaymentQueryService;
import com.dianaglobal.loginregisterdashboardeditora.config.ApiPaths;
import com.dianaglobal.loginregisterdashboardeditora.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controller para endpoints de pagamentos do autor (self-view).
 * Permite que autores vejam apenas seus próprios pagamentos.
 */
@Slf4j
@RestController
@RequestMapping(ApiPaths.API_V1_BASE + "/author/payments")
@RequiredArgsConstructor
public class AuthorPaymentsController {

    private final CurrentAuthorService currentAuthorService;
    private final PaymentQueryService paymentQueryService;

    public record MessageResponse(String message) {}

    /**
     * GET /api/v1/author/payments/summary
     * Retorna resumo de pagamentos do autor autenticado.
     */
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getPaymentSummary(
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        try {
            // Obter author_id do usuário autenticado
            Optional<Long> authorIdOpt = currentAuthorService.getCurrentAuthorId();
            
            if (authorIdOpt.isEmpty()) {
                // Se for admin, não deve usar este endpoint (use o admin endpoint)
                if (currentAuthorService.isCurrentUserAdmin()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(new MessageResponse("Admins devem usar /api/v1/admin/payments/author/{authorId}"));
                }
                
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse("Usuário não possui author_id configurado"));
            }

            Long authorId = authorIdOpt.get();

            // Obter credenciais do banco do usuário
            Optional<User> userOpt = currentAuthorService.getCurrentUser();
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponse("Usuário não encontrado"));
            }

            User user = userOpt.get();
            if (user.getEcommerceDbUrl() == null || user.getEcommerceDbUrl().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("Credenciais do banco do e-commerce não configuradas"));
            }

            // Buscar resumo de pagamentos
            Optional<PaymentSummaryDTO> summaryOpt = paymentQueryService.getPaymentSummary(
                    authorId,
                    user.getEcommerceDbUrl(),
                    user.getEcommerceDbUsername(),
                    user.getEcommerceDbPassword(),
                    limit
            );

            if (summaryOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new MessageResponse("Autor não encontrado ou sem dados no e-commerce"));
            }

            return ResponseEntity.ok(summaryOpt.get());

        } catch (Exception e) {
            log.error("[AUTHOR PAYMENTS] Error fetching payment summary: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erro ao buscar pagamentos: " + e.getMessage()));
        }
    }

    /**
     * GET /api/v1/author/payments/details
     * Retorna lista detalhada de pagamentos do autor autenticado (com paginação).
     */
    @GetMapping("/details")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getPaymentDetails(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "50") int limit
    ) {
        try {
            // Obter author_id do usuário autenticado
            Optional<Long> authorIdOpt = currentAuthorService.getCurrentAuthorId();
            
            if (authorIdOpt.isEmpty()) {
                if (currentAuthorService.isCurrentUserAdmin()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(new MessageResponse("Admins devem usar /api/v1/admin/payments/author/{authorId}/details"));
                }
                
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse("Usuário não possui author_id configurado"));
            }

            Long authorId = authorIdOpt.get();

            // Obter credenciais do banco do usuário
            Optional<User> userOpt = currentAuthorService.getCurrentUser();
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponse("Usuário não encontrado"));
            }

            User user = userOpt.get();
            if (user.getEcommerceDbUrl() == null || user.getEcommerceDbUrl().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponse("Credenciais do banco do e-commerce não configuradas"));
            }

            // Buscar lista de pagamentos
            List<AuthorPaymentDTO> payments = paymentQueryService.listPaymentsForAuthor(
                    authorId,
                    user.getEcommerceDbUrl(),
                    user.getEcommerceDbUsername(),
                    user.getEcommerceDbPassword(),
                    offset,
                    limit
            );

            return ResponseEntity.ok(payments);

        } catch (Exception e) {
            log.error("[AUTHOR PAYMENTS] Error fetching payment details: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erro ao buscar pagamentos: " + e.getMessage()));
        }
    }
}

