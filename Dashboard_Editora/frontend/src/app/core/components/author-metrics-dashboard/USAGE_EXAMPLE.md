# Exemplo de Uso - AuthorMetricsDashboardComponent

## Exemplo 1: Uso Básico

```html
<!-- Em qualquer template Angular -->
<app-author-metrics-dashboard [authorId]="1"></app-author-metrics-dashboard>
```

## Exemplo 2: Com Container Customizado

```html
<div class="w-full space-y-4">
  <h1>Dashboard de Métricas</h1>
  
  <div class="bg-white/5 backdrop-blur-xl border border-white/10 rounded-xl p-4">
    <div class="w-full h-[600px] rounded-lg overflow-hidden">
      <app-author-metrics-dashboard [authorId]="1"></app-author-metrics-dashboard>
    </div>
  </div>
</div>
```

## Exemplo 3: Com Variável Dinâmica

```typescript
// No componente TypeScript
export class MyComponent {
  readonly currentAuthorId = signal<number>(1);
  
  changeAuthor(newId: number): void {
    this.currentAuthorId.set(newId);
  }
}
```

```html
<!-- No template -->
<app-author-metrics-dashboard [authorId]="currentAuthorId()"></app-author-metrics-dashboard>
```

## Exemplo 4: Integração Completa em uma Página

```typescript
// exemplo-uso.component.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthorMetricsDashboardComponent } from '@/app/core/components/author-metrics-dashboard/author-metrics-dashboard.component';

@Component({
  selector: 'app-exemplo-uso',
  standalone: true,
  imports: [CommonModule, AuthorMetricsDashboardComponent],
  template: `
    <div class="w-full space-y-6 p-6">
      <div>
        <h1 class="text-2xl font-bold text-white mb-2">Métricas do Autor</h1>
        <p class="text-gray-400">Visualize as métricas do Looker Studio</p>
      </div>
      
      <div class="bg-white/5 backdrop-blur-xl border border-white/10 rounded-xl p-6">
        <div class="w-full h-[700px] rounded-lg overflow-hidden">
          <app-author-metrics-dashboard [authorId]="1"></app-author-metrics-dashboard>
        </div>
      </div>
    </div>
  `
})
export class ExemploUsoComponent {
  // Componente pronto para uso
}
```

## Exemplo 5: Com Responsividade

```html
<div class="w-full">
  <div class="bg-white/5 backdrop-blur-xl border border-white/10 rounded-xl p-4 sm:p-6">
    <div class="w-full h-[450px] sm:h-[600px] md:h-[700px] lg:h-[800px] rounded-lg overflow-hidden">
      <app-author-metrics-dashboard [authorId]="1"></app-author-metrics-dashboard>
    </div>
  </div>
</div>
```

## Importação no Componente

```typescript
import { Component } from '@angular/core';
import { AuthorMetricsDashboardComponent } from '@/app/core/components/author-metrics-dashboard/author-metrics-dashboard.component';

@Component({
  selector: 'app-meu-componente',
  standalone: true,
  imports: [AuthorMetricsDashboardComponent], // Adicione aqui
  // ...
})
export class MeuComponente {
  // ...
}
```

