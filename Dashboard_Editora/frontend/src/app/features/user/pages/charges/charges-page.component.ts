import { Component, signal, inject, OnInit, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MonthlyChargeService } from '@/app/core/services/monthly-charge.service';
import { MonthlyChargeDTO, ChargeStatus } from '@/app/core/models/charge.model';
import {
  formatCurrency,
  formatDate,
  formatMonthYear,
  getStatusColor,
  getStatusText,
  calculateDaysOverdue
} from '@/app/core/utils/charge.utils';

@Component({
  selector: 'app-charges-page',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './charges-page.component.html',
  styles: []
})
export class ChargesPageComponent implements OnInit {
  private readonly chargeService = inject(MonthlyChargeService);

  readonly charges = signal<MonthlyChargeDTO[]>([]);
  readonly loading = signal<boolean>(false);
  readonly error = signal<string | null>(null);
  readonly selectedCharge = signal<MonthlyChargeDTO | null>(null);
  readonly showPixModal = signal<boolean>(false);
  readonly pixCode = signal<string | null>(null);
  readonly pixAmount = signal<number>(0);
  readonly loadingPix = signal<boolean>(false);
  readonly pixError = signal<string | null>(null);

  // Filtros
  readonly statusFilter = signal<ChargeStatus | 'ALL'>('ALL');
  readonly searchTerm = signal<string>('');

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

    // Filtro por status
    if (this.statusFilter() !== 'ALL') {
      filtered = filtered.filter(c => c.status === this.statusFilter());
    }

    // Filtro por busca (mês/ano)
    const search = this.searchTerm().toLowerCase();
    if (search) {
      filtered = filtered.filter(c => {
        const monthYear = formatMonthYear(c.chargeMonth, c.chargeYear).toLowerCase();
        return monthYear.includes(search);
      });
    }

    // Ordenar por data de vencimento (mais recente primeiro)
    return filtered.sort((a, b) => {
      const dateA = new Date(a.dueDate).getTime();
      const dateB = new Date(b.dueDate).getTime();
      return dateB - dateA;
    });
  });

  ngOnInit(): void {
    this.loadCharges();
  }

  loadCharges(): void {
    this.loading.set(true);
    this.error.set(null);

    this.chargeService.listarCobrancasAutor().subscribe({
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

  openPixModal(charge: MonthlyChargeDTO): void {
    this.selectedCharge.set(charge);
    this.showPixModal.set(true);
    this.pixCode.set(null);
    this.pixError.set(null);
    this.loadingPix.set(true);

    this.chargeService.obterPixCode(charge.id).subscribe({
      next: (response) => {
        this.pixCode.set(response.pixCode);
        this.pixAmount.set(response.amount);
        this.loadingPix.set(false);
      },
      error: (err) => {
        console.error('Erro ao obter código PIX:', err);
        this.pixError.set(err.error?.message || 'Erro ao obter código PIX. Tente novamente.');
        this.loadingPix.set(false);
      }
    });
  }

  closePixModal(): void {
    this.showPixModal.set(false);
    this.selectedCharge.set(null);
    this.pixCode.set(null);
    this.pixError.set(null);
  }

  copyPixCode(): void {
    if (this.pixCode()) {
      navigator.clipboard.writeText(this.pixCode()!).then(() => {
        // Feedback visual pode ser adicionado aqui
        alert('Código PIX copiado para a área de transferência!');
      }).catch(err => {
        console.error('Erro ao copiar código PIX:', err);
      });
    }
  }

  createTicketForCharge(charge: MonthlyChargeDTO): void {
    // Redirecionar para criar ticket com relatedChargeId
    const queryParams = { relatedChargeId: charge.id };
    window.location.href = `/user/tickets?${new URLSearchParams(queryParams).toString()}`;
  }

  // Métodos utilitários expostos para o template
  formatCurrency = formatCurrency;
  formatDate = formatDate;
  formatMonthYear = formatMonthYear;
  getStatusColor = getStatusColor;
  getStatusText = getStatusText;
  calculateDaysOverdue = calculateDaysOverdue;
  ChargeStatus = ChargeStatus;
}

