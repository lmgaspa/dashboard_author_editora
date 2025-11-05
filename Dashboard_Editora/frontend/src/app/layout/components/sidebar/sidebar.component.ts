import { Component, signal, computed, inject, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { MenuService } from '@/app/core/services/menu.service';
import { AuthService } from '@/app/core/services/auth.service';
import { MenuItem } from '@/app/core/models/menu-item.model';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.component.html',
  styles: []
})
export class SidebarComponent {
  private readonly menuService = inject(MenuService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly menuItems = computed(() => this.menuService.sidebarItems());
  readonly expandedItems = signal<Set<string>>(new Set());
  readonly isOpen = signal<boolean>(false);

  constructor() {
    // Sincroniza com localStorage e window events
    if (typeof window !== 'undefined') {
      // Em desktop (lg+), sidebar sempre aberta
      // Em mobile, verifica localStorage ou padrão fechada
      const checkScreenSize = () => {
        if (window.innerWidth >= 1024) {
          this.isOpen.set(true);
        } else {
          const stored = localStorage.getItem('sidebarOpen');
          this.isOpen.set(stored === 'true');
        }
      };

      checkScreenSize();
      
      // Escuta mudanças de tamanho da tela
      window.addEventListener('resize', checkScreenSize);

      // Escuta mudanças no localStorage (cross-component sync)
      window.addEventListener('storage', (e) => {
        if (e.key === 'sidebarOpen' && window.innerWidth < 1024) {
          this.isOpen.set(e.newValue === 'true');
        }
      });

      // Escuta cliques fora da sidebar em mobile
      document.addEventListener('click', (e) => {
        const target = e.target as HTMLElement;
        if (this.isOpen() && window.innerWidth < 1024) {
          if (!target.closest('aside') && !target.closest('button[aria-label="Toggle menu"]')) {
            this.closeSidebar();
          }
        }
      });
    }
  }

  isActive(route: string | undefined): boolean {
    if (!route) return false;
    return this.router.url === route || this.router.url.startsWith(route + '/');
  }

  toggleExpand(item: MenuItem): void {
    if (!item.children || item.children.length === 0) return;
    if (!item.label) return;
    
    const expanded = new Set(this.expandedItems());
    if (expanded.has(item.label)) {
      expanded.delete(item.label);
    } else {
      expanded.add(item.label);
    }
    this.expandedItems.set(expanded);
  }

  isExpanded(item: MenuItem): boolean {
    if (!item.label) return false;
    return this.expandedItems().has(item.label);
  }

  hasActiveChild(item: MenuItem): boolean {
    if (!item.children) return false;
    return item.children.some(child => this.isActive(child.route));
  }

  handleLogout(item: MenuItem): void {
    if (item.route === '/logout') {
      if (confirm('Tem certeza que deseja sair?')) {
        this.authService.logout();
      }
    }
  }

  closeSidebar(): void {
    if (typeof window !== 'undefined' && window.innerWidth < 1024) {
      this.isOpen.set(false);
      localStorage.setItem('sidebarOpen', 'false');
    }
  }
}

