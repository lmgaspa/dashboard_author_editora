import { Component } from '@angular/core';
import { BlockComponent } from 'src/app/shared/block.component';

@Component({
  selector: 'app-product-status-skeleton',
  standalone: true,
  imports: [BlockComponent],
  template: `
    <app-block title="Status dos Produtos">
      <div class="grid gap-6 md:grid-cols-2">
        <!-- Coluna esqueda: barras -->
        <div class="space-y-5">
          <div class="flex items-center justify-between text-sm text-slate-700">
            <span>Já Enviado</span><span class="text-slate-500">—</span>
          </div>
          <div class="h-2 bg-brand-100 rounded"></div>

          <div class="flex items-center justify-between text-sm text-slate-700">
            <span>Em Trânsito</span><span class="text-slate-500">—</span>
          </div>
          <div class="h-2 bg-brand-100 rounded"></div>

          <div class="flex items-center justify-between text-sm text-slate-700">
            <span>Arquivado</span><span class="text-slate-500">—</span>
          </div>
          <div class="h-2 bg-brand-100 rounded"></div>
        </div>

        <!-- Coluna direita: donut fake -->
        <div class="flex items-center justify-center">
          <div class="relative size-40 rounded-full border-[10px] border-brand-200">
            <div class="absolute inset-0 m-6 rounded-full bg-white"></div>
          </div>
        </div>
      </div>
    </app-block>
  `,
})
export class ProductStatusSkeletonComponent {}
