# 📋 Guia de Implementação Angular - Dados de Clientes e Pedidos

## 🎯 Objetivo

Este guia adapta a arquitetura proposta para **Angular**, usando **Services**, **Signals**, **Observables** e **Componentes Standalone** ao invés de React hooks.

---

## ⚠️ CRÍTICO: author_id é OBRIGATÓRIO

**🚨 ATENÇÃO: O `author_id` é o coração do sistema! Sem ele, NADA funciona!**

### Como Funciona no Angular

1. **O `authorId` é obtido automaticamente** após login via `AuthService.getUserProfile()`
2. **Armazenado em Signal reativo** (`AuthService.currentUser()`)
3. **Backend identifica automaticamente** do token JWT na maioria dos endpoints
4. **Alguns endpoints podem aceitar `author_id` como parâmetro opcional** (para admins)

### Passo 1: Obter o authorId (OBRIGATÓRIO!)

**Após login**, o `AuthService` já carrega o perfil automaticamente:

```typescript
// src/app/core/services/auth.service.ts
getUserProfile(): Observable<User> {
  return this.http.get<any>(`${this.API_URL}/api/v1/user/profile`).pipe(
    map((response: any) => {
      const mappedUser: User = {
        // ...
        authorId: response.author_id || response.authorId || null,
        // ...
      };
      return mappedUser;
    }),
    tap((user: User) => {
      this._currentUser.set(user); // ← Armazenado em Signal
      localStorage.setItem('currentUser', JSON.stringify(user));
    })
  );
}
```

### Passo 2: Usar o authorId

**Na maioria dos casos, o frontend NÃO precisa passar `author_id` explicitamente:**

```typescript
// ✅ CORRETO: Backend identifica automaticamente
this.orderService.listOrders('CONFIRMED').subscribe(...);
this.emailService.getPainelEmails().subscribe(...);
```

**Exceções (exportação):**

```typescript
// ✅ Para exportação (opcional)
this.exportService.exportPayments({
  format: 'pdf',
  authorId: this.authService.currentUser()?.authorId // Opcional
});
```

---

## 📊 Estrutura de Arquivos (Angular)

```
src/app/
├── core/
│   ├── models/
│   │   ├── order-dashboard.model.ts          ✅ JÁ CRIADO
│   │   ├── payout-email-dashboard.model.ts  ✅ JÁ CRIADO
│   │   └── email.model.ts                   ✅ JÁ CRIADO
│   ├── services/
│   │   ├── order-dashboard.service.ts       ✅ JÁ CRIADO
│   │   ├── payout-email-dashboard.service.ts ✅ JÁ CRIADO
│   │   ├── email.service.ts                 ✅ JÁ CRIADO
│   │   └── export.service.ts                ✅ JÁ CRIADO
│   └── utils/
│       ├── charge.utils.ts                  ✅ JÁ CRIADO
│       └── payout-email-format.utils.ts     ✅ JÁ CRIADO
└── features/
    └── user/
        └── pages/
            ├── orders/                      ⚠️ A CRIAR
            │   ├── orders-page.component.ts
            │   └── orders-page.component.html
            ├── order-detail/                 ⚠️ A CRIAR
            │   ├── order-detail-page.component.ts
            │   └── order-detail-page.component.html
            └── customer-stats/               ⚠️ A CRIAR
                ├── customer-stats-page.component.ts
                └── customer-stats-page.component.html
```

---

## 🛠️ Implementação Angular

### 1. Modelos (Já Criados ✅)

**`src/app/core/models/order-dashboard.model.ts`** - ✅ Já existe
**`src/app/core/models/payout-email-dashboard.model.ts`** - ✅ Já existe
**`src/app/core/models/email.model.ts`** - ✅ Já existe (atualizado)

### 2. Serviços (Já Criados ✅)

**`src/app/core/services/order-dashboard.service.ts`** - ✅ Já existe
**`src/app/core/services/payout-email-dashboard.service.ts`** - ✅ Já existe
**`src/app/core/services/email.service.ts`** - ✅ Já existe
**`src/app/core/services/export.service.ts`** - ✅ Já existe

**Adicionar método `countOrders` ao `OrderDashboardService`:**

```typescript
/**
 * Conta total de pedidos com filtros
 * 
 * O backend identifica automaticamente o `author_id` do usuário logado via token JWT.
 * 
 * @param status Status do pedido (opcional)
 * @returns Observable com o total de pedidos
 */
countOrders(status?: string): Observable<number> {
  let params = new HttpParams();
  if (status) {
    params = params.set('status', status);
  }
  return this.http.get<number>(`${this.DASHBOARD_API}/count`, { params });
}
```

### 3. Utilitários (Já Criados ✅)

**`src/app/core/utils/charge.utils.ts`** - ✅ Já existe (tem `formatCurrency`, `formatDate`, etc.)
**`src/app/core/utils/payout-email-format.utils.ts`** - ✅ Já existe

**Adicionar utilitários adicionais se necessário:**

```typescript
// src/app/core/utils/order.utils.ts
import { formatCurrency } from './charge.utils';

/**
 * Gera link do WhatsApp
 */
export function getWhatsAppLink(phone: string): string {
  const cleanPhone = phone.replace(/\D/g, '');
  return `https://wa.me/${cleanPhone}`;
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
  // Remove caracteres não numéricos
  const clean = phone.replace(/\D/g, '');
  
  // Formata baseado no tamanho
  if (clean.length === 11) {
    return clean.replace(/(\d{2})(\d{5})(\d{4})/, '($1) $2-$3');
  } else if (clean.length === 10) {
    return clean.replace(/(\d{2})(\d{4})(\d{4})/, '($1) $2-$3');
  }
  return phone;
}
```

### 4. Componentes Angular (A Criar)

#### `src/app/features/user/pages/orders/orders-page.component.ts`

```typescript
import { Component, signal, inject, OnInit } from '@angular/core';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { RouterModule } from '@angular/router';
import { OrderDashboardService } from '@/app/core/services/order-dashboard.service';
import { OrderWithCustomer } from '@/app/core/models/order-dashboard.model';
import { formatCurrency, formatDate } from '@/app/core/utils/charge.utils';
import { getWhatsAppLink } from '@/app/core/utils/order.utils';

@Component({
  selector: 'app-orders-page',
  standalone: true,
  imports: [CommonModule, CurrencyPipe, DatePipe, RouterModule],
  templateUrl: './orders-page.component.html',
  styles: []
})
export class OrdersPageComponent implements OnInit {
  private readonly orderService = inject(OrderDashboardService);

  readonly orders = signal<OrderWithCustomer[]>([]);
  readonly loading = signal<boolean>(false);
  readonly error = signal<string | null>(null);
  readonly statusFilter = signal<string>('CONFIRMED');

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.loading.set(true);
    this.error.set(null);

    this.orderService.listOrders(this.statusFilter()).subscribe({
      next: (data) => {
        this.orders.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Erro ao carregar pedidos:', err);
        this.error.set(err.error?.message || 'Erro ao carregar pedidos');
        this.loading.set(false);
      }
    });
  }

  onStatusFilterChange(status: string): void {
    this.statusFilter.set(status);
    this.loadOrders();
  }

  formatCurrency = formatCurrency;
  formatDate = formatDate;
  getWhatsAppLink = getWhatsAppLink;
}
```

#### `src/app/features/user/pages/orders/orders-page.component.html`

```html
<div class="min-h-screen bg-[color:var(--bg)] p-4 sm:p-6">
  <div class="max-w-7xl mx-auto">
    <!-- Header -->
    <div class="mb-6">
      <h1 class="text-2xl sm:text-3xl font-bold text-[color:var(--ink-1)] mb-4">
        Pedidos
      </h1>
      
      <!-- Filtros -->
      <div class="flex flex-wrap gap-2 mb-4">
        <button
          *ngFor="let status of ['CONFIRMED', 'WAITING', 'NEW']"
          (click)="onStatusFilterChange(status)"
          [class.bg-blue-500]="statusFilter() === status"
          [class.text-white]="statusFilter() === status"
          [class.bg-gray-200]="statusFilter() !== status"
          [class.text-gray-700]="statusFilter() !== status"
          class="px-4 py-2 rounded-lg text-base font-medium transition-colors"
        >
          {{ status }}
        </button>
      </div>
    </div>

    <!-- Loading -->
    @if (loading()) {
      <div class="text-center py-12">
        <span class="material-icons text-4xl text-gray-400 animate-spin">refresh</span>
        <p class="text-gray-500 mt-4">Carregando pedidos...</p>
      </div>
    }

    <!-- Error -->
    @if (error() && !loading()) {
      <div class="bg-red-50 border border-red-200 rounded-lg p-4 text-red-700">
        {{ error() }}
      </div>
    }

    <!-- Orders List -->
    @if (!loading() && !error() && orders().length > 0) {
      <div class="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
        <table class="w-full">
          <thead class="bg-gray-50">
            <tr>
              <th class="px-4 py-3 text-left text-sm font-semibold text-gray-700">Pedido #</th>
              <th class="px-4 py-3 text-left text-sm font-semibold text-gray-700">Cliente</th>
              <th class="px-4 py-3 text-left text-sm font-semibold text-gray-700">Email</th>
              <th class="px-4 py-3 text-left text-sm font-semibold text-gray-700">WhatsApp</th>
              <th class="px-4 py-3 text-right text-sm font-semibold text-gray-700">Valor</th>
              <th class="px-4 py-3 text-center text-sm font-semibold text-gray-700">Cupom</th>
              <th class="px-4 py-3 text-left text-sm font-semibold text-gray-700">Data</th>
              <th class="px-4 py-3 text-center text-sm font-semibold text-gray-700">Ações</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-gray-200">
            @for (order of orders(); track order.id) {
              <tr class="hover:bg-gray-50">
                <td class="px-4 py-3 text-sm text-gray-900">
                  <a [routerLink]="['/user/orders', order.id]" class="text-blue-600 hover:underline">
                    #{{ order.numeroPedido }}
                  </a>
                </td>
                <td class="px-4 py-3 text-sm text-gray-900">{{ order.cliente.nomeCompleto }}</td>
                <td class="px-4 py-3 text-sm text-gray-600">{{ order.cliente.email }}</td>
                <td class="px-4 py-3 text-sm">
                  <a
                    [href]="getWhatsAppLink(order.cliente.whatsapp)"
                    target="_blank"
                    rel="noopener noreferrer"
                    class="text-green-600 hover:underline"
                  >
                    {{ order.cliente.whatsapp }}
                  </a>
                </td>
                <td class="px-4 py-3 text-sm text-right text-gray-900 font-semibold">
                  {{ order.pedido.valorTotal | currency:'BRL':'symbol':'1.2-2' }}
                </td>
                <td class="px-4 py-3 text-center">
                  @if (order.cupom) {
                    <span class="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-green-100 text-green-800">
                      {{ order.cupom.codigo }}
                    </span>
                  } @else {
                    <span class="text-gray-400">-</span>
                  }
                </td>
                <td class="px-4 py-3 text-sm text-gray-600">
                  {{ formatDate(order.pedido.dataPedido) }}
                </td>
                <td class="px-4 py-3 text-center">
                  <a
                    [routerLink]="['/user/orders', order.id]"
                    class="text-blue-600 hover:underline text-sm"
                  >
                    Ver Detalhes
                  </a>
                </td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    }

    <!-- Empty State -->
    @if (!loading() && !error() && orders().length === 0) {
      <div class="text-center py-12">
        <span class="material-icons text-6xl text-gray-300 mb-4">shopping_cart</span>
        <p class="text-gray-500 text-lg">Nenhum pedido encontrado</p>
      </div>
    }
  </div>
</div>
```

#### `src/app/features/user/pages/order-detail/order-detail-page.component.ts`

```typescript
import { Component, signal, inject, OnInit } from '@angular/core';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { OrderDashboardService } from '@/app/core/services/order-dashboard.service';
import { OrderWithCustomer } from '@/app/core/models/order-dashboard.model';
import { formatCurrency, formatDate } from '@/app/core/utils/charge.utils';
import { getWhatsAppLink, formatCPF } from '@/app/core/utils/order.utils';

@Component({
  selector: 'app-order-detail-page',
  standalone: true,
  imports: [CommonModule, CurrencyPipe, DatePipe, RouterModule],
  templateUrl: './order-detail-page.component.html',
  styles: []
})
export class OrderDetailPageComponent implements OnInit {
  private readonly orderService = inject(OrderDashboardService);
  private readonly route = inject(ActivatedRoute);

  readonly order = signal<OrderWithCustomer | null>(null);
  readonly loading = signal<boolean>(false);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    const orderId = this.route.snapshot.paramMap.get('id');
    if (orderId) {
      this.loadOrder(Number(orderId));
    }
  }

  loadOrder(orderId: number): void {
    this.loading.set(true);
    this.error.set(null);

    this.orderService.getOrder(orderId).subscribe({
      next: (data) => {
        this.order.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Erro ao carregar pedido:', err);
        this.error.set(err.error?.message || 'Erro ao carregar pedido');
        this.loading.set(false);
      }
    });
  }

  formatCurrency = formatCurrency;
  formatDate = formatDate;
  getWhatsAppLink = getWhatsAppLink;
  formatCPF = formatCPF;
}
```

#### `src/app/features/user/pages/order-detail/order-detail-page.component.html`

```html
<div class="min-h-screen bg-[color:var(--bg)] p-4 sm:p-6">
  <div class="max-w-4xl mx-auto">
    <!-- Loading -->
    @if (loading()) {
      <div class="text-center py-12">
        <span class="material-icons text-4xl text-gray-400 animate-spin">refresh</span>
        <p class="text-gray-500 mt-4">Carregando pedido...</p>
      </div>
    }

    <!-- Error -->
    @if (error() && !loading()) {
      <div class="bg-red-50 border border-red-200 rounded-lg p-4 text-red-700 mb-6">
        {{ error() }}
      </div>
    }

    <!-- Order Details -->
    @if (order() && !loading()) {
      <div class="space-y-6">
        <!-- Header -->
        <div class="flex items-center justify-between">
          <h1 class="text-2xl sm:text-3xl font-bold text-[color:var(--ink-1)]">
            Pedido #{{ order()!.numeroPedido }}
          </h1>
          <a routerLink="/user/orders" class="text-blue-600 hover:underline">
            ← Voltar para lista
          </a>
        </div>

        <!-- Cliente -->
        <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <h2 class="text-xl font-semibold text-[color:var(--ink-1)] mb-4">Cliente</h2>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <p class="text-sm text-gray-600">Nome Completo</p>
              <p class="text-base font-medium text-gray-900">{{ order()!.cliente.nomeCompleto }}</p>
            </div>
            <div>
              <p class="text-sm text-gray-600">Email</p>
              <p class="text-base font-medium text-gray-900">{{ order()!.cliente.email }}</p>
            </div>
            <div>
              <p class="text-sm text-gray-600">WhatsApp</p>
              <a
                [href]="getWhatsAppLink(order()!.cliente.whatsapp)"
                target="_blank"
                rel="noopener noreferrer"
                class="text-base font-medium text-green-600 hover:underline"
              >
                {{ order()!.cliente.whatsapp }}
              </a>
            </div>
            <div>
              <p class="text-sm text-gray-600">CPF</p>
              <p class="text-base font-medium text-gray-900">{{ formatCPF(order()!.cliente.cpf) }}</p>
            </div>
          </div>
        </div>

        <!-- Endereço -->
        <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <h2 class="text-xl font-semibold text-[color:var(--ink-1)] mb-4">Endereço de Entrega</h2>
          <p class="text-base text-gray-900">{{ order()!.endereco.enderecoCompleto }}</p>
        </div>

        <!-- Detalhes do Pedido -->
        <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <h2 class="text-xl font-semibold text-[color:var(--ink-1)] mb-4">Detalhes do Pedido</h2>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <p class="text-sm text-gray-600">Status</p>
              <p class="text-base font-medium text-gray-900">{{ order()!.pedido.status }}</p>
            </div>
            <div>
              <p class="text-sm text-gray-600">Método de Pagamento</p>
              <p class="text-base font-medium text-gray-900">{{ order()!.pedido.metodoPagamento }}</p>
            </div>
            <div>
              <p class="text-sm text-gray-600">Valor Total</p>
              <p class="text-base font-semibold text-gray-900">
                {{ order()!.pedido.valorTotal | currency:'BRL':'symbol':'1.2-2' }}
              </p>
            </div>
            <div>
              <p class="text-sm text-gray-600">Data do Pedido</p>
              <p class="text-base font-medium text-gray-900">
                {{ formatDate(order()!.pedido.dataPedido) }}
              </p>
            </div>
            @if (order()!.pedido.pago && order()!.pedido.dataPagamento) {
              <div>
                <p class="text-sm text-gray-600">Data do Pagamento</p>
                <p class="text-base font-medium text-gray-900">
                  {{ formatDate(order()!.pedido.dataPagamento!) }}
                </p>
              </div>
            }
          </div>
        </div>

        <!-- Cupom -->
        @if (order()!.cupom) {
          <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <h2 class="text-xl font-semibold text-[color:var(--ink-1)] mb-4">Cupom Aplicado</h2>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <p class="text-sm text-gray-600">Código</p>
                <p class="text-base font-medium text-gray-900">{{ order()!.cupom!.codigo }}</p>
              </div>
              @if (order()!.cupom!.nomeCupom) {
                <div>
                  <p class="text-sm text-gray-600">Nome</p>
                  <p class="text-base font-medium text-gray-900">{{ order()!.cupom!.nomeCupom }}</p>
                </div>
              }
              @if (order()!.cupom!.valorOriginal) {
                <div>
                  <p class="text-sm text-gray-600">Valor Original</p>
                  <p class="text-base font-medium text-gray-900">
                    {{ order()!.cupom!.valorOriginal | currency:'BRL':'symbol':'1.2-2' }}
                  </p>
                </div>
              }
              <div>
                <p class="text-sm text-gray-600">Desconto</p>
                <p class="text-base font-semibold text-green-600">
                  {{ order()!.cupom!.descontoAplicado | currency:'BRL':'symbol':'1.2-2' }}
                </p>
              </div>
            </div>
          </div>
        }

        <!-- Itens do Pedido -->
        <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <h2 class="text-xl font-semibold text-[color:var(--ink-1)] mb-4">Itens do Pedido</h2>
          <table class="w-full">
            <thead class="bg-gray-50">
              <tr>
                <th class="px-4 py-3 text-left text-sm font-semibold text-gray-700">Livro</th>
                <th class="px-4 py-3 text-center text-sm font-semibold text-gray-700">Quantidade</th>
                <th class="px-4 py-3 text-right text-sm font-semibold text-gray-700">Preço Unitário</th>
                <th class="px-4 py-3 text-right text-sm font-semibold text-gray-700">Subtotal</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-200">
              @for (item of order()!.items; track item.bookId) {
                <tr>
                  <td class="px-4 py-3 text-sm text-gray-900">{{ item.titulo }}</td>
                  <td class="px-4 py-3 text-sm text-center text-gray-600">{{ item.quantidade }}</td>
                  <td class="px-4 py-3 text-sm text-right text-gray-600">
                    {{ item.preco | currency:'BRL':'symbol':'1.2-2' }}
                  </td>
                  <td class="px-4 py-3 text-sm text-right font-semibold text-gray-900">
                    {{ (item.preco * item.quantidade) | currency:'BRL':'symbol':'1.2-2' }}
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      </div>
    }
  </div>
</div>
```

#### `src/app/features/user/pages/customer-stats/customer-stats-page.component.ts`

```typescript
import { Component, signal, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OrderDashboardService } from '@/app/core/services/order-dashboard.service';
import { CustomerStats, CouponStats } from '@/app/core/models/order-dashboard.model';
import { formatCurrency } from '@/app/core/utils/charge.utils';

@Component({
  selector: 'app-customer-stats-page',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './customer-stats-page.component.html',
  styles: []
})
export class CustomerStatsPageComponent implements OnInit {
  private readonly orderService = inject(OrderDashboardService);

  readonly customerStats = signal<CustomerStats | null>(null);
  readonly couponStats = signal<CouponStats | null>(null);
  readonly loading = signal<boolean>(false);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.loadStats();
  }

  loadStats(): void {
    this.loading.set(true);
    this.error.set(null);

    // Carregar estatísticas de clientes
    this.orderService.getCustomerStats().subscribe({
      next: (data) => {
        this.customerStats.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Erro ao carregar estatísticas de clientes:', err);
        this.error.set(err.error?.message || 'Erro ao carregar estatísticas');
        this.loading.set(false);
      }
    });

    // Carregar estatísticas de cupons
    this.orderService.getCouponStats().subscribe({
      next: (data) => {
        this.couponStats.set(data);
      },
      error: (err) => {
        console.error('Erro ao carregar estatísticas de cupons:', err);
      }
    });
  }

  formatCurrency = formatCurrency;
}
```

#### `src/app/features/user/pages/customer-stats/customer-stats-page.component.html`

```html
<div class="min-h-screen bg-[color:var(--bg)] p-4 sm:p-6">
  <div class="max-w-7xl mx-auto">
    <h1 class="text-2xl sm:text-3xl font-bold text-[color:var(--ink-1)] mb-6">
      Estatísticas
    </h1>

    <!-- Loading -->
    @if (loading()) {
      <div class="text-center py-12">
        <span class="material-icons text-4xl text-gray-400 animate-spin">refresh</span>
        <p class="text-gray-500 mt-4">Carregando estatísticas...</p>
      </div>
    }

    <!-- Error -->
    @if (error() && !loading()) {
      <div class="bg-red-50 border border-red-200 rounded-lg p-4 text-red-700 mb-6">
        {{ error() }}
      </div>
    }

    <!-- Stats -->
    @if (!loading() && !error()) {
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <!-- Estatísticas de Clientes -->
        @if (customerStats()) {
          <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <h2 class="text-xl font-semibold text-[color:var(--ink-1)] mb-4">
              Estatísticas de Clientes
            </h2>
            <ul class="space-y-3">
              <li class="flex justify-between">
                <span class="text-gray-600">Total de Pedidos Confirmados</span>
                <span class="font-semibold text-gray-900">{{ customerStats()!.totalPedidos }}</span>
              </li>
              <li class="flex justify-between">
                <span class="text-gray-600">Clientes Únicos (Email)</span>
                <span class="font-semibold text-gray-900">{{ customerStats()!.clientesUnicosEmail }}</span>
              </li>
              <li class="flex justify-between">
                <span class="text-gray-600">Clientes Únicos (WhatsApp)</span>
                <span class="font-semibold text-gray-900">{{ customerStats()!.clientesUnicosWhatsapp }}</span>
              </li>
              <li class="flex justify-between">
                <span class="text-gray-600">Clientes Únicos (CPF)</span>
                <span class="font-semibold text-gray-900">{{ customerStats()!.clientesUnicosCpf }}</span>
              </li>
            </ul>
          </div>
        }

        <!-- Estatísticas de Cupons -->
        @if (couponStats()) {
          <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
            <h2 class="text-xl font-semibold text-[color:var(--ink-1)] mb-4">
              Estatísticas de Cupons
            </h2>
            <ul class="space-y-3">
              <li class="flex justify-between">
                <span class="text-gray-600">Total de Pedidos com Cupom</span>
                <span class="font-semibold text-gray-900">{{ couponStats()!.totalPedidosComCupom }}</span>
              </li>
              <li class="flex justify-between">
                <span class="text-gray-600">Total de Desconto Aplicado</span>
                <span class="font-semibold text-green-600">
                  {{ formatCurrency(couponStats()!.totalDescontoAplicado) }}
                </span>
              </li>
            </ul>
            
            @if (couponStats()!.cuponsUtilizados.length > 0) {
              <div class="mt-6">
                <h3 class="text-lg font-semibold text-[color:var(--ink-1)] mb-3">
                  Cupons Utilizados
                </h3>
                <ul class="space-y-2">
                  @for (cupom of couponStats()!.cuponsUtilizados; track cupom.codigo) {
                    <li class="bg-gray-50 rounded-lg p-3">
                      <div class="flex justify-between items-start mb-2">
                        <span class="font-semibold text-gray-900">{{ cupom.codigo }}</span>
                        <span class="text-sm text-gray-600">
                          {{ cupom.vezesUtilizado }} vezes
                        </span>
                      </div>
                      <div class="flex justify-between text-sm">
                        <span class="text-gray-600">
                          {{ cupom.pedidosConfirmados }} confirmados
                        </span>
                        <span class="font-semibold text-green-600">
                          Desconto: {{ formatCurrency(cupom.totalDescontoAplicado) }}
                        </span>
                      </div>
                    </li>
                  }
                </ul>
              </div>
            }
          </div>
        }
      </div>
    }
  </div>
</div>
```

### 5. Rotas (Adicionar ao `app.routes.ts`)

```typescript
{
  path: 'user/orders',
  component: OrdersPageComponent,
  canActivate: [authGuard]
},
{
  path: 'user/orders/:id',
  component: OrderDetailPageComponent,
  canActivate: [authGuard]
},
{
  path: 'user/customer-stats',
  component: CustomerStatsPageComponent,
  canActivate: [authGuard]
}
```

### 6. Menu (Adicionar ao `menu.service.ts`)

```typescript
// Adicionar item de menu para "Pedidos"
{
  label: 'Pedidos',
  icon: 'shopping_cart',
  route: '/user/orders',
  roles: ['USER', 'ADMIN']
}
```

---

## ✅ Checklist de Implementação Angular

### Modelos:
- [x] `order-dashboard.model.ts` - ✅ Já existe
- [x] `payout-email-dashboard.model.ts` - ✅ Já existe
- [x] `email.model.ts` - ✅ Já existe (atualizado)

### Serviços:
- [x] `order-dashboard.service.ts` - ✅ Já existe
- [ ] Adicionar método `countOrders()` - ⚠️ A adicionar
- [x] `payout-email-dashboard.service.ts` - ✅ Já existe
- [x] `email.service.ts` - ✅ Já existe
- [x] `export.service.ts` - ✅ Já existe

### Utilitários:
- [x] `charge.utils.ts` - ✅ Já existe
- [x] `payout-email-format.utils.ts` - ✅ Já existe
- [ ] Criar `order.utils.ts` (getWhatsAppLink, formatCPF, formatPhone) - ⚠️ A criar

### Componentes:
- [ ] Criar `orders-page.component.ts` e `.html` - ⚠️ A criar
- [ ] Criar `order-detail-page.component.ts` e `.html` - ⚠️ A criar
- [ ] Criar `customer-stats-page.component.ts` e `.html` - ⚠️ A criar

### Rotas:
- [ ] Adicionar rotas no `app.routes.ts` - ⚠️ A adicionar

### Menu:
- [ ] Adicionar item "Pedidos" no menu - ⚠️ A adicionar

---

## 🔧 Diferenças: React vs Angular

### React (Documentação Original)
- **Hooks:** `useState`, `useEffect`, `useCallback`
- **API:** Axios com interceptors
- **Estado:** `useState` e `useEffect`

### Angular (Implementação Real)
- **Services:** `@Injectable` com `inject()`
- **Estado:** `signal()` e `computed()` (Angular Signals)
- **HTTP:** `HttpClient` com interceptors
- **Componentes:** Standalone components com `@Component`

---

**Última atualização:** Novembro 2025

