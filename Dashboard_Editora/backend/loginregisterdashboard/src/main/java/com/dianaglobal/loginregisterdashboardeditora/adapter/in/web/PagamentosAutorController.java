package com.dianaglobal.loginregisterdashboardeditora.adapter.in.web;

import com.dianaglobal.loginregisterdashboardeditora.adapter.in.dto.pagamentos.PainelPagamentosAutorDTO;
import com.dianaglobal.loginregisterdashboardeditora.application.service.CurrentAuthorService;
import com.dianaglobal.loginregisterdashboardeditora.application.service.PagamentosAutorService;
import com.dianaglobal.loginregisterdashboardeditora.config.ApiPaths;
import com.dianaglobal.loginregisterdashboardeditora.domain.model.User;
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
 * Controller para o módulo simples de Pagamentos do Autor.
 * Focado em informações claras e fáceis de entender para um escritor leigo.
 * 
 * Endpoint: GET /api/v1/autor/pagamentos/painel
 */
@Slf4j
@RestController
@RequestMapping(ApiPaths.API_V1_BASE + "/autor/pagamentos")
@RequiredArgsConstructor
public class PagamentosAutorController {

    private final PagamentosAutorService pagamentosAutorService;
    private final CurrentAuthorService currentAuthorService;

    /**
     * Endpoint simples para obter o painel de pagamentos do autor logado.
     * Retorna informações claras:
     * - Quanto o autor já vendeu (vendas confirmadas)
     * - Quanto já recebeu (placeholder, por enquanto 0)
     * - Quanto ainda vai receber
     * - Lista de vendas recentes (pedidos confirmados)
     * 
     * O authorId é obtido automaticamente do usuário logado.
     */
    @GetMapping("/painel")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> obterPainelAutor() {
        try {
            // Obter author_id do usuário autenticado
            Optional<Long> authorIdOpt = currentAuthorService.getCurrentAuthorId();
            
            if (authorIdOpt.isEmpty()) {
                // Se for admin, não deve usar este endpoint (admins podem ver qualquer autor via endpoint admin)
                if (currentAuthorService.isCurrentUserAdmin()) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(new MessageResponse("Admins devem usar endpoints de admin para visualizar pagamentos"));
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

            return ResponseEntity.ok(painel);

        } catch (Exception e) {
            log.error("[PAGAMENTOS AUTOR] Erro ao buscar painel de pagamentos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erro ao buscar informações de pagamentos: " + e.getMessage()));
        }
    }

    public record MessageResponse(String message) {}
}

