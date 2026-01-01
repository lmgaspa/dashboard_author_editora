package com.dianaglobal.paineldoauthorbackend.adapter.in.web;

import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.tickets.CreateMessageRequest;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.tickets.CreateTicketRequest;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.tickets.MessageDTO;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.tickets.TicketAdminDTO;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.tickets.TicketDTO;
import com.dianaglobal.paineldoauthorbackend.application.port.out.UserRepositoryPort;
import com.dianaglobal.paineldoauthorbackend.application.service.CurrentAuthorService;
import com.dianaglobal.paineldoauthorbackend.application.service.TicketService;
import com.dianaglobal.paineldoauthorbackend.config.ApiPaths;
import com.dianaglobal.paineldoauthorbackend.domain.model.Ticket;
import com.dianaglobal.paineldoauthorbackend.domain.model.TicketCategory;
import com.dianaglobal.paineldoauthorbackend.domain.model.TicketMessage;
import com.dianaglobal.paineldoauthorbackend.domain.model.TicketStatus;
import com.dianaglobal.paineldoauthorbackend.domain.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller para tickets.
 * Autor vê seus tickets (SEM prioridade), admin vê todos (COM prioridade).
 */
@Slf4j
@RestController
@RequestMapping(ApiPaths.API_V1_BASE + "/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final CurrentAuthorService currentAuthorService;
    private final UserRepositoryPort userRepository;

    /**
     * GET /api/v1/tickets (Autor)
     * Lista tickets do autor logado (SEM prioridade).
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> listarTicketsAutor(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Optional<Long> authorIdOpt = currentAuthorService.getCurrentAuthorId();

            if (authorIdOpt.isEmpty()) {
                if (currentAuthorService.isCurrentUserAdmin()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(new MessageResponse("Admins devem usar endpoints de admin para visualizar tickets"));
                }
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse("Usuário não possui author_id configurado"));
            }

            String authorId = authorIdOpt.get().toString();
            List<Ticket> tickets = ticketService.findAllByAuthorId(authorId);

            List<TicketDTO> dtos = tickets.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dtos);

        } catch (Exception e) {
            log.error("[TICKETS] Erro ao listar tickets: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erro ao listar tickets: " + e.getMessage()));
        }
    }

    /**
     * GET /api/v1/tickets/{ticketId} (Autor)
     * Detalhes de um ticket (SEM prioridade).
     */
    @GetMapping("/{ticketId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> obterTicket(
            @PathVariable UUID ticketId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Optional<Ticket> ticketOpt = ticketService.findById(ticketId);
            if (ticketOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new MessageResponse("Ticket não encontrado"));
            }

            Ticket ticket = ticketOpt.get();

            // Verificar se autor tem acesso
            Optional<Long> authorIdOpt = currentAuthorService.getCurrentAuthorId();
            if (authorIdOpt.isEmpty() || !ticket.getAuthorId().equals(authorIdOpt.get().toString())) {
                if (!currentAuthorService.isCurrentUserAdmin()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(new MessageResponse("Acesso negado"));
                }
            }

            TicketDTO dto = toDTO(ticket);
            return ResponseEntity.ok(dto);

        } catch (Exception e) {
            log.error("[TICKETS] Erro ao obter ticket: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erro ao obter ticket: " + e.getMessage()));
        }
    }

    /**
     * POST /api/v1/tickets (Autor)
     * Criar novo ticket (autor escreve o que quiser).
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> criarTicket(
            @RequestBody @Valid CreateTicketRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

            Optional<Long> authorIdOpt = currentAuthorService.getCurrentAuthorId();
            if (authorIdOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse("Usuário não possui author_id configurado"));
            }

            TicketCategory category = request.category() != null && !request.category().trim().isEmpty()
                    ? TicketCategory.valueOf(request.category().toUpperCase())
                    : TicketCategory.OUTRO;

            Ticket ticket = Ticket.builder()
                    .id(UUID.randomUUID())
                    .title(request.title())
                    .description(request.description())
                    .createdByUserId(user.getId())
                    .authorId(authorIdOpt.get().toString())
                    .category(category)
                    .status(TicketStatus.OPEN)
                    .relatedChargeId(request.relatedChargeId())
                    .build();

            ticket = ticketService.createTicket(ticket);

            TicketDTO dto = toDTO(ticket);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("[TICKETS] Erro ao criar ticket: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erro ao criar ticket: " + e.getMessage()));
        }
    }

    /**
     * POST /api/v1/tickets/{ticketId}/messages (Autor/Admin)
     * Adicionar mensagem ao ticket.
     */
    @PostMapping("/{ticketId}/messages")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> adicionarMensagem(
            @PathVariable UUID ticketId,
            @RequestBody @Valid CreateMessageRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

            // Verificar se é admin para usar nota interna
            boolean isAdmin = currentAuthorService.isCurrentUserAdmin();
            if (request.isInternalNote() && !isAdmin) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse("Apenas admins podem criar notas internas"));
            }

            TicketMessage message = TicketMessage.builder()
                    .id(UUID.randomUUID())
                    .ticketId(ticketId)
                    .sentByUserId(user.getId())
                    .message(request.message())
                    .isInternalNote(request.isInternalNote() && isAdmin)
                    .build();

            message = ticketService.addMessage(ticketId, message);

            // Atualizar status do ticket
            Optional<Ticket> ticketOpt = ticketService.findById(ticketId);
            if (ticketOpt.isPresent()) {
                Ticket ticket = ticketOpt.get();
                if (isAdmin) {
                    ticketService.updateStatus(ticketId, TicketStatus.WAITING_AUTHOR, user.getId());
                } else {
                    ticketService.updateStatus(ticketId, TicketStatus.WAITING_ADMIN, user.getId());
                }
            }

            MessageDTO dto = toMessageDTO(message, user.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("[TICKETS] Erro ao adicionar mensagem: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erro ao adicionar mensagem: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/v1/tickets/{ticketId}/resolve (Autor)
     * Autor marca ticket como resolvido.
     */
    @PutMapping("/{ticketId}/resolve")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> marcarComoResolvido(
            @PathVariable UUID ticketId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

            Ticket ticket = ticketService.updateStatus(ticketId, TicketStatus.RESOLVED, user.getId());

            TicketDTO dto = toDTO(ticket);
            return ResponseEntity.ok(dto);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("[TICKETS] Erro ao marcar como resolvido: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erro ao marcar como resolvido: " + e.getMessage()));
        }
    }

    private TicketDTO toDTO(Ticket ticket) {
        List<MessageDTO> messages = ticketService.getMessages(ticket.getId()).stream()
                .filter(msg -> !msg.isInternalNote()) // Autor não vê notas internas
                .map(msg -> {
                    String senderName = userRepository.findById(msg.getSentByUserId())
                            .map(User::getName)
                            .orElse("Usuário não encontrado");
                    return toMessageDTO(msg, senderName);
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
                messages);
    }

    private MessageDTO toMessageDTO(TicketMessage message, String senderName) {
        return new MessageDTO(
                message.getId(),
                message.getSentByUserId(),
                senderName,
                message.getMessage(),
                message.isInternalNote(),
                OffsetDateTime.ofInstant(message.getCreatedAt(), java.time.ZoneOffset.UTC),
                message.getReadAt() != null ? OffsetDateTime.ofInstant(message.getReadAt(), java.time.ZoneOffset.UTC)
                        : null);
    }

    /**
     * GET /api/v1/tickets/admin
     * Lista TODOS os tickets (Admin).
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> listarTodosTicketsAdmin() {
        try {
            List<Ticket> tickets = ticketService.findAll();
            List<TicketDTO> dtos = tickets.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            log.error("[TICKETS] Erro ao listar tickets admin: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erro ao listar tickets: " + e.getMessage()));
        }
    }

    public record MessageResponse(String message) {
    }
}
