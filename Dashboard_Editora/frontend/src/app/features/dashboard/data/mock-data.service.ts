import { Injectable } from '@angular/core';
import { interval, map, scan, startWith } from 'rxjs';

export type KpiKey = 'revenue' | 'orders' | 'canceled';
export type BrRegion = 'N' | 'NE' | 'CO' | 'SE' | 'S';

@Injectable({ providedIn: 'root' })
export class MockDataService {
  private seeds: Record<KpiKey, number> = {
    revenue: 42380,
    orders: 1284,
    canceled: 37,
  };

  // === KPIs (já usados) ===
  kpi$(key: KpiKey) {
    const base = this.seeds[key];
    const tick$ = interval(4000).pipe(startWith(0));
    return tick$.pipe(
      map(() => {
        const noise = (Math.random() - 0.5);
        const scale = key === 'revenue' ? 300 : key === 'orders' ? 20 : 3;
        return Math.round(noise * scale);
      }),
      scan((acc, delta) => {
        let next = acc.value + delta;
        if (key === 'canceled') next = Math.max(0, next);
        const change = next - acc.value;
        const pct = acc.value === 0 ? 0 : (change / acc.value) * 100;
        return { value: next, change, pct };
      }, { value: base, change: 0, pct: 0 }),
    );
  }

  // === Mapa do Brasil por REGIÃO (mock) ===
  // Gera vendas por região com pequenas oscilações
  brRegions$() {
    const seed: Record<BrRegion, number> = {
      N: 400,   // Norte
      NE: 900,  // Nordeste
      CO: 600,  // Centro-Oeste
      SE: 2200, // Sudeste
      S: 1100,  // Sul
    };

    const tick$ = interval(3500).pipe(startWith(0));
    return tick$.pipe(
      map(() => {
        const next: Record<BrRegion, number> = { ...seed };
        (Object.keys(seed) as BrRegion[]).forEach(r => {
          const noise = (Math.random() - 0.5);
          const scale = r === 'SE' ? 120 : r === 'S' ? 80 : 60;
          next[r] = Math.max(0, Math.round(next[r] + noise * scale));
          // persiste para o próximo ciclo
          seed[r] = next[r];
        });
        const total = Object.values(next).reduce((a, b) => a + b, 0);
        return { regions: next, total };
      })
    );
  }
}
