import { Component, signal, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '@/app/core/services/auth.service';
import { ChangePhotoModalComponent } from '../../components/change-photo-modal/change-photo-modal.component';

@Component({
  selector: 'app-user-profile-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ChangePhotoModalComponent],
  templateUrl: './user-profile-page.component.html',
  styles: []
})
export class UserProfilePageComponent implements OnInit, OnDestroy {
  private readonly fb = inject(FormBuilder);
  readonly authService = inject(AuthService);
  private clickListener?: (e: Event) => void;

  readonly profileForm: FormGroup = this.fb.group({
    name: ['', [Validators.required]],
    email: [{ value: '', disabled: true }, [Validators.required, Validators.email]]
  });

  readonly loading = signal<boolean>(false);
  readonly success = signal<boolean>(false);
  readonly error = signal<string | null>(null);
  readonly showPhotoModal = signal<boolean>(false);
  readonly showPhotoDropdown = signal<boolean>(false);

  ngOnInit(): void {
    // Fechar dropdown ao clicar fora
    this.clickListener = (e: Event) => {
      if (this.showPhotoDropdown()) {
        const target = e.target as HTMLElement;
        // Verificar se o clique foi fora do container do dropdown
        const photoSection = target?.closest('[data-photo-section]');
        if (!photoSection) {
          this.showPhotoDropdown.set(false);
        }
      }
    };
    document.addEventListener('click', this.clickListener);
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

  openPhotoModal(): void {
    this.showPhotoModal.set(true);
    this.showPhotoDropdown.set(false);
  }

  closePhotoModal(): void {
    this.showPhotoModal.set(false);
  }

  togglePhotoDropdown(): void {
    this.showPhotoDropdown.update(v => !v);
  }

  closePhotoDropdown(): void {
    this.showPhotoDropdown.set(false);
  }

  onPhotoUpdated(photoUrl: string | null): void {
    // A foto já foi atualizada pelo AuthService, apenas fechar o modal
    this.closePhotoModal();
    this.closePhotoDropdown();
    this.success.set(true);
    setTimeout(() => this.success.set(false), 3000);
  }

  removePhoto(): void {
    this.loading.set(true);
    this.error.set(null);
    this.closePhotoDropdown();

    this.authService.updateProfile({ profilePhotoUrl: '' }).subscribe({
      next: (updatedUser) => {
        this.loading.set(false);
        this.success.set(true);
        setTimeout(() => this.success.set(false), 3000);
      },
      error: (err) => {
        console.error('Erro ao remover foto:', err);
        this.error.set(err.error?.message || 'Erro ao remover foto de perfil.');
        this.loading.set(false);
      }
    });
  }

  getProfilePhotoUrl(): string | null {
    const user = this.authService.currentUser();
    return user?.profilePhotoUrl || user?.avatar || null;
  }

  getInitials(): string {
    const user = this.authService.currentUser();
    if (!user?.name) return 'U';
    return user.name
      .split(' ')
      .map(n => n[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);
  }

  ngOnDestroy(): void {
    if (this.clickListener) {
      document.removeEventListener('click', this.clickListener);
    }
  }
}

