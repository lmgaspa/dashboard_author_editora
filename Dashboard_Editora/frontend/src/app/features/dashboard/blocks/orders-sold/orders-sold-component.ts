import { Component, signal } from '@angular/core';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { BlockComponent } from 'src/app/shared/block.component';

type OrderStatus = 'PAID' | 'WAITING' | 'CANCELED';

type OrderSold = {
  id: string;
  date: Date;
  customer: string;
  book: string;
  status: OrderStatus;
  total: number; // em reais
};

@Component({
  selector: 'app-orders-sold-component',
  standalone: true,
  imports: [BlockComponent, CommonModule, DatePipe, CurrencyPipe],
  template: `
    <app-block class="text-xs">
      <div block-actions class="flex items-center gap-2 text-[11px]">
        <button class="px-2 py-1 rounded bg-muted hover:bg-muted/80" (click)="exportCSV()">
          Exportar CSV
        </button>
        <button class="px-2 py-1 rounded bg-muted hover:bg-muted/80" (click)="exportPDF()">
          Exportar PDF
        </button>
      </div>

      <!-- cabeçalho (6 colunas, sem Canal) -->
      <div class="hidden md:grid grid-cols-6 gap-2 px-2.5 py-2 rounded bg-brand-100/60 text-[color:var(--ink-2)]">
        <div>TXID</div><div>Data</div><div>Cliente</div><div>Livro</div><div>Status</div><div>Valor</div>
      </div>

      <!-- linhas -->
      <div class="mt-2 space-y-1.5" id="orders-print-area">
        <div
          class="grid grid-cols-1 md:grid-cols-6 gap-2 items-center px-2.5 py-2 rounded border border-[color:var(--border-1)] bg-[color:var(--surface)]"
          *ngFor="let o of orders(); index as i"
        >
          <div class="text-[color:var(--ink-2)] tabular">#{{ o.id }}</div>
          <div class="text-[color:var(--ink-2)]">
            {{ o.date | date:'dd/MM/yyyy' }} às {{ o.date | date:'HH:mm' }}
          </div>
          <div class="text-[color:var(--ink-2)] truncate">{{ o.customer }}</div>
          <div class="text-[color:var(--ink-2)] truncate">{{ o.book }}</div>
          <div>
            <span class="px-2 py-0.5 rounded-full text-[11px]" [ngClass]="statusBadge[o.status]">
              {{ statusLabel[o.status] }}
            </span>
          </div>
          <div class="tabular-nums text-[color:var(--ink-2)]">
            {{ o.total | currency:'BRL':'symbol-narrow':'1.2-2':'pt-BR' }}
          </div>
        </div>
      </div>
    </app-block>
  `,
})
export class OrdersSoldComponent {
  readonly statusLabel: Record<OrderStatus, string> = {
    PAID: 'Pago',
    WAITING: 'Aguardando',
    CANCELED: 'Cancelado',
  };

  readonly statusBadge: Record<OrderStatus, string> = {
    PAID: 'bg-emerald-100 text-emerald-800',
    WAITING: 'bg-amber-100 text-amber-800',
    CANCELED: 'bg-red-100 text-red-800',
  };

  readonly orders = signal<OrderSold[]>([
    { id: '8453', date: new Date(Date.now() - 1000 * 60 * 40),  customer: 'Marina S.',  book: 'Sonhos não são impossíveis', status: 'PAID',    total: 64.9 },
    { id: '8452', date: new Date(Date.now() - 1000 * 60 * 90),  customer: 'João Pedro', book: 'Ameendoeiras de Outono',     status: 'WAITING', total: 58.0 },
    { id: '8451', date: new Date(Date.now() - 1000 * 60 * 160), customer: 'R. Carvalho', book: 'Regressantes',               status: 'PAID',    total: 72.5 },
  ]);

  exportCSV(): void {
    const rows = [
      ['id', 'data', 'cliente', 'livro', 'status', 'valor_brl'],
      ...this.orders().map(o => [
        o.id,
        this.formatDate(o.date),
        o.customer.replaceAll('"', '""'),
        o.book.replaceAll('"', '""'),
        this.statusLabel[o.status],
        o.total.toFixed(2).replace('.', ','),
      ]),
    ];

    const csv = rows
      .map(cols => cols.map(v => /[",;\n]/.test(String(v)) ? `"${v}"` : String(v)).join(';'))
      .join('\n');

    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `pedidos_${this.todaySlug()}.csv`;
    a.click();
    URL.revokeObjectURL(url);
  }

  // Novo nome (substitui qualquer printPDF antigo)
  exportPDF(): void {
  const style = `
    <style>
      * { font-family: ui-sans-serif, system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial, "Apple Color Emoji","Segoe UI Emoji"; }
      h1 { font-size: 16px; margin: 0 0 10px; }
      .meta { color:#334155; margin-bottom: 8px; font-size: 12px; }
      table { border-collapse: collapse; width: 100%; font-size: 12px; }
      th, td { border: 1px solid #e5e7eb; padding: 6px 8px; text-align: left; }
      th { background: #f1f5f9; }
      .right { text-align: right; }
      @media print { @page { size: A4; margin: 14mm; } }
    </style>
  `;

  const rows = this.orders().map(o => `
    <tr>
      <td>#${o.id}</td>
      <td>${this.formatDate(o.date)}</td>
      <td>${this.escape(o.customer)}</td>
      <td>${this.escape(o.book)}</td>
      <td>${this.statusLabel[o.status]}</td>
      <td class="right">${this.formatCurrency(o.total)}</td>
    </tr>
  `).join('');

  const now = new Date();
  const html = `
  <!doctype html>
  <html lang="pt-BR">
    <head><meta charset="utf-8">${style}<title>Pedidos</title></head>
    <body>
      <h1>Pedidos</h1>
      <div class="meta">Gerado em ${this.formatDate(now)}</div>
      <table>
        <thead>
          <tr>
            <th>#</th><th>Data</th><th>Cliente</th><th>Livro</th><th>Status</th><th>Valor</th>
          </tr>
        </thead>
        <tbody>${rows}</tbody>
      </table>
      <!-- Removido o auto-print -->
    </body>
  </html>
`;

  const blob = new Blob([html], { type: 'text/html;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const win = window.open(url, '_blank', 'width=1024,height=768');
  if (!win) window.location.href = url;
}

  private todaySlug(): string {
    const d = new Date();
    const pad = (n: number) => String(n).padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
  }

  private escape(s: string): string {
    const div = document.createElement('div');
    div.innerText = s;
    return div.innerHTML;
  }

  private formatDate(d: Date): string {
    const pad = (n: number) => String(n).padStart(2, '0');
    const dd = pad(d.getDate());
    const mm = pad(d.getMonth() + 1);
    const yyyy = d.getFullYear();
    const hh = pad(d.getHours());
    const mi = pad(d.getMinutes());
    return `${dd}/${mm}/${yyyy} às ${hh}:${mi}`;
  }

  private formatCurrency(v: number): string {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(v);
  }
}
