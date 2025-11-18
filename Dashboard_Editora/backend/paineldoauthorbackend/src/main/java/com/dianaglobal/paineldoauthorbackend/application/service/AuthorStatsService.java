package com.dianaglobal.paineldoauthorbackend.application.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorStatsService {

    /**
     * Busca estatísticas completas de um autor no e-commerce
     * Usa as credenciais do banco armazenadas no usuário
     * Retorna dados agregados: livros, vendas, receita, pagamentos
     */
    public Optional<AuthorStats> getAuthorStats(Long authorId, String dbUrl, String dbUsername, String dbPassword) {
        if (dbUrl == null || dbUrl.isEmpty()) {
            log.warn("[AUTHOR STATS] E-commerce database URL not configured for author {}", authorId);
            return Optional.empty();
        }

        try {
            return queryAuthorStats(authorId, dbUrl, dbUsername, dbPassword);
        } catch (Exception e) {
            log.error("[AUTHOR STATS] Error fetching stats for author {}: {}", authorId, e.getMessage(), e);
            return Optional.empty();
        }
    }

    private Optional<AuthorStats> queryAuthorStats(Long authorId, String dbUrl, String dbUsername, String dbPassword) {
        try (Connection conn = getEcommerceConnection(dbUrl, dbUsername, dbPassword)) {
            if (conn == null) {
                return Optional.empty();
            }

            // Query principal: estatísticas do autor (baseada no comando 14 do SQL)
            String sql = """
                SELECT 
                    a.id as author_id,
                    a.name as author_name,
                    a.email,
                    
                    -- Livros
                    (SELECT COUNT(*) FROM books WHERE author_id = a.id) as total_books,
                    
                    -- Vendas (pedidos completados)
                    (SELECT COUNT(DISTINCT o.id) 
                     FROM orders o
                     JOIN order_items oi ON oi.order_id = o.id
                     JOIN books b ON b.id::text = oi.book_id
                     WHERE b.author_id = a.id
                     AND o.status = 'COMPLETED') as completed_orders,
                    
                    -- Receita total
                    (SELECT COALESCE(SUM(oi.price * oi.quantity), 0)
                     FROM order_items oi
                     JOIN books b ON b.id::text = oi.book_id
                     JOIN orders o ON o.id = oi.order_id
                     WHERE b.author_id = a.id
                     AND o.status = 'COMPLETED') as total_revenue,
                    
                    -- Pagamentos
                    (SELECT COUNT(*) FROM payment_payouts WHERE author_id = a.id) as total_payouts,
                    (SELECT COALESCE(SUM(amount), 0) FROM payment_payouts WHERE author_id = a.id) as total_paid,
                    
                    -- Conta de pagamento
                    (SELECT COUNT(*) FROM payment_author_accounts WHERE author_id = a.id) as has_payment_account
                    
                FROM authors a
                WHERE a.id = ?
                """;

            try (var stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, authorId);
                
                try (var rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        log.debug("[AUTHOR STATS] Author {} not found in e-commerce database", authorId);
                        return Optional.empty();
                    }

                    AuthorStats stats = AuthorStats.builder()
                            .authorId(rs.getLong("author_id"))
                            .authorName(rs.getString("author_name"))
                            .email(rs.getString("email"))
                            .totalBooks(rs.getLong("total_books"))
                            .completedOrders(rs.getLong("completed_orders"))
                            .totalRevenue(rs.getBigDecimal("total_revenue"))
                            .totalPayouts(rs.getLong("total_payouts"))
                            .totalPaid(rs.getBigDecimal("total_paid"))
                            .hasPaymentAccount(rs.getLong("has_payment_account") > 0)
                            .build();

                    return Optional.of(stats);
                }
            }
        } catch (Exception e) {
            log.error("[AUTHOR STATS] Database error: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Busca vendas recentes (últimos 30 dias) do autor
     */
    public Optional<RecentSales> getRecentSales(Long authorId, String dbUrl, String dbUsername, String dbPassword) {
        if (dbUrl == null || dbUrl.isEmpty()) {
            return Optional.empty();
        }

        try (Connection conn = getEcommerceConnection(dbUrl, dbUsername, dbPassword)) {
            if (conn == null) {
                return Optional.empty();
            }

            String sql = """
                SELECT 
                    COUNT(DISTINCT o.id) as recent_orders,
                    COALESCE(SUM(oi.price * oi.quantity), 0) as recent_revenue
                FROM authors a
                LEFT JOIN books b ON b.author_id = a.id
                LEFT JOIN order_items oi ON oi.book_id = b.id::text
                LEFT JOIN orders o ON o.id = oi.order_id 
                    AND o.status = 'COMPLETED'
                    AND o.created_at >= NOW() - INTERVAL '30 days'
                WHERE a.id = ?
                GROUP BY a.id
                """;

            try (var stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, authorId);
                
                try (var rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        return Optional.of(new RecentSales(0L, BigDecimal.ZERO));
                    }

                    return Optional.of(new RecentSales(
                            rs.getLong("recent_orders"),
                            rs.getBigDecimal("recent_revenue")
                    ));
                }
            }
        } catch (Exception e) {
            log.error("[AUTHOR STATS] Error fetching recent sales: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    private Connection getEcommerceConnection(String dbUrl, String dbUsername, String dbPassword) {
        try {
            return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
        } catch (Exception e) {
            log.error("[AUTHOR STATS] Error connecting to e-commerce database: {}", e.getMessage());
            return null;
        }
    }

    // DTOs internos
    @lombok.Builder
    @lombok.Data
    public static class AuthorStats {
        private Long authorId;
        private String authorName;
        private String email;
        private Long totalBooks;
        private Long completedOrders;
        private BigDecimal totalRevenue;
        private Long totalPayouts;
        private BigDecimal totalPaid;
        private Boolean hasPaymentAccount;
    }

    public record RecentSales(Long recentOrders, BigDecimal recentRevenue) {}
}

