import { Injectable, signal, computed } from '@angular/core';

export type Severity = 'info' | 'success' | 'warning' | 'error';
export type AppNotification = {
  id: string;
  title: string;
  message: string;
  when: Date;
  severity: Severity;
  read?: boolean;
};

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly _items = signal<AppNotification[]>([
    {
      id: 'n1',
      title: 'Baixo estoque do Livro "Sonhos não são impossívei…”',
      message: '7 unidades restantes no depósito principal.',
      when: new Date(Date.now() - 5 * 60 * 1000),
      severity: 'warning',
      read: false,
    },
    {
      id: 'n2',
      title: 'Venda confirmada',
      message: 'Pedido #8453 pago via PIX.',
      when: new Date(Date.now() - 45 * 60 * 1000),
      severity: 'success',
      read: false,
    },
    {
      id: 'n3',
      title: 'Falha de sincronização',
      message: 'Marketplace externo fora do ar (HTTP 503).',
      when: new Date(Date.now() - 3 * 60 * 60 * 1000),
      severity: 'error',
      read: false,
    },
  ]);

  readonly items = computed(() => this._items());
  readonly unreadCount = computed(() => this._items().filter(n => !n.read).length);

  push(n: AppNotification) {
    this._items.update(list => [n, ...list]);
  }

  markAllRead() {
    this._items.update(list => list.map(n => ({ ...n, read: true })));
  }

  markRead(id: string) {
    this._items.update(list => list.map(n => (n.id === id ? { ...n, read: true } : n)));
  }

  clear() {
    this._items.set([]);
  }
}
