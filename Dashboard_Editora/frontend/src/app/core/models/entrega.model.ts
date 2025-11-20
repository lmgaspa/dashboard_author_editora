/**
 * Modelos para o sistema de Entregas
 */

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
  | 'ENTREGUE';

export interface AtualizarStatusEnvioRequest {
  enviado: boolean;
  statusEnvio: ShippingStatus;
  codigoRastreamento?: string | null;
}

