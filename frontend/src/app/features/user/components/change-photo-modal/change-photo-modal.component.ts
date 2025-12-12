import { Component, signal, inject, input, output, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl } from '@angular/forms';
import { AuthService } from '@/app/core/services/auth.service';

// Validador customizado para URL
function urlValidator(control: AbstractControl): { [key: string]: any } | null {
  if (!control.value || control.value.trim() === '') return null;
  try {
    const url = new URL(control.value);
    return (url.protocol === 'http:' || url.protocol === 'https:') ? null : { invalidUrl: true };
  } catch {
    return { invalidUrl: true };
  }
}

@Component({
  selector: 'app-change-photo-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './change-photo-modal.component.html',
  styles: []
})
export class ChangePhotoModalComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);

  currentPhotoUrl = input<string | null | undefined>(null);
  onClose = output<void>();
  onSuccess = output<string | null>();

  readonly form: FormGroup = this.fb.group({
    photoUrl: ['', [urlValidator]]
  });

  readonly loading = signal<boolean>(false);
  readonly error = signal<string | null>(null);
  readonly uploadMode = signal<'url' | 'file'>('url');
  readonly previewUrl = signal<string | null>(null);
  readonly selectedFile = signal<File | null>(null);

  ngOnInit(): void {
    const current = this.currentPhotoUrl();
    if (current) {
      this.form.patchValue({ photoUrl: current });
      this.previewUrl.set(current);
    }
  }

  switchMode(mode: 'url' | 'file'): void {
    this.uploadMode.set(mode);
    this.error.set(null);
    if (mode === 'file') {
      this.form.get('photoUrl')?.clearValidators();
    } else {
      this.form.get('photoUrl')?.setValidators([urlValidator]);
    }
    this.form.get('photoUrl')?.updateValueAndValidity();
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    
    if (!file) return;

    // Validar tipo de arquivo
    if (!this.isValidImageFile(file)) {
      this.error.set('Formato inválido. Use JPG, PNG, GIF ou WebP.');
      return;
    }

    // Validar tamanho (5MB)
    if (file.size > 5 * 1024 * 1024) {
      this.error.set('Arquivo muito grande. Tamanho máximo: 5MB.');
      return;
    }

    this.selectedFile.set(file);
    this.error.set(null);

    // Criar preview
    const reader = new FileReader();
    reader.onload = (e) => {
      this.previewUrl.set(e.target?.result as string);
    };
    reader.readAsDataURL(file);
  }

  onUrlChange(): void {
    const url = this.form.get('photoUrl')?.value?.trim();
    if (url && this.form.get('photoUrl')?.valid) {
      this.previewUrl.set(url);
      this.error.set(null);
    } else if (!url) {
      this.previewUrl.set(null);
    }
  }

  async save(): Promise<void> {
    this.loading.set(true);
    this.error.set(null);

    try {
      let photoUrl: string | null = null;

      if (this.uploadMode() === 'file') {
        // Upload de arquivo
        const file = this.selectedFile();
        if (!file) {
          this.error.set('Selecione um arquivo para upload.');
          this.loading.set(false);
          return;
        }

        // Conversão para data URL (base64)
        // Nota: Esta é uma solução temporária que funciona, mas para produção
        // seria recomendado usar um serviço de storage (Firebase Storage, AWS S3, Cloudinary, etc.)
        // para armazenar a imagem e obter uma URL pública.
        // 
        // A solução atual funciona porque o backend aceita data URLs (base64) no campo profilePhotoUrl.
        // Se no futuro for necessário migrar para storage service, descomentar e implementar:
        // const publicUrl = await this.storageService.uploadProfilePhoto(file);
        // photoUrl = publicUrl;
        photoUrl = await this.convertFileToDataUrl(file);
      } else {
        // URL input
        const url = this.form.get('photoUrl')?.value?.trim();
        if (url) {
          // Validar URL
          try {
            new URL(url);
            photoUrl = url;
          } catch {
            this.error.set('URL inválida.');
            this.loading.set(false);
            return;
          }
        }
      }

      // Atualizar perfil
      this.authService.updateProfile({ profilePhotoUrl: photoUrl || '' }).subscribe({
        next: (updatedUser) => {
          this.loading.set(false);
          this.onSuccess.emit(updatedUser.profilePhotoUrl || null);
        },
        error: (err) => {
          console.error('Erro ao atualizar foto:', err);
          this.error.set(err.error?.message || 'Erro ao atualizar foto de perfil.');
          this.loading.set(false);
        }
      });
    } catch (error: any) {
      console.error('Erro ao processar foto:', error);
      this.error.set(error.message || 'Erro ao processar foto.');
      this.loading.set(false);
    }
  }

  removePhoto(): void {
    this.loading.set(true);
    this.error.set(null);

    this.authService.updateProfile({ profilePhotoUrl: '' }).subscribe({
      next: (updatedUser) => {
        this.loading.set(false);
        this.onSuccess.emit(null);
      },
      error: (err) => {
        console.error('Erro ao remover foto:', err);
        this.error.set(err.error?.message || 'Erro ao remover foto de perfil.');
        this.loading.set(false);
      }
    });
  }

  close(): void {
    this.onClose.emit();
  }

  private isValidImageFile(file: File): boolean {
    const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
    return validTypes.includes(file.type);
  }

  private convertFileToDataUrl(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = (e) => resolve(e.target?.result as string);
      reader.onerror = reject;
      reader.readAsDataURL(file);
    });
  }
}

