# ⚠️ Atualização Importante: Valores Reais de Pagamentos

## 📋 O Que Mudou?

O backend foi atualizado para retornar **valores reais** (após taxas e margens) ao invés de valores brutos dos pedidos.

---

## 🔄 Mudanças nos Endpoints

### Endpoint: `GET /api/v1/autor/pagamentos/painel`

**Antes (Valores Brutos - INCORRETO):**
```json
{
  "resumo": {
    "valorVendasConfirmadas": 1533.00,  // ❌ Valor bruto dos pedidos
    "valorJaRecebido": 0.0,
    "valorAReceber": 1533.00
  }
}
```

**Agora (Valores Reais - CORRETO):**
```json
{
  "resumo": {
    "valorVendasConfirmadas": 110.17,  // ✅ Valor REAL recebido (após taxas)
    "valorJaRecebido": 110.17,          // ✅ Valor REAL confirmado
    "valorAReceber": 351.23              // ✅ Valor REAL enviado mas pendente
  }
}
```

---

## 📊 O Que Significa Cada Campo

### `valorVendasConfirmadas`
- **Fonte:** `payment_payouts.amount_net` com `status = 'CONFIRMED'`
- **Significado:** Valor líquido que o autor realmente recebeu (após todas as taxas e margens)
- **Exemplo:** R$ 110,17 (não R$ 1.533,00)

### `valorJaRecebido`
- **Fonte:** `payment_payouts.amount_net` com `status = 'CONFIRMED'`
- **Significado:** Mesmo que `valorVendasConfirmadas` (valores confirmados = já recebidos)
- **Exemplo:** R$ 110,17

### `valorAReceber`
- **Fonte:** `payment_payouts.amount_net` com `status = 'SENT'`
- **Significado:** Valor líquido enviado mas ainda não confirmado (aguardando confirmação)
- **Exemplo:** R$ 351,23

---

## 🎯 Impacto no Frontend

### ✅ O Que Você Precisa Fazer

**Nada!** Os endpoints continuam os mesmos, apenas os valores retornados são diferentes (e mais precisos).

### ⚠️ O Que Você Pode Notar

1. **Valores Menores:** Os valores agora são menores porque descontam taxas e margens
   - Antes: R$ 1.533,00
   - Agora: R$ 110,17
   - **Isso é correto!** O autor realmente recebeu R$ 110,17, não R$ 1.533,00

2. **Valores Mais Precisos:** Os valores refletem exatamente o que o autor recebeu na conta

3. **`valorJaRecebido` Agora Tem Valor:** Antes era sempre 0.0, agora mostra o valor real recebido

---

## 📝 Exemplo de Uso no Frontend

### TypeScript/JavaScript

```typescript
// Buscar dados do painel
const response = await fetch('/api/v1/autor/pagamentos/painel', {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});

const data = await response.json();

// Exibir valores REAIS
console.log('Vendas Confirmadas:', data.resumo.valorVendasConfirmadas);  // 110.17
console.log('Já Recebido:', data.resumo.valorJaRecebido);                // 110.17
console.log('A Receber:', data.resumo.valorAReceber);                    // 351.23

// Formatação monetária brasileira
const formatCurrency = (value: number) => {
  return new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL'
  }).format(value);
};

// Exibir no UI
document.getElementById('vendas-confirmadas').textContent = 
  formatCurrency(data.resumo.valorVendasConfirmadas);  // R$ 110,17

document.getElementById('ja-recebido').textContent = 
  formatCurrency(data.resumo.valorJaRecebido);  // R$ 110,17

document.getElementById('a-receber').textContent = 
  formatCurrency(data.resumo.valorAReceber);  // R$ 351,23
```

---

## 🔍 Funil de Vendas

O campo `funilVendas` também foi atualizado:

**Antes:**
- `valorConfirmado`: Soma de `order_items.quantity * order_items.price` (valor bruto)
- `valorEmAndamento`: Soma de `order_items.quantity * order_items.price` (valor bruto)

**Agora:**
- `valorConfirmado`: Soma de `payment_payouts.amount_net` com `status = 'CONFIRMED'` (valor real)
- `valorEmAndamento`: Soma de `payment_payouts.amount_net` com `status = 'SENT'` (valor real)

**Nota:** A contagem de pedidos (`totalPedidos`, `pedidosConfirmados`, etc.) continua usando `orders` (isso está correto).

---

## 📊 Vendas Recentes

O campo `vendasRecentes` também foi atualizado:

**Antes:**
- `valorTotal`: `order_items.quantity * order_items.price` (valor bruto)

**Agora:**
- `valorTotal`: `payment_payouts.amount_net` (valor real)

---

## ✅ Checklist para Frontend

- [x] **Nenhuma mudança necessária** - Os endpoints continuam os mesmos
- [ ] **Verificar se há cálculos** baseados nos valores antigos (se houver, remover)
- [ ] **Atualizar textos de ajuda** se mencionavam valores brutos
- [ ] **Testar exibição** dos novos valores (devem ser menores e mais precisos)

---

## 🎯 Resumo

| Aspecto | Antes | Agora |
|---------|-------|-------|
| **Fonte dos Valores** | `orders.total` (bruto) | `payment_payouts.amount_net` (real) |
| **Desconta Taxas?** | ❌ Não | ✅ Sim |
| **Valores Mostrados** | R$ 1.533,00 | R$ 110,17 |
| **Precisão** | Aproximada | Exata (o que realmente caiu na conta) |

**Conclusão:** Os valores agora são **mais precisos** e refletem exatamente o que o autor recebeu. Não é necessário fazer nenhuma mudança no frontend, apenas estar ciente de que os valores serão menores (e mais corretos).

---

**Data da Atualização:** Janeiro 2024

