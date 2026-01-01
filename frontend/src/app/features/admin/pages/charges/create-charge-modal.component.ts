import { Component, signal, inject, input, output, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { MonthlyChargeService } from '@/app/core/services/monthly-charge.service';
import { CreateChargeRequest } from '@/app/core/models/charge.model';
import { environment } from '@/environments/environment';

interface User {
  id: string;
  name: string;
  email: string;
  authorId?: string | null;
}

@Component({
  selector: 'app-create-charge-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './create-charge-modal.component.html',
  styles: []
})
export class CreateChargeModalComponent implements OnInit {
  private readonly chargeService = inject(MonthlyChargeService);
  private readonly http = inject(HttpClient);
  private readonly fb = inject(FormBuilder);
  private readonly API_URL = environment.apiUrl;

  readonly isOpen = input<boolean>(false);
  readonly closed = output<void>();
  readonly chargeCreated = output<void>();

  readonly form: FormGroup = this.fb.group({
    authorId: ['', [Validators.required]],
    chargeMonth: [new Date().getMonth() + 1, [Validators.required, Validators.min(1), Validators.max(12)]],
    chargeYear: [new Date().getFullYear(), [Validators.required, Validators.min(2020), Validators.max(2100)]],
    amount: ['', [Validators.required, Validators.pattern(/^\d{1,4},\d{2}$/)]],
    dueDate: ['', [Validators.required]]
  });

  readonly users = signal<User[]>([]);
  readonly loading = signal<boolean>(false);
  readonly error = signal<string | null>(null);

  readonly months = [
    { value: 1, label: 'Janeiro' },
    { value: 2, label: 'Fevereiro' },
    { value: 3, label: 'Março' },
    { value: 4, label: 'Abril' },
    { value: 5, label: 'Maio' },
    { value: 6, label: 'Junho' },
    { value: 7, label: 'Julho' },
    { value: 8, label: 'Agosto' },
    { value: 9, label: 'Setembro' },
    { value: 10, label: 'Outubro' },
    { value: 11, label: 'Novembro' },
    { value: 12, label: 'Dezembro' }
  ];

  readonly years: number[] = [];
  readonly currentYear = new Date().getFullYear();

  ngOnInit(): void {
    // Gerar anos (do ano atual até 5 anos no futuro)
    for (let i = 0; i <= 5; i++) {
      this.years.push(this.currentYear + i);
    }

    this.loadUsers();
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
        this.error.set('Erro ao carregar lista de autores.');
      }
    });
  }

  close(): void {
    this.form.reset();
    this.error.set(null);
    this.closed.emit();
  }

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading.set(true);
    this.error.set(null);

    const formValue = this.form.value;
    
    // Converter valor "100,00" para 100.00
    const amountVal = parseFloat(formValue.amount.replace(',', '.'));

    const request: CreateChargeRequest = {
      authorId: formValue.authorId,
      chargeMonth: parseInt(formValue.chargeMonth),
      chargeYear: parseInt(formValue.chargeYear),
      amount: amountVal,
      dueDate: formValue.dueDate
    };

    this.chargeService.criarCobranca(request).subscribe({
      next: () => {
        this.loading.set(false);
        this.chargeCreated.emit();
        this.close();
      },
      error: (err) => {
        console.error('Erro ao criar cobrança:', err);
        this.error.set(err.error?.message || 'Erro ao criar cobrança. Tente novamente.');
        this.loading.set(false);
      }
    });
  }

  getFieldError(fieldName: string): string {
    const field = this.form.get(fieldName);
    if (field?.hasError('required') && field.touched) {
      return 'Este campo é obrigatório';
    }
    if (field?.hasError('min') && field.touched) {
      return `Valor mínimo: ${field.errors?.['min'].min}`;
    }
    if (field?.hasError('max') && field.touched) {
      return `Valor máximo: ${field.errors?.['max'].max}`;
    }
    if (field?.hasError('pattern') && field.touched) {
      return 'Formato inválido. Use xx,xx (máx 4 dígitos inteiros)';
    }
    return '';
  }
}

