import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';

declare global {
  interface Window {
    dataLayer: any[];
    gtag: (...args: any[]) => void;
  }
}

@Injectable({
  providedIn: 'root'
})
export class AnalyticsService {
  private initialized = false;

  constructor() {
    this.initializeGa4();
  }

  private initializeGa4(): void {
    if (this.initialized || !environment.ga4MeasurementId || environment.ga4MeasurementId === 'YOUR_GA4_ID') {
      return;
    }

    // Inject the GA4 script
    const script = document.createElement('script');
    script.async = true;
    script.src = `https://www.googletagmanager.com/gtag/js?id=${environment.ga4MeasurementId}`;
    document.head.appendChild(script);

    // Initialize dataLayer and gtag
    window.dataLayer = window.dataLayer || [];
    window.gtag = function() {
      window.dataLayer.push(arguments);
    };

    window.gtag('js', new Date());
    window.gtag('config', environment.ga4MeasurementId, {
      send_page_view: false // We'll manually track page views if needed, or let default handling work dependent on router
    });

    this.initialized = true;
    console.log('GA4 Initialized with ID:', environment.ga4MeasurementId);
  }

  /**
   * Identifies the current user/author in GA4.
   * This sets user_properties that will be associated with future events.
   * @param authorId The ID of the logged-in author
   */
  setAuthorId(authorId: string): void {
    if (!this.initialized) return;

    // Set user_id for cross-device tracking (if enabled in GA4)
    window.gtag('config', environment.ga4MeasurementId, {
      user_id: authorId
    });

    // Set stable user_properties for filtering in BigQuery/Looker
    window.gtag('set', 'user_properties', {
      author_id: authorId,
      crm_id: authorId // Redundant backup key often used in templates
    });
    
    console.log('Analytics identified author:', authorId);
  }

  /**
   * Logs a specific event to GA4.
   * @param eventName The name of the event (e.g., 'view_report', 'export_pdf')
   * @param params Optional parameters to attach to the event
   */
  logEvent(eventName: string, params: Record<string, any> = {}): void {
    if (!this.initialized) return;
    window.gtag('event', eventName, params);
  }
}
