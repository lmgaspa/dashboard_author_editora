# 📋 Prompt: Implementação Frontend - Dados de Clientes e Pedidos

## 🎯 Objetivo

Implementar no frontend uma arquitetura **extensível e manutenível** para acessar e exibir dados de clientes e pedidos, seguindo o **Princípio Aberto/Fechado (OCP)** e **boas práticas de React/TypeScript**.

---

## ⚠️ CRÍTICO: author_id é OBRIGATÓRIO - SEM ELE NADA FUNCIONA!

**🚨 ATENÇÃO: O `author_id` é o coração do sistema! Sem ele, NADA funciona!**

### Como Funciona o author_id

1. **O backend obtém o `author_id` automaticamente** do usuário logado na maioria dos endpoints
2. **O frontend precisa pegar o `authorId` do perfil** e armazená-lo
3. **Alguns endpoints podem aceitar `author_id` como parâmetro opcional** (para admins)

### Passo 1: Obter o authorId (OBRIGATÓRIO!)

**Após login**, o frontend DEVE chamar:

```
GET /api/v1/auth/profile
Headers: Authorization: Bearer {token}
```

**A resposta contém o `authorId`**:
```json
{
  "id": "user-1",
  "name": "Nome do Usuário",
  "email": "usuario@example.com",
  "authProvider": "LOCAL",
  "passwordSet": true,
  "profilePhotoUrl": null,
  "authorId": "1",  // ← ESTE É O VALOR CRUCIAL! SEM ELE NADA FUNCIONA!
  "ecommerceUrl": "https://ecommerce-do-autor.com"
}
```

### Passo 2: Armazenar o authorId

**Armazenar o `authorId`** no estado da aplicação:
- Context API (recomendado)
- Redux
- localStorage (não recomendado para dados sensíveis)
- Estado global da aplicação

### Passo 3: Validar o authorId

**⚠️ Se o `authorId` for `null` ou não existir:**
- O usuário não tem autor associado
- Mostrar mensagem: **"Author ID não configurado. Entre em contato com o administrador."**
- Bloquear acesso às funcionalidades que dependem do `author_id`
- Não permitir chamadas aos endpoints que precisam de `author_id`

### Passo 4: Usar o authorId

**A maioria dos endpoints obtém o `author_id` automaticamente**, mas alguns podem precisar:

- **Endpoints que obtêm automaticamente**: `/api/v1/autor/pagamentos/painel`, `/api/v1/autor/emails/painel`
- **Endpoints que podem aceitar como parâmetro**: `/api/v1/payments/export?author_id=1`, `/api/v1/emails/export?author_id=1`

**IMPORTANTE:** Se o usuário for USER (não admin), só pode passar seu próprio `author_id` ou omitir (o backend usa o do usuário logado).

---

## 📊 Endpoints Disponíveis no Backend

### Base URL
**IMPORTANTE:** Não existe uma URL base fixa. Cada autor tem seu próprio e-commerce, e o backend gerencia isso internamente usando as credenciais do usuário.

O backend está em: `https://dashboard-painel-autores-vl-26b242c59563.herokuapp.com` (ou a URL do seu deploy)

**Todos os endpoints abaixo são relativos a `/api/v1`**

### Como Funciona o author_id no Backend

- **A maioria dos endpoints obtém o `author_id` automaticamente** do usuário logado via `CurrentAuthorService`
- **Alguns endpoints aceitam `author_id` como parâmetro opcional** (para admins ou casos especiais)
- **O frontend NÃO precisa passar `author_id` na maioria dos casos**, mas deve tê-lo disponível para casos especiais

---

## 📊 Endpoints Existentes (Implementados)

### 1. Perfil do Usuário (OBRIGATÓRIO - Pegar authorId aqui!)

```
GET /api/v1/auth/profile
```

**Headers:**
- `Authorization: Bearer {token}`

**Response:**
```json
{
  "id": "user-1",
  "name": "Nome do Usuário",
  "email": "usuario@example.com",
  "authProvider": "LOCAL",
  "passwordSet": true,
  "profilePhotoUrl": null,
  "authorId": "1",  // ← USAR ESTE VALOR!
  "ecommerceUrl": "https://ecommerce-do-autor.com"
}
```

### 2. Painel de Pagamentos do Autor

```
GET /api/v1/autor/pagamentos/painel
```

**Como funciona:**
- O backend obtém o `author_id` automaticamente do usuário logado
- Não precisa passar `author_id` como parâmetro
- Retorna dados de pagamentos do autor

**Response:**
```json
{
  "valorVendasConfirmadas": 1500.00,
  "valorJaRecebido": 1200.00,
  "valorAReceber": 300.00,
  "vendasRecentes": [...]
}
```

### 3. Painel de E-mails do Autor

```
GET /api/v1/autor/emails/painel
```

**Como funciona:**
- O backend obtém o `author_id` automaticamente do usuário logado
- Não precisa passar `author_id` como parâmetro

**Response:**
```json
{
  "emailsClientes": [...],
  "emailsRepasse": [...]
}
```

### 4. Exportar Pagamentos

```
GET /api/v1/payments/export?format=pdf&author_id=1
```

**Query Parameters:**
- `format` (obrigatório): `pdf`, `csv`, ou `json`
- `author_id` (opcional): Se não fornecido, usa o `author_id` do usuário logado

**Como funciona:**
- Se o usuário for admin, pode passar qualquer `author_id`
- Se o usuário for USER, só pode passar seu próprio `author_id` (ou omitir)

### 5. Exportar E-mails

```
GET /api/v1/emails/export?format=pdf&author_id=1
```

**Query Parameters:**
- `format` (obrigatório): `pdf`, `csv`, ou `json`
- `author_id` (opcional): Se não fornecido, usa o `author_id` do usuário logado

---

## 📊 Endpoints Futuros (A Implementar)

Os endpoints abaixo **ainda não existem no backend**, mas são a arquitetura proposta:

### Endpoints de Pedidos (A Implementar)

### Endpoints de Pedidos

#### 1. Buscar Pedido Específico
```
GET /api/v1/dashboard/orders/{orderId}
```
**Headers:**
- `Authorization: Bearer {token}`

**Response:**
```json
{
  "id": 1009,
  "numeroPedido": 1009,
  "cliente": {
    "nomeCompleto": "Irene Cazorla",
    "email": "icazorla@uol.com.br",
    "whatsapp": "(73)99177-9913",
    "cpf": "119.156.348-06"
  },
  "endereco": {
    "rua": "Rua Zildo Pedro Guimarães Júnior",
    "numero": "201",
    "complemento": "Apto 202",
    "bairro": "Zildolândia",
    "cidade": "Itabuna",
    "estado": "BA",
    "cep": "45600-730",
    "enderecoCompleto": "Rua Zildo Pedro Guimarães Júnior, 201 - Apto 202 - Zildolândia, Itabuna - BA CEP: 45600-730"
  },
  "pedido": {
    "valorTotal": 45.00,
    "status": "CONFIRMED",
    "metodoPagamento": "card",
    "dataPedido": "2025-11-15T10:30:00Z",
    "pago": true,
    "dataPagamento": "2025-11-15T10:35:00Z"
  },
  "cupom": {
    "codigo": "BONUS",
    "descontoAplicado": 5.00,
    "valorOriginal": 50.00,
    "nomeCupom": "Cupom Bônus"
  },
  "items": [
    {
      "bookId": "123",
      "titulo": "Livro Exemplo",
      "quantidade": 1,
      "preco": 45.00
    }
  ]
}
```

#### 2. Listar Pedidos (com Filtros)
```
GET /api/v1/dashboard/orders?status=CONFIRMED&email=cliente@example.com&limit=50&offset=0
```

**Query Parameters:**
- `status` (opcional): `NEW`, `WAITING`, `CONFIRMED`, etc.
- `email` (opcional): Filtrar por email do cliente
- `phone` (opcional): Filtrar por telefone
- `cpf` (opcional): Filtrar por CPF
- `couponCode` (opcional): Filtrar por código de cupom
- `paid` (opcional): `true` ou `false`
- `limit` (opcional, padrão: 50): Limite de resultados
- `offset` (opcional, padrão: 0): Offset para paginação

**Response:** Array de `OrderWithCustomer`

#### 3. Contar Pedidos
```
GET /api/v1/dashboard/orders/count?status=CONFIRMED
```

**Response:**
```json
42
```

### Endpoints de Estatísticas de Clientes

#### 4. Estatísticas de Clientes
```
GET /api/v1/dashboard/customers/stats
```

**Response:**
```json
{
  "totalPedidos": 36,
  "clientesUnicosEmail": 3,
  "clientesUnicosWhatsapp": 3,
  "clientesUnicosCpf": 3
}
```

### Endpoints de Estatísticas de Cupons

#### 5. Estatísticas de Cupons
```
GET /api/v1/dashboard/coupons/stats
```

**Response:**
```json
{
  "totalPedidosComCupom": 19,
  "totalDescontoAplicado": 444.94,
  "cuponsUtilizados": [
    {
      "codigo": "DESCONTO10",
      "vezesUtilizado": 13,
      "totalDescontoAplicado": 419.94,
      "pedidosConfirmados": 10
    },
    {
      "codigo": "BONUS",
      "vezesUtilizado": 6,
      "totalDescontoAplicado": 25.00,
      "pedidosConfirmados": 5
    }
  ]
}
```

### Endpoints de E-mails de Repasse

#### 6. Listar E-mails de Repasse
```
GET /api/v1/dashboard/payout-emails?emailType=REPASSE_PIX
```

**Query Parameters:**
- `emailType` (opcional): `REPASSE_PIX` ou `REPASSE_CARD`

**Response:**
```json
[
  {
    "id": 38,
    "tipo": "REPASSE_PIX",
    "status": "SENT",
    "enviadoEm": "2025-11-18T15:07:00Z",
    "pedidoId": 1003,
    "valorRepassado": 33.47,
    "cupom": {
      "teveCupom": true,
      "codigoCupom": "BONUS",
      "valorDesconto": 5.00
    }
  },
  {
    "id": 1,
    "tipo": "REPASSE_PIX",
    "status": "SENT",
    "enviadoEm": "2025-11-15T10:34:00Z",
    "pedidoId": 962,
    "valorRepassado": 38.35,
    "cupom": {
      "teveCupom": false,
      "codigoCupom": null,
      "valorDesconto": 0.00
    }
  }
]
```

---

## 🏗️ Arquitetura Frontend Proposta

### Estrutura de Pastas

```
src/
├── types/
│   ├── order.ts
│   ├── customer.ts
│   ├── coupon.ts
│   └── payoutEmail.ts
├── services/
│   ├── api/
│   │   ├── orderApi.ts
│   │   ├── customerApi.ts
│   │   ├── couponApi.ts
│   │   └── payoutEmailApi.ts
│   └── strategies/
│       ├── OrderApiStrategy.ts
│       └── AxiosOrderApiStrategy.ts
├── hooks/
│   ├── useOrder.ts
│   ├── useOrders.ts
│   ├── useCustomerStats.ts
│   ├── useCouponStats.ts
│   └── usePayoutEmails.ts
├── components/
│   ├── orders/
│   │   ├── OrderDetails.tsx
│   │   ├── OrdersList.tsx
│   │   ├── OrderCard.tsx
│   │   └── OrderFilters.tsx
│   ├── customers/
│   │   ├── CustomerStats.tsx
│   │   └── CustomerCard.tsx
│   ├── coupons/
│   │   ├── CouponStats.tsx
│   │   └── CouponUsageCard.tsx
│   └── payoutEmails/
│       ├── PayoutEmailsList.tsx
│       └── PayoutEmailRow.tsx
└── utils/
    ├── format.ts
    └── validation.ts
```

---

## 📝 Implementação

### 1. Types/Interfaces

#### `types/order.ts`

```typescript
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

#### `types/customer.ts`

```typescript
export interface CustomerStats {
  totalPedidos: number;
  clientesUnicosEmail: number;
  clientesUnicosWhatsapp: number;
  clientesUnicosCpf: number;
}
```

#### `types/coupon.ts`

```typescript
export interface CouponStats {
  totalPedidosComCupom: number;
  totalDescontoAplicado: number;
  cuponsUtilizados: CouponUsage[];
}

export interface CouponUsage {
  codigo: string;
  vezesUtilizado: number;
  totalDescontoAplicado: number;
  pedidosConfirmados: number;
}
```

#### `types/payoutEmail.ts`

```typescript
export interface PayoutEmailWithCoupon {
  id: number;
  tipo: string; // "REPASSE_PIX" ou "REPASSE_CARD"
  status: string; // "SENT" ou "FAILED"
  enviadoEm: string; // ISO 8601
  pedidoId: number;
  valorRepassado?: number | null;
  cupom: CouponInfoPayout;
}

export interface CouponInfoPayout {
  teveCupom: boolean;
  codigoCupom?: string | null;
  valorDesconto: number;
}
```

### 2. API Services (Strategy Pattern)

#### `services/strategies/OrderApiStrategy.ts`

```typescript
import { OrderWithCustomer, OrderFilter } from '@/types/order';

/**
 * Interface para diferentes implementações de API.
 * Permite mock, diferentes ambientes, etc.
 */
export interface OrderApiStrategy {
  getOrder(orderId: number): Promise<OrderWithCustomer>;
  listOrders(filter: OrderFilter): Promise<OrderWithCustomer[]>;
  countOrders(filter: OrderFilter): Promise<number>;
}
```

#### `services/strategies/AxiosOrderApiStrategy.ts`

```typescript
import { AxiosInstance } from 'axios';
import { OrderWithCustomer, OrderFilter } from '@/types/order';
import { OrderApiStrategy } from './OrderApiStrategy';

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
```

#### `services/api/orderApi.ts`

```typescript
import axios from 'axios';
import { OrderWithCustomer, OrderFilter } from '@/types/order';
import { OrderApiStrategy, AxiosOrderApiStrategy } from '../strategies';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'https://dashboard-painel-autores-vl-26b242c59563.herokuapp.com';

// Criar instância do Axios com interceptors
const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor para adicionar token de autenticação
axiosInstance.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Interceptor para tratar erros
axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Redirecionar para login
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Factory para criar instância da API
export class OrderApiFactory {
  static create(axiosInstance?: AxiosInstance): OrderApiStrategy {
    const instance = axiosInstance || axiosInstance;
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
const orderApi = OrderApiFactory.create(axiosInstance);

export const orderDashboardApi = {
  async getOrder(orderId: number): Promise<OrderWithCustomer> {
    return orderApi.getOrder(orderId);
  },

  async listOrders(filter: OrderFilter = {}): Promise<OrderWithCustomer[]> {
    return orderApi.listOrders(filter);
  },

  async countOrders(filter: OrderFilter = {}): Promise<number> {
    return orderApi.countOrders(filter);
  },
};
```

#### `services/api/pagamentosApi.ts` (Endpoints Existentes)

```typescript
import axios from 'axios';
import { PainelPagamentosAutorDTO } from '@/types/pagamentos';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'https://dashboard-painel-autores-vl-26b242c59563.herokuapp.com';

const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

axiosInstance.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const pagamentosApi = {
  /**
   * Obtém o painel de pagamentos do autor logado.
   * O backend obtém o author_id automaticamente do usuário logado.
   */
  async getPainelPagamentos(): Promise<PainelPagamentosAutorDTO> {
    const response = await axiosInstance.get<PainelPagamentosAutorDTO>(
      '/api/v1/autor/pagamentos/painel'
    );
    return response.data;
  },
};
```

#### `services/api/emailsApi.ts` (Endpoints Existentes)

```typescript
import axios from 'axios';
import { PainelEmailsAutorDTO } from '@/types/emails';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'https://dashboard-painel-autores-vl-26b242c59563.herokuapp.com';

const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

axiosInstance.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const emailsApi = {
  /**
   * Obtém o painel de e-mails do autor logado.
   * O backend obtém o author_id automaticamente do usuário logado.
   */
  async getPainelEmails(): Promise<PainelEmailsAutorDTO> {
    const response = await axiosInstance.get<PainelEmailsAutorDTO>(
      '/api/v1/autor/emails/painel'
    );
    return response.data;
  },
};
```

#### `services/api/exportApi.ts` (Endpoints Existentes)

```typescript
import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'https://www.paineldavia.com.br';

const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  responseType: 'blob', // Para PDF e CSV
  headers: {
    'Content-Type': 'application/json',
  },
});

axiosInstance.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const exportApi = {
  /**
   * Exporta pagamentos em PDF, CSV ou JSON.
   * @param format - 'pdf', 'csv' ou 'json'
   * @param authorId - Opcional. Se não fornecido, usa o author_id do usuário logado.
   */
  async exportPayments(format: 'pdf' | 'csv' | 'json', authorId?: string): Promise<Blob> {
    const params: Record<string, string> = { format };
    if (authorId) {
      params.author_id = authorId;
    }
    
    const response = await axiosInstance.get('/api/v1/payments/export', { params });
    return response.data;
  },

  /**
   * Exporta e-mails em PDF, CSV ou JSON.
   * @param format - 'pdf', 'csv' ou 'json'
   * @param authorId - Opcional. Se não fornecido, usa o author_id do usuário logado.
   */
  async exportEmails(format: 'pdf' | 'csv' | 'json', authorId?: string): Promise<Blob> {
    const params: Record<string, string> = { format };
    if (authorId) {
      params.author_id = authorId;
    }
    
    const response = await axiosInstance.get('/api/v1/emails/export', { params });
    return response.data;
  },
};
```

#### `services/api/customerApi.ts` (A Implementar)

```typescript
import axios from 'axios';
import { CustomerStats } from '@/types/customer';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'https://dashboard-painel-autores-vl-26b242c59563.herokuapp.com';

const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

axiosInstance.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const customerApi = {
  /**
   * Obtém estatísticas de clientes.
   * O backend obtém o author_id automaticamente do usuário logado.
   * 
   * NOTA: Este endpoint ainda não existe no backend - é uma proposta futura.
   */
  async getCustomerStats(): Promise<CustomerStats> {
    const response = await axiosInstance.get<CustomerStats>(
      '/api/v1/dashboard/customers/stats'
    );
    return response.data;
  },
};
```

#### `services/api/couponApi.ts` (A Implementar)

```typescript
import axios from 'axios';
import { CouponStats } from '@/types/coupon';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'https://dashboard-painel-autores-vl-26b242c59563.herokuapp.com';

const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

axiosInstance.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const couponApi = {
  /**
   * Obtém estatísticas de cupons.
   * O backend obtém o author_id automaticamente do usuário logado.
   * 
   * NOTA: Este endpoint ainda não existe no backend - é uma proposta futura.
   */
  async getCouponStats(): Promise<CouponStats> {
    const response = await axiosInstance.get<CouponStats>(
      '/api/v1/dashboard/coupons/stats'
    );
    return response.data;
  },
};
```

#### `services/api/payoutEmailApi.ts` (A Implementar)

```typescript
import axios from 'axios';
import { PayoutEmailWithCoupon } from '@/types/payoutEmail';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'https://dashboard-painel-autores-vl-26b242c59563.herokuapp.com';

const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

axiosInstance.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const payoutEmailApi = {
  /**
   * Lista e-mails de repasse.
   * O backend obtém o author_id automaticamente do usuário logado.
   * 
   * NOTA: Este endpoint ainda não existe no backend - é uma proposta futura.
   */
  async listPayoutEmails(emailType?: string): Promise<PayoutEmailWithCoupon[]> {
    const params = emailType ? { emailType } : {};
    const response = await axiosInstance.get<PayoutEmailWithCoupon[]>(
      '/api/v1/dashboard/payout-emails',
      { params }
    );
    return response.data;
  },

  async getPayoutEmail(id: number): Promise<PayoutEmailWithCoupon> {
    const response = await axiosInstance.get<PayoutEmailWithCoupon>(
      `/api/v1/dashboard/payout-emails/${id}`
    );
    return response.data;
  },
};
```

### 3. Custom Hooks

#### `hooks/useOrder.ts`

```typescript
import { useState, useEffect } from 'react';
import { OrderWithCustomer } from '@/types/order';
import { orderDashboardApi } from '@/services/api/orderApi';

export function useOrder(orderId: number | null) {
  const [order, setOrder] = useState<OrderWithCustomer | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);

  useEffect(() => {
    if (!orderId) {
      setLoading(false);
      return;
    }

    async function fetchOrder() {
      try {
        setLoading(true);
        setError(null);
        const data = await orderDashboardApi.getOrder(orderId);
        setOrder(data);
      } catch (err) {
        setError(err instanceof Error ? err : new Error('Erro ao buscar pedido'));
      } finally {
        setLoading(false);
      }
    }

    fetchOrder();
  }, [orderId]);

  return { order, loading, error };
}
```

#### `hooks/useOrders.ts`

```typescript
import { useState, useEffect, useCallback } from 'react';
import { OrderWithCustomer, OrderFilter } from '@/types/order';
import { orderDashboardApi } from '@/services/api/orderApi';

export function useOrders(filter: OrderFilter = {}) {
  const [orders, setOrders] = useState<OrderWithCustomer[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);

  const refetch = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await orderDashboardApi.listOrders(filter);
      setOrders(data);
    } catch (err) {
      setError(err instanceof Error ? err : new Error('Erro ao buscar pedidos'));
    } finally {
      setLoading(false);
    }
  }, [filter]);

  useEffect(() => {
    refetch();
  }, [refetch]);

  return { orders, loading, error, refetch };
}
```

#### `hooks/useCustomerStats.ts`

```typescript
import { useState, useEffect } from 'react';
import { CustomerStats } from '@/types/customer';
import { customerApi } from '@/services/api/customerApi';

export function useCustomerStats() {
  const [stats, setStats] = useState<CustomerStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);

  useEffect(() => {
    async function fetchStats() {
      try {
        setLoading(true);
        setError(null);
        const data = await customerApi.getCustomerStats();
        setStats(data);
      } catch (err) {
        setError(err instanceof Error ? err : new Error('Erro ao buscar estatísticas'));
      } finally {
        setLoading(false);
      }
    }

    fetchStats();
  }, []);

  return { stats, loading, error };
}
```

#### `hooks/useCouponStats.ts`

```typescript
import { useState, useEffect } from 'react';
import { CouponStats } from '@/types/coupon';
import { couponApi } from '@/services/api/couponApi';

export function useCouponStats() {
  const [stats, setStats] = useState<CouponStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);

  useEffect(() => {
    async function fetchStats() {
      try {
        setLoading(true);
        setError(null);
        const data = await couponApi.getCouponStats();
        setStats(data);
      } catch (err) {
        setError(err instanceof Error ? err : new Error('Erro ao buscar estatísticas de cupons'));
      } finally {
        setLoading(false);
      }
    }

    fetchStats();
  }, []);

  return { stats, loading, error };
}
```

#### `hooks/usePayoutEmails.ts`

```typescript
import { useState, useEffect } from 'react';
import { PayoutEmailWithCoupon } from '@/types/payoutEmail';
import { payoutEmailApi } from '@/services/api/payoutEmailApi';

export function usePayoutEmails(emailType?: string) {
  const [emails, setEmails] = useState<PayoutEmailWithCoupon[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);

  useEffect(() => {
    async function fetchEmails() {
      try {
        setLoading(true);
        setError(null);
        const data = await payoutEmailApi.listPayoutEmails(emailType);
        setEmails(data);
      } catch (err) {
        setError(err instanceof Error ? err : new Error('Erro ao buscar e-mails de repasse'));
      } finally {
        setLoading(false);
      }
    }

    fetchEmails();
  }, [emailType]);

  return { emails, loading, error };
}
```

### 4. Utilitários

#### `utils/format.ts`

```typescript
/**
 * Formata valor monetário em Real (R$)
 */
export function formatCurrency(value: number): string {
  return new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  }).format(value);
}

/**
 * Formata data/hora
 */
export function formatDate(dateString: string): string {
  const date = new Date(dateString);
  return new Intl.DateTimeFormat('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date);
}

/**
 * Formata apenas data (sem hora)
 */
export function formatDateOnly(dateString: string): string {
  const date = new Date(dateString);
  return new Intl.DateTimeFormat('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  }).format(date);
}

/**
 * Formata CPF
 */
export function formatCPF(cpf: string): string {
  return cpf.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
}

/**
 * Formata telefone/WhatsApp
 */
export function formatPhone(phone: string): string {
  return phone.replace(/(\d{2})(\d{5})(\d{4})/, '($1) $2-$3');
}

/**
 * Gera link do WhatsApp
 */
export function getWhatsAppLink(phone: string): string {
  const cleanPhone = phone.replace(/\D/g, '');
  return `https://wa.me/${cleanPhone}`;
}
```

#### `utils/payoutEmailFormat.ts`

```typescript
import { PayoutEmailWithCoupon } from '@/types/payoutEmail';
import { formatCurrency } from './format';

/**
 * Retorna o texto a ser exibido para "Cupom Utilizado"
 * Regra: "NÃO" se não tiver cupom, "SIM" se tiver
 */
export function getCupomUtilizadoText(email: PayoutEmailWithCoupon): string {
  return email.cupom.teveCupom ? 'SIM' : 'NÃO';
}

/**
 * Retorna o valor formatado do desconto
 * Regra: "R$ 0,00" se não tiver cupom, "R$ X,XX" se tiver
 */
export function getDescontoText(email: PayoutEmailWithCoupon): string {
  if (email.cupom.teveCupom) {
    return formatCurrency(email.cupom.valorDesconto);
  }
  return 'R$ 0,00';
}

/**
 * Retorna as classes CSS apropriadas para estilização
 */
export function getCupomUtilizadoClasses(email: PayoutEmailWithCoupon): {
  textClass: string;
  badgeClass: string;
} {
  if (email.cupom.teveCupom) {
    return {
      textClass: 'cupom-sim',
      badgeClass: 'badge-success',
    };
  }
  return {
    textClass: 'cupom-nao',
    badgeClass: 'badge-secondary',
  };
}
```

### 5. Componentes React

#### `components/orders/OrderDetails.tsx`

```typescript
import React from 'react';
import { useOrder } from '@/hooks/useOrder';
import { formatCurrency, formatDate, getWhatsAppLink } from '@/utils/format';

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

      {/* Informações do Cliente */}
      <section className="customer-info">
        <h3>Cliente</h3>
        <p><strong>Nome:</strong> {order.cliente.nomeCompleto}</p>
        <p><strong>Email:</strong> {order.cliente.email}</p>
        <p>
          <strong>WhatsApp:</strong>{' '}
          <a
            href={getWhatsAppLink(order.cliente.whatsapp)}
            target="_blank"
            rel="noopener noreferrer"
          >
            {order.cliente.whatsapp}
          </a>
        </p>
        <p><strong>CPF:</strong> {order.cliente.cpf}</p>
      </section>

      {/* Endereço */}
      <section className="address-info">
        <h3>Endereço de Entrega</h3>
        <p>{order.endereco.enderecoCompleto}</p>
      </section>

      {/* Detalhes do Pedido */}
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

      {/* Cupom (se aplicado) */}
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

      {/* Itens do Pedido */}
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

#### `components/orders/OrdersList.tsx`

```typescript
import React from 'react';
import { useOrders } from '@/hooks/useOrders';
import { formatCurrency, formatDate, getWhatsAppLink } from '@/utils/format';
import { OrderFilter } from '@/types/order';

interface OrdersListProps {
  filter?: OrderFilter;
}

export function OrdersList({ filter = {} }: OrdersListProps) {
  const { orders, loading, error } = useOrders(filter);

  if (loading) return <div>Carregando pedidos...</div>;
  if (error) return <div>Erro: {error.message}</div>;

  return (
    <div className="orders-list">
      <h2>Pedidos</h2>
      <table>
        <thead>
          <tr>
            <th>Pedido #</th>
            <th>Cliente</th>
            <th>Email</th>
            <th>WhatsApp</th>
            <th>Valor</th>
            <th>Cupom</th>
            <th>Data</th>
          </tr>
        </thead>
        <tbody>
          {orders.map((order) => (
            <tr key={order.id}>
              <td>{order.numeroPedido}</td>
              <td>{order.cliente.nomeCompleto}</td>
              <td>{order.cliente.email}</td>
              <td>
                <a
                  href={getWhatsAppLink(order.cliente.whatsapp)}
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  {order.cliente.whatsapp}
                </a>
              </td>
              <td>{formatCurrency(order.pedido.valorTotal)}</td>
              <td>
                {order.cupom ? (
                  <span title={`Desconto: ${formatCurrency(order.cupom.descontoAplicado)}`}>
                    {order.cupom.codigo}
                  </span>
                ) : (
                  '-'
                )}
              </td>
              <td>{formatDate(order.pedido.dataPedido)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
```

#### `components/customers/CustomerStats.tsx`

```typescript
import React from 'react';
import { useCustomerStats } from '@/hooks/useCustomerStats';

export function CustomerStats() {
  const { stats, loading, error } = useCustomerStats();

  if (loading) return <div>Carregando estatísticas de clientes...</div>;
  if (error) return <div>Erro: {error.message}</div>;
  if (!stats) return null;

  return (
    <div className="customer-stats">
      <h2>Estatísticas de Clientes</h2>
      <ul>
        <li>Total de Pedidos Confirmados: {stats.totalPedidos}</li>
        <li>Clientes Únicos (Email): {stats.clientesUnicosEmail}</li>
        <li>Clientes Únicos (WhatsApp): {stats.clientesUnicosWhatsapp}</li>
        <li>Clientes Únicos (CPF): {stats.clientesUnicosCpf}</li>
      </ul>
    </div>
  );
}
```

#### `components/coupons/CouponStats.tsx`

```typescript
import React from 'react';
import { useCouponStats } from '@/hooks/useCouponStats';
import { formatCurrency } from '@/utils/format';

export function CouponStats() {
  const { stats, loading, error } = useCouponStats();

  if (loading) return <div>Carregando estatísticas de cupons...</div>;
  if (error) return <div>Erro: {error.message}</div>;
  if (!stats) return null;

  return (
    <div className="coupon-stats">
      <h2>Estatísticas de Cupons</h2>
      <ul>
        <li>Total de Pedidos com Cupom: {stats.totalPedidosComCupom}</li>
        <li>Total de Desconto Aplicado: {formatCurrency(stats.totalDescontoAplicado)}</li>
        <li>
          <strong>Cupons Utilizados:</strong>
          <ul>
            {stats.cuponsUtilizados.map((cupom) => (
              <li key={cupom.codigo}>
                <strong>{cupom.codigo}</strong>: {cupom.vezesUtilizado} vezes (
                {cupom.pedidosConfirmados} confirmados) - Desconto total:{' '}
                {formatCurrency(cupom.totalDescontoAplicado)}
              </li>
            ))}
          </ul>
        </li>
      </ul>
    </div>
  );
}
```

#### `components/payoutEmails/PayoutEmailsList.tsx`

```typescript
import React from 'react';
import { usePayoutEmails } from '@/hooks/usePayoutEmails';
import { formatCurrency, formatDate } from '@/utils/format';
import { getCupomUtilizadoText, getDescontoText, getCupomUtilizadoClasses } from '@/utils/payoutEmailFormat';
import { CheckCircle, XCircle } from 'lucide-react';

export function PayoutEmailsList() {
  const { emails, loading, error } = usePayoutEmails('REPASSE_PIX');

  if (loading) return <div>Carregando e-mails de repasse...</div>;
  if (error) return <div>Erro: {error.message}</div>;

  return (
    <div className="payout-emails-list">
      <h2>E-mails de Repasse de PIX</h2>
      <table className="table">
        <thead>
          <tr>
            <th>Tipo</th>
            <th>Status</th>
            <th>Enviado Em</th>
            <th>Pedido ID</th>
            <th>Valor Repassado</th>
            <th>Cupom Utilizado</th>
            <th>Desconto</th>
          </tr>
        </thead>
        <tbody>
          {emails.map((email) => {
            const cupomClasses = getCupomUtilizadoClasses(email);
            return (
              <tr key={email.id}>
                <td>{email.tipo}</td>
                <td>
                  {email.status === 'SENT' ? (
                    <span className="status-sent">
                      <CheckCircle className="icon-check" />
                      SENT
                    </span>
                  ) : (
                    <span className="status-failed">
                      <XCircle className="icon-cancel" />
                      FAILED
                    </span>
                  )}
                </td>
                <td>{formatDate(email.enviadoEm)}</td>
                <td>{email.pedidoId}</td>
                <td>
                  {email.valorRepassado ? formatCurrency(email.valorRepassado) : '-'}
                </td>
                <td>
                  <span className={cupomClasses.textClass}>
                    {getCupomUtilizadoText(email)}
                  </span>
                </td>
                <td>
                  {email.cupom.teveCupom ? (
                    <strong className="desconto-valor">
                      {getDescontoText(email)}
                    </strong>
                  ) : (
                    <span className="desconto-zero">R$ 0,00</span>
                  )}
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
```

---

## 🎨 Regras de Exibição para E-mails de Repasse

### Campo "Cupom Utilizado"

- **Se NÃO tiver cupom** (`teveCupom = false`):
  - Exibir: **"NÃO"** (texto simples, sem destaque)
  - Classe CSS: `cupom-nao` (cor cinza)

- **Se TIVER cupom** (`teveCupom = true`):
  - Exibir: **"SIM"** (texto em negrito ou com destaque visual)
  - Classe CSS: `cupom-sim` (cor verde)

### Campo "Desconto"

- **Se NÃO tiver cupom** (`valorDesconto = 0`):
  - Exibir: **"R$ 0,00"** (texto simples, sem destaque)
  - Classe CSS: `desconto-zero` (cor cinza)

- **Se TIVER cupom** (`valorDesconto > 0`):
  - Exibir: **"R$ X,XX"** (valor formatado com destaque)
  - Classe CSS: `desconto-valor` (cor azul ou negrito)

---

## ✅ Checklist de Implementação

### Estrutura Base:
- [ ] Criar estrutura de pastas (`types/`, `services/`, `hooks/`, `components/`)
- [ ] Configurar variável de ambiente `VITE_API_URL`
- [ ] Configurar interceptors do Axios (token, erros)

### Types:
- [ ] Criar `types/order.ts`
- [ ] Criar `types/customer.ts`
- [ ] Criar `types/coupon.ts`
- [ ] Criar `types/payoutEmail.ts`

### Services:
- [ ] Criar `services/strategies/OrderApiStrategy.ts`
- [ ] Criar `services/strategies/AxiosOrderApiStrategy.ts`
- [ ] Criar `services/api/orderApi.ts`
- [ ] Criar `services/api/customerApi.ts`
- [ ] Criar `services/api/couponApi.ts`
- [ ] Criar `services/api/payoutEmailApi.ts`

### Hooks:
- [ ] Criar `hooks/useOrder.ts`
- [ ] Criar `hooks/useOrders.ts`
- [ ] Criar `hooks/useCustomerStats.ts`
- [ ] Criar `hooks/useCouponStats.ts`
- [ ] Criar `hooks/usePayoutEmails.ts`

### Utils:
- [ ] Criar `utils/format.ts`
- [ ] Criar `utils/payoutEmailFormat.ts`

### Componentes:
- [ ] Criar `components/orders/OrderDetails.tsx`
- [ ] Criar `components/orders/OrdersList.tsx`
- [ ] Criar `components/customers/CustomerStats.tsx`
- [ ] Criar `components/coupons/CouponStats.tsx`
- [ ] Criar `components/payoutEmails/PayoutEmailsList.tsx`

### Integração:
- [ ] Integrar componentes nas páginas do dashboard
- [ ] Adicionar tratamento de erros
- [ ] Adicionar loading states
- [ ] Adicionar estilos CSS

---

## 🔧 Variáveis de Ambiente

Adicionar no `.env`:

```env
VITE_API_URL=https://dashboard-painel-autores-vl-26b242c59563.herokuapp.com
```

**OU** usar a URL do seu deploy do backend.

---

## ✅ Checklist de Implementação

### Passo 1: Obter e Armazenar authorId (CRÍTICO!)
- [ ] Criar `services/api/profileApi.ts` para buscar perfil
- [ ] Criar `contexts/AuthorContext.tsx` para gerenciar `authorId`
- [ ] Chamar `getProfile()` após login
- [ ] Armazenar `authorId` no estado da aplicação
- [ ] Validar se `authorId` existe antes de permitir acesso às funcionalidades
- [ ] Mostrar mensagem de erro se `authorId` for `null`

### Passo 2: Endpoints Existentes (Implementar Agora)
- [ ] Criar `services/api/pagamentosApi.ts` (endpoint `/api/v1/autor/pagamentos/painel`)
- [ ] Criar `services/api/emailsApi.ts` (endpoint `/api/v1/autor/emails/painel`)
- [ ] Criar `services/api/exportApi.ts` (endpoints `/api/v1/payments/export` e `/api/v1/emails/export`)
- [ ] Criar hooks: `usePagamentos`, `useEmails`, `useExport`
- [ ] Criar componentes para exibir dados

### Passo 3: Endpoints Futuros (Aguardar Backend)
- [ ] Aguardar implementação dos endpoints de pedidos no backend
- [ ] Implementar quando backend estiver pronto

### Passo 4: Integração
- [ ] Integrar `AuthorProvider` no `App.tsx`
- [ ] Usar `useAuthor()` nos componentes que precisam de `authorId`
- [ ] Adicionar tratamento de erros
- [ ] Adicionar loading states
- [ ] Adicionar estilos CSS

---

**Última atualização:** Novembro 2025

