export enum TicketCategory {
  PAGAMENTO = 'PAGAMENTO',
  TECNICO = 'TECNICO',
  ALTERACAO = 'ALTERACAO',
  DUVIDA = 'DUVIDA',
  OUTRO = 'OUTRO'
}

export enum TicketStatus {
  OPEN = 'OPEN',
  IN_PROGRESS = 'IN_PROGRESS',
  WAITING_AUTHOR = 'WAITING_AUTHOR',
  WAITING_ADMIN = 'WAITING_ADMIN',
  RESOLVED = 'RESOLVED',
  CLOSED = 'CLOSED'
}

export enum TicketPriority {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH'
}

export interface TicketMessage {
  id: string;
  sentByUserId: string;
  sentByName: string;
  message: string;
  isInternalNote: boolean;
  createdAt: string;
  readAt: string | null;
}

export interface Ticket {
  id: string;
  ticketNumber: string;
  title: string;
  description: string;
  category: TicketCategory;
  status: TicketStatus;
  priority?: TicketPriority; // Apenas admin vê
  priorityReason?: string; // Apenas admin vê
  createdAt: string;
  updatedAt: string;
  resolvedAt?: string | null;
  closedAt?: string | null;
  messages: TicketMessage[];
  relatedChargeId?: string | null;
}

export interface CreateTicketRequest {
  title: string;
  description: string;
  category?: TicketCategory;
  relatedChargeId?: string | null;
}

export interface CreateMessageRequest {
  message: string;
  isInternalNote?: boolean;
}

