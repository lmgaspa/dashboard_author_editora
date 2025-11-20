# 📦 Prompt: Sistema de Entregas - Status da Mercadoria

## 🎯 Objetivo

Implementar no frontend um sistema completo de **gerenciamento de entregas** onde o autor pode:
1. **Visualizar todos os pedidos confirmados** com dados completos para envio
2. **Atualizar status de envio** de cada pedido
3. **Adicionar código de rastreamento** dos Correios
4. **Marcar se o livro foi enviado** (sim/não)

---

## 📊 Endpoints Disponíveis no Backend

### Base URL
O backend está em: `https://dashboard-painel-autores-vl-26b242c59563.herokuapp.com` (ou a URL do seu deploy)

**Todos os endpoints abaixo são relativos a `/api/v1`**

### 1. Listar Todas as Entregas

```
GET /api/v1/entregas
```

**Headers:**
- `Authorization: Bearer {token}`

**Response:**
```json
[
  {
    "pedidoId": 1009,
    "dataPedido": "2025-11-18T18:02:30Z",
    "valorTotal": 45.00,
    "statusPedido": "CONFIRMED",
    "nomeCompleto": "Irene Cazorla",
    "email": "icazorla@uol.com.br",
    "telefone": "(73)99177-9913",
    "cpf": "119.156.348-06",
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
        "bookId": "extase",
        "titulo": "Êxtase, de birra com Jorge Amado e outras crônicas grapiúnas",
        "quantidade": 1,
        "preco": 45.00
      }
    ],
    "enviado": false,
    "statusEnvio": "AGUARDANDO",
    "codigoRastreamento": null,
    "enviadoAt": null,
    "updatedAt": "2025-11-19T21:00:00Z"
  },
  {
    "pedidoId": 1007,
    "dataPedido": "2025-11-18T17:56:39Z",
    "valorTotal": 45.00,
    "statusPedido": "CONFIRMED",
    "nomeCompleto": "Irene Cazorla",
    "email": "icazorla@uol.com.br",
    "telefone": "(73)99177-9913",
    "cpf": "119.156.348-06",
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
        "bookId": "regressantes",
        "titulo": "Regressantes",
        "quantidade": 1,
        "preco": 45.00
      }
    ],
    "enviado": true,
    "statusEnvio": "ENVIADO",
    "codigoRastreamento": "BR123456789BR",
    "enviadoAt": "2025-11-19T10:00:00Z",
    "updatedAt": "2025-11-19T10:00:00Z"
  }
]
```

### 2. Buscar Entrega Específica

```
GET /api/v1/entregas/{orderId}
```

**Response:** Mesmo formato do item acima, mas apenas um objeto.

### 3. Atualizar Status de Envio

```
PUT /api/v1/entregas/{orderId}/status
```

**Headers:**
- `Authorization: Bearer {token}`
- `Content-Type: application/json`

**Request Body:**
```json
{
  "enviado": true,
  "statusEnvio": "ENVIADO",
  "codigoRastreamento": "BR123456789BR"
}
```

**Campos:**
- `enviado` (obrigatório): `true` ou `false` - Se o livro foi enviado
- `statusEnvio` (obrigatório): `"ENVIADO"`, `"AGUARDANDO"`, `"RECUSADO"`, ou `"ENVIO_CONFIRMADO"`
- `codigoRastreamento` (opcional): String com código de rastreamento dos Correios

**Response:**
Retorna o objeto `EntregaDTO` atualizado.

---

## 🎨 Status de Envio

### Valores Possíveis:

1. **`AGUARDANDO`** (padrão)
   - Pedido confirmado, aguardando envio pelo autor
   - `enviado: false`

2. **`ENVIADO`**
   - Livro foi enviado pelos Correios
   - `enviado: true`
   - Deve ter `codigoRastreamento` preenchido

3. **`RECUSADO`**
   - Envio foi recusado/cancelado
   - `enviado: false`

4. **`ENVIO_CONFIRMADO`**
   - Cliente confirmou recebimento
   - `enviado: true`

---

## 🏗️ Implementação Frontend

### 1. Types/Interfaces

#### `types/entregas.ts`

```typescript
export interface Entrega {
  pedidoId: number;
  dataPedido: string; // ISO 8601
  valorTotal: number;
  statusPedido: string;
  
  // Dados do Cliente
  nomeCompleto: string;
  email: string;
  telefone: string;
  cpf: string;
  
  // Endereço
  rua: string;
  numero: string;
  complemento?: string | null;
  bairro: string;
  cidade: string;
  estado: string;
  cep: string;
  enderecoCompleto: string;
  
  // Itens
  itens: ItemEntrega[];
  
  // Status de Envio
  enviado: boolean;
  statusEnvio: ShippingStatus;
  codigoRastreamento?: string | null;
  enviadoAt?: string | null; // ISO 8601
  updatedAt: string; // ISO 8601
}

export interface ItemEntrega {
  bookId: string;
  titulo: string;
  quantidade: number;
  preco: number;
}

export type ShippingStatus = 
  | 'ENVIADO' 
  | 'AGUARDANDO' 
  | 'RECUSADO' 
  | 'ENVIO_CONFIRMADO';

export interface AtualizarStatusEnvioRequest {
  enviado: boolean;
  statusEnvio: ShippingStatus;
  codigoRastreamento?: string | null;
}
```

### 2. API Service

#### `services/api/entregasApi.ts`

```typescript
import axios from 'axios';
import { Entrega, AtualizarStatusEnvioRequest } from '@/types/entregas';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'https://dashboard-painel-autores-vl-26b242c59563.herokuapp.com';

const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

axiosInstance.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export const entregasApi = {
  /**
   * Lista todas as entregas do autor logado.
   */
  async listarEntregas(): Promise<Entrega[]> {
    const response = await axiosInstance.get<Entrega[]>('/api/v1/entregas');
    return response.data;
  },

  /**
   * Busca uma entrega específica por orderId.
   */
  async buscarEntrega(orderId: number): Promise<Entrega> {
    const response = await axiosInstance.get<Entrega>(`/api/v1/entregas/${orderId}`);
    return response.data;
  },

  /**
   * Atualiza o status de envio de um pedido.
   */
  async atualizarStatusEnvio(
    orderId: number,
    request: AtualizarStatusEnvioRequest
  ): Promise<Entrega> {
    const response = await axiosInstance.put<Entrega>(
      `/api/v1/entregas/${orderId}/status`,
      request
    );
    return response.data;
  },
};
```

### 3. Custom Hook

#### `hooks/useEntregas.ts`

```typescript
import { useState, useEffect, useCallback } from 'react';
import { Entrega } from '@/types/entregas';
import { entregasApi } from '@/services/api/entregasApi';

export function useEntregas() {
  const [entregas, setEntregas] = useState<Entrega[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);

  const refetch = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await entregasApi.listarEntregas();
      setEntregas(data);
    } catch (err) {
      setError(err instanceof Error ? err : new Error('Erro ao buscar entregas'));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    refetch();
  }, [refetch]);

  return { entregas, loading, error, refetch };
}
```

### 4. Componente: Lista de Entregas

#### `components/entregas/EntregasList.tsx`

```typescript
import React, { useState } from 'react';
import { useEntregas } from '@/hooks/useEntregas';
import { Entrega, ShippingStatus } from '@/types/entregas';
import { formatCurrency, formatDate } from '@/utils/format';
import { StatusMercadoriaModal } from './StatusMercadoriaModal';
import { Package, MapPin, User, Calendar } from 'lucide-react';

export function EntregasList() {
  const { entregas, loading, error, refetch } = useEntregas();
  const [selectedEntrega, setSelectedEntrega] = useState<Entrega | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);

  const handleOpenModal = (entrega: Entrega) => {
    setSelectedEntrega(entrega);
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setSelectedEntrega(null);
    refetch(); // Atualizar lista após fechar modal
  };

  const getStatusBadgeClass = (status: ShippingStatus): string => {
    switch (status) {
      case 'ENVIADO':
        return 'badge-success';
      case 'AGUARDANDO':
        return 'badge-warning';
      case 'RECUSADO':
        return 'badge-danger';
      case 'ENVIO_CONFIRMADO':
        return 'badge-info';
      default:
        return 'badge-secondary';
    }
  };

  const getStatusLabel = (status: ShippingStatus): string => {
    switch (status) {
      case 'ENVIADO':
        return 'Enviado';
      case 'AGUARDANDO':
        return 'Aguardando';
      case 'RECUSADO':
        return 'Recusado';
      case 'ENVIO_CONFIRMADO':
        return 'Envio Confirmado';
      default:
        return status;
    }
  };

  if (loading) return <div>Carregando entregas...</div>;
  if (error) return <div>Erro: {error.message}</div>;

  return (
    <div className="entregas-list">
      <div className="header">
        <h2>
          <Package className="icon" />
          Entregas
        </h2>
        <p className="subtitle">Gerencie o envio dos livros para seus clientes</p>
      </div>

      <div className="stats-cards">
        <div className="stat-card">
          <div className="stat-value">{entregas.length}</div>
          <div className="stat-label">Total de Pedidos</div>
        </div>
        <div className="stat-card">
          <div className="stat-value">
            {entregas.filter(e => e.statusEnvio === 'AGUARDANDO').length}
          </div>
          <div className="stat-label">Aguardando Envio</div>
        </div>
        <div className="stat-card">
          <div className="stat-value">
            {entregas.filter(e => e.statusEnvio === 'ENVIADO').length}
          </div>
          <div className="stat-label">Enviados</div>
        </div>
        <div className="stat-card">
          <div className="stat-value">
            {entregas.filter(e => e.statusEnvio === 'ENVIO_CONFIRMADO').length}
          </div>
          <div className="stat-label">Confirmados</div>
        </div>
      </div>

      <table className="entregas-table">
        <thead>
          <tr>
            <th>Pedido #</th>
            <th>Cliente</th>
            <th>Endereço</th>
            <th>Itens</th>
            <th>Valor</th>
            <th>Data Pedido</th>
            <th>Status</th>
            <th>Ações</th>
          </tr>
        </thead>
        <tbody>
          {entregas.map((entrega) => (
            <tr key={entrega.pedidoId}>
              <td>
                <strong>#{entrega.pedidoId}</strong>
              </td>
              <td>
                <div className="cliente-info">
                  <User className="icon-small" />
                  <div>
                    <div className="nome">{entrega.nomeCompleto}</div>
                    <div className="email">{entrega.email}</div>
                    <div className="telefone">{entrega.telefone}</div>
                  </div>
                </div>
              </td>
              <td>
                <div className="endereco-info">
                  <MapPin className="icon-small" />
                  <div className="endereco-text">{entrega.enderecoCompleto}</div>
                </div>
              </td>
              <td>
                <div className="itens-list">
                  {entrega.itens.map((item, idx) => (
                    <div key={idx} className="item">
                      {item.quantidade}x {item.titulo}
                    </div>
                  ))}
                </div>
              </td>
              <td>
                <strong>{formatCurrency(entrega.valorTotal)}</strong>
              </td>
              <td>
                <Calendar className="icon-small" />
                {formatDate(entrega.dataPedido)}
              </td>
              <td>
                <span className={`badge ${getStatusBadgeClass(entrega.statusEnvio)}`}>
                  {getStatusLabel(entrega.statusEnvio)}
                </span>
                {entrega.codigoRastreamento && (
                  <div className="rastreamento">
                    <small>Rastreamento: {entrega.codigoRastreamento}</small>
                  </div>
                )}
              </td>
              <td>
                <button
                  className="btn btn-primary"
                  onClick={() => handleOpenModal(entrega)}
                >
                  Status da Mercadoria
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {selectedEntrega && (
        <StatusMercadoriaModal
          entrega={selectedEntrega}
          isOpen={isModalOpen}
          onClose={handleCloseModal}
        />
      )}
    </div>
  );
}
```

### 5. Componente: Modal Status da Mercadoria

#### `components/entregas/StatusMercadoriaModal.tsx`

```typescript
import React, { useState, useEffect } from 'react';
import { Entrega, ShippingStatus, AtualizarStatusEnvioRequest } from '@/types/entregas';
import { entregasApi } from '@/services/api/entregasApi';
import { formatCurrency, formatDate, getWhatsAppLink } from '@/utils/format';
import { X, Package, MapPin, User, Calendar, Truck } from 'lucide-react';

interface StatusMercadoriaModalProps {
  entrega: Entrega;
  isOpen: boolean;
  onClose: () => void;
}

export function StatusMercadoriaModal({
  entrega,
  isOpen,
  onClose,
}: StatusMercadoriaModalProps) {
  const [enviado, setEnviado] = useState(entrega.enviado);
  const [statusEnvio, setStatusEnvio] = useState<ShippingStatus>(entrega.statusEnvio);
  const [codigoRastreamento, setCodigoRastreamento] = useState(entrega.codigoRastreamento || '');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (isOpen) {
      setEnviado(entrega.enviado);
      setStatusEnvio(entrega.statusEnvio);
      setCodigoRastreamento(entrega.codigoRastreamento || '');
      setError(null);
    }
  }, [isOpen, entrega]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const request: AtualizarStatusEnvioRequest = {
        enviado,
        statusEnvio,
        codigoRastreamento: codigoRastreamento.trim() || null,
      };

      await entregasApi.atualizarStatusEnvio(entrega.pedidoId, request);
      onClose(); // Fechar modal e atualizar lista
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao atualizar status');
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h3>
            <Package className="icon" />
            Status da Mercadoria - Pedido #{entrega.pedidoId}
          </h3>
          <button className="btn-close" onClick={onClose}>
            <X />
          </button>
        </div>

        <div className="modal-body">
          {/* Informações do Pedido */}
          <section className="info-section">
            <h4>Informações do Pedido</h4>
            <div className="info-grid">
              <div className="info-item">
                <Calendar className="icon-small" />
                <span><strong>Data:</strong> {formatDate(entrega.dataPedido)}</span>
              </div>
              <div className="info-item">
                <strong>Valor Total:</strong> {formatCurrency(entrega.valorTotal)}
              </div>
            </div>
          </section>

          {/* Informações do Cliente */}
          <section className="info-section">
            <h4>
              <User className="icon" />
              Dados do Cliente
            </h4>
            <div className="info-grid">
              <div className="info-item">
                <strong>Nome:</strong> {entrega.nomeCompleto}
              </div>
              <div className="info-item">
                <strong>Email:</strong> {entrega.email}
              </div>
              <div className="info-item">
                <strong>Telefone:</strong>{' '}
                <a
                  href={getWhatsAppLink(entrega.telefone)}
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  {entrega.telefone}
                </a>
              </div>
              <div className="info-item">
                <strong>CPF:</strong> {entrega.cpf}
              </div>
            </div>
          </section>

          {/* Endereço de Entrega */}
          <section className="info-section">
            <h4>
              <MapPin className="icon" />
              Endereço de Entrega
            </h4>
            <div className="endereco-box">
              <p>{entrega.enderecoCompleto}</p>
            </div>
          </section>

          {/* Itens do Pedido */}
          <section className="info-section">
            <h4>Itens do Pedido</h4>
            <ul className="itens-list">
              {entrega.itens.map((item, idx) => (
                <li key={idx}>
                  {item.quantidade}x {item.titulo} - {formatCurrency(item.preco)}
                </li>
              ))}
            </ul>
          </section>

          {/* Formulário de Status */}
          <section className="form-section">
            <h4>
              <Truck className="icon" />
              Atualizar Status de Envio
            </h4>

            {error && (
              <div className="alert alert-danger">{error}</div>
            )}

            <form onSubmit={handleSubmit}>
              {/* Campo: Enviado (Sim/Não) */}
              <div className="form-group">
                <label>
                  <input
                    type="checkbox"
                    checked={enviado}
                    onChange={(e) => setEnviado(e.target.checked)}
                  />
                  <span>Livro foi enviado</span>
                </label>
              </div>

              {/* Campo: Status de Envio */}
              <div className="form-group">
                <label htmlFor="statusEnvio">Status do Envio *</label>
                <select
                  id="statusEnvio"
                  value={statusEnvio}
                  onChange={(e) => setStatusEnvio(e.target.value as ShippingStatus)}
                  required
                >
                  <option value="AGUARDANDO">Aguardando</option>
                  <option value="ENVIADO">Enviado</option>
                  <option value="RECUSADO">Recusado</option>
                  <option value="ENVIO_CONFIRMADO">Envio Confirmado</option>
                </select>
              </div>

              {/* Campo: Código de Rastreamento */}
              <div className="form-group">
                <label htmlFor="codigoRastreamento">Código de Rastreamento</label>
                <input
                  type="text"
                  id="codigoRastreamento"
                  value={codigoRastreamento}
                  onChange={(e) => setCodigoRastreamento(e.target.value)}
                  placeholder="Ex: BR123456789BR"
                />
                <small>Digite o código de rastreamento dos Correios (opcional)</small>
              </div>

              {/* Botões */}
              <div className="form-actions">
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={onClose}
                  disabled={loading}
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={loading}
                >
                  {loading ? 'Salvando...' : 'Salvar Status'}
                </button>
              </div>
            </form>
          </section>
        </div>
      </div>
    </div>
  );
}
```

### 6. Estilos CSS Sugeridos

#### `styles/entregas.css`

```css
.entregas-list {
  padding: 20px;
}

.entregas-list .header {
  margin-bottom: 30px;
}

.entregas-list .header h2 {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.stat-card .stat-value {
  font-size: 2rem;
  font-weight: bold;
  color: #007bff;
}

.stat-card .stat-label {
  color: #666;
  font-size: 0.9rem;
}

.entregas-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.entregas-table th {
  background: #f8f9fa;
  padding: 15px;
  text-align: left;
  font-weight: 600;
  border-bottom: 2px solid #dee2e6;
}

.entregas-table td {
  padding: 15px;
  border-bottom: 1px solid #dee2e6;
}

.entregas-table tr:hover {
  background: #f8f9fa;
}

.cliente-info,
.endereco-info {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.icon-small {
  width: 16px;
  height: 16px;
  color: #666;
  margin-top: 2px;
}

.endereco-text {
  font-size: 0.9rem;
  color: #666;
  line-height: 1.4;
}

.badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 0.85rem;
  font-weight: 500;
}

.badge-success {
  background: #d4edda;
  color: #155724;
}

.badge-warning {
  background: #fff3cd;
  color: #856404;
}

.badge-danger {
  background: #f8d7da;
  color: #721c24;
}

.badge-info {
  background: #d1ecf1;
  color: #0c5460;
}

.rastreamento {
  margin-top: 5px;
  font-size: 0.85rem;
  color: #666;
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  width: 90%;
  max-width: 800px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #dee2e6;
}

.modal-header h3 {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0;
}

.btn-close {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #666;
}

.modal-body {
  padding: 20px;
}

.info-section {
  margin-bottom: 30px;
}

.info-section h4 {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
  color: #333;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 15px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.endereco-box {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 4px;
  border-left: 4px solid #007bff;
}

.itens-list {
  list-style: none;
  padding: 0;
}

.itens-list li {
  padding: 8px 0;
  border-bottom: 1px solid #eee;
}

.form-section {
  margin-top: 30px;
  padding-top: 30px;
  border-top: 2px solid #dee2e6;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
}

.form-group input[type="text"],
.form-group select {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.form-group small {
  display: block;
  margin-top: 5px;
  color: #666;
  font-size: 0.85rem;
}

.form-group input[type="checkbox"] {
  margin-right: 8px;
}

.form-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 30px;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
  font-weight: 500;
}

.btn-primary {
  background: #007bff;
  color: white;
}

.btn-primary:hover {
  background: #0056b3;
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background: #545b62;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.alert {
  padding: 12px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.alert-danger {
  background: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}
```

---

## ✅ Checklist de Implementação

### Backend (Já Implementado):
- [x] Migration V14 criada
- [x] Domain models criados
- [x] JPA Entities criadas
- [x] Repository criado
- [x] Service criado
- [x] Controller criado
- [x] DTOs criados

### Frontend:
- [ ] Criar types/interfaces (`types/entregas.ts`)
- [ ] Criar API service (`services/api/entregasApi.ts`)
- [ ] Criar custom hook (`hooks/useEntregas.ts`)
- [ ] Criar componente de lista (`components/entregas/EntregasList.tsx`)
- [ ] Criar modal (`components/entregas/StatusMercadoriaModal.tsx`)
- [ ] Adicionar rota "Entregas" no menu lateral
- [ ] Adicionar estilos CSS
- [ ] Testar fluxo completo

---

## 🎯 Fluxo de Uso

1. **Autor acessa "Entregas"** no menu lateral
2. **Sistema lista todos os pedidos confirmados** com dados completos
3. **Autor clica em "Status da Mercadoria"** em um pedido
4. **Modal abre** mostrando:
   - Informações do pedido
   - Dados do cliente
   - Endereço completo
   - Itens do pedido
   - Formulário para atualizar status
5. **Autor preenche:**
   - ☑️ Livro foi enviado (sim/não)
   - Status: ENVIADO, AGUARDANDO, RECUSADO, ENVIO_CONFIRMADO
   - Código de rastreamento (opcional)
6. **Autor clica em "Salvar Status"**
7. **Sistema atualiza** e fecha o modal
8. **Lista é atualizada** automaticamente

---

## 📝 Notas Importantes

1. **Dados Automáticos:**
   - Todos os dados do pedido e cliente vêm automaticamente do banco do e-commerce
   - O autor só precisa preencher: enviado (sim/não), status, e código de rastreamento

2. **Status Padrão:**
   - Quando um pedido é confirmado, o status padrão é `AGUARDANDO`
   - O autor precisa atualizar manualmente quando enviar

3. **Código de Rastreamento:**
   - Opcional, mas recomendado quando status for `ENVIADO`
   - Formato livre (ex: "BR123456789BR", "AA123456789BR")

4. **Validação:**
   - `enviado` e `statusEnvio` são obrigatórios
   - `codigoRastreamento` é opcional

---

**Última atualização:** Novembro 2025

