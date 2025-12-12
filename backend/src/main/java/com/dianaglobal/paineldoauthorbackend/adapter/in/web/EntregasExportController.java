package com.dianaglobal.paineldoauthorbackend.adapter.in.web;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.entregas.EntregaDTO;
import com.dianaglobal.paineldoauthorbackend.application.service.CurrentAuthorService;
import com.dianaglobal.paineldoauthorbackend.application.service.EntregasService;
import com.dianaglobal.paineldoauthorbackend.application.service.ExportService;
import com.dianaglobal.paineldoauthorbackend.config.ApiPaths;
import com.dianaglobal.paineldoauthorbackend.domain.model.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller para exportação de entregas.
 * Endpoint: GET /api/v1/entregas/export
 */
@Slf4j
@RestController
@RequestMapping(ApiPaths.API_V1_BASE + "/entregas")
@RequiredArgsConstructor
public class EntregasExportController {

    private final EntregasService entregasService;
    private final CurrentAuthorService currentAuthorService;
    private final ExportService exportService;

    /**
     * Endpoint para exportar entregas do autor.
     * Aceita parâmetros: format (pdf, csv, json) e author_id (opcional)
     */
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> exportarEntregas(
            @RequestParam(required = false, defaultValue = "json") String format,
            @RequestParam(required = false) Long author_id
    ) {
        try {
            // Determinar author_id
            Long authorId;
            if (author_id != null) {
                Optional<Long> currentAuthorIdOpt = currentAuthorService.getCurrentAuthorId();
                if (currentAuthorIdOpt.isPresent() && !currentAuthorIdOpt.get().equals(author_id)) {
                    if (!currentAuthorService.isCurrentUserAdmin()) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(new MessageResponse("Você só pode exportar suas próprias entregas"));
                    }
                }
                authorId = author_id;
            } else {
                Optional<Long> authorIdOpt = currentAuthorService.getCurrentAuthorId();
                if (authorIdOpt.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(new MessageResponse("Usuário não possui author_id configurado"));
                }
                authorId = authorIdOpt.get();
            }

            // Obter credenciais do banco
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

            // Listar entregas
            List<EntregaDTO> entregas = entregasService.listarEntregas(
                    authorId,
                    user.getEcommerceDbUrl(),
                    user.getEcommerceDbUsername(),
                    user.getEcommerceDbPassword()
            );

            String authorName = user.getName() != null ? user.getName() : "Autor";

            // Exportar conforme formato
            if ("pdf".equalsIgnoreCase(format)) {
                byte[] pdfBytes = exportService.exportEntregasToPdf(entregas, authorName);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.setContentDispositionFormData("attachment", "entregas_" + authorId + ".pdf");
                return ResponseEntity.ok().headers(headers).body(pdfBytes);
            } else if ("csv".equalsIgnoreCase(format)) {
                byte[] csvBytes = exportService.exportEntregasToCsv(entregas, authorName);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.TEXT_PLAIN);
                headers.setContentDispositionFormData("attachment", "entregas_" + authorId + ".csv");
                return ResponseEntity.ok().headers(headers).body(csvBytes);
            }

            // Retornar JSON (padrão)
            return ResponseEntity.ok(entregas);

        } catch (Exception e) {
            log.error("[ENTREGAS EXPORT] Erro ao exportar entregas: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erro ao exportar entregas: " + e.getMessage()));
        }
    }

    /**
     * Endpoint para exportar apenas pedidos arquivados (status ENTREGUE).
     * Aceita parâmetros: format (pdf, csv, json) e author_id (opcional)
     * Endpoint: GET /api/v1/entregas/export/arquivados
     */
    @GetMapping("/export/arquivados")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> exportarPedidosArquivados(
            @RequestParam(required = false, defaultValue = "json") String format,
            @RequestParam(required = false) Long author_id
    ) {
        try {
            // Determinar author_id
            Long authorId;
            if (author_id != null) {
                Optional<Long> currentAuthorIdOpt = currentAuthorService.getCurrentAuthorId();
                if (currentAuthorIdOpt.isPresent() && !currentAuthorIdOpt.get().equals(author_id)) {
                    if (!currentAuthorService.isCurrentUserAdmin()) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(new MessageResponse("Você só pode exportar suas próprias entregas"));
                    }
                }
                authorId = author_id;
            } else {
                Optional<Long> authorIdOpt = currentAuthorService.getCurrentAuthorId();
                if (authorIdOpt.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(new MessageResponse("Usuário não possui author_id configurado"));
                }
                authorId = authorIdOpt.get();
            }

            // Obter credenciais do banco
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

            // Listar todas as entregas
            List<EntregaDTO> todasEntregas = entregasService.listarEntregas(
                    authorId,
                    user.getEcommerceDbUrl(),
                    user.getEcommerceDbUsername(),
                    user.getEcommerceDbPassword()
            );

            // Filtrar apenas entregas arquivadas (status ENTREGUE)
            List<EntregaDTO> entregasArquivadas = todasEntregas.stream()
                    .filter(entrega -> "ENTREGUE".equalsIgnoreCase(entrega.statusEnvio()))
                    .collect(Collectors.toList());

            String authorName = user.getName() != null ? user.getName() : "Autor";

            // Exportar conforme formato
            if ("pdf".equalsIgnoreCase(format)) {
                byte[] pdfBytes = exportService.exportEntregasToPdf(entregasArquivadas, authorName);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.setContentDispositionFormData("attachment", "pedidos_arquivados_" + authorId + ".pdf");
                return ResponseEntity.ok().headers(headers).body(pdfBytes);
            } else if ("csv".equalsIgnoreCase(format)) {
                byte[] csvBytes = exportService.exportEntregasToCsv(entregasArquivadas, authorName);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.TEXT_PLAIN);
                headers.setContentDispositionFormData("attachment", "pedidos_arquivados_" + authorId + ".csv");
                return ResponseEntity.ok().headers(headers).body(csvBytes);
            }

            // Retornar JSON (padrão)
            return ResponseEntity.ok(entregasArquivadas);

        } catch (Exception e) {
            log.error("[ENTREGAS EXPORT ARQUIVADOS] Erro ao exportar pedidos arquivados: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erro ao exportar pedidos arquivados: " + e.getMessage()));
        }
    }

    public record MessageResponse(String message) {}
}

