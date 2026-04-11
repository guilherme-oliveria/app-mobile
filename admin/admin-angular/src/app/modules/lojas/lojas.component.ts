import { Component, OnInit, inject } from '@angular/core';
import { NgClass, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { Loja } from '../../shared/models/models';
import { ApiService } from '../../core/services/api.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-lojas',
  imports: [
    NgClass, DecimalPipe, FormsModule,
    MatCardModule, MatTableModule, MatButtonModule,
    MatIconModule, MatFormFieldModule, MatInputModule
  ],
  templateUrl: './lojas.component.html',
  styleUrl: './lojas.component.css'
})
export class LojasComponent implements OnInit {

  private readonly api = inject(ApiService);
  readonly auth = inject(AuthService);

  lojas: Loja[] = [];
  mostrarForm = false;
  nova: Partial<Loja> = {};
  colunas = ['id', 'nome', 'cnpj', 'email', 'telefone', 'saldo', 'status', 'acoes'];

  get isAdmin(): boolean {
    return this.auth.isAdmin();
  }

  ngOnInit(): void {
    this.carregar();
  }

  carregar(): void {
    this.api.getLojas().subscribe(l => this.lojas = l);
  }

  criar(): void {
    if (!this.nova.nome || !this.nova.cnpj || !this.nova.email) {
      alert('Preencha nome, CNPJ e email.');
      return;
    }
    this.api.criarLoja(this.nova).subscribe(() => {
      this.nova = {};
      this.mostrarForm = false;
      this.carregar();
    });
  }

  inativar(id: number): void {
    if (!confirm('Deseja inativar esta loja? Esta ação remove o acesso dela à plataforma.')) return;
    this.api.inativarLoja(id).subscribe(() => this.carregar());
  }
}
