package com.dianaglobal.paineldoauthorbackend.adapter.in.web;

import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.tickets.TicketDTO;
import com.dianaglobal.paineldoauthorbackend.application.port.out.UserRepositoryPort;
import com.dianaglobal.paineldoauthorbackend.application.service.CurrentAuthorService;
import com.dianaglobal.paineldoauthorbackend.application.service.ExportService;
import com.dianaglobal.paineldoauthorbackend.application.service.TicketService;
import com.dianaglobal.paineldoauthorbackend.config.ApiPaths;
import com.dianaglobal.paineldoauthorbackend.domain.model.Ticket;
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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller para exportação de tickets.
 * Endpoint: GET /api/v1/tickets/export
 */
@Slf4j
@RestController
@RequestMapping(ApiPaths.API_V1_BASE + "/tickets")
@RequiredArgsConstructor
public class TicketsExportController {

    private final TicketService ticketService;
    private final CurrentAuthorService currentAuthorService;
    private final UserRepositoryPort userRepository;
    private final ExportService exportService;

    /**
     * Endpoint para exportar tickets do autor.
     * Aceita parâmetros: format (pdf, csv, json) e author_id (opcional)
     */
    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> exportarTickets(
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
                                .body(new MessageResponse("Você só pode exportar seus próprios tickets"));
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

            // Listar tickets
            List<Ticket> tickets = ticketService.findAllByAuthorId(authorId);
            
            // Converter para DTO
            List<TicketDTO> dtos = tickets.stream()
                    .map(ticket -> {
                        List<com.dianaglobal.paineldoauthorbackend.adapter.in.dto.tickets.MessageDTO> messages = 
                            ticketService.getMessages(ticket.getId()).stream()
                                .filter(msg -> !msg.isInternalNote())
                                .map(msg -> {
                                    String senderName = userRepository.findById(msg.getSentByUserId())
                                            .map(User::getName)
                                            .orElse("Usuário não encontrado");
                                    return new com.dianaglobal.paineldoauthorbackend.adapter.in.dto.tickets.MessageDTO(
                                        msg.getId(),
                                        msg.getSentByUserId(),
                                        senderName,
                                        msg.getMessage(),
                                        msg.isInternalNote(),
                                        OffsetDateTime.ofInstant(msg.getCreatedAt(), java.time.ZoneOffset.UTC),
                                        msg.getReadAt() != null ? 
                                            OffsetDateTime.ofInstant(msg.getReadAt(), java.time.ZoneOffset.UTC) : null
                                    );
                                })
                                .collect(Collectors.toList());
                        
                        return new TicketDTO(
                            ticket.getId(),
                            ticket.getTicketNumber(),
                            ticket.getTitle(),
                            ticket.getDescription(),
                            ticket.getCategory().name(),
                            ticket.getStatus().name(),
                            OffsetDateTime.ofInstant(ticket.getCreatedAt(), java.time.ZoneOffset.UTC),
                            OffsetDateTime.ofInstant(ticket.getUpdatedAt(), java.time.ZoneOffset.UTC),
                            messages
                        );
                    })
                    .collect(Collectors.toList());

            Optional<User> userOpt = currentAuthorService.getCurrentUser();
            String authorName = userOpt.map(User::getName).orElse("Autor");

            // Exportar conforme formato
            if ("pdf".equalsIgnoreCase(format)) {
                byte[] pdfBytes = exportService.exportTicketsToPdf(dtos, authorName);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.setContentDispositionFormData("attachment", "tickets_" + authorId + ".pdf");
                return ResponseEntity.ok().headers(headers).body(pdfBytes);
            } else if ("csv".equalsIgnoreCase(format)) {
                byte[] csvBytes = exportService.exportTicketsToCsv(dtos, authorName);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.TEXT_PLAIN);
                headers.setContentDispositionFormData("attachment", "tickets_" + authorId + ".csv");
                return ResponseEntity.ok().headers(headers).body(csvBytes);
            }

            // Retornar JSON (padrão)
            return ResponseEntity.ok(dtos);

        } catch (Exception e) {
            log.error("[TICKETS EXPORT] Erro ao exportar tickets: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erro ao exportar tickets: " + e.getMessage()));
        }
    }

    public record MessageResponse(String message) {}
}

