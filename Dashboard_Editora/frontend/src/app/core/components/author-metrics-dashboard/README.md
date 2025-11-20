# AuthorMetricsDashboardComponent

Componente Angular standalone para embutir relatórios do Looker Studio por autor.

## Uso

```html
<!-- Exemplo básico -->
<app-author-metrics-dashboard [authorId]="1"></app-author-metrics-dashboard>

<!-- Com container customizado -->
<div class="w-full h-[600px] rounded-lg overflow-hidden">
  <app-author-metrics-dashboard [authorId]="1"></app-author-metrics-dashboard>
</div>
```

## Configuração de Novos Autores

Para adicionar um novo autor ao componente:

1. Abra `author-metrics-dashboard.component.ts`
2. Localize o `MAP_AUTHOR_LOOKER_URLS`
3. Adicione uma nova entrada:

```typescript
private static readonly MAP_AUTHOR_LOOKER_URLS: ReadonlyMap<number, string> = new Map([
  [1, 'https://lookerstudio.google.com/embed/reporting/6286ad72-e690-4009-981e-afa5189fc88b/page/flffF'],
  [2, 'https://lookerstudio.google.com/embed/reporting/SEU_NOVO_ID_AQUI/page/SUA_PAGE_ID_AQUI']
]);
```

## Características

- ✅ Usa `DomSanitizer` para segurança
- ✅ Map/Strategy pattern para fácil extensão
- ✅ TypeScript com tipagem forte
- ✅ Componente standalone (sem módulo necessário)
- ✅ Mensagem de erro amigável quando authorId não está configurado
- ✅ HTML do iframe mantém os atributos originais do Looker Studio

## Imports Necessários

O componente já importa tudo necessário. Apenas importe o componente onde for usar:

```typescript
import { AuthorMetricsDashboardComponent } from '@/app/core/components/author-metrics-dashboard/author-metrics-dashboard.component';

@Component({
  // ...
  imports: [AuthorMetricsDashboardComponent]
})
```

