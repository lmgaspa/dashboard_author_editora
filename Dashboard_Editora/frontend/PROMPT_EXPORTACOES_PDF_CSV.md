# 📄 Prompt: Sistema de Exportação PDF e CSV

## 🎯 Objetivo

Implementar funcionalidade de exportação em PDF e CSV para os seguintes módulos:
- **Entregas** (pedidos arquivados)
- **Cobranças**
- **Métricas/Estatísticas**
- **Tickets**

---

## 🔌 Endpoints de Exportação

### Base URL
```
https://dashboard-painel-autores-vl-26b242c59563.herokuapp.com/api/v1
```

### 1. Exportação de Entregas

**Endpoint:** `GET /api/v1/entregas/export`

**Parâmetros:**
- `format` (opcional): `pdf`, `csv` ou `json` (padrão: `json`)
- `author_id` (opcional): ID do autor (usa o do usuário logado se não fornecido)

**Exemplos:**
```typescript
// Exportar PDF
GET /api/v1/entregas/export?format=pdf

// Exportar CSV
GET /api/v1/entregas/export?format=csv

// Retornar JSON (padrão)
GET /api/v1/entregas/export
```

---

### 1.1. Exportação de Pedidos Arquivados (Apenas ENTREGUE)

**Endpoint:** `GET /api/v1/entregas/export/arquivados`

**Parâmetros:**
- `format` (opcional): `pdf`, `csv` ou `json` (padrão: `json`)
- `author_id` (opcional): ID do autor (usa o do usuário logado se não fornecido)

**Descrição:**
Este endpoint exporta **apenas** pedidos arquivados, ou seja, entregas com status `ENTREGUE`. É o endpoint ideal para exportar o histórico de entregas finalizadas.

**Exemplos:**
```typescript
// Exportar PDF de pedidos arquivados
GET /api/v1/entregas/export/arquivados?format=pdf

// Exportar CSV de pedidos arquivados
GET /api/v1/entregas/export/arquivados?format=csv

// Retornar JSON de pedidos arquivados (padrão)
GET /api/v1/entregas/export/arquivados
```

**Resposta (PDF/CSV):**
- Content-Type: `application/pdf` (PDF) ou `text/plain` (CSV)
- Content-Disposition: `attachment; filename="pedidos_arquivados_{authorId}.pdf"` ou `.csv`
- Body: bytes do arquivo contendo apenas entregas com status `ENTREGUE`

**Resposta (JSON):**
Mesma estrutura do endpoint `/export`, mas contendo apenas entregas com `statusEnvio: "ENTREGUE"`.

**Resposta (PDF/CSV):**
- Content-Type: `application/pdf` (PDF) ou `text/plain` (CSV)
- Content-Disposition: `attachment; filename="entregas_{authorId}.pdf"` ou `.csv`
- Body: bytes do arquivo

**Resposta (JSON):**
```json
[
  {
    "pedidoId": 1009,
    "dataPedido": "2025-11-18T10:30:00Z",
    "valorTotal": 45.00,
    "statusPedido": "CONFIRMED",
    "nomeCompleto": "Irene Cazorla",
    "email": "icazorla@uol.com.br",
    "telefone": "(73)99177-9913",
    "cpf": "123.456.789-00",
    "rua": "Rua Zildo Pedro Guimarães Júnior",
    "numero": "201",
    "complemento": "Apto 202",
    "bairro": "Zildolândia",
    "cidade": "Itabuna",
    "estado": "BA",
    "cep": "45600-730",
    "enderecoCompleto": "Rua Zildo Pedro Guimarães Júnior, 201 - Apto 202 - Zildolândia, Itabuna - BA CEP: 45600-730",
    "itens": [
      {
        "bookId": "123",
        "title": "Livro Exemplo",
        "quantity": 1,
        "price": 45.00
      }
    ],
    "enviado": false,
    "statusEnvio": "AGUARDANDO",
    "codigoRastreamento": null,
    "enviadoAt": null,
    "updatedAt": "2025-11-19T20:00:00Z"
  }
]
```

---

### 2. Exportação de Cobranças

**Endpoint:** `GET /api/v1/cobrancas/export`

**Parâmetros:**
- `format` (opcional): `pdf`, `csv` ou `json` (padrão: `json`)
- `author_id` (opcional): ID do autor (usa o do usuário logado se não fornecido)

**Exemplos:**
```typescript
// Exportar PDF
GET /api/v1/cobrancas/export?format=pdf

// Exportar CSV
GET /api/v1/cobrancas/export?format=csv
```

**Resposta (JSON):**
```json
[
  {
    "id": "uuid-here",
    "authorId": "1",
    "authorName": "Nome do Autor",
    "chargeMonth": 11,
    "chargeYear": 2025,
    "amount": 150.00,
    "dueDate": "2025-12-05",
    "chargeDate": "2025-11-01",
    "status": "PENDING",
    "paidAt": null,
    "confirmedByAdminName": null,
    "confirmedAt": null,
    "pixCode": "00020126...",
    "pixExpiresAt": "2025-11-19T23:59:59Z",
    "daysOverdue": 0,
    "hasOpenTicket": false
  }
]
```

---

### 3. Exportação de Métricas

**Endpoint:** `GET /api/v1/metricas/export`

**Parâmetros:**
- `format` (opcional): `pdf`, `csv` ou `json` (padrão: `json`)
- `author_id` (opcional): ID do autor (usa o do usuário logado se não fornecido)

**Exemplos:**
```typescript
// Exportar PDF
GET /api/v1/metricas/export?format=pdf

// Exportar CSV
GET /api/v1/metricas/export?format=csv
```

**Resposta (JSON):**
```json
{
  "authorId": 1,
  "authorName": "Nome do Autor",
  "email": "autor@example.com",
  "totalBooks": 15,
  "completedOrders": 120,
  "totalRevenue": 4500.00,
  "totalPayouts": 100,
  "totalPaid": 3500.00,
  "hasPaymentAccount": true,
  "recentOrders": 5,
  "recentRevenue": 225.00
}
```

---

### 4. Exportação de Tickets

**Endpoint:** `GET /api/v1/tickets/export`

**Parâmetros:**
- `format` (opcional): `pdf`, `csv` ou `json` (padrão: `json`)
- `author_id` (opcional): ID do autor (usa o do usuário logado se não fornecido)

**Exemplos:**
```typescript
// Exportar PDF
GET /api/v1/tickets/export?format=pdf

// Exportar CSV
GET /api/v1/tickets/export?format=csv
```

**Resposta (JSON):**
```json
[
  {
    "id": "uuid-here",
    "ticketNumber": "TKT-2025-001234",
    "title": "Dúvida sobre pagamento",
    "description": "Quando receberei o pagamento?",
    "category": "PAGAMENTO",
    "status": "OPEN",
    "createdAt": "2025-11-19T10:00:00Z",
    "updatedAt": "2025-11-19T15:30:00Z",
    "messages": [
      {
        "id": "uuid-msg",
        "sentByUserId": "user-id",
        "senderName": "Nome do Usuário",
        "message": "Mensagem do ticket",
        "isInternalNote": false,
        "createdAt": "2025-11-19T10:05:00Z",
        "readAt": null
      }
    ]
  }
]
```

---

## 🔐 Autenticação

Todos os endpoints requerem autenticação JWT:

```typescript
headers: {
  'Authorization': `Bearer ${token}`,
  'Content-Type': 'application/json'
}
```

**Permissões:**
- Usuários (`USER`) só podem exportar seus próprios dados
- Admins (`ADMIN`) podem exportar dados de qualquer autor (passando `author_id`)

---

## 💻 Implementação Frontend

### 1. Service/API Helper

```typescript
// services/export.service.ts
import axios from 'axios';

const API_BASE_URL = 'https://dashboard-painel-autores-vl-26b242c59563.herokuapp.com/api/v1';

export enum ExportFormat {
  PDF = 'pdf',
  CSV = 'csv',
  JSON = 'json'
}

export class ExportService {
  private static getAuthHeaders() {
    const token = localStorage.getItem('token');
    return {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    };
  }

  static async exportEntregas(format: ExportFormat = ExportFormat.JSON, authorId?: number) {
    const params = new URLSearchParams({ format });
    if (authorId) params.append('author_id', authorId.toString());
    
    const response = await axios.get(
      `${API_BASE_URL}/entregas/export?${params}`,
      {
        headers: this.getAuthHeaders(),
        responseType: format === ExportFormat.JSON ? 'json' : 'blob'
      }
    );
    
    if (format !== ExportFormat.JSON) {
      this.downloadFile(response.data, `entregas.${format}`, 
        format === ExportFormat.PDF ? 'application/pdf' : 'text/csv');
    }
    
    return response.data;
  }

  static async exportPedidosArquivados(format: ExportFormat = ExportFormat.JSON, authorId?: number) {
    const params = new URLSearchParams({ format });
    if (authorId) params.append('author_id', authorId.toString());
    
    const response = await axios.get(
      `${API_BASE_URL}/entregas/export/arquivados?${params}`,
      {
        headers: this.getAuthHeaders(),
        responseType: format === ExportFormat.JSON ? 'json' : 'blob'
      }
    );
    
    if (format !== ExportFormat.JSON) {
      this.downloadFile(response.data, `pedidos_arquivados.${format}`, 
        format === ExportFormat.PDF ? 'application/pdf' : 'text/csv');
    }
    
    return response.data;
  }

  static async exportCobrancas(format: ExportFormat = ExportFormat.JSON, authorId?: string) {
    const params = new URLSearchParams({ format });
    if (authorId) params.append('author_id', authorId);
    
    const response = await axios.get(
      `${API_BASE_URL}/cobrancas/export?${params}`,
      {
        headers: this.getAuthHeaders(),
        responseType: format === ExportFormat.JSON ? 'json' : 'blob'
      }
    );
    
    if (format !== ExportFormat.JSON) {
      this.downloadFile(response.data, `cobrancas.${format}`, 
        format === ExportFormat.PDF ? 'application/pdf' : 'text/csv');
    }
    
    return response.data;
  }

  static async exportMetricas(format: ExportFormat = ExportFormat.JSON, authorId?: number) {
    const params = new URLSearchParams({ format });
    if (authorId) params.append('author_id', authorId.toString());
    
    const response = await axios.get(
      `${API_BASE_URL}/metricas/export?${params}`,
      {
        headers: this.getAuthHeaders(),
        responseType: format === ExportFormat.JSON ? 'json' : 'blob'
      }
    );
    
    if (format !== ExportFormat.JSON) {
      this.downloadFile(response.data, `metricas.${format}`, 
        format === ExportFormat.PDF ? 'application/pdf' : 'text/csv');
    }
    
    return response.data;
  }

  static async exportTickets(format: ExportFormat = ExportFormat.JSON, authorId?: string) {
    const params = new URLSearchParams({ format });
    if (authorId) params.append('author_id', authorId);
    
    const response = await axios.get(
      `${API_BASE_URL}/tickets/export?${params}`,
      {
        headers: this.getAuthHeaders(),
        responseType: format === ExportFormat.JSON ? 'json' : 'blob'
      }
    );
    
    if (format !== ExportFormat.JSON) {
      this.downloadFile(response.data, `tickets.${format}`, 
        format === ExportFormat.PDF ? 'application/pdf' : 'text/csv');
    }
    
    return response.data;
  }

  private static downloadFile(blob: Blob, filename: string, contentType: string) {
    const url = window.URL.createObjectURL(new Blob([blob], { type: contentType }));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', filename);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  }
}
```

### 2. Componente de Botões de Exportação

```typescript
// components/ExportButtons.tsx (React) ou ExportButtons.vue (Vue)
import { ExportService, ExportFormat } from '@/services/export.service';
import { useState } from 'react';

interface ExportButtonsProps {
  module: 'entregas' | 'cobrancas' | 'metricas' | 'tickets';
  authorId?: number | string;
}

export function ExportButtons({ module, authorId }: ExportButtonsProps) {
  const [loading, setLoading] = useState<string | null>(null);

  const handleExport = async (format: ExportFormat) => {
    setLoading(format);
    try {
      switch (module) {
        case 'entregas':
          await ExportService.exportEntregas(format, authorId as number);
          break;
        case 'cobrancas':
          await ExportService.exportCobrancas(format, authorId as string);
          break;
        case 'metricas':
          await ExportService.exportMetricas(format, authorId as number);
          break;
        case 'tickets':
          await ExportService.exportTickets(format, authorId as string);
          break;
      }
    } catch (error) {
      console.error(`Erro ao exportar ${module}:`, error);
      alert(`Erro ao exportar ${module}. Tente novamente.`);
    } finally {
      setLoading(null);
    }
  };

  return (
    <div className="export-buttons">
      <button
        onClick={() => handleExport(ExportFormat.PDF)}
        disabled={loading === ExportFormat.PDF}
        className="btn-export btn-pdf"
      >
        {loading === ExportFormat.PDF ? 'Exportando...' : '📄 Exportar PDF'}
      </button>
      <button
        onClick={() => handleExport(ExportFormat.CSV)}
        disabled={loading === ExportFormat.CSV}
        className="btn-export btn-csv"
      >
        {loading === ExportFormat.CSV ? 'Exportando...' : '📊 Exportar CSV'}
      </button>
    </div>
  );
}
```

### 3. Exemplo de Uso em Página de Entregas

```typescript
// pages/EntregasPage.tsx
import { ExportButtons } from '@/components/ExportButtons';

export function EntregasPage() {
  const { authorId } = useAuth(); // Hook para obter authorId do usuário logado

  return (
    <div>
      <div className="page-header">
        <h1>Entregas</h1>
        <ExportButtons module="entregas" authorId={authorId} />
      </div>
      
      {/* Lista de entregas */}
      {/* ... */}
    </div>
  );
}
```

### 4. Exemplo de Uso em Página de Cobranças

```typescript
// pages/CobrancasPage.tsx
import { ExportButtons } from '@/components/ExportButtons';

export function CobrancasPage() {
  const { authorId } = useAuth();

  return (
    <div>
      <div className="page-header">
        <h1>Cobranças Mensais</h1>
        <ExportButtons module="cobrancas" authorId={authorId} />
      </div>
      
      {/* Lista de cobranças */}
      {/* ... */}
    </div>
  );
}
```

### 5. Exemplo de Uso em Página de Métricas

```typescript
// pages/MetricasPage.tsx
import { ExportButtons } from '@/components/ExportButtons';

export function MetricasPage() {
  const { authorId } = useAuth();

  return (
    <div>
      <div className="page-header">
        <h1>Métricas e Estatísticas</h1>
        <ExportButtons module="metricas" authorId={authorId} />
      </div>
      
      {/* Cards de métricas */}
      {/* ... */}
    </div>
  );
}
```

### 6. Exemplo de Uso em Página de Tickets

```typescript
// pages/TicketsPage.tsx
import { ExportButtons } from '@/components/ExportButtons';

export function TicketsPage() {
  const { authorId } = useAuth();

  return (
    <div>
      <div className="page-header">
        <h1>Tickets</h1>
        <ExportButtons module="tickets" authorId={authorId} />
      </div>
      
      {/* Lista de tickets */}
      {/* ... */}
    </div>
  );
}
```

---

## 🎨 Estilos CSS Sugeridos

```css
.export-buttons {
  display: flex;
  gap: 10px;
  margin-left: auto;
}

.btn-export {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-export:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-pdf {
  background-color: #dc3545;
  color: white;
}

.btn-pdf:hover:not(:disabled) {
  background-color: #c82333;
}

.btn-csv {
  background-color: #28a745;
  color: white;
}

.btn-csv:hover:not(:disabled) {
  background-color: #218838;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
```

---

## ⚠️ Tratamento de Erros

```typescript
try {
  await ExportService.exportEntregas(ExportFormat.PDF);
} catch (error) {
  if (axios.isAxiosError(error)) {
    if (error.response?.status === 403) {
      alert('Você não tem permissão para exportar estes dados.');
    } else if (error.response?.status === 401) {
      alert('Sessão expirada. Faça login novamente.');
      // Redirecionar para login
    } else {
      alert('Erro ao exportar. Tente novamente.');
    }
  } else {
    alert('Erro inesperado. Tente novamente.');
  }
}
```

---

## 📋 Checklist de Implementação

- [ ] Criar service `ExportService` com métodos para cada módulo
- [ ] Implementar função `downloadFile` para download automático
- [ ] Criar componente `ExportButtons` reutilizável
- [ ] Adicionar botões de exportação nas páginas:
  - [ ] Entregas
  - [ ] Cobranças
  - [ ] Métricas
  - [ ] Tickets
- [ ] Implementar tratamento de erros
- [ ] Adicionar loading states nos botões
- [ ] Testar exportação PDF
- [ ] Testar exportação CSV
- [ ] Testar exportação JSON (se necessário)
- [ ] Validar permissões (usuário só exporta seus dados)
- [ ] Adicionar feedback visual (toast/notificação) após exportação

---

## 📦 Pedidos Arquivados (Entregas)

### Conceito

Pedidos arquivados são entregas que já foram **finalizadas** (status `ENTREGUE`). O frontend deve gerenciar essa separação visualmente, permitindo que o autor:
- Veja entregas ativas (não arquivadas)
- Veja entregas arquivadas (status `ENTREGUE`)
- Exporte apenas entregas arquivadas ou todas

### Status de Envio

Os possíveis status são:
- `AGUARDANDO` - Aguardando envio
- `ENVIADO` - Livro foi enviado
- `RECUSADO` - Envio recusado/cancelado
- `ENTREGUE` - **Entregue (recebido pelo cliente)** ← Este é o status de arquivado

### Implementação no Frontend

#### 1. Filtro de Entregas

```typescript
// hooks/useEntregas.ts
import { useState, useEffect } from 'react';
import axios from 'axios';

interface EntregaDTO {
  pedidoId: number;
  statusEnvio: 'AGUARDANDO' | 'ENVIADO' | 'RECUSADO' | 'ENTREGUE';
  // ... outros campos
}

export function useEntregas() {
  const [entregas, setEntregas] = useState<EntregaDTO[]>([]);
  const [loading, setLoading] = useState(false);
  const [showArquivados, setShowArquivados] = useState(false);

  useEffect(() => {
    fetchEntregas();
  }, []);

  const fetchEntregas = async () => {
    setLoading(true);
    try {
      const token = localStorage.getItem('token');
      const response = await axios.get(
        'https://dashboard-painel-autores-vl-26b242c59563.herokuapp.com/api/v1/entregas',
        {
          headers: { 'Authorization': `Bearer ${token}` }
        }
      );
      setEntregas(response.data);
    } catch (error) {
      console.error('Erro ao buscar entregas:', error);
    } finally {
      setLoading(false);
    }
  };

  // Filtrar entregas baseado no status
  const entregasAtivas = entregas.filter(e => e.statusEnvio !== 'ENTREGUE');
  const entregasArquivadas = entregas.filter(e => e.statusEnvio === 'ENTREGUE');

  // Entregas a serem exibidas
  const entregasExibidas = showArquivados ? entregasArquivadas : entregasAtivas;

  return {
    entregas: entregasExibidas,
    entregasAtivas,
    entregasArquivadas,
    loading,
    showArquivados,
    setShowArquivados,
    totalAtivas: entregasAtivas.length,
    totalArquivadas: entregasArquivadas.length
  };
}
```

#### 2. Componente de Tabs/Abas

```typescript
// components/EntregasTabs.tsx
import { useState } from 'react';

interface EntregasTabsProps {
  totalAtivas: number;
  totalArquivadas: number;
  activeTab: 'ativas' | 'arquivadas';
  onTabChange: (tab: 'ativas' | 'arquivadas') => void;
}

export function EntregasTabs({ 
  totalAtivas, 
  totalArquivadas, 
  activeTab, 
  onTabChange 
}: EntregasTabsProps) {
  return (
    <div className="entregas-tabs">
      <button
        className={`tab ${activeTab === 'ativas' ? 'active' : ''}`}
        onClick={() => onTabChange('ativas')}
      >
        Entregas Ativas ({totalAtivas})
      </button>
      <button
        className={`tab ${activeTab === 'arquivadas' ? 'active' : ''}`}
        onClick={() => onTabChange('arquivadas')}
      >
        Pedidos Arquivados ({totalArquivadas})
      </button>
    </div>
  );
}
```

#### 3. Página de Entregas com Arquivados

```typescript
// pages/EntregasPage.tsx
import { useState } from 'react';
import { useEntregas } from '@/hooks/useEntregas';
import { EntregasTabs } from '@/components/EntregasTabs';
import { ExportButtons } from '@/components/ExportButtons';
import { ExportService, ExportFormat } from '@/services/export.service';

export function EntregasPage() {
  const { authorId } = useAuth();
  const {
    entregas,
    entregasAtivas,
    entregasArquivadas,
    loading,
    totalAtivas,
    totalArquivadas
  } = useEntregas();

  const [activeTab, setActiveTab] = useState<'ativas' | 'arquivadas'>('ativas');
  const [exporting, setExporting] = useState(false);

  const handleExport = async (format: ExportFormat, apenasArquivados: boolean = false) => {
    setExporting(true);
    try {
      if (apenasArquivados) {
        // Exportar apenas arquivados usando endpoint específico
        await ExportService.exportPedidosArquivados(format, authorId);
      } else {
        // Exportar todas
        await ExportService.exportEntregas(format, authorId);
      }
    } catch (error) {
      console.error('Erro ao exportar:', error);
      alert('Erro ao exportar entregas. Tente novamente.');
    } finally {
      setExporting(false);
    }
  };

  return (
    <div className="entregas-page">
      <div className="page-header">
        <h1>Entregas</h1>
        <div className="header-actions">
          {activeTab === 'arquivadas' && (
            <button
              onClick={() => handleExport(ExportFormat.PDF, true)}
              disabled={exporting}
              className="btn-export btn-pdf"
            >
              📄 Exportar Arquivados (PDF)
            </button>
          )}
          <ExportButtons module="entregas" authorId={authorId} />
        </div>
      </div>

      <EntregasTabs
        totalAtivas={totalAtivas}
        totalArquivadas={totalArquivadas}
        activeTab={activeTab}
        onTabChange={setActiveTab}
      />

      {loading ? (
        <div className="loading">Carregando entregas...</div>
      ) : (
        <div className="entregas-list">
          {entregas.length === 0 ? (
            <div className="empty-state">
              {activeTab === 'ativas' 
                ? 'Nenhuma entrega ativa no momento.'
                : 'Nenhum pedido arquivado.'}
            </div>
          ) : (
            entregas.map(entrega => (
              <EntregaCard key={entrega.pedidoId} entrega={entrega} />
            ))
          )}
        </div>
      )}
    </div>
  );
}
```

#### 4. Badge de Status

```typescript
// components/StatusBadge.tsx
interface StatusBadgeProps {
  status: 'AGUARDANDO' | 'ENVIADO' | 'RECUSADO' | 'ENTREGUE';
}

export function StatusBadge({ status }: StatusBadgeProps) {
  const statusConfig = {
    AGUARDANDO: { label: 'Aguardando', color: '#ffc107', bg: '#fff3cd' },
    ENVIADO: { label: 'Enviado', color: '#0d6efd', bg: '#cfe2ff' },
    RECUSADO: { label: 'Recusado', color: '#dc3545', bg: '#f8d7da' },
    ENTREGUE: { label: 'Entregue', color: '#198754', bg: '#d1e7dd' }
  };

  const config = statusConfig[status];

  return (
    <span 
      className="status-badge"
      style={{ 
        color: config.color, 
        backgroundColor: config.bg,
        padding: '4px 8px',
        borderRadius: '4px',
        fontSize: '12px',
        fontWeight: 'bold'
      }}
    >
      {config.label}
    </span>
  );
}
```

#### 5. Exportação Específica de Arquivados

O método `exportPedidosArquivados` já está implementado no `ExportService` acima. Ele usa o endpoint específico `/api/v1/entregas/export/arquivados` que retorna apenas entregas com status `ENTREGUE`.

**Uso:**
```typescript
// Exportar PDF de pedidos arquivados
await ExportService.exportPedidosArquivados(ExportFormat.PDF, authorId);

// Exportar CSV de pedidos arquivados
await ExportService.exportPedidosArquivados(ExportFormat.CSV, authorId);

// Buscar JSON de pedidos arquivados
const arquivados = await ExportService.exportPedidosArquivados(ExportFormat.JSON, authorId);
```

#### 6. Estilos CSS para Tabs e Arquivados

```css
.entregas-tabs {
  display: flex;
  gap: 0;
  border-bottom: 2px solid #dee2e6;
  margin-bottom: 24px;
}

.entregas-tabs .tab {
  padding: 12px 24px;
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  color: #6c757d;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
  transition: all 0.3s;
}

.entregas-tabs .tab:hover {
  color: #495057;
  background-color: #f8f9fa;
}

.entregas-tabs .tab.active {
  color: #0d6efd;
  border-bottom-color: #0d6efd;
  font-weight: 600;
}

.entregas-list {
  display: grid;
  gap: 16px;
}

.entrega-card {
  border: 1px solid #dee2e6;
  border-radius: 8px;
  padding: 16px;
  background: white;
  transition: box-shadow 0.3s;
}

.entrega-card:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.entrega-card.arquivado {
  opacity: 0.8;
  background-color: #f8f9fa;
}

.entrega-card.arquivado .entrega-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.entrega-card.arquivado .badge-arquivado {
  background-color: #198754;
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: bold;
}

.empty-state {
  text-align: center;
  padding: 48px;
  color: #6c757d;
  font-size: 16px;
}

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}
```

### Comportamento Esperado

1. **Visualização:**
   - Por padrão, mostrar apenas entregas ativas
   - Tab "Pedidos Arquivados" mostra apenas entregas com status `ENTREGUE`
   - Contador mostra quantidade em cada tab

2. **Exportação:**
   - Botão "Exportar PDF/CSV" na tab ativa exporta apenas as entregas visíveis
   - Opção de exportar todas as entregas (ativas + arquivadas)
   - Opção de exportar apenas arquivados

3. **Indicadores Visuais:**
   - Badge de status em cada entrega
   - Cards de entregas arquivadas com estilo diferenciado (opcional)
   - Ícone de "arquivo" para entregas arquivadas

### Exemplo de Card de Entrega

```typescript
// components/EntregaCard.tsx
import { StatusBadge } from './StatusBadge';

interface EntregaCardProps {
  entrega: EntregaDTO;
}

export function EntregaCard({ entrega }: EntregaCardProps) {
  const isArquivada = entrega.statusEnvio === 'ENTREGUE';
  
  return (
    <div className={`entrega-card ${isArquivada ? 'arquivado' : ''}`}>
      <div className="entrega-header">
        <div>
          <h3>Pedido #{entrega.pedidoId}</h3>
          <StatusBadge status={entrega.statusEnvio} />
        </div>
        {isArquivada && (
          <span className="badge-arquivado">📦 ARQUIVADO</span>
        )}
      </div>
      
      <div className="entrega-body">
        <p><strong>Cliente:</strong> {entrega.nomeCompleto}</p>
        <p><strong>Data:</strong> {formatDate(entrega.dataPedido)}</p>
        <p><strong>Valor:</strong> R$ {entrega.valorTotal.toFixed(2)}</p>
        {entrega.codigoRastreamento && (
          <p><strong>Rastreamento:</strong> {entrega.codigoRastreamento}</p>
        )}
      </div>
    </div>
  );
}
```

---

## 🔍 Notas Importantes

1. **Pedidos Arquivados (Entregas):**
   - **Status `ENTREGUE` = Arquivado**: Entregas com status `ENTREGUE` são consideradas arquivadas
   - **Filtro no Frontend**: O frontend deve filtrar entregas com status `ENTREGUE` para mostrar como "arquivados"
   - **Exportação**: A exportação padrão inclui todas as entregas. Para exportar apenas arquivados, filtrar no frontend antes de exportar
   - **Armazenamento Local (Opcional)**: O frontend pode armazenar localmente quais entregas foram visualizadas como arquivadas

2. **Formato de Arquivo:**
   - PDF: arquivo binário, usar `responseType: 'blob'`
   - CSV: arquivo texto, usar `responseType: 'blob'`
   - JSON: objeto JavaScript, usar `responseType: 'json'`

3. **Nome do Arquivo:**
   - O backend retorna o nome no header `Content-Disposition`
   - O frontend pode usar esse nome ou criar um próprio
   - Sugestão: `entregas_arquivadas_{data}.pdf` para arquivados

4. **Performance:**
   - PDFs podem ser grandes, considerar loading state
   - CSVs são mais leves, mas ainda podem demorar para muitos registros
   - Para muitas entregas arquivadas, considerar paginação ou lazy loading

5. **Persistência de Filtros:**
   - Salvar preferência do usuário (ativas vs arquivadas) no localStorage
   - Restaurar última visualização ao retornar à página

---

**Última atualização:** Novembro 2025

