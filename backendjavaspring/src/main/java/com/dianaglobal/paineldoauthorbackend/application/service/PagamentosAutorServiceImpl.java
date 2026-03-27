package com.dianaglobal.paineldoauthorbackend.application.service;

import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.pagamentos.FunilVendasDTO;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.pagamentos.PagamentosAutorResumoDTO;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.pagamentos.PainelPagamentosAutorDTO;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.pagamentos.VendaRecenteDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementação do service de pagamentos do autor.
 * Busca dados de payment_payouts.amount_net para valores reais (após taxas e margens).
 * Usa orders apenas para contagem de pedidos no funil de vendas.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PagamentosAutorServiceImpl implements PagamentosAutorService {

    @Override
    public PainelPagamentosAutorDTO montarPainelPagamentosAutor(
            long autorId,
            String dbUrl,
            String dbUsername,
            String dbPassword
    ) {
        if (dbUrl == null || dbUrl.trim().isEmpty()) {
            log.warn("[PAGAMENTOS AUTOR] URL do banco do e-commerce não configurada para autor {}", autorId);
            return null;
        }

        try (Connection conn = getEcommerceConnection(dbUrl, dbUsername, dbPassword)) {
            if (conn == null) {
                return null;
            }

            // Buscar nome do autor
            String nomeAutor = buscarNomeAutor(conn, autorId);
            if (nomeAutor == null) {
                log.debug("[PAGAMENTOS AUTOR] Autor {} não encontrado no banco do e-commerce", autorId);
                return null;
            }

            // Calcular valores reais usando payment_payouts.amount_net
            double valorVendasConfirmadas = calcularValorVendasConfirmadas(conn, autorId);
            double valorJaRecebido = calcularValorJaRecebido(conn, autorId);
            double valorAReceber = calcularValorAReceber(conn, autorId);

            // Criar resumo
            PagamentosAutorResumoDTO resumo = new PagamentosAutorResumoDTO(
                    autorId,
                    nomeAutor,
                    valorVendasConfirmadas,
                    valorJaRecebido,
                    valorAReceber
            );

            // Calcular funil de vendas (métricas simples e claras)
            FunilVendasDTO funilVendas = calcularFunilVendas(conn, autorId);

            // Buscar todas as vendas confirmadas (sem limite)
            List<VendaRecenteDTO> vendasRecentes = buscarVendasRecentes(conn, autorId);

            return new PainelPagamentosAutorDTO(resumo, funilVendas, vendasRecentes);

        } catch (Exception e) {
            log.error("[PAGAMENTOS AUTOR] Erro ao montar painel para autor {}: {}", autorId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Busca o nome do autor no banco do e-commerce.
     */
    private String buscarNomeAutor(Connection conn, long autorId) throws Exception {
        String sql = "SELECT name FROM authors WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, autorId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
            }
        }
        return null;
    }

    /**
     * Calcula o valor total de vendas confirmadas do autor.
     * Usa payment_payouts.amount_net com status = 'CONFIRMED' para valores reais (após taxas).
     */
    private double calcularValorVendasConfirmadas(Connection conn, long autorId) throws Exception {
        String sql = """
            SELECT COALESCE(SUM(pp.amount_net), 0) AS total_confirmado
            FROM payment_payouts pp
            JOIN orders o ON o.id = pp.order_id
            JOIN order_items oi ON oi.order_id = o.id
            JOIN books b ON b.id::text = oi.book_id
            WHERE b.author_id = ?
              AND pp.status = 'CONFIRMED'
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, autorId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal total = rs.getBigDecimal("total_confirmado");
                    return total != null ? total.doubleValue() : 0.0;
                }
            }
        }
        return 0.0;
    }

    /**
     * Calcula o valor já recebido pelo autor (payouts confirmados).
     * Usa payment_payouts.amount_net com status = 'CONFIRMED'.
     */
    private double calcularValorJaRecebido(Connection conn, long autorId) throws Exception {
        String sql = """
            SELECT COALESCE(SUM(pp.amount_net), 0) AS valor_recebido
            FROM payment_payouts pp
            JOIN orders o ON o.id = pp.order_id
            JOIN order_items oi ON oi.order_id = o.id
            JOIN books b ON b.id::text = oi.book_id
            WHERE b.author_id = ?
              AND pp.status = 'CONFIRMED'
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, autorId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal total = rs.getBigDecimal("valor_recebido");
                    return total != null ? total.doubleValue() : 0.0;
                }
            }
        }
        return 0.0;
    }

    /**
     * Calcula o valor a receber (payouts enviados mas não confirmados).
     * Usa payment_payouts.amount_net com status = 'SENT'.
     */
    private double calcularValorAReceber(Connection conn, long autorId) throws Exception {
        String sql = """
            SELECT COALESCE(SUM(pp.amount_net), 0) AS valor_a_receber
            FROM payment_payouts pp
            JOIN orders o ON o.id = pp.order_id
            JOIN order_items oi ON oi.order_id = o.id
            JOIN books b ON b.id::text = oi.book_id
            WHERE b.author_id = ?
              AND pp.status = 'SENT'
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, autorId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal total = rs.getBigDecimal("valor_a_receber");
                    return total != null ? total.doubleValue() : 0.0;
                }
            }
        }
        return 0.0;
    }

    /**
     * Busca todas as vendas confirmadas do autor.
     * Usa payment_payouts para obter valores reais (amount_net) e informações do pedido.
     * Retorna todas as vendas ordenadas por data de pagamento (mais recente primeiro).
     */
    private List<VendaRecenteDTO> buscarVendasRecentes(Connection conn, long autorId) throws Exception {
        List<VendaRecenteDTO> vendas = new ArrayList<>();

        String sql = """
            SELECT 
                pp.order_id AS pedido_id,
                o.paid_at AS data_pedido,
                COALESCE(b.title, 'N/A') AS titulo_livro,
                COALESCE(oi.quantity, 1) AS quantidade,
                pp.amount_net AS valor_total,
                pp.status AS status
            FROM payment_payouts pp
            JOIN orders o ON o.id = pp.order_id
            JOIN order_items oi ON oi.order_id = o.id
            JOIN books b ON b.id::text = oi.book_id
            WHERE b.author_id = ?
              AND pp.status = 'CONFIRMED'
            ORDER BY o.paid_at DESC NULLS LAST, pp.id DESC
            LIMIT 20
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, autorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OffsetDateTime dataPedido = rs.getTimestamp("data_pedido") != null
                            ? rs.getTimestamp("data_pedido").toInstant()
                                    .atOffset(java.time.ZoneOffset.UTC)
                            : null;

                    BigDecimal valorTotal = rs.getBigDecimal("valor_total");
                    double valor = valorTotal != null ? valorTotal.doubleValue() : 0.0;

                    String statusLegivel = mapearStatusPayout(rs.getString("status"));

                    Long pedidoId = rs.getObject("pedido_id", Long.class);
                    if (pedidoId != null) {
                        VendaRecenteDTO venda = new VendaRecenteDTO(
                                pedidoId,
                                dataPedido,
                                rs.getString("titulo_livro"),
                                rs.getInt("quantidade"),
                                valor,
                                statusLegivel
                        );
                        vendas.add(venda);
                    }
                }
            }
        }

        return vendas;
    }

    /**
     * Calcula o funil de vendas do autor.
     * Métricas simples e fáceis de entender para um escritor leigo.
     * Usa orders para contagem de pedidos e payment_payouts.amount_net para valores monetários reais.
     */
    private FunilVendasDTO calcularFunilVendas(Connection conn, long autorId) throws Exception {
        // Buscar contagem de pedidos por status (usa orders)
        String sqlPedidos = """
            SELECT 
                COUNT(DISTINCT o.id) AS total_pedidos,
                COUNT(DISTINCT CASE WHEN o.status = 'CONFIRMED' THEN o.id END) AS pedidos_confirmados,
                COUNT(DISTINCT CASE WHEN o.status IN ('NEW', 'WAITING', 'RESERVED') THEN o.id END) AS pedidos_em_andamento,
                COUNT(DISTINCT CASE WHEN o.status IN ('CANCELLED', 'CANCELED', 'EXPIRED', 'RESERVA_EXPIRADA') THEN o.id END) AS pedidos_cancelados
            FROM orders o
            JOIN order_items oi ON oi.order_id = o.id
            JOIN books b ON b.id::text = oi.book_id
            WHERE b.author_id = ?
            """;

        long totalPedidos = 0;
        long pedidosConfirmados = 0;
        long pedidosEmAndamento = 0;
        long pedidosCancelados = 0;

        try (PreparedStatement stmt = conn.prepareStatement(sqlPedidos)) {
            stmt.setLong(1, autorId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    totalPedidos = rs.getLong("total_pedidos");
                    pedidosConfirmados = rs.getLong("pedidos_confirmados");
                    pedidosEmAndamento = rs.getLong("pedidos_em_andamento");
                    pedidosCancelados = rs.getLong("pedidos_cancelados");
                }
            }
        }

        // Buscar valores reais usando payment_payouts.amount_net
        String sqlValores = """
            SELECT 
                COALESCE(SUM(CASE WHEN pp.status = 'CONFIRMED' THEN pp.amount_net ELSE 0 END), 0) AS valor_confirmado,
                COALESCE(SUM(CASE WHEN pp.status = 'SENT' THEN pp.amount_net ELSE 0 END), 0) AS valor_em_andamento,
                COALESCE(SUM(pp.amount_net), 0) AS valor_total
            FROM payment_payouts pp
            JOIN orders o ON o.id = pp.order_id
            JOIN order_items oi ON oi.order_id = o.id
            JOIN books b ON b.id::text = oi.book_id
            WHERE b.author_id = ?
            """;

        double valorTotal = 0.0;
        double valorConfirmado = 0.0;
        double valorEmAndamento = 0.0;

        try (PreparedStatement stmt = conn.prepareStatement(sqlValores)) {
            stmt.setLong(1, autorId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal valorTotalBD = rs.getBigDecimal("valor_total");
                    BigDecimal valorConfirmadoBD = rs.getBigDecimal("valor_confirmado");
                    BigDecimal valorEmAndamentoBD = rs.getBigDecimal("valor_em_andamento");
                    
                    valorTotal = valorTotalBD != null ? valorTotalBD.doubleValue() : 0.0;
                    valorConfirmado = valorConfirmadoBD != null ? valorConfirmadoBD.doubleValue() : 0.0;
                    valorEmAndamento = valorEmAndamentoBD != null ? valorEmAndamentoBD.doubleValue() : 0.0;
                }
            }
        }

        // Calcular taxa de conversão (porcentagem de pedidos confirmados)
        double taxaConversao = 0.0;
        if (totalPedidos > 0) {
            taxaConversao = (pedidosConfirmados * 100.0) / totalPedidos;
        }

        return new FunilVendasDTO(
                totalPedidos,
                pedidosConfirmados,
                pedidosEmAndamento,
                pedidosCancelados,
                Math.round(taxaConversao * 100.0) / 100.0, // Arredondar para 2 casas decimais
                valorTotal,
                valorConfirmado,
                valorEmAndamento
        );
    }

    /**
     * Mapeia o status de payout para texto legível em português.
     */
    private String mapearStatusPayout(String status) {
        if (status == null) {
            return "Em andamento";
        }
        return switch (status.toUpperCase()) {
            case "CONFIRMED" -> "Pago";
            case "SENT" -> "Enviado";
            case "PENDING" -> "Pendente";
            case "CANCELLED", "CANCELED" -> "Cancelado";
            default -> "Em andamento";
        };
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
            log.error("[PAGAMENTOS AUTOR] Erro ao conectar ao banco do e-commerce: {}", e.getMessage());
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

