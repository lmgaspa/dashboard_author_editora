import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import {
  OrderWithCustomer,
  CustomerStats,
  CouponStats
} from '../models/order-dashboard.model';

/**
 * Serviço para acessar dados de pedidos do dashboard.
 * 
 * ARQUITETURA MULTI-TENANT:
 * - O backend identifica automaticamente o `author_id` do usuário logado via token JWT
 * - Cada autor só acessa seus próprios dados (isolamento garantido pelo backend)
 * - O frontend NÃO precisa passar `author_id` explicitamente na maioria dos casos
 * - Admins podem acessar dados de qualquer autor passando `author_id` como parâmetro
 * 
 * FLUXO:
 * 1. Frontend envia requisição com token JWT no header Authorization
 * 2. Backend extrai email do token e busca usuário no banco do painel
 * 3. Backend obtém `author_id` do usuário e credenciais do banco do e-commerce
 * 4. Backend conecta ao banco do e-commerce do autor
 * 5. Backend executa query com filtro WHERE b.author_id = ? (garantindo isolamento)
 * 6. Backend retorna apenas dados do autor logado
 */
@Injectable({
  providedIn: 'root'
})
export class OrderDashboardService {
  private readonly http = inject(HttpClient);
  private readonly API_URL = environment.apiUrl;
  private readonly DASHBOARD_API = `${this.API_URL}/api/v1/dashboard/orders`;

  /**
   * Busca um pedido específico com todas as informações do cliente
   * 
   * O backend identifica automaticamente o `author_id` do usuário logado via token JWT.
   * A query é filtrada por `WHERE b.author_id = ?` garantindo isolamento multi-tenant.
   * 
   * @param orderId ID do pedido
   * @returns Observable com os dados do pedido e cliente
   */
  getOrder(orderId: number): Observable<OrderWithCustomer> {
    return this.http.get<OrderWithCustomer>(`${this.DASHBOARD_API}/${orderId}`);
  }

  /**
   * Lista todos os pedidos (opcionalmente filtrar por status)
   * 
   * O backend identifica automaticamente o `author_id` do usuário logado via token JWT.
   * A query é filtrada por `WHERE b.author_id = ?` garantindo isolamento multi-tenant.
   * 
   * @param status Status do pedido (ex: 'CONFIRMED')
   * @returns Observable com lista de pedidos do autor logado
   */
  listOrders(status?: string): Observable<OrderWithCustomer[]> {
    let params = new HttpParams();
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<OrderWithCustomer[]>(this.DASHBOARD_API, { params });
  }

  /**
   * Busca pedidos por cliente (email, phone ou cpf)
   * 
   * O backend identifica automaticamente o `author_id` do usuário logado via token JWT.
   * A query é filtrada por `WHERE b.author_id = ?` garantindo isolamento multi-tenant.
   * 
   * @param email Email do cliente (opcional)
   * @param phone Telefone do cliente (opcional)
   * @param cpf CPF do cliente (opcional)
   * @returns Observable com lista de pedidos do cliente (apenas do autor logado)
   */
  getOrdersByCustomer(
    email?: string,
    phone?: string,
    cpf?: string
  ): Observable<OrderWithCustomer[]> {
    let params = new HttpParams();
    if (email) params = params.set('email', email);
    if (phone) params = params.set('phone', phone);
    if (cpf) params = params.set('cpf', cpf);

    return this.http.get<OrderWithCustomer[]>(
      `${this.DASHBOARD_API}/by-customer`,
      { params }
    );
  }

  /**
   * Obtém estatísticas de clientes
   * 
   * O backend identifica automaticamente o `author_id` do usuário logado via token JWT.
   * As estatísticas são calculadas apenas para pedidos do autor logado.
   * 
   * @returns Observable com estatísticas de clientes do autor logado
   */
  getCustomerStats(): Observable<CustomerStats> {
    return this.http.get<CustomerStats>(`${this.DASHBOARD_API}/stats/customers`);
  }

  /**
   * Obtém estatísticas de cupons
   * 
   * O backend identifica automaticamente o `author_id` do usuário logado via token JWT.
   * As estatísticas são calculadas apenas para pedidos do autor logado.
   * 
   * @returns Observable com estatísticas de cupons do autor logado
   */
  getCouponStats(): Observable<CouponStats> {
    return this.http.get<CouponStats>(`${this.DASHBOARD_API}/stats/coupons`);
  }

  /**
   * Conta total de pedidos com filtros
   * 
   * O backend identifica automaticamente o `author_id` do usuário logado via token JWT.
   * A query é filtrada por `WHERE b.author_id = ?` garantindo isolamento multi-tenant.
   * 
   * @param status Status do pedido (opcional)
   * @returns Observable com o total de pedidos
   */
  countOrders(status?: string): Observable<number> {
    let params = new HttpParams();
    if (status) {
      params = params.set('status', status);
    }
    return this.http.get<number>(`${this.DASHBOARD_API}/count`, { params });
  }
}

