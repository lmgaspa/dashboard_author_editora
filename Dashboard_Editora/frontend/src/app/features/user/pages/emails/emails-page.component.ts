import { Component, signal, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { EmailService } from '@/app/core/services/email.service';
import { ExportService, ExportFormat } from '@/app/core/services/export.service';
import { AuthService } from '@/app/core/services/auth.service';
import { PainelEmailsAutor, ResumoEmailCliente, ResumoEmailRepasse } from '@/app/core/models/email.model';

@Component({
  selector: 'app-emails-page',
  standalone: true,
  imports: [CommonModule, CurrencyPipe],
  templateUrl: './emails-page.component.html',
  styles: []
})
export class EmailsPageComponent implements OnInit, OnDestroy {
  private readonly emailService = inject(EmailService);
  private readonly exportService = inject(ExportService);
  private readonly authService = inject(AuthService);

  readonly painel = signal<PainelEmailsAutor | null>(null);
  readonly loading = signal<boolean>(false);
  readonly error = signal<string | null>(null);
  readonly showExportDropdown = signal<boolean>(false);
  readonly exporting = signal<boolean>(false);
  readonly exportError = signal<string | null>(null);
  readonly exportSuccess = signal<boolean>(false);
  private clickListener?: (e: Event) => void;

  ngOnInit(): void {
    this.loadPainel();
    
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

  loadPainel(): void {
    this.loading.set(true);
    this.error.set(null);

    this.emailService.getPainelEmails().subscribe({
      next: (data) => {
        // Ordenar e-mails de clientes por último pedido (mais recente primeiro)
        if (data.emailsClientes) {
          data.emailsClientes.sort((a, b) => {
            const dateA = a.ultimoPedidoEm ? new Date(a.ultimoPedidoEm).getTime() : 0;
            const dateB = b.ultimoPedidoEm ? new Date(b.ultimoPedidoEm).getTime() : 0;
            return dateB - dateA;
          });
        }

        // Ordenar e-mails de repasse por data de envio (mais recente primeiro)
        if (data.emailsRepasse) {
          data.emailsRepasse.sort((a, b) => {
            const dateA = a.enviadoEm ? new Date(a.enviadoEm).getTime() : 0;
            const dateB = b.enviadoEm ? new Date(b.enviadoEm).getTime() : 0;
            return dateB - dateA;
          });
        }

        this.painel.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Erro ao carregar painel de e-mails:', err);
        const errorMessage = this.getErrorMessage(err);
        this.error.set(errorMessage);
        this.loading.set(false);
      }
    });
  }

  retry(): void {
    this.loadPainel();
  }

  private getErrorMessage(err: any): string {
    if (err.status === 401) {
      return 'Você não está autenticado. Por favor, faça login novamente.';
    }
    if (err.status === 403) {
      return 'Você não possui author_id configurado. Entre em contato com o administrador.';
    }
    if (err.status === 404) {
      return 'Autor não encontrado ou sem dados no e-commerce.';
    }
    if (err.status === 400) {
      return 'Configuração do e-commerce incompleta. Entre em contato com o administrador.';
    }
    if (err.status === 500) {
      return err.error?.message || 'Erro ao buscar informações de e-mails. Tente novamente mais tarde.';
    }
    return err.error?.message || 'Erro ao carregar painel de e-mails. Tente novamente.';
  }

  formatDate(dateString: string | null): string {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  formatDateShort(dateString: string | null): string {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  }

  getTipoBadgeClass(tipoEmail: string): string {
    const tipo = tipoEmail.toLowerCase();
    if (tipo.includes('pix')) {
      return 'bg-purple-500/20 border-purple-500/30 text-purple-300';
    }
    if (tipo.includes('card')) {
      return 'bg-amber-500/20 border-amber-500/30 text-amber-300';
    }
    return 'bg-gray-500/20 border-gray-500/30 text-gray-300';
  }

  getStatusBadgeClass(status: string): string {
    const statusLower = status.toLowerCase();
    if (statusLower === 'sent') {
      return 'bg-emerald-500/20 border-emerald-500/30 text-emerald-300';
    }
    if (statusLower === 'failed') {
      return 'bg-red-500/20 border-red-500/30 text-red-300';
    }
    return 'bg-gray-500/20 border-gray-500/30 text-gray-300';
  }

  getStatusIcon(status: string): string {
    const statusLower = status.toLowerCase();
    if (statusLower === 'sent') {
      return 'check_circle';
    }
    if (statusLower === 'failed') {
      return 'error';
    }
    return 'help_outline';
  }

  calculateConfirmationRate(total: number, confirmed: number): number {
    if (total === 0) return 0;
    return Math.round((confirmed / total) * 100);
  }

  toggleExportDropdown(): void {
    this.showExportDropdown.update(v => !v);
  }

  closeExportDropdown(): void {
    this.showExportDropdown.set(false);
  }

  exportEmails(format: ExportFormat): void {
    this.exporting.set(true);
    this.exportError.set(null);
    this.exportSuccess.set(false);
    this.closeExportDropdown();

    const authorId = this.authService.currentUser()?.authorId;

    if (!authorId) {
      this.exportError.set('Author ID não encontrado');
      this.exporting.set(false);
      return;
    }

    this.exportService.exportEmails({
      format,
      authorId
    }).subscribe({
      next: (blob) => {
        const filename = this.exportService.generateFilename('emails', format, authorId);
        this.exportService.downloadBlob(blob, filename);
        this.exportSuccess.set(true);
        setTimeout(() => this.exportSuccess.set(false), 3000);
        this.exporting.set(false);
      },
      error: (err) => {
        console.error('Erro ao exportar emails:', err);
        this.exportError.set(err.error?.message || 'Erro ao exportar emails. Tente novamente.');
        this.exporting.set(false);
      }
    });
  }
}

