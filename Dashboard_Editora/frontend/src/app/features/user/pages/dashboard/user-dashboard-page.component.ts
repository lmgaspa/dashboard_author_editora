import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '@/app/core/services/auth.service';

@Component({
  selector: 'app-user-dashboard-page',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="user-dashboard">
      <div class="header">
        <h1>
          <span class="title-main">Dashboard do Usuário</span>
          <span class="title-glow">Dashboard do Usuário</span>
        </h1>
        <p>Bem-vindo, {{ authService.currentUser()?.name }}!</p>
      </div>
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
    .header h1 {
      position: relative;
      font-size: 1.75rem;
      font-weight: 700;
      margin-bottom: 0.5rem;
    }
    .header h1 .title-main {
      position: relative;
      z-index: 10;
      background: linear-gradient(to right, #38bdf8, #2563eb, #38bdf8);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
    .header h1 .title-glow {
      position: absolute;
      inset: 0;
      filter: blur(8px);
      opacity: 0.6;
      background: linear-gradient(to right, #38bdf8, #2563eb, #38bdf8);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
    .header p {
      color: #94a3b8;
      font-size: 0.9rem;
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

