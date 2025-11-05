import { Component, signal, computed, inject } from '@angular/core';
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
}

