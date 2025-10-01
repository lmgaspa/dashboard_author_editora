import { Component } from '@angular/core';
import { BlockComponent } from 'src/app/shared/block.component';

@Component({
  selector: 'app-orders-sold-skeleton',
  standalone: true,
  imports: [BlockComponent],
  template: `
    <app-block>
      <div block-actions class="text-sm text-slate-600">CSV · PDF</div>

      <div class="mb-3 text-lg font-semibold text-slate-700">Pedidos Vendidos</div>

      <!-- cabeçalho da tabela -->
      <div class="hidden md:grid grid-cols-7 gap-3 px-3 py-2 rounded bg-brand-100/60 text-sm text-slate-700">
        <div>#</div><div>Data</div><div>Cliente</div><div>Livro</div><div>Status</div><div>Valor</div><div>Canal</div>
      </div>

      <!-- linhas fake -->
      <div class="mt-2 space-y-2">
        <div class="grid grid-cols-1 md:grid-cols-7 gap-3 items-center px-3 py-3 rounded border border-slate-200 bg-white">
          <div class="h-3 w-10 bg-brand-100 rounded"></div>
          <div class="h-3 w-16 bg-brand-100 rounded"></div>
          <div class="h-3 w-28 bg-brand-100 rounded"></div>
          <div class="h-3 w-32 bg-brand-100 rounded"></div>
          <div class="h-3 w-20 bg-brand-100 rounded"></div>
          <div class="h-3 w-16 bg-brand-100 rounded"></div>
          <div class="h-3 w-12 bg-brand-100 rounded"></div>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-7 gap-3 items-center px-3 py-3 rounded border border-slate-200 bg-white">
          <div class="h-3 w-10 bg-brand-100 rounded"></div>
          <div class="h-3 w-16 bg-brand-100 rounded"></div>
          <div class="h-3 w-28 bg-brand-100 rounded"></div>
          <div class="h-3 w-32 bg-brand-100 rounded"></div>
          <div class="h-3 w-20 bg-brand-100 rounded"></div>
          <div class="h-3 w-16 bg-brand-100 rounded"></div>
          <div class="h-3 w-12 bg-brand-100 rounded"></div>
        </div>
      </div>
    </app-block>
  `,
})
export class OrdersSoldSkeletonComponent {}
