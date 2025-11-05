import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const roleGuard = (allowedRoles: string[]): CanActivateFn => {
  return (route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isAuthenticated()) {
      router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
      return false;
    }

    const userRole = authService.currentUser()?.role;
    
    if (!userRole || !allowedRoles.includes(userRole)) {
      // Redirecionar para dashboard baseado no role
      const dashboardRoute = userRole === 'ADMIN' ? '/admin/dashboard' : '/user/dashboard';
      router.navigate([dashboardRoute]);
      return false;
    }

    return true;
  };
};

