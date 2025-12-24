import { Component, signal, inject, OnInit, computed, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '@/app/core/services/auth.service';
import { HttpClient } from '@angular/common/http';
import { environment } from '@/environments/environment';
import { User } from '@/app/core/models/menu-item.model';
import { AuthorMetricsDashboardComponent } from '@/app/core/components/author-metrics-dashboard/author-metrics-dashboard.component';

interface UsersResponse {
  message: string;
  total: number;
  users: User[];
}

@Component({
  selector: 'app-metrics-page',
  standalone: true,
  imports: [CommonModule, AuthorMetricsDashboardComponent],
  templateUrl: './metrics-page.component.html',
  styles: []
})
export class MetricsPageComponent implements OnInit, OnDestroy {
  private readonly authService = inject(AuthService);
  private readonly http = inject(HttpClient);
  private readonly API_URL = environment.apiUrl;

  // Estados
  readonly loading = signal<boolean>(true);
  readonly error = signal<string | null>(null);
  
  // Para ADMIN: seleção de autor
  readonly authors = signal<User[]>([]);
  readonly selectedAuthorId = signal<number | null>(null);

  // Computed
  readonly isAdmin = computed(() => this.authService.isAdmin());
  readonly currentUser = computed(() => this.authService.currentUser());
  
  // AuthorId para o componente de dashboard (número)
  readonly dashboardAuthorId = computed(() => {
    if (this.isAdmin()) {
      return this.selectedAuthorId() || 1; // Default to 1 (General/Admin view) if nothing selected
    }
    // Para USER, pegar do currentUser e converter para número
    const authorId = this.currentUser()?.authorId;
    if (!authorId) return null;
    const numId = Number(authorId);
    return isNaN(numId) ? null : numId;
  });
  
  readonly hasMetrics = computed(() => this.dashboardAuthorId() !== null);
  
  readonly selectedAuthor = computed(() => {
    const authorId = this.selectedAuthorId();
    if (!authorId) return null;
    return this.authors().find(a => {
      const aId = Number(a.authorId);
      return !isNaN(aId) && aId === authorId;
    });
  });

  ngOnInit(): void {
    this.loadMetrics();
    if (this.isAdmin()) {
      this.loadAuthors();
    }
  }

  ngOnDestroy(): void {
    // Cleanup se necessário
  }

  loadMetrics(): void {
    this.loading.set(true);
    this.error.set(null);

    // Buscar perfil atualizado para garantir que temos o authorId
    this.authService.getUserProfile().subscribe({
      next: (user) => {
        // Verificar se tem authorId válido
        // Se for ADMIN, não precisa ter authorId configurado no perfil
        if (!user.authorId && !this.isAdmin()) {
          this.error.set('Author ID não configurado. Entre em contato com o administrador.');
          this.loading.set(false);
          return;
        }
        
        const authorIdNum = Number(user.authorId);
        if (isNaN(authorIdNum)) {
          this.error.set('Author ID inválido. Entre em contato com o administrador.');
          this.loading.set(false);
          return;
        }
        
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Erro ao carregar perfil:', err);
        // Tentar usar dados do cache
        const cachedUser = this.currentUser();
        if (cachedUser?.authorId) {
          const authorIdNum = Number(cachedUser.authorId);
          if (!isNaN(authorIdNum)) {
            this.loading.set(false);
            return;
          }
        }
        this.error.set('Erro ao carregar configurações de métricas');
        this.loading.set(false);
      }
    });
  }

  loadAuthors(): void {
    this.http.get<any>(`${this.API_URL}/api/v1/admin/users`).subscribe({
      next: (response) => {
        // Normalizar author_id para authorId (backend retorna snake_case, frontend usa camelCase)
        const normalizedUsers = (response.users || []).map((u: any) => ({
          ...u,
          authorId: u.author_id || u.authorId,
          lookerStudioUrl: u.looker_studio_url || u.lookerStudioUrl
        }));
        // Filtrar apenas usuários com authorId válido (número)
        const authorsWithId = normalizedUsers.filter((u: User) => {
          if (!u.authorId) return false;
          const numId = Number(u.authorId);
          return !isNaN(numId);
        });
        this.authors.set(authorsWithId);
        
        // Se não houver autor selecionado, tentar selecionar o próprio usuário se ele estiver na lista
        if (!this.selectedAuthorId() && authorsWithId.length > 0) {
          // Tentar encontrar o próprio admin na lista de autores
          const currentUser = this.currentUser();
          let targetAuthorId: number | null = null;
          
          if (currentUser?.authorId) {
            const myId = Number(currentUser.authorId);
            const exists = authorsWithId.some((a: User) => Number(a.authorId) === myId);
            if (exists) {
              targetAuthorId = myId;
            }
          }
          
          // Fallback para o primeiro da lista
          if (!targetAuthorId) {
            targetAuthorId = Number(authorsWithId[0].authorId);
          }
          
          if (targetAuthorId && !isNaN(targetAuthorId)) {
            this.selectedAuthorId.set(targetAuthorId);
          }
        }
      },
      error: (err) => {
        console.error('Erro ao carregar autores:', err);
      }
    });
  }

  onAuthorChange(authorIdStr: string | null): void {
    if (!authorIdStr) {
      this.selectedAuthorId.set(null);
      return;
    }
    
    const authorIdNum = Number(authorIdStr);
    if (isNaN(authorIdNum)) {
      console.error('AuthorId inválido:', authorIdStr);
      return;
    }
    
    this.selectedAuthorId.set(authorIdNum);
  }

  retry(): void {
    this.loadMetrics();
  }
}
