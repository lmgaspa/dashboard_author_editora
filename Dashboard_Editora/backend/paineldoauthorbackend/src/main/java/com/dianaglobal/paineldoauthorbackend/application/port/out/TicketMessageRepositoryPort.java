package com.dianaglobal.paineldoauthorbackend.application.port.out;

import com.dianaglobal.paineldoauthorbackend.domain.model.TicketMessage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketMessageRepositoryPort {
    void save(TicketMessage message);
    Optional<TicketMessage> findById(UUID id);
    List<TicketMessage> findAllByTicketId(UUID ticketId);
    List<TicketMessage> findAllByTicketIdOrderByCreatedAtAsc(UUID ticketId);
}



