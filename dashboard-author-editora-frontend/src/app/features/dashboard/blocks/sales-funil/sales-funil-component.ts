import { Component, DestroyRef, ElementRef, ViewChild, effect, signal } from '@angular/core';
import { CommonModule, DecimalPipe } from '@angular/common';
import { BlockComponent } from '@/app/shared/block.component';
import * as echarts from 'echarts';

type Step = { name: string; value: number; note?: string };

@Component({
  selector: 'app-sales-funil',
  standalone: true,
  imports: [BlockComponent, CommonModule, DecimalPipe],
  template: `
    <app-block title="Funil de Vendas">
      <div class="grid gap-6 md:grid-cols-2 items-center">
        <div class="flex items-center justify-center">
          <div #chart class="size-56 md:size-64" role="img" aria-label="Etapas do funil de vendas"></div>
        </div>
        <ol class="space-y-3" aria-label="Resumo das etapas do funil">
          <li class="flex items-center gap-3" *ngFor="let s of steps(); let i = index">
            <span class="inline-block size-2 rounded-full" [ngClass]="dotClasses[i]"></span>
            <span class="text-sm text-foreground/80">{{ s.name }}</span>
            <span class="ml-auto text-xs tabular-nums text-foreground/70">{{ s.value | number:'1.0-0' }}</span>
            <span class="text-xs text-foreground/60" *ngIf="i>0">(−{{ drop(i) }}% drop-off)</span>
          </li>
        </ol>
      </div>
    </app-block>
  `,
})
export class SalesFunilComponent {
  @ViewChild('chart', { static: true }) chartEl!: ElementRef<HTMLDivElement>;

  // mock inicial (altere livre)
  readonly steps = signal<Step[]>([
    { name: 'Visitas', value: 10000 },
    { name: 'Carrinho', value: 7000, note: '-30%' },
    { name: 'Checkout', value: 6300, note: '-10%' },
    { name: 'Pagamento Aprovado', value: 4200 },
  ]);

  readonly dotClasses = ['bg-brand-400','bg-brand-500','bg-brand-300','bg-brand-600'];

  private chart?: echarts.ECharts;
  private ro?: ResizeObserver;

  constructor(private destroyRef: DestroyRef) {
    effect(() => this.render(this.steps()));
    this.destroyRef.onDestroy(() => this.dispose());
  }

  ngAfterViewInit() {
    this.chart = echarts.init(this.chartEl.nativeElement);
    this.ro = new ResizeObserver(() => this.chart?.resize());
    this.ro.observe(this.chartEl.nativeElement);
    this.render(this.steps());
  }

  drop(i: number): number {
    if (i <= 0) return 0;
    const prev = this.steps()[i - 1].value || 1;
    const cur = this.steps()[i].value;
    return Math.round(((prev - cur) / prev) * 100);
  }

  private render(steps: Step[]) {
    if (!this.chart) return;
    const option: echarts.EChartsOption = {
      tooltip: { trigger: 'item', formatter: '{b}: {c}' },
      series: [
        {
          type: 'funnel',
          left: '5%',
          top: 10,
          bottom: 10,
          width: '90%',
          min: 0,
          max: Math.max(...steps.map(s => s.value)),
          sort: 'descending',
          gap: 6,
          label: { show: true, position: 'inside', color: '#fff', formatter: '{b}' },
          itemStyle: {
            borderColor: 'var(--color-background, #fff)',
            borderWidth: 2,
          },
          data: steps.map((s, i) => ({
            name: s.name,
            value: s.value,
            itemStyle: { color: getComputedStyle(document.documentElement)
              .getPropertyValue(['--brand-300','--brand-500','--brand-400','--brand-600'][i] as any) || undefined },
          })),
        },
      ],
    };
    this.chart.setOption(option, true);
  }

  private dispose() {
    this.ro?.disconnect();
    this.chart?.dispose();
  }
}
