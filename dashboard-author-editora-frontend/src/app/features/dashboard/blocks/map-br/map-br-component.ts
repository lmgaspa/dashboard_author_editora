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

  /** Altura mínima do mapa (px). Mantém o “jeito antigo” sem depender de largura */
  @Input() minHeight = 300;

  /** Se quiser limitar a largura útil, passe um valor (px). Se deixar vazio, ocupa 100% da coluna */
  @Input() maxWidth?: number;

  /** Caminho do GeoJSON (coloque o arquivo em src/assets/geo/br-uf.json) */
  @Input() geoUrl = 'geo/br-uf.json';

  private chart?: echarts.ECharts;
  private ro?: ResizeObserver;

  constructor(private http: HttpClient) {}

  ngAfterViewInit(): void {
    // Instancia o gráfico (renderer SVG = leve e nítido)
    this.chart = echarts.init(this.chartEl.nativeElement, undefined, { renderer: 'svg' });

    // Carrega o GeoJSON e registra o mapa ANTES de setOption
    this.http.get<any>(this.geoUrl).subscribe({
      next: (geojson) => {
        echarts.registerMap('BR', geojson);

        // Mock simples por UF (como antes)
        const features = (geojson?.features ?? []) as any[];
        const data = features.map((f) => {
          const p = f.properties || {};
          const name: string = (p.shapeName || p.name || p.NOME || p.nome) as string;
          const value = Math.floor(200 + Math.random() * 3200);
          return { name, value };
        });

        const max = Math.max(1, ...data.map((d) => d.value));

        const option: echarts.EChartsOption = {
          tooltip: {
            trigger: 'item',
            confine: true,
            formatter: (p: any) =>
              `<div style="font:12px ui-sans-serif,system-ui;color:#0f172a">
                 <b>${p.name}</b><br/>Vendas: <b>${(p.value ?? 0).toLocaleString('pt-BR')}</b>
               </div>`,
          },
          visualMap: {
            min: 0,
            max,
            left: 12,
            bottom: 12,
            orient: 'horizontal',
            inRange: { color: ['#DBEEFE', '#93CDF9', '#2997F0'] },
            itemWidth: 90,
            itemHeight: 8,
            textStyle: { color: '#334155' },
          },
          series: [
            {
              type: 'map',
              name: 'Vendas por UF',
              map: 'BR',
              roam: true,
              zoom: 1.0,
              scaleLimit: { min: 1, max: 1.4 },
              emphasis: { label: { show: false } },
              itemStyle: { borderColor: '#ffffff', borderWidth: 0.8 },
              data,
            },
          ],
        };

        this.chart!.setOption(option);

        // Responsivo como antes
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
