import { Component } from '@angular/core';
import { BlockComponent } from 'src/app/shared/block.component';

@Component({
  selector: 'app-payment-methods-skeleton',
  standalone: true,
  imports: [BlockComponent],
  template: `
    <app-block title="Formas de Pagamento">
      <div class="grid gap-6 md:grid-cols-2">
        <!-- Donut / gráfico fake -->
        <div class="flex items-center justify-center">
          <div class="relative size-40 rounded-full border-[10px] border-brand-200">
            <div class="absolute inset-0 m-6 rounded-full bg-white"></div>
          </div>
        </div>

        <!-- Legenda / barras fake -->
        <div class="space-y-4">
          <div class="flex items-center gap-3">
            <span class="inline-block size-3 rounded-full bg-brand-400"></span>
            <span class="h-3 w-28 rounded bg-brand-100"></span>
            <span class="ml-auto h-3 w-12 rounded bg-brand-100"></span>
          </div>
          <div class="flex items-center gap-3">
            <span class="inline-block size-3 rounded-full bg-brand-500"></span>
            <span class="h-3 w-24 rounded bg-brand-100"></span>
            <span class="ml-auto h-3 w-12 rounded bg-brand-100"></span>
          </div>
          <div class="flex items-center gap-3">
            <span class="inline-block size-3 rounded-full bg-brand-300"></span>
            <span class="h-3 w-20 rounded bg-brand-100"></span>
            <span class="ml-auto h-3 w-12 rounded bg-brand-100"></span>
          </div>
        </div>
      </div>
    </app-block>
  `,
})
export class PaymentMethodsSkeletonComponent {}
