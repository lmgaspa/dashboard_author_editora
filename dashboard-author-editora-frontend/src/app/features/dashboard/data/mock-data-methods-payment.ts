// src/app/data/mock-data-methods-payment.ts
export type PaymentSlice = { name: 'Cartão' | 'PIX'; value: number };

export const METHODS_PAYMENT = {
  current: [
    { name: 'Cartão', value: 52 },
    { name: 'PIX', value: 48 },
  ] as PaymentSlice[],
  lastMonth: [
    { name: 'Cartão', value: 58 },
    { name: 'PIX', value: 42 },
  ] as PaymentSlice[],
};
