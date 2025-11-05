import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-top-controls',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
  <!-- Card completo que preenche a coluna -->
  <section class="app-card h-full flex flex-col p-4 space-y-6">
    <!-- Linha de título + chips (mesma linha, com wrap elegante) -->
    <div class="flex items-start md:items-center gap-2 md:gap-3 flex-wrap">
      <h1 class="text-xl md:text-2xl lg:text-2xl font-bold grow min-w-0 text-[color:var(--ink-1)]">
        Métricas de Vendas e Acesso ao Site
      </h1>

      <div class="inline-flex items-center gap-2 shrink-0">
        <button
          type="button"
          class="rounded-full px-3 py-1 text-sm transition
                 bg-[color:var(--surface-muted)] hover:bg-[color:var(--surface-2)]
                 text-[color:var(--ink-2)] ring-1 ring-[color:var(--border-1)]">
          Vendas
        </button>
        <button
          type="button"
          class="rounded-full px-3 py-1 text-sm transition
                 bg-[color:var(--surface-muted)] hover:bg-[color:var(--surface-2)]
                 text-[color:var(--ink-2)] ring-1 ring-[color:var(--border-1)]">
          Acesso ao site
        </button>

        <div class="relative">
          <button
            type="button"
            class="rounded-md px-3 py-1.5 text-sm shadow-sm transition
                   bg-[color:var(--surface)] hover:bg-[color:var(--surface-2)]
                   text-[color:var(--ink-2)] ring-1 ring-[color:var(--border-1)]">
            •••
          </button>
        </div>
      </div>
    </div>

    <!-- Filtro de calendário -->
    <form
      class="flex flex-wrap items-end gap-3"
      (submit)="$event.preventDefault(); applyFilter()"
      aria-label="Filtro de calendário por intervalo de datas"
    >
      <div class="flex flex-col">
        <label for="start" class="text-xs text-[color:var(--ink-3)] mb-1">De</label>
        <input
          id="start"
          type="date"
          [(ngModel)]="startDate"
          name="startDate"
          class="rounded-md px-3 py-1.5 text-sm shadow-sm
                 bg-[color:var(--surface)] text-[color:var(--ink-2)]
                 border border-[color:var(--border-1)]
                 focus:outline-none focus:ring-2 focus:ring-[color:var(--focus)]"
        />
      </div>
      <div class="flex flex-col">
        <label for="end" class="text-xs text-[color:var(--ink-3)] mb-1">Até</label>
        <input
          id="end"
          type="date"
          [(ngModel)]="endDate"
          name="endDate"
          class="rounded-md px-3 py-1.5 text-sm shadow-sm
                 bg-[color:var(--surface)] text-[color:var(--ink-2)]
                 border border-[color:var(--border-1)]
                 focus:outline-none focus:ring-2 focus:ring-[color:var(--focus)]"
        />
      </div>

      <button
        type="submit"
        class="rounded-md text-white text-sm px-4 py-2 transition
               bg-[color:var(--brand)] hover:bg-[color:var(--brand-contrast)]">
        Aplicar
      </button>

      <button
        type="button"
        (click)="randomizeData()"
        class="rounded-md text-sm px-3 py-2 transition
               bg-[color:var(--surface)] text-[color:var(--ink-2)]
               border border-[color:var(--border-1)] hover:bg-[color:var(--surface-2)]">
        Simular
      </button>
    </form>

    <!-- Barras 00-23h -->
    <!-- flex-1 para ocupar o resto da altura do card; ajuste min-h para igualar ao mapa -->
    <section
      aria-labelledby="horas-title"
      class="rounded-xl border p-4 bg-[color:var(--surface)] border-[color:var(--border-1)] flex-1 min-h-[420px]">
      <div class="flex items-center justify-between mb-3">
        <h2 id="horas-title" class="text-sm font-semibold text-[color:var(--ink-1)]">
          Atividade por hora (00–23)
        </h2>
        <span class="text-xs text-[color:var(--ink-3)]">Total: {{ total }} · Máx/h: {{ max }}</span>
      </div>

      <div class="relative h-full">
        <!-- grade horizontal leve -->
        <div class="absolute inset-0 -z-10">
          <div class="h-full w-full grid" style="grid-template-rows: repeat(4, 1fr)">
            <div class="border-t border-dashed border-[color:var(--border-1)]"></div>
            <div class="border-t border-dashed border-[color:var(--border-1)]"></div>
            <div class="border-t border-dashed border-[color:var(--border-1)]"></div>
            <div class="border-t border-[color:var(--border-1)]"></div>
          </div>
        </div>

        <!-- barras (24 colunas) -->
        <div
          class="h-40 md:h-[260px] grid gap-[6px]"
          style="grid-template-columns: repeat(24, minmax(0, 1fr));"
          role="img"
          aria-label="Histograma por hora"
        >
          <ng-container *ngFor="let v of hourCounts; let h = index">
            <div class="flex items-end justify-center min-w-0">
              <div
                class="w-full rounded-t-md transition"
                [style.backgroundColor]="'var(--brand)'"
                [style.height.%]="max ? (v / max) * 100 : 0"
                [attr.title]="label(h) + ': ' + v"
                aria-hidden="true"
              ></div>
            </div>
          </ng-container>
        </div>

        <!-- Eixo X (00..23) a cada 3 horas -->
        <div class="mt-2 grid" style="grid-template-columns: repeat(24, minmax(0, 1fr));">
          <ng-container *ngFor="let _ of hourCounts; let h = index">
            <div
              class="text-[10px] md:text-xs text-[color:var(--ink-3)] text-center tabular-nums"
              [class.opacity-0]="h % 3 !== 0"
            >
              {{ h | number : '2.0-0' }}
            </div>
          </ng-container>
        </div>
      </div>
    </section>
  </section>
  `,
})
export class TopControlsComponent {
  // Intervalo de datas (ISO yyyy-MM-dd)
  startDate = this.iso(new Date(Date.now() - 6 * 24 * 60 * 60 * 1000)); // hoje-6d
  endDate = this.iso(new Date()); // hoje

  // Série com 24 posições (00–23)
  hourCounts: number[] = [
    4, 2, 1, 0, 0, 1, 3, 8, 12, 18, 24, 16, 14, 10, 12, 20, 22, 18, 15, 12, 9, 7, 5, 3,
  ];

  get max(): number {
    return this.hourCounts.reduce((m, v) => (v > m ? v : m), 0);
  }

  get total(): number {
    return this.hourCounts.reduce((s, v) => s + v, 0);
  }

  applyFilter() {
    // Exemplo (mock): zera madrugada e dá mais peso no horário comercial
    const boosted = this.hourCounts.map((v, h) =>
      h >= 9 && h <= 18 ? Math.round(v * 1.1) : Math.round(v * 0.9)
    );
    this.hourCounts = boosted;
  }

  randomizeData() {
    // Gera dados de exemplo com pico no final da tarde
    const peak = 17;
    this.hourCounts = Array.from({ length: 24 }, (_, h) => {
      const base = Math.max(0, 30 - Math.abs(h - peak) * 4);
      const noise = Math.floor(Math.random() * 5);
      return Math.max(0, base + noise);
    });
  }

  label(h: number): string {
    const hh = h.toString().padStart(2, '0');
    return `${hh}:00`;
    }

  private iso(d: Date): string {
    const y = d.getFullYear();
    const m = (d.getMonth() + 1).toString().padStart(2, '0');
    const day = d.getDate().toString().padStart(2, '0');
    return `${y}-${m}-${day}`;
  }
}
