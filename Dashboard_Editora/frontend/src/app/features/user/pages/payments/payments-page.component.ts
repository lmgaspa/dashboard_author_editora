import { Component, signal, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { PaymentService } from '@/app/core/services/payment.service';
import { ExportService, ExportFormat } from '@/app/core/services/export.service';
import { AuthService } from '@/app/core/services/auth.service';
import { PainelPagamentosAutor, PagamentosAutorResumo, FunilVendas, VendaRecente } from '@/app/core/models/payment.model';

@Component({
  selector: 'app-payments-page',
  standalone: true,
  imports: [CommonModule, CurrencyPipe],
  templateUrl: './payments-page.component.html',
  styles: []
})
export class PaymentsPageComponent implements OnInit, OnDestroy {
  private readonly paymentService = inject(PaymentService);
  private readonly exportService = inject(ExportService);
  private readonly authService = inject(AuthService);

  readonly painel = signal<PainelPagamentosAutor | null>(null);
  readonly loading = signal<boolean>(false);
  readonly error = signal<string | null>(null);
  readonly showExportDropdown = signal<boolean>(false);
  readonly exporting = signal<boolean>(false);
  readonly exportError = signal<string | null>(null);
  readonly exportSuccess = signal<boolean>(false);
  private clickListener?: (e: Event) => void;

  ngOnInit(): void {
    // Carregar perfil se authorId não estiver disponível
    this.ensureAuthorId().then(() => {
      this.loadPainel();
    });
    
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

  private ensureAuthorId(): Promise<void> {
    return new Promise((resolve) => {
      const currentUser = this.authService.currentUser();
      
      // Se já tem authorId, não precisa carregar perfil
      if (currentUser?.authorId) {
        resolve();
        return;
      }

      // Carregar perfil para obter authorId
      console.log('🔍 AuthorId não encontrado, carregando perfil...');
      this.authService.getUserProfile().subscribe({
        next: (user) => {
          console.log('✅ Perfil carregado, authorId:', user.authorId);
          resolve();
        },
        error: (err) => {
          console.error('❌ Erro ao carregar perfil:', err);
          // Continuar mesmo com erro (pode ser que o usuário não tenha authorId configurado)
          resolve();
        }
      });
    });
  }

  ngOnDestroy(): void {
    if (this.clickListener) {
      document.removeEventListener('click', this.clickListener);
    }
  }

  loadPainel(): void {
    this.loading.set(true);
    this.error.set(null);

    this.paymentService.getPainelPagamentos().subscribe({
      next: (data) => {
        this.painel.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Erro ao carregar painel de pagamentos:', err);
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
    if (err.status === 500) {
      return err.error?.message || 'Erro ao buscar informações de pagamentos. Tente novamente mais tarde.';
    }
    return err.error?.message || 'Erro ao carregar painel de pagamentos. Tente novamente.';
  }

  formatDate(dateString: string): string {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  }

  truncateText(text: string, maxLength: number = 30): string {
    if (!text) return '-';
    if (text.length <= maxLength) return text;
    return text.substring(0, maxLength) + '...';
  }

  getStatusIcon(status: string): string {
    const statusLower = status.toLowerCase();
    if (statusLower.includes('pago') || statusLower.includes('confirmado')) {
      return 'check_circle';
    }
    if (statusLower.includes('andamento') || statusLower.includes('pendente')) {
      return 'hourglass_empty';
    }
    if (statusLower.includes('cancelado')) {
      return 'cancel';
    }
    return 'help_outline';
  }

  getStatusColor(status: string): string {
    const statusLower = status.toLowerCase();
    if (statusLower.includes('pago') || statusLower.includes('confirmado')) {
      return 'text-emerald-400';
    }
    if (statusLower.includes('andamento') || statusLower.includes('pendente')) {
      return 'text-amber-400';
    }
    if (statusLower.includes('cancelado')) {
      return 'text-red-400';
    }
    return 'text-gray-400';
  }

  getStatusBgColor(status: string): string {
    const statusLower = status.toLowerCase();
    if (statusLower.includes('pago') || statusLower.includes('confirmado')) {
      return 'bg-emerald-500/20 border-emerald-500/30';
    }
    if (statusLower.includes('andamento') || statusLower.includes('pendente')) {
      return 'bg-amber-500/20 border-amber-500/30';
    }
    if (statusLower.includes('cancelado')) {
      return 'bg-red-500/20 border-red-500/30';
    }
    return 'bg-gray-500/20 border-gray-500/30';
  }

  calculatePercentage(value: number, total: number): number {
    if (total === 0) return 0;
    return Math.round((value / total) * 100);
  }

  toggleExportDropdown(): void {
    this.showExportDropdown.update(v => !v);
  }

  closeExportDropdown(): void {
    this.showExportDropdown.set(false);
  }

  exportPayments(format: ExportFormat): void {
    this.exporting.set(true);
    this.exportError.set(null);
    this.exportSuccess.set(false);
    this.closeExportDropdown();

    // Tentar obter authorId do usuário atual
    let authorId = this.authService.currentUser()?.authorId;

    // Se não tiver authorId, carregar perfil primeiro
    if (!authorId) {
      console.log('🔍 AuthorId não encontrado, carregando perfil antes de exportar...');
      this.authService.getUserProfile().subscribe({
        next: (user) => {
          console.log('📦 Perfil carregado:', user);
          console.log('🔍 AuthorId no perfil:', user.authorId);
          authorId = user.authorId;
          if (!authorId) {
            const currentEmail = this.authService.currentUser()?.email || 'N/A';
            console.error('❌ Author ID não encontrado para o usuário:', currentEmail);
            this.exportError.set(
              `Author ID não configurado. Seu usuário (${currentEmail}) não possui um Author ID vinculado. ` +
              `Entre em contato com o administrador para configurar o Author ID no seu perfil.`
            );
            this.exporting.set(false);
            return;
          }
          this.performExport(format, authorId);
        },
        error: (err) => {
          console.error('❌ Erro ao carregar perfil:', err);
          const currentEmail = this.authService.currentUser()?.email || 'N/A';
          this.exportError.set(
            `Erro ao carregar perfil do usuário (${currentEmail}). ` +
            `Verifique sua conexão e tente novamente. Se o problema persistir, entre em contato com o administrador.`
          );
          this.exporting.set(false);
        }
      });
      return;
    }

    this.performExport(format, authorId);
  }

  private performExport(format: ExportFormat, authorId: string): void {
    this.exportService.exportPayments({
      format,
      authorId
    }).subscribe({
      next: (blob) => {
        const filename = this.exportService.generateFilename('pagamentos', format, authorId);
        this.exportService.downloadBlob(blob, filename);
        this.exportSuccess.set(true);
        setTimeout(() => this.exportSuccess.set(false), 3000);
        this.exporting.set(false);
      },
      error: (err) => {
        console.error('Erro ao exportar pagamentos:', err);
        this.exportError.set(err.error?.message || 'Erro ao exportar pagamentos. Tente novamente.');
        this.exporting.set(false);
      }
    });
  }
}

