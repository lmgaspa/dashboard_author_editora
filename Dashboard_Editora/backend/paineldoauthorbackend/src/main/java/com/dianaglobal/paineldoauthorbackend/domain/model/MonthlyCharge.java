package com.dianaglobal.paineldoauthorbackend.domain.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Domain model para cobrança mensal.
 * Valor varia por contrato (ex: R$ 150,00, R$ 1.550,00, etc.)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyCharge {
    private UUID id;
    private String authorId;                    // author_id do usuário
    private String createdByUserId;            // Admin que criou
    private Integer chargeMonth;               // Mês (1-12)
    private Integer chargeYear;                // Ano (ex: 2024)
    private BigDecimal amount;                 // Valor (variável por contrato)
    private LocalDate dueDate;                 // Data de vencimento
    private LocalDate chargeDate;              // Data que a cobrança foi criada
    private ChargeStatus status;               // Status do pagamento
    private Instant paidAt;                     // Quando foi pago
    private String confirmedByUserId;          // Admin que confirmou
    private Instant confirmedAt;                // Quando admin confirmou
    private String pixCode;                    // Código PIX gerado
    private Instant pixExpiresAt;               // Quando PIX expira
    private Instant createdAt;                 // Data de criação
    private Instant updatedAt;                  // Data de atualização
    private String notes;                      // Notas do admin
}



