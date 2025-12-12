/**
 * Utilitários para formatação e manipulação de cobranças
 */

// Formatação monetária brasileira
export function formatCurrency(value: number): string {
  return new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL'
  }).format(value);
}

// Nome do mês em português
export function getMonthName(month: number): string {
  const months = [
    'Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho',
    'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro'
  ];
  return months[month - 1] || '';
}

// Formato de data brasileira
export function formatDate(date: string): string {
  return new Date(date).toLocaleDateString('pt-BR');
}

// Formato de data e hora brasileira
export function formatDateTime(date: string | null): string {
  if (!date) return '-';
  return new Date(date).toLocaleString('pt-BR');
}

// Verificar se está vencido
export function isOverdue(dueDate: string): boolean {
  return new Date(dueDate) < new Date();
}

// Calcular dias de atraso
export function calculateDaysOverdue(dueDate: string): number {
  const due = new Date(dueDate);
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  due.setHours(0, 0, 0, 0);
  const diffTime = today.getTime() - due.getTime();
  return Math.max(0, Math.ceil(diffTime / (1000 * 60 * 60 * 24)));
}

// Obter cor do badge de status
export function getStatusColor(status: string): string {
  switch (status) {
    case 'PENDING':
      return 'bg-yellow-500/20 text-yellow-600 dark:text-yellow-400';
    case 'PAID':
      return 'bg-green-500/20 text-green-600 dark:text-green-400';
    case 'OVERDUE':
      return 'bg-red-500/20 text-red-600 dark:text-red-400';
    case 'CANCELLED':
      return 'bg-gray-500/20 text-gray-600 dark:text-gray-400';
    default:
      return 'bg-gray-500/20 text-gray-600 dark:text-gray-400';
  }
}

// Obter texto do status em português
export function getStatusText(status: string): string {
  switch (status) {
    case 'PENDING':
      return 'Pendente';
    case 'PAID':
      return 'Paga';
    case 'OVERDUE':
      return 'Atrasada';
    case 'CANCELLED':
      return 'Cancelada';
    default:
      return status;
  }
}

// Formatar mês/ano
export function formatMonthYear(month: number, year: number): string {
  return `${getMonthName(month)}/${year}`;
}

