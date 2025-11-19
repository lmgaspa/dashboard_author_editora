# 📋 Guia de Dados de Clientes e Pedidos - Arquitetura OCP

## 🎯 Visão Geral

Este documento apresenta uma **arquitetura extensível e manutenível** para acessar e utilizar dados de clientes e pedidos, seguindo o **Princípio Aberto/Fechado (OCP)**. O sistema permite **adicionar novas funcionalidades sem modificar código existente**.

**Importante:** No sistema atual, **não existe uma tabela separada de clientes**. Todas as informações do cliente estão armazenadas diretamente na tabela `orders` (pedidos).

---

## 🏗️ Arquitetura Proposta

### Princípios de Design

1. **Open/Closed Principle (OCP)**: Classes abertas para extensão, fechadas para modificação
2. **Dependency Inversion**: Depender de abstrações, não de implementações concretas
3. **Single Responsibility**: Cada classe tem uma única responsabilidade
4. **Strategy Pattern**: Algoritmos intercambiáveis (queries, formatação, validação)
5. **Factory Pattern**: Criação de objetos complexos

### Estrutura de Camadas

```
┌─────────────────────────────────────────┐
│         Controllers (REST API)          │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│      Services (Use Cases)               │
│  - OrderDashboardService                │
│  - CustomerStatsService                 │
│  - CouponAnalyticsService               │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│   Query Strategies (Interfaces)         │
│  - OrderQueryStrategy                   │
│  - CustomerQueryStrategy                │
│  - CouponQueryStrategy                  │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│   Query Implementations                 │
│  - OrderQueryStrategyImpl               │
│  - CustomerQueryStrategyImpl            │
│  - CouponQueryStrategyImpl              │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│   Data Access Layer                     │
│  - EcommerceConnectionProvider           │
│  - QueryExecutor                         │
└──────────────────────────────────────────┘
```

---

## 🗄️ Estrutura de Dados

### Tabela: `orders`

A tabela `orders` contém **todas as informações do cliente e do pedido**:

#### **Dados do Cliente:**
- `id` → Número do pedido (PK)
- `first_name` → Primeiro nome
- `last_name` → Sobrenome
- `email` → Email
- `phone` → Telefone/WhatsApp
- `cpf` → CPF

#### **Endereço:**
- `address` → Rua/Logradouro
- `number` → Número
- `complement` → Complemento (nullable)
- `district` → Bairro
- `city` → Cidade
- `state` → Estado (UF)
- `cep` → CEP

#### **Informações do Pedido:**
- `total` → Valor total (com desconto aplicado)
- `status` → Status (`NEW`, `WAITING`, `CONFIRMED`, etc.)
- `payment_method` → Método (`pix` ou `card`)
- `created_at` → Data/hora do pedido
- `txid` → ID da transação (PIX)
- `charge_id` → ID da cobrança (Cartão)
- `paid` → Boolean (foi pago?)
- `paid_at` → Data/hora do pagamento

#### **Cupom:**
- `coupon_code` → Código do cupom
- `discount_amount` → Valor do desconto

#### **Tabelas Relacionadas:**
- `order_items` → Itens do pedido
- `order_coupons` → Detalhes do cupom
- `coupons` → Informações do cupom
- `payment_payouts` → Repasses (valor líquido após taxas)
- `payout_email` → E-mails de repasse enviados

---

## 🔧 Implementação Backend (Java/Spring Boot)

### 1. Interfaces de Query (Strategy Pattern)

#### `OrderQueryStrategy.java`

```java
package com.dianaglobal.paineldoauthorbackend.application.service.order.query;

import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.order.OrderWithCustomerDTO;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Strategy para queries de pedidos.
 * Permite diferentes implementações (JDBC, JPA, etc.) sem modificar código existente.
 */
public interface OrderQueryStrategy {
    
    /**
     * Busca um pedido específico com todas as informações do cliente.
     */
    Optional<OrderWithCustomerDTO> findOrderById(Connection conn, Long orderId, Long authorId);
    
    /**
     * Lista pedidos com filtros opcionais.
     */
    List<OrderWithCustomerDTO> findOrders(
        Connection conn, 
        Long authorId, 
        OrderFilter filter
    );
    
    /**
     * Conta total de pedidos com filtros.
     */
    long countOrders(Connection conn, Long authorId, OrderFilter filter);
}

/**
 * Filtros para queries de pedidos.
 * Extensível sem modificar a interface.
 */
public record OrderFilter(
    String status,
    String email,
    String phone,
    String cpf,
    String couponCode,
    Boolean paid,
    Integer limit,
    Integer offset
) {
    public static OrderFilter empty() {
        return new OrderFilter(null, null, null, null, null, null, null, null);
    }
    
    public OrderFilter withStatus(String status) {
        return new OrderFilter(status, email, phone, cpf, couponCode, paid, limit, offset);
    }
    
    public OrderFilter withEmail(String email) {
        return new OrderFilter(status, email, phone, cpf, couponCode, paid, limit, offset);
    }
    
    // Builder pattern para facilitar construção
    public static class Builder {
        private String status;
        private String email;
        private String phone;
        private String cpf;
        private String couponCode;
        private Boolean paid;
        private Integer limit;
        private Integer offset;
        
        public Builder status(String status) {
            this.status = status;
            return this;
        }
        
        public Builder email(String email) {
            this.email = email;
            return this;
        }
        
        // ... outros métodos
        
        public OrderFilter build() {
            return new OrderFilter(status, email, phone, cpf, couponCode, paid, limit, offset);
        }
    }
}
```

#### `CustomerQueryStrategy.java`

```java
package com.dianaglobal.paineldoauthorbackend.application.service.customer.query;

import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.customer.CustomerDTO;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.customer.CustomerStatsDTO;
import java.sql.Connection;
import java.util.List;

/**
 * Strategy para queries de clientes.
 * Permite diferentes implementações sem modificar código existente.
 */
public interface CustomerQueryStrategy {
    
    /**
     * Busca clientes únicos por critério (email, phone, cpf).
     */
    List<CustomerDTO> findUniqueCustomers(
        Connection conn, 
        Long authorId, 
        CustomerFilter filter
    );
    
    /**
     * Busca estatísticas de clientes.
     */
    CustomerStatsDTO getCustomerStats(Connection conn, Long authorId);
    
    /**
     * Busca histórico de pedidos de um cliente.
     */
    List<OrderWithCustomerDTO> findCustomerOrderHistory(
        Connection conn, 
        Long authorId, 
        String customerIdentifier, 
        CustomerIdentifierType type
    );
}

public enum CustomerIdentifierType {
    EMAIL, PHONE, CPF
}

public record CustomerFilter(
    CustomerIdentifierType identifierType,
    String identifierValue
) {}
```

#### `CouponQueryStrategy.java`

```java
package com.dianaglobal.paineldoauthorbackend.application.service.coupon.query;

import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.coupon.CouponStatsDTO;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.coupon.CouponUsageDTO;
import java.sql.Connection;
import java.util.List;

/**
 * Strategy para queries de cupons.
 * Permite diferentes implementações sem modificar código existente.
 */
public interface CouponQueryStrategy {
    
    /**
     * Busca estatísticas de uso de cupons.
     */
    CouponStatsDTO getCouponStats(Connection conn, Long authorId);
    
    /**
     * Lista cupons mais utilizados.
     */
    List<CouponUsageDTO> getMostUsedCoupons(
        Connection conn, 
        Long authorId, 
        int limit
    );
    
    /**
     * Busca detalhes de um cupom específico.
     */
    Optional<CouponDetailsDTO> getCouponDetails(
        Connection conn, 
        Long authorId, 
        String couponCode
    );
}
```

### 2. Implementações das Strategies

#### `OrderQueryStrategyImpl.java`

```java
package com.dianaglobal.paineldoauthorbackend.application.service.order.query.impl;

import com.dianaglobal.paineldoauthorbackend.application.service.order.query.OrderQueryStrategy;
import com.dianaglobal.paineldoauthorbackend.application.service.order.query.OrderFilter;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.order.OrderWithCustomerDTO;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.order.CustomerDTO;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.order.AddressDTO;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.order.OrderDetailsDTO;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.order.CouponInfoDTO;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.order.OrderItemDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.*;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderQueryStrategyImpl implements OrderQueryStrategy {
    
    private final OrderDTOMapper dtoMapper;
    private final QueryBuilder queryBuilder;
    
    @Override
    public Optional<OrderWithCustomerDTO> findOrderById(
        Connection conn, 
        Long orderId, 
        Long authorId
    ) {
        String sql = queryBuilder.buildOrderByIdQuery();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            stmt.setLong(2, authorId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(dtoMapper.mapToOrderWithCustomer(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error finding order {}: {}", orderId, e.getMessage(), e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public List<OrderWithCustomerDTO> findOrders(
        Connection conn, 
        Long authorId, 
        OrderFilter filter
    ) {
        QueryBuilder.QueryResult queryResult = queryBuilder.buildOrderListQuery(filter);
        String sql = queryResult.sql();
        List<OrderWithCustomerDTO> orders = new ArrayList<>();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            stmt.setLong(paramIndex++, authorId);
            
            // Setar parâmetros dinâmicos baseados no filter
            for (Object param : queryResult.parameters()) {
                stmt.setObject(paramIndex++, param);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(dtoMapper.mapToOrderWithCustomer(rs));
                }
            }
        } catch (SQLException e) {
            log.error("Error finding orders: {}", e.getMessage(), e);
        }
        
        return orders;
    }
    
    @Override
    public long countOrders(Connection conn, Long authorId, OrderFilter filter) {
        QueryBuilder.QueryResult queryResult = queryBuilder.buildOrderCountQuery(filter);
        String sql = queryResult.sql();
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            stmt.setLong(paramIndex++, authorId);
            
            for (Object param : queryResult.parameters()) {
                stmt.setObject(paramIndex++, param);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            log.error("Error counting orders: {}", e.getMessage(), e);
        }
        
        return 0;
    }
}
```

#### `QueryBuilder.java` (Builder Pattern para SQL)

```java
package com.dianaglobal.paineldoauthorbackend.application.service.order.query.impl;

import com.dianaglobal.paineldoauthorbackend.application.service.order.query.OrderFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder para construção de queries SQL dinâmicas.
 * Facilita extensão de filtros sem modificar código existente.
 */
@Component
public class QueryBuilder {
    
    public record QueryResult(String sql, List<Object> parameters) {}
    
    public QueryResult buildOrderByIdQuery() {
        String sql = """
            SELECT 
                o.id,
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
                o.payment_method,
                o.created_at,
                o.paid,
                o.paid_at,
                o.coupon_code,
                o.discount_amount,
                o.txid,
                o.charge_id
            FROM orders o
            JOIN order_items oi ON oi.order_id = o.id
            JOIN books b ON b.id::text = oi.book_id
            WHERE o.id = ? AND b.author_id = ?
            LIMIT 1
            """;
        
        return new QueryResult(sql, List.of());
    }
    
    public QueryResult buildOrderListQuery(OrderFilter filter) {
        StringBuilder sql = new StringBuilder("""
            SELECT DISTINCT
                o.id,
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
                o.payment_method,
                o.created_at,
                o.paid,
                o.paid_at,
                o.coupon_code,
                o.discount_amount
            FROM orders o
            JOIN order_items oi ON oi.order_id = o.id
            JOIN books b ON b.id::text = oi.book_id
            WHERE b.author_id = ?
            """);
        
        List<Object> parameters = new ArrayList<>();
        
        // Adicionar filtros dinamicamente (OCP: extensível sem modificar)
        if (filter.status() != null) {
            sql.append(" AND o.status = ?");
            parameters.add(filter.status());
        }
        
        if (filter.email() != null) {
            sql.append(" AND o.email = ?");
            parameters.add(filter.email());
        }
        
        if (filter.phone() != null) {
            sql.append(" AND o.phone = ?");
            parameters.add(filter.phone());
        }
        
        if (filter.cpf() != null) {
            sql.append(" AND o.cpf = ?");
            parameters.add(filter.cpf());
        }
        
        if (filter.couponCode() != null) {
            sql.append(" AND o.coupon_code = ?");
            parameters.add(filter.couponCode());
        }
        
        if (filter.paid() != null) {
            sql.append(" AND o.paid = ?");
            parameters.add(filter.paid());
        }
        
        sql.append(" ORDER BY o.created_at DESC");
        
        if (filter.limit() != null) {
            sql.append(" LIMIT ?");
            parameters.add(filter.limit());
        }
        
        if (filter.offset() != null) {
            sql.append(" OFFSET ?");
            parameters.add(filter.offset());
        }
        
        return new QueryResult(sql.toString(), parameters);
    }
    
    public QueryResult buildOrderCountQuery(OrderFilter filter) {
        StringBuilder sql = new StringBuilder("""
            SELECT COUNT(DISTINCT o.id)
            FROM orders o
            JOIN order_items oi ON oi.order_id = o.id
            JOIN books b ON b.id::text = oi.book_id
            WHERE b.author_id = ?
            """);
        
        List<Object> parameters = new ArrayList<>();
        
        // Mesmos filtros do buildOrderListQuery
        if (filter.status() != null) {
            sql.append(" AND o.status = ?");
            parameters.add(filter.status());
        }
        
        // ... outros filtros
        
        return new QueryResult(sql.toString(), parameters);
    }
}
```

#### `OrderDTOMapper.java` (Mapper Pattern)

```java
package com.dianaglobal.paineldoauthorbackend.application.service.order.query.impl;

import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.order.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapper para converter ResultSet em DTOs.
 * Separa lógica de mapeamento, facilitando manutenção e testes.
 */
@Component
public class OrderDTOMapper {
    
    public OrderWithCustomerDTO mapToOrderWithCustomer(ResultSet rs) throws SQLException {
        return new OrderWithCustomerDTO(
            rs.getLong("id"),
            rs.getLong("id"), // numeroPedido
            mapCustomer(rs),
            mapAddress(rs),
            mapOrderDetails(rs),
            mapCouponInfo(rs),
            mapOrderItems(rs) // Será buscado em query separada se necessário
        );
    }
    
    private CustomerDTO mapCustomer(ResultSet rs) throws SQLException {
        return new CustomerDTO(
            rs.getString("first_name") + " " + rs.getString("last_name"),
            rs.getString("email"),
            rs.getString("phone"),
            rs.getString("cpf")
        );
    }
    
    private AddressDTO mapAddress(ResultSet rs) throws SQLException {
        String complement = rs.getString("complement");
        String enderecoCompleto = buildAddressString(
            rs.getString("address"),
            rs.getString("number"),
            complement,
            rs.getString("district"),
            rs.getString("city"),
            rs.getString("state"),
            rs.getString("cep")
        );
        
        return new AddressDTO(
            rs.getString("address"),
            rs.getString("number"),
            complement,
            rs.getString("district"),
            rs.getString("city"),
            rs.getString("state"),
            rs.getString("cep"),
            enderecoCompleto
        );
    }
    
    private OrderDetailsDTO mapOrderDetails(ResultSet rs) throws SQLException {
        Timestamp paidAtTimestamp = rs.getTimestamp("paid_at");
        OffsetDateTime paidAt = paidAtTimestamp != null
            ? paidAtTimestamp.toInstant().atOffset(java.time.ZoneOffset.UTC)
            : null;
        
        Timestamp createdAtTimestamp = rs.getTimestamp("created_at");
        OffsetDateTime createdAt = createdAtTimestamp != null
            ? createdAtTimestamp.toInstant().atOffset(java.time.ZoneOffset.UTC)
            : null;
        
        return new OrderDetailsDTO(
            rs.getBigDecimal("total"),
            rs.getString("status"),
            rs.getString("payment_method"),
            createdAt,
            rs.getBoolean("paid"),
            paidAt
        );
    }
    
    private CouponInfoDTO mapCouponInfo(ResultSet rs) throws SQLException {
        String couponCode = rs.getString("coupon_code");
        if (couponCode == null) {
            return null;
        }
        
        return new CouponInfoDTO(
            couponCode,
            rs.getBigDecimal("discount_amount") != null 
                ? rs.getBigDecimal("discount_amount") 
                : BigDecimal.ZERO,
            null, // valorOriginal será buscado em query separada se necessário
            null  // nomeCupom será buscado em query separada se necessário
        );
    }
    
    private List<OrderItemDTO> mapOrderItems(ResultSet rs) throws SQLException {
        // Se order_items não vierem no ResultSet, retornar lista vazia
        // Será buscado em query separada se necessário
        return new ArrayList<>();
    }
    
    private String buildAddressString(
        String address, String number, String complement,
        String district, String city, String state, String cep
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append(address).append(", ");
        sb.append(number);
        if (complement != null && !complement.isEmpty()) {
            sb.append(" - ").append(complement);
        }
        sb.append(" - ").append(district);
        sb.append(", ").append(city);
        sb.append(" - ").append(state);
        sb.append(" CEP: ").append(cep);
        return sb.toString();
    }
}
```

### 3. Connection Provider (Abstração)

#### `EcommerceConnectionProvider.java`

```java
package com.dianaglobal.paineldoauthorbackend.application.service.connection;

import java.sql.Connection;

/**
 * Abstração para obtenção de conexões com o banco do e-commerce.
 * Permite diferentes implementações (pool, single connection, etc.).
 */
public interface EcommerceConnectionProvider {
    
    /**
     * Obtém uma conexão com o banco do e-commerce do autor.
     * @return Connection ou null se não conseguir conectar
     */
    Connection getConnection(Long authorId);
    
    /**
     * Obtém uma conexão usando credenciais específicas.
     */
    Connection getConnection(String dbUrl, String dbUsername, String dbPassword);
}
```

#### `EcommerceConnectionProviderImpl.java`

```java
package com.dianaglobal.paineldoauthorbackend.application.service.connection.impl;

import com.dianaglobal.paineldoauthorbackend.application.service.connection.EcommerceConnectionProvider;
import com.dianaglobal.paineldoauthorbackend.application.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;

@Slf4j
@Component
@RequiredArgsConstructor
public class EcommerceConnectionProviderImpl implements EcommerceConnectionProvider {
    
    private final UserService userService;
    private final UrlConverter urlConverter;
    
    @Override
    public Connection getConnection(Long authorId) {
        var user = userService.findByAuthorId(String.valueOf(authorId));
        if (user == null || user.getEcommerceDbUrl() == null) {
            log.warn("E-commerce database not configured for author {}", authorId);
            return null;
        }
        
        return getConnection(
            user.getEcommerceDbUrl(),
            user.getEcommerceDbUsername(),
            user.getEcommerceDbPassword()
        );
    }
    
    @Override
    public Connection getConnection(String dbUrl, String dbUsername, String dbPassword) {
        try {
            String jdbcUrl = urlConverter.convertToJdbc(dbUrl);
            return DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword);
        } catch (Exception e) {
            log.error("Error connecting to e-commerce database: {}", e.getMessage(), e);
            return null;
        }
    }
}
```

### 4. Services (Use Cases)

#### `OrderDashboardService.java`

```java
package com.dianaglobal.paineldoauthorbackend.application.service.order;

import com.dianaglobal.paineldoauthorbackend.application.service.connection.EcommerceConnectionProvider;
import com.dianaglobal.paineldoauthorbackend.application.service.order.query.OrderQueryStrategy;
import com.dianaglobal.paineldoauthorbackend.application.service.order.query.OrderFilter;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.order.OrderWithCustomerDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Service para operações de dashboard de pedidos.
 * Usa Strategy Pattern para queries, permitindo extensão sem modificação.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderDashboardService {
    
    private final EcommerceConnectionProvider connectionProvider;
    private final OrderQueryStrategy orderQueryStrategy;
    
    public Optional<OrderWithCustomerDTO> getOrder(Long orderId, Long authorId) {
        try (Connection conn = connectionProvider.getConnection(authorId)) {
            if (conn == null) {
                return Optional.empty();
            }
            return orderQueryStrategy.findOrderById(conn, orderId, authorId);
        } catch (Exception e) {
            log.error("Error getting order {}: {}", orderId, e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    public List<OrderWithCustomerDTO> listOrders(Long authorId, OrderFilter filter) {
        try (Connection conn = connectionProvider.getConnection(authorId)) {
            if (conn == null) {
                return List.of();
            }
            return orderQueryStrategy.findOrders(conn, authorId, filter);
        } catch (Exception e) {
            log.error("Error listing orders: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    public long countOrders(Long authorId, OrderFilter filter) {
        try (Connection conn = connectionProvider.getConnection(authorId)) {
            if (conn == null) {
                return 0;
            }
            return orderQueryStrategy.countOrders(conn, authorId, filter);
        } catch (Exception e) {
            log.error("Error counting orders: {}", e.getMessage(), e);
            return 0;
        }
    }
}
```

#### `CustomerStatsService.java`

```java
package com.dianaglobal.paineldoauthorbackend.application.service.customer;

import com.dianaglobal.paineldoauthorbackend.application.service.connection.EcommerceConnectionProvider;
import com.dianaglobal.paineldoauthorbackend.application.service.customer.query.CustomerQueryStrategy;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.customer.CustomerStatsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Connection;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerStatsService {
    
    private final EcommerceConnectionProvider connectionProvider;
    private final CustomerQueryStrategy customerQueryStrategy;
    
    public CustomerStatsDTO getCustomerStats(Long authorId) {
        try (Connection conn = connectionProvider.getConnection(authorId)) {
            if (conn == null) {
                return CustomerStatsDTO.empty();
            }
            return customerQueryStrategy.getCustomerStats(conn, authorId);
        } catch (Exception e) {
            log.error("Error getting customer stats: {}", e.getMessage(), e);
            return CustomerStatsDTO.empty();
        }
    }
}
```

### 5. DTOs (Data Transfer Objects)

#### `OrderWithCustomerDTO.java`

```java
package com.dianaglobal.paineldoauthorbackend.adapter.in.dto.order;

import java.util.List;

/**
 * DTO para pedido com informações completas do cliente.
 */
public record OrderWithCustomerDTO(
    Long id,
    Long numeroPedido,
    CustomerDTO cliente,
    AddressDTO endereco,
    OrderDetailsDTO pedido,
    CouponInfoDTO cupom,
    List<OrderItemDTO> items
) {}
```

#### `CustomerDTO.java`

```java
package com.dianaglobal.paineldoauthorbackend.adapter.in.dto.order;

public record CustomerDTO(
    String nomeCompleto,
    String email,
    String whatsapp,
    String cpf
) {}
```

#### `AddressDTO.java`

```java
package com.dianaglobal.paineldoauthorbackend.adapter.in.dto.order;

public record AddressDTO(
    String rua,
    String numero,
    String complemento,
    String bairro,
    String cidade,
    String estado,
    String cep,
    String enderecoCompleto
) {}
```

#### `OrderDetailsDTO.java`

```java
package com.dianaglobal.paineldoauthorbackend.adapter.in.dto.order;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record OrderDetailsDTO(
    BigDecimal valorTotal,
    String status,
    String metodoPagamento,
    OffsetDateTime dataPedido,
    Boolean pago,
    OffsetDateTime dataPagamento
) {}
```

#### `CouponInfoDTO.java`

```java
package com.dianaglobal.paineldoauthorbackend.adapter.in.dto.order;

import java.math.BigDecimal;

public record CouponInfoDTO(
    String codigo,
    BigDecimal descontoAplicado,
    BigDecimal valorOriginal,
    String nomeCupom
) {}
```

#### `OrderItemDTO.java`

```java
package com.dianaglobal.paineldoauthorbackend.adapter.in.dto.order;

import java.math.BigDecimal;

public record OrderItemDTO(
    String bookId,
    String titulo,
    Integer quantidade,
    BigDecimal preco
) {}
```

### 6. Controllers (REST API)

#### `OrderDashboardController.java`

```java
package com.dianaglobal.paineldoauthorbackend.adapter.in.web;

import com.dianaglobal.paineldoauthorbackend.application.service.order.OrderDashboardService;
import com.dianaglobal.paineldoauthorbackend.application.service.order.query.OrderFilter;
import com.dianaglobal.paineldoauthorbackend.adapter.in.dto.order.OrderWithCustomerDTO;
import com.dianaglobal.paineldoauthorbackend.application.service.CurrentAuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard/orders")
@RequiredArgsConstructor
public class OrderDashboardController {
    
    private final OrderDashboardService orderDashboardService;
    private final CurrentAuthorService currentAuthorService;
    
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderWithCustomerDTO> getOrder(
        @PathVariable Long orderId,
        Authentication auth
    ) {
        Long authorId = currentAuthorService.getCurrentAuthorId(auth);
        if (authorId == null) {
            return ResponseEntity.forbidden().build();
        }
        
        return orderDashboardService.getOrder(orderId, authorId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public ResponseEntity<List<OrderWithCustomerDTO>> listOrders(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String email,
        @RequestParam(required = false) String phone,
        @RequestParam(required = false) String cpf,
        @RequestParam(required = false) String couponCode,
        @RequestParam(required = false) Boolean paid,
        @RequestParam(required = false, defaultValue = "50") Integer limit,
        @RequestParam(required = false, defaultValue = "0") Integer offset,
        Authentication auth
    ) {
        Long authorId = currentAuthorService.getCurrentAuthorId(auth);
        if (authorId == null) {
            return ResponseEntity.forbidden().build();
        }
        
        OrderFilter filter = new OrderFilter.Builder()
            .status(status)
            .email(email)
            .phone(phone)
            .cpf(cpf)
            .couponCode(couponCode)
            .paid(paid)
            .limit(limit)
            .offset(offset)
            .build();
        
        List<OrderWithCustomerDTO> orders = orderDashboardService.listOrders(authorId, filter);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/count")
    public ResponseEntity<Long> countOrders(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String email,
        Authentication auth
    ) {
        Long authorId = currentAuthorService.getCurrentAuthorId(auth);
        if (authorId == null) {
            return ResponseEntity.forbidden().build();
        }
        
        OrderFilter filter = new OrderFilter.Builder()
            .status(status)
            .email(email)
            .build();
        
        long count = orderDashboardService.countOrders(authorId, filter);
        return ResponseEntity.ok(count);
    }
}
```

---

## 🎨 Frontend (React/TypeScript)

### 1. Types/Interfaces

```typescript
// types/order.ts

export interface OrderWithCustomer {
  id: number;
  numeroPedido: number;
  cliente: Customer;
  endereco: Address;
  pedido: OrderDetails;
  cupom?: CouponInfo | null;
  items: OrderItem[];
}

export interface Customer {
  nomeCompleto: string;
  email: string;
  whatsapp: string;
  cpf: string;
}

export interface Address {
  rua: string;
  numero: string;
  complemento?: string | null;
  bairro: string;
  cidade: string;
  estado: string;
  cep: string;
  enderecoCompleto: string;
}

export interface OrderDetails {
  valorTotal: number;
  status: string;
  metodoPagamento: string;
  dataPedido: string; // ISO 8601
  pago: boolean;
  dataPagamento?: string | null;
}

export interface CouponInfo {
  codigo: string;
  descontoAplicado: number;
  valorOriginal?: number | null;
  nomeCupom?: string | null;
}

export interface OrderItem {
  bookId: string;
  titulo: string;
  quantidade: number;
  preco: number;
}

export interface OrderFilter {
  status?: string;
  email?: string;
  phone?: string;
  cpf?: string;
  couponCode?: string;
  paid?: boolean;
  limit?: number;
  offset?: number;
}
```

### 2. API Service (Strategy Pattern)

```typescript
// services/orderDashboardApi.ts

import axios, { AxiosInstance } from 'axios';
import { OrderWithCustomer, OrderFilter } from '../types/order';

/**
 * Interface para diferentes implementações de API.
 * Permite mock, diferentes ambientes, etc.
 */
export interface OrderApiStrategy {
  getOrder(orderId: number): Promise<OrderWithCustomer>;
  listOrders(filter: OrderFilter): Promise<OrderWithCustomer[]>;
  countOrders(filter: OrderFilter): Promise<number>;
}

/**
 * Implementação real usando Axios.
 */
export class AxiosOrderApiStrategy implements OrderApiStrategy {
  constructor(private axiosInstance: AxiosInstance) {}
  
  async getOrder(orderId: number): Promise<OrderWithCustomer> {
    const response = await this.axiosInstance.get<OrderWithCustomer>(
      `/api/v1/dashboard/orders/${orderId}`
    );
    return response.data;
  }
  
  async listOrders(filter: OrderFilter): Promise<OrderWithCustomer[]> {
    const response = await this.axiosInstance.get<OrderWithCustomer[]>(
      '/api/v1/dashboard/orders',
      { params: filter }
    );
    return response.data;
  }
  
  async countOrders(filter: OrderFilter): Promise<number> {
    const response = await this.axiosInstance.get<number>(
      '/api/v1/dashboard/orders/count',
      { params: filter }
    );
    return response.data;
  }
}

/**
 * Factory para criar instância da API.
 */
export class OrderApiFactory {
  static create(axiosInstance?: AxiosInstance): OrderApiStrategy {
    const instance = axiosInstance || axios.create({
      baseURL: import.meta.env.VITE_API_URL || 'https://api.example.com',
    });
    return new AxiosOrderApiStrategy(instance);
  }
  
  // Para testes: criar mock
  static createMock(): OrderApiStrategy {
    return {
      getOrder: async (id) => ({ /* mock data */ } as OrderWithCustomer),
      listOrders: async () => [],
      countOrders: async () => 0,
    };
  }
}

// Export default para uso simples
const api = OrderApiFactory.create();
export default api;
```

### 3. Custom Hooks

```typescript
// hooks/useOrderDashboard.ts

import { useState, useEffect, useCallback } from 'react';
import { OrderWithCustomer, OrderFilter } from '../types/order';
import { OrderApiStrategy, OrderApiFactory } from '../services/orderDashboardApi';

/**
 * Hook para buscar um pedido específico.
 * Usa Strategy Pattern para permitir diferentes implementações.
 */
export function useOrder(orderId: number | null, apiStrategy?: OrderApiStrategy) {
  const [order, setOrder] = useState<OrderWithCustomer | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);
  
  const api = apiStrategy || OrderApiFactory.create();
  
  useEffect(() => {
    if (!orderId) {
      setLoading(false);
      return;
    }
    
    async function fetchOrder() {
      try {
        setLoading(true);
        setError(null);
        const data = await api.getOrder(orderId);
        setOrder(data);
      } catch (err) {
        setError(err instanceof Error ? err : new Error('Erro ao buscar pedido'));
      } finally {
        setLoading(false);
      }
    }
    
    fetchOrder();
  }, [orderId, api]);
  
  return { order, loading, error };
}

/**
 * Hook para listar pedidos com filtros.
 */
export function useOrders(filter: OrderFilter = {}, apiStrategy?: OrderApiStrategy) {
  const [orders, setOrders] = useState<OrderWithCustomer[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);
  
  const api = apiStrategy || OrderApiFactory.create();
  
  const refetch = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await api.listOrders(filter);
      setOrders(data);
    } catch (err) {
      setError(err instanceof Error ? err : new Error('Erro ao buscar pedidos'));
    } finally {
      setLoading(false);
    }
  }, [filter, api]);
  
  useEffect(() => {
    refetch();
  }, [refetch]);
  
  return { orders, loading, error, refetch };
}
```

### 4. Componentes React

```typescript
// components/OrderDetails.tsx

import React from 'react';
import { useOrder } from '../hooks/useOrderDashboard';
import { formatCurrency, formatDate } from '../utils/format';

interface OrderDetailsProps {
  orderId: number;
}

export function OrderDetails({ orderId }: OrderDetailsProps) {
  const { order, loading, error } = useOrder(orderId);
  
  if (loading) return <div>Carregando...</div>;
  if (error) return <div>Erro: {error.message}</div>;
  if (!order) return <div>Pedido não encontrado</div>;
  
  return (
    <div className="order-details">
      <h2>Pedido #{order.numeroPedido}</h2>
      
      <section className="customer-info">
        <h3>Cliente</h3>
        <p><strong>Nome:</strong> {order.cliente.nomeCompleto}</p>
        <p><strong>Email:</strong> {order.cliente.email}</p>
        <p><strong>WhatsApp:</strong> {order.cliente.whatsapp}</p>
        <p><strong>CPF:</strong> {order.cliente.cpf}</p>
      </section>
      
      <section className="address-info">
        <h3>Endereço de Entrega</h3>
        <p>{order.endereco.enderecoCompleto}</p>
      </section>
      
      <section className="order-info">
        <h3>Detalhes do Pedido</h3>
        <p><strong>Status:</strong> {order.pedido.status}</p>
        <p><strong>Método de Pagamento:</strong> {order.pedido.metodoPagamento}</p>
        <p><strong>Valor Total:</strong> {formatCurrency(order.pedido.valorTotal)}</p>
        <p><strong>Data do Pedido:</strong> {formatDate(order.pedido.dataPedido)}</p>
        {order.pedido.pago && order.pedido.dataPagamento && (
          <p><strong>Data do Pagamento:</strong> {formatDate(order.pedido.dataPagamento)}</p>
        )}
      </section>
      
      {order.cupom && (
        <section className="coupon-info">
          <h3>Cupom Aplicado</h3>
          <p><strong>Código:</strong> {order.cupom.codigo}</p>
          {order.cupom.nomeCupom && <p><strong>Nome:</strong> {order.cupom.nomeCupom}</p>}
          {order.cupom.valorOriginal && (
            <p><strong>Valor Original:</strong> {formatCurrency(order.cupom.valorOriginal)}</p>
          )}
          <p><strong>Desconto:</strong> {formatCurrency(order.cupom.descontoAplicado)}</p>
        </section>
      )}
      
      <section className="order-items">
        <h3>Itens do Pedido</h3>
        <table>
          <thead>
            <tr>
              <th>Livro</th>
              <th>Quantidade</th>
              <th>Preço Unitário</th>
              <th>Subtotal</th>
            </tr>
          </thead>
          <tbody>
            {order.items.map((item, index) => (
              <tr key={index}>
                <td>{item.titulo}</td>
                <td>{item.quantidade}</td>
                <td>{formatCurrency(item.preco)}</td>
                <td>{formatCurrency(item.preco * item.quantidade)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </div>
  );
}
```

---

## 🔄 Extensibilidade (OCP em Ação)

### Exemplo 1: Adicionar Novo Filtro

**Sem modificar código existente**, apenas estendendo:

```java
// Nova implementação de OrderFilter com filtro por data
public record OrderFilterWithDate(
    String status,
    String email,
    LocalDate dataInicio,
    LocalDate dataFim
) extends OrderFilter {
    // Extensão sem modificar OrderFilter original
}
```

### Exemplo 2: Nova Implementação de Query Strategy

```java
// Implementação usando JPA (futuro)
@Component
public class JpaOrderQueryStrategyImpl implements OrderQueryStrategy {
    private final OrderRepository orderRepository;
    
    @Override
    public Optional<OrderWithCustomerDTO> findOrderById(...) {
        // Implementação usando JPA
    }
}
```

### Exemplo 3: Cache Strategy

```java
// Decorator Pattern para adicionar cache
@Component
@Primary
public class CachedOrderQueryStrategy implements OrderQueryStrategy {
    private final OrderQueryStrategy delegate;
    private final Cache cache;
    
    @Override
    public Optional<OrderWithCustomerDTO> findOrderById(...) {
        String key = "order:" + orderId;
        return cache.get(key, () -> delegate.findOrderById(...));
    }
}
```

---

## ✅ Benefícios da Arquitetura OCP

1. **Extensibilidade**: Adicionar novos filtros, queries, formatos sem modificar código existente
2. **Testabilidade**: Fácil criar mocks e testes unitários
3. **Manutenibilidade**: Código organizado e responsabilidades claras
4. **Reutilização**: Strategies podem ser reutilizadas em diferentes contextos
5. **Flexibilidade**: Trocar implementações sem afetar o resto do sistema

---

## 📝 Checklist de Implementação

### Backend:
- [ ] Criar interfaces de Strategy (`OrderQueryStrategy`, `CustomerQueryStrategy`, etc.)
- [ ] Implementar `OrderQueryStrategyImpl`
- [ ] Criar `QueryBuilder` para construção dinâmica de SQL
- [ ] Criar `OrderDTOMapper` para mapeamento
- [ ] Implementar `EcommerceConnectionProvider`
- [ ] Criar Services (`OrderDashboardService`, etc.)
- [ ] Criar Controllers REST
- [ ] Criar DTOs
- [ ] Configurar injeção de dependências no Spring

### Frontend:
- [ ] Criar tipos/interfaces TypeScript
- [ ] Implementar `OrderApiStrategy` e `AxiosOrderApiStrategy`
- [ ] Criar `OrderApiFactory`
- [ ] Criar custom hooks (`useOrder`, `useOrders`)
- [ ] Criar componentes React
- [ ] Criar utilitários de formatação

---

**Última atualização:** Novembro 2025

