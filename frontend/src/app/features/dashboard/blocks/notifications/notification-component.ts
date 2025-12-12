// src/app/features/dashboard/blocks/notifications/notification-component.ts
import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BlockComponent } from 'src/app/shared/block.component';
import { NotificationService, Severity } from './notification.service';

@Component({
  selector: 'app-notifications-component',
  standalone: true,
  imports: [BlockComponent, CommonModule],
  template: `
    <app-block title="Notificações">
      <ul role="list" class="space-y-3" aria-live="polite">
        @for (n of service.items(); track n.id) {
          <li role="listitem" class="flex items-start gap-3">
            <span
              class="mt-1 inline-block size-2 rounded-full"
              [ngClass]="severityDot[n.severity]"
              [attr.aria-label]="n.severity"></span>

            <div class="flex-1">
              <div class="flex items-center gap-2">
                <p class="text-sm font-medium text-foreground">{{ n.title }}</p>
                <span class="text-xs text-foreground/60" [attr.aria-label]="n.when.toISOString()">
                  {{ timeAgo(n.when) }}
                </span>
              </div>
              <p class="text-sm text-foreground/80">{{ n.message }}</p>
            </div>
          </li>
        } @empty {
          <li class="text-sm text-foreground/70">Sem notificações.</li>
        }
      </ul>

      <div class="mt-4 text-right">
        <button
          type="button"
          class="px-3 py-1.5 rounded-md bg-muted hover:bg-muted/80 text-sm"
          aria-label="Ver todas as notificações">
          Ver todas
        </button>
      </div>
    </app-block>
  `,
})
export class NotificationsComponent {
  readonly service = inject(NotificationService);

  readonly severityDot: Record<Severity, string> = {
    info: 'bg-brand-400',
    success: 'bg-emerald-500',
    warning: 'bg-amber-500',
    error: 'bg-red-500',
  };

  timeAgo(d: Date): string {
    const s = Math.floor((Date.now() - d.getTime()) / 1000);
    if (s < 60) return `${s}s atrás`;
    const m = Math.floor(s / 60);
    if (m < 60) return `${m}m atrás`;
    const h = Math.floor(m / 60);
    if (h < 24) return `${h}h atrás`;
    const dd = Math.floor(h / 24);
    return `${dd}d atrás`;
  }
}
