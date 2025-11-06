import { Component, signal, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';
import { TopbarComponent } from '../../components/topbar/topbar.component';
import { FooterComponent } from '../../components/footer/footer.component';
import { AuthService } from '@/app/core/services/auth.service';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [RouterOutlet, SidebarComponent, TopbarComponent, FooterComponent],
  template: `
    <div class="min-h-screen w-full flex flex-col bg-gradient-to-br from-[#0a0a0f] via-[#1a0a1a] to-[#0f0a1a] relative">
      <app-topbar />
      <div class="flex w-full mt-16 flex-1 min-h-0 overflow-hidden">
        <app-sidebar />
        <main class="flex-1 w-full px-3 pt-1 pb-3 ml-0 lg:ml-[280px] overflow-y-auto overflow-x-hidden min-h-0 transition-all duration-300">
          <router-outlet />
        </main>
      </div>
      <app-footer />
    </div>
  `,
  styles: []
})
export class AdminLayoutComponent {
  protected readonly authService = inject(AuthService);
}

