import { Component, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '@/app/core/services/auth.service';

// Validador customizado para força de senha
function passwordStrengthValidator(control: AbstractControl): ValidationErrors | null {
  if (!control.value || control.value.trim() === '') return null;
  const password = control.value;
  const errors: ValidationErrors = {};
  
  if (password.length < 8) {
    errors['minLength'] = true;
  }
  if (!/[A-Z]/.test(password)) {
    errors['noUppercase'] = true;
  }
  if (!/[a-z]/.test(password)) {
    errors['noLowercase'] = true;
  }
  if (!/\d/.test(password)) {
    errors['noNumber'] = true;
  }
  
  return Object.keys(errors).length > 0 ? errors : null;
}

@Component({
  selector: 'app-change-password-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './change-password-page.component.html',
  styles: []
})
export class ChangePasswordPageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly form: FormGroup = this.fb.group({
    newPassword: ['', [Validators.required, passwordStrengthValidator]],
    confirmPassword: ['', [Validators.required]]
  }, { validators: this.passwordMatchValidator });

  readonly loading = signal<boolean>(false);
  readonly success = signal<boolean>(false);
  readonly error = signal<string | null>(null);
  readonly showNewPassword = signal<boolean>(false);
  readonly showConfirmPassword = signal<boolean>(false);

  passwordMatchValidator(form: FormGroup) {
    const newPassword = form.get('newPassword');
    const confirmPassword = form.get('confirmPassword');
    if (newPassword && confirmPassword && newPassword.value !== confirmPassword.value) {
      confirmPassword.setErrors({ passwordMismatch: true });
    }
    return null;
  }

  toggleNewPassword(): void {
    this.showNewPassword.update(v => !v);
  }

  toggleConfirmPassword(): void {
    this.showConfirmPassword.update(v => !v);
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    this.loading.set(true);
    this.error.set(null);

    const payload = {
      newPassword: this.form.value.newPassword
    };

    this.authService.changePassword(payload).subscribe({
      next: () => {
        this.success.set(true);
        this.loading.set(false);
        setTimeout(() => {
          this.router.navigate(['/user/profile']);
        }, 2000);
      },
      error: (err) => {
        this.error.set(err.error?.message || 'Erro ao alterar senha.');
        this.loading.set(false);
      }
    });
  }
}

