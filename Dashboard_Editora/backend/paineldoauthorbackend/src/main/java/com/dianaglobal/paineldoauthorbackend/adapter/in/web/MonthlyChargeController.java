package com.dianaglobal.paineldoauthorbackend.adapter.in.web;

import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.cobrancas.ConfirmPaymentRequest;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.cobrancas.CreateChargeRequest;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.cobrancas.MonthlyChargeDTO;
import com.dianaglobal.paineldoauthorbackend.application.port.out.UserRepositoryPort;
import com.dianaglobal.paineldoauthorbackend.application.service.CurrentAuthorService;
import com.dianaglobal.paineldoauthorbackend.application.service.MonthlyChargeService;
import com.dianaglobal.paineldoauthorbackend.application.service.TicketService;
import com.dianaglobal.paineldoauthorbackend.config.ApiPaths;
import com.dianaglobal.paineldoauthorbackend.domain.model.ChargeStatus;
import com.dianaglobal.paineldoauthorbackend.domain.model.MonthlyCharge;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller para cobranças mensais.
 * Autor vê suas cobranças, admin gerencia todas.
 */
@Slf4j
@RestController
@RequestMapping(ApiPaths.API_V1_BASE + "/cobrancas")
@RequiredArgsConstructor
public class MonthlyChargeController {

    private final MonthlyChargeService chargeService;
    private final CurrentAuthorService currentAuthorService;
    private final UserRepositoryPort userRepository;
    private final TicketService ticketService;

    /**
     * GET /api/v1/cobrancas (Autor)
     * Lista cobranças do autor logado.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> listarCobrancasAutor(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            Optional<Long> authorIdOpt = currentAuthorService.getCurrentAuthorId();
            
            if (authorIdOpt.isEmpty()) {
                if (currentAuthorService.isCurrentUserAdmin()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(new MessageResponse("Admins devem usar endpoints de admin para visualizar cobranças"));
                }
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse("Usuário não possui author_id configurado"));
            }
            
            String authorId = authorIdOpt.get().toString();
            List<MonthlyCharge> charges = chargeService.findAllByAuthorId(authorId);
            
            List<MonthlyChargeDTO> dtos = charges.stream()
                    .map(charge -> toDTO(charge, authorId))
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(dtos);
            
        } catch (Exception e) {
            log.error("[COBRANCAS] Erro ao listar cobranças: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erro ao listar cobranças: " + e.getMessage()));
        }
    }

    /**
     * GET /api/v1/cobrancas/{chargeId}/pix (Autor)
     * Obtém código PIX para pagamento.
     */
    @GetMapping("/{chargeId}/pix")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> obterPixCode(
            @PathVariable UUID chargeId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            Optional<MonthlyCharge> chargeOpt = chargeService.findById(chargeId);
            if (chargeOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new MessageResponse("Cobrança não encontrada"));
            }
            
            MonthlyCharge charge = chargeOpt.get();
            
            // Verificar se autor tem acesso
            Optional<Long> authorIdOpt = currentAuthorService.getCurrentAuthorId();
            if (authorIdOpt.isEmpty() || !charge.getAuthorId().equals(authorIdOpt.get().toString())) {
                if (!currentAuthorService.isCurrentUserAdmin()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(new MessageResponse("Acesso negado"));
                }
            }
            
            return ResponseEntity.ok(new PixCodeResponse(charge.getPixCode(), charge.getAmount()));
            
        } catch (Exception e) {
            log.error("[COBRANCAS] Erro ao obter PIX: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erro ao obter código PIX: " + e.getMessage()));
        }
    }

    /**
     * POST /api/v1/admin/cobrancas (Admin)
     * Cria nova cobrança mensal.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> criarCobranca(
            @RequestBody @Valid CreateChargeRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            User admin = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("Admin não encontrado"));
            
            MonthlyCharge charge = MonthlyCharge.builder()
                    .id(UUID.randomUUID())
                    .authorId(request.authorId())
                    .createdByUserId(admin.getId())
                    .chargeMonth(request.chargeMonth())
                    .chargeYear(request.chargeYear())
                    .amount(request.amount())
                    .dueDate(request.dueDate())
                    .chargeDate(java.time.LocalDate.now())
                    .status(ChargeStatus.PENDING)
                    .build();
            
            charge = chargeService.createCharge(charge);
            
            MonthlyChargeDTO dto = toDTO(charge, request.authorId());
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("[COBRANCAS] Erro ao criar cobrança: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erro ao criar cobrança: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/v1/admin/cobrancas/{chargeId}/confirmar (Admin)
     * Admin confirma pagamento.
     */
    @PutMapping("/{chargeId}/confirmar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> confirmarPagamento(
            @PathVariable UUID chargeId,
            @RequestBody ConfirmPaymentRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            User admin = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("Admin não encontrado"));
            
            MonthlyCharge charge = chargeService.confirmPayment(chargeId, admin.getId(), request.notes());
            
            // TODO: Enviar e-mail "Pagamento confirmado - Mês X/Ano"
            
            MonthlyChargeDTO dto = toDTO(charge, charge.getAuthorId());
            return ResponseEntity.ok(dto);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("[COBRANCAS] Erro ao confirmar pagamento: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erro ao confirmar pagamento: " + e.getMessage()));
        }
    }

    /**
     * GET /api/v1/admin/cobrancas (Admin)
     * Lista todas as cobranças.
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> listarTodasCobrancas(
            @RequestParam(required = false) String authorId,
            @RequestParam(required = false) String status
    ) {
        try {
            List<MonthlyCharge> charges;
            
            if (authorId != null && !authorId.trim().isEmpty()) {
                charges = chargeService.findAllByAuthorId(authorId);
            } else {
                charges = chargeService.findAll();
            }
            
            if (status != null && !status.trim().isEmpty()) {
                ChargeStatus chargeStatus = ChargeStatus.valueOf(status.toUpperCase());
                charges = charges.stream()
                        .filter(c -> c.getStatus() == chargeStatus)
                        .collect(Collectors.toList());
            }
            
            List<MonthlyChargeDTO> dtos = charges.stream()
                    .map(charge -> toDTO(charge, charge.getAuthorId()))
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(dtos);
            
        } catch (Exception e) {
            log.error("[COBRANCAS] Erro ao listar cobranças: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erro ao listar cobranças: " + e.getMessage()));
        }
    }

    private MonthlyChargeDTO toDTO(MonthlyCharge charge, String authorId) {
        String authorName = userRepository.findAllByAuthorId(authorId).stream()
                .findFirst()
                .map(User::getName)
                .orElse("Autor não encontrado");
        
        String confirmedByAdminName = charge.getConfirmedByUserId() != null
                ? userRepository.findById(charge.getConfirmedByUserId())
                        .map(User::getName)
                        .orElse("Admin não encontrado")
                : null;
        
        long daysOverdue = 0;
        if (charge.getStatus() == ChargeStatus.OVERDUE && charge.getDueDate() != null) {
            daysOverdue = ChronoUnit.DAYS.between(charge.getDueDate(), java.time.LocalDate.now());
        }
        
        boolean hasOpenTicket = ticketService.existsTicketForCharge(authorId, charge.getId());
        
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
                charge.getPaidAt() != null ? OffsetDateTime.ofInstant(charge.getPaidAt(), java.time.ZoneOffset.UTC) : null,
                confirmedByAdminName,
                charge.getConfirmedAt() != null ? OffsetDateTime.ofInstant(charge.getConfirmedAt(), java.time.ZoneOffset.UTC) : null,
                charge.getPixCode(),
                charge.getPixExpiresAt() != null ? OffsetDateTime.ofInstant(charge.getPixExpiresAt(), java.time.ZoneOffset.UTC) : null,
                daysOverdue,
                hasOpenTicket
        );
    }

    public record MessageResponse(String message) {}
    public record PixCodeResponse(String pixCode, java.math.BigDecimal amount) {}
}



