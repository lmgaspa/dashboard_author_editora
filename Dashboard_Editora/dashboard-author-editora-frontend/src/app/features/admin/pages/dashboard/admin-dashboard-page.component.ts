import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '@/app/core/services/auth.service';

@Component({
  selector: 'app-admin-dashboard-page',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="admin-dashboard">
      <h1>Dashboard Administrativo</h1>
      <p>Bem-vindo, {{ authService.currentUser()?.name }}!</p>
      <div class="dashboard-grid">
        <div class="card">
          <h2>Usuários</h2>
          <p>Gerenciar usuários do sistema</p>
        </div>
        <div class="card">
          <h2>Administradores</h2>
          <p>Listar administradores</p>
        </div>
        <div class="card">
          <h2>Status do Sistema</h2>
          <p>Monitorar banco de dados</p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .admin-dashboard {
      padding: 2rem;
    }
    h1 {
      margin-bottom: 1rem;
    }
    .dashboard-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: 1.5rem;
      margin-top: 2rem;
    }
    .card {
      background: var(--surface);
      border: 1px solid var(--border-1);
      border-radius: var(--radius-card);
      padding: 1.5rem;
      box-shadow: var(--shadow-card);
    }
  `]
})
export class AdminDashboardPageComponent {
  readonly authService = inject(AuthService);
}

