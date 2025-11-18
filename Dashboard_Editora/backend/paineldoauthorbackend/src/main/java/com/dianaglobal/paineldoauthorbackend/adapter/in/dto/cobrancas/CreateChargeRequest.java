package com.dianaglobal.paineldoauthorbackend.adapter.in.dto.cobrancas;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request para criar cobrança mensal (admin).
 */
public record CreateChargeRequest(
    @NotBlank(message = "authorId é obrigatório")
    String authorId,                  // author_id do autor
    
    @NotNull(message = "chargeMonth é obrigatório")
    @Min(value = 1, message = "Mês deve ser entre 1 e 12")
    @Max(value = 12, message = "Mês deve ser entre 1 e 12")
    Integer chargeMonth,              // 1-12
    
    @NotNull(message = "chargeYear é obrigatório")
    @Min(value = 2020, message = "Ano inválido")
    @Max(value = 2100, message = "Ano inválido")
    Integer chargeYear,               // 2024
    
    @NotNull(message = "dueDate é obrigatório")
    LocalDate dueDate,                // Data de vencimento (dia X do contrato)
    
    @NotNull(message = "amount é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    BigDecimal amount                 // Valor (variável por contrato - ex: 150.00, 1550.00)
) {}



