import { Component, signal, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { TicketService } from '@/app/core/services/ticket.service';
import { AuthService } from '@/app/core/services/auth.service';
import { Ticket, TicketStatus, TicketCategory, TicketPriority, CreateMessageRequest } from '@/app/core/models/ticket.model';

@Component({
  selector: 'app-ticket-detail-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './ticket-detail-page.component.html',
  styles: []
})
export class TicketDetailPageComponent implements OnInit, OnDestroy {
  private readonly ticketService = inject(TicketService);
  private readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  readonly ticket = signal<Ticket | null>(null);
  readonly loading = signal<boolean>(false);
  readonly error = signal<string | null>(null);
  readonly sendingMessage = signal<boolean>(false);
  readonly resolving = signal<boolean>(false);

  readonly messageForm: FormGroup = this.fb.group({
    message: ['', [Validators.required, Validators.maxLength(5000)]]
  });

  readonly isAdmin = signal<boolean>(false);

  ngOnInit(): void {
    this.isAdmin.set(this.authService.isAdmin());
    const ticketId = this.route.snapshot.paramMap.get('id');
    if (ticketId) {
      this.loadTicket(ticketId);
    } else {
      this.error.set('ID do ticket não fornecido');
    }
  }

  ngOnDestroy(): void {
    // Cleanup
  }

  loadTicket(ticketId: string): void {
    this.loading.set(true);
    this.error.set(null);

    this.ticketService.obterTicket(ticketId).subscribe({
      next: (data) => {
        this.ticket.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Erro ao carregar ticket:', err);
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
      return 'Você não tem permissão para visualizar este ticket.';
    }
    if (err.status === 404) {
      return 'Ticket não encontrado.';
    }
    return err.error?.message || 'Erro ao carregar ticket. Tente novamente.';
  }

  sendMessage(): void {
    if (this.messageForm.invalid || !this.ticket()) {
      return;
    }

    this.sendingMessage.set(true);
    const ticketId = this.ticket()!.id;
    const request: CreateMessageRequest = {
      message: this.messageForm.value.message.trim(),
      isInternalNote: false // Apenas admin pode usar true
    };

    this.ticketService.adicionarMensagem(ticketId, request).subscribe({
      next: () => {
        this.messageForm.reset();
        this.sendingMessage.set(false);
        this.loadTicket(ticketId); // Recarregar ticket para ver nova mensagem
      },
      error: (err) => {
        console.error('Erro ao enviar mensagem:', err);
        this.sendingMessage.set(false);
        alert(err.error?.message || 'Erro ao enviar mensagem. Tente novamente.');
      }
    });
  }

  resolveTicket(): void {
    if (!this.ticket() || !confirm('Tem certeza que deseja marcar este ticket como resolvido?')) {
      return;
    }

    this.resolving.set(true);
    const ticketId = this.ticket()!.id;

    this.ticketService.marcarComoResolvido(ticketId).subscribe({
      next: () => {
        this.resolving.set(false);
        this.loadTicket(ticketId); // Recarregar ticket
      },
      error: (err) => {
        console.error('Erro ao marcar como resolvido:', err);
        this.resolving.set(false);
        alert(err.error?.message || 'Erro ao marcar ticket como resolvido. Tente novamente.');
      }
    });
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

  getPriorityBadgeClass(priority?: TicketPriority): string {
    if (!priority) return '';
    const priorityMap: Record<TicketPriority, string> = {
      [TicketPriority.LOW]: 'bg-gray-500/20 border-gray-500/30 text-gray-300',
      [TicketPriority.MEDIUM]: 'bg-amber-500/20 border-amber-500/30 text-amber-300',
      [TicketPriority.HIGH]: 'bg-red-500/20 border-red-500/30 text-red-300'
    };
    return priorityMap[priority] || '';
  }

  getPriorityLabel(priority?: TicketPriority): string {
    if (!priority) return '';
    const priorityMap: Record<TicketPriority, string> = {
      [TicketPriority.LOW]: 'Baixa',
      [TicketPriority.MEDIUM]: 'Média',
      [TicketPriority.HIGH]: 'Alta'
    };
    return priorityMap[priority] || priority;
  }

  canResolve(): boolean {
    const ticket = this.ticket();
    if (!ticket) return false;
    // Pode resolver se não estiver já resolvido ou fechado
    return ticket.status !== TicketStatus.RESOLVED && ticket.status !== TicketStatus.CLOSED;
  }

  isAuthorMessage(messageUserId: string): boolean {
    return messageUserId === this.authService.currentUser()?.id;
  }

  canSendMessage(): boolean {
    const ticket = this.ticket();
    if (!ticket) return false;
    // Não pode enviar mensagem se estiver fechado
    return ticket.status !== TicketStatus.CLOSED;
  }
}

