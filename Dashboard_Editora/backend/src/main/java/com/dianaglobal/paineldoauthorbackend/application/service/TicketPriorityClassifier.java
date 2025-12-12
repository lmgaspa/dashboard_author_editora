package com.dianaglobal.paineldoauthorbackend.application.service;

import com.dianaglobal.paineldoauthorbackend.domain.model.Ticket;
import com.dianaglobal.paineldoauthorbackend.domain.model.TicketCategory;
import com.dianaglobal.paineldoauthorbackend.domain.model.TicketPriority;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Classificador automático de prioridade de tickets.
 * Sistema classifica baseado em palavras-chave e categoria.
 * Apenas admin vê a prioridade.
 */
@Slf4j
@Component
public class TicketPriorityClassifier {

    /**
     * Classifica a prioridade de um ticket automaticamente.
     * 
     * @param ticket Ticket a ser classificado
     * @return Prioridade classificada e motivo
     */
    public PriorityClassificationResult classify(Ticket ticket) {
        String title = ticket.getTitle() != null ? ticket.getTitle().toLowerCase() : "";
        String description = ticket.getDescription() != null ? ticket.getDescription().toLowerCase() : "";
        TicketCategory category = ticket.getCategory();
        
        int score = 0;
        StringBuilder reason = new StringBuilder();
        
        // Palavras-chave de ALTA prioridade
        String[] highPriorityKeywords = {
            "atraso", "atrasado", "vencido", "vencimento",
            "não consigo pagar", "problema com pagamento",
            "erro crítico", "sistema fora do ar", "não funciona",
            "urgente", "emergência", "emergencia"
        };
        
        // Palavras-chave de MÉDIA prioridade
        String[] mediumPriorityKeywords = {
            "dúvida", "duvida", "como fazer", "não entendi",
            "problema", "erro", "bug"
        };
        
        // Palavras-chave de BAIXA prioridade
        String[] lowPriorityKeywords = {
            "sugestão", "sugestao", "melhoria", "ideia",
            "pergunta", "informação", "informacao"
        };
        
        String text = title + " " + description;
        
        // Contar palavras-chave de alta prioridade
        int highCount = 0;
        for (String keyword : highPriorityKeywords) {
            if (text.contains(keyword)) {
                score += 3;
                highCount++;
            }
        }
        if (highCount > 0) {
            reason.append("Palavras-chave de alta prioridade encontradas (").append(highCount).append("). ");
        }
        
        // Contar palavras-chave de média prioridade
        int mediumCount = 0;
        for (String keyword : mediumPriorityKeywords) {
            if (text.contains(keyword)) {
                score += 2;
                mediumCount++;
            }
        }
        if (mediumCount > 0) {
            reason.append("Palavras-chave de média prioridade encontradas (").append(mediumCount).append("). ");
        }
        
        // Contar palavras-chave de baixa prioridade
        int lowCount = 0;
        for (String keyword : lowPriorityKeywords) {
            if (text.contains(keyword)) {
                score += 1;
                lowCount++;
            }
        }
        if (lowCount > 0) {
            reason.append("Palavras-chave de baixa prioridade encontradas (").append(lowCount).append("). ");
        }
        
        // Categoria influencia
        if (category == TicketCategory.PAGAMENTO) {
            score += 2;
            reason.append("Categoria PAGAMENTO aumenta prioridade. ");
        }
        
        // Cobrança relacionada aumenta prioridade
        if (ticket.getRelatedChargeId() != null) {
            score += 2;
            reason.append("Ticket relacionado a cobrança. ");
        }
        
        // Classificar
        TicketPriority priority;
        if (score >= 5) {
            priority = TicketPriority.HIGH;
        } else if (score >= 3) {
            priority = TicketPriority.MEDIUM;
        } else {
            priority = TicketPriority.LOW;
        }
        
        if (reason.length() == 0) {
            reason.append("Classificação padrão (sem palavras-chave específicas).");
        }
        
        log.debug("[PRIORITY CLASSIFIER] Ticket {} classificado como {} (score: {})", 
                ticket.getTicketNumber(), priority, score);
        
        return new PriorityClassificationResult(priority, reason.toString().trim());
    }
    
    /**
     * Resultado da classificação de prioridade.
     */
    public record PriorityClassificationResult(
        TicketPriority priority,
        String reason
    ) {}
}



