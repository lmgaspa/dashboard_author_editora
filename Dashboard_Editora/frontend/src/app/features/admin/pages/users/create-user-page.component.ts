import { Component, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '@/environments/environment';

// Validador customizado para URL
function urlValidator(control: AbstractControl): ValidationErrors | null {
  if (!control.value) return null;
  try {
    const url = new URL(control.value);
    return url.protocol === 'http:' || url.protocol === 'https:' ? null : { invalidUrl: true };
  } catch {
    return { invalidUrl: true };
  }
}

@Component({
  selector: 'app-create-user-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './create-user-page.component.html',
  styles: []
})
export class CreateUserPageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly API_URL = environment.apiUrl;

  readonly form: FormGroup = this.fb.group({
    name: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
    role: ['USER', [Validators.required]],
    authorId: [''],
    ecommerceUrl: ['', [urlValidator]],
    ecommerceDbUrl: [''],
    ecommerceDbUsername: [''],
    ecommerceDbPassword: ['']
  });

  readonly loading = signal<boolean>(false);
  readonly error = signal<string | null>(null);
  readonly showPassword = signal<boolean>(false);
  readonly showDbPassword = signal<boolean>(false);

  constructor() {
    // Listener para mudanças no role
    this.form.get('role')?.valueChanges.subscribe((role) => {
      const authorIdControl = this.form.get('authorId');
      const ecommerceUrlControl = this.form.get('ecommerceUrl');
      
      if (role === 'ADMIN') {
        authorIdControl?.clearValidators();
        ecommerceUrlControl?.clearValidators();
        this.form.patchValue({ authorId: '', ecommerceUrl: '' }, { emitEvent: false });
      } else if (role === 'USER') {
        authorIdControl?.setValidators([Validators.required]);
        const authorId = this.form.get('authorId')?.value;
        if (authorId) {
          ecommerceUrlControl?.setValidators([Validators.required, urlValidator]);
        }
      }
      
      authorIdControl?.updateValueAndValidity({ emitEvent: false });
      ecommerceUrlControl?.updateValueAndValidity({ emitEvent: false });
    });

    // Listener para mudanças no authorId
    this.form.get('authorId')?.valueChanges.subscribe((authorId) => {
      const role = this.form.get('role')?.value;
      const ecommerceUrlControl = this.form.get('ecommerceUrl');
      
      if (role === 'USER') {
        if (authorId) {
          ecommerceUrlControl?.setValidators([Validators.required, urlValidator]);
        } else {
          ecommerceUrlControl?.clearValidators();
          this.form.patchValue({ ecommerceUrl: '' }, { emitEvent: false });
        }
        ecommerceUrlControl?.updateValueAndValidity({ emitEvent: false });
      }
    });

    // Inicializar validações baseado no valor inicial
    const initialRole = this.form.get('role')?.value;
    if (initialRole === 'USER') {
      this.form.get('authorId')?.setValidators([Validators.required]);
    }
  }

  onSubmit(): void {
    // Marcar todos os campos como touched para mostrar erros
    if (this.form.invalid) {
      Object.keys(this.form.controls).forEach(key => {
        this.form.get(key)?.markAsTouched();
      });
      console.warn('⚠️ Formulário inválido:', this.form.errors);
      console.warn('⚠️ Erros por campo:', Object.keys(this.form.controls).map(key => ({
        field: key,
        errors: this.form.get(key)?.errors,
        value: this.form.get(key)?.value
      })));
      return;
    }

    this.loading.set(true);
    this.error.set(null);

    // Preparar dados para envio - capturar valores diretamente dos controles
    const formValue = {
      name: this.form.get('name')?.value?.trim() || '',
      email: this.form.get('email')?.value?.trim() || '',
      password: this.form.get('password')?.value || '',
      role: this.form.get('role')?.value || 'USER',
      authorId: this.form.get('authorId')?.value?.trim() || '',
      ecommerceUrl: this.form.get('ecommerceUrl')?.value?.trim() || '',
      ecommerceDbUrl: this.form.get('ecommerceDbUrl')?.value?.trim() || '',
      ecommerceDbUsername: this.form.get('ecommerceDbUsername')?.value?.trim() || '',
      ecommerceDbPassword: this.form.get('ecommerceDbPassword')?.value?.trim() || ''
    };

    console.log('📝 Valores do formulário:', formValue);

    const payload: any = {
      name: formValue.name,
      email: formValue.email,
      password: formValue.password,
      role: formValue.role
    };

    // Adicionar campos de autor apenas se role for USER
    if (formValue.role === 'USER') {
      // Backend espera author_id (snake_case), não authorId (camelCase)
      // author_id e ecommerce_url são obrigatórios para USER - sempre enviar se preenchidos
      if (formValue.authorId) {
        payload.author_id = formValue.authorId;
      }
      if (formValue.ecommerceUrl) {
        payload.ecommerce_url = formValue.ecommerceUrl;
      }
      
      // Campos opcionais de banco de dados - enviar apenas se preenchidos
      if (formValue.ecommerceDbUrl) {
        payload.ecommerceDbUrl = formValue.ecommerceDbUrl;
      }
      if (formValue.ecommerceDbUsername) {
        payload.ecommerceDbUsername = formValue.ecommerceDbUsername;
      }
      if (formValue.ecommerceDbPassword) {
        payload.ecommerceDbPassword = formValue.ecommerceDbPassword;
      }
    }

    // Log para debug
    console.log('📤 Enviando payload para o backend:', JSON.stringify(payload, null, 2));

    this.http.post(`${this.API_URL}/api/v1/admin/users`, payload).subscribe({
      next: (response) => {
        console.log('✅ Resposta do servidor:', response);
        const name = formValue.name;
        this.loading.set(false);
        this.router.navigate(['/admin/users'], {
          state: {
            successMessage: `Usuário ${name} foi criado com sucesso. Um email foi enviado com a senha.`
          }
        });
      },
      error: (err) => {
        console.error('❌ Erro ao criar usuário:', err);
        console.error('📋 Status:', err.status);
        console.error('📋 Mensagem:', err.message);
        console.error('📋 Erro completo:', err);
        console.error('📋 Payload enviado:', JSON.stringify(payload, null, 2));
        this.error.set(err.error?.message || 'Erro ao criar usuário.');
        this.loading.set(false);
      }
    });
  }

  onCancel(): void {
    this.router.navigate(['/admin/users']);
  }

  togglePassword(): void {
    this.showPassword.update(v => !v);
  }

  toggleDbPassword(): void {
    this.showDbPassword.update(v => !v);
  }

  get isUserRole(): boolean {
    return this.form.get('role')?.value === 'USER';
  }

  get hasAuthorId(): boolean {
    return !!this.form.get('authorId')?.value;
  }
}

