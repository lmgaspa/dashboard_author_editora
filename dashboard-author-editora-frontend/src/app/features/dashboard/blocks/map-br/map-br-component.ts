import {
  AfterViewInit,
  Component,
  ElementRef,
  Input,
  OnDestroy,
  ViewChild,
} from '@angular/core';
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

  /** GeoJSON (coloque em src/assets/geo/br-uf.json). Se usar assets/, troque aqui tbm. */
  @Input() geoUrl = 'geo/br-uf.json';

  private chart?: echarts.ECharts;
  private ro?: ResizeObserver;

  // Vendas por região (mock)
  private regionTotals: Record<'N' | 'NE' | 'CO' | 'SE' | 'S', number> = {
    N: 50,
    NE: 0,
    CO: 180,
    SE: 1320,
    S: 90,
  };

  // Estado -> Região
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

  // 0 -> vermelho, 50 -> amarelo, 80 -> verde claro, >=100 -> verde escuro
  private colorFor(value: number): string {
    const v = Math.max(0, Math.min(100, value));
    const red = [185, 28, 28];
    const yel = [245, 158, 11];
    const gcl = [132, 204, 22];
    const gdk = [22, 101, 52];
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

        const features: any[] = Array.isArray(geojson?.features) ? geojson.features : [];
        console.log('[Mapa BR] features:', features.length, 'exemplo props:', features[0]?.properties);
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

        // Detecta a chave de nome usada no GeoJSON
        const propKeys = Object.keys(features[0].properties || {});
        const guessKey =
          ['name', 'NM_ESTADO', 'NOME', 'estado', 'Estado', 'uf', 'UF', 'sigla', 'SIGLA']
            .find((k) => propKeys.includes(k)) || 'name';
        console.log('[Mapa BR] usando nameProperty =', guessKey);

        // Normalização pra casar nomes
        const norm = (s: string) =>
          String(s).normalize('NFD').replace(/\p{Diacritic}/gu, '').replace(/\s*\(.+\)\s*/, '').trim().toUpperCase();

        const byNorm: Record<string, 'N' | 'NE' | 'CO' | 'SE' | 'S'> = {};
        Object.keys(this.stateToRegion).forEach((n) => (byNorm[norm(n)] = this.stateToRegion[n]));

        const missing: string[] = [];
        const data = features.map((f) => {
          const rawName = String(f.properties?.[guessKey] ?? '');
          const reg = byNorm[norm(rawName)];
          if (!reg) missing.push(rawName);
          const val = reg ? this.regionTotals[reg] : 0;
          return {
            name: rawName,
            value: val,
            itemStyle: reg ? { areaColor: this.colorFor(val) } : undefined,
          };
        });

        if (missing.length) {
          console.warn('[Mapa BR] UFs sem mapeamento de região (ficam na cor-base):', missing);
        }

        // Render (agora com nameProperty configurado!)
        this.chart!.setOption({
          tooltip: {
            trigger: 'item',
            confine: true,
            formatter: (p: any) => {
              const raw = p.name as string;
              const reg =
                byNorm[
                  norm(
                    // garante que a tooltip casa com a mesma lógica
                    p.data?.name ?? raw
                  )
                ];
              return `
                <div style="font:12px ui-sans-serif,system-ui;color:#0f172a">
                  <b>${raw}</b><br/>
                  Região: <b>${reg ?? '—'}</b><br/>
                  Vendas: <b>${(p.value ?? 0).toLocaleString('pt-BR')}</b>
                </div>`;
            },
          },
          series: [
            {
              type: 'map',
              map: 'BR',
              name: 'Vendas por UF (por região)',
              // 🔑 ESSENCIAL para as cores aparecerem: casa o 'name' do data com esta propriedade do GeoJSON
              nameProperty: guessKey,
              roam: true,
              zoom: 1,
              scaleLimit: { min: 1, max: 1.4 },
              // cor-base (quando não houver match)
              itemStyle: { borderColor: '#ffffff', borderWidth: 0.8, areaColor: '#e5e7eb' },
              emphasis: { label: { show: false } },
              // sempre manda o data: onde casar, a cor do itemStyle substitui a base
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
