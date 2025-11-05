import { Component, signal, inject, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';

interface User {
  id: string;
  name: string;
  email: string;
  role: string;
  createdAt: string;
}

@Component({
  selector: 'app-users-page',
  standalone: true,
  imports: [CommonModule, RouterModule, DatePipe],
  templateUrl: './users-page.component.html',
  styleUrl: './users-page.component.scss'
})
export class UsersPageComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly API_URL = 'https://www.dashboard-author-editora.vercel.app';

  readonly users = signal<User[]>([]);
  readonly loading = signal<boolean>(false);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading.set(true);
    this.http.get<User[]>(`${this.API_URL}/api/admin/users`).subscribe({
      next: (users) => {
        this.users.set(users);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set('Erro ao carregar usuários.');
        this.loading.set(false);
      }
    });
  }
}

