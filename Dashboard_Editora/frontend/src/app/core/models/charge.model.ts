export enum ChargeStatus {
  PENDING = 'PENDING',
  PAID = 'PAID',
  OVERDUE = 'OVERDUE',
  CANCELLED = 'CANCELLED'
}

export interface MonthlyChargeDTO {
  id: string;                    // UUID
  authorId: string;              // ID do autor
  authorName: string;           // Nome do autor
  chargeMonth: number;          // Mês (1-12)
  chargeYear: number;           // Ano (ex: 2024)
  amount: number;               // Valor (ex: 150.00)
  dueDate: string;              // Data de vencimento (YYYY-MM-DD)
  chargeDate: string;           // Data que a cobrança foi criada (YYYY-MM-DD)
  status: ChargeStatus;         // Status da cobrança
  paidAt: string | null;        // ISO 8601 format ou null
  confirmedByAdminName: string | null;  // Nome do admin que confirmou
  confirmedAt: string | null;    // ISO 8601 format ou null
  pixCode: string | null;       // Código PIX para pagamento
  pixExpiresAt: string | null;   // ISO 8601 format ou null
  daysOverdue: number;          // Dias de atraso (se OVERDUE)
  hasOpenTicket: boolean;       // Se já existe ticket aberto relacionado
}

export interface PixCodeResponse {
  pixCode: string;
  amount: number;
}

export interface CreateChargeRequest {
  authorId: string;
  chargeMonth: number;
  chargeYear: number;
  amount: number;
  dueDate: string;
}

export interface ConfirmPaymentRequest {
  notes?: string;
}

