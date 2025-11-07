import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, catchError, map } from 'rxjs';
import { User, AuthResponse, LoginRequest, ResetPasswordRequest, ChangePasswordRequest, ChangeEmailRequest, ProfileResponse } from '../models/menu-item.model';
import { environment } from '@/environments/environment';

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
    return this.http.post<any>(`${this.API_URL}/api/v1/auth/login`, credentials)
      .pipe(
        tap(response => {
          console.log('🔐 Login Response completa:', response);
          console.log('📋 Chaves da resposta:', Object.keys(response || {}));
          
          // Normalizar resposta - pode vir como 'token' ou 'accessToken'
          const normalizedResponse: AuthResponse = {
            accessToken: response?.accessToken || response?.token || response?.access_token,
            user: response?.user || response?.userData || response?.userInfo
          };
          
          console.log('🔑 AccessToken encontrado:', normalizedResponse.accessToken ? 'Sim' : 'Não');
          if (normalizedResponse.accessToken) {
            console.log('🔑 Token (primeiros 20 chars):', normalizedResponse.accessToken.substring(0, 20) + '...');
          }
          
          console.log('👤 User encontrado:', normalizedResponse.user ? 'Sim' : 'Não');
          if (normalizedResponse.user) {
            console.log('👤 User data:', normalizedResponse.user);
          }
          
          if (!normalizedResponse.accessToken) {
            console.error('❌ Erro: accessToken não encontrado na resposta');
            console.error('🔍 Resposta original:', response);
            throw new Error('Token de acesso não recebido do servidor');
          }
          
          if (!normalizedResponse.user) {
            console.error('❌ Erro: user não encontrado na resposta');
            console.error('🔍 Resposta original:', response);
            throw new Error('Dados do usuário não recebidos do servidor');
          }
          
          this.setAuth(normalizedResponse);
          console.log('✅ Auth configurado com sucesso');
          console.log('👤 Usuário atual:', this._currentUser());
          console.log('🔐 Token salvo:', localStorage.getItem('accessToken') ? 'Sim' : 'Não');
          this._loading.set(false);
        }),
        catchError(error => {
          console.error('❌ Erro no login:', error);
          console.error('📋 Status:', error.status);
          console.error('📋 Message:', error.message);
          console.error('📋 Error object:', error.error);
          this._loading.set(false);
          throw error;
        })
      );
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


  changePassword(data: ChangePasswordRequest): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.API_URL}/api/v1/auth/password/change`, data);
  }

  changeEmail(data: ChangeEmailRequest): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.API_URL}/api/v1/auth/email/change-request`, data);
  }

  getUserProfile(): Observable<User> {
    return this.http.get<ProfileResponse>(`${this.API_URL}/api/v1/user/profile`).pipe(
      map((response: ProfileResponse) => {
        console.log('📦 ProfileResponse recebida (GET):', response);
        
        // Mapear ProfileResponse para User
        const currentUser = this._currentUser();
        const mappedUser: User = {
          id: response.id,
          name: response.name,
          email: response.email,
          role: currentUser?.role || 'USER', // Manter o role atual ou default
          avatar: currentUser?.avatar,
          createdAt: currentUser?.createdAt
        };
        
        console.log('✅ User mapeado (GET):', mappedUser);
        return mappedUser;
      }),
      tap((user: User) => {
        this._currentUser.set(user);
        localStorage.setItem('currentUser', JSON.stringify(user));
      }),
      catchError((error) => {
        console.error('❌ Erro ao buscar perfil:', error);
        throw error;
      })
    );
  }

  updateProfile(data: { name: string }): Observable<User> {
    return this.http.put<ProfileResponse>(`${this.API_URL}/api/v1/user/profile`, data).pipe(
      map((response: ProfileResponse) => {
        console.log('📦 ProfileResponse recebida:', response);
        
        // Mapear ProfileResponse para User
        // O backend não retorna 'role' no ProfileResponse, então mantemos o role atual
        const currentUser = this._currentUser();
        const mappedUser: User = {
          id: response.id,
          name: response.name,
          email: response.email,
          role: currentUser?.role || 'USER', // Manter o role atual ou default
          avatar: currentUser?.avatar,
          createdAt: currentUser?.createdAt
        };
        
        console.log('✅ User mapeado:', mappedUser);
        return mappedUser;
      }),
      tap((user: User) => {
        console.log('💾 Salvando user atualizado:', user);
        this._currentUser.set(user);
        localStorage.setItem('currentUser', JSON.stringify(user));
        console.log('✅ User salvo com sucesso');
      }),
      catchError((error) => {
        console.error('❌ Erro ao atualizar perfil:', error);
        console.error('📋 Status:', error.status);
        console.error('📋 Message:', error.message);
        console.error('📋 Error object:', error.error);
        throw error;
      })
    );
  }

  /**
   * Deleta a conta do usuário atual (opcional)
   * @returns Observable com mensagem de sucesso
   */
  deleteAccount(): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.API_URL}/api/v1/user/account`).pipe(
      tap(() => {
        // Limpar autenticação após deletar conta
        this.clearAuth();
      })
    );
  }

  private setAuth(response: AuthResponse): void {
    console.log('🔧 setAuth chamado com:', response);
    
    if (response.accessToken) {
      localStorage.setItem('accessToken', response.accessToken);
      console.log('✅ Token salvo no localStorage');
    } else {
      console.error('❌ Token não encontrado na resposta');
    }
    
    if (response.user) {
      localStorage.setItem('currentUser', JSON.stringify(response.user));
      this._currentUser.set(response.user);
      console.log('✅ User salvo e atualizado:', response.user);
    } else {
      console.error('❌ User não encontrado na resposta');
    }
    
    this._isAuthenticated.set(true);
    console.log('✅ Estado de autenticação atualizado:', this._isAuthenticated());
  }

  private clearAuth(): void {
    localStorage.removeItem('accessToken');
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

