import { Component, signal, computed, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';
import { TopbarComponent } from '../../components/topbar/topbar.component';
import { AuthService } from '@/app/core/services/auth.service';

@Component({
  selector: 'app-user-layout',
  standalone: true,
  imports: [RouterOutlet, SidebarComponent, TopbarComponent],
  template: `
    <div class="user-layout">
      <app-topbar />
      <div class="layout-container">
        <app-sidebar />
        <main class="layout-content">
          <router-outlet />
        </main>
      </div>
    </div>
  `,
  styles: [`
    .user-layout {
      min-height: 100vh;
      display: flex;
      flex-direction: column;
    }
    .layout-container {
      display: flex;
      margin-top: 64px;
      min-height: calc(100vh - 64px);
    }
    .layout-content {
      flex: 1;
      padding: 2rem;
      margin-left: 280px;
      
      @media (max-width: 768px) {
        margin-left: 0;
        padding: 1rem;
      }
    }
  `]
})
export class UserLayoutComponent {
  protected readonly authService = inject(AuthService);
}

