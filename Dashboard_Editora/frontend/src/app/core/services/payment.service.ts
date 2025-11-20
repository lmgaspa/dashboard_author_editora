import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { PainelPagamentosAutor } from '../models/payment.model';

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private readonly http = inject(HttpClient);
  private readonly API_URL = environment.apiUrl;

  /**
   * Obtém o painel completo de pagamentos do autor
   * 
   * Nota sobre autenticação:
   * - Dashboard (este sistema): Usa JWT para autenticação de usuários
   * - E-commerce (sistema externo): Usa CORS-based authorization (sem JWT)
   * 
   * O backend identifica o authorId automaticamente do token JWT do usuário logado,
   * garantindo isolamento multi-tenant (cada autor vê apenas seus próprios dados).
   * 
   * Retorna valores reais (payment_payouts.amount_net) após taxas e margens,
   * não valores brutos dos pedidos.
   * 
   * @returns Observable com painel de pagamentos do autor (resumo, funil de vendas e vendas recentes)
   */
  getPainelPagamentos(): Observable<PainelPagamentosAutor> {
    return this.http.get<PainelPagamentosAutor>(
      `${this.API_URL}/api/v1/autor/pagamentos/painel`
    );
  }
}

