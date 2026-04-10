import { Component, OnInit, inject } from '@angular/core';
import { NgClass, DecimalPipe, DatePipe } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { ApiService } from '../../core/services/api.service';
import { Loja, LiquidacaoDia } from '../../shared/models/models';

@Component({
  selector: 'app-relatorios',
  imports: [
    NgClass, DecimalPipe, DatePipe,
    MatCardModule, MatTableModule, MatButtonModule,
    MatIconModule, MatFormFieldModule, MatSelectModule
  ],
  templateUrl: './relatorios.component.html',
  styleUrl: './relatorios.component.css'
})
export class RelatoriosComponent implements OnInit {
  private readonly api = inject(ApiService);

  lojas: Loja[] = [];
  liquidacoes: LiquidacaoDia[] = [];
  lojaId: number | null = null;
  colunas = ['data', 'entregas', 'bruto', 'taxas', 'liquido', 'status'];
  colunasFooter = ['footer-label', 'footer-entregas', 'footer-bruto', 'footer-taxas', 'footer-liquido', 'footer-empty'];

  get totalEntregas() { return this.liquidacoes.reduce((s, l) => s + l.totalEntregas, 0); }
  get totalBruto() { return this.liquidacoes.reduce((s, l) => s + l.valorBruto, 0); }
  get totalTaxas() { return this.liquidacoes.reduce((s, l) => s + l.totalTaxas, 0); }
  get totalLiquido() { return this.liquidacoes.reduce((s, l) => s + l.valorLiquido, 0); }

  ngOnInit() {
    this.api.getLojas().subscribe(l => this.lojas = l);
  }

  carregarLiquidacoes() {
    if (!this.lojaId) return;
    this.api.getLiquidacoesPorLoja(this.lojaId).subscribe(l => this.liquidacoes = l);
  }

  liquidarAgora() {
    if (confirm('Executar liquidação diária agora? (uso em desenvolvimento)')) {
      this.api.liquidarAgora().subscribe(() => {
        alert('Liquidação executada com sucesso!');
        if (this.lojaId) this.carregarLiquidacoes();
      });
    }
  }
}
