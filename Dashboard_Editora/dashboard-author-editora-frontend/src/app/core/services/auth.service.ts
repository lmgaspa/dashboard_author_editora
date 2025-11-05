import { Injectable, signal, computed, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, catchError, of } from 'rxjs';
import { User, AuthResponse, LoginRequest, ResetPasswordRequest, ChangePasswordRequest, ChangeEmailRequest } from '../models/menu-item.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly API_URL = 'https://www.dashboard-author-editora.vercel.app';

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

  refreshToken(): Observable<AuthResponse> {
    const refreshToken = localStorage.getItem('refreshToken');
    if (!refreshToken) {
      return of({} as AuthResponse);
    }
    return this.http.post<AuthResponse>(`${this.API_URL}/api/v1/auth/refresh`, { refreshToken });
  }

  private setAuth(response: AuthResponse): void {
    localStorage.setItem('accessToken', response.accessToken);
    if (response.refreshToken) {
      localStorage.setItem('refreshToken', response.refreshToken);
    }
    localStorage.setItem('currentUser', JSON.stringify(response.user));
    this._currentUser.set(response.user);
    this._isAuthenticated.set(true);
  }

  private clearAuth(): void {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
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

