import { Component, input } from '@angular/core';

@Component({
  selector: 'app-kpi-card',
  standalone: true,
  template: `
    <div class="rounded-card bg-white shadow-sm border border-slate-200 p-4">
      <div class="text-sm text-slate-600">{{ title() }}</div>
      <div class="mt-3 h-7 rounded bg-brand-100"></div>
      <div class="mt-3 h-2 w-24 rounded bg-brand-100"></div>
    </div>
  `,
})
export class KpiCardComponent {
  title = input<string>('KPI');
}
