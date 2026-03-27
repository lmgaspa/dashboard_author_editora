-- Migration V15: Atualiza status ENVIO_CONFIRMADO para ENTREGUE
-- Altera o constraint e atualiza registros existentes

-- 1. Atualizar registros existentes
UPDATE order_shipping 
SET status_envio = 'ENTREGUE' 
WHERE status_envio = 'ENVIO_CONFIRMADO';

-- 2. Remover constraint antigo
ALTER TABLE order_shipping 
DROP CONSTRAINT IF EXISTS chk_status_envio;

-- 3. Adicionar novo constraint com ENTREGUE
ALTER TABLE order_shipping 
ADD CONSTRAINT chk_status_envio 
CHECK (status_envio IN ('ENVIADO', 'AGUARDANDO', 'RECUSADO', 'ENTREGUE'));

-- 4. Atualizar comentário
COMMENT ON COLUMN order_shipping.status_envio IS 'Status do envio: ENVIADO, AGUARDANDO, RECUSADO, ENTREGUE';

