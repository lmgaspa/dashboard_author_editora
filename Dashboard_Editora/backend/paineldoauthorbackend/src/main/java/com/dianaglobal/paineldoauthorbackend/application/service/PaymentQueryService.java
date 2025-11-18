package com.dianaglobal.paineldoauthorbackend.application.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.AuthorPaymentDTO;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.PaymentSummaryDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Serviço para buscar pagamentos de autores no banco do e-commerce.
 * Conecta ao banco do e-commerce usando as credenciais armazenadas no usuário.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentQueryService {

    /**
     * Busca resumo de pagamentos para um autor.
     * Busca pagamentos através de payment_webhook_events e payment_payouts.
     */
    public Optional<PaymentSummaryDTO> getPaymentSummary(
            Long authorId,
            String dbUrl,
            String dbUsername,
            String dbPassword,
            int limit
    ) {
        if (dbUrl == null || dbUrl.isEmpty()) {
            log.warn("[PAYMENT QUERY] E-commerce database URL not configured for author {}", authorId);
            return Optional.empty();
        }

        try {
            return queryPaymentSummary(authorId, dbUrl, dbUsername, dbPassword, limit);
        } catch (Exception e) {
            log.error("[PAYMENT QUERY] Error fetching payment summary for author {}: {}", authorId, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Busca lista de pagamentos para um autor (com paginação).
     */
    public List<AuthorPaymentDTO> listPaymentsForAuthor(
            Long authorId,
            String dbUrl,
            String dbUsername,
            String dbPassword,
            int offset,
            int limit
    ) {
        if (dbUrl == null || dbUrl.isEmpty()) {
            log.warn("[PAYMENT QUERY] E-commerce database URL not configured for author {}", authorId);
            return List.of();
        }

        try {
            return queryPayments(authorId, dbUrl, dbUsername, dbPassword, offset, limit);
        } catch (Exception e) {
            log.error("[PAYMENT QUERY] Error listing payments for author {}: {}", authorId, e.getMessage(), e);
            return List.of();
        }
    }

    private Optional<PaymentSummaryDTO> queryPaymentSummary(
            Long authorId,
            String dbUrl,
            String dbUsername,
            String dbPassword,
            int limit
    ) {
        try (Connection conn = getEcommerceConnection(dbUrl, dbUsername, dbPassword)) {
            if (conn == null) {
                return Optional.empty();
            }

            // Buscar nome do autor
            String authorName;
            try (var stmt = conn.prepareStatement("SELECT name FROM authors WHERE id = ?")) {
                stmt.setLong(1, authorId);
                try (var rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        log.debug("[PAYMENT QUERY] Author {} not found in e-commerce database", authorId);
                        return Optional.empty();
                    }
                    authorName = rs.getString("name");
                }
            }

            // Buscar pagamentos via payment_webhook_events (Pix, etc)
            // Assumindo que order_ref ou payload_json pode ter informações que relacionam com orders/books
            // Por enquanto, vamos buscar de payment_payouts que é mais direto
            String sql = """
                SELECT 
                    pp.id as payment_id,
                    pp.order_id,
                    pp.author_id,
                    COALESCE(b.title, 'N/A') as book_title,
                    COALESCE(b.id::text, '') as book_id,
                    pp.amount,
                    pp.status,
                    pp.paid_at,
                    'PAYOUT' as provider,
                    NULL as external_id
                FROM payment_payouts pp
                LEFT JOIN orders o ON o.id = pp.order_id
                LEFT JOIN order_items oi ON oi.order_id = o.id
                LEFT JOIN books b ON b.id::text = oi.book_id AND b.author_id = pp.author_id
                WHERE pp.author_id = ?
                ORDER BY pp.paid_at DESC NULLS LAST, pp.id DESC
                LIMIT ?
                """;

            List<AuthorPaymentDTO> payments = new ArrayList<>();
            BigDecimal totalReceived = BigDecimal.ZERO;
            BigDecimal recentReceived = BigDecimal.ZERO;
            long totalPayments = 0;
            long recentPayments = 0;

            try (var stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, authorId);
                stmt.setInt(2, limit > 0 ? limit : 100);
                
                try (var rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        OffsetDateTime paidAt = rs.getTimestamp("paid_at") != null
                                ? rs.getTimestamp("paid_at").toInstant().atOffset(java.time.ZoneOffset.UTC)
                                : null;

                        BigDecimal amount = rs.getBigDecimal("amount");
                        if (amount != null) {
                            totalReceived = totalReceived.add(amount);
                            totalPayments++;
                            
                            // Verificar se é recente (últimos 30 dias)
                            if (paidAt != null && paidAt.isAfter(
                                    OffsetDateTime.now().minusDays(30))) {
                                recentReceived = recentReceived.add(amount);
                                recentPayments++;
                            }
                        }

                        AuthorPaymentDTO payment = new AuthorPaymentDTO(
                                rs.getLong("payment_id"),
                                rs.getObject("order_id", Long.class),
                                rs.getLong("author_id"),
                                rs.getString("book_title"),
                                rs.getString("book_id"),
                                amount,
                                rs.getString("status"),
                                paidAt,
                                rs.getString("provider"),
                                rs.getString("external_id")
                        );
                        payments.add(payment);
                    }
                }
            }

            // Buscar total geral (sem limite)
            try (var stmt = conn.prepareStatement(
                    "SELECT COUNT(*), COALESCE(SUM(amount), 0) FROM payment_payouts WHERE author_id = ?")) {
                stmt.setLong(1, authorId);
                try (var rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        totalPayments = rs.getLong(1);
                        totalReceived = rs.getBigDecimal(2);
                    }
                }
            }

            // Buscar totais recentes (últimos 30 dias)
            try (var stmt = conn.prepareStatement(
                    "SELECT COUNT(*), COALESCE(SUM(amount), 0) FROM payment_payouts " +
                    "WHERE author_id = ? AND paid_at >= NOW() - INTERVAL '30 days'")) {
                stmt.setLong(1, authorId);
                try (var rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        recentPayments = rs.getLong(1);
                        recentReceived = rs.getBigDecimal(2);
                    }
                }
            }

            PaymentSummaryDTO summary = new PaymentSummaryDTO(
                    authorId,
                    authorName,
                    totalReceived != null ? totalReceived : BigDecimal.ZERO,
                    totalPayments,
                    recentReceived != null ? recentReceived : BigDecimal.ZERO,
                    recentPayments,
                    payments
            );

            return Optional.of(summary);

        } catch (Exception e) {
            log.error("[PAYMENT QUERY] Database error: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    private List<AuthorPaymentDTO> queryPayments(
            Long authorId,
            String dbUrl,
            String dbUsername,
            String dbPassword,
            int offset,
            int limit
    ) {
        List<AuthorPaymentDTO> payments = new ArrayList<>();

        try (Connection conn = getEcommerceConnection(dbUrl, dbUsername, dbPassword)) {
            if (conn == null) {
                return payments;
            }

            String sql = """
                SELECT 
                    pp.id as payment_id,
                    pp.order_id,
                    pp.author_id,
                    COALESCE(b.title, 'N/A') as book_title,
                    COALESCE(b.id::text, '') as book_id,
                    pp.amount,
                    pp.status,
                    pp.paid_at,
                    'PAYOUT' as provider,
                    NULL as external_id
                FROM payment_payouts pp
                LEFT JOIN orders o ON o.id = pp.order_id
                LEFT JOIN order_items oi ON oi.order_id = o.id
                LEFT JOIN books b ON b.id::text = oi.book_id AND b.author_id = pp.author_id
                WHERE pp.author_id = ?
                ORDER BY pp.paid_at DESC NULLS LAST, pp.id DESC
                LIMIT ? OFFSET ?
                """;

            try (var stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, authorId);
                stmt.setInt(2, limit > 0 ? limit : 50);
                stmt.setInt(3, offset >= 0 ? offset : 0);
                
                try (var rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        OffsetDateTime paidAt = rs.getTimestamp("paid_at") != null
                                ? rs.getTimestamp("paid_at").toInstant().atOffset(java.time.ZoneOffset.UTC)
                                : null;

                        AuthorPaymentDTO payment = new AuthorPaymentDTO(
                                rs.getLong("payment_id"),
                                rs.getObject("order_id", Long.class),
                                rs.getLong("author_id"),
                                rs.getString("book_title"),
                                rs.getString("book_id"),
                                rs.getBigDecimal("amount"),
                                rs.getString("status"),
                                paidAt,
                                rs.getString("provider"),
                                rs.getString("external_id")
                        );
                        payments.add(payment);
                    }
                }
            }

        } catch (Exception e) {
            log.error("[PAYMENT QUERY] Error querying payments: {}", e.getMessage(), e);
        }

        return payments;
    }

    private Connection getEcommerceConnection(String dbUrl, String dbUsername, String dbPassword) {
        try {
            return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
        } catch (Exception e) {
            log.error("[PAYMENT QUERY] Error connecting to e-commerce database: {}", e.getMessage());
            return null;
        }
    }
}

