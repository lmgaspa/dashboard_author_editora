import {
  Component, ElementRef, ViewChild, effect, input, signal, DestroyRef,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { BlockComponent } from '@/app/shared/block.component';
import * as echarts from 'echarts';
import {
  FunnelStep,
  MOCK_FUNIL_ATUAL,
  MOCK_FUNIL_MES_PASSADO,
} from '../../data/mock-data-funil';

type StepName = 'Visitas' | 'Carrinho' | 'Checkout' | 'Pagamento Aprovado';
type ColorMode = 'fixed' | 'delta';

@Component({
  selector: 'app-sales-funil',
  standalone: true,
  imports: [CommonModule, BlockComponent],
  template: `
    <app-block title="Funil de Vendas">
      <div class="grid gap-6 md:grid-cols-2 items-stretch"
           [style.minHeight.px]="blockMinHeight()">

        <!-- Gráfico -->
        <div class="flex items-center justify-center h-full">
          <div #chart class="w-full" [style.height.px]="chartMaxHeight()"
               role="img" aria-label="Etapas do funil de vendas"></div>
        </div>

        <!-- Legenda centralizada -->
        <div class="h-full flex items-center justify-center">
          <ol class="space-y-3" aria-label="Legenda do funil">
            @for (s of currentSteps(); let i = $index; track i) {
              <li class="flex items-center gap-3">
                <span class="inline-block rounded-full"
                      [style.width.px]="dotPx()"
                      [style.height.px]="dotPx()"
                      [style.background-color]="legendDotColor(s.name, i)"></span>
                <span class="text-foreground/80"
                      [style.fontSize.px]="legendFontPx()"
                      [style.lineHeight.px]="legendFontPx() + 4"
                      style="font-weight:600">
                  {{ s.name }}
                </span>
              </li>
            }
          </ol>
        </div>
      </div>

      <!-- Resumo alinhado (controle pelo summaryTopGap) -->
      <div class="leading-snug space-y-1.5"
           [style.fontSize.px]="summaryFontPx()"
           [style.marginTop.px]="summaryTopGap()">
        <p>
          Este mês tivemos <b>{{ fmt(val('Visitas')) }}</b> visitas
          ( <b [style.color]="color('Visitas')">{{ keyword('Visitas') }}</b>{{ suffix('Visitas') }} ),
          em relação ao mês passado.
        </p>

        <p>
          Este mês, o carrinho ficou em <b>{{ fmt(val('Carrinho')) }}</b>
          ( <b [style.color]="color('Carrinho')">{{ keyword('Carrinho') }}</b>{{ suffix('Carrinho') }} ),
          em relação ao mês passado.
        </p>

        <p>
          Este mês, o checkout registrou <b>{{ fmt(val('Checkout')) }}</b>
          ( <b [style.color]="color('Checkout')">{{ keyword('Checkout') }}</b>{{ suffix('Checkout') }} ),
          em relação ao mês passado.
        </p>

        <p>
          Este mês, os pagamentos aprovados
          <b [style.color]="color('Pagamento Aprovado')">somaram</b>
          <b>{{ fmt(val('Pagamento Aprovado')) }}</b>
          ( <b [style.color]="color('Pagamento Aprovado')">{{ keyword('Pagamento Aprovado') }}</b>{{ suffix('Pagamento Aprovado') }} ),
          em relação ao mês passado.
        </p>
      </div>
    </app-block>
  `,
})
export class SalesFunilComponent {
  @ViewChild('chart', { static: true }) chartEl!: ElementRef<HTMLDivElement>;

  /** Layout */
  blockMinHeight = input<number>(360);
  chartMaxHeight  = input<number>(260);

  /** Tamanhos */
  legendFontPx  = input<number>(22);
  summaryFontPx = input<number>(22);
  dotPx         = input<number>(10);
  /** Gap acima do resumo (px) para alinhar com o bloco do Mapa do Brasil */
  summaryTopGap = input<number>(0);

  /** Modo de cor do funil/legenda: 'fixed' (paleta) ou 'delta' (variação) */
  colorMode = input<ColorMode>('fixed');

  /** Paleta fixa quando colorMode='fixed' */
  palette = input<string[]>(['#638cf7', '#9ad95a', '#5a6b85', '#f5a524']);

  /** Cores quando colorMode='delta' */
  deltaColors = input<{ up: string; down: string; flat: string }>({
    up:   '#166534', // verde
    down: '#b91c1c', // vermelho
    flat: '#0f172a', // preto
  });

  /** Dados (se não passar, usa mocks) */
  stepsInput    = input<FunnelStep[] | null>(null);
  previousInput = input<Partial<Record<StepName, number>> | null>(null);

  /** Dados efetivos */
  readonly stepsMock = signal<FunnelStep[]>([...MOCK_FUNIL_ATUAL]);
  readonly prevMock  = { ...MOCK_FUNIL_MES_PASSADO };
  currentSteps = signal<FunnelStep[]>([]);
  previous     = signal<Partial<Record<StepName, number>>>({});

  private chart?: echarts.ECharts;
  private ro?: ResizeObserver;

  // cores do texto (palavras-chave)
  private readonly GREEN = '#166534';
  private readonly RED   = '#b91c1c';
  private readonly BLACK = '#0f172a';

  constructor(private destroyRef: DestroyRef) {
    effect(() => {
      const cur  = this.stepsInput() ?? this.stepsMock();
      const prev = this.previousInput() ?? this.prevMock;
      this.currentSteps.set(cur);
      this.previous.set(prev);
      this.render(cur);
    });

    this.destroyRef.onDestroy(() => this.dispose());
  }

  ngAfterViewInit() {
    this.chart = echarts.init(this.chartEl.nativeElement);
    this.ro = new ResizeObserver(() => this.chart?.resize());
    this.ro.observe(this.chartEl.nativeElement);
    this.render(this.currentSteps());
  }

  // ===== helpers numéricos/texto =====
  fmt(n?: number): string {
    return new Intl.NumberFormat('pt-BR').format(n ?? 0);
  }

  val(name: StepName): number {
    return this.currentSteps().find(s => s.name === name)?.value ?? 0;
  }

  private change(name: StepName): number | undefined {
    const cur  = this.val(name);
    const prev = this.previous()[name];
    if (prev == null) return undefined;
    if (prev === 0) return cur ? 100 : 0;
    return Math.round(((cur ?? 0) - prev) / prev * 100);
  }

  color(name: StepName): string {
    const d = this.change(name);
    if (d === undefined || d === 0) return this.BLACK;
    return d > 0 ? this.GREEN : this.RED;
  }

  keyword(name: StepName): string {
    const d = this.change(name);
    if (d === undefined) return 'sem base de comparação';
    if (d === 0) return 'estável';
    return d > 0 ? 'subida' : 'diminuição';
  }

  suffix(name: StepName): string {
    const d = this.change(name);
    if (d === undefined) return '';
    if (d === 0) return ' vs. mês passado';
    return ` de ${this.fmt(Math.abs(d))}%`;
  }

  // ===== cores do gráfico/legenda =====
  private itemColorFor(name: StepName, idx: number): string {
    if (this.colorMode() === 'fixed') {
      const pal = this.palette();
      return pal[idx % pal.length];
    }
    const d = this.change(name);
    const c = this.deltaColors();
    if (d === undefined || d === 0) return c.flat;
    return d > 0 ? c.up : c.down;
  }

  legendDotColor(name: string, idx: number): string {
    return this.itemColorFor(name as StepName, idx);
  }

  // ===== gráfico ECharts =====
  private render(steps: FunnelStep[]) {
    if (!this.chart) return;

    const data = steps.map((s, i) => ({
      name: s.name,
      value: s.value,
      itemStyle: { color: this.itemColorFor(s.name as StepName, i) },
    }));

    const option: echarts.EChartsOption = {
      tooltip: {
        trigger: 'item',
        formatter: (p: any) => this.fmt(p?.value ?? 0),
        textStyle: { fontSize: 14 },
      },
      series: [{
        type: 'funnel',
        left: '4%', right: '4%', top: 12, bottom: 12, width: '92%',
        min: 0, max: Math.max(...steps.map(s => s.value)),
        sort: 'descending', gap: 6,
        label: { show: false }, emphasis: { label: { show: false } },
        itemStyle: { borderColor: 'var(--surface, #fff)', borderWidth: 2 },
        data,
      }],
    };

    this.chart.setOption(option, true);
  }

  private dispose() {
    this.ro?.disconnect();
    this.chart?.dispose();
  }
}
