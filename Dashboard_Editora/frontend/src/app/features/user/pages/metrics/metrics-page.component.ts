import { Component, signal, inject, OnInit, computed, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { AuthService } from '@/app/core/services/auth.service';
import { ExportService, ExportFormat } from '@/app/core/services/export.service';
import { HttpClient } from '@angular/common/http';
import { environment } from '@/environments/environment';
import { User } from '@/app/core/models/menu-item.model';

interface UsersResponse {
  message: string;
  total: number;
  users: User[];
}

@Component({
  selector: 'app-metrics-page',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './metrics-page.component.html',
  styles: []
})
export class MetricsPageComponent implements OnInit, OnDestroy {
  private readonly authService = inject(AuthService);
  private readonly sanitizer = inject(DomSanitizer);
  private readonly exportService = inject(ExportService);
  private readonly http = inject(HttpClient);
  private readonly API_URL = environment.apiUrl;

  // Estados
  readonly loading = signal<boolean>(true);
  readonly error = signal<string | null>(null);
  readonly lookerStudioUrl = signal<SafeResourceUrl | null>(null);
  readonly hasMetrics = computed(() => this.lookerStudioUrl() !== null);
  
  // Para ADMIN: seleção de autor
  readonly authors = signal<User[]>([]);
  readonly selectedAuthorId = signal<string | null>(null);
  readonly showExportDropdown = signal<boolean>(false);
  readonly exporting = signal<boolean>(false);
  readonly exportError = signal<string | null>(null);
  readonly exportSuccess = signal<boolean>(false);
  private clickListener?: (e: Event) => void;

  // Computed
  readonly isAdmin = computed(() => this.authService.isAdmin());
  readonly currentUser = computed(() => this.authService.currentUser());
  readonly selectedAuthor = computed(() => {
    const authorId = this.selectedAuthorId();
    if (!authorId) return null;
    return this.authors().find(a => a.authorId === authorId);
  });

  ngOnInit(): void {
    this.loadMetrics();
    if (this.isAdmin()) {
      this.loadAuthors();
    }

    // Listener para fechar dropdown ao clicar fora
    this.clickListener = (e: Event) => {
      if (this.showExportDropdown()) {
        const target = e.target as HTMLElement;
        const exportDropdown = target?.closest('[data-export-dropdown]');
        if (!exportDropdown) {
          this.showExportDropdown.set(false);
        }
      }
    };
    document.addEventListener('click', this.clickListener);
  }

  ngOnDestroy(): void {
    if (this.clickListener) {
      document.removeEventListener('click', this.clickListener);
    }
  }

  loadMetrics(): void {
    this.loading.set(true);
    this.error.set(null);

    // Buscar perfil atualizado para pegar lookerStudioUrl
    this.authService.getUserProfile().subscribe({
      next: (user) => {
        this.processLookerStudioUrl(user.lookerStudioUrl, user.authorId);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Erro ao carregar perfil:', err);
        // Tentar usar dados do cache
        const cachedUser = this.currentUser();
        if (cachedUser) {
          this.processLookerStudioUrl(cachedUser.lookerStudioUrl, cachedUser.authorId);
          this.loading.set(false);
        } else {
          this.error.set('Erro ao carregar configurações de métricas');
          this.loading.set(false);
        }
      }
    });
  }

  loadAuthors(): void {
    this.http.get<UsersResponse>(`${this.API_URL}/api/v1/admin/users`).subscribe({
      next: (response) => {
        // Filtrar apenas usuários com authorId
        const authorsWithId = (response.users || []).filter(u => u.authorId);
        this.authors.set(authorsWithId);
        
        // Se não houver autor selecionado, selecionar o primeiro
        if (!this.selectedAuthorId() && authorsWithId.length > 0) {
          this.selectedAuthorId.set(authorsWithId[0].authorId || null);
          this.updateMetricsForAuthor(authorsWithId[0].authorId || null);
        }
      },
      error: (err) => {
        console.error('Erro ao carregar autores:', err);
      }
    });
  }

  onAuthorChange(authorId: string | null): void {
    this.selectedAuthorId.set(authorId);
    this.updateMetricsForAuthor(authorId);
  }

  private updateMetricsForAuthor(authorId: string | null): void {
    if (!authorId) {
      this.lookerStudioUrl.set(null);
      return;
    }

    const author = this.authors().find(a => a.authorId === authorId);
    if (!author || !author.lookerStudioUrl) {
      this.lookerStudioUrl.set(null);
      return;
    }

    this.processLookerStudioUrl(author.lookerStudioUrl, authorId);
  }

  private processLookerStudioUrl(url: string | null | undefined, authorId?: string | null): void {
    if (!url || url.trim() === '') {
      this.lookerStudioUrl.set(null);
      return;
    }

    // Validar URL
    if (!this.isValidLookerStudioUrl(url)) {
      this.error.set('URL do Looker Studio inválida');
      this.lookerStudioUrl.set(null);
      return;
    }

    // Se for USER e tiver authorId, adicionar parâmetro
    let finalUrl = url.trim();
    if (!this.isAdmin() && authorId) {
      // Adicionar parâmetro author_id_param na URL
      const urlObj = new URL(finalUrl);
      urlObj.searchParams.set('params.author_id_param', authorId);
      finalUrl = urlObj.toString();
    } else if (this.isAdmin() && this.selectedAuthorId()) {
      // Para ADMIN, usar o authorId selecionado
      const urlObj = new URL(finalUrl);
      urlObj.searchParams.set('params.author_id_param', this.selectedAuthorId()!);
      finalUrl = urlObj.toString();
    }

    // Sanitizar URL para uso seguro no iframe
    const safeUrl = this.sanitizer.bypassSecurityTrustResourceUrl(finalUrl);
    this.lookerStudioUrl.set(safeUrl);
  }

  private isValidLookerStudioUrl(url: string): boolean {
    try {
      const urlObj = new URL(url);
      return urlObj.hostname === 'lookerstudio.google.com' 
        || urlObj.hostname === 'datastudio.google.com';
    } catch {
      return false;
    }
  }

  retry(): void {
    this.loadMetrics();
  }

  toggleExportDropdown(): void {
    this.showExportDropdown.update(v => !v);
  }

  closeExportDropdown(): void {
    this.showExportDropdown.set(false);
  }

  exportMetrics(format: ExportFormat): void {
    this.exporting.set(true);
    this.exportError.set(null);
    this.exportSuccess.set(false);
    this.closeExportDropdown();

    const authorId = this.isAdmin() ? this.selectedAuthorId() : this.currentUser()?.authorId;

    if (!authorId) {
      this.exportError.set('Author ID não encontrado');
      this.exporting.set(false);
      return;
    }

    this.exportService.exportMetrics({
      format,
      authorId
    }).subscribe({
      next: (blob) => {
        const filename = this.exportService.generateFilename('metricas', format, authorId);
        this.exportService.downloadBlob(blob, filename);
        this.exportSuccess.set(true);
        setTimeout(() => this.exportSuccess.set(false), 3000);
        this.exporting.set(false);
      },
      error: (err) => {
        console.error('Erro ao exportar métricas:', err);
        this.exportError.set(err.error?.message || 'Erro ao exportar métricas. Tente novamente.');
        this.exporting.set(false);
      }
    });
  }
}
