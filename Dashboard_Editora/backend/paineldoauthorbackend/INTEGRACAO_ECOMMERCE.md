# 🔗 Integração Painel do Autor ↔ E-Commerce

Este documento explica como o **Painel do Autor Backend** (`paineldoauthorbackend`) se integra com o **Sistema de E-Commerce**, destacando as diferenças arquiteturais e como os dois sistemas trabalham juntos.

---

## 📋 Visão Geral

### Dois Sistemas Diferentes

1. **E-Commerce** (Sistema de vendas)
   - Origin-Based Authorization (CORS Whitelist)
   - **NÃO usa JWT**
   - Stateless (sem sessões)
   - Multi-tenant por `author_id`

2. **Painel do Autor Backend** (Sistema de gestão)
   - JWT Authentication
   - Autenticação de usuários
   - Sessões com refresh tokens
   - Conecta ao banco do e-commerce para buscar dados

---

## 🔐 Diferenças de Autorização

### E-Commerce (Origin-Based)

```kotlin
// E-Commerce usa CORS Whitelist
allowedOriginPatterns(
    "https://www.agenorgasparetto.com.br",
    "https://agenorgasparetto.com.br"
)
```

**Características:**
- ✅ Sem autenticação de usuário
- ✅ Sem JWT
- ✅ Sem sessões
- ✅ Autorização implícita pela origem

### Painel do Autor (JWT-Based)

```java
// Painel do Autor usa JWT
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public ResponseEntity<?> getDashboard() {
    // Requer token JWT válido
}
```

**Características:**
- ✅ Autenticação de usuários (email/password)
- ✅ JWT tokens (access + refresh)
- ✅ Sessões com refresh tokens
- ✅ Role-based access control (USER/ADMIN)

---

## 🔄 Como Funciona a Integração

### Fluxo de Integração

```
┌─────────────────────┐
│  Painel do Autor    │
│   (Backend)         │
│                     │
│  - JWT Auth         │
│  - Usuários         │
│  - Sessões          │
└──────────┬──────────┘
           │
           │ Conecta via JDBC
           │ usando credenciais
           │ do usuário
           ▼
┌─────────────────────┐
│  Banco do E-Commerce│
│                     │
│  - orders           │
│  - payment_payouts  │
│  - books            │
│  - authors          │
└─────────────────────┘
```

### 1. Autenticação no Painel do Autor

O usuário faz login no **Painel do Autor**:

```java
// POST /api/v1/auth/login
{
  "email": "autor@example.com",
  "password": "senha123"
}

// Resposta: JWT token
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "refresh_token_here"
}
```

### 2. Credenciais do Banco do E-Commerce

Cada usuário no **Painel do Autor** possui credenciais do banco do e-commerce:

```java
// Tabela: users
{
  "id": "user-1",
  "email": "autor@example.com",
  "author_id": "1",  // ID do autor no e-commerce
  "ecommerce_db_url": "jdbc:postgresql://...",
  "ecommerce_db_username": "db_user",
  "ecommerce_db_password": "db_pass"
}
```

### 3. Consulta ao Banco do E-Commerce

Quando o usuário autenticado acessa dados de pagamentos:

```java
// GET /api/v1/autor/pagamentos/painel
// Headers: Authorization: Bearer {jwt_token}

// Backend:
// 1. Valida JWT token
// 2. Obtém usuário autenticado
// 3. Busca credenciais do banco do e-commerce
// 4. Conecta ao banco do e-commerce usando JDBC
// 5. Consulta dados filtrados por author_id
// 6. Retorna dados para o frontend
```

---

## 📊 Consultas ao Banco do E-Commerce

### ⚠️ IMPORTANTE: Valores Reais vs Valores "de Mentira"

O documento do e-commerce explica que existem **dois tipos de valores**:

#### ❌ Valores "de Mentira" (NÃO USE)

```sql
-- NÃO USE: Mostra valores brutos dos pedidos
SELECT SUM(o.total) 
FROM orders o 
WHERE o.status = 'CONFIRMED';
-- Resultado: R$ 1.533,00 (valor que clientes pagaram)
```

**Problema:** Não desconta taxas, margens e comissões.

#### ✅ Valores Reais (USE ESTE)

```sql
-- USE: Mostra valores líquidos dos payouts
SELECT SUM(pp.amount_net) 
FROM payment_payouts pp 
WHERE pp.status = 'CONFIRMED';
-- Resultado: R$ 110,17 (valor que autor realmente recebeu)
```

**Correto:** Reflete o que o autor realmente recebeu após todas as deduções.

### Implementação no Painel do Autor

**Status Atual:**

O sistema possui **dois serviços** diferentes:

1. **`PaymentQueryService`** ✅ **CORRETO**
   - Já usa `payment_payouts` para buscar valores reais
   - Usa `amount_net` (valor líquido após taxas)
   - Filtra por `author_id`

2. **`PagamentosAutorServiceImpl`** ⚠️ **PRECISA CORREÇÃO**
   - Atualmente usa `orders` com status `CONFIRMED`
   - Mostra valores brutos (não descontam taxas)
   - **Deveria usar `payment_payouts.amount_net`**

**Código Atual (Incorreto):**

```java
// PagamentosAutorServiceImpl.java
// Busca de orders (mostra valores incorretos - não descontam taxas)
double valorVendasConfirmadas = calcularValorVendasConfirmadas(conn, autorId);
// Query: SUM(oi.quantity * oi.price) FROM orders WHERE status = 'CONFIRMED'
```

**Código Correto (Recomendado):**

```java
// Deveria buscar de payment_payouts.amount_net
String sql = """
    SELECT 
        COUNT(*) as payouts_confirmados,
        COALESCE(SUM(amount_net), 0) as valor_recebido_real
    FROM payment_payouts
    WHERE author_id = ? 
    AND status = 'CONFIRMED'
""";
```

**Nota:** O `PaymentQueryService` já está correto e pode ser usado como referência para atualizar o `PagamentosAutorServiceImpl`.

---

## 🔍 Filtragem por `author_id`

### Isolamento Multi-Tenant

O e-commerce usa **isolamento por `author_id`** para garantir que cada autor veja apenas seus próprios dados.

### No Painel do Autor

O `author_id` é obtido do usuário autenticado:

```java
// CurrentAuthorService.java
Optional<Long> authorIdOpt = currentAuthorService.getCurrentAuthorId();

// Todas as queries devem filtrar por author_id
String sql = """
    SELECT * FROM payment_payouts 
    WHERE author_id = ?
""";
```

**Regra Fundamental:** Todas as consultas ao banco do e-commerce devem filtrar por `author_id` do usuário autenticado.

---

## 📡 Endpoints do Painel do Autor

### 1. Painel de Pagamentos

**Endpoint:** `GET /api/v1/autor/pagamentos/painel`

**Autenticação:** JWT (Bearer token)

**Como funciona:**
1. Valida JWT token
2. Obtém `author_id` do usuário autenticado
3. Busca credenciais do banco do e-commerce
4. Conecta ao banco do e-commerce via JDBC
5. Consulta dados filtrados por `author_id`
6. Retorna dados formatados

**Resposta:**
```json
{
  "resumo": {
    "autorId": 1,
    "nomeAutor": "Agenor Gasparetto",
    "valorVendasConfirmadas": 110.17,  // ⚠️ Deveria vir de payment_payouts
    "valorJaRecebido": 110.17,          // ⚠️ Deveria vir de payment_payouts
    "valorAReceber": 351.23              // ⚠️ Deveria vir de payment_payouts (status = 'SENT')
  }
}
```

### 2. Lista de Cobranças Mensais

**Endpoint:** `GET /api/v1/cobrancas`

**Autenticação:** JWT (Bearer token)

**Dados:** Vem do banco do **Painel do Autor** (tabela `monthly_charges`), não do e-commerce.

---

## 🔧 Queries Recomendadas

### 1. Valor Real Recebido (CORRETO)

```sql
-- Buscar valor REAL que o autor recebeu
SELECT 
    COUNT(*) as payouts_confirmados,
    COALESCE(SUM(amount_net), 0) as valor_recebido_real
FROM payment_payouts
WHERE author_id = ? 
AND status = 'CONFIRMED';
```

### 2. Valor A Receber (Enviado mas não confirmado)

```sql
-- Buscar valor enviado mas ainda não confirmado
SELECT 
    COUNT(*) as payouts_enviados,
    COALESCE(SUM(amount_net), 0) as valor_enviado_pendente
FROM payment_payouts
WHERE author_id = ? 
AND status = 'SENT';
```

### 3. Funil de Vendas (Status dos Pedidos)

```sql
-- Funil de vendas (usar orders para quantidade, não para valores)
SELECT 
    status,
    COUNT(*) as total_pedidos
FROM orders
WHERE EXISTS (
    SELECT 1 FROM order_items oi
    JOIN books b ON b.id::text = oi.book_id
    WHERE oi.order_id = orders.id
    AND b.author_id = ?
)
GROUP BY status;
```

### 4. Vendas Recentes

```sql
-- Vendas recentes (usar payment_payouts para valores reais)
SELECT 
    pp.id as payment_id,
    pp.order_id,
    pp.amount_net as valor_total,
    pp.status,
    pp.paid_at as data_pagamento,
    b.title as titulo_livro
FROM payment_payouts pp
JOIN orders o ON o.id = pp.order_id
JOIN order_items oi ON oi.order_id = o.id
JOIN books b ON b.id::text = oi.book_id
WHERE pp.author_id = ?
ORDER BY pp.paid_at DESC NULLS LAST, pp.id DESC
LIMIT 20;
```

---

## ⚠️ Problemas Identificados

### 1. Uso de `orders.total` ao invés de `payment_payouts.amount_net`

**Problema:** O `PagamentosAutorServiceImpl` usa valores de `orders` que não refletem o valor real recebido.

**Impacto:**
- Mostra valores brutos dos pedidos (ex: R$ 1.533,00)
- Não descontam taxas, margens e comissões
- Autor vê valores maiores do que realmente recebeu

**Solução:** Atualizar `PagamentosAutorServiceImpl` para usar `payment_payouts.amount_net` (seguir o padrão do `PaymentQueryService`).

**Exemplo de Diferença:**
- **Valor de Orders:** R$ 1.533,00 (valor bruto que clientes pagaram)
- **Valor Real (Payouts):** R$ 110,17 (valor líquido que autor recebeu)
- **Diferença:** R$ 1.422,83 (taxas, margens, pedidos sem payout ainda)

### 2. Falta de Filtro por `author_id` em Algumas Queries

**Problema:** Algumas queries podem não estar filtrando corretamente por `author_id`.

**Solução:** Garantir que todas as queries ao banco do e-commerce filtrem por `author_id` do usuário autenticado.

---

## ✅ Checklist de Integração

### Backend (Painel do Autor)

- [ ] **Usar `payment_payouts.amount_net` para valores recebidos** (não `orders.total`)
- [ ] **Filtrar por `author_id` em todas as queries** ao banco do e-commerce
- [ ] **Usar `status = 'CONFIRMED'` para valores já recebidos**
- [ ] **Usar `status = 'SENT'` para valores enviados mas pendentes**
- [ ] **Validar credenciais do banco do e-commerce** antes de conectar
- [ ] **Tratar erros de conexão** ao banco do e-commerce

### Frontend

- [ ] **Usar JWT tokens** para autenticação (não CORS)
- [ ] **Enviar token no header** `Authorization: Bearer {token}`
- [ ] **Exibir valores líquidos** (`amount_net`), não valores brutos
- [ ] **Mostrar diferença** entre "Vendas Confirmadas" e "Valor Real Recebido"

---

## 🔗 Referências

### Documentação do E-Commerce
- Sistema de autorização e compartilhamento de pagamentos
- Queries corretas para métricas
- Diferença entre valores reais e valores "de mentira"

### Código do Painel do Autor
- `PagamentosAutorServiceImpl.java` - Service de pagamentos
- `PaymentQueryService.java` - Service de consulta de pagamentos
- `CurrentAuthorService.java` - Service para obter `author_id` do usuário

---

## 📝 Resumo

| Aspecto | E-Commerce | Painel do Autor |
|---------|------------|-----------------|
| **Autorização** | CORS Whitelist | JWT Tokens |
| **Autenticação** | Não há | Email/Password |
| **Sessões** | Stateless | Refresh Tokens |
| **Acesso** | Por origem | Por usuário autenticado |
| **Dados** | Banco próprio | Conecta ao banco do e-commerce |
| **Filtragem** | Por `author_id` | Por `author_id` do usuário |

**Princípio Fundamental:** Ambos os sistemas usam **isolamento por `author_id`**, mas com técnicas de autorização diferentes.

---

**Última atualização:** Janeiro 2024

