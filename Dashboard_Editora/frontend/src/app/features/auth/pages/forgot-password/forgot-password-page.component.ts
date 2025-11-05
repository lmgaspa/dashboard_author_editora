import { Component, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AuthService } from '@/app/core/services/auth.service';

@Component({
  selector: 'app-forgot-password-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './forgot-password-page.component.html',
  styleUrl: './forgot-password-page.component.scss'
})
export class ForgotPasswordPageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);

  readonly form: FormGroup = this.fb.group({
    email: ['', [Validators.required, Validators.email]]
  });

  readonly loading = signal<boolean>(false);
  readonly success = signal<boolean>(false);
  readonly error = signal<string | null>(null);

  onSubmit(): void {
    if (this.form.invalid) return;

    this.loading.set(true);
    this.error.set(null);

    this.authService.forgotPassword(this.form.value.email).subscribe({
      next: () => {
        this.success.set(true);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(err.error?.message || 'Erro ao enviar email de recuperação.');
        this.loading.set(false);
      }
    });
  }
}

