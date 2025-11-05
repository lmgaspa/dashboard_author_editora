import { HttpInterceptorFn, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError, switchMap } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { getCsrfTokenFromCookie } from '../utils/cookie.util';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const authService = inject(AuthService);
  const token = localStorage.getItem('accessToken');

  // Clonar requisição para adicionar headers
  let headers = new HttpHeaders();
  
  // Adicionar Authorization header se tiver token
  if (token) {
    headers = headers.set('Authorization', `Bearer ${token}`);
  }

  // Adicionar CSRF token se for requisição que precisa (POST, PUT, DELETE, PATCH)
  const method = req.method.toUpperCase();
  if (['POST', 'PUT', 'DELETE', 'PATCH'].includes(method)) {
    const csrfToken = getCsrfTokenFromCookie();
    if (csrfToken) {
      headers = headers.set('X-CSRF-Token', csrfToken);
    }
  }

  // Adicionar withCredentials para enviar cookies
  const clonedReq = req.clone({
    headers,
    withCredentials: true
  });

  return next(clonedReq).pipe(
    catchError((error: HttpErrorResponse) => {
      // Se for erro 401 e não for a requisição de refresh token, tentar fazer refresh
      if (error.status === 401 && !req.url.includes('/refresh-token')) {
        // Verificar se deve tentar refresh
        if (authService.shouldAttemptRefresh()) {
          return authService.refreshToken().pipe(
            switchMap(() => {
              // Se o refresh foi bem-sucedido, repetir a requisição original
              const newToken = localStorage.getItem('accessToken');
              let newHeaders = new HttpHeaders()
                .set('Authorization', `Bearer ${newToken}`);
              
              const csrfToken = getCsrfTokenFromCookie();
              if (csrfToken && ['POST', 'PUT', 'DELETE', 'PATCH'].includes(method)) {
                newHeaders = newHeaders.set('X-CSRF-Token', csrfToken);
              }

              return next(req.clone({
                headers: newHeaders,
                withCredentials: true
              }));
            }),
            catchError((refreshError) => {
              // Se o refresh falhou, limpar auth e redirecionar
              authService.logout();
              return throwError(() => refreshError);
            })
          );
        } else {
          // Não deve tentar refresh - limpar e redirecionar
          localStorage.removeItem('accessToken');
          localStorage.removeItem('currentUser');
          router.navigate(['/login']);
        }
      } else if (error.status === 403) {
        // CSRF token inválido - pode tentar novamente ou redirecionar
        console.warn('CSRF token validation failed');
      }
      
      return throwError(() => error);
    })
  );
};

