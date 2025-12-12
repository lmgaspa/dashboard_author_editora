import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';
import { PainelEmailsAutor } from '../models/email.model';

@Injectable({
  providedIn: 'root'
})
export class EmailService {
  private readonly http = inject(HttpClient);
  private readonly API_URL = environment.apiUrl;

  /**
   * Obtém o painel de e-mails do autor logado
   * 
   * Nota sobre autenticação:
   * - Dashboard (este sistema): Usa JWT para autenticação de usuários
   * - E-commerce (sistema externo): Usa CORS-based authorization (sem JWT)
   * 
   * O backend identifica o authorId automaticamente do token JWT do usuário logado,
   * garantindo isolamento multi-tenant (cada autor vê apenas seus próprios dados).
   * 
   * @returns Observable com o painel de e-mails (clientes e repasse)
   */
  getPainelEmails(): Observable<PainelEmailsAutor> {
    return this.http.get<PainelEmailsAutor>(
      `${this.API_URL}/api/v1/autor/emails/painel`
    );
  }
}

