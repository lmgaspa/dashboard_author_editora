import { Component } from '@angular/core';

@Component({
  selector: 'app-delivery-status',
  standalone: true,
  template: `
    <section class="rounded-card bg-white shadow-sm border border-slate-200 p-4">
      <div class="text-sm text-slate-600 mb-3">Status da Entrega</div>
      <div class="space-y-3">
        <div class="flex items-center gap-3">
          <span class="w-28 h-2 rounded bg-brand-100"></span>
          <span class="flex-1 h-2 rounded bg-brand-100"></span>
        </div>
        <div class="flex items-center gap-3">
          <span class="w-28 h-2 rounded bg-brand-100"></span>
          <span class="flex-1 h-2 rounded bg-brand-100"></span>
        </div>
        <div class="flex items-center gap-3">
          <span class="w-28 h-2 rounded bg-brand-100"></span>
          <span class="flex-1 h-2 rounded bg-brand-100"></span>
        </div>
        <div class="flex items-center gap-3">
          <span class="w-28 h-2 rounded bg-brand-100"></span>
          <span class="flex-1 h-2 rounded bg-brand-100"></span>
        </div>
      </div>
    </section>
  `,
})
export class DeliveryStatusComponent {}
