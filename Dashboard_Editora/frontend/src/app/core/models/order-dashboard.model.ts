// DTOs para o módulo de Pedidos do Dashboard

export interface OrderWithCustomer {
  id: number;
  numeroPedido: number;
  cliente: Customer;
  endereco: Address;
  pedido: OrderDetails;
  cupom?: CouponInfo | null;
  items: OrderItem[];
}

export interface Customer {
  nomeCompleto: string;
  email: string;
  whatsapp: string;
  cpf: string;
}

export interface Address {
  rua: string;
  numero: string;
  complemento?: string | null;
  bairro: string;
  cidade: string;
  estado: string;
  cep: string;
  enderecoCompleto: string;
}

export interface OrderDetails {
  valorTotal: number;
  status: string;
  metodoPagamento: string;
  dataPedido: string; // ISO 8601
  pago: boolean;
  dataPagamento?: string | null;
}

export interface CouponInfo {
  codigo: string;
  descontoAplicado: number;
  valorOriginal?: number | null;
  nomeCupom?: string | null;
}

export interface OrderItem {
  bookId: string;
  titulo: string;
  quantidade: number;
  preco: number;
}

export interface CustomerStats {
  totalPedidos: number;
  clientesUnicosEmail: number;
  clientesUnicosWhatsapp: number;
  clientesUnicosCpf: number;
}

export interface CouponStats {
  totalPedidosComCupom: number;
  totalDescontoAplicado: number;
  cuponsUtilizados: CouponUsage[];
}

export interface CouponUsage {
  codigo: string;
  vezesUtilizado: number;
  totalDescontoAplicado: number;
  pedidosConfirmados: number;
}

