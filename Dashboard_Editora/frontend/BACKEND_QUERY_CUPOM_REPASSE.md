# 🔍 Query SQL para Buscar Cupons nos E-mails de Repasse

## 📋 Problema Identificado

O frontend está exibindo "NÃO" e "R$ 0,00" para todos os e-mails de repasse, mas o usuário suspeita que alguns pedidos realmente tiveram cupom aplicado.

**Pedidos suspeitos:**
- Pedido #1003
- Pedido #1005
- Pedido #1007
- Pedido #962

## 🔧 Solução: Atualizar Query SQL no Backend

O endpoint `/api/v1/autor/emails/painel` precisa fazer JOIN com a tabela `orders` para buscar informações de cupom.

### Query SQL Atual (Provavelmente)

```sql
SELECT 
    pe.id,
    pe.email_type as tipo_email,
    pe.status,
    pe.sent_at as enviado_em,
    pe.order_id as pedido_id,
    pp.amount_net as valor_repassado
FROM payout_email pe
LEFT JOIN payment_payouts pp ON pp.id = pe.payout_id
WHERE pe.to_email = :authorEmail
ORDER BY pe.sent_at DESC;
```

**Problema:** Esta query **NÃO** busca informações de cupom da tabela `orders`.

### Query SQL Corrigida (Com Cupom)

```sql
SELECT 
    pe.id,
    pe.email_type as tipo_email,
    pe.status,
    pe.sent_at as enviado_em,
    pe.order_id as pedido_id,
    pp.amount_net as valor_repassado,
    -- Informações de cupom
    CASE 
        WHEN o.coupon_code IS NOT NULL THEN true
        ELSE false
    END as teve_cupom,
    o.coupon_code as codigo_cupom,
    COALESCE(o.discount_amount, 0) as valor_desconto
FROM payout_email pe
LEFT JOIN payment_payouts pp ON pp.id = pe.payout_id
JOIN orders o ON o.id = pe.order_id  -- ← ADICIONAR ESTE JOIN!
WHERE pe.to_email = :authorEmail
ORDER BY pe.sent_at DESC;
```

### Query SQL com Filtro por Author (Se necessário)

Se o sistema precisar filtrar por `author_id` também:

```sql
SELECT 
    pe.id,
    pe.email_type as tipo_email,
    pe.status,
    pe.sent_at as enviado_em,
    pe.order_id as pedido_id,
    pp.amount_net as valor_repassado,
    -- Informações de cupom
    CASE 
        WHEN o.coupon_code IS NOT NULL THEN true
        ELSE false
    END as teve_cupom,
    o.coupon_code as codigo_cupom,
    COALESCE(o.discount_amount, 0) as valor_desconto
FROM payout_email pe
LEFT JOIN payment_payouts pp ON pp.id = pe.payout_id
JOIN orders o ON o.id = pe.order_id
JOIN order_items oi ON oi.order_id = o.id
JOIN books b ON b.id::text = oi.book_id
WHERE pe.to_email = :authorEmail
  AND b.author_id = :authorId  -- ← Filtro por author_id (se necessário)
ORDER BY pe.sent_at DESC;
```

## 📊 Estrutura de Dados Esperada

O backend precisa retornar o campo `cupom` em cada `ResumoEmailRepasse`:

```json
{
  "emailsRepasse": [
    {
      "id": 38,
      "pedidoId": 1003,
      "tipoEmail": "REPASSE_PIX",
      "status": "SENT",
      "enviadoEm": "2025-11-18T15:07:00Z",
      "valorRepassado": 20.00,
      "cupom": {
        "teveCupom": true,        // ← true se coupon_code IS NOT NULL
        "codigoCupom": "BONUS",   // ← o.coupon_code
        "valorDesconto": 5.00     // ← o.discount_amount
      }
    },
    {
      "id": 1,
      "pedidoId": 962,
      "tipoEmail": "REPASSE_PIX",
      "status": "SENT",
      "enviadoEm": "2025-11-15T10:34:00Z",
      "valorRepassado": 20.00,
      "cupom": {
        "teveCupom": false,       // ← false se coupon_code IS NULL
        "codigoCupom": null,      // ← null
        "valorDesconto": 0.00     // ← 0
      }
    }
  ]
}
```

## 🔍 Query para Verificar Cupons nos Pedidos Específicos

Para verificar se os pedidos realmente tiveram cupom, execute esta query no banco do e-commerce:

```sql
-- Verificar cupons nos pedidos suspeitos
SELECT 
    o.id as pedido_id,
    o.coupon_code,
    o.discount_amount,
    o.total,
    CASE 
        WHEN o.coupon_code IS NOT NULL THEN 'SIM'
        ELSE 'NÃO'
    END as teve_cupom
FROM orders o
WHERE o.id IN (1003, 1005, 1007, 962)
ORDER BY o.id;
```

**Resultado esperado:**
```
pedido_id | coupon_code | discount_amount | total | teve_cupom
----------|-------------|-----------------|-------|------------
962       | NULL        | NULL            | 50.00 | NÃO
1003      | BONUS       | 5.00            | 45.00 | SIM
1005      | NULL        | NULL            | 50.00 | NÃO
1007      | DESCONTO10  | 10.00           | 40.00 | SIM
```

## 🛠️ Implementação no Backend (Java/Kotlin)

### DTO Atualizado

```java
public class ResumoEmailRepasse {
    private Long id;
    private Long pedidoId;
    private String tipoEmail;
    private String status;
    private OffsetDateTime enviadoEm;
    private BigDecimal valorRepassado;
    private CouponInfoPayout cupom;  // ← ADICIONAR ESTE CAMPO!
}

public class CouponInfoPayout {
    private Boolean teveCupom;
    private String codigoCupom;
    private BigDecimal valorDesconto;
}
```

### Service/Repository Atualizado

```java
public List<ResumoEmailRepasse> buscarEmailsRepasse(String authorEmail, Long authorId) {
    String sql = """
        SELECT 
            pe.id,
            pe.email_type as tipo_email,
            pe.status,
            pe.sent_at as enviado_em,
            pe.order_id as pedido_id,
            pp.amount_net as valor_repassado,
            CASE 
                WHEN o.coupon_code IS NOT NULL THEN true
                ELSE false
            END as teve_cupom,
            o.coupon_code as codigo_cupom,
            COALESCE(o.discount_amount, 0) as valor_desconto
        FROM payout_email pe
        LEFT JOIN payment_payouts pp ON pp.id = pe.payout_id
        JOIN orders o ON o.id = pe.order_id
        JOIN order_items oi ON oi.order_id = o.id
        JOIN books b ON b.id::text = oi.book_id
        WHERE pe.to_email = ?
          AND b.author_id = ?
        ORDER BY pe.sent_at DESC
        """;
    
    return jdbcTemplate.query(sql, (rs, rowNum) -> {
        ResumoEmailRepasse repasse = new ResumoEmailRepasse();
        repasse.setId(rs.getLong("id"));
        repasse.setPedidoId(rs.getLong("pedido_id"));
        repasse.setTipoEmail(rs.getString("tipo_email"));
        repasse.setStatus(rs.getString("status"));
        repasse.setEnviadoEm(rs.getTimestamp("enviado_em").toInstant()
            .atOffset(ZoneOffset.UTC));
        repasse.setValorRepassado(rs.getBigDecimal("valor_repassado"));
        
        // Mapear informações de cupom
        CouponInfoPayout cupom = new CouponInfoPayout();
        cupom.setTeveCupom(rs.getBoolean("teve_cupom"));
        cupom.setCodigoCupom(rs.getString("codigo_cupom"));
        cupom.setValorDesconto(rs.getBigDecimal("valor_desconto"));
        repasse.setCupom(cupom);
        
        return repasse;
    }, authorEmail, authorId);
}
```

## ✅ Checklist de Implementação

- [ ] Atualizar query SQL para fazer JOIN com `orders`
- [ ] Adicionar campos `teve_cupom`, `codigo_cupom`, `valor_desconto` na query
- [ ] Atualizar DTO `ResumoEmailRepasse` para incluir campo `cupom`
- [ ] Criar DTO `CouponInfoPayout` (se não existir)
- [ ] Atualizar mapeamento do ResultSet para incluir informações de cupom
- [ ] Testar com pedidos que têm cupom (ex: 1003, 1007)
- [ ] Testar com pedidos que não têm cupom (ex: 962, 1005)
- [ ] Verificar se o frontend está recebendo os dados corretamente

## 🔍 Verificação Rápida

Execute esta query no banco do e-commerce para verificar se os pedidos têm cupom:

```sql
SELECT 
    o.id,
    o.coupon_code,
    o.discount_amount,
    o.total,
    pe.id as email_repasse_id,
    pe.email_type
FROM orders o
LEFT JOIN payout_email pe ON pe.order_id = o.id
WHERE o.id IN (1003, 1005, 1007, 962)
ORDER BY o.id;
```

**Se algum pedido tiver `coupon_code IS NOT NULL`, então o backend precisa retornar `teveCupom: true` para aquele e-mail de repasse.**

---

**Última atualização:** Novembro 2025

