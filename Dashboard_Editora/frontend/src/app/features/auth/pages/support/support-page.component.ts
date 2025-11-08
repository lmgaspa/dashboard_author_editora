import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-support-page',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="min-h-screen w-full relative overflow-hidden bg-gradient-to-br from-[#eef2ff] via-[#f8fbff] to-white flex items-center justify-center px-4 py-10">
      <!-- Orbs -->
      <div class="absolute inset-0 pointer-events-none overflow-hidden">
        <div class="absolute w-[420px] h-[420px] -top-[210px] -left-[210px] rounded-full bg-gradient-radial from-sky-200/60 via-sky-100/30 to-transparent blur-[120px] animate-float"></div>
        <div class="absolute w-[360px] h-[360px] top-1/2 right-1/5 -translate-y-1/2 rounded-full bg-gradient-radial from-indigo-200/60 via-indigo-100/25 to-transparent blur-[120px] animate-float" style="animation-delay: -6s;"></div>
        <div class="absolute w-[400px] h-[400px] -bottom-[200px] -right-[220px] rounded-full bg-gradient-radial from-rose-200/50 via-rose-100/20 to-transparent blur-[120px] animate-float" style="animation-delay: -12s;"></div>
      </div>

      <div class="relative z-10 max-w-3xl w-full bg-white/75 backdrop-blur-xl border border-pink-200 rounded-3xl shadow-[0_20px_60px_rgba(203,46,110,0.12)] px-6 sm:px-10 py-10 space-y-8">
        <header class="space-y-2 text-center">
          <h1 class="text-3xl sm:text-4xl font-black relative">
            <span class="relative z-10 bg-gradient-to-r from-[#ec4899] via-[#a855f7] to-[#6366f1] bg-clip-text text-transparent">
              Suporte & Ajuda — Painel dos Autores Via Litterarum
            </span>
            <span class="absolute inset-0 blur-xl opacity-50 bg-gradient-to-r from-[#ec4899] via-[#a855f7] to-[#6366f1] bg-clip-text text-transparent">
              Suporte & Ajuda — Painel dos Autores Via Litterarum
            </span>
          </h1>
          <p class="text-slate-600 text-sm sm:text-base">
            Problemas para entrar? Não recebeu o e-mail? Esqueceu a senha? Reunimos abaixo as soluções mais comuns.
            Se ainda precisar de ajuda, fale com a gente ao final da página.
          </p>
          <p class="text-xs uppercase tracking-wide text-slate-400">
            Atualizado em: 11/03/2025
          </p>
        </header>

        <section class="space-y-3">
          <h2 class="text-xl sm:text-2xl font-semibold text-slate-800 flex items-center gap-2">
            <span class="material-icons text-sky-500 text-2xl">login</span>
            1. Não consigo fazer login
          </h2>
          <p class="text-slate-600 text-sm sm:text-base">
            Antes de se preocupar, tente estas verificações rápidas:
          </p>
          <ul class="space-y-2 text-sm sm:text-base text-slate-600 list-disc pl-6">
            <li>Revise e-mail e senha — espaços extras ou vírgulas no final impedem o acesso.</li>
            <li>Veja se o CAPS LOCK está desligado.</li>
            <li>Se viu “E-mail não confirmado”, procure o link de confirmação que enviamos para o seu endereço.</li>
            <li>Usou “Continuar com Google” ao se cadastrar? Faça login novamente com Google; talvez você ainda não tenha criado uma senha.</li>
            <li>Já confirmou o e-mail e mesmo assim não entra? Use “Esqueci minha senha” para definir uma nova.</li>
          </ul>
        </section>

        <section class="space-y-3">
          <h2 class="text-xl sm:text-2xl font-semibold text-slate-800 flex items-center gap-2">
            <span class="material-icons text-sky-500 text-2xl">mark_email_unread</span>
            2. Não recebi o e-mail de confirmação
          </h2>
          <p class="text-slate-600 text-sm sm:text-base">
            O e-mail de confirmação garante que a caixa de entrada é sua. Se não chegou:
          </p>
          <ul class="space-y-2 text-sm sm:text-base text-slate-600 list-disc pl-6">
            <li>Olhe as abas Spam / Lixo / Promoções.</li>
            <li>Busque por “andescore” ou “confirmação” na sua caixa de entrada.</li>
            <li>Na tela “Verifique seu e-mail”, clique em “Reenviar confirmação”. Mandaremos um novo link.</li>
            <li>Se aparecer que você atingiu o limite, aguarde alguns minutos e tente novamente (protegemos sua caixa contra abuso).</li>
            <li>Sem retorno mesmo após reenviar? Entre em contato com a gente informando qual e-mail tentou cadastrar.</li>
          </ul>
        </section>

        <section class="space-y-3">
          <h2 class="text-xl sm:text-2xl font-semibold text-slate-800 flex items-center gap-2">
            <span class="material-icons text-sky-500 text-2xl">lock_reset</span>
            3. Esqueci minha senha
          </h2>
          <p class="text-slate-600 text-sm sm:text-base">
            Tudo bem, você pode redefinir em poucos cliques:
          </p>
          <ul class="space-y-2 text-sm sm:text-base text-slate-600 list-disc pl-6">
            <li>Acesse <strong>Esqueci minha senha</strong> e informe seu e-mail.</li>
            <li>Enviaremos um link seguro para criar uma nova senha.</li>
            <li>O link expira por segurança. Caso expire, solicite outro.</li>
            <li>Depois de redefinir, faça login normalmente. Não é preciso confirmar o e-mail novamente.</li>
          </ul>
        </section>

        <section class="space-y-3">
          <h2 class="text-xl sm:text-2xl font-semibold text-slate-800 flex items-center gap-2">
            <span class="material-icons text-sky-500 text-2xl">alternate_email</span>
            4. Troquei meu e-mail e agora não consigo entrar
          </h2>
          <p class="text-slate-600 text-sm sm:text-base">
            Ao solicitar a troca de e-mail, enviamos um link de confirmação para o novo endereço.
          </p>
          <ul class="space-y-2 text-sm sm:text-base text-slate-600 list-disc pl-6">
            <li>Procure por “Confirmar alteração de e-mail” na nova caixa de entrada.</li>
            <li>Clique no link para concluir. Depois disso, use o novo e-mail para entrar.</li>
            <li>Não clicou a tempo? O link pode expirar — peça outra alteração nas configurações da conta.</li>
            <li>Se alguém solicitou a troca sem sua autorização, ignore o e-mail e nos avise imediatamente.</li>
          </ul>
        </section>

        <section class="space-y-3">
          <h2 class="text-xl sm:text-2xl font-semibold text-slate-800 flex items-center gap-2">
            <span class="material-icons text-sky-500 text-2xl">shield</span>
            5. Segurança básica
          </h2>
          <ul class="space-y-2 text-sm sm:text-base text-slate-600 list-disc pl-6">
            <li>Nunca pediremos sua senha em texto puro nem frase-semente de wallet. Não compartilhe isso com ninguém.</li>
            <li>Só confie em links que comecem com <strong>andescoresoftware.com</strong>.</li>
            <li>Se algo parecer suspeito (erros grosseiros, ameaças urgentes, “confirme agora ou perca acesso”), pare e fale conosco antes de clicar.</li>
            <li>Suspeitou de acesso indevido? Redefina sua senha imediatamente e nos informe.</li>
          </ul>
        </section>

        <section class="space-y-3 border-t border-pink-100 pt-6">
          <h2 class="text-xl sm:text-2xl font-semibold text-slate-800 flex items-center gap-2">
            <span class="material-icons text-pink-500 text-2xl">support_agent</span>
            Ainda precisa de ajuda?
          </h2>
          <p class="text-slate-600 text-sm sm:text-base">
            Estamos disponíveis para dúvidas sobre acesso, segurança e qualquer assunto relacionado ao seu login ou wallet.
          </p>
          <p class="text-slate-600 text-sm sm:text-base">Ao falar com a gente, inclua:</p>
          <ul class="space-y-2 text-sm sm:text-base text-slate-600 list-disc pl-6">
            <li>O e-mail utilizado;</li>
            <li>O que você estava fazendo (login, redefinir senha, confirmar e-mail...);</li>
            <li>A mensagem que apareceu na tela (ex.: “E-mail não confirmado”, “Token inválido” etc.).</li>
          </ul>
          <div class="rounded-xl border border-pink-100 bg-rose-50/70 px-4 py-3">
            <p class="font-semibold text-pink-700">Suporte do Painel Editora dos Autores da Via Litterarum</p>
            <p class="text-pink-600 text-sm sm:text-base flex items-center gap-2 mt-1">
              <span class="material-icons text-base">email</span>
              <a href="mailto:andescoresoftware@gmail.com" class="text-pink-600 hover:text-pink-700 transition-colors duration-200">
                andescoresoftware@gmail.com
              </a>
            </p>
            <p class="text-slate-500 text-xs sm:text-sm mt-2">
              Nós respondemos por ordem de chegada. Nunca envie senhas ou frases-semente — nós jamais solicitaremos esses dados.
            </p>
          </div>
        </section>
      </div>
    </div>
  `,
  styles: [`
    .animate-float {
      animation: float 14s ease-in-out infinite;
    }

    @keyframes float {
      0%, 100% { transform: translateY(0px); }
      50% { transform: translateY(-20px); }
    }
  `]
})
export class SupportPageComponent {}


