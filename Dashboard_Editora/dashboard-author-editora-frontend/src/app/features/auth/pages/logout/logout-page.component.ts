import { Component, inject, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '@/app/core/services/auth.service';

@Component({
  selector: 'app-logout-page',
  standalone: true,
  template: `
    <div class="logout-page">
      <p>Saindo...</p>
    </div>
  `,
  styles: [`
    .logout-page {
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 100vh;
    }
  `]
})
export class LogoutPageComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  ngOnInit(): void {
    this.authService.logout();
  }
}

