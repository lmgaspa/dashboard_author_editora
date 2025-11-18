package com.dianaglobal.paineldoauthorbackend.domain.model;

/**
 * Status de um ticket.
 */
public enum TicketStatus {
    OPEN,           // Criado, aguardando admin
    IN_PROGRESS,    // Admin está trabalhando
    WAITING_AUTHOR, // Aguardando resposta do autor
    WAITING_ADMIN,  // Aguardando resposta do admin
    RESOLVED,       // Autor marcou como resolvido
    CLOSED          // Admin fechou
}



