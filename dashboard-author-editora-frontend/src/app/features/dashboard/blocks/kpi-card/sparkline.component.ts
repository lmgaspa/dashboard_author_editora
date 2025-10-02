import { Component, input, computed } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-sparkline',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="w-full overflow-hidden">
      <!-- SVG responsivo (100% da largura do card) -->
      <svg
        class="block w-full"
        [attr.viewBox]="'0 0 ' + WIDTH + ' ' + height()"
        role="img" aria-label="Histórico semanal"
      >
        @if (areaPath()) {
          <path [attr.d]="areaPath()!" [attr.fill]="areaFill()" opacity="0.08"></path>
        }
        @if (pathD()) {
          <path [attr.d]="pathD()!"
                [attr.stroke]="stroke()" [attr.stroke-width]="strokeWidth()"
                fill="none" stroke-linecap="round" stroke-linejoin="round"></path>
        }
        <!-- linha base bem sutil -->
        <line [attr.x1]="paddingX()" [attr.x2]="WIDTH - paddingX()"
              [attr.y1]="height() - paddingY()" [attr.y2]="height() - paddingY()"
              stroke="currentColor" opacity="0.05"></line>
      </svg>

      <!-- legenda embaixo (mesmo tamanho do texto de variação: text-sm/md:text-base) -->
      <div class="mt-1">
        <div class="text-sm md:text-base text-[color:var(--ink-3)] leading-none">
          Desempenho da Semana
        </div>
        <div class="mt-1 grid grid-cols-4 text-sm md:text-base text-[color:var(--ink-3)]">
          @for (t of ticks(); let i = $index; track i) {
            <div class="text-center">{{ t }}</div>
          }
        </div>
      </div>
    </div>
  `,
})
export class SparklineComponent {
  readonly WIDTH = 100;

  // ↓ gráfico mais baixo e linha mais fina
  data = input<number[]>([]);
  height = input<number>(16);
  stroke = input<string>('#2997f0');
  areaFill = input<string>('#2997f0');
  strokeWidth = input<number>(2);
  paddingX = input<number>(4);
  paddingY = input<number>(3);

  ticks = input<string[]>(['1','2','3','4']);

  private stats = computed(() => {
    const d = this.data();
    if (!d || d.length === 0) return null;
    let min = d[0], max = d[0];
    for (const v of d) { if (v < min) min = v; if (v > max) max = v; }
    if (min === max) { min -= 1; max += 1; }
    return { min, max, n: d.length };
  });

  private points = computed<[number, number][]>(() => {
    const s = this.stats(); if (!s) return [];
    const { min, max, n } = s;
    const w = this.WIDTH - this.paddingX() * 2;
    const h = this.height() - this.paddingY() * 2;
    const dx = n > 1 ? w / (n - 1) : 0;
    const sy = (v: number) =>
      this.paddingY() + (h - (v - min) * (h / (max - min)));
    return Array.from({ length: n }, (_, i) =>
      [this.paddingX() + dx * i, sy(this.data()[i])] as [number, number]
    );
  });

  pathD = computed(() => {
    const pts = this.points(); if (!pts.length) return null;
    return 'M ' + pts.map(([x, y]) => `${x} ${y}`).join(' L ');
  });

  areaPath = computed(() => {
    const pts = this.points(); if (!pts.length) return null;
    const bottom = this.height() - this.paddingY();
    return `M ${pts[0][0]} ${bottom} L `
      + pts.map(([x, y]) => `${x} ${y}`).join(' L ')
      + ` L ${pts[pts.length - 1][0]} ${bottom} Z`;
  });
}
