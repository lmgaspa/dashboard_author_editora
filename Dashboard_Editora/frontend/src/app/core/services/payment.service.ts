import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { AuthService } from './auth.service';
import { AuthorPaymentDTO, PaymentSummaryDTO, PaymentPageResponse, PainelPagamentosAutor } from '../models/payment.model';

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private readonly http = inject(HttpClient);
  private readonly authService = inject(AuthService);
  private readonly API_URL = environment.apiUrl;

  /**
   * @deprecated Este método não funciona mais. Use getPainelPagamentos() ao invés.
   * Obtém resumo de pagamentos do autor logado
   * Para USER: retorna seus próprios pagamentos (baseado no authorId do token)
   * Para ADMIN: retorna pagamentos de um autor específico se authorId fornecido, senão seus próprios
   * @param authorId ID do autor (opcional, apenas para ADMIN ver pagamentos de outro autor)
   * @returns Observable com resumo de pagamentos
   */
  getPaymentSummary(authorId?: string): Observable<PaymentSummaryDTO> {
    console.warn('⚠️ getPaymentSummary() está deprecated. Use getPainelPagamentos() ao invés.');
    const isAdmin = this.authService.isAdmin();
    
    if (isAdmin && authorId) {
      // Admin buscando pagamentos de um autor específico
      return this.http.get<PaymentSummaryDTO>(
        `${this.API_URL}/api/v1/admin/payments/author/${authorId}/summary`
      );
    } else {
      // Autor (USER ou ADMIN) buscando seus próprios pagamentos
      // O backend identifica o authorId automaticamente do token JWT
      return this.http.get<PaymentSummaryDTO>(
        `${this.API_URL}/api/v1/author/payments/summary`
      );
    }
  }

  /**
   * @deprecated Este método não funciona mais. Use getPainelPagamentos() ao invés.
   * Lista pagamentos detalhados com paginação
   * Para USER: retorna seus próprios pagamentos (baseado no authorId do token)
   * Para ADMIN: retorna pagamentos de um autor específico se authorId fornecido, senão seus próprios
   * @param page Número da página (começa em 0)
   * @param size Tamanho da página
   * @param authorId ID do autor (opcional, apenas para ADMIN ver pagamentos de outro autor)
   * @returns Observable com página de pagamentos
   */
  getPaymentDetails(page: number = 0, size: number = 20, authorId?: string): Observable<PaymentPageResponse> {
    console.warn('⚠️ getPaymentDetails() está deprecated. Use getPainelPagamentos() ao invés.');
    const isAdmin = this.authService.isAdmin();
    
    if (isAdmin && authorId) {
      // Admin buscando pagamentos de um autor específico
      return this.http.get<PaymentPageResponse>(
        `${this.API_URL}/api/v1/admin/payments/author/${authorId}/details`,
        {
          params: {
            page: page.toString(),
            size: size.toString()
          }
        }
      );
    } else {
      // Autor (USER ou ADMIN) buscando seus próprios pagamentos
      // O backend identifica o authorId automaticamente do token JWT
      return this.http.get<PaymentPageResponse>(
        `${this.API_URL}/api/v1/author/payments/details`,
        {
          params: {
            page: page.toString(),
            size: size.toString()
          }
        }
      );
    }
  }

  /**
   * Obtém o painel completo de pagamentos do autor
   * Retorna resumo, funil de vendas e vendas recentes
   * @returns Observable com painel de pagamentos do autor
   */
  getPainelPagamentos(): Observable<PainelPagamentosAutor> {
    return this.http.get<PainelPagamentosAutor>(
      `${this.API_URL}/api/v1/autor/pagamentos/painel`
    );
  }
}

