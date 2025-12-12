import { Component, input, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MockDataService, KpiKey } from '../../data/mock-data.service';
import { Subscription } from 'rxjs';
import { SparklineComponent } from './sparkline.component';

@Component({
  selector: 'app-kpi-card',
  standalone: true,
  imports: [CommonModule, SparklineComponent],
  templateUrl: './kpi-card-component.html',
  // escala do card inteiro por tamanho (opcional)
  host: { '[class]': 'hostClass' },
})
export class KpiCardComponent {
  title = input<string>('KPI');
  kpiKey = input<KpiKey>('revenue');
  size = input<'sm' | 'md' | 'lg' | 'xl' | 'xxl'>('lg');

  value = signal<number>(0);
  change = signal<number>(0);
  pct = signal<number>(0);

  private sub?: Subscription;

  // histórico bruto (ex.: amostras ao longo do tempo)
  private readonly MAX_POINTS = 32;
  history = signal<number[]>([]);
  // série agregada em 4 semanas (média de blocos)
  weekly = signal<number[]>([0, 0, 0, 0]);

  constructor(private mock: MockDataService) {}

  get hostClass(): string {
    const map = { sm: 'text-base', md: 'text-lg', lg: 'text-xl', xl: 'text-2xl', xxl: 'text-3xl' } as const;
    return map[this.size()];
  }

  get sparkColor(): string {
    const k = this.kpiKey();
    if (k === 'revenue') return '#10b981';    // verde
    if (k === 'orders')  return '#0ea5e9';    // azul
    if (k === 'canceled') return '#ef4444';   // vermelho
    return '#2997f0';
  }

  ngOnInit() {
    this.sub = this.mock.kpi$(this.kpiKey()).subscribe(s => {
      this.value.set(s.value);
      this.change.set(s.change);
      this.pct.set(Number(s.pct.toFixed(1)));

      // atualiza buffer
      this.history.update(arr => {
        const next = [...arr, s.value];
        if (next.length > this.MAX_POINTS) next.splice(0, next.length - this.MAX_POINTS);
        return next;
      });

      // agrega em 4 semanas (média por bloco sequencial)
      this.weekly.set(chunkAverage(this.history(), 4));
    });
  }

  ngOnDestroy() { this.sub?.unsubscribe(); }
  get isUp() { return this.change() >= 0; }
}

/** Divide data em N blocos sequenciais e retorna a média de cada bloco. */
function chunkAverage(data: number[], blocks: number): number[] {
  if (!data.length) return Array(blocks).fill(0);
  const n = data.length;
  const size = Math.ceil(n / blocks);
  const out: number[] = [];
  for (let i = 0; i < blocks; i++) {
    const start = i * size;
    const end = Math.min(start + size, n);
    if (start >= n) { out.push(data[n - 1] ?? 0); continue; }
    let sum = 0; let count = 0;
    for (let j = start; j < end; j++) { sum += data[j]; count++; }
    out.push(count ? sum / count : (data[n - 1] ?? 0));
  }
  return out;
}
