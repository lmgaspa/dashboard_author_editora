# 🏗️ Arquitetura Multi-Tenant - Frontend Angular

## 📋 Visão Geral

Este documento explica como o frontend Angular trabalha com a arquitetura **multi-tenant baseada em `author_id`** do sistema.

---

## 🔐 Como Funciona o `author_id` no Frontend

### 1. Obtendo o `author_id`

O `author_id` é obtido automaticamente após o login através do endpoint de perfil:

```typescript
// src/app/core/services/auth.service.ts
getUserProfile(): Observable<User> {
  return this.http.get<any>(`${this.API_URL}/api/v1/user/profile`).pipe(
    map((response: any) => {
      // Backend pode retornar author_id (snake_case) ou authorId (camelCase)
      const mappedUser: User = {
        // ...
        authorId: response.author_id || response.authorId || currentUser?.authorId || null,
        // ...
      };
      return mappedUser;
    })
  );
}
```

**Resposta do backend:**
```json
{
  "id": "user-1",
  "name": "Nome do Usuário",
  "email": "usuario@example.com",
  "author_id": "1",  // ← Este valor é armazenado no frontend
  "ecommerceUrl": "https://ecommerce-do-autor.com"
}
```

### 2. Armazenando o `author_id`

O `author_id` é armazenado em:
- **Signal reativo** (`AuthService._currentUser`)
- **localStorage** (para persistência entre sessões)

```typescript
// src/app/core/services/auth.service.ts
private readonly _currentUser = signal<User | null>(null);

// Após login ou carregar perfil
this._currentUser.set(user);
localStorage.setItem('currentUser', JSON.stringify(user));
```

### 3. Usando o `author_id`

**IMPORTANTE:** Na maioria dos casos, o frontend **NÃO precisa passar `author_id` explicitamente** nas requisições. O backend identifica automaticamente o `author_id` do usuário logado via token JWT.

#### Casos onde o frontend NÃO passa `author_id`:

```typescript
// ✅ CORRETO: Backend identifica automaticamente
this.http.get(`${API_URL}/api/v1/autor/pagamentos/painel`);
this.http.get(`${API_URL}/api/v1/autor/emails/painel`);
this.http.get(`${API_URL}/api/v1/cobrancas`);
this.http.get(`${API_URL}/api/v1/dashboard/orders`);
```

#### Casos onde o frontend PODE passar `author_id`:

```typescript
// ✅ Para exportação (opcional, backend usa do token se não passar)
this.exportService.exportPayments({
  format: 'pdf',
  authorId: currentUser.authorId  // Opcional, backend usa do token se não passar
});

// ✅ Para admins acessarem dados de outros autores
this.http.get(`${API_URL}/api/v1/admin/payments/author/${authorId}/summary`);
```

---

## 🔄 Fluxo Completo: Do Login até a Query

### Passo 1: Usuário Faz Login

```typescript
// src/app/features/auth/pages/login/login-page.component.ts
login(email: string, password: string) {
  this.authService.login({ email, password }).subscribe({
    next: (response) => {
      // Token JWT é armazenado automaticamente
      // Perfil do usuário (incluindo authorId) é carregado
    }
  });
}
```

**Backend:**
- Valida credenciais
- Gera JWT token (contém email do usuário)
- Retorna token + dados do usuário

### Passo 2: Frontend Armazena Token e Carrega Perfil

```typescript
// src/app/core/services/auth.service.ts
login(data: LoginRequest): Observable<AuthResponse> {
  return this.http.post<AuthResponse>(`${this.API_URL}/api/v1/auth/login`, data).pipe(
    tap((response) => {
      // Armazenar token
      localStorage.setItem('accessToken', response.accessToken);
      
      // Armazenar usuário (incluindo authorId)
      this._currentUser.set(response.user);
      localStorage.setItem('currentUser', JSON.stringify(response.user));
    })
  );
}
```

### Passo 3: Frontend Chama Endpoint Protegido

```typescript
// src/app/core/services/payment.service.ts
getPainelPagamentos(): Observable<PainelPagamentosAutorDTO> {
  // Token JWT é enviado automaticamente pelo interceptor
  return this.http.get<PainelPagamentosAutorDTO>(
    `${this.API_URL}/api/v1/autor/pagamentos/painel`
  );
}
```

**Interceptor adiciona token automaticamente:**
```typescript
// src/app/core/interceptors/auth.interceptor.ts
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('accessToken');
  
  let headers = new HttpHeaders();
  if (token) {
    headers = headers.set('Authorization', `Bearer ${token}`);
  }
  
  const clonedReq = req.clone({ headers });
  return next(clonedReq);
};
```

### Passo 4: Backend Processa Requisição

**Backend (Java):**
1. Extrai email do token JWT
2. Busca usuário no banco do painel
3. Obtém `author_id` do usuário
4. Obtém credenciais do banco do e-commerce
5. Conecta ao banco do e-commerce do autor
6. Executa query com filtro `WHERE b.author_id = ?`
7. Retorna apenas dados do autor logado

### Passo 5: Frontend Recebe e Exibe Dados

```typescript
// src/app/features/user/pages/payments/payments-page.component.ts
loadPainel(): void {
  this.paymentService.getPainelPagamentos().subscribe({
    next: (data) => {
      // Dados já filtrados pelo backend (apenas do autor logado)
      this.painel.set(data);
    }
  });
}
```

---

## 🛡️ Segurança e Isolamento

### Garantias de Segurança

1. **Token JWT no Header**: Todas as requisições incluem token JWT automaticamente via interceptor
2. **Backend Valida Token**: Backend valida token e extrai `author_id` do usuário logado
3. **Filtro Obrigatório**: Backend sempre filtra por `WHERE b.author_id = ?` nas queries
4. **Isolamento por Banco (Opcional)**: Cada autor pode ter seu próprio banco do e-commerce
5. **Validação de Permissões**: Usuários só podem acessar seu próprio `author_id` (validado no backend)

### Exemplo de Tentativa de Acesso Não Autorizado

**Cenário:** Autor 1 tenta acessar dados do Autor 2

```typescript
// ❌ Frontend tenta passar author_id = 2
this.http.get(`${API_URL}/api/v1/payments/export?author_id=2`);
```

**Backend valida:**
```java
// Backend obtém author_id do token JWT (autor 1)
Long currentAuthorId = currentAuthorService.getCurrentAuthorId(); // 1L

// Se author_id passado for diferente e usuário não for admin
if (authorId != null && !authorId.equals(currentAuthorId) && !isAdmin) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body("Você não tem permissão para acessar dados de outro autor");
}
```

**Resultado:** ❌ **403 Forbidden** - Usuário não pode acessar dados de outro autor

---

## 📊 Mapeamento: Frontend → Backend → Banco

### Estrutura de Dados

```
┌─────────────────────────────────────────────────────────┐
│  Frontend Angular                                       │
│  ┌───────────────────────────────────────────────────┐  │
│  │  AuthService.currentUser()                        │  │
│  │  - authorId: "1"                                  │  │
│  │  - email: "autor1@example.com"                    │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                        │
                        │ HTTP Request
                        │ Authorization: Bearer {token}
                        ▼
┌─────────────────────────────────────────────────────────┐
│  Backend (Spring Boot)                                 │
│  ┌───────────────────────────────────────────────────┐  │
│  │  1. Extrai email do token JWT                    │  │
│  │  2. Busca user no banco do painel                │  │
│  │  3. Obtém author_id = "1"                        │  │
│  │  4. Obtém credenciais do banco do e-commerce     │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                        │
                        │ JDBC Connection
                        │ jdbc:postgresql://host:5432/ecom
                        ▼
┌─────────────────────────────────────────────────────────┐
│  Banco do E-commerce (PostgreSQL)                        │
│  ┌───────────────────────────────────────────────────┐  │
│  │  Query: SELECT * FROM orders o                    │  │
│  │  JOIN books b ON ...                              │  │
│  │  WHERE b.author_id = 1  ← FILTRO POR author_id! │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

---

## 🔧 Implementação nos Serviços

### Padrão de Serviços

Todos os serviços seguem o mesmo padrão:

```typescript
@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private readonly http = inject(HttpClient);
  private readonly API_URL = environment.apiUrl;

  /**
   * Obtém o painel de pagamentos do autor logado.
   * 
   * ARQUITETURA MULTI-TENANT:
   * - O backend identifica automaticamente o `author_id` do usuário logado via token JWT
   * - Cada autor só acessa seus próprios dados (isolamento garantido pelo backend)
   * - O frontend NÃO precisa passar `author_id` explicitamente
   */
  getPainelPagamentos(): Observable<PainelPagamentosAutorDTO> {
    // Token JWT é enviado automaticamente pelo interceptor
    return this.http.get<PainelPagamentosAutorDTO>(
      `${this.API_URL}/api/v1/autor/pagamentos/painel`
    );
  }
}
```

### Serviços que Seguem Este Padrão

1. **PaymentService** (`src/app/core/services/payment.service.ts`)
   - Endpoint: `/api/v1/autor/pagamentos/painel`
   - Backend identifica `author_id` automaticamente

2. **EmailService** (`src/app/core/services/email.service.ts`)
   - Endpoint: `/api/v1/autor/emails/painel`
   - Backend identifica `author_id` automaticamente

3. **MonthlyChargeService** (`src/app/core/services/monthly-charge.service.ts`)
   - Endpoint: `/api/v1/cobrancas`
   - Backend identifica `author_id` automaticamente

4. **OrderDashboardService** (`src/app/core/services/order-dashboard.service.ts`)
   - Endpoint: `/api/v1/dashboard/orders`
   - Backend identifica `author_id` automaticamente

5. **PayoutEmailDashboardService** (`src/app/core/services/payout-email-dashboard.service.ts`)
   - Endpoint: `/api/v1/dashboard/payout-emails`
   - Backend identifica `author_id` automaticamente

### Exceções: Quando Passar `author_id` Explicitamente

#### 1. Exportação (Opcional)

```typescript
// src/app/core/services/export.service.ts
exportPayments(options: ExportOptions): Observable<Blob> {
  // authorId é opcional - backend usa do token se não passar
  const params = this.buildParams(options);
  return this.http.get(`${this.API_URL}/api/v1/payments/export`, {
    params,
    responseType: 'blob'
  });
}
```

**Uso:**
```typescript
// Opção 1: Não passar (backend usa do token)
this.exportService.exportPayments({ format: 'pdf' });

// Opção 2: Passar explicitamente (útil para garantir)
this.exportService.exportPayments({
  format: 'pdf',
  authorId: this.authService.currentUser()?.authorId
});
```

#### 2. Endpoints de Admin

```typescript
// Admin pode acessar dados de qualquer autor
this.http.get(`${API_URL}/api/v1/admin/payments/author/${authorId}/summary`);
this.http.get(`${API_URL}/api/v1/cobrancas/admin?authorId=${authorId}`);
```

---

## ✅ Checklist de Implementação

### Frontend:

- [x] Interceptor adiciona token JWT automaticamente
- [x] AuthService armazena `authorId` após login
- [x] Serviços não passam `author_id` explicitamente (na maioria dos casos)
- [x] ExportService pode passar `author_id` opcionalmente
- [x] Componentes validam se `authorId` existe antes de exportar
- [x] Mensagens de erro informam quando `authorId` não está configurado

### Backend (para referência):

- [x] Backend extrai `author_id` do token JWT
- [x] Backend valida permissões (usuário só acessa seu próprio `author_id`)
- [x] Backend filtra todas as queries por `WHERE b.author_id = ?`
- [x] Backend conecta ao banco do e-commerce usando credenciais do usuário

---

## 📝 Notas Importantes

1. **Não existe URL base fixa do e-commerce**: Cada autor tem seu próprio e-commerce, e o backend gerencia isso internamente usando as credenciais do usuário.

2. **Isolamento é garantido pelo backend**: O frontend não precisa se preocupar com isolamento - o backend garante que cada autor só acessa seus próprios dados.

3. **Token JWT é suficiente**: O frontend não precisa passar `author_id` na maioria dos casos - o backend identifica automaticamente do token.

4. **Compatibilidade retroativa**: Se o backend não retornar `author_id` no perfil, o frontend trata graciosamente (mostra mensagem de erro apropriada).

5. **Admin pode acessar qualquer autor**: Admins podem passar `author_id` explicitamente para acessar dados de outros autores.

---

**Última atualização:** Novembro 2025

