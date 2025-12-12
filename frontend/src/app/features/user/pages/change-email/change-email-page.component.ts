import { Component, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '@/app/core/services/auth.service';

@Component({
  selector: 'app-change-email-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './change-email-page.component.html',
  styles: []
})
export class ChangeEmailPageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly form: FormGroup = this.fb.group({
    newEmail: ['', [Validators.required, Validators.email]]
  });

  readonly loading = signal<boolean>(false);
  readonly success = signal<boolean>(false);
  readonly error = signal<string | null>(null);

  onSubmit(): void {
    if (this.form.invalid) return;

    this.loading.set(true);
    this.error.set(null);

    this.authService.changeEmail(this.form.value).subscribe({
      next: (response) => {
        this.success.set(true);
        this.loading.set(false);
        setTimeout(() => {
          this.router.navigate(['/user/profile']);
        }, 3000);
      },
      error: (err) => {
        this.error.set(err.error?.message || 'Erro ao solicitar alteração de email. Verifique o novo email e tente novamente.');
        this.loading.set(false);
      }
    });
  }
}

