import { Component, signal, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TicketService } from '@/app/core/services/ticket.service';
import { AuthService } from '@/app/core/services/auth.service';
import { Ticket, TicketStatus, TicketCategory } from '@/app/core/models/ticket.model';
import { CreateTicketModalComponent } from './create-ticket-modal.component';

@Component({
  selector: 'app-tickets-page',
  standalone: true,
  imports: [CommonModule, RouterModule, CreateTicketModalComponent],
  templateUrl: './tickets-page.component.html',
  styles: []
})
export class TicketsPageComponent implements OnInit, OnDestroy {
  private readonly ticketService = inject(TicketService);
  readonly authService = inject(AuthService);

  readonly tickets = signal<Ticket[]>([]);
  readonly loading = signal<boolean>(false);
  readonly error = signal<string | null>(null);
  readonly showCreateModal = signal<boolean>(false);

  readonly isAdmin = signal<boolean>(false);

  ngOnInit(): void {
    this.isAdmin.set(this.authService.isAdmin());
    this.loadTickets();
  }

  ngOnDestroy(): void {
    // Cleanup se necessário
  }

  loadTickets(): void {
    this.loading.set(true);
    this.error.set(null);

    this.ticketService.listarTickets().subscribe({
      next: (data) => {
        // Ordenar por data de atualização (mais recente primeiro)
        const sorted = [...data].sort((a, b) => {
          const dateA = new Date(a.updatedAt).getTime();
          const dateB = new Date(b.updatedAt).getTime();
          return dateB - dateA;
        });
        this.tickets.set(sorted);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Erro ao carregar tickets:', err);
        const errorMessage = this.getErrorMessage(err);
        this.error.set(errorMessage);
        this.loading.set(false);
      }
    });
  }

  private getErrorMessage(err: any): string {
    if (err.status === 401) {
      return 'Você não está autenticado. Por favor, faça login novamente.';
    }
    if (err.status === 403) {
      return 'Você não possui author_id configurado. Entre em contato com o administrador.';
    }
    if (err.status === 500) {
      return err.error?.message || 'Erro ao buscar tickets. Tente novamente mais tarde.';
    }
    return err.error?.message || 'Erro ao carregar tickets. Tente novamente.';
  }

  formatDate(dateString: string): string {
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

  getStatusBadgeClass(status: TicketStatus): string {
    const statusMap: Record<TicketStatus, string> = {
      [TicketStatus.OPEN]: 'bg-blue-500/20 border-blue-500/30 text-blue-300',
      [TicketStatus.IN_PROGRESS]: 'bg-amber-500/20 border-amber-500/30 text-amber-300',
      [TicketStatus.WAITING_AUTHOR]: 'bg-orange-500/20 border-orange-500/30 text-orange-300',
      [TicketStatus.WAITING_ADMIN]: 'bg-sky-500/20 border-sky-500/30 text-sky-300',
      [TicketStatus.RESOLVED]: 'bg-emerald-500/20 border-emerald-500/30 text-emerald-300',
      [TicketStatus.CLOSED]: 'bg-gray-500/20 border-gray-500/30 text-gray-300'
    };
    return statusMap[status] || 'bg-gray-500/20 border-gray-500/30 text-gray-300';
  }

  getStatusLabel(status: TicketStatus): string {
    const statusMap: Record<TicketStatus, string> = {
      [TicketStatus.OPEN]: 'Aberto',
      [TicketStatus.IN_PROGRESS]: 'Em Progresso',
      [TicketStatus.WAITING_AUTHOR]: 'Aguardando Autor',
      [TicketStatus.WAITING_ADMIN]: 'Aguardando Admin',
      [TicketStatus.RESOLVED]: 'Resolvido',
      [TicketStatus.CLOSED]: 'Fechado'
    };
    return statusMap[status] || status;
  }

  getCategoryBadgeClass(category: TicketCategory): string {
    const categoryMap: Record<TicketCategory, string> = {
      [TicketCategory.PAGAMENTO]: 'bg-emerald-500/20 border-emerald-500/30 text-emerald-300',
      [TicketCategory.TECNICO]: 'bg-red-500/20 border-red-500/30 text-red-300',
      [TicketCategory.DUVIDA]: 'bg-blue-500/20 border-blue-500/30 text-blue-300',
      [TicketCategory.OUTRO]: 'bg-gray-500/20 border-gray-500/30 text-gray-300'
    };
    return categoryMap[category] || 'bg-gray-500/20 border-gray-500/30 text-gray-300';
  }

  getCategoryLabel(category: TicketCategory): string {
    const categoryMap: Record<TicketCategory, string> = {
      [TicketCategory.PAGAMENTO]: 'Pagamento',
      [TicketCategory.TECNICO]: 'Técnico',
      [TicketCategory.DUVIDA]: 'Dúvida',
      [TicketCategory.OUTRO]: 'Outro'
    };
    return categoryMap[category] || category;
  }

  getPriorityBadgeClass(priority?: string): string {
    if (!priority) return '';
    const priorityMap: Record<string, string> = {
      LOW: 'bg-gray-500/20 border-gray-500/30 text-gray-300',
      MEDIUM: 'bg-amber-500/20 border-amber-500/30 text-amber-300',
      HIGH: 'bg-red-500/20 border-red-500/30 text-red-300'
    };
    return priorityMap[priority] || '';
  }

  getPriorityLabel(priority?: string): string {
    if (!priority) return '';
    const priorityMap: Record<string, string> = {
      LOW: 'Baixa',
      MEDIUM: 'Média',
      HIGH: 'Alta'
    };
    return priorityMap[priority] || priority;
  }

  hasUnreadMessages(ticket: Ticket): boolean {
    if (!ticket.messages || ticket.messages.length === 0) return false;
    // Verificar se há mensagens não lidas (readAt === null)
    // e que não sejam notas internas (autores não veem)
    return ticket.messages.some(
      msg => !msg.readAt && !msg.isInternalNote
    );
  }

  retry(): void {
    this.loadTickets();
  }

  openCreateModal(): void {
    this.showCreateModal.set(true);
  }

  closeCreateModal(): void {
    this.showCreateModal.set(false);
  }

  onTicketCreated(): void {
    this.closeCreateModal();
    this.loadTickets();
  }
}

