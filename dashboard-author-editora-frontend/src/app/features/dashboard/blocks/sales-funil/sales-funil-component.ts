import { Component, DestroyRef, ElementRef, ViewChild, effect, signal, input } from '@angular/core';
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
      <div class="grid gap-6 md:grid-cols-2 items-stretch"
           [style.minHeight.px]="blockMinHeight()">
        <div class="flex items-center justify-center h-full">
          <div #chart class="w-full" [style.height.px]="chartMaxHeight()"
               role="img" aria-label="Etapas do funil de vendas"></div>
        </div>

        <ol class="space-y-3 md:h-full md:overflow-auto" aria-label="Resumo das etapas do funil">
          @for (s of steps(); let i = $index; track i) {
            <li class="flex items-center gap-3">
              <span class="inline-block size-2 rounded-full"
                    [style.background-color]="palette[i % palette.length]"></span>
              <span class="text-sm text-foreground/80">{{ s.name }}</span>
              <span class="ml-auto text-xs tabular-nums text-foreground/70">
                {{ s.value | number:'1.0-0' }}
              </span>
              @if (i > 0) {
                <span class="text-xs text-foreground/60">(−{{ drop(i) }}% drop-off)</span>
              }
            </li>
          }
        </ol>
      </div>
    </app-block>
  `,
})
export class SalesFunilComponent {
  @ViewChild('chart', { static: true }) chartEl!: ElementRef<HTMLDivElement>;

  /** Altura mínima do CARD (não do funil) */
  blockMinHeight = input<number>(360);
  /** Altura máxima do funil (limita o gráfico) */
  chartMaxHeight = input<number>(260);

  readonly steps = signal<Step[]>([
    { name: 'Visitas', value: 10000 },
    { name: 'Carrinho', value: 7000 },
    { name: 'Checkout', value: 6300 },
    { name: 'Pagamento Aprovado', value: 4200 },
  ]);

  readonly palette = ['#638cf7', '#9ad95a', '#5a6b85', '#f5a524'];

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
    const prev = this.steps()[i - 1]?.value ?? 1;
    const cur  = this.steps()[i]?.value ?? 0;
    return i <= 0 ? 0 : Math.round(((prev - cur) / prev) * 100);
  }

  private render(steps: Step[]) {
    if (!this.chart) return;
    const option: echarts.EChartsOption = {
      tooltip: { trigger: 'item', formatter: (p: any) => new Intl.NumberFormat('pt-BR').format(p?.value ?? 0) },
      series: [{
        type: 'funnel',
        left: '4%', right: '4%', top: 12, bottom: 12, width: '92%',
        min: 0, max: Math.max(...steps.map(s => s.value)),
        sort: 'descending', gap: 6,
        label: { show: false }, emphasis: { label: { show: false } },
        itemStyle: { borderColor: 'var(--surface, #fff)', borderWidth: 2 },
        data: steps.map((s, i) => ({ name: s.name, value: s.value, itemStyle: { color: this.palette[i % this.palette.length] } })),
      }],
    };
    this.chart.setOption(option, true);
  }

  private dispose() { this.ro?.disconnect(); this.chart?.dispose(); }
}
