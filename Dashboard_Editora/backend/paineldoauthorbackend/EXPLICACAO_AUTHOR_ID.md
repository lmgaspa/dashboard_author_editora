# 🔐 Como o Backend Usa o `author_id` para Cada Autor

## 📋 Visão Geral

O sistema funciona com **multi-tenancy por `author_id`**: cada autor tem seu próprio banco de dados do e-commerce, e o backend usa o `author_id` para garantir que cada usuário só acesse os dados do seu próprio autor.

---

## 🏗️ Arquitetura: Multi-Tenancy por Author

### Estrutura de Dados

```
┌─────────────────────────────────────────────────────────┐
│  Banco do Painel (PostgreSQL - Heroku)                  │
│  ┌───────────────────────────────────────────────────┐  │
│  │  Tabela: users                                    │  │
│  │  ┌─────────────┬──────────────┬────────────────┐ │  │
│  │  │ id          │ author_id    │ ecommerce_db_* │ │  │
│  │  ├─────────────┼──────────────┼────────────────┤ │  │
│  │  │ user-1      │ "1"          │ postgres://... │ │  │
│  │  │ user-2      │ "2"          │ postgres://... │ │  │
│  │  │ user-3      │ "3"          │ postgres://... │ │  │
│  │  │ admin-1     │ null         │ null           │ │  │
│  │  └─────────────┴──────────────┴────────────────┘ │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────┐
│  Banco(s) do E-commerce (PostgreSQL)                    │
│  ┌───────────────────────────────────────────────────┐  │
│  │  Tabelas: authors, books, orders, payment_payouts│  │
│  │                                                   │  │
│  │  CENÁRIO 1: Banco Único Compartilhado            │  │
│  │  - Todos os autores no mesmo banco                │  │
│  │  - Isolamento por WHERE b.author_id = ?          │  │
│  │                                                   │  │
│  │  CENÁRIO 2: Múltiplos Bancos                     │  │
│  │  - Cada autor pode ter seu próprio banco         │  │
│  │  - Isolamento por banco + WHERE b.author_id = ?  │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

**IMPORTANTE:** O sistema suporta **ambos os cenários**:
- **Cenário 1:** Vários autores compartilham o mesmo banco (isolamento por `author_id` nas queries)
- **Cenário 2:** Cada autor tem seu próprio banco (isolamento total por banco + `author_id`)

**O código funciona igual nos dois casos!** As credenciais do banco (`ecommerce_db_url`) são armazenadas por usuário, então cada autor pode apontar para um banco diferente ou o mesmo banco.

---

## 🔄 Fluxo Completo: Do Login até a Query

### Passo 1: Usuário Faz Login

```
POST /api/v1/auth/login
{
  "email": "autor1@example.com",
  "password": "senha123"
}
```

**Backend:**
- Valida credenciais
- Gera JWT token
- Token contém o `email` do usuário

### Passo 2: Frontend Chama Endpoint Protegido

```
GET /api/v1/autor/pagamentos/painel
Headers: Authorization: Bearer {token}
```

### Passo 3: Backend Obtém `author_id` do Usuário Logado

**Código:** `CurrentAuthorService.getCurrentAuthorId()`

```java
// 1. Extrai o email do token JWT
Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
UserDetails userDetails = (UserDetails) authentication.getPrincipal();
String email = userDetails.getUsername(); // "autor1@example.com"

// 2. Busca o usuário no banco do painel
Optional<User> userOpt = userRepositoryPort.findByEmail(email);

// 3. Extrai o author_id do usuário
User user = userOpt.get();
String authorIdStr = user.getAuthorId(); // "1"
Long authorId = Long.parseLong(authorIdStr); // 1L
```

**Resultado:** `authorId = 1L`

### Passo 4: Backend Obtém Credenciais do Banco do E-commerce

**Código:** `CurrentAuthorService.getCurrentUser()`

```java
// O mesmo usuário contém as credenciais do banco do e-commerce
User user = currentAuthorService.getCurrentUser().get();

String dbUrl = user.getEcommerceDbUrl();        // "jdbc:postgresql://host:5432/ecommerce_autor1"
String dbUsername = user.getEcommerceDbUsername(); // "user_db"
String dbPassword = user.getEcommerceDbPassword(); // "senha_db"
```

**Resultado:**
- Cada autor tem suas próprias credenciais de banco
- Essas credenciais apontam para o banco do e-commerce específico daquele autor

### Passo 5: Backend Conecta ao Banco do E-commerce do Autor

**Código:** `PagamentosAutorServiceImpl.montarPainelPagamentosAutor()`

```java
// Conecta ao banco do e-commerce usando as credenciais do usuário
Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
```

**Resultado:** Conexão estabelecida com o banco do e-commerce do Autor 1

### Passo 6: Backend Executa Query Filtrando por `author_id`

**Código:** `PagamentosAutorServiceImpl.calcularValorVendasConfirmadas()`

```sql
SELECT COALESCE(SUM(pp.amount_net), 0) AS total_confirmado
FROM payment_payouts pp
JOIN orders o ON o.id = pp.order_id
JOIN order_items oi ON oi.order_id = o.id
JOIN books b ON b.id::text = oi.book_id
WHERE b.author_id = ?  -- ← FILTRO POR author_id!
  AND pp.status = 'CONFIRMED'
```

**Parâmetro:** `stmt.setLong(1, authorId);` → `authorId = 1`

**Resultado:** Query retorna apenas dados do autor com `author_id = 1`

---

## 🔍 Exemplo Prático Completo

### Cenário: Autor 1 (author_id = 1) acessa seu painel

#### 1. Dados no Banco do Painel

```sql
-- Tabela: users
SELECT id, email, author_id, ecommerce_db_url 
FROM users 
WHERE email = 'autor1@example.com';

-- Resultado:
id          | email                | author_id | ecommerce_db_url
------------|----------------------|-----------|------------------
user-1      | autor1@example.com   | "1"       | jdbc:postgresql://host1:5432/ecommerce_autor1
```

#### 2. Backend Obtém `author_id`

```java
Long authorId = currentAuthorService.getCurrentAuthorId().get(); // 1L
User user = currentAuthorService.getCurrentUser().get();
String dbUrl = user.getEcommerceDbUrl(); // "jdbc:postgresql://host1:5432/ecommerce_autor1"
```

#### 3. Backend Conecta ao Banco do E-commerce do Autor 1

```java
Connection conn = DriverManager.getConnection(
    "jdbc:postgresql://host1:5432/ecommerce_autor1",
    "user_db",
    "senha_db"
);
```

#### 4. Backend Executa Query com Filtro por `author_id`

```sql
-- Query executada no banco do e-commerce do Autor 1
SELECT COALESCE(SUM(pp.amount_net), 0) AS total_confirmado
FROM payment_payouts pp
JOIN orders o ON o.id = pp.order_id
JOIN order_items oi ON oi.order_id = o.id
JOIN books b ON b.id::text = oi.book_id
WHERE b.author_id = 1  -- ← Só retorna dados do autor 1!
  AND pp.status = 'CONFIRMED'
```

**Resultado:** `total_confirmado = 1500.00` (apenas vendas do autor 1)

---

## 🔒 Segurança: Isolamento de Dados

### Por que é Seguro?

1. **Backend sempre filtra por `author_id` nas queries**
   - **TODAS** as queries têm `WHERE b.author_id = ?` ou similar
   - O `author_id` é passado como parâmetro, não pode ser injetado
   - Impossível acessar dados de outro autor mesmo em banco compartilhado

2. **Cada autor pode ter seu próprio banco (opcional)**
   - Se cada autor tiver banco separado: isolamento total por banco
   - Se vários autores compartilharem banco: isolamento por `author_id` nas queries
   - O sistema funciona igual nos dois casos

3. **`author_id` vem do usuário logado**
   - Não pode ser alterado pelo frontend
   - Vem do token JWT e do banco do painel
   - Usuário só pode acessar seu próprio `author_id`
   - Validação no backend: usuário não pode passar `author_id` de outro autor

4. **Credenciais do banco são por usuário**
   - Cada usuário tem suas próprias credenciais (`ecommerce_db_url`, etc.)
   - Mesmo que vários autores compartilhem o mesmo banco, cada um tem suas credenciais
   - Admin pode acessar qualquer autor, mas precisa passar `author_id` explicitamente

### Exemplo de Tentativa de Acesso Não Autorizado

**Cenário:** Autor 1 tenta acessar dados do Autor 2

```java
// Frontend tenta passar author_id = 2
GET /api/v1/payments/export?format=pdf&author_id=2
```

**Backend valida:**

```java
// Em PaymentsExportController
Optional<Long> currentAuthorIdOpt = currentAuthorService.getCurrentAuthorId();
// currentAuthorIdOpt = Optional.of(1L) (do usuário logado)

if (author_id != null) {
    if (currentAuthorIdOpt.isPresent() && !currentAuthorIdOpt.get().equals(author_id)) {
        // Usuário tentando acessar outro author_id - só admin pode
        if (!currentAuthorService.isCurrentUserAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Você não tem permissão para acessar dados de outro autor");
        }
    }
}
```

**Resultado:** ❌ **403 Forbidden** - Usuário não pode acessar dados de outro autor

---

## 📊 Mapeamento: User → Author → Banco

### Tabela de Mapeamento

**Exemplo: Cada autor com banco separado**
| Usuário (email)           | author_id | Banco do E-commerce                    | Credenciais DB                    |
|---------------------------|-----------|----------------------------------------|-----------------------------------|
| autor1@example.com        | 1         | `jdbc:postgresql://host1:5432/ecom1`   | user1 / pass1                     |
| autor2@example.com        | 2         | `jdbc:postgresql://host2:5432/ecom2`   | user2 / pass2                     |
| autor3@example.com        | 3         | `jdbc:postgresql://host3:5432/ecom3`   | user3 / pass3                     |
| admin@example.com         | null      | N/A (admin pode acessar qualquer um)   | N/A                               |

**Exemplo: Vários autores compartilhando o mesmo banco**
| Usuário (email)           | author_id | Banco do E-commerce                    | Credenciais DB                    |
|---------------------------|-----------|----------------------------------------|-----------------------------------|
| autor1@example.com        | 1         | `jdbc:postgresql://host:5432/ecom`    | user1 / pass1                     |
| autor2@example.com        | 2         | `jdbc:postgresql://host:5432/ecom`    | user2 / pass2                     |
| autor3@example.com        | 3         | `jdbc:postgresql://host:5432/ecom`    | user3 / pass3                     |
| admin@example.com         | null      | N/A (admin pode acessar qualquer um)   | N/A                               |

**Nota:** No segundo caso, todos apontam para o mesmo banco, mas o isolamento é garantido pelo filtro `WHERE b.author_id = ?` em todas as queries.

### Fluxo de Dados

```
┌─────────────────────────────────────────────────────────────┐
│  1. Login: autor1@example.com                              │
│     ↓                                                        │
│  2. JWT Token gerado (contém email)                        │
│     ↓                                                        │
│  3. Request: GET /api/v1/autor/pagamentos/painel          │
│     Headers: Authorization: Bearer {token}                 │
│     ↓                                                        │
│  4. Backend extrai email do token                          │
│     ↓                                                        │
│  5. Backend busca user no banco do painel                  │
│     SELECT * FROM users WHERE email = 'autor1@example.com'│
│     ↓                                                        │
│  6. Backend obtém:                                          │
│     - author_id = "1"                                       │
│     - ecommerce_db_url = "jdbc:postgresql://host1:5432/ecom1"│
│     - ecommerce_db_username = "user1"                      │
│     - ecommerce_db_password = "pass1"                       │
│     ↓                                                        │
│  7. Backend conecta ao banco do e-commerce do Autor 1       │
│     Connection conn = DriverManager.getConnection(...)     │
│     ↓                                                        │
│  8. Backend executa query com filtro:                       │
│     WHERE b.author_id = 1                                   │
│     ↓                                                        │
│  9. Retorna apenas dados do Autor 1                        │
└─────────────────────────────────────────────────────────────┘
```

---

## 🔧 Implementação no Código

### 1. Obter `author_id` do Usuário Logado

**Classe:** `CurrentAuthorService.java`

```java
@Service
public class CurrentAuthorService {
    
    public Optional<Long> getCurrentAuthorId() {
        // 1. Extrai email do token JWT
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = ((UserDetails) auth.getPrincipal()).getUsername();
        
        // 2. Busca usuário no banco do painel
        Optional<User> userOpt = userRepositoryPort.findByEmail(email);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }
        
        User user = userOpt.get();
        
        // 3. Se for admin, retorna empty (admin pode acessar qualquer autor)
        if (user.getRole() == Role.ADMIN) {
            return Optional.empty();
        }
        
        // 4. Extrai author_id do usuário
        String authorIdStr = user.getAuthorId();
        if (authorIdStr == null || authorIdStr.trim().isEmpty()) {
            return Optional.empty();
        }
        
        // 5. Converte para Long
        Long authorId = Long.parseLong(authorIdStr.trim());
        return Optional.of(authorId);
    }
    
    public Optional<User> getCurrentUser() {
        // Retorna o User completo (com credenciais do banco)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = ((UserDetails) auth.getPrincipal()).getUsername();
        return userRepositoryPort.findByEmail(email);
    }
}
```

### 2. Usar `author_id` e Credenciais para Conectar ao Banco

**Classe:** `PagamentosAutorController.java`

```java
@RestController
@RequestMapping("/api/v1/autor/pagamentos")
public class PagamentosAutorController {
    
    @GetMapping("/painel")
    public ResponseEntity<?> obterPainelAutor() {
        // 1. Obter author_id do usuário logado
        Optional<Long> authorIdOpt = currentAuthorService.getCurrentAuthorId();
        if (authorIdOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Usuário não possui author_id configurado");
        }
        Long authorId = authorIdOpt.get(); // 1L
        
        // 2. Obter credenciais do banco do e-commerce
        Optional<User> userOpt = currentAuthorService.getCurrentUser();
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Usuário não encontrado");
        }
        
        User user = userOpt.get();
        String dbUrl = user.getEcommerceDbUrl();        // "jdbc:postgresql://host1:5432/ecom1"
        String dbUsername = user.getEcommerceDbUsername(); // "user1"
        String dbPassword = user.getEcommerceDbPassword(); // "pass1"
        
        // 3. Montar painel usando author_id e credenciais
        PainelPagamentosAutorDTO painel = pagamentosAutorService.montarPainelPagamentosAutor(
            authorId,      // 1L
            dbUrl,         // "jdbc:postgresql://host1:5432/ecom1"
            dbUsername,    // "user1"
            dbPassword     // "pass1"
        );
        
        return ResponseEntity.ok(painel);
    }
}
```

### 3. Executar Query Filtrando por `author_id`

**Classe:** `PagamentosAutorServiceImpl.java`

```java
@Service
public class PagamentosAutorServiceImpl {
    
    public PainelPagamentosAutorDTO montarPainelPagamentosAutor(
            long autorId,        // 1L
            String dbUrl,        // "jdbc:postgresql://host1:5432/ecom1"
            String dbUsername,   // "user1"
            String dbPassword    // "pass1"
    ) {
        // 1. Conectar ao banco do e-commerce do autor
        Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
        
        // 2. Executar query filtrando por author_id
        String sql = """
            SELECT COALESCE(SUM(pp.amount_net), 0) AS total_confirmado
            FROM payment_payouts pp
            JOIN orders o ON o.id = pp.order_id
            JOIN order_items oi ON oi.order_id = o.id
            JOIN books b ON b.id::text = oi.book_id
            WHERE b.author_id = ?  -- ← FILTRO POR author_id!
              AND pp.status = 'CONFIRMED'
            """;
        
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setLong(1, autorId); // 1L
        
        ResultSet rs = stmt.executeQuery();
        // ... processar resultados
    }
}
```

---

## 🎯 Resumo: Como Funciona

1. **Usuário faz login** → Backend gera JWT token com email
2. **Frontend chama endpoint** → Envia token no header
3. **Backend extrai email do token** → Busca usuário no banco do painel
4. **Backend obtém `author_id`** → Do campo `users.author_id`
5. **Backend obtém credenciais do banco** → Do usuário (`ecommerce_db_url`, etc.)
6. **Backend conecta ao banco do e-commerce** → Usando credenciais do autor
7. **Backend executa query** → Sempre com `WHERE b.author_id = ?`
8. **Backend retorna dados** → Apenas do autor logado

---

## ✅ Garantias de Segurança

1. ✅ **Filtro obrigatório por `author_id`**: **TODAS** as queries filtram por `author_id` usando `WHERE b.author_id = ?`
2. ✅ **Isolamento por banco (opcional)**: Cada autor pode ter seu próprio banco, mas não é obrigatório
3. ✅ **`author_id` do usuário logado**: Não pode ser alterado pelo frontend, vem do token JWT
4. ✅ **Validação de permissões**: Usuários só podem acessar seu próprio `author_id` (validado no backend)
5. ✅ **Admin pode acessar qualquer autor**: Mas precisa passar `author_id` explicitamente e ter credenciais do banco
6. ✅ **Parâmetros preparados (PreparedStatement)**: Previne SQL injection, `author_id` é sempre passado como parâmetro

**O isolamento é garantido principalmente pelo filtro `WHERE b.author_id = ?` em todas as queries, independente de ter banco separado ou compartilhado.**

---

**Última atualização:** Novembro 2025

