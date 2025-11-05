import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, catchError, of, throwError } from 'rxjs';
import { User, AuthResponse, LoginRequest, ResetPasswordRequest, ChangePasswordRequest, ChangeEmailRequest } from '../models/menu-item.model';
import { environment } from '@/environments/environment';
import { getCsrfTokenFromCookie, hasCookie } from '../utils/cookie.util';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly API_URL = environment.apiUrl;

  // Signals para estado reativo
  private readonly _currentUser = signal<User | null>(null);
  private readonly _isAuthenticated = signal<boolean>(false);
  private readonly _loading = signal<boolean>(false);

  // Computed signals
  readonly currentUser = this._currentUser.asReadonly();
  readonly isAuthenticated = computed(() => this._isAuthenticated());
  readonly loading = this._loading.asReadonly();
  readonly isAdmin = computed(() => this._currentUser()?.role === 'ADMIN');
  readonly isUser = computed(() => this._currentUser()?.role === 'USER');

  constructor() {
    this.loadUserFromStorage();
  }

  private loadUserFromStorage(): void {
    const token = localStorage.getItem('accessToken');
    const userStr = localStorage.getItem('currentUser');
    
    if (token && userStr) {
      try {
        const user = JSON.parse(userStr);
        this._currentUser.set(user);
        this._isAuthenticated.set(true);
      } catch (error) {
        this.clearAuth();
      }
    }
  }

  login(credentials: LoginRequest): Observable<AuthResponse> {
    this._loading.set(true);
    return this.http.post<AuthResponse>(`${this.API_URL}/api/v1/auth/login`, credentials)
      .pipe(
        tap(response => {
          this.setAuth(response);
          this._loading.set(false);
        }),
        catchError(error => {
          this._loading.set(false);
          throw error;
        })
      );
  }

  loginWithGoogle(): void {
    // Redirecionar para endpoint OAuth do backend
    window.location.href = `${this.API_URL}/api/v1/auth/google`;
  }

  logout(): void {
    this.http.post(`${this.API_URL}/api/v1/auth/logout`, {}).subscribe({
      next: () => this.clearAuth(),
      error: () => this.clearAuth() // Limpar mesmo se houver erro
    });
  }

  forgotPassword(email: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.API_URL}/api/v1/auth/password/forgot`, { email });
  }

  resetPassword(data: ResetPasswordRequest): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.API_URL}/api/v1/auth/password/reset`, data);
  }

  confirmAccount(token: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.API_URL}/api/v1/auth/confirm`, { token });
  }

  changePassword(data: ChangePasswordRequest): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.API_URL}/api/v1/auth/password/change`, data);
  }

  changeEmail(data: ChangeEmailRequest): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.API_URL}/api/v1/auth/email/change`, data);
  }

  getUserProfile(): Observable<User> {
    return this.http.get<User>(`${this.API_URL}/api/user/profile`).pipe(
      tap(user => {
        this._currentUser.set(user);
        localStorage.setItem('currentUser', JSON.stringify(user));
      })
    );
  }

  /**
   * Verifica se o frontend deve tentar fazer refresh do token
   * @returns true se deve tentar refresh, false caso contrário
   */
  shouldAttemptRefresh(): boolean {
    // Verificar se existe cookie de refresh_token
    const hasRefreshToken = hasCookie('refresh_token');
    
    // Lista de páginas públicas onde refresh não deve ser tentado
    const publicPages = [
      '/login',
      '/register',
      '/forgot-password',
      '/reset-password',
      '/confirm-account',
      '/set-password',
      '' // landing page
    ];
    
    const currentPath = this.router.url;
    const isPublicPage = publicPages.some(path => 
      currentPath.includes(path) || currentPath === path
    );
    
    // Só tenta refresh se tem token E não está em página pública
    return hasRefreshToken && !isPublicPage;
  }

  /**
   * Faz refresh do token de acesso usando o refresh token do cookie
   * @returns Observable com o novo access token
   */
  refreshToken(): Observable<{ token: string }> {
    // ✅ VALIDAÇÃO: Não faz requisição se não deve
    if (!this.shouldAttemptRefresh()) {
      return throwError(() => new Error('Should not refresh token on public pages'));
    }

    // Obter CSRF token do cookie
    const csrfToken = getCsrfTokenFromCookie();
    if (!csrfToken) {
      return throwError(() => new Error('CSRF token not found'));
    }

    // Fazer requisição com CSRF token no header
    const headers = new HttpHeaders({
      'X-CSRF-Token': csrfToken
    });

    return this.http.post<{ token: string }>(
      `${this.API_URL}/api/v1/auth/refresh-token`,
      {}, // Body vazio - o refresh token vem do cookie
      {
        headers,
        withCredentials: true // Importante para enviar cookies
      }
    ).pipe(
      tap(response => {
        // Atualizar access token no localStorage
        if (response.token) {
          localStorage.setItem('accessToken', response.token);
        }
      }),
      catchError(error => {
        // Tratar erros
        if (error.status === 401) {
          // Token expirado ou inválido - redirecionar para login
          this.clearAuth();
        } else if (error.status === 403) {
          // CSRF inválido - pode tentar novamente ou redirecionar
          console.warn('CSRF token validation failed');
          // Tentar novamente pode ser feito aqui, mas por segurança vamos limpar
          this.clearAuth();
        }
        return throwError(() => error);
      })
    );
  }

  private setAuth(response: AuthResponse): void {
    localStorage.setItem('accessToken', response.accessToken);
    // refreshToken agora é gerenciado pelo backend via cookies (httpOnly)
    // Não precisa armazenar no localStorage
    localStorage.setItem('currentUser', JSON.stringify(response.user));
    this._currentUser.set(response.user);
    this._isAuthenticated.set(true);
  }

  private clearAuth(): void {
    localStorage.removeItem('accessToken');
    // refreshToken está nos cookies, será limpo pelo backend no logout
    localStorage.removeItem('currentUser');
    this._currentUser.set(null);
    this._isAuthenticated.set(false);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem('accessToken');
  }

  hasRole(role: string): boolean {
    const user = this._currentUser();
    return user?.role === role;
  }

  hasAnyRole(roles: string[]): boolean {
    const user = this._currentUser();
    return user ? roles.includes(user.role) : false;
  }
}

