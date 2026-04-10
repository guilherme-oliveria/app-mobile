// lib/screens/home_screen.dart
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../services/auth_service.dart';
import '../services/entrega_service.dart';
import '../models/models.dart';
import 'entrega_ativa_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => _carregar());
  }

  Future<void> _carregar() async {
    final auth = context.read<AuthService>();
    final entregaService = context.read<EntregaService>();
    await entregaService.carregarEntregas(auth.token, auth.usuario!.refId);
  }

  Color _corStatus(StatusEntrega status) {
    return switch (status) {
      StatusEntrega.ATRIBUIDA  => Colors.blue,
      StatusEntrega.COLETADA   => Colors.orange,
      StatusEntrega.FINALIZADA => Colors.green,
      StatusEntrega.CANCELADA  => Colors.red,
      _                        => Colors.grey,
    };
  }

  String _labelStatus(StatusEntrega status) {
    return switch (status) {
      StatusEntrega.PENDENTE   => 'Pendente',
      StatusEntrega.ATRIBUIDA  => 'A caminho da loja',
      StatusEntrega.COLETADA   => 'Em entrega',
      StatusEntrega.FINALIZADA => 'Finalizada',
      StatusEntrega.CANCELADA  => 'Cancelada',
    };
  }

  @override
  Widget build(BuildContext context) {
    final auth = context.watch<AuthService>();
    final service = context.watch<EntregaService>();

    return Scaffold(
      appBar: AppBar(
        title: Text('Olá, ${auth.usuario?.nome ?? ''}'),
        backgroundColor: const Color(0xFFFF6B00),
        foregroundColor: Colors.white,
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _carregar,
          ),
          IconButton(
            icon: const Icon(Icons.logout),
            onPressed: () => auth.logout(),
          ),
        ],
      ),
      body: service.carregando
          ? const Center(child: CircularProgressIndicator())
          : RefreshIndicator(
              onRefresh: _carregar,
              child: service.entregas.isEmpty
                  ? const Center(
                      child: Text('Nenhuma entrega ainda hoje.',
                          style: TextStyle(fontSize: 16, color: Colors.grey)))
                  : ListView.builder(
                      padding: const EdgeInsets.all(12),
                      itemCount: service.entregas.length,
                      itemBuilder: (ctx, i) {
                        final e = service.entregas[i];
                        return Card(
                          margin: const EdgeInsets.only(bottom: 10),
                          child: ListTile(
                            leading: CircleAvatar(
                              backgroundColor: _corStatus(e.status),
                              child: Text('#${e.id}',
                                  style: const TextStyle(
                                      color: Colors.white, fontSize: 12)),
                            ),
                            title: Text(e.pedido.clienteNome,
                                style: const TextStyle(
                                    fontWeight: FontWeight.bold)),
                            subtitle: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(e.pedido.enderecoEntrega,
                                    maxLines: 1,
                                    overflow: TextOverflow.ellipsis),
                                Text(
                                  'R\$ ${e.pedido.valorTotal.toStringAsFixed(2)}  •  ${_labelStatus(e.status)}',
                                  style: TextStyle(
                                      color: _corStatus(e.status),
                                      fontWeight: FontWeight.w600),
                                ),
                              ],
                            ),
                            trailing: (e.status == StatusEntrega.ATRIBUIDA ||
                                    e.status == StatusEntrega.COLETADA)
                                ? const Icon(Icons.arrow_forward_ios,
                                    size: 16)
                                : null,
                            onTap: (e.status == StatusEntrega.ATRIBUIDA ||
                                    e.status == StatusEntrega.COLETADA)
                                ? () => Navigator.push(
                                      context,
                                      MaterialPageRoute(
                                        builder: (_) =>
                                            EntregaAtivaScreen(entrega: e),
                                      ),
                                    ).then((_) => _carregar())
                                : null,
                          ),
                        );
                      },
                    ),
            ),
    );
  }
}
