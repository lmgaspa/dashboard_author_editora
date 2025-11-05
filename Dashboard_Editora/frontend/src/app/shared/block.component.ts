import { Component, input } from '@angular/core';

@Component({
  selector: 'app-block',
  standalone: true,
  template: `
    <section class="app-card overflow-hidden rounded-card border border-[var(--border-1)] shadow-card">
      <header class="flex items-center justify-between gap-3 px-4 py-3 border-b border-[var(--border-1)]">
        <h3 class="font-bold text-2xl text-[color:var(--ink-1)] truncate min-w-0">{{ title() }}</h3>
        <ng-content select="[block-actions]"></ng-content>
      </header>
      <div class="p-4 min-w-0">
        <ng-content></ng-content>
      </div>
    </section>
  `,
})
export class BlockComponent {
  title = input<string>('Produtos Vendidos');
}
