import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, shareReplay } from 'rxjs';
import { environment } from '@/environments/environment';
import { Entrega, AtualizarStatusEnvioRequest } from '../models/entrega.model';

/**
 * Serviço para gerenciar entregas.
 * 
 * ARQUITETURA MULTI-TENANT:
 * - O backend identifica automaticamente o `author_id` do usuário logado via token JWT
 * - Cada autor só acessa seus próprios dados (isolamento garantido pelo backend)
 */
@Injectable({
  providedIn: 'root'
})
export class EntregaService {
  private readonly http = inject(HttpClient);
  private readonly API_URL = environment.apiUrl;
  private readonly ENTREGAS_API = `${this.API_URL}/api/v1/entregas`;
  
  // Cache simples para evitar múltiplas requisições simultâneas
  private entregasCache$?: Observable<Entrega[]>;

  /**
   * Lista todas as entregas do autor logado.
   * O backend identifica automaticamente o `author_id` do usuário logado via token JWT.
   * Usa cache para evitar requisições duplicadas.
   */
  listarEntregas(forceRefresh: boolean = false): Observable<Entrega[]> {
    if (forceRefresh || !this.entregasCache$) {
      this.entregasCache$ = this.http.get<Entrega[]>(this.ENTREGAS_API).pipe(
        shareReplay(1)
      );
    }
    return this.entregasCache$;
  }
  
  /**
   * Limpa o cache de entregas (útil após atualizar status)
   */
  clearCache(): void {
    this.entregasCache$ = undefined;
  }

  /**
   * Busca uma entrega específica por orderId.
   * O backend identifica automaticamente o `author_id` do usuário logado via token JWT.
   */
  buscarEntrega(orderId: number): Observable<Entrega> {
    return this.http.get<Entrega>(`${this.ENTREGAS_API}/${orderId}`);
  }

  /**
   * Atualiza o status de envio de um pedido.
   * O backend identifica automaticamente o `author_id` do usuário logado via token JWT.
   */
  atualizarStatusEnvio(
    orderId: number,
    request: AtualizarStatusEnvioRequest
  ): Observable<Entrega> {
    return this.http.put<Entrega>(
      `${this.ENTREGAS_API}/${orderId}/status`,
      request
    );
  }
}

