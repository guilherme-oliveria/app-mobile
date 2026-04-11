// lib/services/pedido_service.dart
import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:flutter/foundation.dart' show kIsWeb;
import 'package:http/http.dart' as http;

const _baseUrl = kIsWeb
    ? 'http://localhost:8080/api'
    : 'http://10.0.2.2:8080/api';

// ── Modelos ────────────────────────────────────────────────────────────────

class Pedido {
  final int id;
  final String clienteNome;
  final String enderecoEntrega;
  final double valorTotal;
  final String status;
  final List<ItemPedido> itens;
  final String criadoEm;

  const Pedido({
    required this.id,
    required this.clienteNome,
    required this.enderecoEntrega,
    required this.valorTotal,
    required this.status,
    required this.itens,
    required this.criadoEm,
  });

  factory Pedido.fromJson(Map<String, dynamic> j) => Pedido(
        id: j['id'],
        clienteNome: j['clienteNome'] ?? '',
        enderecoEntrega: j['enderecoEntrega'] ?? '',
        valorTotal: (j['valorTotal'] ?? 0).toDouble(),
        status: j['status'] ?? '',
        criadoEm: j['criadoEm'] ?? '',
        itens: (j['itens'] as List<dynamic>? ?? [])
            .map((i) => ItemPedido.fromJson(i))
            .toList(),
      );
}

class ItemPedido {
  final String descricao;
  final int quantidade;
  final double valorUnitario;

  const ItemPedido(
      {required this.descricao,
      required this.quantidade,
      required this.valorUnitario});

  factory ItemPedido.fromJson(Map<String, dynamic> j) => ItemPedido(
        descricao: j['descricao'],
        quantidade: j['quantidade'],
        valorUnitario: (j['valorUnitario'] ?? 0).toDouble(),
      );
}

// ── Service ────────────────────────────────────────────────────────────────

class PedidoService extends ChangeNotifier {
  List<Pedido> _pedidos = [];
  bool _carregando = false;

  List<Pedido> get pedidos => _pedidos;
  bool get carregando => _carregando;

  List<Pedido> get ativos => _pedidos
      .where((p) => p.status != 'ENTREGUE' && p.status != 'CANCELADO')
      .toList();

  Map<String, String> _headers(String token) => {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer $token',
      };

  Future<void> carregar(String token, int lojaId) async {
    _carregando = true;
    notifyListeners();
    try {
      final res = await http.get(
        Uri.parse('$_baseUrl/pedidos/loja/$lojaId'),
        headers: _headers(token),
      );
      if (res.statusCode == 200) {
        final List<dynamic> lista = jsonDecode(res.body);
        _pedidos = lista.map((p) => Pedido.fromJson(p)).toList();
      }
    } finally {
      _carregando = false;
      notifyListeners();
    }
  }

  Future<void> criarPedido(String token, Map<String, dynamic> payload) async {
    final res = await http.post(
      Uri.parse('$_baseUrl/pedidos'),
      headers: _headers(token),
      body: jsonEncode(payload),
    );
    if (res.statusCode != 201) throw Exception('Erro ao criar pedido');
  }

  Future<void> atualizarStatus(
      String token, int pedidoId, String status) async {
    await http.patch(
      Uri.parse('$_baseUrl/pedidos/$pedidoId/status'),
      headers: _headers(token),
      body: jsonEncode({'status': status}),
    );
  }
}
