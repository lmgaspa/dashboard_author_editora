import { Component, signal, inject, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterModule } from '@angular/router';
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
  selector: 'app-users-page',
  standalone: true,
  imports: [CommonModule, RouterModule, DatePipe],
  templateUrl: './users-page.component.html',
  styles: []
})
export class UsersPageComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly API_URL = environment.apiUrl;

  readonly users = signal<User[]>([]);
  readonly loading = signal<boolean>(false);
  readonly error = signal<string | null>(null);
  readonly total = signal<number>(0);
  readonly deletingUserId = signal<string | null>(null);

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading.set(true);
    this.error.set(null);
    
    this.http.get<UsersResponse>(`${this.API_URL}/api/v1/admin/users`).subscribe({
      next: (response) => {
        // Extrair o array de usuários da resposta
        const users = response?.users || [];
        this.users.set(users);
        this.total.set(response?.total || 0);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Erro ao carregar usuários:', err);
        const errorMessage = err.error?.message || 'Erro ao carregar usuários.';
        this.error.set(errorMessage);
        this.loading.set(false);
        this.users.set([]);
        this.total.set(0);
      }
    });
  }

  deleteUser(user: User): void {
    // Confirmação antes de deletar
    const confirmMessage = `Tem certeza que deseja excluir o usuário "${user.name}" (${user.email})?\n\nEsta ação não pode ser desfeita.`;
    
    if (!confirm(confirmMessage)) {
      return;
    }

    // Usar ID como identificador (o backend também aceita email)
    const identifier = user.id;
    this.deletingUserId.set(identifier);

    this.http.delete(`${this.API_URL}/api/v1/admin/users/${identifier}`).subscribe({
      next: () => {
        // Remover o usuário da lista localmente
        this.users.update(users => users.filter(u => u.id !== user.id));
        this.total.update(total => Math.max(0, total - 1));
        this.deletingUserId.set(null);
        
        // Opcional: mostrar mensagem de sucesso
        console.log(`Usuário ${user.name} deletado com sucesso`);
      },
      error: (err) => {
        console.error('Erro ao deletar usuário:', err);
        const errorMessage = err.error?.message || 'Erro ao deletar usuário.';
        alert(`Erro ao deletar usuário: ${errorMessage}`);
        this.deletingUserId.set(null);
      }
    });
  }
}

