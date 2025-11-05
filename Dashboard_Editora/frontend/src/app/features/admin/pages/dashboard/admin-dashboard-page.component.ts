import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '@/app/core/services/auth.service';

@Component({
  selector: 'app-admin-dashboard-page',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="space-y-4">
      <!-- Header Section -->
      <div class="space-y-1">
        <h1 class="text-2xl font-black bg-gradient-to-r from-sky-400 via-blue-500 to-sky-400 bg-clip-text text-transparent">
          Dashboard Administrativo
        </h1>
        <p class="text-sm text-gray-400">
          Bem-vindo, <span class="font-semibold text-white">{{ authService.currentUser()?.name }}</span>! 👋
        </p>
      </div>

      <!-- Quick Actions Cards -->
      <div class="grid grid-cols-3 gap-3">
        <!-- Usuários Card -->
        <a [routerLink]="['/admin/users']" 
           class="group relative bg-gradient-to-br from-sky-500/10 via-blue-600/10 to-sky-500/10 backdrop-blur-xl border border-sky-500/20 rounded-xl p-4 shadow-lg hover:shadow-xl hover:shadow-sky-500/20 transition-all duration-300 hover:-translate-y-1 hover:border-sky-400/40 cursor-pointer overflow-hidden">
          <div class="absolute inset-0 bg-gradient-to-br from-sky-500/5 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
          
          <div class="relative z-10">
            <div class="w-10 h-10 rounded-lg bg-gradient-to-br from-sky-500 to-blue-600 flex items-center justify-center mb-3 shadow-md shadow-sky-500/30 group-hover:scale-110 transition-transform duration-300">
              <span class="material-icons text-xl text-white">people</span>
            </div>
            <h3 class="text-base font-bold text-white mb-1 group-hover:text-sky-300 transition-colors">
              Usuários
            </h3>
            <p class="text-xs text-gray-400 leading-tight">
              Gerenciar usuários do sistema
            </p>
          </div>
        </a>

        <!-- Administradores Card -->
        <a [routerLink]="['/admin/admin-info']" 
           class="group relative bg-gradient-to-br from-purple-500/10 via-pink-600/10 to-purple-500/10 backdrop-blur-xl border border-purple-500/20 rounded-xl p-4 shadow-lg hover:shadow-xl hover:shadow-purple-500/20 transition-all duration-300 hover:-translate-y-1 hover:border-purple-400/40 cursor-pointer overflow-hidden">
          <div class="absolute inset-0 bg-gradient-to-br from-purple-500/5 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
          
          <div class="relative z-10">
            <div class="w-10 h-10 rounded-lg bg-gradient-to-br from-purple-500 to-pink-600 flex items-center justify-center mb-3 shadow-md shadow-purple-500/30 group-hover:scale-110 transition-transform duration-300">
              <span class="material-icons text-xl text-white">admin_panel_settings</span>
            </div>
            <h3 class="text-base font-bold text-white mb-1 group-hover:text-purple-300 transition-colors">
              Administradores
            </h3>
            <p class="text-xs text-gray-400 leading-tight">
              Visualizar administradores
            </p>
          </div>
        </a>

        <!-- Status do Sistema Card -->
        <a [routerLink]="['/admin/database/status']" 
           class="group relative bg-gradient-to-br from-emerald-500/10 via-teal-600/10 to-emerald-500/10 backdrop-blur-xl border border-emerald-500/20 rounded-xl p-4 shadow-lg hover:shadow-xl hover:shadow-emerald-500/20 transition-all duration-300 hover:-translate-y-1 hover:border-emerald-400/40 cursor-pointer overflow-hidden">
          <div class="absolute inset-0 bg-gradient-to-br from-emerald-500/5 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
          
          <div class="relative z-10">
            <div class="w-10 h-10 rounded-lg bg-gradient-to-br from-emerald-500 to-teal-600 flex items-center justify-center mb-3 shadow-md shadow-emerald-500/30 group-hover:scale-110 transition-transform duration-300">
              <span class="material-icons text-xl text-white">monitoring</span>
            </div>
            <h3 class="text-base font-bold text-white mb-1 group-hover:text-emerald-300 transition-colors">
              Status do Sistema
            </h3>
            <p class="text-xs text-gray-400 leading-tight">
              Monitorar banco de dados
            </p>
          </div>
        </a>
      </div>
    </div>
  `,
  styles: []
})
export class AdminDashboardPageComponent {
  readonly authService = inject(AuthService);
}

