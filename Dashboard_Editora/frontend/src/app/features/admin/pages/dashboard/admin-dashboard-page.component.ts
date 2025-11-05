import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '@/app/core/services/auth.service';

@Component({
  selector: 'app-admin-dashboard-page',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="p-0">
      <h1 class="text-3xl font-bold text-white mb-4">Dashboard Administrativo</h1>
      <p class="text-gray-300 mb-8">Bem-vindo, {{ authService.currentUser()?.name }}!</p>
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mt-8">
        <div class="bg-[color:var(--surface)] border border-[color:var(--border-1)] rounded-[var(--radius-card)] p-6 shadow-[var(--shadow-card)]">
          <h2 class="text-xl font-semibold text-white mb-2">Usuários</h2>
          <p class="text-gray-400">Gerenciar usuários do sistema</p>
        </div>
        <div class="bg-[color:var(--surface)] border border-[color:var(--border-1)] rounded-[var(--radius-card)] p-6 shadow-[var(--shadow-card)]">
          <h2 class="text-xl font-semibold text-white mb-2">Administradores</h2>
          <p class="text-gray-400">Listar administradores</p>
        </div>
        <div class="bg-[color:var(--surface)] border border-[color:var(--border-1)] rounded-[var(--radius-card)] p-6 shadow-[var(--shadow-card)]">
          <h2 class="text-xl font-semibold text-white mb-2">Status do Sistema</h2>
          <p class="text-gray-400">Monitorar banco de dados</p>
        </div>
      </div>
    </div>
  `,
  styles: []
})
export class AdminDashboardPageComponent {
  readonly authService = inject(AuthService);
}

