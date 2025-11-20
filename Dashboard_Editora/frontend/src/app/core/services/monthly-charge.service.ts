import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { MonthlyChargeDTO, PixCodeResponse, CreateChargeRequest, ConfirmPaymentRequest } from '../models/charge.model';

@Injectable({
  providedIn: 'root'
})
export class MonthlyChargeService {
  private readonly http = inject(HttpClient);
  private readonly API_URL = environment.apiUrl;
  private readonly baseUrl = `${this.API_URL}/api/v1/cobrancas`;

  /**
   * Autor: Listar suas cobranças mensais
   * 
   * Nota sobre autenticação:
   * - Dashboard (este sistema): Usa JWT para autenticação de usuários
   * - E-commerce (sistema externo): Usa CORS-based authorization (sem JWT)
   * 
   * O backend identifica o authorId automaticamente do token JWT do usuário logado,
   * garantindo isolamento multi-tenant (cada autor vê apenas suas próprias cobranças).
   * 
   * @returns Observable com lista de cobranças do autor logado
   */
  listarCobrancasAutor(): Observable<MonthlyChargeDTO[]> {
    return this.http.get<MonthlyChargeDTO[]>(this.baseUrl);
  }

  /**
   * Autor: Obter código PIX
   */
  obterPixCode(chargeId: string): Observable<PixCodeResponse> {
    return this.http.get<PixCodeResponse>(`${this.baseUrl}/${chargeId}/pix`);
  }

  /**
   * Admin: Criar cobrança
   */
  criarCobranca(request: CreateChargeRequest): Observable<MonthlyChargeDTO> {
    return this.http.post<MonthlyChargeDTO>(this.baseUrl, request);
  }

  /**
   * Admin: Confirmar pagamento
   */
  confirmarPagamento(chargeId: string, notes?: string): Observable<MonthlyChargeDTO> {
    const body: ConfirmPaymentRequest = { notes: notes || '' };
    return this.http.put<MonthlyChargeDTO>(
      `${this.baseUrl}/${chargeId}/confirmar`,
      body
    );
  }

  /**
   * Admin: Listar todas as cobranças
   */
  listarTodasCobrancas(authorId?: string, status?: string): Observable<MonthlyChargeDTO[]> {
    let params = new HttpParams();
    // Backend espera author_id (snake_case), não authorId (camelCase)
    if (authorId) {
      params = params.set('author_id', authorId);
    }
    if (status) {
      params = params.set('status', status);
    }
    
    return this.http.get<MonthlyChargeDTO[]>(`${this.baseUrl}/admin`, { params });
  }
}

