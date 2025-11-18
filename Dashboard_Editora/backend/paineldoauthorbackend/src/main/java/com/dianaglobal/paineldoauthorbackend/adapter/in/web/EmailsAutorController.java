package com.dianaglobal.paineldoauthorbackend.adapter.in.web;

import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.emails.PainelEmailsAutorDTO;
import com.dianaglobal.paineldoauthorbackend.application.service.CurrentAuthorService;
import com.dianaglobal.paineldoauthorbackend.application.service.EmailsAutorService;
import com.dianaglobal.paineldoauthorbackend.config.ApiPaths;
import com.dianaglobal.paineldoauthorbackend.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * Controller para o módulo de E-mails do Autor.
 * Consolida e-mails de clientes (orders) e e-mails de repasse (payout_email).
 * 
 * Endpoint: GET /api/v1/autor/emails/painel
 */
@Slf4j
@RestController
@RequestMapping(ApiPaths.API_V1_BASE + "/autor/emails")
@RequiredArgsConstructor
public class EmailsAutorController {

    private final EmailsAutorService emailsAutorService;
    private final CurrentAuthorService currentAuthorService;

    /**
     * Endpoint para obter o painel de e-mails do autor logado.
     * Retorna:
     * - E-mails de clientes agrupados por email (com estatísticas de pedidos)
     * - E-mails de repasse enviados ao autor
     * 
     * O authorId é obtido automaticamente do usuário logado.
     */
    @GetMapping("/painel")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> obterPainelEmails() {
        try {
            // Obter author_id do usuário autenticado
            Optional<Long> authorIdOpt = currentAuthorService.getCurrentAuthorId();
            
            if (authorIdOpt.isEmpty()) {
                // Se for admin, não deve usar este endpoint (admins podem ver qualquer autor via endpoint admin)
                if (currentAuthorService.isCurrentUserAdmin()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(new MessageResponse("Admins devem usar endpoints de admin para visualizar e-mails"));
                }
                
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse("Usuário não possui author_id configurado. Entre em contato com o administrador."));
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
                        .body(new MessageResponse("Credenciais do banco do e-commerce não configuradas. Entre em contato com o administrador."));
            }

            // Montar painel de e-mails
            PainelEmailsAutorDTO painel = emailsAutorService.montarPainelEmailsAutor(
                    authorId,
                    user.getEcommerceDbUrl(),
                    user.getEcommerceDbUsername(),
                    user.getEcommerceDbPassword()
            );

            if (painel == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new MessageResponse("Autor não encontrado ou sem dados no e-commerce"));
            }

            return ResponseEntity.ok(painel);

        } catch (Exception e) {
            log.error("[EMAILS AUTOR] Erro ao buscar painel de e-mails: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erro ao buscar informações de e-mails: " + e.getMessage()));
        }
    }

    public record MessageResponse(String message) {}
}

