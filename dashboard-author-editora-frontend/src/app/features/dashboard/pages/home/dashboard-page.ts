import { Component, signal, effect } from '@angular/core';
import { CommonModule } from '@angular/common';

// tudo vem de ../../blocks/
import { TopControlsComponent } from '../../blocks/top-controls/top-controls';
import { KpiCardComponent } from '../../blocks/kpi-card/kpi-card-component';
import { DeliveryStatusComponent } from '../../blocks/delivery-status/delivery-status-component';
import { MapBrComponent } from '../../blocks/map-br/map-br-component';
import { SalesFunilComponent } from '../../blocks/sales-funil/sales-funil-component';
import { PaymentMethodsComponent } from '../../blocks/payment-methods/payment-methods-component';
import { NotificationsComponent } from '../../blocks/notification/notifications-component';
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
    NotificationsComponent,
  ],
  template: `
    <div class="min-h-screen bg-brand-50 px-4 md:px-8 py-6 space-y-6">
      <!-- Header -->
      <div class="flex items-center justify-between gap-4">
        <h1 class="text-2xl md:text-3xl font-bold text-slate-700 dark:text-slate-100">
          Painel do Autor
        </h1>

        <div class="flex items-center gap-2">
          <!-- Botão tema: Sol/Lua -->
          <button
            type="button"
            class="inline-flex items-center justify-center size-9 rounded-lg bg-white/80 hover:bg-white text-slate-700 dark:bg-slate-800 dark:text-slate-100 shadow-sm ring-1 ring-black/5"
            (click)="toggleTheme()"
            [attr.aria-label]="isDark() ? 'Ativar tema claro' : 'Ativar tema escuro'"
            title="Alternar tema"
          >
            <!-- Sol -->
            <svg
              *ngIf="!isDark()"
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
            <!-- Lua -->
            <svg
              *ngIf="isDark()"
              width="18"
              height="18"
              viewBox="0 0 24 24"
              fill="currentColor"
              aria-hidden="true"
            >
              <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 1 0 21 12.79z" />
            </svg>
          </button>

          <!-- Persona (placeholder) -->
          <button
            type="button"
            class="inline-flex items-center justify-center size-9 rounded-lg bg-white/80 hover:bg-white text-slate-700 dark:bg-slate-800 dark:text-slate-100 shadow-sm ring-1 ring-black/5"
            aria-label="Abrir configurações do autor"
            title="Perfil / Configurações"
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor" aria-hidden="true">
              <path
                d="M12 12a5 5 0 1 0-5-5 5 5 0 0 0 5 5Zm0 2c-4.42 0-8 2.24-8 5v1h16v-1c0-2.76-3.58-5-8-5Z"
              />
            </svg>
          </button>
        </div>
      </div>

      <!-- KPIs + Status -->
      <div class="grid gap-4 md:grid-cols-4 items-stretch">
        <app-kpi-card title="Receita (Mês)" kpiKey="revenue" />
        <app-kpi-card title="Pedidos" kpiKey="orders" />
        <app-kpi-card title="Cancelados" kpiKey="canceled" />
        <app-delivery-status />
      </div>

      <!-- Mapa com largura limitada pelo container -->
      <div class="flex justify-center items-center gap-12">
        <div class="w-full max-w-[720px]">
          <app-map-br [minHeight]="360"></app-map-br>
        </div>
        <div>
          <div class="grid gap-6 md:grid-cols-2">
            <app-sales-funil />
            <app-payment-methods />
          </div>
        </div>

        <!-- Coluna direita -->
        <div class="space-y-4">
          <app-top-controls />
        </div>
      </div>

      <!-- Rodapé -->
      <app-orders-sold-component />
    </div>
  `,
})
export class DashboardPage {
  // Tema: salva em localStorage e aplica em <html class="dark"> (Tailwind darkMode:'class')
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
