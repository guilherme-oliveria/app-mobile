import { Component, OnInit, inject } from '@angular/core';
import { NgClass } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Motoboy } from '../../shared/models/models';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-motoboys',
  imports: [
    NgClass, FormsModule,
    MatCardModule, MatTableModule, MatButtonModule,
    MatIconModule, MatFormFieldModule, MatInputModule
  ],
  templateUrl: './motoboys.component.html',
  styleUrl: './motoboys.component.css'
})
export class MotoboysComponent implements OnInit {

  private readonly api = inject(ApiService);
  private readonly snackBar = inject(MatSnackBar);
  readonly auth = inject(AuthService);

  motoboys: Motoboy[] = [];
  mostrarForm = false;
  novo: Partial<Motoboy> = {};
  colunas = ['id', 'nome', 'cpf', 'email', 'telefone', 'smartPos', 'status', 'acoes'];

  get isAdmin(): boolean {
    return this.auth.isAdmin();
  }

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.api.getMotoboys().subscribe({
      next: m => this.motoboys = m,
      error: () => {} // interceptor já mostra snackbar
    });
  }

  criar(): void {
    if (!this.novo.nome || !this.novo.cpf || !this.novo.email || !this.novo.senhaInicial) {
      this.snackBar.open('Preencha nome, CPF, email e senha de acesso.', 'Fechar', {
        duration: 5000, horizontalPosition: 'center', verticalPosition: 'top'
      });
      return;
    }
    if (this.novo.senhaInicial!.length < 6) {
      this.snackBar.open('A senha deve ter no mínimo 6 caracteres.', 'Fechar', {
        duration: 5000, horizontalPosition: 'center', verticalPosition: 'top'
      });
      return;
    }
    this.api.criarMotoboy(this.novo).subscribe({
      next: () => {
        this.snackBar.open('Motoboy cadastrado com sucesso!', 'Fechar', {
          duration: 4000, horizontalPosition: 'center', verticalPosition: 'top',
          panelClass: ['snackbar-sucesso']
        });
        this.novo = {};
        this.mostrarForm = false;
        this.carregar();
      },
      error: () => {} // interceptor já mostra snackbar com detalhe do erro
    });
  }

  inativar(id: number): void {
    if (!confirm('Deseja inativar este motoboy? Ele perderá o acesso à plataforma.')) return;
    this.api.inativarMotoboy(id).subscribe({
      next: () => {
        this.snackBar.open('Motoboy inativado.', 'Fechar', {
          duration: 4000, horizontalPosition: 'center', verticalPosition: 'top'
        });
        this.carregar();
      },
      error: () => {} // interceptor já mostra snackbar
    });
  }
}
