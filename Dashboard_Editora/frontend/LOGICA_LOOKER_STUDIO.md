# 📊 Lógica Completa: Integração Looker Studio por Author ID

## 🎯 Objetivo

Permitir que cada autor (usuário com `authorId`) tenha sua própria URL do Looker Studio configurada, exibindo métricas personalizadas baseadas em sua própria fonte de dados (Google Analytics, BigQuery, etc.).

---

## 🔄 Fluxo Completo

### **1. Setup Inicial (Admin no Looker Studio)**

```
┌─────────────────────────────────────────────────────────────┐
│ ADMIN cria relatório no Looker Studio                      │
│                                                             │
│ Passos:                                                     │
│ 1. Acessa lookerstudio.google.com                          │
│ 2. Cria novo relatório                                      │
│ 3. Conecta fonte de dados:                                  │
│    - Google Analytics (com chave do autor)                 │
│    - OU BigQuery (com credenciais do autor)                 │
│    - OU qualquer outra fonte                                │
│ 4. Configura filtros/parâmetros por author_id (se necessário)│
│ 5. Personaliza visualizações                                │
│ 6. Publica o relatório                                      │
│ 7. Clica em "Compartilhar" → "Incorporar relatório"       │
│ 8. Copia a URL de embed gerada                              │
│    Ex: https://lookerstudio.google.com/embed/...           │
└─────────────────────────────────────────────────────────────┘
```

### **2. Configuração no Sistema (Admin no Dashboard)**

```
┌─────────────────────────────────────────────────────────────┐
│ ADMIN acessa: /admin/users                                  │
│                                                             │
│ 1. Clica no ícone de editar (lápis) do usuário              │
│ 2. Modal de edição abre                                     │
│ 3. Preenche campos existentes:                              │
│    - authorId: "1"                                          │
│    - ecommerceUrl: "https://loja.autor.com"                 │
│    - ...                                                     │
│ 4. NOVO CAMPO aparece:                                      │
│    - lookerStudioUrl: "https://lookerstudio.google.com/..." │
│ 5. Cola a URL copiada do Looker Studio                      │
│ 6. Clica em "Salvar"                                        │
│ 7. Backend salva no banco de dados                         │
└─────────────────────────────────────────────────────────────┘
```

### **3. Visualização (Usuário/Autor)**

```
┌─────────────────────────────────────────────────────────────┐
│ AUTOR loga no sistema                                       │
│                                                             │
│ 1. Acessa menu lateral → "Métricas"                         │
│ 2. Frontend busca dados do usuário logado:                  │
│    - Pega token JWT do localStorage                          │
│    - Chama GET /api/v1/user/profile (ou similar)             │
│    - Backend retorna User com lookerStudioUrl                │
│ 3. Componente de métricas recebe:                             │
│    - currentUser().lookerStudioUrl                           │
│ 4. Se URL existe:                                            │
│    - Sanitiza URL (segurança Angular)                         │
│    - Renderiza iframe com URL                               │
│ 5. Se URL não existe:                                        │
│    - Mostra mensagem: "Métricas não configuradas"            │
│    - OU mostra URL padrão (fallback)                        │
└─────────────────────────────────────────────────────────────┘
```

---

## 🗄️ Estrutura de Dados

### **Backend - Tabela `users`**

```sql
-- Campo a ser adicionado
ALTER TABLE users 
ADD COLUMN looker_studio_url VARCHAR(500) NULL;

-- Exemplo de dados
id          | author_id | ecommerce_url              | looker_studio_url
------------|-----------|----------------------------|--------------------------------------------------
user-1      | 1         | https://loja1.com          | https://lookerstudio.google.com/embed/reporting/ABC123...
user-2      | 2         | https://loja2.com          | https://lookerstudio.google.com/embed/reporting/XYZ789...
user-3      | 3         | https://loja3.com          | NULL (não configurado ainda)
```

### **Frontend - Interface `User`**

```typescript
interface User {
  id: string;
  email: string;
  name: string;
  role: 'ADMIN' | 'USER';
  authorId?: string;
  ecommerceUrl?: string;
  lookerStudioUrl?: string | null;  // ✨ NOVO CAMPO
  // ... outros campos
}
```

### **Backend - DTOs**

```java
// UserListResponse (GET /api/v1/admin/users)
{
  "id": "user-1",
  "name": "João Silva",
  "email": "joao@example.com",
  "role": "USER",
  "authorId": "1",
  "ecommerceUrl": "https://loja1.com",
  "lookerStudioUrl": "https://lookerstudio.google.com/embed/reporting/ABC123...",
  // ... outros campos
}

// UpdateUserRequest (PUT /api/v1/admin/users/{id})
{
  "name": "João Silva",
  "authorId": "1",
  "ecommerceUrl": "https://loja1.com",
  "lookerStudioUrl": "https://lookerstudio.google.com/embed/reporting/ABC123...",  // ✨ NOVO CAMPO
  // ... outros campos opcionais
}
```

---

## 🔧 Implementação Detalhada

### **PARTE 1: Backend**

#### **1.1. Migration (PostgreSQL)**

```sql
-- Migration: V12__add_looker_studio_url_to_users.sql
ALTER TABLE users 
ADD COLUMN looker_studio_url VARCHAR(500) NULL;

COMMENT ON COLUMN users.looker_studio_url IS 
'URL de embed do relatório Looker Studio para visualização de métricas do autor';
```

#### **1.2. Entity (UserEntity.java)**

```java
@Entity
@Table(name = "users")
public class UserEntity {
    // ... campos existentes
    
    @Column(name = "looker_studio_url", length = 500)
    private String lookerStudioUrl;
    
    // Getters e Setters
    public String getLookerStudioUrl() {
        return lookerStudioUrl;
    }
    
    public void setLookerStudioUrl(String lookerStudioUrl) {
        this.lookerStudioUrl = lookerStudioUrl;
    }
}
```

#### **1.3. DTOs**

```java
// UserListResponse.java
@Data
public class UserListResponse {
    private String id;
    private String name;
    private String email;
    private String role;
    private String authorId;
    private String ecommerceUrl;
    private String lookerStudioUrl;  // ✨ NOVO
    // ... outros campos
}

// UpdateUserRequest.java
@Data
public class UpdateUserRequest {
    private String name;
    private String role;
    private String authorId;
    private String ecommerceUrl;
    private String lookerStudioUrl;  // ✨ NOVO (opcional)
    // ... outros campos
}
```

#### **1.4. Controller (AdminUsersController.java)**

```java
@PutMapping("/{identifier}")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<UserListResponse> updateUser(
    @PathVariable String identifier,
    @RequestBody UpdateUserRequest request
) {
    // ... lógica existente
    
    // NOVO: Atualizar lookerStudioUrl se fornecido
    if (request.getLookerStudioUrl() != null) {
        // Se string vazia, setar como null (remover)
        if (request.getLookerStudioUrl().trim().isEmpty()) {
            userEntity.setLookerStudioUrl(null);
        } else {
            // Validar URL antes de salvar
            if (isValidLookerStudioUrl(request.getLookerStudioUrl())) {
                userEntity.setLookerStudioUrl(request.getLookerStudioUrl().trim());
            } else {
                throw new BadRequestException("URL do Looker Studio inválida");
            }
        }
    }
    
    // ... salvar e retornar
}

private boolean isValidLookerStudioUrl(String url) {
    if (url == null || url.trim().isEmpty()) {
        return true; // null/empty é válido (remove campo)
    }
    // Validar se começa com https://lookerstudio.google.com/embed/
    return url.startsWith("https://lookerstudio.google.com/embed/") 
        || url.startsWith("https://datastudio.google.com/embed/"); // compatibilidade
}
```

#### **1.5. Endpoint de Perfil (UserController.java)**

```java
@GetMapping("/profile")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public ResponseEntity<ProfileResponse> getProfile(Authentication auth) {
    UserEntity user = userService.findByEmail(auth.getName());
    
    ProfileResponse response = new ProfileResponse();
    response.setId(user.getId());
    response.setName(user.getName());
    response.setEmail(user.getEmail());
    response.setLookerStudioUrl(user.getLookerStudioUrl());  // ✨ NOVO
    
    return ResponseEntity.ok(response);
}
```

---

### **PARTE 2: Frontend**

#### **2.1. Model (menu-item.model.ts)**

```typescript
export interface User {
  id: string;
  email: string;
  name: string;
  role: 'ADMIN' | 'USER';
  authorId?: string;
  ecommerceUrl?: string;
  lookerStudioUrl?: string | null;  // ✨ NOVO
  // ... outros campos
}

export interface ProfileResponse {
  id: string;
  name: string;
  email: string;
  authProvider: string;
  passwordSet: boolean;
  profilePhotoUrl?: string | null;
  lookerStudioUrl?: string | null;  // ✨ NOVO (se backend retornar)
}
```

#### **2.2. AuthService (auth.service.ts)**

```typescript
getUserProfile(): Observable<User> {
  return this.http.get<ProfileResponse>(`${this.API_URL}/api/v1/user/profile`).pipe(
    map((response: ProfileResponse) => {
      const currentUser = this._currentUser();
      const mappedUser: User = {
        id: response.id,
        name: response.name,
        email: response.email,
        role: currentUser?.role || 'USER',
        lookerStudioUrl: response.lookerStudioUrl || null,  // ✨ NOVO
        // ... outros campos
      };
      return mappedUser;
    }),
    tap((user: User) => {
      this._currentUser.set(user);
      localStorage.setItem('currentUser', JSON.stringify(user));
    })
  );
}
```

#### **2.3. Componente de Métricas (metrics-page.component.ts)**

```typescript
import { Component, signal, inject, OnInit, computed } from '@angular/core';
import { CommonModule, DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { AuthService } from '@/app/core/services/auth.service';

@Component({
  selector: 'app-metrics-page',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './metrics-page.component.html',
  styles: []
})
export class MetricsPageComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly sanitizer = inject(DomSanitizer);

  // Estado do componente
  readonly loading = signal<boolean>(true);
  readonly error = signal<string | null>(null);
  readonly lookerStudioUrl = signal<SafeResourceUrl | null>(null);
  readonly hasMetrics = computed(() => this.lookerStudioUrl() !== null);

  ngOnInit(): void {
    this.loadMetrics();
  }

  loadMetrics(): void {
    this.loading.set(true);
    this.error.set(null);

    // Buscar dados do usuário atual
    const currentUser = this.authService.currentUser();
    
    // Se não tiver usuário logado, buscar do backend
    if (!currentUser || !currentUser.lookerStudioUrl) {
      this.authService.getUserProfile().subscribe({
        next: (user) => {
          this.processLookerStudioUrl(user.lookerStudioUrl);
          this.loading.set(false);
        },
        error: (err) => {
          console.error('Erro ao carregar perfil:', err);
          this.error.set('Erro ao carregar configurações de métricas');
          this.loading.set(false);
        }
      });
    } else {
      // Usar dados já carregados
      this.processLookerStudioUrl(currentUser.lookerStudioUrl);
      this.loading.set(false);
    }
  }

  private processLookerStudioUrl(url: string | null | undefined): void {
    if (!url || url.trim() === '') {
      // Sem URL configurada
      this.lookerStudioUrl.set(null);
      return;
    }

    // Validar URL
    if (!this.isValidLookerStudioUrl(url)) {
      this.error.set('URL do Looker Studio inválida');
      this.lookerStudioUrl.set(null);
      return;
    }

    // Sanitizar URL para uso seguro no iframe
    const safeUrl = this.sanitizer.bypassSecurityTrustResourceUrl(url);
    this.lookerStudioUrl.set(safeUrl);
  }

  private isValidLookerStudioUrl(url: string): boolean {
    try {
      const urlObj = new URL(url);
      return urlObj.hostname === 'lookerstudio.google.com' 
        || urlObj.hostname === 'datastudio.google.com';
    } catch {
      return false;
    }
  }

  retry(): void {
    this.loadMetrics();
  }
}
```

#### **2.4. Template HTML (metrics-page.component.html)**

```html
<div class="w-full space-y-4 sm:space-y-6">
  <!-- Título -->
  <div>
    <h1 class="text-xl sm:text-2xl md:text-3xl font-bold relative mb-2">
      <span class="relative z-10" style="background: linear-gradient(to right, #38bdf8, #2563eb, #38bdf8); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text;">Métricas</span>
      <span class="absolute inset-0 blur-xl opacity-60" style="background: linear-gradient(to right, #38bdf8, #2563eb, #38bdf8); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; filter: blur(8px);">Métricas</span>
    </h1>
    <p class="text-sm sm:text-base text-gray-400">Visualize suas métricas e análises detalhadas</p>
  </div>

  <!-- Loading State -->
  @if (loading()) {
    <div class="flex flex-col items-center justify-center py-12 sm:py-16 space-y-4">
      <span class="w-12 h-12 border-4 border-sky-500/30 border-t-sky-500 rounded-full animate-spin"></span>
      <p class="text-gray-400 text-sm sm:text-base">Carregando métricas...</p>
    </div>
  }

  <!-- Error State -->
  @if (error() && !loading()) {
    <div class="bg-red-500/10 border border-red-500/30 rounded-xl p-4 sm:p-6 space-y-4">
      <div class="flex items-start gap-3">
        <span class="material-icons text-red-400 text-xl sm:text-2xl flex-shrink-0">error_outline</span>
        <div class="flex-1">
          <h3 class="text-red-300 font-semibold text-sm sm:text-base mb-1">Erro ao carregar métricas</h3>
          <p class="text-red-400/80 text-xs sm:text-sm">{{ error() }}</p>
        </div>
      </div>
      <button
        type="button"
        class="px-4 py-2 bg-red-500/20 hover:bg-red-500/30 border border-red-500/30 rounded-lg text-red-300 text-sm font-medium transition-colors duration-200"
        (click)="retry()"
      >
        Tentar novamente
      </button>
    </div>
  }

  <!-- Empty State (sem URL configurada) -->
  @if (!loading() && !error() && !hasMetrics()) {
    <div class="bg-yellow-500/10 border border-yellow-500/30 rounded-xl p-4 sm:p-6 space-y-4">
      <div class="flex items-start gap-3">
        <span class="material-icons text-yellow-400 text-xl sm:text-2xl flex-shrink-0">info</span>
        <div class="flex-1">
          <h3 class="text-yellow-300 font-semibold text-sm sm:text-base mb-1">Métricas não configuradas</h3>
          <p class="text-yellow-400/80 text-xs sm:text-sm">
            Entre em contato com o administrador para configurar suas métricas no Looker Studio.
          </p>
        </div>
      </div>
    </div>
  }

  <!-- Iframe do Looker Studio -->
  @if (!loading() && !error() && hasMetrics()) {
    <div class="bg-white/5 backdrop-blur-xl border border-white/10 rounded-xl sm:rounded-2xl p-4 sm:p-6 shadow-lg">
      <div class="w-full h-[450px] sm:h-[600px] md:h-[700px] lg:h-[800px] rounded-lg overflow-hidden">
        <iframe
          width="100%"
          height="100%"
          [src]="lookerStudioUrl()!"
          frameborder="0"
          style="border:0"
          allowfullscreen
          sandbox="allow-storage-access-by-user-activation allow-scripts allow-same-origin allow-popups allow-popups-to-escape-sandbox"
          title="Métricas do Autor"
          loading="lazy"
        ></iframe>
      </div>
    </div>
  }
</div>
```

#### **2.5. Formulário de Edição (edit-user-modal.component.ts)**

```typescript
// Adicionar ao FormGroup
readonly form: FormGroup = this.fb.group({
  // ... campos existentes
  lookerStudioUrl: [''],  // ✨ NOVO (opcional)
});

// No ngOnInit, preencher com dados do usuário
ngOnInit(): void {
  // ... código existente
  const userData = this.user();
  
  this.form.patchValue({
    // ... outros campos
    lookerStudioUrl: userData.lookerStudioUrl || '',  // ✨ NOVO
  });
}

// No onSubmit, incluir no payload
onSubmit(): void {
  // ... código existente
  
  const formValue = this.form.value;
  const userData = this.user();
  
  // ... outros campos
  
  // ✨ NOVO: lookerStudioUrl
  const lookerStudioUrl = formValue.lookerStudioUrl?.trim() || '';
  if (lookerStudioUrl !== (userData.lookerStudioUrl || '')) {
    payload.lookerStudioUrl = lookerStudioUrl || null; // Empty string vira null
  }
  
  // ... enviar payload
}
```

#### **2.6. Template do Formulário (edit-user-modal.component.html)**

```html
<!-- Adicionar após o campo ecommerceUrl -->

<!-- Looker Studio URL Field (only if authorId is filled) -->
@if (hasAuthorId() && isUserRole()) {
  <div class="space-y-2">
    <div class="flex items-center justify-between">
      <label for="lookerStudioUrl" class="block text-xs sm:text-sm font-semibold text-gray-200">
        URL do Looker Studio
      </label>
      @if (form.get('lookerStudioUrl')?.value) {
        <button
          type="button"
          class="text-xs text-red-400 hover:text-red-300 transition-colors"
          (click)="removeField('lookerStudioUrl')"
          [disabled]="loading()"
        >
          Remover
        </button>
      }
    </div>
    <div class="relative">
      <span class="absolute left-3 sm:left-4 top-1/2 -translate-y-1/2 material-icons text-gray-400 text-lg sm:text-xl">analytics</span>
      <input 
        type="url" 
        id="lookerStudioUrl" 
        formControlName="lookerStudioUrl" 
        placeholder="https://lookerstudio.google.com/embed/reporting/..."
        class="w-full pl-10 sm:pl-12 pr-4 py-2.5 sm:py-3.5 bg-white/5 border border-white/10 rounded-lg sm:rounded-xl text-white placeholder:text-gray-500 focus:outline-none focus:border-sky-500 focus:ring-4 focus:ring-sky-500/20 transition-all duration-200 text-sm sm:text-base"
        [disabled]="loading()"
      />
    </div>
    @if (form.get('lookerStudioUrl')?.invalid && form.get('lookerStudioUrl')?.touched) {
      <p class="text-red-400 text-[10px] sm:text-xs">
        URL inválida. Deve começar com https://lookerstudio.google.com/embed/
      </p>
    }
    <small class="text-gray-500 text-[10px] sm:text-xs">
      URL de embed do relatório Looker Studio. Obtenha esta URL ao compartilhar o relatório no Looker Studio.
    </small>
  </div>
}
```

---

## ✅ Validações

### **Backend**

1. **Formato da URL**
   - Deve começar com `https://lookerstudio.google.com/embed/`
   - OU `https://datastudio.google.com/embed/` (compatibilidade)
   - Máximo 500 caracteres

2. **Opcional**
   - Pode ser `null` ou string vazia (remove configuração)
   - Não é obrigatório ter métricas configuradas

### **Frontend**

1. **Validação de URL**
   - Verificar formato antes de sanitizar
   - Mostrar erro se URL inválida

2. **Sanitização**
   - Usar `DomSanitizer.bypassSecurityTrustResourceUrl()`
   - Prevenir XSS

3. **Estados**
   - Loading: enquanto busca dados
   - Error: se houver erro na busca
   - Empty: se não houver URL configurada
   - Success: se URL válida e carregada

---

## 🎯 Casos de Uso

### **Caso 1: Autor com métricas configuradas**

```
1. Admin configura lookerStudioUrl para authorId = "1"
2. Autor 1 acessa /user/metrics
3. Sistema busca URL do perfil
4. Exibe iframe com métricas do autor 1
```

### **Caso 2: Autor sem métricas configuradas**

```
1. Admin não configurou lookerStudioUrl
2. Autor acessa /user/metrics
3. Sistema busca perfil → lookerStudioUrl = null
4. Exibe mensagem: "Métricas não configuradas"
```

### **Caso 3: Admin editando usuário**

```
1. Admin acessa /admin/users
2. Clica em editar usuário
3. Preenche lookerStudioUrl com URL do Looker Studio
4. Salva
5. Backend valida e salva no banco
6. Próximo acesso do autor mostra métricas
```

### **Caso 4: Remover métricas**

```
1. Admin edita usuário
2. Clica em "Remover" no campo lookerStudioUrl
3. Campo fica vazio
4. Salva
5. Backend seta lookerStudioUrl = null
6. Autor vê mensagem "não configurado"
```

---

## 🔒 Segurança

1. **Sanitização de URL**
   - Angular sanitiza URLs dinâmicas em iframes
   - Previne XSS

2. **Validação de domínio**
   - Apenas URLs do Looker Studio são aceitas
   - Previne redirecionamento malicioso

3. **Autorização**
   - Apenas ADMIN pode editar lookerStudioUrl
   - USER apenas visualiza suas próprias métricas

4. **Sandbox no iframe**
   - Atributo `sandbox` restringe permissões
   - Previne execução de scripts maliciosos

---

## 📝 Checklist de Implementação

### **Backend**
- [ ] Criar migration para adicionar coluna `looker_studio_url`
- [ ] Atualizar `UserEntity` com novo campo
- [ ] Atualizar `UserListResponse` DTO
- [ ] Atualizar `UpdateUserRequest` DTO
- [ ] Adicionar validação de URL no controller
- [ ] Atualizar endpoint `/api/v1/user/profile` para retornar `lookerStudioUrl`
- [ ] Testar criação de usuário com lookerStudioUrl
- [ ] Testar atualização de usuário com lookerStudioUrl
- [ ] Testar remoção de lookerStudioUrl (setar null)

### **Frontend**
- [ ] Atualizar interface `User` com `lookerStudioUrl`
- [ ] Atualizar interface `ProfileResponse` com `lookerStudioUrl`
- [ ] Atualizar `AuthService.getUserProfile()` para mapear `lookerStudioUrl`
- [ ] Criar/atualizar `MetricsPageComponent` com lógica completa
- [ ] Adicionar campo `lookerStudioUrl` no formulário de edição
- [ ] Adicionar validação de URL no formulário
- [ ] Implementar estados: loading, error, empty, success
- [ ] Testar exibição de métricas com URL válida
- [ ] Testar exibição quando URL não configurada
- [ ] Testar sanitização de URL

---

## 🚀 Próximos Passos

1. **Revisar esta documentação**
2. **Aprovar implementação**
3. **Implementar backend primeiro**
4. **Testar backend isoladamente**
5. **Implementar frontend**
6. **Testar integração completa**
7. **Documentar para admins como obter URL do Looker Studio**

---

## 📌 Notas Importantes

1. **URL do Looker Studio é pública**
   - A URL de embed é pública (qualquer um com o link pode ver)
   - Não armazenar dados sensíveis no Looker Studio
   - Usar filtros/parâmetros se necessário restringir acesso

2. **Performance**
   - Iframe carrega de forma lazy (`loading="lazy"`)
   - Looker Studio gerencia cache internamente

3. **Compatibilidade**
   - Suporta URLs antigas do Data Studio (`datastudio.google.com`)
   - Suporta apenas HTTPS

4. **Extensibilidade Futura**
   - Pode adicionar campo `lookerStudioPageId` para múltiplas páginas
   - Pode adicionar parâmetros dinâmicos na URL (ex: `?authorId=1`)


