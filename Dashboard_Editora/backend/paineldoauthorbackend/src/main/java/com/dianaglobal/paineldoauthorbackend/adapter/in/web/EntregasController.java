package com.dianaglobal.paineldoauthorbackend.adapter.in.web;

import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.entregas.AtualizarStatusEnvioRequest;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.entregas.EntregaDTO;
import com.dianaglobal.paineldoauthorbackend.application.service.CurrentAuthorService;
import com.dianaglobal.paineldoauthorbackend.application.service.EntregasService;
import com.dianaglobal.paineldoauthorbackend.config.ApiPaths;
import com.dianaglobal.paineldoauthorbackend.domain.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controller para gerenciar entregas de pedidos.
 * Permite ao autor visualizar pedidos confirmados e atualizar status de envio.
 */
@Slf4j
@RestController
@RequestMapping(ApiPaths.API_V1_BASE + "/entregas")
@RequiredArgsConstructor
public class EntregasController {

    private final EntregasService entregasService;
    private final CurrentAuthorService currentAuthorService;

    /**
     * GET /api/v1/entregas
     * Lista todos os pedidos confirmados do autor com informações para envio.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> listarEntregas() {
        try {
            // Obter author_id do usuário autenticado
            Optional<Long> authorIdOpt = currentAuthorService.getCurrentAuthorId();
            
            if (authorIdOpt.isEmpty()) {
                if (currentAuthorService.isCurrentUserAdmin()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(new MessageResponse("Admins devem usar endpoints de admin para visualizar entregas"));
                }
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse("Usuário não possui author_id configurado. Entre em contato com o administrador."));
            }

            Long authorId = authorIdOpt.get();

            // Obter credenciais do banco do e-commerce
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

            // Listar entregas
            List<EntregaDTO> entregas = entregasService.listarEntregas(
                    authorId,
                    user.getEcommerceDbUrl(),
                    user.getEcommerceDbUsername(),
                    user.getEcommerceDbPassword()
            );

            return ResponseEntity.ok(entregas);

        } catch (Exception e) {
            log.error("[ENTREGAS] Erro ao listar entregas: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erro ao buscar entregas: " + e.getMessage()));
        }
    }

    /**
     * GET /api/v1/entregas/{orderId}
     * Busca uma entrega específica por order_id.
     */
    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> buscarEntrega(@PathVariable Long orderId) {
        try {
            Optional<Long> authorIdOpt = currentAuthorService.getCurrentAuthorId();
            
            if (authorIdOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse("Usuário não possui author_id configurado"));
            }

            Long authorId = authorIdOpt.get();

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

            EntregaDTO entrega = entregasService.buscarEntrega(
                    orderId,
                    authorId,
                    user.getEcommerceDbUrl(),
                    user.getEcommerceDbUsername(),
                    user.getEcommerceDbPassword()
            );

            if (entrega == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new MessageResponse("Entrega não encontrada"));
            }

            return ResponseEntity.ok(entrega);

        } catch (Exception e) {
            log.error("[ENTREGAS] Erro ao buscar entrega {}: {}", orderId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erro ao buscar entrega: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/v1/entregas/{orderId}/status
     * Atualiza o status de envio de um pedido.
     */
    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> atualizarStatusEnvio(
            @PathVariable Long orderId,
            @RequestBody @Valid AtualizarStatusEnvioRequest request
    ) {
        try {
            Optional<Long> authorIdOpt = currentAuthorService.getCurrentAuthorId();
            
            if (authorIdOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new MessageResponse("Usuário não possui author_id configurado"));
            }

            String authorId = authorIdOpt.get().toString();

            // Atualizar status
            entregasService.atualizarStatusEnvio(orderId, authorId, request);

            // Buscar entrega atualizada
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

            EntregaDTO entrega = entregasService.buscarEntrega(
                    orderId,
                    authorIdOpt.get(),
                    user.getEcommerceDbUrl(),
                    user.getEcommerceDbUsername(),
                    user.getEcommerceDbPassword()
            );

            if (entrega == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new MessageResponse("Entrega não encontrada"));
            }

            return ResponseEntity.ok(entrega);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("[ENTREGAS] Erro ao atualizar status de envio: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erro ao atualizar status de envio: " + e.getMessage()));
        }
    }

    public record MessageResponse(String message) {}
}

