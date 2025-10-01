import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type DeliveryStatusKeys = 'jaEnviado' | 'emTransito' | 'entregue' | 'retornando';

export interface DeliveryStatus {
  jaEnviado: number;
  emTransito: number;
  entregue: number;
  retornando: number;
  total: number;
}

@Injectable({ providedIn: 'root' })
export class MockDeliveryService {
  private readonly initial: DeliveryStatus = {
    jaEnviado: 312,
    emTransito: 455,
    entregue: 380,
    retornando: 17,
    total: 312 + 455 + 380 + 17,
  };

  private readonly _state$ = new BehaviorSubject<DeliveryStatus>(this.initial);
  readonly state$ = this._state$.asObservable();

  update(partial: Partial<Omit<DeliveryStatus, 'total'>>) {
    const current = this._state$.value;
    const next = { ...current, ...partial };
    next.total = next.jaEnviado + next.emTransito + next.entregue + next.retornando;
    this._state$.next(next);
  }
}
