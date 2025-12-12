package com.dianaglobal.paineldoauthorbackend.adapter.in.web;

import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.pagamentos.PainelPagamentosAutorDTO;
import com.dianaglobal.paineldoauthorbackend.application.service.CurrentAuthorService;
import com.dianaglobal.paineldoauthorbackend.application.service.ExportService;
import com.dianaglobal.paineldoauthorbackend.application.service.PagamentosAutorService;
import com.dianaglobal.paineldoauthorbackend.config.ApiPaths;
import com.dianaglobal.paineldoauthorbackend.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * Controller para exportação de pagamentos.
 * Endpoint: GET /api/v1/payments/export
 */
@Slf4j
@RestController
@RequestMapping(ApiPaths.API_V1_BASE + "/payments")
@RequiredArgsConstructor
public class PaymentsExportController {

    private final PagamentosAutorService pagamentosAutorService;
    private final CurrentAuthorService currentAuthorService;
    private final ExportService exportService;

    /**
     * Endpoint para exportar pagamentos do autor.
     * Aceita parâmetros: format (pdf, json) e author_id (opcional, usa do usuário logado se não fornecido)
     * 
     * Por enquanto, retorna JSON. Export PDF será implementado futuramente.
     */
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> exportarPagamentos(
            @RequestParam(required = false) String format,
            @RequestParam(required = false) Long author_id
    ) {
        try {
            // Determinar author_id: usar parâmetro ou do usuário logado
            Long authorId;
            if (author_id != null) {
                // Se fornecido via parâmetro, validar se é admin ou se é o próprio author_id
                Optional<Long> currentAuthorIdOpt = currentAuthorService.getCurrentAuthorId();
                if (currentAuthorIdOpt.isPresent() && !currentAuthorIdOpt.get().equals(author_id)) {
                    // Usuário tentando acessar outro author_id - só admin pode
                    if (!currentAuthorService.isCurrentUserAdmin()) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(new MessageResponse("Você só pode exportar seus próprios pagamentos"));
                    }
                }
                authorId = author_id;
            } else {
                // Usar author_id do usuário logado
                Optional<Long> authorIdOpt = currentAuthorService.getCurrentAuthorId();
                if (authorIdOpt.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(new MessageResponse("Usuário não possui author_id configurado. Entre em contato com o administrador."));
                }
                authorId = authorIdOpt.get();
            }

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

            // Montar painel de pagamentos
            PainelPagamentosAutorDTO painel = pagamentosAutorService.montarPainelPagamentosAutor(
                    authorId,
                    user.getEcommerceDbUrl(),
                    user.getEcommerceDbUsername(),
                    user.getEcommerceDbPassword()
            );

            if (painel == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new MessageResponse("Autor não encontrado ou sem dados no e-commerce"));
            }

            String authorName = painel.resumo() != null ? painel.resumo().nomeAutor() : "Autor";

            // Exportar conforme formato solicitado
            if ("pdf".equalsIgnoreCase(format)) {
                byte[] pdfBytes = exportService.exportPaymentsToPdf(painel, authorName);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.setContentDispositionFormData("attachment", "pagamentos_" + authorId + ".pdf");
                return ResponseEntity.ok()
                        .headers(headers)
                        .body(pdfBytes);
            } else if ("csv".equalsIgnoreCase(format)) {
                byte[] csvBytes = exportService.exportPaymentsToCsv(painel, authorName);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.TEXT_PLAIN);
                headers.setContentDispositionFormData("attachment", "pagamentos_" + authorId + ".csv");
                return ResponseEntity.ok()
                        .headers(headers)
                        .body(csvBytes);
            }

            // Retornar JSON (padrão)
            return ResponseEntity.ok(painel);

        } catch (Exception e) {
            log.error("[PAYMENTS EXPORT] Erro ao exportar pagamentos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erro ao exportar pagamentos: " + e.getMessage()));
        }
    }

    public record MessageResponse(String message) {}
}

