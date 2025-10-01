import { Component } from '@angular/core';

@Component({
  selector: 'app-top-controls',
  standalone: true,
  template: `
    <div class="flex items-center justify-between">
      <h1 class="text-2xl md:text-3xl font-bold text-slate-700">Painel do Autor</h1>

      <!-- Filtros (skeletons clicáveis, mas sem lógica ainda) -->
      <div class="hidden md:flex items-center gap-2">
        <button
          type="button"
          class="rounded-full bg-brand-100 hover:bg-brand-200 px-3 py-1 text-sm text-slate-700 transition"
          aria-pressed="true">
          Métricas
        </button>
        <button
          type="button"
          class="rounded-full bg-brand-100 hover:bg-brand-200 px-3 py-1 text-sm text-slate-700 transition">
          Vendas
        </button>
        <div class="relative">
          <button
            type="button"
            class="rounded-md bg-white border border-slate-200 px-3 py-1.5 text-sm text-slate-700 shadow-sm hover:bg-slate-50">
            •••
          </button>
        </div>
      </div>
    </div>
  `,
})
export class TopControlsComponent {}
