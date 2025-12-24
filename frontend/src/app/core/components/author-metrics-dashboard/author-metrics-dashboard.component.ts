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
  styles: [],
})
export class AuthorMetricsDashboardComponent {
  private readonly sanitizer = inject(DomSanitizer);

  /**
   * ID do autor para exibir o dashboard correspondente.
   */
  @Input({ required: true }) authorId!: number;

  /**
   * Base URL do relatório do Looker Studio
   */
  private static readonly REPORT_BASE_URL =
    'https://lookerstudio.google.com/embed/reporting/6286ad72-e690-4009-981e-afa5189fc88b/page/flffF';

  /**
   * Computed que retorna a URL do Looker Studio para o authorId atual.
   * Gera a URL com o parâmetro p_author_id e um cache buster.
   */
  readonly lookerStudioUrl = computed<SafeResourceUrl | null>(() => {
    // Garantir que authorId é um número
    let authorIdNum = typeof this.authorId === 'string' ? Number(this.authorId) : this.authorId;

    if (isNaN(authorIdNum)) {
      console.error(
        `[AuthorMetricsDashboard] authorId inválido: ${this.authorId} (tipo: ${typeof this
          .authorId})`
      );
      return null;
    }

    // Override: Se for autor 2, usar ID 11
    if (authorIdNum === 2) {
      console.warn('[AuthorMetricsDashboard] Override: Redirecionando autor 2 para dashboard do autor 11');
      authorIdNum = 11;
    }

    // Configurar parâmetros JSON
    const paramsJson = JSON.stringify({
      p_author_id: authorIdNum,
    });

    // Codificar parâmetros para URL
    const encodedParams = encodeURIComponent(paramsJson);

    // Adicionar cache buster para garantir atualização
    const cacheBuster = Date.now();

    // Montar URL final
    const finalUrl = `${AuthorMetricsDashboardComponent.REPORT_BASE_URL}?params=${encodedParams}&v=${cacheBuster}`;

    console.log(`[AuthorMetricsDashboard] Carregando dashboard para authorId=${authorIdNum}`);
    console.log(`[AuthorMetricsDashboard] URL gerada: ${finalUrl}`);

    // Sanitizar a URL para uso seguro no iframe
    return this.sanitizer.bypassSecurityTrustResourceUrl(finalUrl);
  });

  /**
   * Computed que indica se há uma URL válida para exibir.
   */
  readonly hasValidUrl = computed(() => this.lookerStudioUrl() !== null);
}
