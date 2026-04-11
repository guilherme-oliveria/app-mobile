// lib/services/auth_service.dart
import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:flutter/foundation.dart' show kIsWeb;
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:http/http.dart' as http;

// Browser (Chrome/Edge) → localhost | Emulador Android → 10.0.2.2
const _baseUrl = kIsWeb
    ? 'http://localhost:8080/api'
    : 'http://10.0.2.2:8080/api';

class Usuario {
  final String token;
  final String role;
  final String nome;
  final int refId;
  final bool deveAlterarSenha;
  const Usuario({
    required this.token,
    required this.role,
    required this.nome,
    required this.refId,
    this.deveAlterarSenha = false,
  });
  factory Usuario.fromJson(Map<String, dynamic> j) => Usuario(
      token: j['token'],
      role: j['role'],
      nome: j['nome'],
      refId: j['refId'] ?? 0,
      deveAlterarSenha: j['deveAlterarSenha'] ?? false);
}

class AuthService extends ChangeNotifier {
  final _storage = const FlutterSecureStorage();
  Usuario? _usuario;

  bool get isLoggedIn => _usuario != null;
  Usuario? get usuario => _usuario;
  String get token => _usuario?.token ?? '';

  AuthService() { _carregarDoStorage(); }

  Future<void> _carregarDoStorage() async {
    final dados = await _storage.read(key: 'usuario');
    if (dados != null) { _usuario = Usuario.fromJson(jsonDecode(dados)); notifyListeners(); }
  }

  /// Roles permitidos neste app
  static const _rolesPermitidos = ['LOJA', 'ADMIN'];

  Future<void> login(String email, String senha) async {
    final res = await http.post(
      Uri.parse('$_baseUrl/auth/login'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({'email': email, 'senha': senha}),
    );
    if (res.statusCode == 200) {
      final json = jsonDecode(res.body);
      final role = json['role'] as String? ?? '';

      // ── Validação de perfil ──────────────────────────────
      if (!_rolesPermitidos.contains(role)) {
        String msg;
        if (role == 'MOTOBOY') {
          msg = 'Este é o app da Loja. Use o app do Motoboy para acessar.';
        } else if (role == 'SUPORTE') {
          msg = 'Acesso de suporte disponível apenas no painel web.';
        } else {
          msg = 'Seu perfil ($role) não tem acesso a este aplicativo.';
        }
        throw Exception(msg);
      }

      _usuario = Usuario.fromJson(json);
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
