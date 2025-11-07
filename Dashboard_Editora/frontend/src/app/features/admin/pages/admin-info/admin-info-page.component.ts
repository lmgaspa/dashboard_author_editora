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
    
    this.http.get<UsersResponse>(`${this.API_URL}/api/v1/admin/admin-info`).subscribe({
      next: (response) => {
        // Extrair o array de usuários da resposta
        const users = response?.users || [];
        this.admins.set(users);
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

