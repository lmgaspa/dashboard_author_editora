import { Component, signal, inject, OnInit, computed } from '@angular/core';
import { CommonModule, CurrencyPipe, NgClass } from '@angular/common';
import { EntregaService } from '@/app/core/services/entrega.service';
import { Entrega, ShippingStatus } from '@/app/core/models/entrega.model';
import { formatDate } from '@/app/core/utils/charge.utils';
import { getWhatsAppLink } from '@/app/core/utils/order.utils';
import { StatusMercadoriaModalComponent } from './status-mercadoria-modal.component';
import { ExportButtonsComponent } from '@/app/core/components/export-buttons/export-buttons.component';

@Component({
  selector: 'app-entregas-page',
  standalone: true,
  imports: [CommonModule, CurrencyPipe, NgClass, StatusMercadoriaModalComponent, ExportButtonsComponent],
  templateUrl: './entregas-page.component.html',
  styles: []
})
export class EntregasPageComponent implements OnInit {
  private readonly entregaService = inject(EntregaService);

  readonly allEntregas = signal<Entrega[]>([]);
  readonly loading = signal<boolean>(false);
  readonly error = signal<string | null>(null);
  readonly selectedEntrega = signal<Entrega | null>(null);
  readonly showModal = signal<boolean>(false);
  readonly activeTab = signal<'ativas' | 'arquivadas'>('ativas');

  // Filtrar entregas baseado na tab ativa
  readonly entregasAtivas = computed(() => 
    this.allEntregas().filter(e => e.statusEnvio !== 'ENTREGUE')
  );

  readonly entregasArquivadas = computed(() => 
    this.allEntregas().filter(e => e.statusEnvio === 'ENTREGUE')
  );

  readonly entregasExibidas = computed(() => 
    this.activeTab() === 'ativas' ? this.entregasAtivas() : this.entregasArquivadas()
  );

  ngOnInit(): void {
    // Carregar imediatamente sem verificações desnecessárias
    // O backend identifica automaticamente o author_id via token JWT
    this.loadEntregas();
  }

  loadEntregas(): void {
    // Não mostrar loading se já tiver dados (evita flicker)
    if (this.allEntregas().length === 0) {
      this.loading.set(true);
    }
    this.error.set(null);

    this.entregaService.listarEntregas().subscribe({
      next: (data) => {
        this.allEntregas.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Erro ao carregar entregas:', err);
        this.error.set(err.error?.message || 'Erro ao carregar entregas. Tente novamente.');
        this.loading.set(false);
      }
    });
  }

  openModal(entrega: Entrega): void {
    this.selectedEntrega.set(entrega);
    this.showModal.set(true);
  }

  closeModal(): void {
    this.showModal.set(false);
    this.selectedEntrega.set(null);
  }

  onUpdate(updated: Entrega): void {
    // Limpar cache e recarregar para garantir dados atualizados
    this.entregaService.clearCache();
    
    // Atualizar a entrega na lista
    const entregas = this.allEntregas();
    const index = entregas.findIndex(e => e.pedidoId === updated.pedidoId);
    if (index !== -1) {
      entregas[index] = updated;
      this.allEntregas.set([...entregas]);
    }
  }

  setActiveTab(tab: 'ativas' | 'arquivadas'): void {
    this.activeTab.set(tab);
  }

  getStatusBadgeClass(status: ShippingStatus): string {
    switch (status) {
      case 'ENVIADO':
        return 'bg-green-500/20 text-green-600 dark:text-green-400';
      case 'AGUARDANDO':
        return 'bg-yellow-500/20 text-yellow-600 dark:text-yellow-400';
      case 'RECUSADO':
        return 'bg-red-500/20 text-red-600 dark:text-red-400';
      case 'ENTREGUE':
        return 'bg-blue-500/20 text-blue-600 dark:text-blue-400';
      default:
        return 'bg-gray-500/20 text-gray-600 dark:text-gray-400';
    }
  }

  getStatusLabel(status: ShippingStatus): string {
    switch (status) {
      case 'ENVIADO':
        return 'Enviado';
      case 'AGUARDANDO':
        return 'Aguardando';
      case 'RECUSADO':
        return 'Recusado';
      case 'ENTREGUE':
        return 'Entregue';
      default:
        return status;
    }
  }

  formatDate = formatDate;
  getWhatsAppLink = getWhatsAppLink;
}

