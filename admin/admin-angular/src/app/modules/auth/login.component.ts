import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-login',
  imports: [
    FormsModule,
    MatCardModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatIconModule, MatProgressSpinnerModule
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  email = '';
  senha = '';
  erro = '';
  carregando = false;
  ocultarSenha = true;

  login() {
    if (this.carregando) return;   // evita duplo-clique
    this.carregando = true;
    this.erro = '';
    this.auth.login(this.email, this.senha).subscribe({
      next: () => {
        this.carregando = false;
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.erro = err?.status === 401
          ? 'Email ou senha inválidos'
          : 'Erro ao conectar. Tente novamente.';
        this.carregando = false;
      }
    });
  }
}
