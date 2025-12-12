import { Component, signal, input, output, effect, inject, computed } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EntregaService } from '@/app/core/services/entrega.service';
import { Entrega, ShippingStatus, AtualizarStatusEnvioRequest } from '@/app/core/models/entrega.model';
import { formatDate } from '@/app/core/utils/charge.utils';
import { getWhatsAppLink } from '@/app/core/utils/order.utils';

@Component({
  selector: 'app-status-mercadoria-modal',
  standalone: true,
  imports: [CommonModule, CurrencyPipe, FormsModule],
  templateUrl: './status-mercadoria-modal.component.html',
  styles: []
})
export class StatusMercadoriaModalComponent {
  private readonly entregaService = inject(EntregaService);

  entrega = input.required<Entrega>();
  isOpen = input.required<boolean>();
  onClose = output<void>();
  onUpdate = output<Entrega>();

  readonly enviado = signal<boolean>(false);
  readonly statusEnvio = signal<ShippingStatus>('AGUARDANDO');
  readonly codigoRastreamento = signal<string>('');
  readonly loading = signal<boolean>(false);
  readonly error = signal<string | null>(null);

  readonly statusOptions: ShippingStatus[] = ['AGUARDANDO', 'ENVIADO', 'RECUSADO', 'ENTREGUE'];

  // Computed para mostrar o dropdown de status apenas quando enviado = true
  readonly showStatusDropdown = computed(() => this.enviado() === true);

  constructor() {
    // Atualizar valores quando o modal abrir ou a entrega mudar
    effect(() => {
      if (this.isOpen() && this.entrega()) {
        const e = this.entrega();
        this.enviado.set(e.enviado);
        this.statusEnvio.set(e.statusEnvio);
        this.codigoRastreamento.set(e.codigoRastreamento || '');
        this.error.set(null);
      }
    });
  }

  onSubmit(): void {
    this.loading.set(true);
    this.error.set(null);

    const request: AtualizarStatusEnvioRequest = {
      enviado: this.enviado(),
      statusEnvio: this.statusEnvio(),
      codigoRastreamento: this.codigoRastreamento().trim() || null,
    };

    this.entregaService.atualizarStatusEnvio(this.entrega().pedidoId, request).subscribe({
      next: (updated) => {
        this.onUpdate.emit(updated);
        this.onClose.emit();
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Erro ao atualizar status:', err);
        this.error.set(err.error?.message || 'Erro ao atualizar status. Tente novamente.');
        this.loading.set(false);
      }
    });
  }

  close(): void {
    this.onClose.emit();
  }

  formatDate = formatDate;
  getWhatsAppLink = getWhatsAppLink;
}

