import { Component, OnInit, inject } from '@angular/core';
import { NgClass, DecimalPipe, DatePipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { ApiService } from '../../core/services/api.service';
import { Pedido, StatusPedido } from '../../shared/models/models';

@Component({
  selector: 'app-pedidos',
  imports: [
    NgClass, DecimalPipe, DatePipe,
    MatCardModule, MatTableModule, MatButtonModule,
    MatIconModule, MatFormFieldModule, MatSelectModule
  ],
  templateUrl: './pedidos.component.html',
  styleUrl: './pedidos.component.css'
})
export class PedidosComponent implements OnInit {
  pedidos: Pedido[] = [];
  pedidosFiltrados: Pedido[] = [];
  filtroStatus = '';
  colunas = ['id', 'loja', 'cliente', 'endereco', 'valor', 'status', 'data', 'acoes'];

  private readonly api = inject(ApiService);

  ngOnInit() {
    this.carregar();
  }

  carregar() {
    this.api.getPedidosPorStatus('AGUARDANDO_ACEITE').subscribe(p => {
      this.pedidos = p;
      this.filtrar();
    });
    ['ACEITO', 'COLETADO', 'EM_ENTREGA', 'ENTREGUE'].forEach(status => {
      this.api.getPedidosPorStatus(status as StatusPedido).subscribe(p => {
        this.pedidos = [...this.pedidos, ...p];
        this.pedidos.sort((a, b) => b.id - a.id);
        this.filtrar();
      });
    });
  }

  filtrar() {
    this.pedidosFiltrados = this.filtroStatus
      ? this.pedidos.filter(p => p.status === this.filtroStatus)
      : [...this.pedidos];
  }

  labelStatus(status: string): string {
    const map: Record<string, string> = {
      'AGUARDANDO_ACEITE': 'Aguardando',
      'ACEITO': 'Aceito',
      'COLETADO': 'Coletado',
      'EM_ENTREGA': 'Em entrega',
      'ENTREGUE': 'Entregue',
      'CANCELADO': 'Cancelado'
    };
    return map[status] ?? status;
  }

  criarEntrega(pedidoId: number) {
    this.api.criarEntrega(pedidoId).subscribe(() => {
      alert('Entrega criada! Motoboys serão notificados. Você também pode atribuir manualmente no Dashboard.');
    });
  }
}
