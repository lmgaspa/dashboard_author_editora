import { Component } from '@angular/core';

// tudo vem de ../../blocks/ agora
import { TopControlsComponent } from '../../blocks/top-controls';
import { KpiCardComponent } from '../../blocks/kpi-card.component'; // seu arquivo ficou com ".component.ts"
import { DeliveryStatusComponent } from '../../blocks/delivery-status';

import { MapBrPlaceholderComponent } from '../../blocks/map-br-placeholder';
import { ProductStatusSkeletonComponent } from '../../blocks/product-status-skeleton';
import { PaymentMethodsSkeletonComponent } from '../../blocks/payment-methods-skeleton';
import { NotificationsSkeletonComponent } from '../../blocks/notifications-skeleton';
import { OrdersSoldSkeletonComponent } from '../../blocks/orders-sold-skeleton';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [
    TopControlsComponent,
    KpiCardComponent,
    DeliveryStatusComponent,
    MapBrPlaceholderComponent,
    ProductStatusSkeletonComponent,
    PaymentMethodsSkeletonComponent,
    OrdersSoldSkeletonComponent,
    NotificationsSkeletonComponent,
  ],
  template: `
    <div class="min-h-screen bg-brand-50 px-4 md:px-8 py-6 space-y-6">
      <!-- Topo -->
      <app-top-controls />

      <!-- Primeira linha: KPIs + Status da Entrega -->
      <div class="grid gap-4 md:grid-cols-4">
        <app-kpi-card title="Receita (Mês)"></app-kpi-card>
        <app-kpi-card title="Pedidos"></app-kpi-card>
        <app-kpi-card title="Cancelados"></app-kpi-card>
        <app-delivery-status></app-delivery-status>
      </div>

      <!-- Meio: 3 rows à esquerda + coluna direita -->
      <div class="grid gap-4 lg:grid-cols-3">
        <div class="lg:col-span-2 space-y-4">
          <app-map-br-placeholder />
          <app-product-status-skeleton />
          <app-payment-methods-skeleton />
        </div>

        <!-- Coluna direita -->
        <div class="space-y-4">
          <!-- Se você ainda tiver Canais de Aquisição como placeholder, pode manter acima -->
          <app-notifications-skeleton />
        </div>
      </div>

      <!-- Rodapé: Pedidos Vendidos (largura total) -->
      <app-orders-sold-skeleton />
    </div>
  `,
})
export class DashboardPage {}
