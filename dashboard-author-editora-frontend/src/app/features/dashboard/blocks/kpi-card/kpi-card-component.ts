import { Component, input, signal } from '@angular/core';
import { CommonModule } from '@angular/common'; // <- traz NgIf, NgSwitch, DecimalPipe
import { MockDataService, KpiKey } from '../../data/mock-data.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-kpi-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './kpi-card-component.html',
})
export class KpiCardComponent {
  title = input<string>('KPI');
  kpiKey = input<KpiKey>('revenue');

  value = signal<number>(0);
  change = signal<number>(0);
  pct = signal<number>(0);

  private sub?: Subscription;

  constructor(private mock: MockDataService) {}

  ngOnInit() {
    this.sub = this.mock.kpi$(this.kpiKey()).subscribe(s => {
      this.value.set(s.value);
      this.change.set(s.change);
      this.pct.set(Number(s.pct.toFixed(1)));
    });
  }

  ngOnDestroy() {
    this.sub?.unsubscribe();
  }

  get isUp() { return this.change() >= 0; }
}
