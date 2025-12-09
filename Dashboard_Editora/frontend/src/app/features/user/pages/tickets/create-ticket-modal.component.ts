import { Component, signal, inject, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { TicketService } from '@/app/core/services/ticket.service';
import { TicketCategory, CreateTicketRequest } from '@/app/core/models/ticket.model';

@Component({
  selector: 'app-create-ticket-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './create-ticket-modal.component.html',
  styles: []
})
export class CreateTicketModalComponent {
  private readonly fb = inject(FormBuilder);
  private readonly ticketService = inject(TicketService);

  onClose = output<void>();
  onSuccess = output<void>();

  readonly form: FormGroup = this.fb.group({
    title: ['', [Validators.required, Validators.maxLength(255)]],
    description: ['', [Validators.required, Validators.maxLength(5000)]],
    category: [TicketCategory.OUTRO]
  });

  readonly loading = signal<boolean>(false);
  readonly error = signal<string | null>(null);

  readonly categories = [
    { value: TicketCategory.PAGAMENTO, label: 'Pagamento' },
    { value: TicketCategory.TECNICO, label: 'Técnico' },
    { value: TicketCategory.ALTERACAO, label: 'Alteração' },
    { value: TicketCategory.DUVIDA, label: 'Dúvida' },
    { value: TicketCategory.OUTRO, label: 'Outro' }
  ];

  onSubmit(): void {
    if (this.form.invalid) {
      Object.keys(this.form.controls).forEach(key => {
        this.form.get(key)?.markAsTouched();
      });
      return;
    }

    this.loading.set(true);
    this.error.set(null);

    const request: CreateTicketRequest = {
      title: this.form.value.title.trim(),
      description: this.form.value.description.trim(),
      category: this.form.value.category
    };

    this.ticketService.criarTicket(request).subscribe({
      next: () => {
        this.loading.set(false);
        this.onSuccess.emit();
      },
      error: (err) => {
        console.error('Erro ao criar ticket:', err);
        this.error.set(err.error?.message || 'Erro ao criar ticket. Tente novamente.');
        this.loading.set(false);
      }
    });
  }

  close(): void {
    this.onClose.emit();
  }
}

