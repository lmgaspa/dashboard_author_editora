import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { FooterComponent } from '../../components/footer/footer.component';

@Component({
  selector: 'app-public-layout',
  standalone: true,
  imports: [RouterOutlet, FooterComponent],
  template: `
    <div class="public-layout">
      <main class="public-content">
        <router-outlet />
      </main>
      <app-footer />
    </div>
  `,
  styles: [`
    .public-layout {
      width: 100%;
      display: flex;
      flex-direction: column;
      position: relative;
    }
    .public-content {
      width: 100%;
      display: block;
      flex-shrink: 0;
    }
  `]
})
export class PublicLayoutComponent {}

