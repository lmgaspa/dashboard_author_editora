package com.dianaglobal.paineldoauthorbackend.application.service;

import com.dianaglobal.paineldoauthorbackend.application.port.out.MonthlyChargeRepositoryPort;
import com.dianaglobal.paineldoauthorbackend.domain.model.ChargeStatus;
import com.dianaglobal.paineldoauthorbackend.domain.model.MonthlyCharge;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonthlyChargeServiceImpl implements MonthlyChargeService {

    private final MonthlyChargeRepositoryPort chargeRepository;

    @Override
    @Transactional
    public MonthlyCharge createCharge(MonthlyCharge charge) {
        // Verificar se já existe cobrança para o mesmo mês/ano/autor
        Optional<MonthlyCharge> existing = chargeRepository.findByAuthorIdAndMonthAndYear(
                charge.getAuthorId(), 
                charge.getChargeMonth(), 
                charge.getChargeYear()
        );
        
        if (existing.isPresent()) {
            throw new IllegalArgumentException(
                    String.format("Já existe cobrança para autor %s no mês %d/%d", 
                            charge.getAuthorId(), charge.getChargeMonth(), charge.getChargeYear())
            );
        }
        
        // Gerar PIX code
        if (charge.getPixCode() == null || charge.getPixCode().isEmpty()) {
            charge.setPixCode(generatePixCode(charge));
        }
        
        // Definir timestamps
        if (charge.getCreatedAt() == null) {
            charge.setCreatedAt(Instant.now());
        }
        if (charge.getUpdatedAt() == null) {
            charge.setUpdatedAt(Instant.now());
        }
        
        // Status padrão
        if (charge.getStatus() == null) {
            charge.setStatus(ChargeStatus.PENDING);
        }
        
        chargeRepository.save(charge);
        log.info("[CHARGE SERVICE] Cobrança criada: {} - Autor: {} - Valor: R$ {}", 
                charge.getId(), charge.getAuthorId(), charge.getAmount());
        
        return charge;
    }

    @Override
    public Optional<MonthlyCharge> findById(UUID id) {
        return chargeRepository.findById(id);
    }

    @Override
    public List<MonthlyCharge> findAllByAuthorId(String authorId) {
        return chargeRepository.findAllByAuthorId(authorId);
    }

    @Override
    public List<MonthlyCharge> findAll() {
        return chargeRepository.findAllByStatus(ChargeStatus.PENDING);
    }

    @Override
    @Transactional
    public MonthlyCharge confirmPayment(UUID chargeId, String adminUserId, String notes) {
        MonthlyCharge charge = chargeRepository.findById(chargeId)
                .orElseThrow(() -> new IllegalArgumentException("Cobrança não encontrada: " + chargeId));
        
        charge.setStatus(ChargeStatus.PAID);
        charge.setPaidAt(Instant.now());
        charge.setConfirmedByUserId(adminUserId);
        charge.setConfirmedAt(Instant.now());
        charge.setUpdatedAt(Instant.now());
        if (notes != null && !notes.trim().isEmpty()) {
            charge.setNotes(notes);
        }
        
        chargeRepository.save(charge);
        log.info("[CHARGE SERVICE] Pagamento confirmado para cobrança {} por {}", chargeId, adminUserId);
        
        return charge;
    }

    @Override
    public List<MonthlyCharge> findOverdueCharges() {
        LocalDate today = LocalDate.now();
        return chargeRepository.findAllByStatusAndDueDateBefore(ChargeStatus.PENDING, today);
    }

    @Override
    @Transactional
    public void markAsOverdue(UUID chargeId) {
        MonthlyCharge charge = chargeRepository.findById(chargeId)
                .orElseThrow(() -> new IllegalArgumentException("Cobrança não encontrada: " + chargeId));
        
        charge.setStatus(ChargeStatus.OVERDUE);
        charge.setUpdatedAt(Instant.now());
        
        chargeRepository.save(charge);
        log.info("[CHARGE SERVICE] Cobrança {} marcada como atrasada", chargeId);
    }

    @Override
    public String generatePixCode(MonthlyCharge charge) {
        // TODO: Integrar com gerador de PIX real (ex: API do banco)
        // Por enquanto, retorna um código mock
        return String.format("00020126580014BR.GOV.BCB.PIX0136%s5204000053039865802BR5913PAINEL%20VIA6008BRASILIA62070503***6304%s",
                charge.getAuthorId(),
                generateChecksum(charge));
    }
    
    private String generateChecksum(MonthlyCharge charge) {
        // Checksum simples (deve ser substituído por algoritmo real)
        return String.format("%04d", (charge.getId().hashCode() % 10000));
    }
}



