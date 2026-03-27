package com.dianaglobal.paineldoauthorbackend.adapter.out.persistence.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpringTicketMessageRepository extends JpaRepository<TicketMessageEntity, UUID> {
    List<TicketMessageEntity> findByTicketIdOrderByCreatedAtAsc(UUID ticketId);
    List<TicketMessageEntity> findByTicketId(UUID ticketId);
}

