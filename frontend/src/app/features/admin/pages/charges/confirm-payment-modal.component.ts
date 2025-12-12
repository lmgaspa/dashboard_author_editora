import { Component, signal, input, output, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MonthlyChargeService } from '@/app/core/services/monthly-charge.service';
import { MonthlyChargeDTO } from '@/app/core/models/charge.model';
import { formatCurrency, formatDate, formatMonthYear } from '@/app/core/utils/charge.utils';

@Component({
  selector: 'app-confirm-payment-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './confirm-payment-modal.component.html',
  styles: []
})
export class ConfirmPaymentModalComponent {
  private readonly chargeService = inject(MonthlyChargeService);
  private readonly fb = inject(FormBuilder);

  readonly charge = input<MonthlyChargeDTO | null>(null);
  readonly closed = output<void>();
  readonly paymentConfirmed = output<void>();

  readonly form: FormGroup = this.fb.group({
    notes: ['']
  });

  readonly loading = signal<boolean>(false);
  readonly error = signal<string | null>(null);

  readonly isOpen = computed(() => this.charge() !== null);

  close(): void {
    this.closed.emit();
  }

  onSubmit(): void {
    const charge = this.charge();
    if (!charge) return;

    this.loading.set(true);
    this.error.set(null);

    const notes = this.form.value.notes || '';

    this.chargeService.confirmarPagamento(charge.id, notes).subscribe({
      next: () => {
        this.loading.set(false);
        this.paymentConfirmed.emit();
        this.close();
      },
      error: (err) => {
        console.error('Erro ao confirmar pagamento:', err);
        this.error.set(err.error?.message || 'Erro ao confirmar pagamento. Tente novamente.');
        this.loading.set(false);
      }
    });
  }

  formatCurrency = formatCurrency;
  formatDate = formatDate;
  formatMonthYear = formatMonthYear;
}

