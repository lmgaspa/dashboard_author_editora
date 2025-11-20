// DTOs para E-mails de Repasse com Informações de Cupom

export interface PayoutEmailWithCoupon {
  id: number;
  tipo: string; // "REPASSE_PIX" ou "REPASSE_CARD"
  status: string; // "SENT" ou "FAILED"
  enviadoEm: string; // ISO 8601
  pedidoId: number;
  valorRepassado?: number | null;
  cupom: CouponInfoPayout;
}

export interface CouponInfoPayout {
  teveCupom: boolean; // true se teve cupom, false caso contrário
  codigoCupom?: string | null; // null se não tiver cupom
  valorDesconto: number; // 0 se não tiver cupom
}

