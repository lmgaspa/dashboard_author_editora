# 📝 Changelog: Atualização de E-mails - Cupom e Valor Repassado

## ✅ Mudanças Implementadas

### 1. Modelos TypeScript Atualizados

#### `src/app/core/models/email.model.ts`

**Mudanças em `ResumoEmailCliente`:**
- ❌ Removido: `valorTotalConfirmado: number`
- ✅ Adicionado: `valorRepassado: number` (valor real após taxas)
- ✅ Adicionado: `cupom?: CouponInfoCliente | null` (informações agregadas de cupom)

**Nova interface `CouponInfoCliente`:**
```typescript
export interface CouponInfoCliente {
  pedidosComCupom: number; // Quantidade de pedidos confirmados com cupom
  totalDesconto: number; // Soma total de descontos aplicados
}
```

**`ResumoEmailRepasse` já estava correto:**
- ✅ `valorRepassado` já existia
- ✅ `cupom?: CouponInfoPayout | null` já existia

### 2. Componente HTML Atualizado

#### `src/app/features/user/pages/emails/emails-page.component.html`

**Tabela Desktop - E-mails de Clientes:**
- ✅ Label alterado: "Valor Total" → "Valor Repassado"
- ✅ Campo alterado: `valorTotalConfirmado` → `valorRepassado`
- ✅ Nova coluna: "Pedidos com Cupom"
- ✅ Nova coluna: "Total Desconto"
- ✅ Estilos aplicados:
  - Pedidos com cupom > 0: badge verde em destaque
  - Total desconto > 0: texto azul em negrito
  - Caso contrário: texto cinza

**Cards Mobile - E-mails de Clientes:**
- ✅ Label alterado: "Valor Total" → "Valor Repassado"
- ✅ Campo alterado: `valorTotalConfirmado` → `valorRepassado`
- ✅ Nova seção: "Pedidos com Cupom" e "Total Desconto"
- ✅ Mesmos estilos aplicados

**E-mails de Repasse:**
- ✅ Já estava correto (cupom já implementado anteriormente)
- ✅ `valorRepassado` já estava sendo exibido corretamente

### 3. Compatibilidade Retroativa

- ✅ Campo `cupom` é opcional (`cupom?: CouponInfoCliente | null`)
- ✅ Template usa optional chaining (`email.cupom?.pedidosComCupom`)
- ✅ Valores padrão exibidos quando `cupom` não está presente:
  - Pedidos com cupom: `0`
  - Total desconto: `R$ 0,00`

## 📊 Estrutura de Dados Esperada do Backend

### E-mails de Clientes

```json
{
  "email": "icazorla@uol.com.br",
  "totalPedidos": 15,
  "totalPedidosConfirmados": 9,
  "valorRepassado": 268.65,
  "primeiroPedidoEm": "2025-11-12T10:00:00Z",
  "ultimoPedidoEm": "2025-11-18T15:02:00Z",
  "cupom": {
    "pedidosComCupom": 5,
    "totalDesconto": 25.00
  }
}
```

### E-mails de Repasse

```json
{
  "id": 38,
  "pedidoId": 1003,
  "valorRepassado": 33.47,
  "cupom": {
    "teveCupom": true,
    "codigoCupom": "BONUS",
    "valorDesconto": 5.00
  }
}
```

## 🎨 Regras de Exibição Implementadas

### E-mails de Clientes

#### Valor Repassado
- **Label:** "Valor Repassado"
- **Valor:** `email.valorRepassado`
- **Formato:** `currency:'BRL':'symbol':'1.2-2'`

#### Pedidos com Cupom
- **Se `cupom.pedidosComCupom > 0`:**
  - Exibe: número em badge verde (`bg-emerald-500/20 border-emerald-500/30 text-emerald-300`)
- **Se `cupom.pedidosComCupom = 0` ou não existe:**
  - Exibe: `0` em texto cinza

#### Total Desconto
- **Se `cupom.totalDesconto > 0`:**
  - Exibe: valor formatado em azul e negrito (`text-blue-400 font-semibold`)
- **Se `cupom.totalDesconto = 0` ou não existe:**
  - Exibe: `R$ 0,00` em texto cinza

### E-mails de Repasse

#### Cupom Utilizado
- **Se `cupom.teveCupom = true`:**
  - Exibe: "SIM" em verde e negrito
- **Se `cupom.teveCupom = false`:**
  - Exibe: "NÃO" em cinza

#### Desconto
- **Se `cupom.teveCupom = true`:**
  - Exibe: valor formatado em azul e negrito
- **Se `cupom.teveCupom = false`:**
  - Exibe: `R$ 0,00` em cinza

## ✅ Checklist de Implementação

### Types/Interfaces:
- [x] Atualizar `ResumoEmailCliente`:
  - [x] Remover `valorTotalConfirmado`
  - [x] Adicionar `valorRepassado`
  - [x] Adicionar campo `cupom?: CouponInfoCliente | null`
- [x] Criar `CouponInfoCliente` interface
- [x] Verificar `ResumoEmailRepasse` (já estava correto)

### Componentes:
- [x] Atualizar tabela desktop de e-mails de clientes:
  - [x] Mudar label de "Valor Total" para "Valor Repassado"
  - [x] Usar `email.valorRepassado`
  - [x] Adicionar coluna "Pedidos com Cupom"
  - [x] Adicionar coluna "Total Desconto"
  - [x] Aplicar estilos
- [x] Atualizar cards mobile de e-mails de clientes:
  - [x] Mudar label de "Valor Total" para "Valor Repassado"
  - [x] Usar `email.valorRepassado`
  - [x] Adicionar seção de cupom
  - [x] Aplicar estilos
- [x] Verificar e-mails de repasse (já estava correto)

### Compatibilidade:
- [x] Tornar campo `cupom` opcional
- [x] Usar optional chaining no template
- [x] Valores padrão quando `cupom` não existe

## 🔍 Validação

### Exemplo Real (icazorla@uol.com.br)

**Backend retorna:**
```json
{
  "email": "icazorla@uol.com.br",
  "valorRepassado": 268.65,
  "cupom": {
    "pedidosComCupom": 5,
    "totalDesconto": 25.00
  }
}
```

**Frontend exibe:**
- ✅ E-mail: `icazorla@uol.com.br`
- ✅ Valor Repassado: `R$ 268,65` (não R$ 220,00)
- ✅ Pedidos com Cupom: `5` (badge verde)
- ✅ Total Desconto: `R$ 25,00` (azul, negrito)

### Exemplo Real (E-mail de Repasse - Pedido 1003)

**Backend retorna:**
```json
{
  "pedidoId": 1003,
  "valorRepassado": 33.47,
  "cupom": {
    "teveCupom": true,
    "codigoCupom": "BONUS",
    "valorDesconto": 5.00
  }
}
```

**Frontend exibe:**
- ✅ Pedido ID: `1003`
- ✅ Valor Repassado: `R$ 33,47` (não R$ 20,00)
- ✅ Cupom Utilizado: `SIM` (verde, negrito)
- ✅ Desconto: `R$ 5,00` (azul, negrito)

## ⚠️ Breaking Changes

### ⚠️ ATENÇÃO: Mudança Breaking!

O campo `valorTotalConfirmado` foi **removido** e substituído por `valorRepassado`.

**Todas as referências foram atualizadas:**
- ✅ Modelo TypeScript atualizado
- ✅ Template HTML atualizado (desktop e mobile)
- ✅ Nenhuma referência restante a `valorTotalConfirmado`

## 📝 Notas Importantes

1. **Valor Repassado vs Valor Total:**
   - **Valor Total:** Soma bruta dos itens (`oi.quantity * oi.price`)
   - **Valor Repassado:** Valor real após taxas (`pp.amount_net`)
   - **Sempre usar Valor Repassado** para exibir ao autor

2. **Cupom em E-mails de Clientes:**
   - É uma **agregação** de todos os pedidos confirmados do cliente
   - `pedidosComCupom`: Quantidade de pedidos que tiveram cupom
   - `totalDesconto`: Soma de todos os descontos aplicados

3. **Cupom em E-mails de Repasse:**
   - É **específico do pedido** daquele e-mail
   - `teveCupom`: true/false se aquele pedido específico teve cupom
   - `codigoCupom`: Código do cupom usado naquele pedido
   - `valorDesconto`: Desconto aplicado naquele pedido específico

---

**Última atualização:** Novembro 2025

