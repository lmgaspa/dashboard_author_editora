import { Component, signal, computed, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '@/app/core/services/auth.service';
import { MenuService } from '@/app/core/services/menu.service';
import { ChangePhotoModalComponent } from '@/app/features/user/components/change-photo-modal/change-photo-modal.component';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [CommonModule, RouterModule, ChangePhotoModalComponent],
  templateUrl: './topbar.component.html',
  styles: []
})
export class TopbarComponent implements OnInit, OnDestroy {
  private readonly authService = inject(AuthService);
  private readonly menuService = inject(MenuService);
  private readonly router = inject(Router);
  private clickListener?: (e: Event) => void;

  readonly currentUser = computed(() => this.authService.currentUser());
  readonly isMenuOpen = signal<boolean>(false);
  readonly sidebarOpen = signal<boolean>(false);
  readonly showPhotoModal = signal<boolean>(false);
  readonly showPhotoDropdown = signal<boolean>(false);

  ngOnInit(): void {
    // Fechar menu ao clicar fora
    this.clickListener = (e: Event) => {
      const target = e.target as HTMLElement;
      
      if (this.isMenuOpen()) {
        if (target && !target.closest('.relative.z-50')) {
          this.isMenuOpen.set(false);
        }
      }
      
      if (this.showPhotoDropdown()) {
        const photoDropdown = target?.closest('[data-photo-dropdown]');
        if (!photoDropdown) {
          this.showPhotoDropdown.set(false);
        }
      }
    };
    document.addEventListener('click', this.clickListener);

    // Sincroniza com eventos da sidebar
    if (typeof window !== 'undefined') {
      window.addEventListener('sidebarToggle', ((e: CustomEvent) => {
        this.sidebarOpen.set(e.detail.isOpen);
      }) as EventListener);
    }
  }

  ngOnDestroy(): void {
    if (this.clickListener) {
      document.removeEventListener('click', this.clickListener);
    }
  }

  toggleMenu(): void {
    this.isMenuOpen.update(v => !v);
  }

  toggleSidebar(): void {
    this.sidebarOpen.update(v => !v);
    const newState = this.sidebarOpen();
    
    // Atualiza localStorage para sincronizar com sidebar
    if (typeof window !== 'undefined') {
      localStorage.setItem('sidebarOpen', String(newState));
      
      // Dispara evento customizado para a sidebar na mesma página
      window.dispatchEvent(new CustomEvent('sidebarToggle', { 
        detail: { isOpen: newState } 
      }));
    }
  }

  logout(): void {
    if (confirm('Tem certeza que deseja sair?')) {
      this.authService.logout();
    }
  }

  getInitials(name: string): string {
    return name
      .split(' ')
      .map(n => n[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);
  }

  togglePhotoDropdown(): void {
    this.showPhotoDropdown.update(v => !v);
    // Fechar menu principal se estiver aberto
    if (this.showPhotoDropdown()) {
      this.isMenuOpen.set(false);
    }
  }

  openPhotoModal(): void {
    this.showPhotoModal.set(true);
    this.showPhotoDropdown.set(false);
  }

  closePhotoModal(): void {
    this.showPhotoModal.set(false);
  }

  onPhotoUpdated(photoUrl: string | null): void {
    // A foto já foi atualizada pelo AuthService
    this.closePhotoModal();
    this.showPhotoDropdown.set(false);
  }

  removePhoto(): void {
    this.showPhotoDropdown.set(false);
    
    if (!confirm('Tem certeza que deseja remover sua foto de perfil?')) {
      return;
    }

    this.authService.updateProfile({ profilePhotoUrl: '' }).subscribe({
      next: () => {
        // Foto já foi atualizada pelo AuthService
      },
      error: (err) => {
        console.error('Erro ao remover foto:', err);
        alert(err.error?.message || 'Erro ao remover foto de perfil.');
      }
    });
  }

  getProfilePhotoUrl(): string | null {
    const user = this.currentUser();
    return user?.profilePhotoUrl || user?.avatar || null;
  }
}

