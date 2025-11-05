/**
 * Utilitário para trabalhar com cookies
 */

/**
 * Lê o valor de um cookie pelo nome
 * @param name Nome do cookie
 * @returns Valor do cookie ou null se não encontrado
 */
export function getCookie(name: string): string | null {
  const cookies = document.cookie.split(';');
  const cookie = cookies.find(c => c.trim().startsWith(`${name}=`));
  
  if (cookie) {
    return cookie.split('=')[1];
  }
  
  return null;
}

/**
 * Lê o token CSRF do cookie
 * @returns Token CSRF ou null se não encontrado
 */
export function getCsrfTokenFromCookie(): string | null {
  return getCookie('csrf_token');
}

/**
 * Verifica se um cookie existe
 * @param name Nome do cookie
 * @returns true se o cookie existe, false caso contrário
 */
export function hasCookie(name: string): boolean {
  return getCookie(name) !== null;
}

