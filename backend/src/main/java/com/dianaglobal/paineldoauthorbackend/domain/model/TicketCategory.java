package com.dianaglobal.paineldoauthorbackend.domain.model;

/**
 * Categoria de um ticket.
 */
public enum TicketCategory {
    PAGAMENTO, // Relacionado a pagamentos/cobranças
    TECNICO, // Problemas técnicos
    ALTERACAO, // Alteração de dados cadastrais/bancários
    DUVIDA, // Dúvidas gerais
    OUTRO // Outros assuntos
}
