import { Component, Input, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';

/**
 * Componente para embutir relatórios do Looker Studio por autor.
 * 
 * Este componente usa um map/strategy para mapear authorId para URLs do Looker Studio.
 * 
 * Para adicionar novos autores, edite o MAP_AUTHOR_LOOKER_URLS abaixo.
 */
@Component({
  selector: 'app-author-metrics-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './author-metrics-dashboard.component.html',
  styles: []
})
export class AuthorMetricsDashboardComponent {
  private readonly sanitizer = inject(DomSanitizer);

  /**
   * ID do autor para exibir o dashboard correspondente.
   * Por enquanto, apenas authorId = 1 está configurado.
   */
  @Input({ required: true }) authorId!: number;

  /**
   * Map/Strategy para mapear authorId para URLs do Looker Studio.
   * 
   * Para adicionar novos autores:
   * 1. Obtenha a URL do relatório do Looker Studio
   * 2. Adicione uma entrada neste map: [authorId]: 'https://lookerstudio.google.com/embed/reporting/...'
   * 
   * Exemplo para adicionar authorId = 2:
   * [2]: 'https://lookerstudio.google.com/embed/reporting/abc123-def456-ghi789/page/xyz'
   */
  private static readonly MAP_AUTHOR_LOOKER_URLS: ReadonlyMap<number, string> = new Map([
    [
      1,
      'https://lookerstudio.google.com/embed/reporting/6286ad72-e690-4009-981e-afa5189fc88b/page/flffF'
    ]
    // Adicione novos autores aqui:
    // [2, 'https://lookerstudio.google.com/embed/reporting/...'],
    // [3, 'https://lookerstudio.google.com/embed/reporting/...'],
  ]);

  /**
   * Computed que retorna a URL do Looker Studio para o authorId atual.
   * Retorna null se o authorId não estiver mapeado.
   */
  readonly lookerStudioUrl = computed<SafeResourceUrl | null>(() => {
    // Garantir que authorId é um número
    const authorIdNum = typeof this.authorId === 'string' ? Number(this.authorId) : this.authorId;
    
    if (isNaN(authorIdNum)) {
      console.error(`[AuthorMetricsDashboard] authorId inválido: ${this.authorId} (tipo: ${typeof this.authorId})`);
      return null;
    }
    
    const url = AuthorMetricsDashboardComponent.MAP_AUTHOR_LOOKER_URLS.get(authorIdNum);
    
    if (!url) {
      console.warn(`[AuthorMetricsDashboard] URL do Looker Studio não encontrada para authorId=${authorIdNum}`);
      return null;
    }

    console.log(`[AuthorMetricsDashboard] Carregando dashboard para authorId=${authorIdNum}`);
    
    // Sanitizar a URL para uso seguro no iframe
    return this.sanitizer.bypassSecurityTrustResourceUrl(url);
  });

  /**
   * Computed que indica se há uma URL válida para exibir.
   */
  readonly hasValidUrl = computed(() => this.lookerStudioUrl() !== null);
}

