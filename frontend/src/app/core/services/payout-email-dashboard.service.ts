import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { PayoutEmailWithCoupon } from '../models/payout-email-dashboard.model';

/**
 * Serviço para acessar e-mails de repasse com informações de cupom.
 * 
 * ARQUITETURA MULTI-TENANT:
 * - O backend identifica automaticamente o `author_id` do usuário logado via token JWT
 * - Cada autor só acessa seus próprios e-mails de repasse (isolamento garantido pelo backend)
 * - O frontend NÃO precisa passar `author_id` explicitamente
 * 
 * FLUXO:
 * 1. Frontend envia requisição com token JWT no header Authorization
 * 2. Backend extrai email do token e busca usuário no banco do painel
 * 3. Backend obtém `author_id` do usuário e credenciais do banco do e-commerce
 * 4. Backend conecta ao banco do e-commerce do autor
 * 5. Backend executa query com JOIN em `orders` para buscar informações de cupom
 * 6. Backend retorna apenas e-mails de repasse do autor logado com informações de cupom
 */
@Injectable({
  providedIn: 'root'
})
export class PayoutEmailDashboardService {
  private readonly http = inject(HttpClient);
  private readonly API_URL = environment.apiUrl;
  private readonly DASHBOARD_API = `${this.API_URL}/api/v1/dashboard/payout-emails`;

  /**
   * Lista todos os e-mails de repasse (opcionalmente filtrar por tipo)
   * 
   * O backend identifica automaticamente o `author_id` do usuário logado via token JWT.
   * A query faz JOIN com `orders` para buscar informações de cupom (`coupon_code`, `discount_amount`).
   * 
   * @param emailType Tipo de e-mail ('REPASSE_PIX' ou 'REPASSE_CARD')
   * @returns Observable com lista de e-mails de repasse incluindo informações de cupom
   */
  listPayoutEmails(emailType?: string): Observable<PayoutEmailWithCoupon[]> {
    let params = new HttpParams();
    if (emailType) {
      params = params.set('emailType', emailType);
    }
    return this.http.get<PayoutEmailWithCoupon[]>(this.DASHBOARD_API, { params });
  }

  /**
   * Busca um e-mail de repasse específico
   * 
   * O backend identifica automaticamente o `author_id` do usuário logado via token JWT.
   * A query faz JOIN com `orders` para buscar informações de cupom.
   * 
   * @param id ID do e-mail de repasse
   * @returns Observable com dados do e-mail de repasse incluindo informações de cupom
   */
  getPayoutEmail(id: number): Observable<PayoutEmailWithCoupon> {
    return this.http.get<PayoutEmailWithCoupon>(`${this.DASHBOARD_API}/${id}`);
  }
}

