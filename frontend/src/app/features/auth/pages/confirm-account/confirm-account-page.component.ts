import { Component, signal, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { AuthService } from '@/app/core/services/auth.service';

@Component({
  selector: 'app-confirm-account-page',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './confirm-account-page.component.html',
  styles: []
})
export class ConfirmAccountPageComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly loading = signal<boolean>(true);
  readonly success = signal<boolean>(false);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    const token = this.route.snapshot.queryParams['token'];
    if (!token) {
      this.error.set('Token inválido ou ausente.');
      this.loading.set(false);
      return;
    }

    this.authService.confirmAccount(token).subscribe({
      next: () => {
        this.success.set(true);
        this.loading.set(false);
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 3000);
      },
      error: (err) => {
        this.error.set(err.error?.message || 'Erro ao confirmar conta.');
        this.loading.set(false);
      }
    });
  }
}

