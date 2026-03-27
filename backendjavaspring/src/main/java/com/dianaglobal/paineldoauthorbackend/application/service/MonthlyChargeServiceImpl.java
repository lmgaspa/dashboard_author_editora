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
    private final com.dianaglobal.paineldoauthorbackend.adapter.out.efi.EfiPayService efiPayService;

    @Override
    @Transactional
    public MonthlyCharge createCharge(MonthlyCharge charge) {
        // Verificar se já existe cobrança para o mesmo mês/ano/autor
        Optional<MonthlyCharge> existing = chargeRepository.findByAuthorIdAndMonthAndYear(
                charge.getAuthorId(),
                charge.getChargeMonth(),
                charge.getChargeYear());

        if (existing.isPresent()) {
            throw new IllegalArgumentException(
                    String.format("Já existe cobrança para autor %s no mês %d/%d",
                            charge.getAuthorId(), charge.getChargeMonth(), charge.getChargeYear()));
        }

        // Gerar PIX via EFI
        try {
            if (charge.getAmount() != null && charge.getAmount().signum() > 0) {
                efiPayService.generatePixCharge(charge);
            }
        } catch (Exception e) {
            log.error("[CHARGE SERVICE] Erro ao gerar PIX para cobrança: {}", e.getMessage());
            // Não impede a criação, mas loga erro. Pode-se decidir falhar aqui se PIX for
            // obrigatório.
            // Para garantir robustez, talvez queiramos persistir sem PIX e tentar depois
            // via job,
            // mas o requisito do usuário implica que o PIX deve estar disponível.
            // Vamos lançar exceção para garantir que o admin saiba que falhou.
            throw new RuntimeException("Falha ao gerar cobrança PIX: " + e.getMessage(), e);
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
        log.info("[CHARGE SERVICE] Cobrança criada: {} - Autor: {} - Valor: R$ {} - TxID: {}",
                charge.getId(), charge.getAuthorId(), charge.getAmount(), charge.getTxid());

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
        // Redundante se usarmos o createCharge, mas mantido para compatibilidade se
        // chamado isoladamente
        // (ex: re-gerar pix)
        efiPayService.generatePixCharge(charge);
        return charge.getPixCode();
    }
}
