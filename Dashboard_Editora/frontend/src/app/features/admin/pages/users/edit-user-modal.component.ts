import { Component, signal, computed, inject, input, output, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { environment } from '@/environments/environment';

interface User {
  id: string;
  name: string;
  email: string;
  role: string;
  emailConfirmed: boolean;
  authProvider: string;
  authorId?: string | null;
  ecommerceUrl?: string | null;
  ecommerceDbUrl?: string | null;
  ecommerceDbUsername?: string | null;
  ecommerceDbPassword?: string | null;
  profilePhotoUrl?: string | null;
  lookerStudioUrl?: string | null;
}

// Validador customizado para URL
function urlValidator(control: AbstractControl): ValidationErrors | null {
  if (!control.value || control.value.trim() === '') return null;
  try {
    const url = new URL(control.value);
    return url.protocol === 'http:' || url.protocol === 'https:' ? null : { invalidUrl: true };
  } catch {
    return { invalidUrl: true };
  }
}

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
  selector: 'app-edit-user-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './edit-user-modal.component.html',
  styles: []
})
export class EditUserModalComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly http = inject(HttpClient);
  private readonly API_URL = environment.apiUrl;

  user = input.required<User>();
  onClose = output<void>();
  onSuccess = output<User>();

  readonly form: FormGroup = this.fb.group({
    name: ['', [Validators.required]],
    role: ['USER', [Validators.required]],
    authorId: [''],
    ecommerceUrl: ['', [urlValidator]],
    ecommerceDbUrl: [''],
    ecommerceDbUsername: [''],
    ecommerceDbPassword: [''],
    password: [''], // Campo para admin mudar senha do usuário
    profilePhotoUrl: [''],
    lookerStudioUrl: [''] // URL do Looker Studio para métricas
  });

  readonly loading = signal<boolean>(false);
  readonly error = signal<string | null>(null);
  readonly showDbPassword = signal<boolean>(false);
  readonly showPasswordChange = signal<boolean>(false);
  readonly showUserPassword = signal<boolean>(false);
  readonly showUserPasswordChange = signal<boolean>(false);

  // Computed signals para template
  readonly isUserRole = computed(() => this.form.get('role')?.value === 'USER');
  readonly hasAuthorId = computed(() => {
    const authorId = this.form.get('authorId')?.value;
    return authorId && authorId.trim() !== '';
  });

  ngOnInit(): void {
    const userData = this.user();
    
    // Normalizar author_id para authorId (backend pode retornar snake_case)
    const normalizedUser = {
      ...userData,
      authorId: (userData as any).author_id || userData.authorId,
      ecommerceUrl: (userData as any).ecommerce_url || userData.ecommerceUrl,
      ecommerceDbUrl: (userData as any).ecommerce_db_url || userData.ecommerceDbUrl,
      ecommerceDbUsername: (userData as any).ecommerce_db_username || userData.ecommerceDbUsername,
      ecommerceDbPassword: (userData as any).ecommerce_db_password || userData.ecommerceDbPassword,
      profilePhotoUrl: (userData as any).profile_photo_url || userData.profilePhotoUrl,
      lookerStudioUrl: (userData as any).looker_studio_url || userData.lookerStudioUrl
    };
    
    // Preencher formulário com dados do usuário
    this.form.patchValue({
      name: normalizedUser.name || '',
      role: normalizedUser.role || 'USER',
      authorId: normalizedUser.authorId || '',
      ecommerceUrl: normalizedUser.ecommerceUrl || '',
      ecommerceDbUrl: normalizedUser.ecommerceDbUrl || '',
      ecommerceDbUsername: normalizedUser.ecommerceDbUsername || '',
      ecommerceDbPassword: normalizedUser.ecommerceDbPassword || '', // Agora retorna do backend
      password: '', // Sempre vazio - admin precisa preencher para mudar
      profilePhotoUrl: normalizedUser.profilePhotoUrl || '',
      lookerStudioUrl: normalizedUser.lookerStudioUrl || ''
    });
    
    // Configurar validação de senha do usuário
    this.setupPasswordValidation();

    // Configurar validações condicionais
    this.setupConditionalValidations();
  }

  private setupConditionalValidations(): void {
    // Listener para mudanças no role
    this.form.get('role')?.valueChanges.subscribe((role) => {
      const authorIdControl = this.form.get('authorId');
      const ecommerceUrlControl = this.form.get('ecommerceUrl');
      
      if (role === 'ADMIN') {
        authorIdControl?.clearValidators();
        ecommerceUrlControl?.clearValidators();
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
        }
        ecommerceUrlControl?.updateValueAndValidity({ emitEvent: false });
      }
    });

    // Inicializar validações baseado no valor inicial
    const initialRole = this.form.get('role')?.value;
    if (initialRole === 'USER') {
      this.form.get('authorId')?.setValidators([Validators.required]);
      const authorId = this.form.get('authorId')?.value;
      if (authorId) {
        this.form.get('ecommerceUrl')?.setValidators([Validators.required, urlValidator]);
      }
    }
  }

  toggleDbPassword(): void {
    this.showDbPassword.update(v => !v);
  }

  togglePasswordChange(): void {
    this.showPasswordChange.update(v => !v);
    if (!this.showPasswordChange()) {
      this.form.patchValue({ ecommerceDbPassword: '' });
    }
  }

  toggleUserPassword(): void {
    this.showUserPassword.update(v => !v);
  }

  toggleUserPasswordChange(): void {
    this.showUserPasswordChange.update(v => !v);
    const passwordControl = this.form.get('password');
    if (!this.showUserPasswordChange()) {
      passwordControl?.setValue('');
      passwordControl?.clearValidators();
    } else {
      passwordControl?.setValidators([Validators.required, passwordStrengthValidator]);
    }
    passwordControl?.updateValueAndValidity();
  }

  private setupPasswordValidation(): void {
    // Listener para mudanças no toggle de mudança de senha do usuário
    // A validação será aplicada quando o toggle estiver ativo
    const passwordControl = this.form.get('password');
    if (passwordControl) {
      passwordControl.valueChanges.subscribe(() => {
        if (this.showUserPasswordChange() && passwordControl.value) {
          passwordControl.setValidators([Validators.required, passwordStrengthValidator]);
          passwordControl.updateValueAndValidity({ emitEvent: false });
        }
      });
    }
  }

  removeField(fieldName: string): void {
    this.form.patchValue({ [fieldName]: '' });
  }

  onSubmit(): void {
    if (this.form.invalid) {
      Object.keys(this.form.controls).forEach(key => {
        this.form.get(key)?.markAsTouched();
      });
      return;
    }

    this.loading.set(true);
    this.error.set(null);

    const formValue = this.form.value;
    const userData = this.user();

    // Construir payload apenas com campos que foram alterados ou que têm valor
    const payload: any = {};

    // Campos sempre enviados se alterados
    if (formValue.name !== userData.name) {
      payload.name = formValue.name.trim();
    }
    if (formValue.role !== userData.role) {
      payload.role = formValue.role;
    }

    // Campos condicionais para USER
    if (formValue.role === 'USER') {
      // Normalizar authorId do userData também
      const currentAuthorId = (userData as any).author_id || userData.authorId;
      const authorId = formValue.authorId?.trim() || '';
      const ecommerceUrl = formValue.ecommerceUrl?.trim() || '';
      
      // Se authorId mudou ou foi removido
      if (authorId !== (currentAuthorId || '')) {
        payload.authorId = authorId || null;
      }
      
      // Se ecommerceUrl mudou ou foi removido
      const currentEcommerceUrl = (userData as any).ecommerce_url || userData.ecommerceUrl;
      if (ecommerceUrl !== (currentEcommerceUrl || '')) {
        payload.ecommerceUrl = ecommerceUrl || null;
      }

      // Campos opcionais de banco de dados
      if (formValue.ecommerceDbUrl?.trim()) {
        payload.ecommerceDbUrl = formValue.ecommerceDbUrl.trim();
      }
      if (formValue.ecommerceDbUsername?.trim()) {
        payload.ecommerceDbUsername = formValue.ecommerceDbUsername.trim();
      }
      // Só enviar password se o usuário quiser alterar
      if (this.showPasswordChange() && formValue.ecommerceDbPassword?.trim()) {
        payload.ecommerceDbPassword = formValue.ecommerceDbPassword.trim();
      }
    } else {
      // Se mudou para ADMIN, limpar campos de autor
      if (userData.role === 'USER') {
        payload.authorId = null;
        payload.ecommerceUrl = null;
      }
    }

    // Profile photo URL
    const currentProfilePhotoUrl = (userData as any).profile_photo_url || userData.profilePhotoUrl;
    const profilePhotoUrl = formValue.profilePhotoUrl?.trim() || '';
    if (profilePhotoUrl !== (currentProfilePhotoUrl || '')) {
      payload.profilePhotoUrl = profilePhotoUrl || null;
    }

    // Looker Studio URL (opcional, pode ser usado por USER ou ADMIN)
    const currentLookerStudioUrl = (userData as any).looker_studio_url || userData.lookerStudioUrl;
    const lookerStudioUrl = formValue.lookerStudioUrl?.trim() || '';
    if (lookerStudioUrl !== (currentLookerStudioUrl || '')) {
      payload.lookerStudioUrl = lookerStudioUrl || null;
    }

    // Password do usuário (admin pode mudar senha de qualquer usuário)
    if (this.showUserPasswordChange() && formValue.password?.trim()) {
      payload.password = formValue.password.trim();
    }

    console.log('📤 Enviando payload para atualizar usuário:', JSON.stringify(payload, null, 2));

    this.http.put<any>(`${this.API_URL}/api/v1/admin/users/${userData.id}`, payload).subscribe({
      next: (response) => {
        console.log('✅ Usuário atualizado com sucesso:', response);
        // Normalizar author_id para authorId (backend retorna snake_case, frontend usa camelCase)
        const normalizedResponse: User = {
          ...response,
          authorId: response.author_id || response.authorId,
          ecommerceUrl: response.ecommerce_url || response.ecommerceUrl,
          ecommerceDbUrl: response.ecommerce_db_url || response.ecommerceDbUrl,
          ecommerceDbUsername: response.ecommerce_db_username || response.ecommerceDbUsername,
          ecommerceDbPassword: response.ecommerce_db_password || response.ecommerceDbPassword,
          profilePhotoUrl: response.profile_photo_url || response.profilePhotoUrl,
          lookerStudioUrl: response.looker_studio_url || response.lookerStudioUrl
        };
        this.loading.set(false);
        this.onSuccess.emit(normalizedResponse);
      },
      error: (err) => {
        console.error('❌ Erro ao atualizar usuário:', err);
        this.error.set(err.error?.message || 'Erro ao atualizar usuário.');
        this.loading.set(false);
      }
    });
  }

  close(): void {
    this.onClose.emit();
  }
}

