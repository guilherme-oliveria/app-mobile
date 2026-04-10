// lib/services/entrega_service.dart
import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import '../models/models.dart';

const _baseUrl = 'http://10.0.2.2:8080/api';

class EntregaService extends ChangeNotifier {
  List<Entrega> _entregas = [];
  Entrega? _entregaAtiva;
  bool _carregando = false;

  List<Entrega> get entregas => _entregas;
  Entrega? get entregaAtiva => _entregaAtiva;
  bool get carregando => _carregando;

  Map<String, String> _headers(String token) => {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer $token',
      };

  Future<void> carregarEntregas(String token, int motoboyId) async {
    _carregando = true;
    notifyListeners();

    final res = await http.get(
      Uri.parse('$_baseUrl/entregas/motoboy/$motoboyId'),
      headers: _headers(token),
    );

    if (res.statusCode == 200) {
      final List<dynamic> lista = jsonDecode(res.body);
      _entregas = lista.map((e) => Entrega.fromJson(e)).toList();

      // identifica se há entrega ativa (atribuída ou coletada)
      try {
        _entregaAtiva = _entregas.firstWhere((e) =>
            e.status == StatusEntrega.ATRIBUIDA ||
            e.status == StatusEntrega.COLETADA);
      } catch (_) {
        _entregaAtiva = null;
      }
    }

    _carregando = false;
    notifyListeners();
  }

  Future<void> confirmarColeta(String token, int entregaId) async {
    final res = await http.patch(
      Uri.parse('$_baseUrl/entregas/$entregaId/coletar'),
      headers: _headers(token),
    );
    if (res.statusCode != 200) throw Exception('Erro ao confirmar coleta');
  }

  Future<void> finalizarEntrega(
      String token, int entregaId, String codigo) async {
    final res = await http.patch(
      Uri.parse('$_baseUrl/entregas/$entregaId/finalizar'),
      headers: _headers(token),
      body: jsonEncode({'codigoConfirmacao': codigo}),
    );
    if (res.statusCode != 200) throw Exception('Erro ao finalizar entrega');
  }

  /// Chama o endpoint de pagamento após o cliente pagar na maquininha
  Future<void> processarPagamento({
    required String token,
    required int entregaId,
    required double valor,
    required String smartPosSerial,
    required String formaPagamento,
  }) async {
    final res = await http.post(
      Uri.parse('$_baseUrl/pagamentos/processar'),
      headers: _headers(token),
      body: jsonEncode({
        'entregaId': entregaId,
        'valor': valor,
        'smartPosSerial': smartPosSerial,
        'formaPagamento': formaPagamento,
      }),
    );
    if (res.statusCode != 200) throw Exception('Erro ao processar pagamento');
  }

  Future<void> atualizarLocalizacao(
      String token, int motoboyId, double lat, double lng) async {
    await http.patch(
      Uri.parse('$_baseUrl/motoboys/$motoboyId/localizacao'),
      headers: _headers(token),
      body: jsonEncode({'latitude': lat, 'longitude': lng}),
    );
  }
}
