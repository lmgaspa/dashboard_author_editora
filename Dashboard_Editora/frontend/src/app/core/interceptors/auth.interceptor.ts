import { HttpInterceptorFn, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

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

  const clonedReq = req.clone({
    headers
  });

  return next(clonedReq).pipe(
    catchError((error: HttpErrorResponse) => {
      // Se for erro 401, limpar autenticação e redirecionar para login
      if (error.status === 401) {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('currentUser');
        router.navigate(['/login']);
      }
      
      return throwError(() => error);
    })
  );
};

