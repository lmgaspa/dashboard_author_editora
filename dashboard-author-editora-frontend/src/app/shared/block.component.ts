import { Component, input } from '@angular/core';

@Component({
  selector: 'app-block',
  standalone: true,
  template: `
    <section class="rounded-card bg-[var(--card-bg)] shadow-sm border border-[var(--border-1)]">
      <header class="flex items-center justify-between px-4 py-3 border-b border-[var(--border-1)]">
        <h3 class="font-semibold text-[var(--ink-1)]">{{title()}}</h3>
        <ng-content select="[block-actions]"></ng-content>
      </header>
      <div class="p-4">
        <ng-content></ng-content>
      </div>
    </section>
  `,
})
export class BlockComponent {
  title = input<string>('Título');
}
