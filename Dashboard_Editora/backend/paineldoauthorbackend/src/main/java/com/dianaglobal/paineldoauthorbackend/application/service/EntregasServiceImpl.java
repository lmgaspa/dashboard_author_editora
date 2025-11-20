package com.dianaglobal.paineldoauthorbackend.application.service;

import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.entregas.AtualizarStatusEnvioRequest;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.entregas.EntregaDTO;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.entregas.ItemEntregaDTO;
import com.dianaglobal.paineldoauthorbackend.application.port.out.OrderShippingRepositoryPort;
import com.dianaglobal.paineldoauthorbackend.domain.model.OrderShipping;
import com.dianaglobal.paineldoauthorbackend.domain.model.ShippingStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EntregasServiceImpl implements EntregasService {

    private final OrderShippingRepositoryPort shippingRepository;

    @Override
    public List<EntregaDTO> listarEntregas(
        Long authorId,
        String dbUrl,
        String dbUsername,
        String dbPassword
    ) {
        if (dbUrl == null || dbUrl.trim().isEmpty()) {
            log.warn("[ENTREGAS] URL do banco do e-commerce não configurada para autor {}", authorId);
            return List.of();
        }

        try (Connection conn = getEcommerceConnection(dbUrl, dbUsername, dbPassword)) {
            if (conn == null) {
                return List.of();
            }

            return buscarPedidosParaEnvio(conn, authorId);

        } catch (Exception e) {
            log.error("[ENTREGAS] Erro ao listar entregas para autor {}: {}", authorId, e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public EntregaDTO buscarEntrega(
        Long orderId,
        Long authorId,
        String dbUrl,
        String dbUsername,
        String dbPassword
    ) {
        if (dbUrl == null || dbUrl.trim().isEmpty()) {
            log.warn("[ENTREGAS] URL do banco do e-commerce não configurada para autor {}", authorId);
            return null;
        }

        try (Connection conn = getEcommerceConnection(dbUrl, dbUsername, dbPassword)) {
            if (conn == null) {
                return null;
            }

            return buscarPedidoParaEnvio(conn, orderId, authorId);

        } catch (Exception e) {
            log.error("[ENTREGAS] Erro ao buscar entrega {} para autor {}: {}", orderId, authorId, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public EntregaDTO atualizarStatusEnvio(
        Long orderId,
        String authorId,
        AtualizarStatusEnvioRequest request
    ) {
        try {
            // Validar status
            ShippingStatus status;
            try {
                status = ShippingStatus.valueOf(request.statusEnvio());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Status de envio inválido: " + request.statusEnvio());
            }

            // Buscar ou criar shipping
            Optional<OrderShipping> shippingOpt = shippingRepository.findByOrderIdAndAuthorId(orderId, authorId);
            
            OrderShipping shipping;
            if (shippingOpt.isPresent()) {
                shipping = shippingOpt.get();
                shipping.setEnviado(request.enviado());
                shipping.setStatusEnvio(status);
                shipping.setCodigoRastreamento(request.codigoRastreamento());
                shipping.setUpdatedAt(Instant.now());
                
                // Se foi marcado como enviado, atualizar enviadoAt
                if (request.enviado() && shipping.getEnviadoAt() == null) {
                    shipping.setEnviadoAt(Instant.now());
                }
            } else {
                shipping = OrderShipping.builder()
                        .id(java.util.UUID.randomUUID())
                        .orderId(orderId)
                        .authorId(authorId)
                        .enviado(request.enviado())
                        .statusEnvio(status)
                        .codigoRastreamento(request.codigoRastreamento())
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .enviadoAt(request.enviado() ? Instant.now() : null)
                        .build();
            }

            shipping = shippingRepository.save(shipping);

            // Retornar DTO atualizado (precisa buscar dados do pedido do e-commerce)
            // Por enquanto, retornamos null e o controller vai buscar novamente
            return null;

        } catch (Exception e) {
            log.error("[ENTREGAS] Erro ao atualizar status de envio: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao atualizar status de envio: " + e.getMessage(), e);
        }
    }

    /**
     * Busca todos os pedidos confirmados do autor com informações para envio.
     */
    private List<EntregaDTO> buscarPedidosParaEnvio(Connection conn, long authorId) throws Exception {
        List<EntregaDTO> entregas = new ArrayList<>();

        String sql = """
            SELECT DISTINCT
                o.id as pedido_id,
                o.first_name,
                o.last_name,
                o.email,
                o.phone,
                o.cpf,
                o.address,
                o.number,
                o.complement,
                o.district,
                o.city,
                o.state,
                o.cep,
                o.total,
                o.status,
                o.created_at as data_pedido
            FROM orders o
            JOIN order_items oi ON oi.order_id = o.id
            JOIN books b ON b.id::text = oi.book_id
            WHERE b.author_id = ?
              AND o.status = 'CONFIRMED'
            ORDER BY o.created_at DESC
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, authorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Long pedidoId = rs.getLong("pedido_id");
                    
                    // Buscar itens do pedido
                    List<ItemEntregaDTO> itens = buscarItensPedido(conn, pedidoId, authorId);
                    
                    // Buscar status de envio do banco do painel
                    Optional<OrderShipping> shippingOpt = shippingRepository.findByOrderIdAndAuthorId(
                        pedidoId, 
                        String.valueOf(authorId)
                    );

                    EntregaDTO entrega = mapearParaEntregaDTO(rs, itens, shippingOpt);
                    entregas.add(entrega);
                }
            }
        }

        return entregas;
    }

    /**
     * Busca um pedido específico para envio.
     * OTIMIZADO: Busca pedido e itens em uma única query.
     */
    private EntregaDTO buscarPedidoParaEnvio(Connection conn, long orderId, long authorId) throws Exception {
        // Query única que busca pedido + itens em uma única execução
        String sql = """
            WITH pedido_base AS (
                SELECT DISTINCT
                    o.id as pedido_id,
                    o.first_name,
                    o.last_name,
                    o.email,
                    o.phone,
                    o.cpf,
                    o.address,
                    o.number,
                    o.complement,
                    o.district,
                    o.city,
                    o.state,
                    o.cep,
                    o.total,
                    o.status,
                    o.created_at as data_pedido
                FROM orders o
                JOIN order_items oi ON oi.order_id = o.id
                JOIN books b ON b.id::text = oi.book_id
                WHERE o.id = ?
                  AND b.author_id = ?
                  AND o.status = 'CONFIRMED'
                LIMIT 1
            )
            SELECT 
                pb.*,
                oi.book_id,
                b.title as book_title,
                oi.quantity,
                oi.price
            FROM pedido_base pb
            JOIN order_items oi ON oi.order_id = pb.pedido_id
            JOIN books b ON b.id::text = oi.book_id
            WHERE b.author_id = ?
            ORDER BY oi.id
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql, 
                ResultSet.TYPE_SCROLL_INSENSITIVE, 
                ResultSet.CONCUR_READ_ONLY)) {
            stmt.setLong(1, orderId);
            stmt.setLong(2, authorId);
            stmt.setLong(3, authorId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                // Guardar dados do pedido da primeira linha
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String email = rs.getString("email");
                String phone = rs.getString("phone");
                String cpf = rs.getString("cpf");
                String address = rs.getString("address");
                String number = rs.getString("number");
                String complement = rs.getString("complement");
                String district = rs.getString("district");
                String city = rs.getString("city");
                String state = rs.getString("state");
                String cep = rs.getString("cep");
                java.math.BigDecimal total = rs.getBigDecimal("total");
                String status = rs.getString("status");
                java.sql.Timestamp dataPedidoTimestamp = rs.getTimestamp("data_pedido");
                
                // Coletar todos os itens (incluindo primeira linha)
                List<ItemEntregaDTO> itens = new ArrayList<>();
                do {
                    ItemEntregaDTO item = new ItemEntregaDTO(
                            rs.getString("book_id"),
                            rs.getString("book_title"),
                            rs.getInt("quantity"),
                            rs.getBigDecimal("price")
                    );
                    itens.add(item);
                } while (rs.next());

                // Buscar status de envio
                Optional<OrderShipping> shippingOpt = shippingRepository.findByOrderIdAndAuthorId(
                    orderId, 
                    String.valueOf(authorId)
                );

                // Criar um ResultSet mock para mapear (ou refatorar mapearParaEntregaDTO)
                // Por enquanto, vamos mapear diretamente
                return criarEntregaDTO(
                    orderId,
                    firstName, lastName, email, phone, cpf,
                    address, number, complement, district, city, state, cep,
                    total, status, dataPedidoTimestamp,
                    itens, shippingOpt
                );
            }
        }
    }
    
    /**
     * Helper para criar EntregaDTO sem precisar de ResultSet.
     */
    private EntregaDTO criarEntregaDTO(
        Long pedidoId,
        String firstName, String lastName, String email, String phone, String cpf,
        String address, String number, String complement, String district, String city, String state, String cep,
        java.math.BigDecimal total, String status, java.sql.Timestamp dataPedidoTimestamp,
        List<ItemEntregaDTO> itens,
        Optional<OrderShipping> shippingOpt
    ) {
        String nomeCompleto = firstName + " " + lastName;
        String enderecoCompleto = buildAddressString(address, number, complement, district, city, state, cep);

        // Status de envio (do banco do painel ou padrão)
        Boolean enviado = false;
        String statusEnvio = "AGUARDANDO";
        String codigoRastreamento = null;
        Instant enviadoAt = null;
        Instant updatedAt = Instant.now();

        if (shippingOpt.isPresent()) {
            OrderShipping shipping = shippingOpt.get();
            enviado = shipping.getEnviado();
            statusEnvio = shipping.getStatusEnvio().name();
            codigoRastreamento = shipping.getCodigoRastreamento();
            enviadoAt = shipping.getEnviadoAt();
            updatedAt = shipping.getUpdatedAt();
        }

        Instant dataPedido = dataPedidoTimestamp != null 
            ? dataPedidoTimestamp.toInstant() 
            : Instant.now();

        return new EntregaDTO(
                pedidoId,
                dataPedido,
                total,
                status,
                nomeCompleto,
                email,
                phone,
                cpf,
                address,
                number,
                complement,
                district,
                city,
                state,
                cep,
                enderecoCompleto,
                itens,
                enviado,
                statusEnvio,
                codigoRastreamento,
                enviadoAt,
                updatedAt
        );
    }

    /**
     * Busca itens de um pedido.
     */
    private List<ItemEntregaDTO> buscarItensPedido(Connection conn, long orderId, long authorId) throws Exception {
        List<ItemEntregaDTO> itens = new ArrayList<>();

        String sql = """
            SELECT 
                oi.book_id,
                b.title,
                oi.quantity,
                oi.price
            FROM order_items oi
            JOIN books b ON b.id::text = oi.book_id
            WHERE oi.order_id = ?
              AND b.author_id = ?
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            stmt.setLong(2, authorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ItemEntregaDTO item = new ItemEntregaDTO(
                            rs.getString("book_id"),
                            rs.getString("title"),
                            rs.getInt("quantity"),
                            rs.getBigDecimal("price")
                    );
                    itens.add(item);
                }
            }
        }

        return itens;
    }

    /**
     * Mapeia ResultSet para EntregaDTO.
     */
    private EntregaDTO mapearParaEntregaDTO(
        ResultSet rs,
        List<ItemEntregaDTO> itens,
        Optional<OrderShipping> shippingOpt
    ) throws Exception {
        Long pedidoId = rs.getLong("pedido_id");
        String nomeCompleto = rs.getString("first_name") + " " + rs.getString("last_name");
        
        // Montar endereço completo
        String enderecoCompleto = buildAddressString(
            rs.getString("address"),
            rs.getString("number"),
            rs.getString("complement"),
            rs.getString("district"),
            rs.getString("city"),
            rs.getString("state"),
            rs.getString("cep")
        );

        // Status de envio (do banco do painel ou padrão)
        Boolean enviado = false;
        String statusEnvio = "AGUARDANDO";
        String codigoRastreamento = null;
        Instant enviadoAt = null;
        Instant updatedAt = Instant.now();

        if (shippingOpt.isPresent()) {
            OrderShipping shipping = shippingOpt.get();
            enviado = shipping.getEnviado();
            statusEnvio = shipping.getStatusEnvio().name();
            codigoRastreamento = shipping.getCodigoRastreamento();
            enviadoAt = shipping.getEnviadoAt();
            updatedAt = shipping.getUpdatedAt();
        }

        java.sql.Timestamp dataPedidoTimestamp = rs.getTimestamp("data_pedido");
        Instant dataPedido = dataPedidoTimestamp != null 
            ? dataPedidoTimestamp.toInstant() 
            : Instant.now();

        return new EntregaDTO(
                pedidoId,
                dataPedido,
                rs.getBigDecimal("total"),
                rs.getString("status"),
                nomeCompleto,
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("cpf"),
                rs.getString("address"),
                rs.getString("number"),
                rs.getString("complement"),
                rs.getString("district"),
                rs.getString("city"),
                rs.getString("state"),
                rs.getString("cep"),
                enderecoCompleto,
                itens,
                enviado,
                statusEnvio,
                codigoRastreamento,
                enviadoAt,
                updatedAt
        );
    }

    /**
     * Monta string de endereço completo.
     */
    private String buildAddressString(
        String address, String number, String complement,
        String district, String city, String state, String cep
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append(address).append(", ");
        sb.append(number);
        if (complement != null && !complement.trim().isEmpty()) {
            sb.append(" - ").append(complement);
        }
        sb.append(" - ").append(district);
        sb.append(", ").append(city);
        sb.append(" - ").append(state);
        sb.append(" CEP: ").append(cep);
        return sb.toString();
    }

    /**
     * Estabelece conexão com o banco de dados do e-commerce.
     */
    private Connection getEcommerceConnection(String dbUrl, String dbUsername, String dbPassword) {
        try {
            String jdbcUrl = converterUrlParaJdbc(dbUrl);
            return DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword);
        } catch (Exception e) {
            log.error("[ENTREGAS] Erro ao conectar ao banco do e-commerce: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Converte URL do formato postgres:// para jdbc:postgresql://
     */
    private String converterUrlParaJdbc(String url) {
        if (url == null || url.trim().isEmpty()) {
            return url;
        }

        if (url.startsWith("jdbc:postgresql://")) {
            return url;
        }

        if (url.startsWith("postgres://")) {
            String semPrefixo = url.substring("postgres://".length());
            int atIndex = semPrefixo.indexOf('@');
            if (atIndex > 0) {
                String hostPortDb = semPrefixo.substring(atIndex + 1);
                return "jdbc:postgresql://" + hostPortDb;
            } else {
                return "jdbc:postgresql://" + semPrefixo;
            }
        }

        return url;
    }
}

