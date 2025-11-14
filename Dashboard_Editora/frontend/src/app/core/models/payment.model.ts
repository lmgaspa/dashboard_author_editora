// DTO para um pagamento individual
export interface AuthorPaymentDTO {
  id: number;
  orderId?: string;
  amount: number;
  currency: string;
  status: string;
  paymentMethod: string;
  transactionId?: string;
  paidAt?: string; // ISO string
  createdAt?: string; // ISO string
  customerName?: string;
  customerEmail?: string;
  customerDocument?: string;
  description?: string;
}

// DTO para resumo de pagamentos
export interface PaymentSummaryDTO {
  totalAmount: number;
  totalCount: number;
  paidCount: number;
  pendingCount: number;
  cancelledCount: number;
  currency: string;
  period?: {
    startDate?: string; // ISO string
    endDate?: string; // ISO string
  };
}

// Resposta paginada de pagamentos
export interface PaymentPageResponse {
  content: AuthorPaymentDTO[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      sorted: boolean;
    };
  };
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
  size: number;
  number: number;
}

// Interfaces para o Painel de Pagamentos do Autor
export interface PagamentosAutorResumo {
  autorId: number;
  nomeAutor: string;
  valorVendasConfirmadas: number;
  valorJaRecebido: number;
  valorAReceber: number;
}

export interface FunilVendas {
  totalPedidos: number;
  pedidosConfirmados: number;
  pedidosEmAndamento: number;
  pedidosCancelados: number;
  taxaConversao: number;
  valorTotalPedidos: number;
  valorConfirmado: number;
  valorEmAndamento: number;
}

export interface VendaRecente {
  pedidoId: number;
  dataPedido: string; // ISO string
  tituloLivro: string;
  quantidade: number;
  valorTotal: number;
  statusLegivel: string;
}

export interface PainelPagamentosAutor {
  resumo: PagamentosAutorResumo;
  funilVendas: FunilVendas;
  vendasRecentes: VendaRecente[];
}

