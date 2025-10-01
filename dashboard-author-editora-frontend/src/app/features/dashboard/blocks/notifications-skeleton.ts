import { Component } from '@angular/core';
import { BlockComponent } from 'src/app/shared/block.component';

@Component({
  selector: 'app-notifications-skeleton',
  standalone: true,
  imports: [BlockComponent],
  template: `
    <app-block title="Notificações">
      <ul class="space-y-3">
        <li class="flex items-start gap-3">
          <span class="mt-1 size-2 rounded-full bg-brand-500 inline-block"></span>
          <div class="flex-1 space-y-2">
            <div class="h-3 w-64 bg-brand-100 rounded"></div>
            <div class="h-3 w-40 bg-brand-100 rounded"></div>
          </div>
        </li>
        <li class="flex items-start gap-3">
          <span class="mt-1 size-2 rounded-full bg-brand-400 inline-block"></span>
          <div class="flex-1 space-y-2">
            <div class="h-3 w-56 bg-brand-100 rounded"></div>
            <div class="h-3 w-36 bg-brand-100 rounded"></div>
          </div>
        </li>
      </ul>
    </app-block>
  `,
})
export class NotificationsSkeletonComponent {}
