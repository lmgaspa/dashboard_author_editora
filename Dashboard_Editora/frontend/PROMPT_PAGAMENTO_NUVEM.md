# 💰 Sistema de Pagamento a Nuvem - Documentação Frontend

Este documento contém todas as informações necessárias para implementar o sistema de **Pagamento a Nuvem** (Cobranças Mensais) no frontend.

---

## 📋 Visão Geral

O sistema de **Pagamento a Nuvem** permite que:
- **Autores** visualizem suas cobranças mensais, códigos PIX para pagamento e histórico
- **Administradores** criem cobranças, confirmem pagamentos e gerenciem todas as cobranças

Cada autor (`author_id`) possui cobranças mensais variáveis por contrato (ex: R$ 150,00, R$ 1.550,00, etc.).

---

## 🔐 Autenticação

Todos os endpoints requerem autenticação JWT. O token deve ser enviado no header:
```
Authorization: Bearer {token}
```

---

## 📡 Endpoints da API

### Base URL
```
/api/v1/cobrancas
```

---

### 1. Listar Cobranças do Autor
**GET** `/api/v1/cobrancas`

**Permissões:** USER ou ADMIN (mas admins devem usar endpoints de admin)

**Descrição:** Lista todas as cobranças do autor logado.

**Resposta (200 OK):**
```json
[
  {
    "id": "uuid",
    "authorId": "123",
    "authorName": "Nome do Autor",
    "chargeMonth": 1,
    "chargeYear": 2024,
    "amount": 150.00,
    "dueDate": "2024-01-15",
    "chargeDate": "2024-01-01",
    "status": "PENDING",
    "paidAt": null,
    "confirmedByAdminName": null,
    "confirmedAt": null,
    "pixCode": "00020126580014BR.GOV.BCB.PIX...",
    "pixExpiresAt": "2024-01-16T10:30:00Z",
    "daysOverdue": 0,
    "hasOpenTicket": false
  }
]
```

**Estrutura do DTO:**
```typescript
interface MonthlyChargeDTO {
  id: string;                    // UUID
  authorId: string;              // ID do autor
  authorName: string;           // Nome do autor
  chargeMonth: number;          // Mês (1-12)
  chargeYear: number;           // Ano (ex: 2024)
  amount: number;               // Valor (ex: 150.00)
  dueDate: string;              // Data de vencimento (YYYY-MM-DD)
  chargeDate: string;           // Data que a cobrança foi criada (YYYY-MM-DD)
  status: string;                // 'PENDING' | 'PAID' | 'OVERDUE' | 'CANCELLED'
  paidAt: string | null;        // ISO 8601 format ou null
  confirmedByAdminName: string | null;  // Nome do admin que confirmou
  confirmedAt: string | null;    // ISO 8601 format ou null
  pixCode: string | null;       // Código PIX para pagamento
  pixExpiresAt: string | null;   // ISO 8601 format ou null
  daysOverdue: number;          // Dias de atraso (se OVERDUE)
  hasOpenTicket: boolean;       // Se já existe ticket aberto relacionado
}
```

**Erros:**
- `403 Forbidden`: Usuário não possui author_id configurado ou é admin (deve usar endpoint de admin)
- `500 Internal Server Error`: Erro ao listar cobranças

---

### 2. Obter Código PIX para Pagamento
**GET** `/api/v1/cobrancas/{chargeId}/pix`

**Parâmetros:**
- `chargeId` (UUID): ID da cobrança

**Permissões:** USER ou ADMIN

**Descrição:** Obtém o código PIX e o valor para pagamento de uma cobrança específica.

**Resposta (200 OK):**
```json
{
  "pixCode": "00020126580014BR.GOV.BCB.PIX01361235204000053039865802BR5913PAINEL%20VIA6008BRASILIA62070503***6304ABCD",
  "amount": 150.00
}
```

**Estrutura:**
```typescript
interface PixCodeResponse {
  pixCode: string;
  amount: number;
}
```

**Erros:**
- `404 Not Found`: Cobrança não encontrada
- `403 Forbidden`: Acesso negado (cobrança não pertence ao autor)
- `500 Internal Server Error`: Erro ao obter código PIX

---

### 3. Criar Nova Cobrança (Admin)
**POST** `/api/v1/cobrancas`

**Permissões:** ADMIN

**Body:**
```json
{
  "authorId": "123",
  "chargeMonth": 1,
  "chargeYear": 2024,
  "amount": 150.00,
  "dueDate": "2024-01-15"
}
```

**Validações:**
- `authorId`: Obrigatório, string
- `chargeMonth`: Obrigatório, número entre 1-12
- `chargeYear`: Obrigatório, número entre 2020-2100
- `amount`: Obrigatório, número maior que 0
- `dueDate`: Obrigatório, formato YYYY-MM-DD

**Resposta (201 Created):**
```json
{
  "id": "uuid",
  "authorId": "123",
  "authorName": "Nome do Autor",
  "chargeMonth": 1,
  "chargeYear": 2024,
  "amount": 150.00,
  "dueDate": "2024-01-15",
  "chargeDate": "2024-01-01",
  "status": "PENDING",
  "paidAt": null,
  "confirmedByAdminName": null,
  "confirmedAt": null,
  "pixCode": "00020126580014BR.GOV.BCB.PIX...",
  "pixExpiresAt": "2024-01-16T10:30:00Z",
  "daysOverdue": 0,
  "hasOpenTicket": false
}
```

**Erros:**
- `400 Bad Request`: Dados inválidos ou já existe cobrança para o mesmo mês/ano/autor
- `403 Forbidden`: Apenas admins podem criar cobranças
- `500 Internal Server Error`: Erro ao criar cobrança

---

### 4. Confirmar Pagamento (Admin)
**PUT** `/api/v1/cobrancas/{chargeId}/confirmar`

**Parâmetros:**
- `chargeId` (UUID): ID da cobrança

**Permissões:** ADMIN

**Body:**
```json
{
  "notes": "Pagamento confirmado via PIX"
}
```

**Validações:**
- `notes`: Opcional, string

**Resposta (200 OK):**
```json
{
  "id": "uuid",
  "authorId": "123",
  "authorName": "Nome do Autor",
  "chargeMonth": 1,
  "chargeYear": 2024,
  "amount": 150.00,
  "dueDate": "2024-01-15",
  "chargeDate": "2024-01-01",
  "status": "PAID",
  "paidAt": "2024-01-15T10:30:00Z",
  "confirmedByAdminName": "Nome do Admin",
  "confirmedAt": "2024-01-15T10:30:00Z",
  "pixCode": "00020126580014BR.GOV.BCB.PIX...",
  "pixExpiresAt": null,
  "daysOverdue": 0,
  "hasOpenTicket": false
}
```

**Erros:**
- `404 Not Found`: Cobrança não encontrada
- `403 Forbidden`: Apenas admins podem confirmar pagamentos
- `500 Internal Server Error`: Erro ao confirmar pagamento

---

### 5. Listar Todas as Cobranças (Admin)
**GET** `/api/v1/cobrancas/admin`

**Permissões:** ADMIN

**Query Parameters:**
- `authorId` (opcional): Filtrar por author_id
- `status` (opcional): Filtrar por status ('PENDING', 'PAID', 'OVERDUE', 'CANCELLED')

**Exemplos:**
- `/api/v1/cobrancas/admin` - Todas as cobranças pendentes
- `/api/v1/cobrancas/admin?authorId=123` - Cobranças do autor 123
- `/api/v1/cobrancas/admin?status=PAID` - Todas as cobranças pagas
- `/api/v1/cobrancas/admin?authorId=123&status=PENDING` - Cobranças pendentes do autor 123

**Resposta (200 OK):**
```json
[
  {
    "id": "uuid",
    "authorId": "123",
    "authorName": "Nome do Autor",
    "chargeMonth": 1,
    "chargeYear": 2024,
    "amount": 150.00,
    "dueDate": "2024-01-15",
    "chargeDate": "2024-01-01",
    "status": "PENDING",
    "paidAt": null,
    "confirmedByAdminName": null,
    "confirmedAt": null,
    "pixCode": "00020126580014BR.GOV.BCB.PIX...",
    "pixExpiresAt": "2024-01-16T10:30:00Z",
    "daysOverdue": 0,
    "hasOpenTicket": false
  }
]
```

**Erros:**
- `400 Bad Request`: Status inválido
- `403 Forbidden`: Apenas admins podem acessar
- `500 Internal Server Error`: Erro ao listar cobranças

---

## 📊 Enums e Valores

### ChargeStatus
```typescript
enum ChargeStatus {
  PENDING = "PENDING",    // Pendente (aguardando pagamento)
  PAID = "PAID",          // Paga (admin confirmou)
  OVERDUE = "OVERDUE",    // Atrasada (vencida e não paga)
  CANCELLED = "CANCELLED" // Cancelada
}
```

---

## 🎨 Sugestões de UI/UX

### Para Autores (Painel de Cobranças)

1. **Lista de Cobranças:**
   - Cards ou tabela com todas as cobranças
   - Badge de status com cores:
     - `PENDING`: Amarelo/Laranja
     - `PAID`: Verde
     - `OVERDUE`: Vermelho
     - `CANCELLED`: Cinza
   - Mostrar mês/ano (ex: "Janeiro/2024")
   - Valor formatado em R$ (ex: R$ 150,00)
   - Data de vencimento destacada
   - Indicador de dias em atraso (se OVERDUE)
   - Botão "Ver PIX" para cobranças pendentes

2. **Modal/Componente de PIX:**
   - Exibir código PIX (QR Code se possível)
   - Botão para copiar código PIX
   - Valor a pagar em destaque
   - Data de expiração do PIX
   - Link para criar ticket relacionado (se necessário)

3. **Filtros e Ordenação:**
   - Filtrar por status
   - Ordenar por data de vencimento (mais recente primeiro)
   - Buscar por mês/ano

4. **Indicadores:**
   - Total de cobranças pendentes
   - Total de cobranças atrasadas
   - Valor total pendente
   - Valor total pago (mês atual/ano atual)

### Para Administradores (Gestão de Cobranças)

1. **Criação de Cobrança:**
   - Formulário com:
     - Seleção de autor (dropdown ou busca)
     - Mês e ano (dropdowns)
     - Valor (input numérico com formatação)
     - Data de vencimento (date picker)
   - Validação em tempo real
   - Mensagem de erro se já existe cobrança para o mesmo mês/ano

2. **Lista de Todas as Cobranças:**
   - Tabela com todas as cobranças
   - Filtros por autor e status
   - Colunas: Autor, Mês/Ano, Valor, Vencimento, Status, Ações
   - Botão "Confirmar Pagamento" para cobranças pendentes
   - Link para criar ticket relacionado

3. **Confirmação de Pagamento:**
   - Modal com:
     - Informações da cobrança
     - Campo opcional para notas
     - Botão de confirmação
   - Feedback visual após confirmação

4. **Dashboard/Resumo:**
   - Total de cobranças pendentes
   - Total de cobranças atrasadas
   - Valor total pendente
   - Valor total recebido (mês atual/ano atual)
   - Gráfico de cobranças por status

---

## 🔧 Configurações Técnicas

### Base URL da API
Verificar no arquivo de configuração do ambiente:
- Development: `http://localhost:8080`
- Production: Verificar variável de ambiente `FRONTEND_BASE_URL` ou configurar conforme necessário

### Headers Obrigatórios
```typescript
{
  "Authorization": "Bearer {jwt_token}",
  "Content-Type": "application/json"
}
```

### Tratamento de Erros
Todos os endpoints podem retornar:
- `400 Bad Request`: Dados inválidos
- `401 Unauthorized`: Token inválido ou expirado
- `403 Forbidden`: Sem permissão
- `404 Not Found`: Recurso não encontrado
- `500 Internal Server Error`: Erro do servidor

**Formato de Erro:**
```json
{
  "message": "Mensagem de erro descritiva"
}
```

---

## 📝 Notas Importantes

1. **Código PIX:**
   - O código PIX é gerado automaticamente quando a cobrança é criada
   - O código é um mock por enquanto (TODO: integrar com gerador de PIX real)
   - O PIX expira após um período determinado

2. **Unicidade:**
   - Não pode existir duas cobranças para o mesmo autor no mesmo mês/ano
   - O sistema valida isso automaticamente

3. **Status OVERDUE:**
   - O sistema pode marcar automaticamente cobranças como atrasadas
   - O campo `daysOverdue` mostra quantos dias a cobrança está atrasada

4. **Tickets Relacionados:**
   - O campo `hasOpenTicket` indica se já existe um ticket aberto relacionado à cobrança
   - Autores podem criar tickets relacionados a cobranças usando o campo `relatedChargeId` no endpoint de tickets

5. **Valor Variável:**
   - Cada autor pode ter um valor diferente por contrato
   - O valor é definido pelo admin ao criar a cobrança

---

## 🚀 Exemplo de Implementação TypeScript

### Service para Cobranças
```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class MonthlyChargeService {
  private baseUrl = '/api/v1/cobrancas';

  constructor(private http: HttpClient) {}

  // Autor: Listar suas cobranças
  listarCobrancasAutor(): Observable<MonthlyChargeDTO[]> {
    return this.http.get<MonthlyChargeDTO[]>(this.baseUrl);
  }

  // Autor: Obter código PIX
  obterPixCode(chargeId: string): Observable<PixCodeResponse> {
    return this.http.get<PixCodeResponse>(`${this.baseUrl}/${chargeId}/pix`);
  }

  // Admin: Criar cobrança
  criarCobranca(request: CreateChargeRequest): Observable<MonthlyChargeDTO> {
    return this.http.post<MonthlyChargeDTO>(this.baseUrl, request);
  }

  // Admin: Confirmar pagamento
  confirmarPagamento(chargeId: string, notes?: string): Observable<MonthlyChargeDTO> {
    return this.http.put<MonthlyChargeDTO>(
      `${this.baseUrl}/${chargeId}/confirmar`,
      { notes: notes || '' }
    );
  }

  // Admin: Listar todas as cobranças
  listarTodasCobrancas(authorId?: string, status?: string): Observable<MonthlyChargeDTO[]> {
    let params = new HttpParams();
    if (authorId) params = params.set('authorId', authorId);
    if (status) params = params.set('status', status);
    
    return this.http.get<MonthlyChargeDTO[]>(`${this.baseUrl}/admin`, { params });
  }
}
```

### Interfaces TypeScript
```typescript
interface MonthlyChargeDTO {
  id: string;
  authorId: string;
  authorName: string;
  chargeMonth: number;
  chargeYear: number;
  amount: number;
  dueDate: string;
  chargeDate: string;
  status: 'PENDING' | 'PAID' | 'OVERDUE' | 'CANCELLED';
  paidAt: string | null;
  confirmedByAdminName: string | null;
  confirmedAt: string | null;
  pixCode: string | null;
  pixExpiresAt: string | null;
  daysOverdue: number;
  hasOpenTicket: boolean;
}

interface PixCodeResponse {
  pixCode: string;
  amount: number;
}

interface CreateChargeRequest {
  authorId: string;
  chargeMonth: number;
  chargeYear: number;
  amount: number;
  dueDate: string;
}

interface ConfirmPaymentRequest {
  notes?: string;
}
```

### Utilitários
```typescript
// Formatação monetária brasileira
export function formatCurrency(value: number): string {
  return new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL'
  }).format(value);
}

// Nome do mês em português
export function getMonthName(month: number): string {
  const months = [
    'Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho',
    'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro'
  ];
  return months[month - 1];
}

// Formato de data brasileira
export function formatDate(date: string): string {
  return new Date(date).toLocaleDateString('pt-BR');
}

// Verificar se está vencido
export function isOverdue(dueDate: string): boolean {
  return new Date(dueDate) < new Date();
}

// Calcular dias de atraso
export function calculateDaysOverdue(dueDate: string): number {
  const due = new Date(dueDate);
  const today = new Date();
  const diffTime = today.getTime() - due.getTime();
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
}
```

---

## ✅ Checklist de Implementação

### Para Autores
- [ ] Criar service para cobranças
- [ ] Criar componente de lista de cobranças
- [ ] Criar componente de visualização de PIX
- [ ] Implementar formatação monetária brasileira
- [ ] Implementar badges de status com cores
- [ ] Adicionar filtros e ordenação
- [ ] Implementar indicadores (totais, pendentes, etc.)
- [ ] Adicionar link para criar ticket relacionado
- [ ] Implementar tratamento de erros
- [ ] Adicionar loading states

### Para Administradores
- [ ] Criar service para cobranças (admin)
- [ ] Criar componente de criação de cobrança
- [ ] Criar componente de lista de todas as cobranças
- [ ] Criar modal de confirmação de pagamento
- [ ] Implementar filtros por autor e status
- [ ] Criar dashboard/resumo de cobranças
- [ ] Implementar validações de formulário
- [ ] Adicionar feedback visual após ações
- [ ] Implementar tratamento de erros
- [ ] Adicionar loading states

---

## 🔗 Integração com Tickets

O sistema de cobranças está integrado com o sistema de tickets:
- Autores podem criar tickets relacionados a cobranças usando o campo `relatedChargeId`
- O campo `hasOpenTicket` indica se já existe ticket aberto relacionado
- Ao criar um ticket, pode-se passar o `relatedChargeId` no body da requisição

**Exemplo de criação de ticket relacionado:**
```json
{
  "title": "Dúvida sobre cobrança de Janeiro/2024",
  "description": "Tenho uma dúvida sobre o valor...",
  "category": "PAGAMENTO",
  "relatedChargeId": "uuid-da-cobranca"
}
```

---

**Última atualização:** Janeiro 2024

