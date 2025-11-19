import { Component, signal, inject, OnInit, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { MonthlyChargeService } from '@/app/core/services/monthly-charge.service';
import { MonthlyChargeDTO, ChargeStatus } from '@/app/core/models/charge.model';
import { CreateChargeModalComponent } from './create-charge-modal.component';
import { ConfirmPaymentModalComponent } from './confirm-payment-modal.component';
import {
  formatCurrency,
  formatDate,
  formatMonthYear,
  getStatusColor,
  getStatusText
} from '@/app/core/utils/charge.utils';
import { environment } from '@/environments/environment';

interface User {
  id: string;
  name: string;
  email: string;
  authorId?: string | null;
}

@Component({
  selector: 'app-admin-charges-page',
  standalone: true,
  imports: [CommonModule, RouterModule, CreateChargeModalComponent, ConfirmPaymentModalComponent],
  templateUrl: './admin-charges-page.component.html',
  styles: []
})
export class AdminChargesPageComponent implements OnInit {
  private readonly chargeService = inject(MonthlyChargeService);
  private readonly http = inject(HttpClient);
  private readonly API_URL = environment.apiUrl;

  readonly charges = signal<MonthlyChargeDTO[]>([]);
  readonly users = signal<User[]>([]);
  readonly loading = signal<boolean>(false);
  readonly error = signal<string | null>(null);
  readonly showCreateModal = signal<boolean>(false);
  readonly confirmingCharge = signal<MonthlyChargeDTO | null>(null);

  // Filtros
  readonly authorFilter = signal<string>('');
  readonly statusFilter = signal<ChargeStatus | 'ALL'>('ALL');

  // Estatísticas
  readonly stats = computed(() => {
    const allCharges = this.charges();
    const pending = allCharges.filter(c => c.status === ChargeStatus.PENDING).length;
    const overdue = allCharges.filter(c => c.status === ChargeStatus.OVERDUE).length;
    const paid = allCharges.filter(c => c.status === ChargeStatus.PAID).length;
    const totalPending = allCharges
      .filter(c => c.status === ChargeStatus.PENDING || c.status === ChargeStatus.OVERDUE)
      .reduce((sum, c) => sum + c.amount, 0);
    const totalPaid = allCharges
      .filter(c => c.status === ChargeStatus.PAID)
      .reduce((sum, c) => sum + c.amount, 0);

    return { pending, overdue, paid, totalPending, totalPaid };
  });

  // Cobranças filtradas
  readonly filteredCharges = computed(() => {
    let filtered = this.charges();

    // Filtro por autor
    if (this.authorFilter()) {
      filtered = filtered.filter(c => c.authorId === this.authorFilter());
    }

    // Filtro por status
    if (this.statusFilter() !== 'ALL') {
      filtered = filtered.filter(c => c.status === this.statusFilter());
    }

    // Ordenar por data de vencimento (mais recente primeiro)
    return filtered.sort((a, b) => {
      const dateA = new Date(a.dueDate).getTime();
      const dateB = new Date(b.dueDate).getTime();
      return dateB - dateA;
    });
  });

  ngOnInit(): void {
    this.loadUsers();
    this.loadCharges();
  }

  loadUsers(): void {
    this.http.get<any>(`${this.API_URL}/api/v1/admin/users`).subscribe({
      next: (response) => {
        const users = response?.users || [];
        const normalizedUsers = users
          .map((user: any) => ({
            ...user,
            authorId: user.author_id || user.authorId
          }))
          .filter((user: User) => user.authorId); // Apenas usuários com authorId
        this.users.set(normalizedUsers);
      },
      error: (err) => {
        console.error('Erro ao carregar usuários:', err);
      }
    });
  }

  loadCharges(): void {
    this.loading.set(true);
    this.error.set(null);

    this.chargeService.listarTodasCobrancas().subscribe({
      next: (charges) => {
        this.charges.set(charges);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Erro ao carregar cobranças:', err);
        this.error.set(err.error?.message || 'Erro ao carregar cobranças. Tente novamente.');
        this.loading.set(false);
      }
    });
  }

  openCreateModal(): void {
    this.showCreateModal.set(true);
  }

  closeCreateModal(): void {
    this.showCreateModal.set(false);
  }

  onChargeCreated(): void {
    this.closeCreateModal();
    this.loadCharges();
  }

  openConfirmModal(charge: MonthlyChargeDTO): void {
    this.confirmingCharge.set(charge);
  }

  closeConfirmModal(): void {
    this.confirmingCharge.set(null);
  }

  onPaymentConfirmed(): void {
    this.closeConfirmModal();
    this.loadCharges();
  }

  // Métodos utilitários expostos para o template
  formatCurrency = formatCurrency;
  formatDate = formatDate;
  formatMonthYear = formatMonthYear;
  getStatusColor = getStatusColor;
  getStatusText = getStatusText;
  ChargeStatus = ChargeStatus;
}

