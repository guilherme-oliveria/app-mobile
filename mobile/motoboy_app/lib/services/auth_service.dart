// lib/services/auth_service.dart
import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:http/http.dart' as http;
import '../models/models.dart';

const _baseUrl = 'http://10.0.2.2:8080/api'; // 10.0.2.2 = localhost no emulador Android
// Em produção: const _baseUrl = 'https://sua-api.com/api';

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

  Future<void> login(String email, String senha) async {
    final response = await http.post(
      Uri.parse('$_baseUrl/auth/login'),
      headers: {'Content-Type': 'application/json'},
      body: jsonEncode({'email': email, 'senha': senha}),
    );

    if (response.statusCode == 200) {
      final json = jsonDecode(response.body);
      _usuario = Usuario.fromJson(json);
      await _storage.write(key: 'token', value: _usuario!.token);
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
