// src/app/features/dashboard/blocks/payment-methods/payment-methods-component.ts
import { Component, DestroyRef, ElementRef, ViewChild, effect, signal, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BlockComponent } from '@/app/shared/block.component';
import * as echarts from 'echarts';

type PaymentSlice = { name: 'Cartão' | 'PIX'; value: number };

// === MONOLITO: mock local (sem helpers externos) ===
const METHODS_PAYMENT = {
  current: [
    { name: 'Cartão', value: 52 },
    { name: 'PIX', value: 48 },
  ] as PaymentSlice[],
  lastMonth: [
    { name: 'Cartão', value: 58 },
    { name: 'PIX', value: 42 },
  ] as PaymentSlice[],
};

@Component({
  selector: 'app-payment-methods',
  standalone: true,
  imports: [BlockComponent, CommonModule],
  template: `
    <app-block title="Formas de Pagamento">
      <div
        class="flex flex-col"
        [style.minHeight.px]="blockMinHeight()"
        style="font-size:22px; line-height:1.25;"
      >
        <!-- DONUT (desce um pouco para respirar) -->
        <div class="flex justify-center mt-8" aria-label="Gráfico de pizza">
          <div
            #chart
            class="w-full max-w-[520px]"
            [style.height.px]="chartMaxHeight()"
            role="img"
            aria-label="Distribuição de pagamentos por método"
          ></div>
        </div>

        <!-- LEGENDA (abaixo do donut, centralizada) -->
        <div class="mt-2 flex justify-center">
          <ul class="mx-auto text-center" aria-label="Legenda das formas de pagamento">
            <!-- PIX primeiro -->
            @for (item of data(); track item.name) { @if (item.name === 'PIX') {
            <li class="inline-flex items-center" style="margin-right:12px;">
              <!-- ponto -->
              <span
                class="inline-block"
                style="width:10px;height:10px;border-radius:9999px"
                [style.backgroundColor]="pixColor"
              ></span>

              <!-- label coladinho -->
              <span style="opacity:.85; margin:0 4px 0 4px;">PIX</span>

              <!-- Barra SEM depender do %, sempre preenchendo até o fim -->
              <div class="rounded bg-muted" style="height:8px; width:56px; margin-right:2px;">
                <div
                  class="h-2 rounded"
                  [style.backgroundColor]="pixColor"
                  style="width:100%"
                ></div>
              </div>

              <!-- % do PIX (colada na barra) -->
              <span class="tabular-nums" style="opacity:.8;">{{ percent(item) }}%</span>
            </li>
            } }

            <!-- Depois Cartão, com respiro ANTES do pontinho vermelho -->
            @for (item of data(); track item.name) { @if (item.name === 'Cartão') {
            <li class="inline-flex items-center" style="margin-left:12px;">
              <!-- ponto com respiro da % anterior -->
              <span
                class="inline-block"
                style="width:10px;height:10px;border-radius:9999px"
                [style.backgroundColor]="cartaoColor"
              ></span>

              <!-- label coladinho -->
              <span style="opacity:.85; margin:0 4px 0 4px;">Cartão</span>

              <div class="rounded bg-muted" style="height:8px; width:56px; margin-right:4px;">
                <div
                  class="h-2 rounded"
                  [style.backgroundColor]="cartaoColor"
                  style="width:100%"
                ></div>
              </div>

              <!-- % do Cartão -->
              <span class="tabular-nums" style="opacity:.8;">{{ percent(item) }}%</span>
            </li>
            } }
          </ul>
        </div>

        <!-- TEXTOS comparativos -->
        <div class="mt-4 w-full max-w-[800px] self-center text-center space-y-2">
          <div>
            <strong>PIX:</strong>
            Em relação ao mês passado, @if (pixDeltaPct > 0) {
            <span [style.color]="pixColor"><strong>aumentou</strong></span>
            em {{ pixDeltaPctAbs }}% (de {{ pixPrev }} para {{ pixCur }}). } @else if (pixDeltaPct <
            0) {
            <span [style.color]="pixColor"><strong>diminuiu</strong></span>
            em {{ pixDeltaPctAbs }}% (de {{ pixPrev }} para {{ pixCur }}). } @else {
            <span style="color:inherit"><strong>se manteve</strong></span>
            ({{ pixCur }}). }
          </div>

          <div>
            <strong>Cartão de crédito:</strong>
            Em relação ao mês passado, @if (cartaoDeltaPct > 0) {
            <span [style.color]="cartaoColor"><strong>aumentou</strong></span>
            em {{ cartaoDeltaPctAbs }}% (de {{ cartaoPrev }} para {{ cartaoCur }}). } @else if
            (cartaoDeltaPct < 0) {
            <span [style.color]="cartaoColor"><strong>diminuiu</strong></span>
            em {{ cartaoDeltaPctAbs }}% (de {{ cartaoPrev }} para {{ cartaoCur }}). } @else {
            <span style="color:inherit"><strong>se manteve</strong></span>
            ({{ cartaoCur }}). }
          </div>
        </div>
      </div>
    </app-block>
  `,
})
export class PaymentMethodsComponent {
  @ViewChild('chart', { static: true }) chartEl!: ElementRef<HTMLDivElement>;

  // Deixe igual ao bloco "Mapa do Brasil" (ajuste se seu outro bloco usar outro valor)
  blockMinHeight = input<number>(520);
  chartMaxHeight = input<number>(280);

  // Estado (mock atual e anterior)
  readonly data = signal<PaymentSlice[]>(METHODS_PAYMENT.current);
  readonly last = signal<PaymentSlice[]>(METHODS_PAYMENT.lastMonth);

  // Comparativos/cores já calculados (sem helpers externos)
  pixCur = 0;
  pixPrev = 0;
  pixDeltaPct = 0;
  pixDeltaPctAbs = 0;
  pixColor = '#10b981';
  cartaoCur = 0;
  cartaoPrev = 0;
  cartaoDeltaPct = 0;
  cartaoDeltaPctAbs = 0;
  cartaoColor = '#ef4444';
  neutralColor = '#334155';

  private chart?: echarts.ECharts;
  private ro?: ResizeObserver;

  constructor(private destroyRef: DestroyRef) {
    // Recalcula quando os dados mudarem e redesenha o donut com as MESMAS cores da legenda
    effect(() => {
      // valores atuais/anteriores
      this.pixCur = this.data().find((d) => d.name === 'PIX')?.value ?? 0;
      this.pixPrev = this.last().find((d) => d.name === 'PIX')?.value ?? 0;
      this.cartaoCur = this.data().find((d) => d.name === 'Cartão')?.value ?? 0;
      this.cartaoPrev = this.last().find((d) => d.name === 'Cartão')?.value ?? 0;

      // deltas
      this.pixDeltaPct =
        this.pixPrev === 0 && this.pixCur === 0
          ? 0
          : this.pixPrev === 0
          ? 100
          : Math.round(((this.pixCur - this.pixPrev) / this.pixPrev) * 100);
      this.cartaoDeltaPct =
        this.cartaoPrev === 0 && this.cartaoCur === 0
          ? 0
          : this.cartaoPrev === 0
          ? 100
          : Math.round(((this.cartaoCur - this.cartaoPrev) / this.cartaoPrev) * 100);
      this.pixDeltaPctAbs = Math.abs(this.pixDeltaPct);
      this.cartaoDeltaPctAbs = Math.abs(this.cartaoDeltaPct);

      // resolve tokens CSS para cores reais (canvas do ECharts não entende var(--x))
      const root = getComputedStyle(document.documentElement);
      const bg = root.getPropertyValue('--surface').trim() || '#ffffff';

      const success = root.getPropertyValue('--success').trim() || '#10b981';
      const danger = root.getPropertyValue('--danger').trim() || '#ef4444';
      const neutral = root.getPropertyValue('--ink-2').trim() || '#334155';
      this.neutralColor = neutral;

      // escolhe cores por delta
      this.pixColor = this.pixDeltaPct > 0 ? success : this.pixDeltaPct < 0 ? danger : neutral;
      this.cartaoColor =
        this.cartaoDeltaPct > 0 ? success : this.cartaoDeltaPct < 0 ? danger : neutral;

      // redesenha com as cores sincronizadas
      this.render(this.data());
    });

    this.destroyRef.onDestroy(() => this.dispose());
  }

  ngAfterViewInit() {
    this.chart = echarts.init(this.chartEl.nativeElement);
    this.ro = new ResizeObserver(() => this.chart?.resize());
    this.ro.observe(this.chartEl.nativeElement);
    this.render(this.data());
  }

  // % de cada fatia no mês atual
  percent(item: PaymentSlice): number {
    const total = this.data().reduce((a, b) => a + b.value, 0) || 1;
    return Math.round((item.value / total) * 100);
  }

  // Render do ECharts (usa as mesmas cores da legenda)
  private render(slices: PaymentSlice[]) {
    if (!this.chart) return;

    const total = slices.reduce((a, b) => a + b.value, 0) || 1;
    const top = slices.slice().sort((a, b) => b.value - a.value)[0] ?? {
      name: '' as any,
      value: 0,
    };
    const centerText = `${Math.round((top.value / total) * 100)}%`; // só a %

    // cores por método (sem helpers)
    const colors = slices.map((s) =>
      s.name === 'PIX' ? this.pixColor : s.name === 'Cartão' ? this.cartaoColor : this.neutralColor
    );

    const root = getComputedStyle(document.documentElement);
    const bg = root.getPropertyValue('--color-background').trim() || '#fff';

    const option: echarts.EChartsOption = {
      tooltip: { trigger: 'item' },
      legend: { show: false },
      series: [
        {
          type: 'pie',
          radius: ['60%', '85%'],
          label: {
            show: true,
            position: 'center',
            fontSize: 22,
            lineHeight: 24,
            formatter: centerText, // sem o nome
          },
          labelLine: { show: false },
          itemStyle: { borderColor: bg, borderWidth: 2 },
          data: slices.map((s, i) => ({
            name: s.name,
            value: s.value,
            itemStyle: { color: colors[i] },
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
