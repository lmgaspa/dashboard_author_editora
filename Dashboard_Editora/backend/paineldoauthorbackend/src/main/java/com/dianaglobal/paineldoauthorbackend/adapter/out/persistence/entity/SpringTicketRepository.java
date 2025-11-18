package com.dianaglobal.paineldoauthorbackend.adapter.out.persistence.entity;

import com.dianaglobal.paineldoauthorbackend.domain.model.TicketCategory;
import com.dianaglobal.paineldoauthorbackend.domain.model.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringTicketRepository extends JpaRepository<TicketEntity, UUID> {
    Optional<TicketEntity> findByTicketNumber(String ticketNumber);
    Optional<TicketEntity> findByAuthorIdAndRelatedChargeId(String authorId, UUID chargeId);
    List<TicketEntity> findByAuthorId(String authorId);
    List<TicketEntity> findByAuthorIdAndStatus(String authorId, TicketStatus status);
    List<TicketEntity> findByAuthorIdAndCategory(String authorId, TicketCategory category);
    List<TicketEntity> findByStatus(TicketStatus status);
    List<TicketEntity> findByAssignedToUserId(String userId);
    
    @Query("SELECT MAX(CAST(SUBSTRING(t.ticketNumber, 9) AS long)) FROM TicketEntity t WHERE SUBSTRING(t.ticketNumber, 5, 4) = :year")
    Optional<Long> findLastTicketNumberByYear(@Param("year") String year);
}



