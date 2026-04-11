// lib/models/models.dart

class Usuario {
  final String token;
  final String role;
  final String nome;
  final int refId; // ID do motoboy
  final bool deveAlterarSenha;

  const Usuario({
    required this.token,
    required this.role,
    required this.nome,
    required this.refId,
    this.deveAlterarSenha = false,
  });

  factory Usuario.fromJson(Map<String, dynamic> json) => Usuario(
        token: json['token'],
        role: json['role'],
        nome: json['nome'],
        refId: json['refId'] ?? 0,
        deveAlterarSenha: json['deveAlterarSenha'] ?? false,
      );
}

enum StatusEntrega { DISPONIVEL, ATRIBUIDA, COLETADA, FINALIZADA, CANCELADA }

class Entrega {
  final int id;
  final Pedido pedido;
  final StatusEntrega status;
  final String? atribuidaEm;
  final String? coletadaEm;
  final String? finalizadaEm;

  const Entrega({
    required this.id,
    required this.pedido,
    required this.status,
    this.atribuidaEm,
    this.coletadaEm,
    this.finalizadaEm,
  });

  factory Entrega.fromJson(Map<String, dynamic> json) => Entrega(
        id: json['id'],
        pedido: Pedido.fromJson(json['pedido']),
        status: StatusEntrega.values.byName(json['status']),
        atribuidaEm: json['atribuidaEm'],
        coletadaEm: json['coletadaEm'],
        finalizadaEm: json['finalizadaEm'],
      );
}

class Pedido {
  final int id;
  final String lojaNome;
  final String clienteNome;
  final String clienteTelefone;
  final String enderecoEntrega;
  final double latitudeEntrega;
  final double longitudeEntrega;
  final double valorTotal;
  final String status;
  final List<ItemPedido> itens;

  const Pedido({
    required this.id,
    required this.lojaNome,
    required this.clienteNome,
    required this.clienteTelefone,
    required this.enderecoEntrega,
    required this.latitudeEntrega,
    required this.longitudeEntrega,
    required this.valorTotal,
    required this.status,
    required this.itens,
  });

  factory Pedido.fromJson(Map<String, dynamic> json) => Pedido(
        id: json['id'],
        lojaNome: json['lojaNome'] ?? '',
        clienteNome: json['clienteNome'] ?? '',
        clienteTelefone: json['clienteTelefone'] ?? '',
        enderecoEntrega: json['enderecoEntrega'] ?? '',
        latitudeEntrega: (json['latitudeEntrega'] ?? 0).toDouble(),
        longitudeEntrega: (json['longitudeEntrega'] ?? 0).toDouble(),
        valorTotal: (json['valorTotal'] ?? 0).toDouble(),
        status: json['status'] ?? '',
        itens: (json['itens'] as List<dynamic>? ?? [])
            .map((i) => ItemPedido.fromJson(i))
            .toList(),
      );
}

class ItemPedido {
  final String descricao;
  final int quantidade;
  final double valorUnitario;

  const ItemPedido({
    required this.descricao,
    required this.quantidade,
    required this.valorUnitario,
  });

  factory ItemPedido.fromJson(Map<String, dynamic> json) => ItemPedido(
        descricao: json['descricao'],
        quantidade: json['quantidade'],
        valorUnitario: (json['valorUnitario'] ?? 0).toDouble(),
      );
}
