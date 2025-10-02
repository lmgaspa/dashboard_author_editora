export type FunnelStep = { name: string; value: number };

// Mês atual (mock)
export const MOCK_FUNIL_ATUAL: FunnelStep[] = [
  { name: 'Visitas',             value: 10000 },
  { name: 'Carrinho',            value: 7000  },
  { name: 'Checkout',            value: 6300  },
  { name: 'Pagamento Aprovado',  value: 4200  },
];

// Mês passado (mock) — usado SEMPRE para comparação
export const MOCK_FUNIL_MES_PASSADO: Partial<Record<
  'Visitas' | 'Carrinho' | 'Checkout' | 'Pagamento Aprovado',
  number
>> = {
  Visitas: 9400,              // +6%
  Carrinho: 7500,             // -7%
  Checkout: 6300,             //  0%
  'Pagamento Aprovado': 3900, // +8%
};
