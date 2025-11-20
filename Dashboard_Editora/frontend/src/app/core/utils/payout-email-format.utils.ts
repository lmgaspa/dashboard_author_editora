/**
 * Utilitários para formatação de informações de cupom em e-mails de repasse
 */

import { CouponInfoPayout } from '../models/email.model';
import { formatCurrency } from './charge.utils';

/**
 * Retorna o texto a ser exibido para "Cupom Utilizado"
 * Regra: "NÃO" se não tiver cupom, "SIM" se tiver
 */
export function getCupomUtilizadoText(cupom?: CouponInfoPayout | null): string {
  return cupom?.teveCupom ? 'SIM' : 'NÃO';
}

/**
 * Retorna o valor formatado do desconto
 * Regra: "R$ 0,00" se não tiver cupom, "R$ X,XX" se tiver
 */
export function getDescontoText(cupom?: CouponInfoPayout | null): string {
  if (cupom?.teveCupom && cupom.valorDesconto > 0) {
    return formatCurrency(cupom.valorDesconto);
  }
  return 'R$ 0,00';
}

/**
 * Retorna as classes CSS apropriadas para estilização do cupom
 */
export function getCupomUtilizadoClasses(cupom?: CouponInfoPayout | null): {
  textClass: string;
  badgeClass: string;
} {
  if (cupom?.teveCupom) {
    return {
      textClass: 'text-emerald-300 font-bold',
      badgeClass: 'bg-emerald-500/20 border-emerald-500/30 text-emerald-300'
    };
  }
  return {
    textClass: 'text-gray-500',
    badgeClass: 'bg-gray-500/20 border-gray-500/30 text-gray-500'
  };
}

/**
 * Retorna as classes CSS apropriadas para estilização do desconto
 */
export function getDescontoClasses(cupom?: CouponInfoPayout | null): {
  textClass: string;
} {
  if (cupom?.teveCupom && cupom.valorDesconto > 0) {
    return {
      textClass: 'text-blue-400 font-semibold'
    };
  }
  return {
    textClass: 'text-gray-500'
  };
}

