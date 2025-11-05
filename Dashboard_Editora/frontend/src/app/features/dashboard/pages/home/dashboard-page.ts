import { Component, signal, effect } from '@angular/core';
import { CommonModule } from '@angular/common';

/* blocks */
import { TopControlsComponent } from '../../blocks/top-controls/top-controls';
import { KpiCardComponent } from '../../blocks/kpi-card/kpi-card-component';
import { DeliveryStatusComponent } from '../../blocks/delivery-status/delivery-status-component';
import { MapBrComponent } from '../../blocks/map-br/map-br-component';
import { SalesFunilComponent } from '../../blocks/sales-funil/sales-funil-component';
import { PaymentMethodsComponent } from '../../blocks/payment-methods/payment-methods-component';
import { NotificationBellComponent } from '../../blocks/notifications/notification-bell.component';
import { OrdersSoldComponent } from '../../blocks/orders-sold/orders-sold-component';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [
    CommonModule,
    TopControlsComponent,
    KpiCardComponent,
    DeliveryStatusComponent,
    MapBrComponent,
    SalesFunilComponent,
    PaymentMethodsComponent,
    OrdersSoldComponent,
    NotificationBellComponent,
  ],
  template: `
    <div class="min-h-screen bg-[color:var(--bg)]">
      <div class="mx-auto w-full [max-width:100vw] px-3 md:px-6 py-6 space-y-6">
        <!-- Header -->
        <div class="flex flex-wrap items-center gap-4">
          <h1 class="text-2xl md:text-3xl font-bold text-[color:var(--ink-1)] min-w-0 grow">
            Painel do Autor
          </h1>

          <div class="flex items-center gap-3 shrink-0">
            <!-- Botão tema -->
            <button
              type="button"
              class="inline-flex items-center justify-center size-9 rounded-lg
                     bg-[color:var(--surface)]/80 hover:bg-[color:var(--surface)]
                     text-[color:var(--ink-1)] shadow-sm ring-1 ring-[color:var(--border-1)]"
              (click)="toggleTheme()"
              [attr.aria-label]="isDark() ? 'Ativar tema claro' : 'Ativar tema escuro'"
              title="Alternar tema"
            >
              @if (!isDark()) {
              <svg
                width="18"
                height="18"
                viewBox="0 0 24 24"
                fill="currentColor"
                aria-hidden="true"
              >
                <path
                  d="M12 4.5a1 1 0 0 1 1 1V7a1 1 0 1 1-2 0V5.5a1 1 0 0 1 1-1Zm0 11a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7Zm7.5-3.5a1 1 0 0 1-1 1H16a1 1 0 1 1 0-2h2.5a1 1 0 0 1 1 1ZM12 17a1 1 0 0 1 1 1v1.5a1 1 0 1 1-2 0V18a1 1 0 0 1 1-1ZM7 12a1 1 0 0 1-1 1H3.5a1 1 0 1 1 0-2H6a1 1 0 0 1 1 1Zm9.96 5.46a1 1 0 0 1 0 1.41l-1.06 1.06a1 1 0 0 1-1.41-1.41l1.06-1.06a1 1 0 0 1 1.41 0Zm0-11.32a1 1 0 0 1-1.41 0L14.49 4.1a1 1 0 1 1 1.41-1.41l1.06 1.06a1 1 0 0 1 0 1.41ZM7.55 18.46a1 1 0 0 1 0 1.41l-1.06 1.06A1 1 0 1 1 5.08 19.5l1.06-1.06a1 1 0 0 1 1.41 0ZM6.61 2.69A1 1 0 0 1 8 4.1L6.94 5.16A1 1 0 1 1 5.53 3.75L6.6 2.69Z"
                />
              </svg>
              } @else {
              <svg
                width="18"
                height="18"
                viewBox="0 0 24 24"
                fill="currentColor"
                aria-hidden="true"
              >
                <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 1 0 21 12.79z" />
              </svg>
              }
            </button>

            <div class="inline-flex transform scale-150 origin-center">
              <app-notification-bell></app-notification-bell>
            </div>

            <button
              type="button"
              class="inline-flex items-center justify-center size-9 rounded-lg
                     bg-[color:var(--surface)]/80 hover:bg-[color:var(--surface)]
                     text-[color:var(--ink-1)] shadow-sm ring-1 ring-[color:var(--border-1)]"
              aria-label="Abrir configurações do autor"
              title="Perfil / Configurações"
            >
              <svg
                width="18"
                height="18"
                viewBox="0 0 24 24"
                fill="currentColor"
                aria-hidden="true"
              >
                <path
                  d="M12 12a5 5 0 1 0-5-5 5 5 0 0 0 5 5Zm0 2c-4.42 0-8 2.24-8 5v1h16v-1c0-2.76-3.58-5-8-5Z"
                />
              </svg>
            </button>
          </div>
        </div>

        <!-- KPIs -->
        <div class="grid gap-4 grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 items-stretch">
          <app-kpi-card class="min-w-0" title="Receita (Mês)" kpiKey="revenue"></app-kpi-card>
          <app-kpi-card class="min-w-0" title="Pedidos" kpiKey="orders"></app-kpi-card>
          <app-kpi-card class="min-w-0" title="Cancelados" kpiKey="canceled"></app-kpi-card>
          <app-delivery-status class="min-w-0"></app-delivery-status>
        </div>

        <!-- ROW 1: Mapa + Métricas de acesso -->
        <div class="grid gap-6 md:gap-8 items-stretch grid-cols-1 md:grid-cols-2">
          <!-- Mapa ocupa toda a coluna, sem limitar a 760px quando está lado a lado -->
          <div class="min-w-0">
            <app-map-br class="block min-w-0" [minHeight]="360"></app-map-br>
          </div>
          <div class="min-w-0 h-full">
            <app-top-controls class="block h-full"></app-top-controls>
          </div>
        </div>

        <!-- ROW 2: Funil de Vendas + Formas de Pagamento -->
        <div class="grid gap-6 md:gap-8 items-stretch grid-cols-1 md:grid-cols-2">
          <app-sales-funil class="min-w-0" [blockMinHeight]="360" [chartMaxHeight]="260">
          </app-sales-funil>

          <app-payment-methods class="min-w-0" [blockMinHeight]="360" [chartMaxHeight]="280">
          </app-payment-methods>
        </div>

        <!-- ROW 3: Produtos Vendidos (sozinho, full-width) -->
        <app-orders-sold-component class="block min-w-0"></app-orders-sold-component>
      </div>
    </div>
  `,
})
export class DashboardPage {
  isDark = signal<boolean>(false);

  constructor() {
    const saved = localStorage.getItem('theme');
    this.isDark.set(saved === 'dark');
    effect(() => {
      const dark = this.isDark();
      document.documentElement.classList.toggle('dark', dark);
      localStorage.setItem('theme', dark ? 'dark' : 'light');
    });
  }

  toggleTheme() {
    this.isDark.update((v) => !v);
  }
}
