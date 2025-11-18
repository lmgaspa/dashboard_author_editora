package com.dianaglobal.paineldoauthorbackend.application.service;

import com.dianaglobal.paineldoauthorbackend.application.port.out.TicketMessageRepositoryPort;
import com.dianaglobal.paineldoauthorbackend.application.port.out.TicketRepositoryPort;
import com.dianaglobal.paineldoauthorbackend.domain.model.Ticket;
import com.dianaglobal.paineldoauthorbackend.domain.model.TicketMessage;
import com.dianaglobal.paineldoauthorbackend.domain.model.TicketStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepositoryPort ticketRepository;
    private final TicketMessageRepositoryPort messageRepository;
    private final TicketPriorityClassifier priorityClassifier;

    @Override
    @Transactional
    public Ticket createTicket(Ticket ticket) {
        // Gerar número do ticket se não existir
        if (ticket.getTicketNumber() == null || ticket.getTicketNumber().isEmpty()) {
            int year = java.time.LocalDate.now().getYear();
            ticket.setTicketNumber(generateTicketNumber(year));
        }
        
        // Classificar prioridade automaticamente
        var classification = priorityClassifier.classify(ticket);
        ticket.setPriority(classification.priority());
        ticket.setPriorityReason(classification.reason());
        
        // Definir timestamps
        if (ticket.getCreatedAt() == null) {
            ticket.setCreatedAt(Instant.now());
        }
        if (ticket.getUpdatedAt() == null) {
            ticket.setUpdatedAt(Instant.now());
        }
        
        ticketRepository.save(ticket);
        log.info("[TICKET SERVICE] Ticket criado: {} - Prioridade: {}", ticket.getTicketNumber(), ticket.getPriority());
        
        return ticket;
    }

    @Override
    public Optional<Ticket> findById(UUID id) {
        return ticketRepository.findById(id);
    }

    @Override
    public Optional<Ticket> findByTicketNumber(String ticketNumber) {
        return ticketRepository.findByTicketNumber(ticketNumber);
    }

    @Override
    public List<Ticket> findAllByAuthorId(String authorId) {
        return ticketRepository.findAllByAuthorId(authorId);
    }

    @Override
    public List<Ticket> findAll() {
        return ticketRepository.findAll();
    }

    @Override
    @Transactional
    public Ticket updateStatus(UUID ticketId, TicketStatus newStatus, String userId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket não encontrado: " + ticketId));
        
        ticket.setStatus(newStatus);
        ticket.setUpdatedAt(Instant.now());
        
        if (newStatus == TicketStatus.RESOLVED) {
            ticket.setResolvedAt(Instant.now());
        } else if (newStatus == TicketStatus.CLOSED) {
            ticket.setClosedAt(Instant.now());
        }
        
        ticketRepository.save(ticket);
        log.info("[TICKET SERVICE] Status do ticket {} atualizado para {} por {}", 
                ticket.getTicketNumber(), newStatus, userId);
        
        return ticket;
    }

    @Override
    @Transactional
    public Ticket assignTicket(UUID ticketId, String adminUserId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket não encontrado: " + ticketId));
        
        ticket.setAssignedToUserId(adminUserId);
        ticket.setUpdatedAt(Instant.now());
        
        ticketRepository.save(ticket);
        log.info("[TICKET SERVICE] Ticket {} atribuído a {}", ticket.getTicketNumber(), adminUserId);
        
        return ticket;
    }

    @Override
    @Transactional
    public TicketMessage addMessage(UUID ticketId, TicketMessage message) {
        // Verificar se ticket existe
        ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket não encontrado: " + ticketId));
        
        message.setTicketId(ticketId);
        if (message.getCreatedAt() == null) {
            message.setCreatedAt(Instant.now());
        }
        
        messageRepository.save(message);
        log.info("[TICKET SERVICE] Mensagem adicionada ao ticket {}", ticketId);
        
        return message;
    }

    @Override
    public List<TicketMessage> getMessages(UUID ticketId) {
        return messageRepository.findAllByTicketIdOrderByCreatedAtAsc(ticketId);
    }

    @Override
    public String generateTicketNumber(int year) {
        Optional<Long> lastNumber = ticketRepository.findLastTicketNumberByYear(year);
        long nextNumber = (lastNumber.isEmpty() || lastNumber.get() == null) ? 1 : lastNumber.get() + 1;
        return String.format("TKT-%d-%06d", year, nextNumber);
    }

    @Override
    public boolean existsTicketForCharge(String authorId, UUID chargeId) {
        return ticketRepository.findByAuthorIdAndRelatedChargeId(authorId, chargeId).isPresent();
    }
}



