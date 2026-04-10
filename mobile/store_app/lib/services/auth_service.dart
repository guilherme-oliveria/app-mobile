// lib/services/auth_service.dart
import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:http/http.dart' as http;

const _baseUrl = 'http://10.0.2.2:8080/api';

class Usuario {
  final String token;
  final String role;
  final String nome;
  final int refId;
  const Usuario({required this.token, required this.role, required this.nome, required this.refId});
  factory Usuario.fromJson(Map<String, dynamic> j) =>
      Usuario(token: j['token'], role: j['role'], nome: j['nome'], refId: j['refId'] ?? 0);
}

class AuthService extends ChangeNotifier {
  final _storage = const FlutterSecureStorage();
  Usuario? _usuario;

  bool get isLoggedIn => _usuario != null;
  Usuario? get usuario => _usuario;
  String get token => _usuario?.token ?? '';

  void Function()? logoutCallback;

  AuthService() { _carregarDoStorage(); }

  Future<void> _carregarDoStorage() async {
    final dados = await _storage.read(key: 'usuario');
    if (dados != null) { _usuario = Usuario.fromJson(jsonDecode(dados)); notifyListeners(); }
  }

  Future<void> login(String email, String senha) async {
    final res = await http.post(
      Uri.parse('$_baseUrl/auth/login'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({'email': email, 'senha': senha}),
    );
    if (res.statusCode == 200) {
      final json = jsonDecode(res.body);
      _usuario = Usuario.fromJson(json);
      await _storage.write(key: 'usuario', value: jsonEncode(json));
      notifyListeners();
    } else {
      throw Exception('Credenciais inválidas');
    }
  }

  Future<void> logout() async {
    _usuario = null;
    await _storage.deleteAll();
    notifyListeners();
  }
}
