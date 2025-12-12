import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';

export type ExportFormat = 'pdf' | 'csv' | 'json';

export interface ExportOptions {
  format: ExportFormat;
  authorId?: string;
  startDate?: string;
  endDate?: string;
  status?: string;
  [key: string]: any; // Para permitir outros parâmetros
}

@Injectable({
  providedIn: 'root'
})
export class ExportService {
  private readonly http = inject(HttpClient);
  private readonly API_URL = environment.apiUrl;

  /**
   * Exporta métricas do autor
   */
  exportMetrics(options: ExportOptions): Observable<Blob> {
    const params = this.buildParams(options);
    return this.http.get(`${this.API_URL}/api/v1/metricas/export`, {
      params,
      responseType: 'blob'
    });
  }

  /**
   * Exporta emails do autor
   */
  exportEmails(options: ExportOptions): Observable<Blob> {
    const params = this.buildParams(options);
    return this.http.get(`${this.API_URL}/api/v1/emails/export`, {
      params,
      responseType: 'blob'
    });
  }

  /**
   * Exporta pagamentos do autor
   */
  exportPayments(options: ExportOptions): Observable<Blob> {
    const params = this.buildParams(options);
    return this.http.get(`${this.API_URL}/api/v1/payments/export`, {
      params,
      responseType: 'blob'
    });
  }

  /**
   * Constrói os parâmetros HTTP a partir das opções
   * Nota: author_id não é passado como parâmetro, pois é obtido automaticamente do token JWT
   */
  private buildParams(options: ExportOptions): HttpParams {
    let params = new HttpParams();
    
    if (options.format) {
      params = params.set('format', options.format);
    }
    // author_id não é passado como parâmetro - vem do token JWT automaticamente
    if (options.startDate) {
      params = params.set('startDate', options.startDate);
    }
    if (options.endDate) {
      params = params.set('endDate', options.endDate);
    }
    if (options.status) {
      params = params.set('status', options.status);
    }

    // Adicionar outros parâmetros dinamicamente (exceto authorId)
    Object.keys(options).forEach(key => {
      if (!['format', 'authorId', 'startDate', 'endDate', 'status'].includes(key) && options[key] != null) {
        params = params.set(key, String(options[key]));
      }
    });

    return params;
  }

  /**
   * Faz download de um blob com nome de arquivo específico
   */
  downloadBlob(blob: Blob, filename: string): void {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  }

  /**
   * Exporta entregas do autor
   */
  exportEntregas(options: ExportOptions): Observable<Blob> {
    const params = this.buildParams(options);
    return this.http.get(`${this.API_URL}/api/v1/entregas/export`, {
      params,
      responseType: 'blob'
    });
  }

  /**
   * Exporta pedidos arquivados (apenas ENTREGUE)
   */
  exportEntregasArquivadas(options: ExportOptions): Observable<Blob> {
    const params = this.buildParams(options);
    return this.http.get(`${this.API_URL}/api/v1/entregas/export/arquivados`, {
      params,
      responseType: 'blob'
    });
  }

  /**
   * Exporta cobranças do autor
   */
  exportCobrancas(options: ExportOptions): Observable<Blob> {
    const params = this.buildParams(options);
    return this.http.get(`${this.API_URL}/api/v1/cobrancas/export`, {
      params,
      responseType: 'blob'
    });
  }

  /**
   * Exporta tickets do autor
   */
  exportTickets(options: ExportOptions): Observable<Blob> {
    const params = this.buildParams(options);
    return this.http.get(`${this.API_URL}/api/v1/tickets/export`, {
      params,
      responseType: 'blob'
    });
  }

  /**
   * Gera nome de arquivo com timestamp
   */
  generateFilename(prefix: string, format: ExportFormat, authorId?: string, suffix?: string): string {
    const date = new Date().toISOString().split('T')[0]; // YYYY-MM-DD
    const authorSuffix = authorId ? `-${authorId}` : '';
    const extraSuffix = suffix ? `-${suffix}` : '';
    return `${prefix}${authorSuffix}${extraSuffix}-${date}.${format}`;
  }
}

