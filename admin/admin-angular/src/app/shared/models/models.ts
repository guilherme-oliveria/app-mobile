// src/app/shared/models/models.ts

// ── Resposta de erro padronizada do backend ─────────────────
export interface ErrorResponse {
  status: number;
  erro: string;
  mensagem: string;
  campos?: CampoErro[];
  timestamp: string;
}

export interface CampoErro {
  campo: string;
  mensagem: string;
}

export interface Loja {
  id: number;
  nome: string;
  cnpj: string;
  email: string;
  telefone: string;
  endereco: string;
  chavePix?: string;
  ativo: boolean;
  saldoPendente: number;
  senhaInicial?: string;   // usado apenas no cadastro
}

export interface Motoboy {
  id: number;
  nome: string;
  cpf: string;
  email: string;
  telefone: string;
  cnh?: string;
  smartPosSerial: string;
  status: 'DISPONIVEL' | 'EM_ENTREGA' | 'INATIVO';
  latitudeAtual?: number;
  longitudeAtual?: number;
  ativo: boolean;
  senhaInicial?: string;   // usado apenas no cadastro
}

export interface Pedido {
  id: number;
  lojaId: number;
  lojaNome: string;
  clienteNome: string;
  clienteTelefone: string;
  enderecoEntrega: string;
  itens: ItemPedido[];
  valorTotal: number;
  taxaEntrega: number;
  status: StatusPedido;
  observacao: string;
  criadoEm: string;
}

export interface ItemPedido {
  id: number;
  descricao: string;
  quantidade: number;
  valorUnitario: number;
  valorTotal: number;
}

export interface Entrega {
  id: number;
  pedido: Pedido;
  motoboy?: Motoboy;
  status: 'DISPONIVEL' | 'ATRIBUIDA' | 'COLETADA' | 'FINALIZADA' | 'CANCELADA';
  atribuidaEm?: string;
  coletadaEm?: string;
  finalizadaEm?: string;
  criadoEm?: string;
}

export interface LiquidacaoDia {
  id: number;
  lojaId: number;
  lojaNome: string;
  dataReferencia: string;
  totalEntregas: number;
  valorBruto: number;
  totalTaxas: number;
  valorLiquido: number;
  status: 'PENDENTE' | 'PROCESSADA' | 'FALHA';
}

export type StatusPedido =
  | 'AGUARDANDO_ACEITE'
  | 'ACEITO'
  | 'MOTOBOY_A_CAMINHO_LOJA'
  | 'COLETADO'
  | 'EM_ENTREGA'
  | 'ENTREGUE'
  | 'CANCELADO';
