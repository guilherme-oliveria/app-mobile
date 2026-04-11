// lib/services/auth_service.dart
import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:flutter/foundation.dart' show kIsWeb;
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:http/http.dart' as http;
import '../models/models.dart';

// Browser (Chrome/Edge) → localhost | Emulador Android → 10.0.2.2
// Celular físico na rede → troque para: 'http://192.168.X.X:8080/api'
// Produção → troque para: 'https://sua-api.com/api'
const _baseUrl = kIsWeb
    ? 'http://localhost:8080/api'
    : 'http://10.0.2.2:8080/api';

class AuthService extends ChangeNotifier {
  final _storage = const FlutterSecureStorage();
  Usuario? _usuario;

  bool get isLoggedIn => _usuario != null;
  Usuario? get usuario => _usuario;
  String get token => _usuario?.token ?? '';

  AuthService() {
    _carregarDoStorage();
  }

  Future<void> _carregarDoStorage() async {
    final tokenSalvo = await _storage.read(key: 'token');
    final dadosSalvos = await _storage.read(key: 'usuario');
    if (tokenSalvo != null && dadosSalvos != null) {
      _usuario = Usuario.fromJson(jsonDecode(dadosSalvos));
      notifyListeners();
    }
  }

  /// Roles permitidos neste app
  static const _rolesPermitidos = ['MOTOBOY', 'ADMIN'];

  Future<void> login(String email, String senha) async {
    final response = await http.post(
      Uri.parse('$_baseUrl/auth/login'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({'email': email, 'senha': senha}),
    );

    if (response.statusCode == 200) {
      final json = jsonDecode(response.body);
      final role = json['role'] as String? ?? '';

      // ── Validação de perfil ──────────────────────────────
      if (!_rolesPermitidos.contains(role)) {
        String msg;
        if (role == 'LOJA') {
          msg = 'Este é o app do Motoboy. Use o app da Loja para acessar.';
        } else if (role == 'SUPORTE') {
          msg = 'Acesso de suporte disponível apenas no painel web.';
        } else {
          msg = 'Seu perfil ($role) não tem acesso a este aplicativo.';
        }
        throw Exception(msg);
      }

      _usuario = Usuario.fromJson(json);
      await _storage.write(key: 'token', value: _usuario!.token);
      await _storage.write(key: 'usuario', value: jsonEncode(json));
      notifyListeners();
    } else {
      throw Exception('Email ou senha inválidos');
    }
  }

  Future<void> logout() async {
    _usuario = null;
    await _storage.deleteAll();
    notifyListeners();
  }

  Future<void> trocarSenha(String senhaAtual, String novaSenha) async {
    final response = await http.post(
      Uri.parse('$_baseUrl/auth/trocar-senha'),
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer $token',
      },
      body: jsonEncode({
        'senhaAtual': senhaAtual,
        'novaSenha': novaSenha,
      }),
    );

    if (response.statusCode == 200) {
      // Atualiza o flag local — não precisa relogar
      if (_usuario != null) {
        _usuario = Usuario(
          token: _usuario!.token,
          role: _usuario!.role,
          nome: _usuario!.nome,
          refId: _usuario!.refId,
          deveAlterarSenha: false,
        );
        await _storage.write(key: 'usuario', value: jsonEncode({
          'token': _usuario!.token,
          'role': _usuario!.role,
          'nome': _usuario!.nome,
          'refId': _usuario!.refId,
          'deveAlterarSenha': false,
        }));
        notifyListeners();
      }
    } else if (response.statusCode == 422) {
      final body = jsonDecode(response.body);
      throw Exception(body['erro'] ?? 'Erro ao trocar senha');
    } else {
      throw Exception('Erro ao trocar senha');
    }
  }
}
