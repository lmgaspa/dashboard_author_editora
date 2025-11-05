import { Injectable, computed, signal, inject } from '@angular/core';
import { MenuItem } from '../models/menu-item.model';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class MenuService {
  private readonly authService = inject(AuthService);

  private readonly _menuItems = signal<MenuItem[]>([
    // Menu público
    {
      label: 'Login',
      icon: 'login',
      route: '/login',
      roles: []
    },
    {
      label: 'Sobre',
      icon: 'info',
      route: '/about',
      roles: []
    },
    {
      label: 'Suporte',
      icon: 'help',
      route: '/support',
      roles: []
    }
  ]);

  private readonly _userMenuItems = signal<MenuItem[]>([
    {
      label: 'Dashboard',
      icon: 'dashboard',
      route: '/user/dashboard',
      roles: ['USER', 'ADMIN']
    },
    {
      label: 'Meu Perfil',
      icon: 'person',
      route: '/user/profile',
      roles: ['USER', 'ADMIN']
    },
    {
      label: 'Configurações',
      icon: 'settings',
      roles: ['USER', 'ADMIN'],
      children: [
        {
          label: 'Alterar Senha',
          icon: 'lock',
          route: '/user/change-password',
          roles: ['USER', 'ADMIN']
        },
        {
          label: 'Alterar Email',
          icon: 'email',
          route: '/user/change-email',
          roles: ['USER', 'ADMIN']
        }
      ]
    },
    {
      divider: true,
      roles: ['USER', 'ADMIN']
    },
    {
      label: 'Sair',
      icon: 'logout',
      route: '/logout',
      roles: ['USER', 'ADMIN']
    }
  ]);

  private readonly _adminMenuItems = signal<MenuItem[]>([
    {
      label: 'Dashboard',
      icon: 'dashboard',
      route: '/admin/dashboard',
      roles: ['ADMIN']
    },
    {
      label: 'Usuários',
      icon: 'people',
      roles: ['ADMIN'],
      children: [
        {
          label: 'Listar Usuários',
          icon: 'list',
          route: '/admin/users',
          roles: ['ADMIN']
        },
        {
          label: 'Criar Usuário',
          icon: 'person_add',
          route: '/admin/users/create',
          roles: ['ADMIN']
        }
      ]
    },
    {
      label: 'Administradores',
      icon: 'admin_panel_settings',
      route: '/admin/admin-info',
      roles: ['ADMIN']
    },
    {
      label: 'Status do Sistema',
      icon: 'monitoring',
      roles: ['ADMIN'],
      children: [
        {
          label: 'Banco de Dados',
          icon: 'storage',
          route: '/admin/database/status',
          roles: ['ADMIN']
        }
      ]
    },
    {
      divider: true,
      roles: ['ADMIN']
    },
    {
      label: 'Meu Perfil',
      icon: 'person',
      route: '/user/profile',
      roles: ['ADMIN']
    },
    {
      label: 'Configurações',
      icon: 'settings',
      roles: ['ADMIN'],
      children: [
        {
          label: 'Alterar Senha',
          icon: 'lock',
          route: '/user/change-password',
          roles: ['ADMIN']
        },
        {
          label: 'Alterar Email',
          icon: 'email',
          route: '/user/change-email',
          roles: ['ADMIN']
        }
      ]
    },
    {
      divider: true,
      roles: ['ADMIN']
    },
    {
      label: 'Sair',
      icon: 'logout',
      route: '/logout',
      roles: ['ADMIN']
    }
  ]);

  readonly menuItems = computed(() => {
    const isAuthenticated = this.authService.isAuthenticated();
    const userRole = this.authService.currentUser()?.role;

    if (!isAuthenticated) {
      return this._menuItems().filter(item => this.canAccess(item, null));
    }

    if (userRole === 'ADMIN') {
      return this._adminMenuItems().filter(item => this.canAccess(item, userRole));
    }

    return this._userMenuItems().filter(item => this.canAccess(item, userRole));
  });

  readonly sidebarItems = computed(() => {
    const userRole = this.authService.currentUser()?.role;
    
    if (userRole === 'ADMIN') {
      return this._adminMenuItems().filter(item => this.canAccess(item, userRole) && !item.divider);
    }

    return this._userMenuItems().filter(item => this.canAccess(item, userRole) && !item.divider);
  });

  private canAccess(item: MenuItem, role: string | null): boolean {
    if (!item.roles || item.roles.length === 0) {
      return true; // Público
    }

    if (!role) {
      return false; // Requer autenticação
    }

    return item.roles.includes(role);
  }

  getMenuItemsByRole(role: string | null): MenuItem[] {
    if (!role) {
      return this._menuItems().filter(item => this.canAccess(item, null));
    }

    if (role === 'ADMIN') {
      return this._adminMenuItems().filter(item => this.canAccess(item, role));
    }

    return this._userMenuItems().filter(item => this.canAccess(item, role));
  }
}

