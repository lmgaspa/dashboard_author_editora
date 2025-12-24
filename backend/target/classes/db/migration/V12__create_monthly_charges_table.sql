-- Create monthly_charges table
-- Sistema de cobrança mensal variável por contrato
CREATE TABLE IF NOT EXISTS monthly_charges (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Relacionamento
    author_id VARCHAR(255) NOT NULL,           -- author_id do usuário
    created_by_user_id VARCHAR(20) NOT NULL,  -- Admin que criou (FK users.id)
    
    -- Cobrança
    charge_month INTEGER NOT NULL,             -- Mês (1-12)
    charge_year INTEGER NOT NULL,              -- Ano (ex: 2024)
    amount DECIMAL(10, 2) NOT NULL,            -- Valor (variável por contrato - ex: 150.00, 1550.00)
    due_date DATE NOT NULL,                     -- Data de vencimento (dia X do contrato)
    charge_date DATE NOT NULL,                  -- Data que a cobrança foi criada
    
    -- Status do pagamento
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- 'PENDING', 'PAID', 'OVERDUE', 'CANCELLED'
    paid_at TIMESTAMP,                         -- Quando foi pago
    confirmed_by_user_id VARCHAR(20),          -- Admin que confirmou (FK users.id)
    confirmed_at TIMESTAMP,                    -- Quando admin confirmou
    
    -- PIX
    pix_code TEXT,                             -- Código PIX gerado (QR Code)
    pix_expires_at TIMESTAMP,                  -- Quando PIX expira
    
    -- Metadados
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,                                 -- Notas do admin
    
    CONSTRAINT fk_charge_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id),
    CONSTRAINT fk_charge_confirmed_by FOREIGN KEY (confirmed_by_user_id) REFERENCES users(id),
    CONSTRAINT uk_charge_month UNIQUE (author_id, charge_month, charge_year),
    CONSTRAINT chk_charge_month CHECK (charge_month >= 1 AND charge_month <= 12),
    CONSTRAINT chk_charge_year CHECK (charge_year >= 2020 AND charge_year <= 2100),
    CONSTRAINT chk_charge_amount CHECK (amount > 0),
    CONSTRAINT chk_charge_status CHECK (status IN ('PENDING', 'PAID', 'OVERDUE', 'CANCELLED'))
);

CREATE INDEX idx_charge_author_status ON monthly_charges(author_id, status);
CREATE INDEX idx_charge_due_date ON monthly_charges(due_date);
CREATE INDEX idx_charge_status ON monthly_charges(status);
CREATE INDEX idx_charge_created_by ON monthly_charges(created_by_user_id);
CREATE INDEX idx_charge_month_year ON monthly_charges(charge_month, charge_year);

