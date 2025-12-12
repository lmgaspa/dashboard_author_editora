import { Component, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-landing-page',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './landing-page.component.html',
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

    /* Gradiente do título com várias cores */
    .title-gradient-1 {
      background: linear-gradient(to right, 
        #2563eb 0%, 
        #3b82f6 20%, 
        #0ea5e9 40%, 
        #06b6d4 60%, 
        #14b8a6 80%, 
        #2dd4bf 100%);
      -webkit-background-clip: text;
      background-clip: text;
    }

    .title-gradient-2 {
      background: linear-gradient(to right, 
        #2dd4bf 0%, 
        #14b8a6 20%, 
        #06b6d4 40%, 
        #0ea5e9 60%, 
        #3b82f6 80%, 
        #2563eb 100%);
      -webkit-background-clip: text;
      background-clip: text;
    }
  `]
})
export class LandingPageComponent implements OnInit {
  readonly colorIntensity = signal<'weak' | 'medium' | 'strong'>('weak');

  ngOnInit(): void {
    // Mostra conteúdo imediatamente e muda background progressivamente
    this.startColorTransition();
  }

  private startColorTransition(): void {
    // Primeiro segundo - Fraco (já está como padrão)
    
    // Segundo segundo - Médio
    setTimeout(() => {
      this.colorIntensity.set('medium');
      console.log('Mudando para medium');
    }, 1000);

    // Terceiro segundo - Forte
    setTimeout(() => {
      this.colorIntensity.set('strong');
      console.log('Mudando para strong');
    }, 2000);
  }
}
