import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '@/app/core/services/auth.service';

@Component({
  selector: 'app-metrics-page',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './metrics-page.component.html',
  styles: []
})
export class MetricsPageComponent {
  readonly authService = inject(AuthService);
}

