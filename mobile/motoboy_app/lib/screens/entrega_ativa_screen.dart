// lib/screens/entrega_ativa_screen.dart
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/models.dart';
import '../services/auth_service.dart';
import '../services/entrega_service.dart';

class EntregaAtivaScreen extends StatefulWidget {
  final Entrega entrega;
  const EntregaAtivaScreen({super.key, required this.entrega});

  @override
  State<EntregaAtivaScreen> createState() => _EntregaAtivaScreenState();
}

class _EntregaAtivaScreenState extends State<EntregaAtivaScreen> {
  late Entrega _entrega;
  bool _processando = false;

  @override
  void initState() {
    super.initState();
    _entrega = widget.entrega;
  }

  Future<void> _confirmarColeta() async {
    final auth = context.read<AuthService>();
    final service = context.read<EntregaService>();
    setState(() => _processando = true);
    try {
      await service.confirmarColeta(auth.token, _entrega.id);
      setState(() => _entrega = Entrega(
            id: _entrega.id,
            pedido: _entrega.pedido,
            status: StatusEntrega.COLETADA,
          ));
      _mostrarSnack('✅ Coleta confirmada! Vá até o cliente.');
    } catch (e) {
      _mostrarSnack('Erro ao confirmar coleta', erro: true);
    } finally {
      setState(() => _processando = false);
    }
  }

  /// Abre diálogo de confirmação de pagamento
  /// Na integração real, o SDK da maquininha (Stone/Pagar.me) é chamado aqui
  Future<void> _abrirPagamento() async {
    final confirmar = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Liberar pagamento'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Cliente: ${_entrega.pedido.clienteNome}'),
            const SizedBox(height: 8),
            Text(
              'Valor: R\$ ${_entrega.pedido.valorTotal.toStringAsFixed(2)}',
              style: const TextStyle(
                  fontSize: 20, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 16),
            const Text(
              '⚠️ A maquininha será ativada para cobrança.\n'
              'Aguarde o cliente realizar o pagamento.',
              style: TextStyle(fontSize: 13, color: Colors.grey),
            ),
          ],
        ),
        actions: [
          TextButton(
              onPressed: () => Navigator.pop(ctx, false),
              child: const Text('Cancelar')),
          ElevatedButton(
            onPressed: () => Navigator.pop(ctx, true),
            style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFFFF6B00),
                foregroundColor: Colors.white),
            child: const Text('Ativar maquininha'),
          ),
        ],
      ),
    );

    if (confirmar != true) return;

    setState(() => _processando = true);
    try {
      final auth = context.read<AuthService>();
      final service = context.read<EntregaService>();

      // TODO: integrar SDK da maquininha aqui (Stone SmartPOS SDK)
      // O SDK retorna sucesso → chamamos o backend para registrar
      await service.processarPagamento(
        token: auth.token,
        entregaId: _entrega.id,
        valor: _entrega.pedido.valorTotal,
        smartPosSerial: 'SERIAL_DA_MAQUININHA', // ler do SharedPreferences
        formaPagamento: 'credit_card',
      );

      // Finaliza a entrega
      await service.finalizarEntrega(auth.token, _entrega.id, 'CONF01');

      if (mounted) {
        _mostrarSnack('✅ Pagamento aprovado! Entrega finalizada.');
        Navigator.pop(context);
      }
    } catch (e) {
      _mostrarSnack('Erro ao processar pagamento', erro: true);
    } finally {
      if (mounted) setState(() => _processando = false);
    }
  }

  void _mostrarSnack(String msg, {bool erro = false}) {
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(
      content: Text(msg),
      backgroundColor: erro ? Colors.red : Colors.green,
    ));
  }

  @override
  Widget build(BuildContext context) {
    final pedido = _entrega.pedido;
    final coletado = _entrega.status == StatusEntrega.COLETADA;

    return Scaffold(
      appBar: AppBar(
        title: Text('Entrega #${_entrega.id}'),
        backgroundColor: const Color(0xFFFF6B00),
        foregroundColor: Colors.white,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Status
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: coletado
                    ? Colors.orange.shade50
                    : Colors.blue.shade50,
                borderRadius: BorderRadius.circular(8),
                border: Border.all(
                    color: coletado ? Colors.orange : Colors.blue),
              ),
              child: Text(
                coletado
                    ? '🛵 Em rota de entrega'
                    : '📦 Ir buscar na loja: ${pedido.lojaNome}',
                style: const TextStyle(
                    fontWeight: FontWeight.bold, fontSize: 15),
              ),
            ),

            const SizedBox(height: 20),

            // Dados do cliente
            _secaoTitulo('Dados do cliente'),
            _infoRow(Icons.person, pedido.clienteNome),
            _infoRow(Icons.phone, pedido.clienteTelefone),
            _infoRow(Icons.location_on, pedido.enderecoEntrega),

            const SizedBox(height: 20),

            // Itens
            _secaoTitulo('Itens do pedido'),
            ...pedido.itens.map((item) => Padding(
                  padding: const EdgeInsets.symmetric(vertical: 4),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text('${item.quantidade}x ${item.descricao}'),
                      Text(
                        'R\$ ${(item.valorUnitario * item.quantidade).toStringAsFixed(2)}',
                        style: const TextStyle(fontWeight: FontWeight.w600),
                      ),
                    ],
                  ),
                )),

            const Divider(height: 24),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text('Total',
                    style: TextStyle(
                        fontSize: 18, fontWeight: FontWeight.bold)),
                Text(
                  'R\$ ${pedido.valorTotal.toStringAsFixed(2)}',
                  style: const TextStyle(
                      fontSize: 18,
                      fontWeight: FontWeight.bold,
                      color: Color(0xFFFF6B00)),
                ),
              ],
            ),

            const SizedBox(height: 32),

            // Botão de ação principal
            SizedBox(
              width: double.infinity,
              height: 56,
              child: ElevatedButton.icon(
                onPressed: _processando
                    ? null
                    : (coletado ? _abrirPagamento : _confirmarColeta),
                icon: Icon(coletado
                    ? Icons.payment
                    : Icons.check_circle_outline),
                label: Text(
                  coletado
                      ? '💳 Cobrar R\$ ${pedido.valorTotal.toStringAsFixed(2)}'
                      : '✅ Confirmar coleta na loja',
                  style: const TextStyle(fontSize: 16),
                ),
                style: ElevatedButton.styleFrom(
                  backgroundColor:
                      coletado ? Colors.green : const Color(0xFFFF6B00),
                  foregroundColor: Colors.white,
                  shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(12)),
                ),
              ),
            ),

            if (_processando)
              const Padding(
                padding: EdgeInsets.only(top: 16),
                child: Center(child: CircularProgressIndicator()),
              ),
          ],
        ),
      ),
    );
  }

  Widget _secaoTitulo(String titulo) => Padding(
        padding: const EdgeInsets.only(bottom: 8),
        child: Text(titulo,
            style: const TextStyle(
                fontWeight: FontWeight.bold,
                fontSize: 14,
                color: Colors.grey)),
      );

  Widget _infoRow(IconData icon, String texto) => Padding(
        padding: const EdgeInsets.symmetric(vertical: 4),
        child: Row(
          children: [
            Icon(icon, size: 18, color: const Color(0xFFFF6B00)),
            const SizedBox(width: 8),
            Expanded(child: Text(texto)),
          ],
        ),
      );
}
