# Implementação: Dados de Clientes e Pedidos - Frontend Angular

## ✅ O que foi implementado

### 1. Modelos TypeScript

#### `src/app/core/models/order-dashboard.model.ts`
- `OrderWithCustomer`: Interface completa para pedido com dados do cliente
- `Customer`: Dados do cliente (nome, email, WhatsApp, CPF)
- `Address`: Endereço completo do cliente
- `OrderDetails`: Detalhes do pedido (valor, status, método de pagamento, etc.)
- `CouponInfo`: Informações do cupom aplicado
- `OrderItem`: Itens do pedido
- `CustomerStats`: Estatísticas de clientes
- `CouponStats`: Estatísticas de cupons
- `CouponUsage`: Uso de cupons

#### `src/app/core/models/payout-email-dashboard.model.ts`
- `PayoutEmailWithCoupon`: E-mail de repasse com informações de cupom
- `CouponInfoPayout`: Informações de cupom para e-mails de repasse

#### `src/app/core/models/email.model.ts` (Atualizado)
- Adicionado campo `cupom?: CouponInfoPayout | null` em `ResumoEmailRepasse`
- `CouponInfoPayout`: Interface para informações de cupom em e-mails de repasse

### 2. Serviços

#### `src/app/core/services/order-dashboard.service.ts`
Serviço para acessar dados de pedidos do dashboard:
- `getOrder(orderId: number)`: Busca um pedido específico
- `listOrders(status?: string)`: Lista pedidos (opcionalmente filtrado por status)
- `getOrdersByCustomer(email?, phone?, cpf?)`: Busca pedidos por cliente
- `getCustomerStats()`: Estatísticas de clientes
- `getCouponStats()`: Estatísticas de cupons

**Endpoints esperados:**
- `GET /api/v1/dashboard/orders/{orderId}`
- `GET /api/v1/dashboard/orders?status=CONFIRMED`
- `GET /api/v1/dashboard/orders/by-customer?email=...&phone=...&cpf=...`
- `GET /api/v1/dashboard/orders/stats/customers`
- `GET /api/v1/dashboard/orders/stats/coupons`

#### `src/app/core/services/payout-email-dashboard.service.ts`
Serviço para acessar e-mails de repasse com informações de cupom:
- `listPayoutEmails(emailType?: string)`: Lista e-mails de repasse
- `getPayoutEmail(id: number)`: Busca um e-mail específico

**Endpoints esperados:**
- `GET /api/v1/dashboard/payout-emails?emailType=REPASSE_PIX`
- `GET /api/v1/dashboard/payout-emails/{id}`

### 3. Utilitários

#### `src/app/core/utils/payout-email-format.utils.ts`
Funções utilitárias para formatação de cupons:
- `getCupomUtilizadoText(cupom)`: Retorna "SIM" ou "NÃO"
- `getDescontoText(cupom)`: Retorna "R$ X,XX" ou "R$ 0,00"
- `getCupomUtilizadoClasses(cupom)`: Classes CSS para estilização
- `getDescontoClasses(cupom)`: Classes CSS para desconto

### 4. Componentes Atualizados

#### `src/app/features/user/pages/emails/emails-page.component.html`
Adicionadas duas novas colunas na tabela de e-mails de repasse:
- **Cupom Utilizado**: Exibe "SIM" (verde, negrito) ou "NÃO" (cinza)
- **Desconto**: Exibe "R$ X,XX" (azul, negrito) ou "R$ 0,00" (cinza)

**Regras de exibição:**
- Se `cupom.teveCupom === true`: Exibe "SIM" em verde e desconto formatado
- Se `cupom.teveCupom === false` ou `cupom === null`: Exibe "NÃO" em cinza e "R$ 0,00"

Também atualizado para versão mobile (cards) com as mesmas informações.

---

## 🔧 O que o Backend precisa implementar

### 1. Endpoint de E-mails de Repasse com Cupom

O endpoint atual `/api/v1/autor/emails/painel` precisa retornar informações de cupom em `ResumoEmailRepasse`:

**Estrutura esperada:**
```json
{
  "emailsRepasse": [
    {
      "id": 38,
      "pedidoId": 1003,
      "tipoEmail": "REPASSE_PIX",
      "status": "SENT",
      "enviadoEm": "2025-11-18T15:07:00Z",
      "valorRepassado": 33.47,
      "cupom": {
        "teveCupom": true,
        "codigoCupom": "BONUS",
        "valorDesconto": 5.00
      }
    },
    {
      "id": 1,
      "pedidoId": 962,
      "tipoEmail": "REPASSE_PIX",
      "status": "SENT",
      "enviadoEm": "2025-11-15T10:34:00Z",
      "valorRepassado": 38.35,
      "cupom": {
        "teveCupom": false,
        "codigoCupom": null,
        "valorDesconto": 0.00
      }
    }
  ]
}
```

**Query SQL sugerida (backend):**
```sql
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
WHERE pe.to_email = :authorEmail
ORDER BY pe.sent_at DESC;
```

### 2. Endpoints de Dashboard (Opcional - para funcionalidades futuras)

Se quiser implementar funcionalidades adicionais de visualização de pedidos:

**Endpoints necessários:**
- `GET /api/v1/dashboard/orders/{orderId}` - Detalhes completos do pedido
- `GET /api/v1/dashboard/orders?status=CONFIRMED` - Lista de pedidos
- `GET /api/v1/dashboard/orders/by-customer?email=...` - Pedidos por cliente
- `GET /api/v1/dashboard/orders/stats/customers` - Estatísticas
- `GET /api/v1/dashboard/orders/stats/coupons` - Estatísticas de cupons
- `GET /api/v1/dashboard/payout-emails?emailType=REPASSE_PIX` - E-mails de repasse com cupom

---

## 📋 Checklist de Implementação Backend

### Prioridade Alta (para funcionar com o frontend atual)
- [ ] Atualizar endpoint `/api/v1/autor/emails/painel` para incluir campo `cupom` em `ResumoEmailRepasse`
- [ ] Fazer JOIN com tabela `orders` para buscar `coupon_code` e `discount_amount`
- [ ] Retornar estrutura `CouponInfoPayout` conforme especificado

### Prioridade Baixa (funcionalidades futuras)
- [ ] Implementar endpoints de dashboard de pedidos (`/api/v1/dashboard/orders/*`)
- [ ] Implementar endpoints de estatísticas
- [ ] Implementar endpoint de e-mails de repasse do dashboard (`/api/v1/dashboard/payout-emails`)

---

## 🎨 Exemplo de Uso no Frontend

### Exibir informações de cupom em e-mails de repasse

O componente `emails-page.component.html` já está configurado para exibir as informações de cupom automaticamente quando o backend retornar o campo `cupom` em `ResumoEmailRepasse`.

**Exemplo de código (já implementado):**
```html
<td class="py-3 px-4 text-center">
  @if (repasse.cupom?.teveCupom) {
    <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-bold text-emerald-300 bg-emerald-500/20 border border-emerald-500/30">
      SIM
    </span>
  } @else {
    <span class="text-gray-500">NÃO</span>
  }
</td>
<td class="py-3 px-4 text-sm">
  @if (repasse.cupom?.teveCupom && repasse.cupom.valorDesconto > 0) {
    <span class="text-blue-400 font-semibold">
      {{ repasse.cupom.valorDesconto | currency:'BRL':'symbol':'1.2-2' }}
    </span>
  } @else {
    <span class="text-gray-500">R$ 0,00</span>
  }
</td>
```

### Usar serviços para buscar dados de pedidos (futuro)

```typescript
import { OrderDashboardService } from '@/app/core/services/order-dashboard.service';

// No componente
private orderService = inject(OrderDashboardService);

// Buscar pedido específico
this.orderService.getOrder(1003).subscribe(order => {
  console.log('Cliente:', order.cliente.nomeCompleto);
  console.log('Cupom:', order.cupom?.codigo);
});

// Listar pedidos confirmados
this.orderService.listOrders('CONFIRMED').subscribe(orders => {
  console.log('Total de pedidos:', orders.length);
});

// Estatísticas de cupons
this.orderService.getCouponStats().subscribe(stats => {
  console.log('Total de desconto aplicado:', stats.totalDescontoAplicado);
});
```

---

## 🔍 Compatibilidade

O frontend está preparado para trabalhar com:
- ✅ Backend que retorna `cupom` em `ResumoEmailRepasse` (estrutura completa)
- ✅ Backend que retorna `cupom: null` (sem informações de cupom)
- ✅ Backend que não retorna o campo `cupom` (usará valores padrão: "NÃO" e "R$ 0,00")

**Nota:** Se o backend não retornar o campo `cupom`, o frontend exibirá "NÃO" e "R$ 0,00" por padrão, sem quebrar a aplicação.

---

## 📝 Notas Importantes

1. **Compatibilidade retroativa**: O frontend funciona mesmo se o backend ainda não retornar informações de cupom
2. **Formatação**: Valores monetários são formatados automaticamente usando `CurrencyPipe` do Angular
3. **Responsividade**: As informações de cupom são exibidas tanto na versão desktop (tabela) quanto mobile (cards)
4. **Estilização**: Cores e badges seguem o padrão visual do sistema (verde para "SIM", cinza para "NÃO", azul para valores)

---

**Última atualização:** Novembro 2025

