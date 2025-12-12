package com.dianaglobal.paineldoauthorbackend.adapter.in.web;

import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.cobrancas.MonthlyChargeDTO;
import com.dianaglobal.paineldoauthorbackend.application.service.CurrentAuthorService;
import com.dianaglobal.paineldoauthorbackend.application.service.ExportService;
import com.dianaglobal.paineldoauthorbackend.application.service.MonthlyChargeService;
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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller para exportação de cobranças.
 * Endpoint: GET /api/v1/cobrancas/export
 */
@Slf4j
@RestController
@RequestMapping(ApiPaths.API_V1_BASE + "/cobrancas")
@RequiredArgsConstructor
public class CobrancasExportController {

    private final MonthlyChargeService chargeService;
    private final CurrentAuthorService currentAuthorService;
    private final ExportService exportService;

    /**
     * Endpoint para exportar cobranças do autor.
     * Aceita parâmetros: format (pdf, csv, json) e author_id (opcional)
     */
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> exportarCobrancas(
            @RequestParam(required = false, defaultValue = "json") String format,
            @RequestParam(required = false) String author_id
    ) {
        try {
            // Determinar author_id
            String authorId;
            if (author_id != null && !author_id.trim().isEmpty()) {
                Optional<Long> currentAuthorIdOpt = currentAuthorService.getCurrentAuthorId();
                if (currentAuthorIdOpt.isPresent() && !currentAuthorIdOpt.get().toString().equals(author_id)) {
                    if (!currentAuthorService.isCurrentUserAdmin()) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(new MessageResponse("Você só pode exportar suas próprias cobranças"));
                    }
                }
                authorId = author_id;
            } else {
                Optional<Long> authorIdOpt = currentAuthorService.getCurrentAuthorId();
                if (authorIdOpt.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(new MessageResponse("Usuário não possui author_id configurado"));
                }
                authorId = authorIdOpt.get().toString();
            }

            // Listar cobranças
            List<com.dianaglobal.paineldoauthorbackend.domain.model.MonthlyCharge> charges = 
                chargeService.findAllByAuthorId(authorId);
            
            // Converter para DTO (simplificado - em produção, usar um mapper)
            List<MonthlyChargeDTO> dtos = charges.stream()
                    .map(charge -> {
                        // Buscar nome do autor
                        Optional<User> userOpt = currentAuthorService.getCurrentUser();
                        String authorName = userOpt.map(User::getName).orElse("Autor");
                        
                        // Calcular dias de atraso
                        long daysOverdue = 0;
                        if (charge.getStatus() == com.dianaglobal.paineldoauthorbackend.domain.model.ChargeStatus.OVERDUE 
                            && charge.getDueDate() != null) {
                            daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(
                                charge.getDueDate(), 
                                java.time.LocalDate.now()
                            );
                        }
                        
                        // Verificar se tem ticket aberto
                        boolean hasOpenTicket = false; // TODO: implementar verificação de ticket
                        
                        return new MonthlyChargeDTO(
                            charge.getId(),
                            charge.getAuthorId(),
                            authorName,
                            charge.getChargeMonth(),
                            charge.getChargeYear(),
                            charge.getAmount(),
                            charge.getDueDate(),
                            charge.getChargeDate(),
                            charge.getStatus().name(),
                            charge.getPaidAt() != null ? 
                                java.time.OffsetDateTime.ofInstant(charge.getPaidAt(), java.time.ZoneOffset.UTC) : null,
                            null, // confirmedByAdminName - precisa buscar
                            charge.getConfirmedAt() != null ? 
                                java.time.OffsetDateTime.ofInstant(charge.getConfirmedAt(), java.time.ZoneOffset.UTC) : null,
                            charge.getPixCode(),
                            charge.getPixExpiresAt() != null ? 
                                java.time.OffsetDateTime.ofInstant(charge.getPixExpiresAt(), java.time.ZoneOffset.UTC) : null,
                            daysOverdue,
                            hasOpenTicket
                        );
                    })
                    .collect(Collectors.toList());

            Optional<User> userOpt = currentAuthorService.getCurrentUser();
            String authorName = userOpt.map(User::getName).orElse("Autor");

            // Exportar conforme formato
            if ("pdf".equalsIgnoreCase(format)) {
                byte[] pdfBytes = exportService.exportCobrancasToPdf(dtos, authorName);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.setContentDispositionFormData("attachment", "cobrancas_" + authorId + ".pdf");
                return ResponseEntity.ok().headers(headers).body(pdfBytes);
            } else if ("csv".equalsIgnoreCase(format)) {
                byte[] csvBytes = exportService.exportCobrancasToCsv(dtos, authorName);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.TEXT_PLAIN);
                headers.setContentDispositionFormData("attachment", "cobrancas_" + authorId + ".csv");
                return ResponseEntity.ok().headers(headers).body(csvBytes);
            }

            // Retornar JSON (padrão)
            return ResponseEntity.ok(dtos);

        } catch (Exception e) {
            log.error("[COBRANCAS EXPORT] Erro ao exportar cobranças: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erro ao exportar cobranças: " + e.getMessage()));
        }
    }

    public record MessageResponse(String message) {}
}

