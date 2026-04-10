import { Component, OnInit, inject } from '@angular/core';
import { NgClass, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ApiService } from '../../core/services/api.service';
import { Loja } from '../../shared/models/models';

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

  lojas: Loja[] = [];
  mostrarForm = false;
  nova: Partial<Loja> = {};
  colunas = ['id', 'nome', 'cnpj', 'email', 'telefone', 'saldo', 'status'];

  ngOnInit() {
    this.carregar();
  }

  carregar() {
    this.api.getLojas().subscribe(l => this.lojas = l);
  }

  criar() {
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
}
