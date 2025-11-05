import { Component, signal, computed, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';
import { TopbarComponent } from '../../components/topbar/topbar.component';
import { FooterComponent } from '../../components/footer/footer.component';
import { AuthService } from '@/app/core/services/auth.service';

@Component({
  selector: 'app-user-layout',
  standalone: true,
  imports: [RouterOutlet, SidebarComponent, TopbarComponent, FooterComponent],
  template: `
    <div class="user-layout">
      <app-topbar />
      <div class="layout-container">
        <app-sidebar />
        <main class="layout-content">
          <router-outlet />
        </main>
      </div>
      <app-footer />
    </div>
  `,
  styles: [`
    .user-layout {
      min-height: 100vh;
      width: 100%;
      display: flex;
      flex-direction: column;
      background: linear-gradient(to bottom right, #0a0a0f 0%, #1a0a1a 50%, #0f0a1a 100%);
    }
    .layout-container {
      display: flex;
      width: 100%;
      margin-top: 4rem;
      min-height: calc(100vh - 4rem);
    }
    .layout-content {
      flex: 1;
      width: 100%;
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

