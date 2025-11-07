import { Component, signal, computed, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '@/app/core/services/auth.service';
import { MenuService } from '@/app/core/services/menu.service';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [CommonModule, RouterModule],
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

  ngOnInit(): void {
    // Fechar menu ao clicar fora
    this.clickListener = (e: Event) => {
      if (this.isMenuOpen()) {
        const target = e.target as HTMLElement;
        if (target && !target.closest('.relative.z-50')) {
          this.isMenuOpen.set(false);
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
}

