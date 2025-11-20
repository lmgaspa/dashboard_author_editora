/**
 * Utilitários para formatação e manipulação de pedidos e clientes
 */

/**
 * Gera link do WhatsApp
 */
export function getWhatsAppLink(phone: string): string {
  const cleanPhone = phone.replace(/\D/g, '');
  return `https://wa.me/${cleanPhone}`;
}

/**
 * Formata CPF
 */
export function formatCPF(cpf: string): string {
  // Remove caracteres não numéricos
  const clean = cpf.replace(/\D/g, '');
  
  // Formata: XXX.XXX.XXX-XX
  if (clean.length === 11) {
    return clean.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, '$1.$2.$3-$4');
  }
  
  // Se já estiver formatado ou não tiver 11 dígitos, retorna como está
  return cpf;
}

/**
 * Formata telefone/WhatsApp
 */
export function formatPhone(phone: string): string {
  // Remove caracteres não numéricos
  const clean = phone.replace(/\D/g, '');
  
  // Formata baseado no tamanho
  if (clean.length === 11) {
    // Celular: (XX) XXXXX-XXXX
    return clean.replace(/(\d{2})(\d{5})(\d{4})/, '($1) $2-$3');
  } else if (clean.length === 10) {
    // Fixo: (XX) XXXX-XXXX
    return clean.replace(/(\d{2})(\d{4})(\d{4})/, '($1) $2-$3');
  }
  
  // Se não tiver tamanho padrão, retorna como está
  return phone;
}

