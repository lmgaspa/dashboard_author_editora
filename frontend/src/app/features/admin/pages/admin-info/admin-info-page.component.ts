import { Component, signal, inject, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { environment } from '@/environments/environment';

interface User {
  id: string;
  name: string;
  email: string;
  role: string;
  emailConfirmed: boolean;
  authProvider: string;
  createdAt?: string;
  authorId?: string;
  ecommerceUrl?: string;
  ecommerceDbUrl?: string;
  ecommerceDbUsername?: string;
  ecommerceDbPassword?: string | null;
}

interface UsersResponse {
  message: string;
  total: number;
  users: User[];
}

@Component({
  selector: 'app-admin-info-page',
  standalone: true,
  imports: [CommonModule, DatePipe],
  templateUrl: './admin-info-page.component.html',
  styles: []
})
export class AdminInfoPageComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly API_URL = environment.apiUrl;

  readonly admins = signal<User[]>([]);
  readonly loading = signal<boolean>(false);
  readonly error = signal<string | null>(null);
  readonly total = signal<number>(0);

  ngOnInit(): void {
    this.loadAdmins();
  }

  loadAdmins(): void {
    this.loading.set(true);
    this.error.set(null);
    
    this.http.get<any>(`${this.API_URL}/api/v1/admin/admin-info`).subscribe({
      next: (response) => {
        // Extrair o array de usuários da resposta
        const users = response?.users || [];
        // Normalizar author_id para authorId (backend retorna snake_case, frontend usa camelCase)
        const normalizedUsers = users.map((user: any) => ({
          ...user,
          authorId: user.author_id || user.authorId,
          ecommerceUrl: user.ecommerce_url || user.ecommerceUrl
        }));
        this.admins.set(normalizedUsers);
        this.total.set(response?.total || 0);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Erro ao carregar administradores:', err);
        const errorMessage = err.error?.message || 'Erro ao carregar administradores.';
        this.error.set(errorMessage);
        this.loading.set(false);
        this.admins.set([]);
        this.total.set(0);
      }
    });
  }
}

