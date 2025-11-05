import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '@/app/core/services/auth.service';

@Component({
  selector: 'app-user-dashboard-page',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="user-dashboard">
      <h1>Dashboard do Usuário</h1>
      <p>Bem-vindo, {{ authService.currentUser()?.name }}!</p>
      <div class="dashboard-content">
        <div class="card">
          <h2>Meu Perfil</h2>
          <p>Gerencie suas informações pessoais</p>
        </div>
        <div class="card">
          <h2>Configurações</h2>
          <p>Altere sua senha e email</p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .user-dashboard {
      padding: 2rem;
    }
    h1 {
      margin-bottom: 1rem;
    }
    .dashboard-content {
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
export class UserDashboardPageComponent {
  readonly authService = inject(AuthService);
}

