import { Component, signal, inject, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { environment } from '@/environments/environment';

interface Admin {
  id: string;
  name: string;
  email: string;
  createdAt: string;
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

  readonly admins = signal<Admin[]>([]);
  readonly loading = signal<boolean>(false);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.loadAdmins();
  }

  loadAdmins(): void {
    this.loading.set(true);
    this.http.get<Admin[]>(`${this.API_URL}/api/admin/admin-info`).subscribe({
      next: (admins) => {
        // Garante que sempre seja um array
        this.admins.set(Array.isArray(admins) ? admins : []);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set('Erro ao carregar administradores.');
        this.loading.set(false);
        this.admins.set([]); // Garante array vazio em caso de erro
      }
    });
  }
}

