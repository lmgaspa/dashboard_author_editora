import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-public-layout',
  standalone: true,
  imports: [RouterOutlet],
  template: `
    <div class="public-layout">
      <main class="public-content">
        <router-outlet />
      </main>
    </div>
  `,
  styles: [`
    .public-layout {
      min-height: 100vh;
      display: flex;
      flex-direction: column;
    }
    .public-content {
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  `]
})
export class PublicLayoutComponent {}

