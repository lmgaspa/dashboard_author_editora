package com.dianaglobal.paineldoauthorbackend.adapter.in.dto.tickets;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Request para criar ticket (autor escreve o que quiser).
 */
public record CreateTicketRequest(
    @NotBlank(message = "Título é obrigatório")
    @Size(max = 255, message = "Título deve ter no máximo 255 caracteres")
    String title,
    
    @NotBlank(message = "Descrição é obrigatória")
    @Size(max = 5000, message = "Descrição deve ter no máximo 5000 caracteres")
    String description,
    
    String category,                  // Opcional - sistema classifica se não fornecido
    UUID relatedChargeId              // Opcional - relacionar com cobrança
) {}



