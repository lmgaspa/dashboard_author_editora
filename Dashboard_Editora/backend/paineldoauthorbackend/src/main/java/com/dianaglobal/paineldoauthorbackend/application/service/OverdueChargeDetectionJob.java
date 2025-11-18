package com.dianaglobal.paineldoauthorbackend.application.service;

import com.dianaglobal.paineldoauthorbackend.domain.model.MonthlyCharge;
import com.dianaglobal.paineldoauthorbackend.domain.model.Ticket;
import com.dianaglobal.paineldoauthorbackend.domain.model.TicketCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Job periódico para detectar cobranças atrasadas e criar tickets automaticamente.
 * Executa diariamente às 6h da manhã.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OverdueChargeDetectionJob {

    private final MonthlyChargeService chargeService;
    private final TicketService ticketService;

    @Scheduled(cron = "0 0 6 * * *") // Diariamente às 6h
    public void detectOverdueCharges() {
        log.info("[OVERDUE JOB] Iniciando detecção de cobranças atrasadas...");
        
        try {
            List<MonthlyCharge> overdueCharges = chargeService.findOverdueCharges();
            
            log.info("[OVERDUE JOB] Encontradas {} cobranças atrasadas", overdueCharges.size());
            
            for (MonthlyCharge charge : overdueCharges) {
                try {
                    // Marcar como OVERDUE se ainda estiver PENDING
                    if (charge.getStatus() == com.dianaglobal.paineldoauthorbackend.domain.model.ChargeStatus.PENDING) {
                        chargeService.markAsOverdue(charge.getId());
                    }
                    
                    // Verificar se já existe ticket aberto para esta cobrança
                    boolean hasTicket = ticketService.existsTicketForCharge(charge.getAuthorId(), charge.getId());
                    
                    if (!hasTicket) {
                        // Criar ticket automaticamente
                        Ticket ticket = Ticket.builder()
                                .id(UUID.randomUUID())
                                .createdByUserId("system")
                                .authorId(charge.getAuthorId())
                                .category(TicketCategory.PAGAMENTO)
                                .status(com.dianaglobal.paineldoauthorbackend.domain.model.TicketStatus.OPEN)
                                .relatedChargeId(charge.getId())
                                .title(String.format("Cobrança atrasada - %s/%d", 
                                        getMonthName(charge.getChargeMonth()), charge.getChargeYear()))
                                .description(generateTicketDescription(charge))
                                .createdAt(Instant.now())
                                .updatedAt(Instant.now())
                                .build();
                        
                        ticket = ticketService.createTicket(ticket);
                        
                        log.info("[OVERDUE JOB] Ticket criado automaticamente: {} para cobrança {}", 
                                ticket.getTicketNumber(), charge.getId());
                        
                        // TODO: Enviar e-mail para autor e admin
                    } else {
                        log.debug("[OVERDUE JOB] Já existe ticket aberto para cobrança {}", charge.getId());
                    }
                    
                } catch (Exception e) {
                    log.error("[OVERDUE JOB] Erro ao processar cobrança {}: {}", charge.getId(), e.getMessage(), e);
                }
            }
            
            log.info("[OVERDUE JOB] Detecção de cobranças atrasadas concluída");
            
        } catch (Exception e) {
            log.error("[OVERDUE JOB] Erro ao executar job: {}", e.getMessage(), e);
        }
    }
    
    private String generateTicketDescription(MonthlyCharge charge) {
        long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(
                charge.getDueDate(), 
                LocalDate.now()
        );
        
        return String.format(
                "Pagamento de cobrança identificado como atrasado:\n\n" +
                "• Mês/Ano: %s/%d\n" +
                "• Valor: R$ %.2f\n" +
                "• Data de vencimento: %s\n" +
                "• Dias de atraso: %d dias\n\n" +
                "Por favor, efetue o pagamento ou entre em contato se houver algum problema.",
                getMonthName(charge.getChargeMonth()),
                charge.getChargeYear(),
                charge.getAmount(),
                charge.getDueDate(),
                daysOverdue
        );
    }
    
    private String getMonthName(int month) {
        String[] months = {
            "", "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
        };
        return months[month];
    }
}



