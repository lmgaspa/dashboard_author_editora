package com.dianaglobal.paineldoauthorbackend.adapter.in.dto.tickets;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request para criar mensagem em ticket.
 */
public record CreateMessageRequest(
    @NotBlank(message = "Mensagem é obrigatória")
    @Size(max = 5000, message = "Mensagem deve ter no máximo 5000 caracteres")
    String message,
    
    boolean isInternalNote            // Apenas admin pode usar (nota interna)
) {}



