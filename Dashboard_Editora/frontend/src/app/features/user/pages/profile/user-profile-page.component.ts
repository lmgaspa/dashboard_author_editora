import { Component, signal, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '@/app/core/services/auth.service';

@Component({
  selector: 'app-user-profile-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './user-profile-page.component.html',
  styles: []
})
export class UserProfilePageComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);

  readonly profileForm: FormGroup = this.fb.group({
    name: ['', [Validators.required]],
    email: [{ value: '', disabled: true }, [Validators.required, Validators.email]]
  });

  readonly loading = signal<boolean>(false);
  readonly success = signal<boolean>(false);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    const user = this.authService.currentUser();
    if (user) {
      this.profileForm.patchValue({
        name: user.name
      });
      // Para campos disabled, precisamos usar setValue ou habilitar temporariamente
      this.profileForm.get('email')?.setValue(user.email, { emitEvent: false });
    }
  }

  onSubmit(): void {
    if (this.profileForm.invalid) {
      console.warn('⚠️ Formulário inválido');
      return;
    }

    this.loading.set(true);
    this.error.set(null);
    this.success.set(false);

    const name = this.profileForm.get('name')?.value;
    console.log('📤 Enviando atualização de perfil:', { name });

    this.authService.updateProfile({ name }).subscribe({
      next: (updatedUser) => {
        console.log('✅ Perfil atualizado com sucesso:', updatedUser);
        this.success.set(true);
        this.loading.set(false);
        
        // Atualizar o formulário com os dados atualizados
        this.profileForm.patchValue({
          name: updatedUser.name
        });
        // Atualizar email (campo disabled)
        this.profileForm.get('email')?.setValue(updatedUser.email, { emitEvent: false });
        
        setTimeout(() => this.success.set(false), 3000);
      },
      error: (err) => {
        console.error('❌ Erro ao atualizar perfil:', err);
        console.error('📋 Status:', err.status);
        console.error('📋 Message:', err.message);
        console.error('📋 Error object:', err.error);
        
        const errorMessage = err.error?.message || 
                            err.message || 
                            'Erro ao atualizar perfil. Tente novamente.';
        
        this.error.set(errorMessage);
        this.loading.set(false);
      }
    });
  }
}

