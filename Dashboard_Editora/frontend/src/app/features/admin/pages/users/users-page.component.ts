import { Component, signal, inject, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { environment } from '@/environments/environment';
import { EditUserModalComponent } from './edit-user-modal.component';

interface User {
  id: string;
  name: string;
  email: string;
  role: string;
  emailConfirmed: boolean;
  authProvider: string;
  createdAt?: string;
  authorId?: string | null;
  ecommerceUrl?: string | null;
  ecommerceDbUrl?: string | null;
  ecommerceDbUsername?: string | null;
  ecommerceDbPassword?: string | null;
  profilePhotoUrl?: string | null;
}

interface UsersResponse {
  message: string;
  total: number;
  users: User[];
}

@Component({
  selector: 'app-users-page',
  standalone: true,
  imports: [CommonModule, RouterModule, DatePipe, EditUserModalComponent],
  templateUrl: './users-page.component.html',
  styles: []
})
export class UsersPageComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly API_URL = environment.apiUrl;

  readonly users = signal<User[]>([]);
  readonly loading = signal<boolean>(false);
  readonly error = signal<string | null>(null);
  readonly total = signal<number>(0);
  readonly deletingUserId = signal<string | null>(null);
  readonly success = signal<string | null>(null);
  readonly editingUser = signal<User | null>(null);

  ngOnInit(): void {
    const navigationState = history.state;
    if (navigationState?.successMessage) {
      this.success.set(navigationState.successMessage);
      history.replaceState({}, '', this.router.url);
    }

    this.loadUsers();
  }

  loadUsers(): void {
    this.loading.set(true);
    this.error.set(null);
    
    this.http.get<any>(`${this.API_URL}/api/v1/admin/users`).subscribe({
      next: (response) => {
        // Extrair o array de usuários da resposta
        const users = response?.users || [];
        // Normalizar author_id para authorId (backend retorna snake_case, frontend usa camelCase)
        const normalizedUsers = users.map((user: any) => ({
          ...user,
          authorId: user.author_id || user.authorId,
          ecommerceUrl: user.ecommerce_url || user.ecommerceUrl,
          ecommerceDbUrl: user.ecommerce_db_url || user.ecommerceDbUrl,
          ecommerceDbUsername: user.ecommerce_db_username || user.ecommerceDbUsername,
          ecommerceDbPassword: user.ecommerce_db_password || user.ecommerceDbPassword,
          profilePhotoUrl: user.profile_photo_url || user.profilePhotoUrl,
          lookerStudioUrl: user.looker_studio_url || user.lookerStudioUrl
        }));
        const sortedUsers = [...normalizedUsers].sort((a, b) => {
          if (a.role === 'ADMIN' && b.role !== 'ADMIN') return -1;
          if (a.role !== 'ADMIN' && b.role === 'ADMIN') return 1;
          return a.name.localeCompare(b.name);
        });
        this.users.set(sortedUsers);
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

  editUser(user: User): void {
    this.editingUser.set(user);
  }

  closeEditModal(): void {
    this.editingUser.set(null);
  }

  onUserUpdated(updatedUser: User): void {
    // Atualizar o usuário na lista
    this.users.update(users => {
      const index = users.findIndex(u => u.id === updatedUser.id);
      if (index !== -1) {
        const updated = [...users];
        updated[index] = updatedUser;
        // Reordenar para manter ADMINs no topo
        return updated.sort((a, b) => {
          if (a.role === 'ADMIN' && b.role !== 'ADMIN') return -1;
          if (a.role !== 'ADMIN' && b.role === 'ADMIN') return 1;
          return a.name.localeCompare(b.name);
        });
      }
      return users;
    });
    this.closeEditModal();
    this.success.set(`Usuário ${updatedUser.name} foi atualizado com sucesso.`);
    this.error.set(null);
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
        this.success.set(`Usuário ${user.name} foi excluído com sucesso.`);
        this.error.set(null);
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

