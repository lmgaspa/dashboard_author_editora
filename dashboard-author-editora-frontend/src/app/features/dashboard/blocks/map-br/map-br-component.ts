import { AfterViewInit, Component, ElementRef, Input, OnDestroy, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import * as echarts from 'echarts';
import { BlockComponent } from 'src/app/shared/block.component';

@Component({
  selector: 'app-map-br',
  standalone: true,
  imports: [CommonModule, BlockComponent],
  templateUrl: './map-br-component.html',
  styleUrls: ['./map-br-component.css'],
})
export class MapBrComponent implements AfterViewInit, OnDestroy {
  @ViewChild('chart', { static: true }) chartEl!: ElementRef<HTMLDivElement>;

  /** Altura mínima do mapa (px) */
  @Input() minHeight = 300;

  /** Largura máxima opcional (px). Se vazio, ocupa 100% */
  @Input() maxWidth?: number;

  /** GeoJSON em src/assets/geo/br-uf.json */
  @Input() geoUrl = 'geo/br-uf.json';

  private chart?: echarts.ECharts;
  private ro?: ResizeObserver;

  // ===== VALORES FIXOS POR REGIÃO (mock) =====
  // Regra: 0 = vermelho, >=100 = verde escuro (graduação no meio).
  private regionTotals: Record<'N' | 'NE' | 'CO' | 'SE' | 'S', number> = {
    N: 250,
    NE: 420,
    CO: 180,
    SE: 1320,
    S: 90, // < 100 → tende ao amarelo
  };

  // ===== ESTADO (NOME COMPLETO) -> REGIÃO =====
  private stateToRegion: Record<string, 'N' | 'NE' | 'CO' | 'SE' | 'S'> = {
    Acre: 'N',
    Alagoas: 'NE',
    Amapá: 'N',
    Amazonas: 'N',
    Bahia: 'NE',
    Ceará: 'NE',
    'Distrito Federal': 'CO',
    'Espírito Santo': 'SE',
    Goiás: 'CO',
    Maranhão: 'NE',
    'Mato Grosso': 'CO',
    'Mato Grosso do Sul': 'CO',
    'Minas Gerais': 'SE',
    Pará: 'N',
    Paraíba: 'NE',
    Paraná: 'S',
    Pernambuco: 'NE',
    Piauí: 'NE',
    'Rio de Janeiro': 'SE',
    'Rio Grande do Norte': 'NE',
    'Rio Grande do Sul': 'S',
    Rondônia: 'N',
    Roraima: 'N',
    'Santa Catarina': 'S',
    'São Paulo': 'SE',
    Sergipe: 'NE',
    Tocantins: 'N',
  };

  constructor(private http: HttpClient) {}

  // 0 -> vermelho, ~50 -> amarelo, ~80 -> verde claro, >=100 -> verde escuro
  private colorFor(value: number): string {
    const v = Math.max(0, Math.min(100, value));
    const red = [185, 28, 28]; // #b91c1c
    const yel = [245, 158, 11]; // #f59e0b
    const gcl = [132, 204, 22]; // #84cc16
    const gdk = [22, 101, 52]; // #166534
    const lerp = (a: number[], b: number[], t: number) =>
      `rgb(${Math.round(a[0] + (b[0] - a[0]) * t)},${Math.round(
        a[1] + (b[1] - a[1]) * t
      )},${Math.round(a[2] + (b[2] - a[2]) * t)})`;
    if (v <= 50) return lerp(red, yel, v / 50);
    if (v <= 80) return lerp(yel, gcl, (v - 50) / 30);
    if (v < 100) return lerp(gcl, gdk, (v - 80) / 20);
    return `rgb(${gdk[0]},${gdk[1]},${gdk[2]})`;
  }

  ngAfterViewInit(): void {
    this.chart = echarts.init(this.chartEl.nativeElement, undefined, { renderer: 'svg' });

    this.http.get<any>(this.geoUrl).subscribe({
      next: (geojson) => {
        echarts.registerMap('BR', geojson);

        const features = Array.isArray(geojson?.features) ? geojson.features : [];
        console.log(
          '[Mapa BR] features:',
          features.length,
          'exemplo props:',
          features[0]?.properties
        );

        if (features.length < 5) {
          this.chart!.setOption({
            title: {
              text: 'GeoJSON sem UFs separadas',
              subtext: 'Use um arquivo com 27 features (1 por estado).',
              left: 'center',
              top: 'middle',
              textStyle: { color: '#0f172a', fontSize: 16 },
              subtextStyle: { color: '#64748b' },
            },
          });
          return;
        }

        // 1) Descobrir a melhor chave de nome
        const propKeys = Object.keys(features[0].properties || {});
        const guessKey =
          ['name', 'NM_ESTADO', 'NOME', 'estado', 'Estado', 'uf', 'UF', 'sigla', 'SIGLA'].find(
            (k) => propKeys.includes(k)
          ) || 'name';

        // 2) Totais por região (fixos)
        const regionTotals: Record<'N' | 'NE' | 'CO' | 'SE' | 'S', number> = {
          N: 0,
          NE: 420,
          CO: 180,
          SE: 20,
          S: 90,
        };

        // 3) Dicionário Estado -> Região (com acentos)
        const stateToRegion: Record<string, 'N' | 'NE' | 'CO' | 'SE' | 'S'> = {
          Acre: 'N',
          Alagoas: 'NE',
          Amapá: 'N',
          Amazonas: 'N',
          Bahia: 'NE',
          Ceará: 'NE',
          'Distrito Federal': 'CO',
          'Espírito Santo': 'SE',
          Goiás: 'CO',
          Maranhão: 'NE',
          'Mato Grosso': 'CO',
          'Mato Grosso do Sul': 'CO',
          'Minas Gerais': 'SE',
          Pará: 'N',
          Paraíba: 'NE',
          Paraná: 'S',
          Pernambuco: 'NE',
          Piauí: 'NE',
          'Rio de Janeiro': 'SE',
          'Rio Grande do Norte': 'NE',
          'Rio Grande do Sul': 'S',
          Rondônia: 'N',
          Roraima: 'N',
          'Santa Catarina': 'S',
          'São Paulo': 'SE',
          Sergipe: 'NE',
          Tocantins: 'N',
        };

        // 4) Normalização p/ casar nomes diferentes (remove acentos, “(SP)”, espaços extras)
        const norm = (s: string) =>
          s
            .normalize('NFD')
            .replace(/\p{Diacritic}/gu, '')
            .replace(/\s*\(.+\)\s*/, '')
            .trim()
            .toUpperCase();

        // 5) Índice normalizado do dicionário
        const byNorm: Record<string, 'N' | 'NE' | 'CO' | 'SE' | 'S'> = {};
        Object.keys(stateToRegion).forEach((n) => (byNorm[norm(n)] = stateToRegion[n]));

        // 6) Monta data pintando por item (sem visualMap visível)
        const missing: string[] = [];
        const data = features.map((f: any) => {
          const rawName = String(f.properties?.[guessKey] ?? '');
          const key = norm(rawName);
          const reg = byNorm[key];
          if (!reg) missing.push(rawName);
          const val = reg ? regionTotals[reg] : 0;
          return {
            name: rawName,
            value: val,
            itemStyle: {
              areaColor: ((): string => {
                // 0→vermelho, 50→amarelo, 80→verde claro, >=100→verde escuro
                const clamp = Math.max(0, Math.min(100, val));
                const lerp = (a: number[], b: number[], t: number) =>
                  `rgb(${Math.round(a[0] + (b[0] - a[0]) * t)},${Math.round(
                    a[1] + (b[1] - a[1]) * t
                  )},${Math.round(a[2] + (b[2] - a[2]) * t)})`;
                const red = [185, 28, 28],
                  yel = [245, 158, 11],
                  gcl = [132, 204, 22],
                  gdk = [22, 101, 52];
                if (clamp <= 50) return lerp(red, yel, clamp / 50);
                if (clamp <= 80) return lerp(yel, gcl, (clamp - 50) / 30);
                if (clamp < 100) return lerp(gcl, gdk, (clamp - 80) / 20);
                return `rgb(${gdk[0]},${gdk[1]},${gdk[2]})`;
              })(),
            },
          };
        });

        if (missing.length) {
          console.warn('[Mapa BR] UFs sem mapeamento de região:', missing);
        }

        // 7) Render
        this.chart!.setOption({
          tooltip: {
            trigger: 'item',
            confine: true,
            formatter: (p: any) => {
              const raw = p.name as string;
              const reg = byNorm[norm(raw)];
              return `<div style="font:12px ui-sans-serif,system-ui;color:#0f172a">
          <b>${raw}</b><br/>Região: <b>${reg ?? '—'}</b><br/>
          Vendas: <b>${(p.value ?? 0).toLocaleString('pt-BR')}</b>
        </div>`;
            },
          },
          series: [
            {
              type: 'map',
              map: 'BR',
              name: 'Vendas por UF (por região)',
              nameProperty: guessKey, // usa a chave detectada
              roam: true,
              zoom: 1,
              scaleLimit: { min: 1, max: 1.4 },
              emphasis: { label: { show: false } },
              itemStyle: { borderColor: '#ffffff', borderWidth: 0.8 },
              data,
            },
          ],
        });

        // responsivo
        this.ro = new ResizeObserver(() => this.chart?.resize());
        this.ro.observe(this.chartEl.nativeElement);
        queueMicrotask(() => this.chart?.resize());
      },
      error: (err) => {
        console.error('Falha ao carregar GeoJSON', err);
        this.chart?.setOption({
          title: {
            text: 'GeoJSON não encontrado',
            left: 'center',
            top: 'middle',
            textStyle: { color: '#64748b' },
          },
        });
      },
    });
  }

  ngOnDestroy(): void {
    this.ro?.disconnect();
    this.chart?.dispose();
  }
}
