# Dashboard do Autor - Editora

Dashboard web para gerenciamento de autores, pedidos, pagamentos, entregas e métricas desenvolvido em Angular.

## 📋 Sobre o Projeto

Sistema completo de gestão para autores de uma editora, permitindo visualização de métricas, gerenciamento de pedidos, controle de entregas, acompanhamento de pagamentos e comunicação com clientes através de e-mails.

## 🚀 Tecnologias

- **Angular** 20.3.0
- **TypeScript** 5.9.2
- **Tailwind CSS** 3.4.17
- **RxJS** 7.8.0
- **ECharts** 6.0.0 (gráficos e visualizações)
- **Angular Signals** (gerenciamento de estado reativo)

## 📁 Estrutura do Projeto

```
src/
├── app/
│   ├── core/                    # Funcionalidades core compartilhadas
│   │   ├── components/          # Componentes reutilizáveis
│   │   │   ├── author-metrics-dashboard/  # Dashboard Looker Studio
│   │   │   └── export-buttons/            # Botões de exportação
│   │   ├── guards/              # Guards de rota (auth, role)
│   │   ├── interceptors/       # Interceptors HTTP (auth)
│   │   ├── models/             # Interfaces e tipos TypeScript
│   │   ├── services/           # Serviços compartilhados
│   │   └── utils/              # Utilitários
│   ├── features/               # Módulos de funcionalidades
│   │   ├── admin/              # Funcionalidades administrativas
│   │   ├── auth/               # Autenticação e autorização
│   │   ├── dashboard/           # Dashboard com gráficos
│   │   └── user/               # Funcionalidades do usuário
│   └── layout/                 # Componentes de layout
│       ├── components/         # Sidebar, Topbar, Footer
│       └── layouts/            # Layouts (admin, user, public)
└── environments/               # Configurações de ambiente
```

## ✨ Funcionalidades Principais

### 🔐 Autenticação e Autorização
- Login com JWT
- Recuperação de senha
- Confirmação de conta por e-mail
- Guards de rota por role (USER, ADMIN)
- Interceptor HTTP para adicionar token automaticamente

### 👤 Gestão de Usuários (Admin)
- Criação e edição de usuários
- Gerenciamento de roles e permissões
- Visualização de lista de administradores
- Configuração de `author_id` e URLs do Looker Studio

### 💰 Pagamentos
- Visualização de resumo de pagamentos
- Funil de vendas (Total, Confirmados, Desistências)
- Lista de pedidos recentes com valores reais (após taxas)
- Exportação em PDF/CSV

### 📧 E-mails
- Visualização de e-mails de clientes
- E-mails de repasse (PIX/Card)
- Informações de cupons aplicados
- Estatísticas de pedidos por cliente
- Exportação em PDF/CSV

### 📊 Métricas
- Dashboard integrado com Looker Studio
- Visualização de métricas por autor
- Suporte multi-tenant (cada autor vê apenas suas métricas)
- Seleção de autor para administradores

### 🚚 Entregas
- Lista de entregas ativas
- Status de envio (AGUARDANDO, ENVIADO, RECUSADO, ENTREGUE)
- Atualização de código de rastreamento
- Modal para atualização de status
- Filtro entre entregas ativas e arquivadas

### 📦 Pedidos Arquivados
- Visualização de pedidos com status ENTREGUE
- Mesma interface de entregas, filtrada para arquivados
- Exportação específica de pedidos arquivados

### 🎫 Tickets
- Criação de tickets de suporte
- Visualização de tickets abertos e fechados
- Detalhes de cada ticket
- Exportação em PDF/CSV

### 💳 Cobranças Mensais
- Visualização de cobranças mensais
- Códigos PIX para pagamento
- Status de pagamento (PENDING, PAID, OVERDUE, CANCELLED)
- Histórico de cobranças
- Exportação em PDF/CSV

### 📈 Dashboard
- KPIs principais (Receita, Pedidos, Cancelados, Status de Entrega)
- Mapa do Brasil com distribuição de vendas
- Funil de vendas
- Métricas de formas de pagamento
- Produtos mais vendidos

## 🔧 Configuração

### Pré-requisitos
- Node.js 18+ 
- npm ou yarn

### Instalação

1. Clone o repositório:
```bash
git clone <repository-url>
cd frontend
```

2. Instale as dependências:
```bash
npm install
```

3. Configure as variáveis de ambiente:
```typescript
// src/environments/environment.ts
export const environment = {
  production: false,
  apiUrl: 'https://dashboard-painel-autores-vl-26b242c59563.herokuapp.com',
  backendPort: 8000,
};
```

4. Inicie o servidor de desenvolvimento:
```bash
npm start
# ou
ng serve
```

5. Acesse `http://localhost:4200`

## 🏗️ Arquitetura

### Multi-Tenancy
O sistema implementa isolamento multi-tenant baseado em `author_id`:
- Cada autor vê apenas seus próprios dados
- O `author_id` é extraído automaticamente do token JWT
- Não é necessário passar `author_id` como parâmetro nas requisições
- Backend garante isolamento completo dos dados

### Padrões de Design
- **Open/Closed Principle (OCP)**: Componentes extensíveis sem modificação
- **Single Responsibility**: Cada serviço/componente tem uma responsabilidade única
- **Dependency Injection**: Uso extensivo de `inject()` do Angular
- **Signals**: Estado reativo com Angular Signals
- **Strategy Pattern**: Mapeamento de URLs do Looker Studio por `authorId`

### Serviços Principais

#### AuthService
- Gerenciamento de autenticação
- Armazenamento de token JWT
- Gerenciamento de perfil do usuário
- Métodos de login, logout, recuperação de senha

#### EmailService
- Busca de e-mails de clientes e repasse
- Agregação de informações de cupons
- Painel completo de e-mails

#### PaymentService
- Resumo de pagamentos
- Funil de vendas
- Pedidos recentes

#### EntregaService
- Lista de entregas
- Atualização de status de envio
- Cache com `shareReplay`

#### ExportService
- Exportação em PDF/CSV
- Suporte para múltiplos módulos (emails, entregas, cobranças, tickets, métricas)
- Geração automática de nomes de arquivo

#### MonthlyChargeService
- Gestão de cobranças mensais
- Códigos PIX
- Status de pagamento

#### TicketService
- Criação e listagem de tickets
- Detalhes de tickets

## 🎨 Design System

### Tema
- Design dark mode com gradientes
- Cores principais: Sky Blue (#38bdf8, #2563eb)
- Efeitos de blur e glassmorphism
- Responsivo (mobile-first)

### Componentes Reutilizáveis
- **ExportButtonsComponent**: Botões de exportação com dropdown (PDF/CSV)
- **AuthorMetricsDashboardComponent**: Embed do Looker Studio por autor
- Cards, modais, tabelas padronizadas

## 📤 Exportação

O sistema suporta exportação em múltiplos formatos:
- **PDF**: Relatórios formatados
- **CSV**: Dados tabulares
- **JSON**: Dados estruturados (alguns endpoints)

Módulos com exportação:
- E-mails
- Entregas (ativas e arquivadas)
- Cobranças
- Tickets
- Métricas

## 🔒 Segurança

- Autenticação JWT
- Interceptor HTTP para adicionar token automaticamente
- Guards de rota para proteger rotas autenticadas
- Validação de roles (USER, ADMIN)
- Sanitização de URLs para iframes (DomSanitizer)
- Isolamento multi-tenant garantido pelo backend

## 📱 Responsividade

- Design mobile-first
- Breakpoints Tailwind (sm, md, lg, xl)
- Tabelas responsivas (desktop) / Cards (mobile)
- Modais adaptáveis
- Navegação otimizada para mobile

## 🚀 Build para Produção

```bash
ng build --configuration=production
```

Os arquivos compilados estarão em `dist/dashboard-author-editora-frontend/browser/`

## 🧪 Testes

```bash
ng test
```

## 📝 Scripts Disponíveis

- `npm start` / `ng serve`: Servidor de desenvolvimento
- `npm run build`: Build de produção
- `npm test`: Executar testes
- `ng generate component`: Gerar novo componente

## 🔗 API Backend

**URL Base**: `https://dashboard-painel-autores-vl-26b242c59563.herokuapp.com`

### Endpoints Principais

- `/api/v1/auth/*` - Autenticação
- `/api/v1/author/*` - Endpoints do autor
- `/api/v1/admin/*` - Endpoints administrativos
- `/api/v1/entregas/*` - Entregas
- `/api/v1/cobrancas/*` - Cobranças
- `/api/v1/tickets/*` - Tickets
- `/api/v1/emails/*` - E-mails
- `/api/v1/*/export` - Exportações

## 📚 Recursos Adicionais

- [Angular Documentation](https://angular.dev)
- [Tailwind CSS](https://tailwindcss.com)
- [ECharts](https://echarts.apache.org)
- [RxJS](https://rxjs.dev)

## 👥 Contribuição

Este é um projeto privado da editora. Para contribuições, entre em contato com a equipe de desenvolvimento.

## 📄 Licença

Proprietário - Todos os direitos reservados

---

**Desenvolvido com ❤️ para a Editora**
