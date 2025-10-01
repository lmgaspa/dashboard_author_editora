import { Component } from '@angular/core';
import { BlockComponent } from 'src/app/shared/block.component';

@Component({
  selector: 'app-map-br-placeholder',
  standalone: true,
  imports: [BlockComponent],
  template: `
    <app-block title="Mapa do Brasil">
      <div class="h-[320px] rounded-md bg-brand-100/60"></div>
    </app-block>
  `,
})
export class MapBrPlaceholderComponent {}
