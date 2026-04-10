// src/app/core/services/api.service.ts
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Entrega, Loja, LiquidacaoDia, Motoboy, Pedido, StatusPedido } from '../../shared/models/models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly api = environment.apiUrl;

  // ── Lojas ──────────────────────────────────────────
  getLojas(): Observable<Loja[]> {
    return this.http.get<Loja[]>(`${this.api}/lojas`);
  }
  getLoja(id: number): Observable<Loja> {
    return this.http.get<Loja>(`${this.api}/lojas/${id}`);
  }
  criarLoja(data: Partial<Loja>): Observable<Loja> {
    return this.http.post<Loja>(`${this.api}/lojas`, data);
  }

  // ── Motoboys ───────────────────────────────────────
  getMotoboys(): Observable<Motoboy[]> {
    return this.http.get<Motoboy[]>(`${this.api}/motoboys`);
  }
  getMotoboysdisponiveis(): Observable<Motoboy[]> {
    return this.http.get<Motoboy[]>(`${this.api}/motoboys/disponiveis`);
  }
  criarMotoboy(data: Partial<Motoboy>): Observable<Motoboy> {
    return this.http.post<Motoboy>(`${this.api}/motoboys`, data);
  }

  // ── Pedidos ────────────────────────────────────────
  getPedidosPorLoja(lojaId: number): Observable<Pedido[]> {
    return this.http.get<Pedido[]>(`${this.api}/pedidos/loja/${lojaId}`);
  }
  getPedidosPorStatus(status: StatusPedido): Observable<Pedido[]> {
    return this.http.get<Pedido[]>(`${this.api}/pedidos/status/${status}`);
  }
  atualizarStatusPedido(id: number, status: StatusPedido): Observable<Pedido> {
    return this.http.patch<Pedido>(`${this.api}/pedidos/${id}/status`, { status });
  }

  // ── Entregas ───────────────────────────────────────
  getEntregasPendentes(): Observable<Entrega[]> {
    return this.http.get<Entrega[]>(`${this.api}/entregas/pendentes`);
  }
  atribuirMotoboy(entregaId: number, motoboyId: number): Observable<Entrega> {
    return this.http.patch<Entrega>(`${this.api}/entregas/${entregaId}/atribuir`, { motoboyId });
  }
  criarEntrega(pedidoId: number): Observable<Entrega> {
    return this.http.post<Entrega>(`${this.api}/entregas/pedido/${pedidoId}`, {});
  }

  // ── Pagamentos / Liquidações ───────────────────────
  getLiquidacoesPorLoja(lojaId: number): Observable<LiquidacaoDia[]> {
    return this.http.get<LiquidacaoDia[]>(`${this.api}/pagamentos/liquidacoes/loja/${lojaId}`);
  }
  liquidarAgora(): Observable<void> {
    return this.http.post<void>(`${this.api}/pagamentos/liquidar-agora`, {});
  }
}
