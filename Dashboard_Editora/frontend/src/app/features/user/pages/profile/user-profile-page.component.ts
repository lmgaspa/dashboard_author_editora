import { Component, signal, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '@/app/core/services/auth.service';

@Component({
  selector: 'app-user-profile-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './user-profile-page.component.html',
  styles: []
})
export class UserProfilePageComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);

  readonly profileForm: FormGroup = this.fb.group({
    name: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]]
  });

  readonly loading = signal<boolean>(false);
  readonly success = signal<boolean>(false);

  ngOnInit(): void {
    const user = this.authService.currentUser();
    if (user) {
      this.profileForm.patchValue({
        name: user.name,
        email: user.email
      });
    }
  }

  onSubmit(): void {
    if (this.profileForm.invalid) return;

    this.loading.set(true);
    // Atualizar perfil via API
    setTimeout(() => {
      this.success.set(true);
      this.loading.set(false);
      setTimeout(() => this.success.set(false), 3000);
    }, 1000);
  }
}

