import { Component, signal, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { TicketService } from '@/app/core/services/ticket.service';
import { Ticket, TicketStatus, TicketCategory } from '@/app/core/models/ticket.model';
import { ExportButtonsComponent } from '@/app/core/components/export-buttons/export-buttons.component';

@Component({
  selector: 'app-admin-tickets-page',
  standalone: true,
  imports: [CommonModule, RouterModule, ExportButtonsComponent],
  templateUrl: './admin-tickets-page.component.html',
  styles: []
})
export class AdminTicketsPageComponent implements OnInit {
  private readonly ticketService = inject(TicketService);

  readonly tickets = signal<Ticket[]>([]);
  readonly loading = signal<boolean>(false);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.loadTickets();
  }

  loadTickets(): void {
    this.loading.set(true);
    this.error.set(null);

    this.ticketService.listarTodosTicketsAdmin().subscribe({
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
        this.error.set(err.error?.message || 'Erro ao carregar tickets. Tente novamente.');
        this.loading.set(false);
      }
    });
  }

  retry(): void {
    this.loadTickets();
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
      [TicketCategory.ALTERACAO]: 'bg-red-500/20 border-red-500/30 text-red-300',
      [TicketCategory.DUVIDA]: 'bg-blue-500/20 border-blue-500/30 text-blue-300',
      [TicketCategory.OUTRO]: 'bg-gray-500/20 border-gray-500/30 text-gray-300'
    };
    return categoryMap[category] || 'bg-gray-500/20 border-gray-500/30 text-gray-300';
  }

  getCategoryLabel(category: TicketCategory): string {
    const categoryMap: Record<TicketCategory, string> = {
      [TicketCategory.PAGAMENTO]: 'Pagamento',
      [TicketCategory.TECNICO]: 'Técnico',
      [TicketCategory.ALTERACAO]: 'Alteração',
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
    // Admins need to see if there are unread messages from USER?
    // The current logic in user page was checking !msg.readAt && !msg.isInternalNote
    // Adminlogic might be customized later, for now we keep similar behavior or adapt:
    // Admin sees unread messages from user?
    return ticket.messages.some(msg => !msg.readAt); // Simply any unread message
  }
}
