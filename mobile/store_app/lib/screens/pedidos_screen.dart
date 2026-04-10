// lib/screens/pedidos_screen.dart
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../services/auth_service.dart';
import '../services/pedido_service.dart';

class PedidosScreen extends StatefulWidget {
  const PedidosScreen({super.key});
  @override
  State<PedidosScreen> createState() => _PedidosScreenState();
}

class _PedidosScreenState extends State<PedidosScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => _carregar());
  }

  Future<void> _carregar() async {
    final auth = context.read<AuthService>();
    await context.read<PedidoService>().carregar(auth.token, auth.usuario!.refId);
  }

  Color _corStatus(String status) {
    return switch (status) {
      'AGUARDANDO_ACEITE'       => Colors.amber,
      'ACEITO'                  => Colors.blue,
      'MOTOBOY_A_CAMINHO_LOJA'  => Colors.indigo,
      'COLETADO'                => Colors.orange,
      'EM_ENTREGA'              => Colors.deepOrange,
      'ENTREGUE'                => Colors.green,
      'CANCELADO'               => Colors.red,
      _                         => Colors.grey,
    };
  }

  String _labelStatus(String status) {
    return switch (status) {
      'AGUARDANDO_ACEITE'       => '⏳ Aguardando',
      'ACEITO'                  => '✅ Aceito',
      'MOTOBOY_A_CAMINHO_LOJA'  => '🛵 Motoboy a caminho',
      'COLETADO'                => '📦 Coletado',
      'EM_ENTREGA'              => '🚀 Em entrega',
      'ENTREGUE'                => '✅ Entregue',
      'CANCELADO'               => '❌ Cancelado',
      _                         => status,
    };
  }

  @override
  Widget build(BuildContext context) {
    final auth = context.watch<AuthService>();
    final service = context.watch<PedidoService>();

    return DefaultTabController(
      length: 2,
      child: Scaffold(
        appBar: AppBar(
          title: const Text('Meus Pedidos'),
          backgroundColor: const Color(0xFF2563EB),
          foregroundColor: Colors.white,
          actions: [
            IconButton(icon: const Icon(Icons.refresh), onPressed: _carregar),
            IconButton(icon: const Icon(Icons.logout), onPressed: auth.logout),
          ],
          bottom: const TabBar(
            labelColor: Colors.white,
            unselectedLabelColor: Colors.white60,
            indicatorColor: Colors.white,
            tabs: [
              Tab(text: 'Ativos'),
              Tab(text: 'Todos'),
            ],
          ),
        ),
        body: service.carregando
            ? const Center(child: CircularProgressIndicator())
            : TabBarView(
                children: [
                  _listaPedidos(service.ativos),
                  _listaPedidos(service.pedidos),
                ],
              ),
        floatingActionButton: FloatingActionButton.extended(
          onPressed: () => _abrirNovoPedido(context),
          backgroundColor: const Color(0xFF2563EB),
          foregroundColor: Colors.white,
          icon: const Icon(Icons.add),
          label: const Text('Novo Pedido'),
        ),
      ),
    );
  }

  Widget _listaPedidos(List<Pedido> pedidos) {
    if (pedidos.isEmpty) {
      return const Center(child: Text('Nenhum pedido.', style: TextStyle(color: Colors.grey)));
    }
    return RefreshIndicator(
      onRefresh: _carregar,
      child: ListView.builder(
        padding: const EdgeInsets.all(12),
        itemCount: pedidos.length,
        itemBuilder: (ctx, i) {
          final p = pedidos[i];
          return Card(
            margin: const EdgeInsets.only(bottom: 10),
            child: ListTile(
              leading: CircleAvatar(
                backgroundColor: _corStatus(p.status),
                child: Text('#${p.id}', style: const TextStyle(color: Colors.white, fontSize: 11)),
              ),
              title: Text(p.clienteNome, style: const TextStyle(fontWeight: FontWeight.bold)),
              subtitle: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(p.enderecoEntrega, maxLines: 1, overflow: TextOverflow.ellipsis),
                  Text(
                    'R\$ ${p.valorTotal.toStringAsFixed(2)}  •  ${_labelStatus(p.status)}',
                    style: TextStyle(color: _corStatus(p.status), fontWeight: FontWeight.w600),
                  ),
                ],
              ),
              isThreeLine: true,
            ),
          );
        },
      ),
    );
  }

  void _abrirNovoPedido(BuildContext context) {
    // TODO: navegar para tela de criação de pedido
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('Tela de novo pedido - a implementar')),
    );
  }
}
