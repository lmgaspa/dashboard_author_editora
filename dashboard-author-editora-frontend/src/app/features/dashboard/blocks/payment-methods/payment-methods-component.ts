import { Component, DestroyRef, ElementRef, ViewChild, effect, signal, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BlockComponent } from '@/app/shared/block.component';
import * as echarts from 'echarts';

type PaymentSlice = { name: 'Cartão' | 'PIX'; value: number };

@Component({
  selector: 'app-payment-methods',
  standalone: true,
  imports: [BlockComponent, CommonModule],
  template: `
    <app-block title="Formas de Pagamento">
      <div class="grid gap-6 md:grid-cols-2 items-stretch"
           [style.minHeight.px]="blockMinHeight()">
        <div class="flex items-center justify-center h-full" aria-label="Gráfico de pizza das formas de pagamento">
          <div #chart class="w-full" [style.height.px]="chartMaxHeight()"
               role="img" aria-label="Distribuição de pagamentos por método"></div>
        </div>

        <ul class="space-y-4 md:h-full md:overflow-auto" aria-label="Legenda das formas de pagamento">
          <li class="flex items-center gap-3" *ngFor="let item of data(); let i = index">
            <span class="inline-block size-3 rounded-full" [ngClass]="dotClasses[i]"></span>
            <span class="text-sm text-foreground/80">{{ item.name }}</span>
            <div class="ml-auto flex items-center gap-3">
              <div class="h-2 w-28 rounded bg-muted">
                <div class="h-2 rounded" [ngClass]="barClasses[i]" [style.width.%]="percent(item)"></div>
              </div>
              <span class="text-xs tabular-nums text-foreground/70">{{ percent(item) }}%</span>
            </div>
          </li>
        </ul>
      </div>
    </app-block>
  `,
})
export class PaymentMethodsComponent {
  @ViewChild('chart', { static: true }) chartEl!: ElementRef<HTMLDivElement>;

  /** Altura mínima do CARD (não do donut) */
  blockMinHeight = input<number>(360);
  /** Altura máxima do donut */
  chartMaxHeight = input<number>(260);

  readonly data = signal<PaymentSlice[]>([
    { name: 'Cartão', value: 52 },
    { name: 'PIX', value: 48 },
  ]);

  readonly dotClasses = ['bg-brand-500', 'bg-brand-400'];
  readonly barClasses = ['bg-brand-500', 'bg-brand-400'];

  private chart?: echarts.ECharts;
  private ro?: ResizeObserver;

  constructor(private destroyRef: DestroyRef) {
    effect(() => this.render(this.data()));
    this.destroyRef.onDestroy(() => this.dispose());
  }

  ngAfterViewInit() {
    this.chart = echarts.init(this.chartEl.nativeElement);
    this.ro = new ResizeObserver(() => this.chart?.resize());
    this.ro.observe(this.chartEl.nativeElement);
    this.render(this.data());
  }

  percent(item: PaymentSlice): number {
    const total = this.data().reduce((a, b) => a + b.value, 0) || 1;
    return Math.round((item.value / total) * 100);
  }

  private render(slices: PaymentSlice[]) {
    if (!this.chart) return;

    const total = slices.reduce((a, b) => a + b.value, 0) || 1;
    const top = slices.slice().sort((a, b) => b.value - a.value)[0] ?? { name: '' as any, value: 0 };
    const centerText = `${top.name}\n${Math.round((top.value / total) * 100)}%`;

    const cssVars = ['--brand-500', '--brand-400'];
    const colors = slices.map((_, i) =>
      getComputedStyle(document.documentElement).getPropertyValue(cssVars[i] as any).trim() || undefined
    );

    const option: echarts.EChartsOption = {
      tooltip: { trigger: 'item' }, legend: { show: false },
      series: [{
        type: 'pie', radius: ['60%', '85%'],
        label: { show: true, position: 'center', fontSize: 16, lineHeight: 20, formatter: centerText },
        labelLine: { show: false },
        itemStyle: { borderColor: 'var(--color-background, #fff)', borderWidth: 2 },
        data: slices.map((s, i) => ({ name: s.name, value: s.value, itemStyle: { color: colors[i] } })),
      }],
    };

    this.chart.setOption(option, true);
  }

  private dispose() { this.ro?.disconnect(); this.chart?.dispose(); }
}
