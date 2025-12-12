package com.dianaglobal.paineldoauthorbackend.application.service;

import com.dianaglobal.paineldoauthorbackend.domain.model.Ticket;
import com.dianaglobal.paineldoauthorbackend.domain.model.TicketMessage;
import com.dianaglobal.paineldoauthorbackend.domain.model.TicketStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketService {
    Ticket createTicket(Ticket ticket);
    Optional<Ticket> findById(UUID id);
    Optional<Ticket> findByTicketNumber(String ticketNumber);
    List<Ticket> findAllByAuthorId(String authorId);
    List<Ticket> findAll();
    Ticket updateStatus(UUID ticketId, TicketStatus newStatus, String userId);
    Ticket assignTicket(UUID ticketId, String adminUserId);
    TicketMessage addMessage(UUID ticketId, TicketMessage message);
    List<TicketMessage> getMessages(UUID ticketId);
    String generateTicketNumber(int year);
    boolean existsTicketForCharge(String authorId, UUID chargeId);
}



