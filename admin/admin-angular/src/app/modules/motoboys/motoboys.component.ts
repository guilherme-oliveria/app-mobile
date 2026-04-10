import { Component, OnInit, inject } from '@angular/core';
import { NgClass } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ApiService } from '../../core/services/api.service';
import { Motoboy } from '../../shared/models/models';

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

  motoboys: Motoboy[] = [];
  mostrarForm = false;
  novo: Partial<Motoboy> = {};
  colunas = ['id', 'nome', 'cpf', 'email', 'telefone', 'smartPos', 'status'];

  ngOnInit() {
    this.carregar();
  }

  carregar() {
    this.api.getMotoboys().subscribe(m => this.motoboys = m);
  }

  criar() {
    if (!this.novo.nome || !this.novo.cpf || !this.novo.email) {
      alert('Preencha nome, CPF e email.');
      return;
    }
    this.api.criarMotoboy(this.novo).subscribe(() => {
      this.novo = {};
      this.mostrarForm = false;
      this.carregar();
    });
  }
}
