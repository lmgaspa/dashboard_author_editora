// DTOs para o módulo de E-mails do Autor

export interface ResumoEmailCliente {
  email: string;
  totalPedidos: number;
  totalPedidosConfirmados: number;
  valorTotalConfirmado: number;
  primeiroPedidoEm: string | null; // ISO 8601, UTC
  ultimoPedidoEm: string | null; // ISO 8601, UTC
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
}

export interface PainelEmailsAutor {
  emailsClientes: ResumoEmailCliente[];
  emailsRepasse: ResumoEmailRepasse[];
}

