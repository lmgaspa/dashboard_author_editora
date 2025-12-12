package com.dianaglobal.paineldoauthorbackend.adapter.out.persistence;

import com.dianaglobal.paineldoauthorbackend.adapter.out.persistence.entity.SpringTicketRepository;
import com.dianaglobal.paineldoauthorbackend.adapter.out.persistence.entity.TicketEntity;
import com.dianaglobal.paineldoauthorbackend.application.port.out.TicketRepositoryPort;
import com.dianaglobal.paineldoauthorbackend.domain.model.Ticket;
import com.dianaglobal.paineldoauthorbackend.domain.model.TicketCategory;
import com.dianaglobal.paineldoauthorbackend.domain.model.TicketStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaTicketRepository implements TicketRepositoryPort {

    private final SpringTicketRepository repository;

    @Override
    public void save(Ticket ticket) {
        repository.save(TicketEntity.fromDomain(ticket));
    }

    @Override
    public Optional<Ticket> findById(UUID id) {
        return repository.findById(id).map(TicketEntity::toDomain);
    }

    @Override
    public Optional<Ticket> findByTicketNumber(String ticketNumber) {
        return repository.findByTicketNumber(ticketNumber).map(TicketEntity::toDomain);
    }

    @Override
    public Optional<Ticket> findByAuthorIdAndRelatedChargeId(String authorId, UUID chargeId) {
        return repository.findByAuthorIdAndRelatedChargeId(authorId, chargeId)
                .map(TicketEntity::toDomain);
    }

    @Override
    public List<Ticket> findAllByAuthorId(String authorId) {
        return repository.findByAuthorId(authorId).stream()
                .map(TicketEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findAllByAuthorIdAndStatus(String authorId, TicketStatus status) {
        return repository.findByAuthorIdAndStatus(authorId, status).stream()
                .map(TicketEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findAllByAuthorIdAndCategory(String authorId, TicketCategory category) {
        return repository.findByAuthorIdAndCategory(authorId, category).stream()
                .map(TicketEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findAll() {
        return repository.findAll().stream()
                .map(TicketEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findAllByStatus(TicketStatus status) {
        return repository.findByStatus(status).stream()
                .map(TicketEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Ticket> findAllByAssignedToUserId(String userId) {
        return repository.findByAssignedToUserId(userId).stream()
                .map(TicketEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Long> findLastTicketNumberByYear(int year) {
        return repository.findLastTicketNumberByYear(String.valueOf(year));
    }
}



