import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { Ticket, CreateTicketRequest, CreateMessageRequest, TicketMessage } from '../models/ticket.model';

@Injectable({
  providedIn: 'root'
})
export class TicketService {
  private readonly http = inject(HttpClient);
  private readonly API_URL = environment.apiUrl;

  /**
   * Lista todos os tickets do autor logado
   * Autores veem apenas seus próprios tickets (sem prioridade)
   */
  listarTickets(): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(`${this.API_URL}/api/v1/tickets`);
  }

  /**
   * Obtém detalhes de um ticket específico
   */
  obterTicket(ticketId: string): Observable<Ticket> {
    return this.http.get<Ticket>(`${this.API_URL}/api/v1/tickets/${ticketId}`);
  }

  /**
   * Cria um novo ticket
   */
  criarTicket(request: CreateTicketRequest): Observable<Ticket> {
    return this.http.post<Ticket>(`${this.API_URL}/api/v1/tickets`, request);
  }

  /**
   * Adiciona uma mensagem ao ticket
   * Autores não podem criar notas internas (isInternalNote = true)
   */
  adicionarMensagem(ticketId: string, request: CreateMessageRequest): Observable<TicketMessage> {
    return this.http.post<TicketMessage>(
      `${this.API_URL}/api/v1/tickets/${ticketId}/messages`,
      request
    );
  }

  /**
   * Marca um ticket como resolvido
   */
  marcarComoResolvido(ticketId: string): Observable<Ticket> {
    return this.http.put<Ticket>(`${this.API_URL}/api/v1/tickets/${ticketId}/resolve`, {});
  }
}

