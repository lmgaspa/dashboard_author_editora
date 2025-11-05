import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MockDeliveryService, DeliveryStatus } from '../../data/mock-delivery.service';

type FormShape = { jaEnviado: number; emTransito: number; entregue: number; retornando: number };

@Component({
  selector: 'app-delivery-status',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './delivery-status-component.html',
  styleUrls: ['./delivery-status-component.css'],
})
export class DeliveryStatusComponent {
  current = signal<DeliveryStatus>({ jaEnviado: 0, emTransito: 0, entregue: 0, retornando: 0, total: 0 });
  editing = signal(false);
  form = signal<FormShape>({ jaEnviado: 0, emTransito: 0, entregue: 0, retornando: 0 });

  constructor(private svc: MockDeliveryService) {
    this.svc.state$.subscribe(s => {
      this.current.set(s);
      if (!this.editing()) {
        this.form.set({
          jaEnviado: s.jaEnviado,
          emTransito: s.emTransito,
          entregue: s.entregue,
          retornando: s.retornando,
        });
      }
    });
  }

  /** Atualiza um campo do form (usado no template) */
  updateField<K extends keyof FormShape>(key: K, value: number) {
    const curr = this.form();
    this.form.set({ ...curr, [key]: +value } as FormShape);
  }

  maxBarWidth(n: number) {
    const total = this.current().total || 1;
    return `${(n / total) * 100}%`;
  }

  toggleEdit() { this.editing.update(v => !v); }
  save() { this.svc.update(this.form()); this.editing.set(false); }
  cancel() {
    const s = this.current();
    this.form.set({ jaEnviado: s.jaEnviado, emTransito: s.emTransito, entregue: s.entregue, retornando: s.retornando });
    this.editing.set(false);
  }
}
