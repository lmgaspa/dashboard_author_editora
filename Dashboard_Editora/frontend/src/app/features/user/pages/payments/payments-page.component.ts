import { Component, signal, inject, OnInit } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { PaymentService } from '@/app/core/services/payment.service';
import { PainelPagamentosAutor, PagamentosAutorResumo, FunilVendas, VendaRecente } from '@/app/core/models/payment.model';

@Component({
  selector: 'app-payments-page',
  standalone: true,
  imports: [CommonModule, CurrencyPipe],
  templateUrl: './payments-page.component.html',
  styles: []
})
export class PaymentsPageComponent implements OnInit {
  private readonly paymentService = inject(PaymentService);

  readonly painel = signal<PainelPagamentosAutor | null>(null);
  readonly loading = signal<boolean>(false);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.loadPainel();
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
}

