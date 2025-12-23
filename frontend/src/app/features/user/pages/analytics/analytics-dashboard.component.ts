import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { AuthService } from '../../../../core/services/auth.service';
import { AnalyticsService } from '../../../../core/services/analytics.service';
import { environment } from '../../../../../environments/environment';

@Component({
  selector: 'app-analytics-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="container mx-auto px-4 py-8">
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900 mb-2">Relatórios e Métricas</h1>
        <p class="text-gray-600">Acompanhe o desempenho das suas vendas e acessos em tempo real.</p>
      </div>

      <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden" style="min-height: 800px;">
        <div *ngIf="loading" class="flex items-center justify-center h-96">
          <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
        </div>
        
        <iframe 
          *ngIf="iframeUrl"
          [src]="iframeUrl" 
          width="100%" 
          height="800" 
          frameborder="0" 
          style="border:0" 
          allowfullscreen
          (load)="onIframeLoad()">
        </iframe>

        <div *ngIf="!iframeUrl && !loading" class="flex flex-col items-center justify-center h-96 text-center p-8">
          <span class="material-icons text-4xl text-gray-400 mb-4">analytics</span>
          <h3 class="text-lg font-medium text-gray-900">Relatório Indisponível</h3>
          <p class="text-gray-500 mt-2">Não foi possível carregar o painel de métricas. Entre em contato com o suporte se o problema persistir.</p>
        </div>
      </div>
    </div>
  `
})
export class AnalyticsDashboardComponent implements OnInit {
  private sanitizer = inject(DomSanitizer);
  private authService = inject(AuthService);
  private analyticsService = inject(AnalyticsService);

  iframeUrl: SafeResourceUrl | null = null;
  loading = true;

  ngOnInit() {
    this.setupDashboard();
  }

  setupDashboard() {
    const user = this.authService.currentUser();
    
    if (!user || (!user.authorId && !user.id)) {
      this.loading = false;
      return;
    }

    const authorId = user.authorId || user.id; // Fallback to user ID if authorId is missing
    
    // 1. Identify user in GA4 for future events
    this.analyticsService.setAuthorId(authorId);
    
    // 2. Generate Looker Studio URL with parameters
    // Parameter Format: params={"ds0.author_id":"123"}
    // This allows the report to filter data based on the logged-in user.
    if (environment.lookerStudioUrl && environment.lookerStudioUrl !== 'YOUR_LOOKER_URL') {
      const params = {
        'ds0.author_id': authorId
      };
      
      const paramsJson = JSON.stringify(params);
      const encodedParams = encodeURIComponent(paramsJson);
      const fullUrl = `${environment.lookerStudioUrl}?params=${encodedParams}`;
      
      this.iframeUrl = this.sanitizer.bypassSecurityTrustResourceUrl(fullUrl);
    } else {
      console.warn('Looker Studio URL is not configured in environment.');
    }
  }

  onIframeLoad() {
    this.loading = false;
  }
}
