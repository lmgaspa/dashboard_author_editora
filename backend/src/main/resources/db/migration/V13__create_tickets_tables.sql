-- Create tickets table
-- Sistema de tickets para comunicação autor ↔ admin
CREATE TABLE IF NOT EXISTS tickets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Identificação
    ticket_number VARCHAR(20) UNIQUE NOT NULL,  -- "TKT-2024-001234"
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,                  -- Autor escreve o que quiser
    
    -- Relacionamentos
    created_by_user_id VARCHAR(20) NOT NULL,   -- FK para users.id (autor)
    assigned_to_user_id VARCHAR(20),           -- FK para users.id (admin responsável)
    author_id VARCHAR(255),                     -- author_id do criador (para filtros)
    
    -- Classificação
    category VARCHAR(50) NOT NULL,              -- 'PAGAMENTO', 'TECNICO', 'DUVIDA', 'OUTRO'
    
    -- Prioridade (APENAS ADMIN VÊ - sistema classifica automaticamente)
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM', -- 'LOW', 'MEDIUM', 'HIGH'
    priority_reason TEXT,                       -- Por que foi classificada assim (automático)
    
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN', -- 'OPEN', 'IN_PROGRESS', 'WAITING_AUTHOR', 'WAITING_ADMIN', 'RESOLVED', 'CLOSED'
    
    -- Metadados
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP,
    closed_at TIMESTAMP,
    
    -- Contexto (opcional - para referenciar cobranças)
    related_charge_id UUID,                     -- FK para monthly_charges.id (se relacionado a cobrança)
    
    CONSTRAINT fk_ticket_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    CONSTRAINT fk_ticket_assigned_to FOREIGN KEY (assigned_to_user_id) REFERENCES users(id),
    CONSTRAINT fk_ticket_charge FOREIGN KEY (related_charge_id) REFERENCES monthly_charges(id),
    CONSTRAINT chk_ticket_category CHECK (category IN ('PAGAMENTO', 'TECNICO', 'DUVIDA', 'OUTRO')),
    CONSTRAINT chk_ticket_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH')),
    CONSTRAINT chk_ticket_status CHECK (status IN ('OPEN', 'IN_PROGRESS', 'WAITING_AUTHOR', 'WAITING_ADMIN', 'RESOLVED', 'CLOSED'))
);

CREATE INDEX idx_tickets_created_by ON tickets(created_by_user_id);
CREATE INDEX idx_tickets_assigned_to ON tickets(assigned_to_user_id);
CREATE INDEX idx_tickets_author_id ON tickets(author_id);
CREATE INDEX idx_tickets_status ON tickets(status);
CREATE INDEX idx_tickets_category ON tickets(category);
CREATE INDEX idx_tickets_priority ON tickets(priority);
CREATE INDEX idx_tickets_created_at ON tickets(created_at DESC);
CREATE INDEX idx_tickets_ticket_number ON tickets(ticket_number);
CREATE INDEX idx_tickets_related_charge ON tickets(related_charge_id);

-- Create ticket_messages table
CREATE TABLE IF NOT EXISTS ticket_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    ticket_id UUID NOT NULL,                     -- FK para tickets.id
    sent_by_user_id VARCHAR(20) NOT NULL,       -- FK para users.id
    
    message TEXT NOT NULL,
    is_internal_note BOOLEAN NOT NULL DEFAULT FALSE, -- Nota interna (só admin vê)
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP,                          -- Quando foi lido pelo destinatário
    
    -- Anexos (futuro)
    attachments JSONB,                           -- Array de URLs de arquivos
    
    CONSTRAINT fk_message_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    CONSTRAINT fk_message_user FOREIGN KEY (sent_by_user_id) REFERENCES users(id)
);

CREATE INDEX idx_messages_ticket_id ON ticket_messages(ticket_id, created_at DESC);
CREATE INDEX idx_messages_sent_by ON ticket_messages(sent_by_user_id);
CREATE INDEX idx_messages_read_at ON ticket_messages(read_at) WHERE read_at IS NULL;

-- Create ticket_status_history table (Auditoria)
CREATE TABLE IF NOT EXISTS ticket_status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_id UUID NOT NULL,
    old_status VARCHAR(20),
    new_status VARCHAR(20) NOT NULL,
    changed_by_user_id VARCHAR(20) NOT NULL,
    change_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_history_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    CONSTRAINT fk_history_user FOREIGN KEY (changed_by_user_id) REFERENCES users(id)
);

CREATE INDEX idx_history_ticket_id ON ticket_status_history(ticket_id, created_at DESC);

