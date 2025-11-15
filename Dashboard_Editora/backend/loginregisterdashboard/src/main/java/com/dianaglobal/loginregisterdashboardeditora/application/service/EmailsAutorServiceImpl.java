package com.dianaglobal.loginregisterdashboardeditora.application.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.dianaglobal.loginregisterdashboardeditora.adapter.in.dto.emails.PainelEmailsAutorDTO;
import com.dianaglobal.loginregisterdashboardeditora.adapter.in.dto.emails.ResumoEmailClienteDTO;
import com.dianaglobal.loginregisterdashboardeditora.adapter.in.dto.emails.ResumoEmailRepasseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementação do service de e-mails do autor.
 * Busca dados de e-mails de clientes (orders) e e-mails de repasse (payout_email).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailsAutorServiceImpl implements EmailsAutorService {

    @Override
    public PainelEmailsAutorDTO montarPainelEmailsAutor(
            long autorId,
            String dbUrl,
            String dbUsername,
            String dbPassword
    ) {
        if (dbUrl == null || dbUrl.trim().isEmpty()) {
            log.warn("[EMAILS AUTOR] URL do banco do e-commerce não configurada para autor {}", autorId);
            return null;
        }

        try (Connection conn = getEcommerceConnection(dbUrl, dbUsername, dbPassword)) {
            if (conn == null) {
                return null;
            }

            // Buscar e-mails de clientes (agrupados por email)
            List<ResumoEmailClienteDTO> emailsClientes = buscarEmailsClientes(conn, autorId);

            // Buscar e-mails de repasse
            List<ResumoEmailRepasseDTO> emailsRepasse = buscarEmailsRepasse(conn, autorId);

            return new PainelEmailsAutorDTO(emailsClientes, emailsRepasse);

        } catch (Exception e) {
            log.error("[EMAILS AUTOR] Erro ao montar painel para autor {}: {}", autorId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Busca e-mails de clientes agrupados por email.
     * Considera apenas pedidos que contêm livros do autor.
     */
    private List<ResumoEmailClienteDTO> buscarEmailsClientes(Connection conn, long autorId) throws Exception {
        List<ResumoEmailClienteDTO> emails = new ArrayList<>();

        String sql = """
            SELECT 
                o.email,
                COUNT(DISTINCT o.id) AS total_pedidos,
                COUNT(DISTINCT CASE WHEN o.status = 'CONFIRMED' THEN o.id END) AS total_pedidos_confirmados,
                COALESCE(SUM(CASE WHEN o.status = 'CONFIRMED' THEN oi.quantity * oi.price ELSE 0 END), 0) AS valor_total_confirmado,
                MIN(o.created_at) AS primeiro_pedido_em,
                MAX(o.created_at) AS ultimo_pedido_em
            FROM orders o
            JOIN order_items oi ON oi.order_id = o.id
            JOIN books b ON b.id::text = oi.book_id
            WHERE b.author_id = ?
              AND o.email IS NOT NULL
              AND o.email != ''
            GROUP BY o.email
            ORDER BY ultimo_pedido_em DESC
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, autorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String email = rs.getString("email");
                    long totalPedidos = rs.getLong("total_pedidos");
                    long totalPedidosConfirmados = rs.getLong("total_pedidos_confirmados");
                    BigDecimal valorTotalConfirmado = rs.getBigDecimal("valor_total_confirmado");
                    if (valorTotalConfirmado == null) {
                        valorTotalConfirmado = BigDecimal.ZERO;
                    }

                    Instant primeiroPedidoEm = null;
                    Instant ultimoPedidoEm = null;
                    
                    java.sql.Timestamp primeiroTimestamp = rs.getTimestamp("primeiro_pedido_em");
                    if (primeiroTimestamp != null) {
                        primeiroPedidoEm = primeiroTimestamp.toInstant();
                    }
                    
                    java.sql.Timestamp ultimoTimestamp = rs.getTimestamp("ultimo_pedido_em");
                    if (ultimoTimestamp != null) {
                        ultimoPedidoEm = ultimoTimestamp.toInstant();
                    }

                    ResumoEmailClienteDTO resumo = new ResumoEmailClienteDTO(
                            email,
                            totalPedidos,
                            totalPedidosConfirmados,
                            valorTotalConfirmado,
                            primeiroPedidoEm,
                            ultimoPedidoEm
                    );
                    emails.add(resumo);
                }
            }
        }

        return emails;
    }

    /**
     * Busca e-mails de repasse filtrados por autor.
     * Filtra via order_id que pertence a livros do autor.
     * Usa DISTINCT para evitar duplicatas quando um pedido tem múltiplos itens do autor.
     */
    private List<ResumoEmailRepasseDTO> buscarEmailsRepasse(Connection conn, long autorId) throws Exception {
        List<ResumoEmailRepasseDTO> emails = new ArrayList<>();

        String sql = """
            SELECT DISTINCT
                pe.id,
                pe.order_id,
                pe.payout_id,
                pe.to_email,
                pe.email_type,
                pe.status,
                pe.sent_at,
                pe.error_message
            FROM payout_email pe
            JOIN orders o ON o.id = pe.order_id
            JOIN order_items oi ON oi.order_id = o.id
            JOIN books b ON b.id::text = oi.book_id
            WHERE b.author_id = ?
            ORDER BY pe.sent_at DESC NULLS LAST, pe.id DESC
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, autorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Long id = rs.getLong("id");
                    if (rs.wasNull()) {
                        id = null;
                    }

                    Long pedidoId = rs.getLong("order_id");
                    if (rs.wasNull()) {
                        pedidoId = null;
                    }

                    Long repasseId = rs.getLong("payout_id");
                    if (rs.wasNull()) {
                        repasseId = null;
                    }

                    String emailDestinatario = rs.getString("to_email");
                    String tipoEmail = rs.getString("email_type");
                    String status = rs.getString("status");
                    String mensagemErro = rs.getString("error_message");

                    Instant enviadoEm = null;
                    java.sql.Timestamp sentAtTimestamp = rs.getTimestamp("sent_at");
                    if (sentAtTimestamp != null) {
                        enviadoEm = sentAtTimestamp.toInstant();
                    }

                    ResumoEmailRepasseDTO resumo = new ResumoEmailRepasseDTO(
                            id,
                            pedidoId,
                            repasseId,
                            emailDestinatario,
                            tipoEmail,
                            status,
                            enviadoEm,
                            mensagemErro
                    );
                    emails.add(resumo);
                }
            }
        }

        return emails;
    }

    /**
     * Estabelece conexão com o banco de dados do e-commerce.
     * Converte URL no formato postgres:// para jdbc:postgresql:// se necessário.
     */
    private Connection getEcommerceConnection(String dbUrl, String dbUsername, String dbPassword) {
        try {
            // Converter URL do formato postgres:// para jdbc:postgresql:// se necessário
            String jdbcUrl = converterUrlParaJdbc(dbUrl);
            return DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword);
        } catch (Exception e) {
            log.error("[EMAILS AUTOR] Erro ao conectar ao banco do e-commerce: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Converte URL do formato postgres:// para jdbc:postgresql://
     * Exemplo: postgres://user:pass@host:5432/db -> jdbc:postgresql://host:5432/db
     */
    private String converterUrlParaJdbc(String url) {
        if (url == null || url.trim().isEmpty()) {
            return url;
        }

        // Se já está no formato JDBC, retornar como está
        if (url.startsWith("jdbc:postgresql://")) {
            return url;
        }

        // Se está no formato postgres://, converter para jdbc:postgresql://
        if (url.startsWith("postgres://")) {
            // Remover postgres://
            String semPrefixo = url.substring("postgres://".length());
            
            // Encontrar @ para separar credenciais do resto da URL
            int atIndex = semPrefixo.indexOf('@');
            if (atIndex > 0) {
                // Extrair apenas a parte após @ (host:port/database)
                String hostPortDb = semPrefixo.substring(atIndex + 1);
                return "jdbc:postgresql://" + hostPortDb;
            } else {
                // Se não tem @, assumir que já está sem credenciais
                return "jdbc:postgresql://" + semPrefixo;
            }
        }

        // Se não reconhecer o formato, retornar como está
        return url;
    }
}

