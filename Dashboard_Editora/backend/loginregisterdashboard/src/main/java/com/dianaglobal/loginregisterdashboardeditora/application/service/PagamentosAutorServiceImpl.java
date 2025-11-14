package com.dianaglobal.loginregisterdashboardeditora.application.service;

import com.dianaglobal.loginregisterdashboardeditora.adapter.in.dto.pagamentos.FunilVendasDTO;
import com.dianaglobal.loginregisterdashboardeditora.adapter.in.dto.pagamentos.PagamentosAutorResumoDTO;
import com.dianaglobal.loginregisterdashboardeditora.adapter.in.dto.pagamentos.PainelPagamentosAutorDTO;
import com.dianaglobal.loginregisterdashboardeditora.adapter.in.dto.pagamentos.VendaRecenteDTO;
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
 * Busca dados diretamente de orders/order_items/books com status CONFIRMED.
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

            // Calcular valor de vendas confirmadas
            double valorVendasConfirmadas = calcularValorVendasConfirmadas(conn, autorId);

            // Por enquanto, valorJaRecebido é 0.0 (placeholder para futuro)
            double valorJaRecebido = 0.0;

            // Calcular valor a receber
            double valorAReceber = Math.max(0.0, valorVendasConfirmadas - valorJaRecebido);

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

            // Buscar vendas recentes (últimas 20 vendas confirmadas)
            List<VendaRecenteDTO> vendasRecentes = buscarVendasRecentes(conn, autorId, 20);

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
     * Soma: order_items.quantity * order_items.price para livros do autor,
     * apenas com orders.status = 'CONFIRMED'.
     */
    private double calcularValorVendasConfirmadas(Connection conn, long autorId) throws Exception {
        String sql = """
            SELECT COALESCE(SUM(oi.quantity * oi.price), 0) AS total_confirmado
            FROM order_items oi
            JOIN books b ON b.id::text = oi.book_id
            JOIN orders o ON o.id = oi.order_id
            WHERE b.author_id = ?
              AND o.status = 'CONFIRMED'
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
     * Busca as últimas vendas confirmadas do autor.
     * Retorna as últimas N vendas ordenadas por data de criação do pedido (mais recente primeiro).
     */
    private List<VendaRecenteDTO> buscarVendasRecentes(Connection conn, long autorId, int limite) throws Exception {
        List<VendaRecenteDTO> vendas = new ArrayList<>();

        String sql = """
            SELECT 
                o.id AS pedido_id,
                o.created_at AS data_pedido,
                b.title AS titulo_livro,
                oi.quantity AS quantidade,
                (oi.quantity * oi.price) AS valor_total,
                o.status AS status
            FROM orders o
            JOIN order_items oi ON oi.order_id = o.id
            JOIN books b ON b.id::text = oi.book_id
            WHERE b.author_id = ?
              AND o.status = 'CONFIRMED'
            ORDER BY o.created_at DESC
            LIMIT ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, autorId);
            stmt.setInt(2, limite);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OffsetDateTime dataPedido = rs.getTimestamp("data_pedido") != null
                            ? rs.getTimestamp("data_pedido").toInstant()
                                    .atOffset(java.time.ZoneOffset.UTC)
                            : null;

                    BigDecimal valorTotal = rs.getBigDecimal("valor_total");
                    double valor = valorTotal != null ? valorTotal.doubleValue() : 0.0;

                    String statusLegivel = mapearStatusLegivel(rs.getString("status"));

                    VendaRecenteDTO venda = new VendaRecenteDTO(
                            rs.getLong("pedido_id"),
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

        return vendas;
    }

    /**
     * Calcula o funil de vendas do autor.
     * Métricas simples e fáceis de entender para um escritor leigo.
     */
    private FunilVendasDTO calcularFunilVendas(Connection conn, long autorId) throws Exception {
        // Buscar totais por status
        String sql = """
            SELECT 
                COUNT(DISTINCT o.id) AS total_pedidos,
                COUNT(DISTINCT CASE WHEN o.status = 'CONFIRMED' THEN o.id END) AS pedidos_confirmados,
                COUNT(DISTINCT CASE WHEN o.status IN ('NEW', 'WAITING') THEN o.id END) AS pedidos_em_andamento,
                COUNT(DISTINCT CASE WHEN o.status IN ('CANCELLED', 'CANCELED', 'EXPIRED') THEN o.id END) AS pedidos_cancelados,
                COALESCE(SUM(CASE WHEN o.status = 'CONFIRMED' THEN oi.quantity * oi.price ELSE 0 END), 0) AS valor_confirmado,
                COALESCE(SUM(CASE WHEN o.status IN ('NEW', 'WAITING') THEN oi.quantity * oi.price ELSE 0 END), 0) AS valor_em_andamento,
                COALESCE(SUM(oi.quantity * oi.price), 0) AS valor_total
            FROM orders o
            JOIN order_items oi ON oi.order_id = o.id
            JOIN books b ON b.id::text = oi.book_id
            WHERE b.author_id = ?
            """;

        long totalPedidos = 0;
        long pedidosConfirmados = 0;
        long pedidosEmAndamento = 0;
        long pedidosCancelados = 0;
        double valorTotal = 0.0;
        double valorConfirmado = 0.0;
        double valorEmAndamento = 0.0;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, autorId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    totalPedidos = rs.getLong("total_pedidos");
                    pedidosConfirmados = rs.getLong("pedidos_confirmados");
                    pedidosEmAndamento = rs.getLong("pedidos_em_andamento");
                    pedidosCancelados = rs.getLong("pedidos_cancelados");
                    
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
     * Mapeia o status técnico para texto legível em português.
     */
    private String mapearStatusLegivel(String status) {
        if (status == null) {
            return "Em andamento";
        }
        return switch (status.toUpperCase()) {
            case "CONFIRMED" -> "Pago";
            case "NEW", "WAITING" -> "Em andamento";
            case "CANCELLED", "CANCELED" -> "Cancelado";
            case "EXPIRED" -> "Expirado";
            default -> "Em andamento";
        };
    }

    /**
     * Estabelece conexão com o banco de dados do e-commerce.
     */
    private Connection getEcommerceConnection(String dbUrl, String dbUsername, String dbPassword) {
        try {
            return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
        } catch (Exception e) {
            log.error("[PAGAMENTOS AUTOR] Erro ao conectar ao banco do e-commerce: {}", e.getMessage());
            return null;
        }
    }
}

