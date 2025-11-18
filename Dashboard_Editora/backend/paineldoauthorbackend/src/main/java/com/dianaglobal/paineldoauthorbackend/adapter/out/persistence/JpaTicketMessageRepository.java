package com.dianaglobal.paineldoauthorbackend.adapter.out.persistence;

import com.dianaglobal.paineldoauthorbackend.adapter.out.persistence.entity.SpringTicketMessageRepository;
import com.dianaglobal.paineldoauthorbackend.adapter.out.persistence.entity.TicketMessageEntity;
import com.dianaglobal.paineldoauthorbackend.application.port.out.TicketMessageRepositoryPort;
import com.dianaglobal.paineldoauthorbackend.domain.model.TicketMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaTicketMessageRepository implements TicketMessageRepositoryPort {

    private final SpringTicketMessageRepository repository;

    @Override
    public void save(TicketMessage message) {
        repository.save(TicketMessageEntity.fromDomain(message));
    }

    @Override
    public Optional<TicketMessage> findById(UUID id) {
        return repository.findById(id).map(TicketMessageEntity::toDomain);
    }

    @Override
    public List<TicketMessage> findAllByTicketId(UUID ticketId) {
        return repository.findByTicketId(ticketId).stream()
                .map(TicketMessageEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<TicketMessage> findAllByTicketIdOrderByCreatedAtAsc(UUID ticketId) {
        return repository.findByTicketIdOrderByCreatedAtAsc(ticketId).stream()
                .map(TicketMessageEntity::toDomain)
                .collect(Collectors.toList());
    }
}



