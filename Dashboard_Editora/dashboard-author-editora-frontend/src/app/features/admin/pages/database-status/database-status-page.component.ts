import { Component, signal, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';

interface DatabaseStatus {
  status: string;
  version?: string;
  connections?: number;
  uptime?: string;
}

@Component({
  selector: 'app-database-status-page',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './database-status-page.component.html',
  styleUrl: './database-status-page.component.scss'
})
export class DatabaseStatusPageComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly API_URL = 'https://www.dashboard-author-editora.vercel.app';

  readonly status = signal<DatabaseStatus | null>(null);
  readonly loading = signal<boolean>(false);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.loadStatus();
  }

  loadStatus(): void {
    this.loading.set(true);
    this.http.get<DatabaseStatus>(`${this.API_URL}/api/admin/database/status`).subscribe({
      next: (status) => {
        this.status.set(status);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set('Erro ao carregar status do banco de dados.');
        this.loading.set(false);
      }
    });
  }
}

