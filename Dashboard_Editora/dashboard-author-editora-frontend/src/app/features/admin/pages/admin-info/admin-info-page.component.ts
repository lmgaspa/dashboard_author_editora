import { Component, signal, inject, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';

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
  styleUrl: './admin-info-page.component.scss'
})
export class AdminInfoPageComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly API_URL = 'https://www.dashboard-author-editora.vercel.app';

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
        this.admins.set(admins);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set('Erro ao carregar administradores.');
        this.loading.set(false);
      }
    });
  }
}

