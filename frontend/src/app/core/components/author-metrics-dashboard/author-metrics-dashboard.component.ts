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

    // Configurar parâmetros JSON
    // O Looker Studio espera params={"p_author_id": ID} (encoded)
    const paramsObj = {
      p_author_id: authorIdNum
    };
    
    const paramsJson = JSON.stringify(paramsObj);
    const encodedParams = encodeURIComponent(paramsJson);

    // URL base
    const baseUrl = AuthorMetricsDashboardComponent.REPORT_BASE_URL;
    
    // Cache buster (opcional, mas bom pra evitar cache do iframe)
    const cacheBuster = Date.now();

    // Montar URL final: base?params=...&v=...
    const finalUrl = `${baseUrl}?params=${encodedParams}&v=${cacheBuster}`;

    console.log(`[AuthorMetricsDashboard] 🟢 Gerando URL para authorId=${authorIdNum}`);
    console.log(`[AuthorMetricsDashboard] 📦 Params Obj:`, paramsObj);
    console.log(`[AuthorMetricsDashboard] 🔗 URL Final: ${finalUrl}`);

    // Sanitizar a URL para uso seguro no iframe
    return this.sanitizer.bypassSecurityTrustResourceUrl(finalUrl);
  });

  /**
   * Computed que indica se há uma URL válida para exibir.
   */
  readonly hasValidUrl = computed(() => this.lookerStudioUrl() !== null);
}
