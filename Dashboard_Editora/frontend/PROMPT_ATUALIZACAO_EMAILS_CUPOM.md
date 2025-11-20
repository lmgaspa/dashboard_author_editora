# 📧 Prompt: Atualização de E-mails - Cupom e Valor Repassado

## 🎯 Objetivo

Atualizar o frontend para exibir corretamente:
1. **Valor Repassado** (não "Valor Total") em E-mails de Clientes
2. **Informações de Cupom** em E-mails de Clientes
3. **Valor Repassado correto** em E-mails de Repasse (já corrigido no backend)
4. **Informações de Cupom** em E-mails de Repasse (já corrigido no backend)

---

## ⚠️ IMPORTANTE: Mudanças no Backend

### Endpoint: `GET /api/v1/autor/emails/painel`

**Este endpoint foi atualizado e agora retorna dados diferentes!**

---

## 📊 Mudanças na Estrutura de Dados

### 1. E-mails de Clientes (`emailsClientes`)

#### ❌ Estrutura ANTIGA (NÃO USAR MAIS):

```typescript
interface ResumoEmailCliente {
  email: string;
  totalPedidos: number;
  totalPedidosConfirmados: number;
  valorTotalConfirmado: number;  // ← REMOVIDO!
  primeiroPedidoEm: string;
  ultimoPedidoEm: string;
}
```

#### ✅ Estrutura NOVA (USAR ESTA):

```typescript
interface ResumoEmailCliente {
  email: string;
  totalPedidos: number;
  totalPedidosConfirmados: number;
  valorRepassado: number;  // ← NOVO! Usa amount_net (valor real após taxas)
  primeiroPedidoEm: string;
  ultimoPedidoEm: string;
  cupom: CouponInfoCliente;  // ← NOVO! Informações de cupom
}

interface CouponInfoCliente {
  pedidosComCupom: number;      // Quantidade de pedidos confirmados com cupom
  totalDesconto: number;        // Soma total de descontos aplicados
}
```

**Mudanças:**
- ❌ `valorTotalConfirmado` → ✅ `valorRepassado` (nome diferente + valor diferente)
- ✅ Adicionado campo `cupom` com informações agregadas

### 2. E-mails de Repasse (`emailsRepasse`)

#### ✅ Estrutura (JÁ ATUALIZADA NO BACKEND):

```typescript
interface ResumoEmailRepasse {
  id: number;
  pedidoId: number;
  repasseId: number | null;
  emailDestinatario: string;
  tipoEmail: string;
  status: string;
  enviadoEm: string;
  mensagemErro: string | null;
  valorRepassado: number;  // ← CORRIGIDO! Agora usa amount_net (valor real)
  cupom: CouponInfoPayout;  // ← NOVO! Informações de cupom
}

interface CouponInfoPayout {
  teveCupom: boolean;        // true se coupon_code IS NOT NULL
  codigoCupom: string | null; // o.coupon_code (pode ser null)
  valorDesconto: number;     // o.discount_amount (0 se não tiver cupom)
}
```

**Mudanças:**
- ✅ `valorRepassado` agora usa `amount_net` (valor real após taxas)
- ✅ Adicionado campo `cupom` com informações do pedido

---

## 🔍 Exemplo de Resposta do Backend

### Resposta Completa:

```json
{
  "emailsClientes": [
    {
      "email": "icazorla@uol.com.br",
      "totalPedidos": 15,
      "totalPedidosConfirmados": 9,
      "valorRepassado": 268.65,  // ← Valor real repassado (amount_net)
      "primeiroPedidoEm": "2025-11-12T10:00:00Z",
      "ultimoPedidoEm": "2025-11-18T15:02:00Z",
      "cupom": {
        "pedidosComCupom": 5,
        "totalDesconto": 25.00
      }
    },
    {
      "email": "luhmgasparetto@gmail.com",
      "totalPedidos": 162,
      "totalPedidosConfirmados": 19,
      "valorRepassado": 385.50,
      "primeiroPedidoEm": "2025-10-01T08:00:00Z",
      "ultimoPedidoEm": "2025-11-18T08:39:00Z",
      "cupom": {
        "pedidosComCupom": 0,
        "totalDesconto": 0.00
      }
    }
  ],
  "emailsRepasse": [
    {
      "id": 38,
      "pedidoId": 1003,
      "repasseId": 123,
      "emailDestinatario": "autor@example.com",
      "tipoEmail": "REPASSE_PIX",
      "status": "SENT",
      "enviadoEm": "2025-11-18T18:07:20Z",
      "mensagemErro": null,
      "valorRepassado": 33.47,  // ← Valor real (amount_net), não 20.00
      "cupom": {
        "teveCupom": true,
        "codigoCupom": "BONUS",
        "valorDesconto": 5.00
      }
    },
    {
      "id": 1,
      "pedidoId": 962,
      "repasseId": 45,
      "emailDestinatario": "autor@example.com",
      "tipoEmail": "REPASSE_PIX",
      "status": "SENT",
      "enviadoEm": "2025-11-15T10:34:00Z",
      "mensagemErro": null,
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

---

## 🛠️ Implementação no Frontend

### 1. Atualizar Types/Interfaces

#### `types/emails.ts`

```typescript
// E-mails de Clientes
export interface ResumoEmailCliente {
  email: string;
  totalPedidos: number;
  totalPedidosConfirmados: number;
  valorRepassado: number;  // ← MUDOU! Era "valorTotalConfirmado"
  primeiroPedidoEm: string;
  ultimoPedidoEm: string;
  cupom: CouponInfoCliente;  // ← NOVO!
}

export interface CouponInfoCliente {
  pedidosComCupom: number;
  totalDesconto: number;
}

// E-mails de Repasse
export interface ResumoEmailRepasse {
  id: number;
  pedidoId: number;
  repasseId: number | null;
  emailDestinatario: string;
  tipoEmail: string;
  status: string;
  enviadoEm: string;
  mensagemErro: string | null;
  valorRepassado: number;  // ← Já estava correto, mas agora usa amount_net
  cupom: CouponInfoPayout;  // ← NOVO!
}

export interface CouponInfoPayout {
  teveCupom: boolean;
  codigoCupom: string | null;
  valorDesconto: number;
}

// DTO Principal
export interface PainelEmailsAutor {
  emailsClientes: ResumoEmailCliente[];
  emailsRepasse: ResumoEmailRepasse[];
}
```

### 2. Atualizar Componente: E-mails de Clientes

#### `components/emails/EmailsClientesList.tsx`

```typescript
import React from 'react';
import { ResumoEmailCliente } from '@/types/emails';
import { formatCurrency, formatDate } from '@/utils/format';

interface EmailsClientesListProps {
  emails: ResumoEmailCliente[];
}

export function EmailsClientesList({ emails }: EmailsClientesListProps) {
  return (
    <div className="emails-clientes-list">
      <h2>E-mails de Clientes</h2>
      <table>
        <thead>
          <tr>
            <th>E-mail</th>
            <th>Total de Pedidos</th>
            <th>Confirmados</th>
            <th>Valor Repassado</th>  {/* ← MUDOU! Era "Valor Total" */}
            <th>Pedidos com Cupom</th>  {/* ← NOVO! */}
            <th>Total Desconto</th>  {/* ← NOVO! */}
            <th>Último Pedido</th>
          </tr>
        </thead>
        <tbody>
          {emails.map((email) => (
            <tr key={email.email}>
              <td>{email.email}</td>
              <td>{email.totalPedidos}</td>
              <td>
                {email.totalPedidosConfirmados} (
                {Math.round((email.totalPedidosConfirmados / email.totalPedidos) * 100)}%)
              </td>
              <td className="valor-repassado">
                {formatCurrency(email.valorRepassado)}  {/* ← MUDOU! Era valorTotalConfirmado */}
              </td>
              <td>
                {email.cupom.pedidosComCupom > 0 ? (
                  <span className="cupom-sim">
                    {email.cupom.pedidosComCupom}
                  </span>
                ) : (
                  <span className="cupom-nao">0</span>
                )}
              </td>
              <td>
                {email.cupom.totalDesconto > 0 ? (
                  <span className="desconto-valor">
                    {formatCurrency(email.cupom.totalDesconto)}
                  </span>
                ) : (
                  <span className="desconto-zero">R$ 0,00</span>
                )}
              </td>
              <td>{formatDate(email.ultimoPedidoEm)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
```

### 3. Atualizar Componente: E-mails de Repasse

#### `components/emails/PayoutEmailsList.tsx`

```typescript
import React from 'react';
import { ResumoEmailRepasse } from '@/types/emails';
import { formatCurrency, formatDate } from '@/utils/format';
import { getCupomUtilizadoText, getDescontoText, getCupomUtilizadoClasses } from '@/utils/payoutEmailFormat';
import { CheckCircle, XCircle } from 'lucide-react';

interface PayoutEmailsListProps {
  emails: ResumoEmailRepasse[];
}

export function PayoutEmailsList({ emails }: PayoutEmailsListProps) {
  return (
    <div className="payout-emails-list">
      <h2>E-mails de Repasse de PIX</h2>
      <table className="table">
        <thead>
          <tr>
            <th>Tipo</th>
            <th>Status</th>
            <th>Enviado Em</th>
            <th>Pedido ID</th>
            <th>Valor Repassado</th>  {/* ← Já estava correto, mas agora mostra valor real */}
            <th>Cupom Utilizado</th>  {/* ← NOVO! */}
            <th>Desconto</th>  {/* ← NOVO! */}
          </tr>
        </thead>
        <tbody>
          {emails.map((email) => {
            const cupomClasses = getCupomUtilizadoClasses(email.cupom);
            return (
              <tr key={email.id}>
                <td>{email.tipoEmail}</td>
                <td>
                  {email.status === 'SENT' ? (
                    <span className="status-sent">
                      <CheckCircle className="icon-check" />
                      SENT
                    </span>
                  ) : (
                    <span className="status-failed">
                      <XCircle className="icon-cancel" />
                      FAILED
                    </span>
                  )}
                </td>
                <td>{formatDate(email.enviadoEm)}</td>
                <td>{email.pedidoId}</td>
                <td className="valor-repassado">
                  {email.valorRepassado 
                    ? formatCurrency(email.valorRepassado)  // ← Agora mostra valor real (amount_net)
                    : '-'}
                </td>
                <td>
                  <span className={cupomClasses.textClass}>
                    {email.cupom.teveCupom ? 'SIM' : 'NÃO'}
                  </span>
                </td>
                <td>
                  {email.cupom.teveCupom ? (
                    <strong className="desconto-valor">
                      {formatCurrency(email.cupom.valorDesconto)}
                    </strong>
                  ) : (
                    <span className="desconto-zero">R$ 0,00</span>
                  )}
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
```

### 4. Utilitários (Já Existem, Mas Verificar)

#### `utils/payoutEmailFormat.ts`

```typescript
import { CouponInfoPayout } from '@/types/emails';
import { formatCurrency } from './format';

/**
 * Retorna o texto a ser exibido para "Cupom Utilizado"
 * Regra: "NÃO" se não tiver cupom, "SIM" se tiver
 */
export function getCupomUtilizadoText(cupom: CouponInfoPayout): string {
  return cupom.teveCupom ? 'SIM' : 'NÃO';
}

/**
 * Retorna o valor formatado do desconto
 * Regra: "R$ 0,00" se não tiver cupom, "R$ X,XX" se tiver
 */
export function getDescontoText(cupom: CouponInfoPayout): string {
  if (cupom.teveCupom) {
    return formatCurrency(cupom.valorDesconto);
  }
  return 'R$ 0,00';
}

/**
 * Retorna as classes CSS apropriadas para estilização
 */
export function getCupomUtilizadoClasses(cupom: CouponInfoPayout): {
  textClass: string;
  badgeClass: string;
} {
  if (cupom.teveCupom) {
    return {
      textClass: 'cupom-sim',
      badgeClass: 'badge-success',
    };
  }
  return {
    textClass: 'cupom-nao',
    badgeClass: 'badge-secondary',
  };
}
```

---

## 🎨 Regras de Exibição

### E-mails de Clientes

#### Campo "Valor Repassado"
- **Label:** "Valor Repassado" (não "Valor Total" ou "Valor Total Confirmado")
- **Valor:** Usar `email.valorRepassado` (valor real após taxas)
- **Formato:** `formatCurrency(email.valorRepassado)`

#### Campo "Pedidos com Cupom"
- **Se `cupom.pedidosComCupom > 0`:**
  - Exibir: número em destaque (ex: cor verde)
  - Exemplo: `5`
- **Se `cupom.pedidosComCupom = 0`:**
  - Exibir: `0` (sem destaque)

#### Campo "Total Desconto"
- **Se `cupom.totalDesconto > 0`:**
  - Exibir: `formatCurrency(cupom.totalDesconto)` (ex: "R$ 25,00")
  - Estilo: destaque (ex: cor azul ou negrito)
- **Se `cupom.totalDesconto = 0`:**
  - Exibir: `"R$ 0,00"` (sem destaque)

### E-mails de Repasse

#### Campo "Valor Repassado"
- **Label:** "Valor Repassado"
- **Valor:** Usar `email.valorRepassado` (agora usa `amount_net`, valor real)
- **Formato:** `formatCurrency(email.valorRepassado)`
- **Nota:** O valor agora está correto (usa `amount_net` ao invés de cálculo bruto)

#### Campo "Cupom Utilizado"
- **Se `cupom.teveCupom = true`:**
  - Exibir: **"SIM"** (texto em negrito ou com destaque visual, ex: cor verde)
- **Se `cupom.teveCupom = false`:**
  - Exibir: **"NÃO"** (texto simples, sem destaque)

#### Campo "Desconto"
- **Se `cupom.teveCupom = true`:**
  - Exibir: **`formatCurrency(cupom.valorDesconto)`** (ex: "R$ 5,00")
  - Estilo: destaque (ex: cor azul ou negrito)
- **Se `cupom.teveCupom = false`:**
  - Exibir: **"R$ 0,00"** (sem destaque)

---

## ✅ Checklist de Implementação

### Types/Interfaces:
- [ ] Atualizar `ResumoEmailCliente`:
  - [ ] Remover `valorTotalConfirmado`
  - [ ] Adicionar `valorRepassado`
  - [ ] Adicionar campo `cupom: CouponInfoCliente`
- [ ] Criar `CouponInfoCliente` interface
- [ ] Atualizar `ResumoEmailRepasse`:
  - [ ] Adicionar campo `cupom: CouponInfoPayout` (se ainda não tiver)
- [ ] Verificar se `CouponInfoPayout` já existe

### Componentes:
- [ ] Atualizar `EmailsClientesList`:
  - [ ] Mudar label de "Valor Total" para "Valor Repassado"
  - [ ] Usar `email.valorRepassado` ao invés de `email.valorTotalConfirmado`
  - [ ] Adicionar coluna "Pedidos com Cupom"
  - [ ] Adicionar coluna "Total Desconto"
  - [ ] Aplicar estilos para destacar quando há cupom
- [ ] Atualizar `PayoutEmailsList`:
  - [ ] Verificar se já exibe "Cupom Utilizado" e "Desconto"
  - [ ] Verificar se `valorRepassado` está sendo exibido corretamente

### Utilitários:
- [ ] Verificar se `payoutEmailFormat.ts` existe e está correto
- [ ] Criar utilitários para `CouponInfoCliente` se necessário

### Testes:
- [ ] Testar com e-mails que têm cupom
- [ ] Testar com e-mails que não têm cupom
- [ ] Verificar se valores estão corretos (comparar com backend)
- [ ] Verificar se formatação está correta

---

## 🔍 Validação: Comparar com Backend

### Exemplo Real (icazorla@uol.com.br):

**Backend retorna:**
```json
{
  "email": "icazorla@uol.com.br",
  "totalPedidos": 15,
  "totalPedidosConfirmados": 9,
  "valorRepassado": 268.65,  // ← Valor real (amount_net)
  "cupom": {
    "pedidosComCupom": 5,
    "totalDesconto": 25.00
  }
}
```

**Frontend deve exibir:**
- E-mail: `icazorla@uol.com.br`
- Total de Pedidos: `15`
- Confirmados: `9 (60%)`
- **Valor Repassado:** `R$ 268,65` ← (não R$ 220,00)
- **Pedidos com Cupom:** `5` ← (em destaque)
- **Total Desconto:** `R$ 25,00` ← (em destaque)

### Exemplo Real (E-mail de Repasse - Pedido 1003):

**Backend retorna:**
```json
{
  "id": 38,
  "pedidoId": 1003,
  "valorRepassado": 33.47,  // ← Valor real (amount_net)
  "cupom": {
    "teveCupom": true,
    "codigoCupom": "BONUS",
    "valorDesconto": 5.00
  }
}
```

**Frontend deve exibir:**
- Pedido ID: `1003`
- **Valor Repassado:** `R$ 33,47` ← (não R$ 20,00)
- **Cupom Utilizado:** `SIM` ← (em destaque, cor verde)
- **Desconto:** `R$ 5,00` ← (em destaque)

---

## ⚠️ Breaking Changes

### ⚠️ ATENÇÃO: Mudança Breaking!

O campo `valorTotalConfirmado` foi **removido** e substituído por `valorRepassado`.

**Se o frontend ainda usa `valorTotalConfirmado`, vai dar erro!**

**Ação necessária:**
1. Buscar e substituir todas as ocorrências de `valorTotalConfirmado` por `valorRepassado`
2. Atualizar labels de "Valor Total" para "Valor Repassado"
3. Adicionar campos de cupom nos componentes

---

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

