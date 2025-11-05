import { Component, signal, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { AuthService } from '@/app/core/services/auth.service';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './login-page.component.html',
  styles: [`
    .bg-transition {
      transition: background 1s ease-in-out;
    }
    
    .bg-weak {
      background: linear-gradient(to top left, #020617 0%, #0a1220 30%, #0f172a 60%, #0a1220 100%);
    }
    
    .bg-medium {
      background: linear-gradient(to top left, #0a1220 0%, #0f172a 30%, #1e293b 60%, #0f172a 100%);
    }
    
    .bg-strong {
      background: linear-gradient(to top left, #0f172a 0%, #1e293b 30%, #334155 60%, #1e293b 100%);
    }

    /* Animação de cor dos orbs: Azul Marinho -> Azul Celeste -> Branco (3 segundos) */
    @keyframes colorTransition {
      0% {
        background: radial-gradient(circle, rgba(15, 23, 42, 0.4) 0%, rgba(15, 23, 42, 0.2) 50%, transparent 100%);
      }
      33% {
        background: radial-gradient(circle, rgba(56, 189, 248, 0.4) 0%, rgba(56, 189, 248, 0.2) 50%, transparent 100%);
      }
      66% {
        background: radial-gradient(circle, rgba(125, 211, 252, 0.4) 0%, rgba(125, 211, 252, 0.2) 50%, transparent 100%);
      }
      100% {
        background: radial-gradient(circle, rgba(255, 255, 255, 0.3) 0%, rgba(255, 255, 255, 0.15) 50%, transparent 100%);
      }
    }

    .animate-color-transition {
      animation: colorTransition 3s ease-in-out infinite;
    }
  `]
})
export class LoginPageComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly loginForm: FormGroup = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]]
  });

  readonly loading = signal<boolean>(false);
  readonly error = signal<string | null>(null);
  readonly showPassword = signal<boolean>(false);
  readonly colorIntensity = signal<'weak' | 'medium' | 'strong'>('weak');

  ngOnInit(): void {
    this.startColorTransition();
  }

  private startColorTransition(): void {
    setTimeout(() => {
      this.colorIntensity.set('medium');
    }, 1000);

    setTimeout(() => {
      this.colorIntensity.set('strong');
    }, 2000);
  }

  togglePassword(): void {
    this.showPassword.update(v => !v);
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      return;
    }

    this.loading.set(true);
    this.error.set(null);

    this.authService.login(this.loginForm.value).subscribe({
      next: (response) => {
        console.log('✅ Login bem-sucedido!');
        console.log('📦 Resposta completa:', response);
        
        const currentUser = this.authService.currentUser();
        console.log('👤 Usuário atual do service:', currentUser);
        console.log('🎭 Role do usuário:', currentUser?.role);
        
        const returnUrl = this.route.snapshot.queryParams['returnUrl'] || 
          (currentUser?.role === 'ADMIN' ? '/admin/dashboard' : '/user/dashboard');
        
        console.log('🚀 Redirecionando para:', returnUrl);
        
        // Pequeno delay para garantir que o estado foi atualizado
        setTimeout(() => {
          this.router.navigate([returnUrl]).then(
            (success) => {
              if (success) {
                console.log('✅ Redirecionamento bem-sucedido para:', returnUrl);
              } else {
                console.error('❌ Falha no redirecionamento');
              }
            }
          );
        }, 100);
      },
      error: (err) => {
        console.error('❌ Erro no login:', err);
        console.error('📋 Detalhes do erro:', {
          status: err.status,
          message: err.message,
          error: err.error
        });
        this.error.set(err.error?.message || 'Erro ao fazer login. Verifique suas credenciais.');
        this.loading.set(false);
      }
    });
  }

  loginWithGoogle(): void {
    this.authService.loginWithGoogle();
  }
}

