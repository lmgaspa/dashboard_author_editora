-- Migration V14: Create order_shipping table
-- Tabela para rastrear status de envio de pedidos
-- Cada pedido pode ter um registro de shipping associado ao author_id

CREATE TABLE IF NOT EXISTS order_shipping (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Relacionamentos
    order_id BIGINT NOT NULL,                    -- ID do pedido no e-commerce
    author_id VARCHAR(255) NOT NULL,             -- author_id do autor responsável pelo envio
    
    -- Status de envio
    enviado BOOLEAN NOT NULL DEFAULT FALSE,      -- Se o livro foi enviado (sim/não)
    status_envio VARCHAR(20) NOT NULL DEFAULT 'AGUARDANDO', -- ENVIADO, AGUARDANDO, RECUSADO, ENVIO_CONFIRMADO
    codigo_rastreamento VARCHAR(255),            -- Código de rastreamento dos Correios
    
    -- Metadados
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    enviado_at TIMESTAMP,                        -- Quando foi marcado como enviado
    
    -- Constraints
    CONSTRAINT chk_status_envio CHECK (status_envio IN ('ENVIADO', 'AGUARDANDO', 'RECUSADO', 'ENVIO_CONFIRMADO')),
    CONSTRAINT uk_order_author UNIQUE (order_id, author_id) -- Um pedido só pode ter um registro por autor
);

-- Índices
CREATE INDEX idx_shipping_order_id ON order_shipping(order_id);
CREATE INDEX idx_shipping_author_id ON order_shipping(author_id);
CREATE INDEX idx_shipping_status ON order_shipping(status_envio);
CREATE INDEX idx_shipping_enviado ON order_shipping(enviado);
CREATE INDEX idx_shipping_created_at ON order_shipping(created_at DESC);

-- Comentários
COMMENT ON TABLE order_shipping IS 'Rastreamento de envio de pedidos por autor';
COMMENT ON COLUMN order_shipping.order_id IS 'ID do pedido no banco do e-commerce';
COMMENT ON COLUMN order_shipping.author_id IS 'author_id do autor responsável pelo envio';
COMMENT ON COLUMN order_shipping.enviado IS 'Se o livro foi enviado (sim/não)';
COMMENT ON COLUMN order_shipping.status_envio IS 'Status do envio: ENVIADO, AGUARDANDO, RECUSADO, ENVIO_CONFIRMADO';
COMMENT ON COLUMN order_shipping.codigo_rastreamento IS 'Código de rastreamento dos Correios';

