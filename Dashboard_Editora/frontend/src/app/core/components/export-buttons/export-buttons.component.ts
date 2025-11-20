import { Component, Input, signal, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { firstValueFrom } from 'rxjs';
import { ExportService, ExportFormat } from '@/app/core/services/export.service';
import { AuthService } from '@/app/core/services/auth.service';

export type ExportModule = 'entregas' | 'cobrancas' | 'metricas' | 'tickets';

const MODULE_LABELS: Record<ExportModule, string> = {
  entregas: 'Entregas',
  cobrancas: 'Cobranças',
  metricas: 'Métricas',
  tickets: 'Tickets'
};

const MODULE_LABELS_ARQUIVADOS: Record<ExportModule, string> = {
  entregas: 'Pedidos Arquivados',
  cobrancas: 'Cobranças',
  metricas: 'Métricas',
  tickets: 'Tickets'
};

@Component({
  selector: 'app-export-buttons',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './export-buttons.component.html',
  styles: []
})
export class ExportButtonsComponent implements OnInit, OnDestroy {
  private readonly exportService = inject(ExportService);
  private readonly authService = inject(AuthService);

  @Input() module: ExportModule = 'entregas';
  @Input() authorId?: string;
  @Input() apenasArquivados: boolean = false;

  readonly showExportDropdown = signal<boolean>(false);
  readonly loading = signal<ExportFormat | null>(null);
  readonly error = signal<string | null>(null);
  private clickListener?: (e: Event) => void;

  get currentAuthorId(): string | undefined {
    return this.authorId || this.authService.currentUser()?.authorId || undefined;
  }

  get moduleLabel(): string {
    if (this.apenasArquivados && this.module === 'entregas') {
      return MODULE_LABELS_ARQUIVADOS[this.module] || 'Pedidos Arquivados';
    }
    return MODULE_LABELS[this.module] || 'Dados';
  }

  ngOnInit(): void {
    // Listener para fechar dropdown ao clicar fora
    this.clickListener = (e: Event) => {
      if (this.showExportDropdown()) {
        const target = e.target as HTMLElement;
        const exportDropdown = target?.closest('[data-export-dropdown]');
        if (!exportDropdown) {
          this.showExportDropdown.set(false);
        }
      }
    };
    document.addEventListener('click', this.clickListener);
  }

  ngOnDestroy(): void {
    if (this.clickListener) {
      document.removeEventListener('click', this.clickListener);
    }
  }

  toggleExportDropdown(): void {
    this.showExportDropdown.update(v => !v);
  }

  closeExportDropdown(): void {
    this.showExportDropdown.set(false);
  }

  async handleExport(format: ExportFormat): Promise<void> {
    this.loading.set(format);
    this.error.set(null);
    this.closeExportDropdown();

    try {
      // author_id não é passado como parâmetro - vem do token JWT automaticamente
      const options: any = {
        format
      };

      let blob: Blob;
      let filename: string;

      switch (this.module) {
        case 'entregas':
          if (this.apenasArquivados) {
            // Usar endpoint específico para pedidos arquivados
            blob = await firstValueFrom(this.exportService.exportEntregasArquivadas(options));
            filename = this.exportService.generateFilename(
              'pedidos-arquivados',
              format,
              this.currentAuthorId
            );
          } else {
            blob = await firstValueFrom(this.exportService.exportEntregas(options));
            filename = this.exportService.generateFilename(
              'entregas',
              format,
              this.currentAuthorId
            );
          }
          break;
        case 'cobrancas':
          blob = await firstValueFrom(this.exportService.exportCobrancas(options));
          filename = this.exportService.generateFilename('cobrancas', format, this.currentAuthorId);
          break;
        case 'metricas':
          blob = await firstValueFrom(this.exportService.exportMetrics(options));
          filename = this.exportService.generateFilename('metricas', format, this.currentAuthorId);
          break;
        case 'tickets':
          blob = await firstValueFrom(this.exportService.exportTickets(options));
          filename = this.exportService.generateFilename('tickets', format, this.currentAuthorId);
          break;
        default:
          throw new Error(`Módulo de exportação desconhecido: ${this.module}`);
      }

      this.exportService.downloadBlob(blob, filename);
    } catch (err: any) {
      console.error(`Erro ao exportar ${this.module}:`, err);
      this.error.set(err.error?.message || `Erro ao exportar ${this.module}. Tente novamente.`);
    } finally {
      this.loading.set(null);
    }
  }
}

