// DTOs para o módulo de E-mails do Autor

export interface ResumoEmailCliente {
  email: string;
  totalPedidos: number;
  totalPedidosConfirmados: number;
  valorRepassado: number; // Valor real repassado (amount_net) após taxas
  primeiroPedidoEm: string | null; // ISO 8601, UTC
  ultimoPedidoEm: string | null; // ISO 8601, UTC
  cupom?: CouponInfoCliente | null; // Informações agregadas de cupom (opcional para compatibilidade)
}

export interface CouponInfoCliente {
  pedidosComCupom: number; // Quantidade de pedidos confirmados com cupom
  totalDesconto: number; // Soma total de descontos aplicados
}

export interface ResumoEmailRepasse {
  id: number | null;
  pedidoId: number | null;
  repasseId: number | null;
  emailDestinatario: string;
  tipoEmail: string; // 'REPASSE_PIX', 'REPASSE_CARD', etc.
  status: string; // 'SENT', 'FAILED'
  enviadoEm: string | null; // ISO 8601, UTC
  mensagemErro: string | null;
  valorRepassado: number | null; // Valor repassado em caso de erro
  cupom?: CouponInfoPayout | null; // Informações de cupom (se disponível)
}

export interface CouponInfoPayout {
  teveCupom: boolean; // true se teve cupom, false caso contrário
  codigoCupom?: string | null; // null se não tiver cupom
  valorDesconto: number; // 0 se não tiver cupom
}

export interface PainelEmailsAutor {
  emailsClientes: ResumoEmailCliente[];
  emailsRepasse: ResumoEmailRepasse[];
}

