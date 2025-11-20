# 🔄 Atualização Backend - Status de Envio: ENVIO_CONFIRMADO → ENTREGUE

## 📋 Mudança Necessária no Backend

O frontend foi atualizado para usar **`ENTREGUE`** ao invés de **`ENVIO_CONFIRMADO`**.

### Status Atual vs Novo Status

**ANTES:**
- `ENVIO_CONFIRMADO` - Cliente confirmou recebimento

**DEPOIS:**
- `ENTREGUE` - Cliente confirmou recebimento

---

## 🔧 O que precisa ser alterado no Backend

### 1. Enum/Constantes de Status

**Se usar Enum:**
```java
// ANTES
public enum ShippingStatus {
    AGUARDANDO,
    ENVIADO,
    RECUSADO,
    ENVIO_CONFIRMADO  // ← MUDAR PARA ENTREGUE
}

// DEPOIS
public enum ShippingStatus {
    AGUARDANDO,
    ENVIADO,
    RECUSADO,
    ENTREGUE  // ← NOVO
}
```

**Se usar String/Constantes:**
```java
// ANTES
public static final String ENVIO_CONFIRMADO = "ENVIO_CONFIRMADO";

// DEPOIS
public static final String ENTREGUE = "ENTREGUE";
```

### 2. Validação no Endpoint

**Endpoint:** `PUT /api/v1/entregas/{orderId}/status`

**Validação atual:**
```java
// ANTES
if (!status.equals("ENVIADO") && 
    !status.equals("AGUARDANDO") && 
    !status.equals("RECUSADO") && 
    !status.equals("ENVIO_CONFIRMADO")) {
    throw new BadRequestException("Status inválido");
}
```

**Validação nova:**
```java
// DEPOIS
if (!status.equals("ENVIADO") && 
    !status.equals("AGUARDANDO") && 
    !status.equals("RECUSADO") && 
    !status.equals("ENTREGUE")) {  // ← MUDAR AQUI
    throw new BadRequestException("Status inválido");
}
```

### 3. Banco de Dados (se aplicável)

**Se houver coluna com valores fixos ou constraint:**

```sql
-- Verificar se há constraint ou valores fixos
SELECT constraint_name, constraint_type 
FROM information_schema.table_constraints 
WHERE table_name = 'orders' OR table_name = 'deliveries';

-- Se houver CHECK constraint, atualizar:
ALTER TABLE orders 
DROP CONSTRAINT IF EXISTS check_shipping_status;

ALTER TABLE orders 
ADD CONSTRAINT check_shipping_status 
CHECK (status_envio IN ('AGUARDANDO', 'ENVIADO', 'RECUSADO', 'ENTREGUE'));
```

**Se houver dados antigos com `ENVIO_CONFIRMADO`:**
```sql
-- Migrar dados antigos (se necessário)
UPDATE orders 
SET status_envio = 'ENTREGUE' 
WHERE status_envio = 'ENVIO_CONFIRMADO';
```

### 4. DTOs/Models

**Se houver DTOs:**
```java
// ANTES
public class EntregaDTO {
    private ShippingStatus statusEnvio; // ENVIO_CONFIRMADO
}

// DEPOIS
public class EntregaDTO {
    private ShippingStatus statusEnvio; // ENTREGUE
}
```

### 5. Documentação/Swagger

**Atualizar documentação da API:**
```yaml
# ANTES
statusEnvio:
  type: string
  enum: [AGUARDANDO, ENVIADO, RECUSADO, ENVIO_CONFIRMADO]

# DEPOIS
statusEnvio:
  type: string
  enum: [AGUARDANDO, ENVIADO, RECUSADO, ENTREGUE]
```

---

## ✅ Checklist de Implementação Backend

- [ ] Atualizar Enum/Constantes de `ShippingStatus`
- [ ] Atualizar validação no endpoint `PUT /api/v1/entregas/{orderId}/status`
- [ ] Verificar e atualizar constraints do banco de dados (se houver)
- [ ] Migrar dados antigos de `ENVIO_CONFIRMADO` para `ENTREGUE` (se necessário)
- [ ] Atualizar DTOs/Models
- [ ] Atualizar documentação/Swagger
- [ ] Testar endpoint com novo status `ENTREGUE`
- [ ] Verificar compatibilidade com dados existentes

---

## 🔄 Retrocompatibilidade (Opcional)

Se quiser manter compatibilidade temporária com dados antigos:

```java
// Converter ENVIO_CONFIRMADO para ENTREGUE ao receber
public ShippingStatus normalizeStatus(String status) {
    if ("ENVIO_CONFIRMADO".equals(status)) {
        return ShippingStatus.ENTREGUE;
    }
    return ShippingStatus.valueOf(status);
}
```

---

## 📝 Notas

- O frontend já está atualizado para usar `ENTREGUE`
- O backend precisa aceitar `ENTREGUE` no lugar de `ENVIO_CONFIRMADO`
- Se houver dados antigos com `ENVIO_CONFIRMADO`, considere migrar ou manter compatibilidade temporária

---

**Última atualização:** Novembro 2025

