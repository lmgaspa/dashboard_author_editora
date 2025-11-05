import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  // Landing page
  {
    path: '',
    loadComponent: () => import('./layout/layouts/public-layout/public-layout.component').then(m => m.PublicLayoutComponent),
    children: [
      {
        path: '',
        loadComponent: () => import('./features/auth/pages/landing/landing-page.component').then(m => m.LandingPageComponent)
      }
    ]
  },
  
  // Rotas públicas (sem autenticação)
  {
    path: '',
    loadComponent: () => import('./layout/layouts/public-layout/public-layout.component').then(m => m.PublicLayoutComponent),
    children: [
      {
        path: 'login',
        loadComponent: () => import('./features/auth/pages/login/login-page.component').then(m => m.LoginPageComponent)
      },
      {
        path: 'forgot-password',
        loadComponent: () => import('./features/auth/pages/forgot-password/forgot-password-page.component').then(m => m.ForgotPasswordPageComponent)
      },
      {
        path: 'reset-password',
        loadComponent: () => import('./features/auth/pages/reset-password/reset-password-page.component').then(m => m.ResetPasswordPageComponent)
      },
      {
        path: 'confirm-account',
        loadComponent: () => import('./features/auth/pages/confirm-account/confirm-account-page.component').then(m => m.ConfirmAccountPageComponent)
      }
    ]
  },

  // Rotas de usuário (requer autenticação USER ou ADMIN)
  {
    path: '',
    loadComponent: () => import('./layout/layouts/user-layout/user-layout.component').then(m => m.UserLayoutComponent),
    canActivate: [authGuard],
    children: [
      {
        path: 'user',
        canActivate: [roleGuard(['USER', 'ADMIN'])],
        children: [
          {
            path: 'dashboard',
            loadComponent: () => import('./features/user/pages/dashboard/user-dashboard-page.component').then(m => m.UserDashboardPageComponent)
          },
          {
            path: 'profile',
            loadComponent: () => import('./features/user/pages/profile/user-profile-page.component').then(m => m.UserProfilePageComponent)
          },
          {
            path: 'change-password',
            loadComponent: () => import('./features/user/pages/change-password/change-password-page.component').then(m => m.ChangePasswordPageComponent)
          },
          {
            path: 'change-email',
            loadComponent: () => import('./features/user/pages/change-email/change-email-page.component').then(m => m.ChangeEmailPageComponent)
          }
        ]
      }
    ]
  },

  // Rotas de admin (requer role ADMIN)
  {
    path: '',
    loadComponent: () => import('./layout/layouts/admin-layout/admin-layout.component').then(m => m.AdminLayoutComponent),
    canActivate: [authGuard, roleGuard(['ADMIN'])],
    children: [
      {
        path: 'admin',
        children: [
          {
            path: 'dashboard',
            loadComponent: () => import('./features/admin/pages/dashboard/admin-dashboard-page.component').then(m => m.AdminDashboardPageComponent)
          },
          {
            path: 'users',
            loadComponent: () => import('./features/admin/pages/users/users-page.component').then(m => m.UsersPageComponent)
          },
          {
            path: 'users/create',
            loadComponent: () => import('./features/admin/pages/users/create-user-page.component').then(m => m.CreateUserPageComponent)
          },
          {
            path: 'admin-info',
            loadComponent: () => import('./features/admin/pages/admin-info/admin-info-page.component').then(m => m.AdminInfoPageComponent)
          },
          {
            path: 'database/status',
            loadComponent: () => import('./features/admin/pages/database-status/database-status-page.component').then(m => m.DatabaseStatusPageComponent)
          }
        ]
      }
    ]
  },

  // Rota de logout
  {
    path: 'logout',
    canActivate: [authGuard],
    loadComponent: () => import('./features/auth/pages/logout/logout-page.component').then(m => m.LogoutPageComponent)
  },

  // Rota 404
  { path: '**', redirectTo: '/login' }
];
