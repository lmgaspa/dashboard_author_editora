package com.dianaglobal.paineldoauthorbackend.adapter.in.web;

import com.dianaglobal.paineldoauthorbackend.adapter.in.web.AdminController.AuthorStatsResponse;
import com.dianaglobal.paineldoauthorbackend.application.service.AuthorStatsService;
import com.dianaglobal.paineldoauthorbackend.application.service.CurrentAuthorService;
import com.dianaglobal.paineldoauthorbackend.application.service.ExportService;
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
 * Controller para exportação de métricas/estatísticas.
 * Endpoint: GET /api/v1/metricas/export
 */
@Slf4j
@RestController
@RequestMapping(ApiPaths.API_V1_BASE + "/metricas")
@RequiredArgsConstructor
public class MetricasExportController {

    private final AuthorStatsService authorStatsService;
    private final CurrentAuthorService currentAuthorService;
    private final ExportService exportService;

    /**
     * Endpoint para exportar métricas do autor.
     * Aceita parâmetros: format (pdf, csv, json) e author_id (opcional)
     */
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> exportarMetricas(
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
                                .body(new MessageResponse("Você só pode exportar suas próprias métricas"));
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

            // Buscar estatísticas
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

            AuthorStatsResponse response = new AuthorStatsResponse(
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

            String authorName = stats.getAuthorName() != null ? stats.getAuthorName() : "Autor";

            // Exportar conforme formato
            if ("pdf".equalsIgnoreCase(format)) {
                byte[] pdfBytes = exportService.exportMetricasToPdf(response, authorName);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.setContentDispositionFormData("attachment", "metricas_" + authorId + ".pdf");
                return ResponseEntity.ok().headers(headers).body(pdfBytes);
            } else if ("csv".equalsIgnoreCase(format)) {
                byte[] csvBytes = exportService.exportMetricasToCsv(response, authorName);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.TEXT_PLAIN);
                headers.setContentDispositionFormData("attachment", "metricas_" + authorId + ".csv");
                return ResponseEntity.ok().headers(headers).body(csvBytes);
            }

            // Retornar JSON (padrão)
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("[METRICAS EXPORT] Erro ao exportar métricas: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erro ao exportar métricas: " + e.getMessage()));
        }
    }

    public record MessageResponse(String message) {}
}

