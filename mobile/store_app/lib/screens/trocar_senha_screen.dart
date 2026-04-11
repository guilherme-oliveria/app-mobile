// lib/screens/trocar_senha_screen.dart
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../services/auth_service.dart';

class TrocarSenhaScreen extends StatefulWidget {
  /// Se true, é troca obrigatória (primeiro login) — não pode voltar
  final bool obrigatoria;

  const TrocarSenhaScreen({super.key, this.obrigatoria = false});

  @override
  State<TrocarSenhaScreen> createState() => _TrocarSenhaScreenState();
}

class _TrocarSenhaScreenState extends State<TrocarSenhaScreen> {
  final _senhaAtualCtrl = TextEditingController();
  final _novaSenhaCtrl = TextEditingController();
  final _confirmarCtrl = TextEditingController();
  bool _carregando = false;
  String? _erro;
  bool _ocultarAtual = true;
  bool _ocultarNova = true;

  Future<void> _trocar() async {
    final novaSenha = _novaSenhaCtrl.text.trim();
    final confirmar = _confirmarCtrl.text.trim();

    if (_senhaAtualCtrl.text.trim().isEmpty) {
      setState(() => _erro = 'Informe a senha atual');
      return;
    }
    if (novaSenha.length < 6) {
      setState(() => _erro = 'Nova senha deve ter no mínimo 6 caracteres');
      return;
    }
    if (novaSenha != confirmar) {
      setState(() => _erro = 'As senhas não conferem');
      return;
    }

    setState(() { _carregando = true; _erro = null; });

    try {
      await context.read<AuthService>().trocarSenha(
        _senhaAtualCtrl.text.trim(),
        novaSenha,
      );

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('✅ Senha alterada com sucesso!'),
            backgroundColor: Colors.green,
          ),
        );
        // Se obrigatória (primeiro login), o Consumer no main.dart
        // reconstrói automaticamente para PedidosScreen quando
        // deveAlterarSenha muda para false — não precisa de pop.
        // Se aberta pelo menu, faz pop normal.
        if (!widget.obrigatoria) {
          Navigator.of(context).pop(true);
        }
      }
    } catch (e) {
      setState(() => _erro = e.toString().replaceAll('Exception: ', ''));
    } finally {
      if (mounted) setState(() => _carregando = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Trocar Senha'),
        backgroundColor: const Color(0xFF2563EB),
        foregroundColor: Colors.white,
        automaticallyImplyLeading: !widget.obrigatoria,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            if (widget.obrigatoria) ...[
              const Card(
                color: Color(0xFFE3F2FD),
                child: Padding(
                  padding: EdgeInsets.all(16),
                  child: Row(
                    children: [
                      Icon(Icons.info_outline, color: Color(0xFF2563EB)),
                      SizedBox(width: 12),
                      Expanded(
                        child: Text(
                          'Este é seu primeiro acesso. Por segurança, altere sua senha antes de continuar.',
                          style: TextStyle(fontSize: 14),
                        ),
                      ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 24),
            ],

            TextField(
              controller: _senhaAtualCtrl,
              obscureText: _ocultarAtual,
              decoration: InputDecoration(
                labelText: 'Senha atual',
                prefixIcon: const Icon(Icons.lock_outlined),
                suffixIcon: IconButton(
                  icon: Icon(_ocultarAtual ? Icons.visibility_off : Icons.visibility),
                  onPressed: () => setState(() => _ocultarAtual = !_ocultarAtual),
                ),
                border: const OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 16),

            TextField(
              controller: _novaSenhaCtrl,
              obscureText: _ocultarNova,
              decoration: InputDecoration(
                labelText: 'Nova senha',
                prefixIcon: const Icon(Icons.lock),
                suffixIcon: IconButton(
                  icon: Icon(_ocultarNova ? Icons.visibility_off : Icons.visibility),
                  onPressed: () => setState(() => _ocultarNova = !_ocultarNova),
                ),
                border: const OutlineInputBorder(),
                helperText: 'Mínimo 6 caracteres',
              ),
            ),
            const SizedBox(height: 16),

            TextField(
              controller: _confirmarCtrl,
              obscureText: true,
              decoration: const InputDecoration(
                labelText: 'Confirmar nova senha',
                prefixIcon: Icon(Icons.lock_reset),
                border: OutlineInputBorder(),
              ),
            ),

            if (_erro != null) ...[
              const SizedBox(height: 12),
              Text(_erro!, style: const TextStyle(color: Colors.red, fontSize: 14)),
            ],

            const SizedBox(height: 24),

            SizedBox(
              height: 48,
              child: ElevatedButton(
                onPressed: _carregando ? null : _trocar,
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF2563EB),
                  foregroundColor: Colors.white,
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                ),
                child: _carregando
                    ? const CircularProgressIndicator(color: Colors.white)
                    : const Text('Alterar Senha', style: TextStyle(fontSize: 16)),
              ),
            ),
          ],
        ),
      ),
    );
  }

  @override
  void dispose() {
    _senhaAtualCtrl.dispose();
    _novaSenhaCtrl.dispose();
    _confirmarCtrl.dispose();
    super.dispose();
  }
}

