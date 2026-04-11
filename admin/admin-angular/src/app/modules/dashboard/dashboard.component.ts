// src/app/modules/dashboard/dashboard.component.ts
import { Component, OnInit, OnDestroy, inject } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatBadgeModule } from '@angular/material/badge';
import { MatChipsModule } from '@angular/material/chips';
import { ApiService } from '../../core/services/api.service';
import { Entrega, Motoboy, Pedido } from '../../shared/models/models';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-dashboard',
  imports: [
    DecimalPipe,
    MatCardModule, MatIconModule, MatTableModule,
    MatFormFieldModule, MatSelectModule,
    MatBadgeModule, MatChipsModule
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit, OnDestroy {
  private readonly api = inject(ApiService);
  private refreshInterval: ReturnType<typeof setInterval> | null = null;

  pedidosPendentes: Pedido[] = [];
  entregasDisponiveis: Entrega[] = [];
  motoboysDisponiveis: Motoboy[] = [];
  colunas = ['id', 'loja', 'cliente', 'endereco', 'valor', 'tempo', 'acao'];

  ngOnInit() {
    this.carregarDados();
    // Polling a cada 15s como fallback do WebSocket
    this.refreshInterval = setInterval(() => this.carregarDados(), 15_000);
  }

  ngOnDestroy() {
    if (this.refreshInterval) {
      clearInterval(this.refreshInterval);
    }
  }

  carregarDados() {
    forkJoin({
      pedidos: this.api.getPedidosPorStatus('AGUARDANDO_ACEITE'),
      entregas: this.api.getEntregasDisponiveis(),
      motoboys: this.api.getMotoboysdisponiveis()
    }).subscribe(({ pedidos, entregas, motoboys }) => {
      this.pedidosPendentes = pedidos;
      this.entregasDisponiveis = entregas;
      this.motoboysDisponiveis = motoboys;
    });
  }

  atribuirMotoboy(entregaId: number, motoboyId: number) {
    if (!motoboyId) return;
    this.api.atribuirMotoboy(entregaId, motoboyId).subscribe(() => this.carregarDados());
  }

  /**
   * Calcula minutos desde que a entrega foi criada.
   * Usado para highlight vermelho em entregas que ninguém aceitou.
   */
  minutosEsperando(entrega: Entrega): number {
    if (!entrega.criadoEm) return 0;
    const criadoEm = new Date(entrega.criadoEm).getTime();
    return Math.floor((Date.now() - criadoEm) / 60_000);
  }

  isTimeout(entrega: Entrega): boolean {
    return this.minutosEsperando(entrega) >= 10;
  }
}
