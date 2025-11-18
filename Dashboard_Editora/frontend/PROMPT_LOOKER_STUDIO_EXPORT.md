# 🚀 Prompt: Integração Looker Studio com RLS + Exportação PDF/CSV

## Objetivo
Implementar integração híbrida de Looker Studio (RLS + parâmetros) e adicionar funcionalidades de exportação em PDF/CSV para as páginas de Métricas, Emails e Pagamentos.

---

## 📋 Parte 1: Integração Looker Studio com Segurança (RLS) e UX (Parâmetros)

### 1.1 Configuração da Página de Métricas

**Requisitos:**
- Usar um único relatório Looker Studio master (configurado no backend via `lookerStudioUrl` do usuário)
- Para usuários (USER): embedar iframe já com parâmetro `author_id` na URL (deep link)
- Para admins (ADMIN): permitir seleção de autor via dropdown + atualizar iframe dinamicamente
- Suportar fallback quando `lookerStudioUrl` não estiver configurado (mostrar mensagem amigável)

**Componente:** `src/app/features/user/pages/metrics/metrics-page.component.ts` e `.html`

**Lógica:**
```typescript
// 1. Buscar lookerStudioUrl do perfil do usuário (já existe no AuthService)
// 2. Se USER: extrair authorId do perfil e montar URL com parâmetro:
//    const url = `${lookerStudioUrl}?params.author_id_param=${authorId}`
// 3. Se ADMIN: mostrar dropdown de autores + atualizar URL ao mudar seleção
// 4. Validar URL antes de embedar (deve começar com https://lookerstudio.google.com/embed/)
// 5. Usar DomSanitizer para marcar URL como segura (SafeResourceUrl)
```

**Interface sugerida:**
- Se USER sem authorId: mostrar mensagem "Entre em contato com o administrador para configurar suas métricas"
- Se USER com authorId: iframe diretamente com parâmetro (sem controles visíveis)
- Se ADMIN: dropdown "Selecione o autor" acima do iframe + botão "Ver métricas do autor selecionado"
- Loading state enquanto carrega o iframe
- Error handling se iframe falhar ao carregar

---

## 📋 Parte 2: Exportação PDF/CSV

### 2.1 Exportação na Página de Métricas

**Funcionalidade:**
- Botão "Exportar Métricas" com dropdown: "Exportar como PDF" e "Exportar como CSV"
- PDF: capturar/gerar screenshot do iframe do Looker Studio ou gerar relatório PDF customizado
- CSV: extrair dados do Looker Studio via API (se disponível) ou gerar CSV com dados resumidos do backend

**Componente:** `src/app/features/user/pages/metrics/metrics-page.component.ts`

**Lógica:**
```typescript
// PDF:
// Opção 1: Usar Looker Studio Embed API para exportar PDF (se disponível)
// Opção 2: Gerar PDF customizado usando jsPDF ou similar com dados resumidos
// Opção 3: Abrir Looker Studio em nova aba e usar print-to-PDF do navegador

// CSV:
// Opção 1: Chamar endpoint backend que retorna CSV de métricas por authorId
// Opção 2: Extrair dados do Looker Studio via Embed API (se disponível)
// Opção 3: Gerar CSV client-side com dados disponíveis no componente
```

**Endpoints backend sugeridos:**
- `GET /api/v1/metrics/export?format=pdf&authorId={authorId}`
- `GET /api/v1/metrics/export?format=csv&authorId={authorId}`

**UI:**
- Botão com ícone de download no canto superior direito da página de métricas
- Dropdown com opções: "PDF" e "CSV"
- Toast/notificação de sucesso/erro após exportar

---

### 2.2 Exportação na Página de Emails

**Funcionalidade:**
- Botão "Exportar Emails" com dropdown: "Exportar como PDF" e "Exportar como CSV"
- Exportar lista de emails enviados com filtros aplicados (data, status, etc.)

**Componente:** `src/app/features/user/pages/emails/emails-page.component.ts`

**Lógica:**
```typescript
// PDF:
// Usar jsPDF ou pdfmake para gerar PDF com tabela de emails:
// - Colunas: Data, Destinatário, Assunto, Status, Tipo
// - Aplicar filtros da página (dateRange, status, etc.)
// - Incluir cabeçalho com nome do autor/data de exportação

// CSV:
// Gerar CSV com mesmas colunas do PDF
// Usar biblioteca como papaparse ou gerar manualmente
```

**Endpoints backend sugeridos:**
- `GET /api/v1/emails/export?format=pdf&authorId={authorId}&startDate={}&endDate={}&status={}`
- `GET /api/v1/emails/export?format=csv&authorId={authorId}&startDate={}&endDate={}&status={}`

**UI:**
- Botão "Exportar" ao lado dos filtros na página de emails
- Dropdown: "PDF" e "CSV"
- Respeitar filtros ativos (data, status) na exportação
- Indicador de progresso durante exportação (se for assíncrono)

---

### 2.3 Exportação na Página de Pagamentos

**Funcionalidade:**
- Botão "Exportar Pagamentos" com dropdown: "Exportar como PDF" e "Exportar como CSV"
- Exportar lista de pagamentos com todos os detalhes (resumo + detalhes)

**Componente:** `src/app/features/user/pages/payments/payments-page.component.ts`

**Lógica:**
```typescript
// PDF:
// Gerar PDF com duas seções:
// 1. Resumo: total recebido, pendente, etc.
// 2. Detalhes: tabela com todos os pagamentos
// Usar jsPDF ou pdfmake

// CSV:
// Gerar CSV com linha de resumo (se necessário) + linhas de detalhes
// Colunas: Data, Valor, Status, Método de Pagamento, Descrição, etc.
```

**Endpoints backend sugeridos:**
- `GET /api/v1/payments/export?format=pdf&authorId={authorId}&startDate={}&endDate={}&status={}`
- `GET /api/v1/payments/export?format=csv&authorId={authorId}&startDate={}&endDate={}&status={}`

**UI:**
- Botão "Exportar" ao lado dos filtros/controles na página de pagamentos
- Dropdown: "PDF" e "CSV"
- Respeitar filtros ativos (data, status, método de pagamento) na exportação
- Toast de confirmação após exportar

---

## 📦 Bibliotecas Sugeridas

### Para PDF:
- **jsPDF** (https://github.com/parallax/jsPDF) - Geração de PDF client-side
- **pdfmake** (https://pdfmake.github.io/docs/) - Mais recursos, mas maior bundle size
- **html2pdf.js** - Converter HTML para PDF (útil se já tem templates HTML)

### Para CSV:
- **papaparse** (https://www.papaparse.com/) - Parsing e geração de CSV
- Ou gerar manualmente usando `Blob` e `URL.createObjectURL`

### Para Looker Studio Embed:
- **@looker/embed-sdk** (opcional, se disponível) - SDK oficial do Looker
- Ou usar iframe direto (como já está implementado)

---

## 🔒 Segurança e Validação

1. **Validação de URL do Looker Studio:**
   - Verificar se começa com `https://lookerstudio.google.com/embed/`
   - Usar `DomSanitizer.bypassSecurityTrustResourceUrl()` para URLs válidas

2. **Autorização de exportação:**
   - USER: só pode exportar seus próprios dados (authorId do token)
   - ADMIN: pode exportar dados de qualquer autor (se fornecer authorId)

3. **Rate limiting:**
   - Backend deve implementar rate limiting nas rotas de exportação
   - Frontend pode desabilitar botão de exportação temporariamente após uso

4. **Sanitização de dados:**
   - CSV/PDF devem sanitizar dados sensíveis (se houver)
   - Não incluir tokens ou credenciais nos arquivos exportados

---

## 📱 UX/UI Guidelines

1. **Consistência:**
   - Mesmo padrão de botão "Exportar" em todas as páginas (Métricas, Emails, Pagamentos)
   - Ícone de download (📥 ou similar) + dropdown com opções
   - Posição: canto superior direito ou ao lado dos filtros

2. **Feedback visual:**
   - Loading spinner durante geração de exportação
   - Toast de sucesso: "Arquivo exportado com sucesso! Verifique sua pasta de downloads"
   - Toast de erro: "Erro ao exportar. Tente novamente ou entre em contato com suporte"

3. **Nomenclatura de arquivos:**
   - PDF: `metricas-{authorId}-{YYYY-MM-DD}.pdf`
   - CSV: `emails-{authorId}-{YYYY-MM-DD}.csv`
   - Pagamentos: `pagamentos-{authorId}-{YYYY-MM-DD}.csv`

4. **Acessibilidade:**
   - Labels descritivos nos botões
   - Suporte a navegação por teclado
   - Mensagens de erro claras

---

## 🧪 Casos de Teste

1. **Métricas:**
   - [ ] USER sem authorId vê mensagem amigável
   - [ ] USER com authorId vê iframe com parâmetro correto
   - [ ] ADMIN pode selecionar autor e atualizar iframe
   - [ ] Exportação PDF funciona (screenshot ou PDF customizado)
   - [ ] Exportação CSV funciona
   - [ ] URL inválida do Looker Studio mostra erro

2. **Emails:**
   - [ ] Exportação PDF inclui todos os emails com filtros aplicados
   - [ ] Exportação CSV tem formato correto e pode ser aberto no Excel
   - [ ] Filtros (data, status) são respeitados na exportação

3. **Pagamentos:**
   - [ ] Exportação PDF inclui resumo + detalhes
   - [ ] Exportação CSV tem todas as colunas necessárias
   - [ ] Filtros são respeitados na exportação

---

## 📝 Checklist de Implementação

### Fase 1: Looker Studio com RLS + Parâmetros
- [ ] Atualizar `metrics-page.component.ts` para buscar `lookerStudioUrl` do perfil
- [ ] Implementar lógica de parâmetro `author_id_param` na URL para USER
- [ ] Implementar dropdown de seleção de autor para ADMIN
- [ ] Adicionar validação de URL do Looker Studio
- [ ] Testar embed com diferentes authorIds

### Fase 2: Exportação Métricas
- [ ] Adicionar botão "Exportar Métricas" com dropdown
- [ ] Implementar exportação PDF (screenshot ou PDF customizado)
- [ ] Implementar exportação CSV (via backend ou client-side)
- [ ] Adicionar endpoints backend se necessário
- [ ] Testar exportação com diferentes authorIds

### Fase 3: Exportação Emails
- [ ] Adicionar botão "Exportar Emails" com dropdown
- [ ] Implementar exportação PDF com jsPDF/pdfmake
- [ ] Implementar exportação CSV
- [ ] Adicionar endpoints backend se necessário
- [ ] Testar exportação com filtros aplicados

### Fase 4: Exportação Pagamentos
- [ ] Adicionar botão "Exportar Pagamentos" com dropdown
- [ ] Implementar exportação PDF (resumo + detalhes)
- [ ] Implementar exportação CSV
- [ ] Adicionar endpoints backend se necessário
- [ ] Testar exportação com filtros aplicados

### Fase 5: Polimento
- [ ] Adicionar loading states em todas as exportações
- [ ] Adicionar toasts de sucesso/erro
- [ ] Testar acessibilidade (teclado, leitores de tela)
- [ ] Revisar nomenclatura de arquivos exportados
- [ ] Documentar código e adicionar comentários

---

## 🔗 Referências

- Looker Studio Embed: https://lookerstudio.google.com/embed
- jsPDF: https://github.com/parallax/jsPDF
- pdfmake: https://pdfmake.github.io/docs/
- papaparse: https://www.papaparse.com/
- Angular DomSanitizer: https://angular.io/api/platform-browser/DomSanitizer

---

## 💡 Notas Importantes

1. **Looker Studio Embed API:** Verificar se há SDK oficial disponível ou usar iframe direto (atual implementação)

2. **Performance:** Exportação de grandes volumes de dados pode ser lenta; considerar paginação ou processamento assíncrono no backend

3. **Compatibilidade:** Testar exportação PDF/CSV em diferentes navegadores (Chrome, Firefox, Safari, Edge)

4. **Segurança:** Nunca incluir tokens JWT ou credenciais nos arquivos exportados

5. **Backend:** Se backend não tiver endpoints de exportação, pode ser necessário criar ou fazer exportação client-side (menos ideal para grandes volumes)

