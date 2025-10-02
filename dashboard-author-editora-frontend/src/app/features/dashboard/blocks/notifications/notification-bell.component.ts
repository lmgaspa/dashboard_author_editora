import { Component, ElementRef, HostListener, ViewChild, computed, inject, signal, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationsComponent } from './notification-component';
import { NotificationService } from './notification.service';

@Component({
  standalone: true,
  selector: 'app-notification-bell',
  imports: [CommonModule, NotificationsComponent],
  host: { class: 'relative inline-block text-left' },
  template: `
    <button
      #btn
      type="button"
      class="relative inline-flex items-center justify-center rounded-full p-2 hover:bg-muted focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-ring"
      aria-label="Abrir notificações"
      [attr.aria-expanded]="open() ? 'true' : 'false'"
      (click)="onToggleClick($event)"
    >
      <svg class="h-6 w-6 text-foreground/80" viewBox="0 0 24 24" fill="none" aria-hidden="true">
        <path d="M15 17h5l-1.405-1.405A2.032 2.032 0 0 1 18 14.158V11a6 6 0 1 0-12 0v3.159c0 .538-.214 1.055-.595 1.436L4 17h5"
              stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        <path d="M9 17a3 3 0 0 0 6 0" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
      </svg>

      @if (count() > 0) {
        <span
          class="absolute -top-0.5 -right-0.5 min-w-5 h-5 px-1 rounded-full bg-amber-500 text-white text-xs font-semibold flex items-center justify-center ring-2 ring-surface"
          aria-live="polite" aria-atomic="true"
        >
          {{ count() > 99 ? '99+' : count() }}
        </span>
      }
    </button>

    @if (open()) {
      <!-- O elemento nasce aqui, mas será movido para document.body ao abrir -->
      <div
        #panel
        class="notif-portal w-[22rem] max-h-[70vh] overflow-hidden rounded-xl border border-border-1 bg-surface shadow-xl"
        role="dialog" aria-label="Notificações" [attr.aria-modal]="true"
        (click)="$event.stopPropagation()"
      >
        <div class="flex items-center justify-between px-3 py-2 border-b border-border-1 bg-surface">
          <p class="text-sm font-medium text-foreground">Notificações</p>
          <button
            type="button"
            class="rounded-md px-2 py-1 text-xs text-foreground/70 hover:bg-muted"
            (click)="markAllRead()"
          >
            Marcar tudo como lido
          </button>
        </div>

        <div class="max-h-[60vh] overflow-y-auto p-2">
          <app-notifications-component />
        </div>

        <div class="px-3 py-2 border-t border-border-1 bg-surface flex justify-end">
          <button type="button" class="rounded-md bg-muted hover:bg-muted/80 px-3 py-1.5 text-sm" (click)="close()">
            Fechar
          </button>
        </div>
      </div>
    }
  `,
})
export class NotificationBellComponent implements AfterViewInit {
  private readonly service = inject(NotificationService);

  @ViewChild('btn',   { static: true }) btnRef!: ElementRef<HTMLElement>;
  @ViewChild('panel', { static: false }) panelRef?: ElementRef<HTMLElement>;

  open = signal(false);
  count = computed(() => this.service.unreadCount());

  // evita fechar no mesmo clique que abre
  private ignoreNextDocClick = false;

  ngAfterViewInit(): void {
    // nada aqui por enquanto
  }

  onToggleClick(ev: MouseEvent) {
    ev.stopPropagation();
    const next = !this.open();
    this.open.set(next);

    if (next) {
      // aguarda o *ngIf pintar o #panel, então move para <body> e posiciona
      setTimeout(() => {
        const panelEl = this.panelRef?.nativeElement;
        if (!panelEl) return;

        // move para body (portal manual)
        if (!panelEl.isConnected || panelEl.parentElement !== document.body) {
          document.body.appendChild(panelEl);
        }

        // estilo FIXED + topo real
        panelEl.style.position = 'fixed';
        panelEl.style.zIndex   = '2147483647';
        panelEl.style.maxHeight = '70vh';

        // posiciona alinhado ao botão (direita, com gap)
        this.positionPanel();

        // protege contra o document:click do mesmo tick
        this.ignoreNextDocClick = true;
        setTimeout(() => (this.ignoreNextDocClick = false), 0);
      });
    } else {
      // fechar: o *ngIf remove o elemento do DOM automaticamente
    }
  }

  close() { this.open.set(false); }
  markAllRead() { this.service.markAllRead(); }

  private positionPanel() {
    const btn = this.btnRef?.nativeElement;
    const panelEl = this.panelRef?.nativeElement;
    if (!btn || !panelEl) return;

    const rect = btn.getBoundingClientRect();
    const gap = 8;

    // largura calculada após mover para body
    const panelWidth = panelEl.getBoundingClientRect().width || 22 * (parseFloat(getComputedStyle(document.documentElement).fontSize) || 16);

    // preferir bottom-end; se não couber, usar top-end
    const viewW = document.documentElement.clientWidth;
    const viewH = document.documentElement.clientHeight;

    let top = rect.bottom + gap;
    let left = rect.right - panelWidth;

    // clamp horizontal
    left = Math.max(8, Math.min(viewW - panelWidth - 8, left));

    // se passar do bottom, sobe
    const panelHeight = panelEl.getBoundingClientRect().height || 0;
    if (top + panelHeight + 8 > viewH) {
      const altTop = rect.top - gap - panelHeight;
      if (altTop >= 8) top = altTop;
    }

    panelEl.style.top  = `${top}px`;
    panelEl.style.left = `${left}px`;
  }

  @HostListener('window:resize') onResize() { if (this.open()) this.positionPanel(); }
  @HostListener('window:scroll') onScroll() { if (this.open()) this.positionPanel(); }

  @HostListener('document:click', ['$event'])
  onDocClick(e: MouseEvent) {
    if (!this.open()) return;
    if (this.ignoreNextDocClick) return;

    const target = e.target as Node;
    const btnEl = this.btnRef?.nativeElement;
    const panelEl = this.panelRef?.nativeElement;

    // se clicar no botão ou no painel, não fecha
    if ((btnEl && btnEl.contains(target)) || (panelEl && panelEl.contains(target))) {
      return;
    }

    this.close();
  }

  @HostListener('document:keydown.escape')
  onEsc() { if (this.open()) this.close(); }
}
