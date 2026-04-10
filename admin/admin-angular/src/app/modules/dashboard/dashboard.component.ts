// src/app/modules/dashboard/dashboard.component.ts
import { Component, OnInit, inject } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { ApiService } from '../../core/services/api.service';
import { Entrega, Motoboy, Pedido } from '../../shared/models/models';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-dashboard',
  imports: [
    DecimalPipe,
    MatCardModule, MatIconModule, MatTableModule,
    MatFormFieldModule, MatSelectModule
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  private readonly api = inject(ApiService);

  pedidosPendentes: Pedido[] = [];
  entregasPendentes: Entrega[] = [];
  motoboysDisponiveis: Motoboy[] = [];
  colunas = ['id', 'cliente', 'endereco', 'valor', 'acao'];

  ngOnInit() {
    forkJoin({
      pedidos: this.api.getPedidosPorStatus('AGUARDANDO_ACEITE'),
      entregas: this.api.getEntregasPendentes(),
      motoboys: this.api.getMotoboysdisponiveis()
    }).subscribe(({ pedidos, entregas, motoboys }) => {
      this.pedidosPendentes = pedidos;
      this.entregasPendentes = entregas;
      this.motoboysDisponiveis = motoboys;
    });
  }

  atribuirMotoboy(entregaId: number, motoboyId: number) {
    if (!motoboyId) return;
    this.api.atribuirMotoboy(entregaId, motoboyId).subscribe(() => this.ngOnInit());
  }
}
