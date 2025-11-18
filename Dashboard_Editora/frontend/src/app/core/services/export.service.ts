import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/environments/environment';

export type ExportFormat = 'pdf' | 'csv';

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
    return this.http.get(`${this.API_URL}/api/v1/metrics/export`, {
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
   */
  private buildParams(options: ExportOptions): HttpParams {
    let params = new HttpParams();
    
    if (options.format) {
      params = params.set('format', options.format);
    }
    if (options.authorId) {
      params = params.set('authorId', options.authorId);
    }
    if (options.startDate) {
      params = params.set('startDate', options.startDate);
    }
    if (options.endDate) {
      params = params.set('endDate', options.endDate);
    }
    if (options.status) {
      params = params.set('status', options.status);
    }

    // Adicionar outros parâmetros dinamicamente
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
   * Gera nome de arquivo com timestamp
   */
  generateFilename(prefix: string, format: ExportFormat, authorId?: string): string {
    const date = new Date().toISOString().split('T')[0]; // YYYY-MM-DD
    const authorSuffix = authorId ? `-${authorId}` : '';
    return `${prefix}${authorSuffix}-${date}.${format}`;
  }
}

