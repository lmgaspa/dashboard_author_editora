package com.dianaglobal.paineldoauthorbackend.domain.model;

/**
 * Status de uma cobrança mensal.
 */
public enum ChargeStatus {
    PENDING,    // Pendente (aguardando pagamento)
    PAID,       // Paga (admin confirmou)
    OVERDUE,    // Atrasada (vencida e não paga)
    CANCELLED   // Cancelada
}



