# 🎨 PROMPT PARA O FRONTEND - MÓDULO "PAGAMENTOS DO AUTOR"

## 📋 Contexto

Você é um Engenheiro Frontend trabalhando no painel do autor da editora. O backend já implementou um módulo simples de pagamentos que retorna dados em português, fáceis de entender para escritores leigos.

**Endpoint disponível:** `GET /api/v1/autor/pagamentos/painel`

**Autenticação:** Requer JWT token no header `Authorization: Bearer <token>`

**Permissão:** Apenas usuários com role `USER` ou `ADMIN` podem acessar.

---

## 📡 Estrutura da API

### Endpoint
```
GET /api/v1/autor/pagamentos/painel
```

### Headers necessários
```
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json
```

### Resposta de sucesso (200 OK)
```json
{
  "resumo": {
    "autorId": 1,
    "nomeAutor": "Agenor Gasparetto",
    "valorVendasConfirmadas": 630.00,
    "valorJaRecebido": 0.0,
    "valorAReceber": 630.00
  },
  "funilVendas": {
    "totalPedidos": 50,
    "pedidosConfirmados": 35,
    "pedidosEmAndamento": 10,
    "pedidosCancelados": 5,
    "taxaConversao": 70.0,
    "valorTotalPedidos": 850.00,
    "valorConfirmado": 630.00,
    "valorEmAndamento": 150.00
  },
  "vendasRecentes": [
    {
      "pedidoId": 123,
      "dataPedido": "2025-01-15T10:30:00Z",
      "tituloLivro": "O Livro do Autor",
      "quantidade": 2,
      "valorTotal": 120.00,
      "statusLegivel": "Pago"
    },
    {
      "pedidoId": 122,
      "dataPedido": "2025-01-14T14:20:00Z",
      "tituloLivro": "Outro Livro",
      "quantidade": 1,
      "valorTotal": 50.00,
      "statusLegivel": "Pago"
    }
  ]
}
```

### Respostas de erro

**401 Unauthorized:**
```json
{
  "message": "Usuário não encontrado"
}
```

**403 Forbidden:**
```json
{
  "message": "Usuário não possui author_id configurado. Entre em contato com o administrador."
}
```

**404 Not Found:**
```json
{
  "message": "Autor não encontrado ou sem dados no e-commerce"
}
```

**500 Internal Server Error:**
```json
{
  "message": "Erro ao buscar informações de pagamentos: <detalhes>"
}
```

---

## 🎯 Objetivo da Interface

Criar uma **interface simples e clara** para que um escritor leigo possa entender facilmente:
- **Quanto ele já vendeu** (vendas confirmadas)
- **Quanto ele já recebeu** (por enquanto 0, mas preparado para futuro)
- **Quanto ainda vai receber** (valor a receber)
- **Funil de vendas** (quantos pedidos em cada etapa)
- **Vendas recentes** (lista das últimas vendas)

---

## 📐 Estrutura da Página

### 1. Seção de Resumo (Cards no topo)

Exibir 3 cards principais:

#### Card 1: "Vendas Confirmadas"
- **Valor grande e destacado:** `resumo.valorVendasConfirmadas`
- **Formato:** R$ 630,00 (formato brasileiro)
- **Descrição:** "Total de vendas já confirmadas"
- **Ícone:** 📈 (tendência positiva, verde)

#### Card 2: "Já Recebido"
- **Valor grande e destacado:** `resumo.valorJaRecebido`
- **Formato:** R$ 0,00 (formato brasileiro)
- **Descrição:** "Valor já recebido"
- **Ícone:** 💰 (dinheiro, amarelo/dourado)
- **Nota:** Por enquanto será sempre 0, mas deixar preparado

#### Card 3: "A Receber"
- **Valor grande e destacado:** `resumo.valorAReceber`
- **Formato:** R$ 630,00 (formato brasileiro)
- **Descrição:** "Valor ainda a receber"
- **Ícone:** ⏳ (pendente, laranja)
- **Destaque:** Se o valor for alto, destacar visualmente

### 2. Seção de Funil de Vendas

Exibir um **funil visual simples** com:

#### Opção A: Cards horizontais
```
┌─────────────────────────────────────────────────────────┐
│  📊 Funil de Vendas                                     │
├─────────────────────────────────────────────────────────┤
│  Total de Pedidos: 50                                   │
│  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ │
│                                                          │
│  ✅ Confirmados: 35 (R$ 630,00)                         │
│  ⏳ Em Andamento: 10 (R$ 150,00)                        │
│  ❌ Cancelados: 5                                        │
│                                                          │
│  Taxa de Conversão: 70%                                 │
└─────────────────────────────────────────────────────────┘
```

#### Opção B: Barras de progresso
```
Total de Pedidos: 50

✅ Confirmados: 35 ████████████████░░░░░░░ 70%
⏳ Em Andamento: 10 █████░░░░░░░░░░░░░░░░ 20%
❌ Cancelados: 5   ██░░░░░░░░░░░░░░░░░░░ 10%
```

**Valores a usar:**
- `funilVendas.totalPedidos` - Total de pedidos
- `funilVendas.pedidosConfirmados` - Pedidos confirmados
- `funilVendas.pedidosEmAndamento` - Pedidos em andamento
- `funilVendas.pedidosCancelados` - Pedidos cancelados
- `funilVendas.taxaConversao` - Taxa de conversão (%)
- `funilVendas.valorConfirmado` - Valor dos confirmados
- `funilVendas.valorEmAndamento` - Valor dos em andamento

**Cores sugeridas:**
- Confirmados: Verde (#10B981)
- Em Andamento: Laranja (#F59E0B)
- Cancelados: Vermelho (#EF4444)

### 3. Seção de Vendas Recentes

Exibir uma **tabela simples** ou **lista de cards** com as últimas vendas:

#### Formato de Tabela
```
┌────────────────────────────────────────────────────────────┐
│  📋 Vendas Recentes                                        │
├──────┬─────────────┬────────────────┬──────────┬───────────┤
│ Data │ Livro       │ Quantidade     │ Valor    │ Status    │
├──────┼─────────────┼────────────────┼──────────┼───────────┤
│ 15/01│ O Livro...  │ 2              │ R$ 120,00│ ✅ Pago   │
│ 14/01│ Outro Livro │ 1              │ R$ 50,00 │ ✅ Pago   │
└──────┴─────────────┴────────────────┴──────────┴───────────┘
```

#### Campos da tabela:
- **Data:** Formatar `venda.dataPedido` como "DD/MM/YYYY" (português)
- **Livro:** `venda.tituloLivro` (truncar se muito longo, ex: "O Livro do Autor...")
- **Quantidade:** `venda.quantidade`
- **Valor:** `venda.valorTotal` formatado como R$ X,XX
- **Status:** `venda.statusLegivel` com ícone:
  - "Pago" → ✅ Verde
  - "Em andamento" → ⏳ Laranja
  - "Cancelado" → ❌ Vermelho

#### Limitação:
- Mostrar apenas as últimas 20 vendas (já vem do backend)
- Se houver mais vendas, pode adicionar botão "Ver todas" (futuro)

---

## 🎨 Design e UX

### Cores sugeridas
- **Primária:** Azul (#3B82F6) - para elementos principais
- **Sucesso:** Verde (#10B981) - para valores positivos
- **Atenção:** Laranja (#F59E0B) - para pendências
- **Erro:** Vermelho (#EF4444) - para cancelados
- **Background:** Branco/Cinza claro (#F9FAFB)

### Tipografia
- **Títulos:** Negrito, tamanho médio-grande
- **Valores:** Negrito, tamanho grande (destaque)
- **Texto normal:** Regular, tamanho padrão

### Responsividade
- **Desktop:** Layout em grid (3 colunas para cards, 2 colunas para funil)
- **Tablet:** Layout em grid (2 colunas)
- **Mobile:** Stack vertical (1 coluna)

### Estados da página

#### 1. Loading
- Exibir skeleton/loading enquanto carrega os dados
- Mostrar spinner no centro da página

#### 2. Sucesso
- Exibir todos os dados conforme descrito acima

#### 3. Erro
- Exibir mensagem de erro amigável
- Botão "Tentar novamente"
- Se for erro de autorização, orientar a entrar em contato com admin

#### 4. Vazio
- Se não houver vendas, exibir mensagem: "Ainda não há vendas registradas"

---

## 📝 Checklist de Implementação

### Componentes necessários
- [ ] `PagamentosAutorPage` - Página principal
- [ ] `ResumoCards` - Cards de resumo (3 cards)
- [ ] `FunilVendas` - Seção do funil de vendas
- [ ] `VendasRecentes` - Tabela/lista de vendas recentes
- [ ] `LoadingSkeleton` - Skeleton para loading
- [ ] `ErrorMessage` - Componente de erro

### Funcionalidades
- [ ] Fazer chamada GET para `/api/v1/autor/pagamentos/painel`
- [ ] Tratar erros (401, 403, 404, 500)
- [ ] Formatar valores em Real brasileiro (R$ X,XX)
- [ ] Formatar datas em português (DD/MM/YYYY)
- [ ] Exibir loading enquanto carrega
- [ ] Responsividade (mobile, tablet, desktop)

### Validações
- [ ] Verificar se usuário está autenticado antes de fazer a requisição
- [ ] Tratar caso onde `vendasRecentes` esteja vazio
- [ ] Tratar caso onde valores sejam 0 ou null

### Acessibilidade
- [ ] Adicionar labels descritivos para valores
- [ ] Usar cores com bom contraste
- [ ] Suporte a leitores de tela (aria-labels)

---

## 🔧 Exemplo de Código (Angular/TypeScript)

```typescript
// services/pagamentos.service.ts
export interface PagamentosAutorResumo {
  autorId: number;
  nomeAutor: string;
  valorVendasConfirmadas: number;
  valorJaRecebido: number;
  valorAReceber: number;
}

export interface FunilVendas {
  totalPedidos: number;
  pedidosConfirmados: number;
  pedidosEmAndamento: number;
  pedidosCancelados: number;
  taxaConversao: number;
  valorTotalPedidos: number;
  valorConfirmado: number;
  valorEmAndamento: number;
}

export interface VendaRecente {
  pedidoId: number;
  dataPedido: string; // ISO string
  tituloLivro: string;
  quantidade: number;
  valorTotal: number;
  statusLegivel: string;
}

export interface PainelPagamentosAutor {
  resumo: PagamentosAutorResumo;
  funilVendas: FunilVendas;
  vendasRecentes: VendaRecente[];
}

// Método no service
getPainelPagamentos(): Observable<PainelPagamentosAutor> {
  return this.http.get<PainelPagamentosAutor>(
    `${this.apiUrl}/autor/pagamentos/painel`
  );
}
```

---

## 📌 Observações Importantes

1. **Formato de moeda:** Sempre usar formato brasileiro: R$ 1.234,56
2. **Formato de data:** Sempre usar formato brasileiro: DD/MM/YYYY
3. **Idioma:** Todos os textos devem estar em português
4. **Simplicidade:** Manter a interface simples e clara para escritores leigos
5. **Performance:** Cachear dados se necessário, mas não obrigatório
6. **Futuro:** Preparar para quando `valorJaRecebido` começar a vir com valores reais

---

**Data:** 2025-01-XX  
**Versão da API:** v1  
**Status:** ✅ Backend Implementado

