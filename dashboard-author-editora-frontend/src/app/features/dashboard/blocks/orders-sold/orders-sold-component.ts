import { Component, signal } from '@angular/core';
import { CommonModule, CurrencyPipe, DatePipe } from '@angular/common';
import { BlockComponent } from 'src/app/shared/block.component';

type OrderStatus = 'PAID' | 'WAITING' | 'CANCELED';
type Channel = 'Site' | 'Shopee' | 'Mercado Livre' | 'Amazon';

type OrderSold = {
  id: string;
  date: Date;
  customer: string;
  book: string;
  status: OrderStatus;
  total: number; // em reais
  channel: Channel;
};

@Component({
  selector: 'app-orders-sold-component',
  standalone: true,
  imports: [BlockComponent, CommonModule, DatePipe, CurrencyPipe],
  template: `
    <app-block>
      <div block-actions class="flex items-center gap-2 text-sm">
        <button class="px-2 py-1 rounded bg-muted hover:bg-muted/80" (click)="exportCSV()">
          Exportar CSV
        </button>
        <button class="px-2 py-1 rounded bg-muted hover:bg-muted/80" (click)="printPDF()">
          Imprimir PDF
        </button>
      </div>

      <div class="mb-3 text-lg font-semibold text-slate-700">Pedidos Vendidos</div>

      <!-- cabeçalho -->
      <div class="hidden md:grid grid-cols-7 gap-3 px-3 py-2 rounded bg-brand-100/60 text-sm text-slate-700">
        <div>#</div><div>Data</div><div>Cliente</div><div>Livro</div><div>Status</div><div>Valor</div><div>Canal</div>
      </div>

      <!-- linhas -->
      <div class="mt-2 space-y-2" id="orders-print-area">
        <div
          class="grid grid-cols-1 md:grid-cols-7 gap-3 items-center px-3 py-3 rounded border border-slate-200 bg-white"
          *ngFor="let o of orders(); index as i"
        >
          <div class="text-xs md:text-sm text-slate-700">#{{ o.id }}</div>
          <div class="text-xs md:text-sm text-slate-700">{{ o.date | date:'dd/MM/yyyy HH:mm' }}</div>
          <div class="text-xs md:text-sm text-slate-700 truncate">{{ o.customer }}</div>
          <div class="text-xs md:text-sm text-slate-700 truncate">{{ o.book }}</div>
          <div class="text-xs md:text-sm">
            <span
              class="px-2 py-0.5 rounded-full text-[11px] md:text-xs"
              [ngClass]="statusBadge[o.status]"
            >
              {{ statusLabel[o.status] }}
            </span>
          </div>
          <div class="text-xs md:text-sm tabular-nums text-slate-700">
            {{ o.total | currency:'BRL':'symbol-narrow':'1.2-2':'pt-BR' }}
          </div>
          <div class="text-xs md:text-sm text-slate-700">{{ o.channel }}</div>
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

  // badges por status (mapeie para seus tokens se preferir)
  readonly statusBadge: Record<OrderStatus, string> = {
    PAID: 'bg-emerald-100 text-emerald-800',
    WAITING: 'bg-amber-100 text-amber-800',
    CANCELED: 'bg-red-100 text-red-800',
  };

  // MOCK com seus livros
  readonly orders = signal<OrderSold[]>([
    {
      id: '8453',
      date: new Date(Date.now() - 1000 * 60 * 40),
      customer: 'Marina S.',
      book: 'Sonhos não são impossíveis',
      status: 'PAID',
      total: 64.9,
      channel: 'Site',
    },
    {
      id: '8452',
      date: new Date(Date.now() - 1000 * 60 * 90),
      customer: 'João Pedro',
      book: 'Ameendoeiras de Outono',
      status: 'WAITING',
      total: 58.0,
      channel: 'Mercado Livre',
    },
    {
      id: '8451',
      date: new Date(Date.now() - 1000 * 60 * 160),
      customer: 'R. Carvalho',
      book: 'Regressantes',
      status: 'PAID',
      total: 72.5,
      channel: 'Shopee',
    },
  ]);

  exportCSV(): void {
    const rows = [
      ['id', 'data', 'cliente', 'livro', 'status', 'valor_brl', 'canal'],
      ...this.orders().map(o => [
        o.id,
        o.date.toISOString(),
        o.customer.replaceAll('"', '""'),
        o.book.replaceAll('"', '""'),
        this.statusLabel[o.status],
        o.total.toFixed(2).replace('.', ','),
        o.channel,
      ]),
    ];

    const csv = rows
      .map(cols => cols.map(v => /[",;\n]/.test(String(v)) ? `"${v}"` : String(v)).join(';'))
      .join('\n');

    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `pedidos_vendidos_${this.todaySlug()}.csv`;
    a.click();
    URL.revokeObjectURL(url);
  }

  printPDF(): void {
    // Abre uma janela com HTML simples e estilo de impressão
    const win = window.open('', '_blank', 'width=1024,height=768');
    if (!win) return;

    const style = `
      <style>
        * { font-family: ui-sans-serif, system-ui, -apple-system, Segoe UI, Roboto, Helvetica, Arial, "Apple Color Emoji","Segoe UI Emoji"; }
        h1 { font-size: 18px; margin: 0 0 12px; }
        table { border-collapse: collapse; width: 100%; font-size: 12px; }
        th, td { border: 1px solid #e5e7eb; padding: 8px 10px; text-align: left; }
        th { background: #f1f5f9; }
        .right { text-align: right; }
        @media print { @page { size: A4; margin: 14mm; } }
      </style>
    `;

    const rows = this.orders()
      .map(
        o => `
        <tr>
          <td>#${o.id}</td>
          <td>${this.formatDate(o.date)}</td>
          <td>${this.escape(o.customer)}</td>
          <td>${this.escape(o.book)}</td>
          <td>${this.statusLabel[o.status]}</td>
          <td class="right">${this.formatCurrency(o.total)}</td>
          <td>${o.channel}</td>
        </tr>`
      )
      .join('');

    win.document.write(`
      <!doctype html>
      <html lang="pt-BR">
        <head><meta charset="utf-8">${style}<title>Pedidos Vendidos</title></head>
        <body>
          <h1>Pedidos Vendidos</h1>
          <table>
            <thead>
              <tr>
                <th>#</th><th>Data</th><th>Cliente</th><th>Livro</th><th>Status</th><th>Valor</th><th>Canal</th>
              </tr>
            </thead>
            <tbody>${rows}</tbody>
          </table>
          <script>window.addEventListener('load', () => { window.print(); setTimeout(() => window.close(), 300); });</script>
        </body>
      </html>
    `);
    win.document.close();
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
    // dd/MM/yyyy HH:mm
    const pad = (n: number) => String(n).padStart(2, '0');
    return `${pad(d.getDate())}/${pad(d.getMonth() + 1)}/${d.getFullYear()} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
  }

  private formatCurrency(v: number): string {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(v);
  }
}
