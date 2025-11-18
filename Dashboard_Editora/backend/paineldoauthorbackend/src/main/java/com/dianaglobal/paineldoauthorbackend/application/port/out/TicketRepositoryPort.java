package com.dianaglobal.paineldoauthorbackend.application.port.out;

import com.dianaglobal.paineldoauthorbackend.domain.model.Ticket;
import com.dianaglobal.paineldoauthorbackend.domain.model.TicketCategory;
import com.dianaglobal.paineldoauthorbackend.domain.model.TicketStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketRepositoryPort {
    void save(Ticket ticket);
    Optional<Ticket> findById(UUID id);
    Optional<Ticket> findByTicketNumber(String ticketNumber);
    Optional<Ticket> findByAuthorIdAndRelatedChargeId(String authorId, UUID chargeId);
    List<Ticket> findAllByAuthorId(String authorId);
    List<Ticket> findAllByAuthorIdAndStatus(String authorId, TicketStatus status);
    List<Ticket> findAllByAuthorIdAndCategory(String authorId, TicketCategory category);
    List<Ticket> findAll();
    List<Ticket> findAllByStatus(TicketStatus status);
    List<Ticket> findAllByAssignedToUserId(String userId);
    Optional<Long> findLastTicketNumberByYear(int year);
}



