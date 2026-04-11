// lib/screens/home_screen.dart
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../services/auth_service.dart';
import '../services/entrega_service.dart';
import '../models/models.dart';
import 'entrega_ativa_screen.dart';
import 'trocar_senha_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> with SingleTickerProviderStateMixin {
  late TabController _tabController;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);
    WidgetsBinding.instance.addPostFrameCallback((_) => _carregarTudo());
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  Future<void> _carregarTudo() async {
    final auth = context.read<AuthService>();
    final service = context.read<EntregaService>();
    await Future.wait([
      service.carregarDisponiveis(auth.token),
      service.carregarEntregas(auth.token, auth.usuario!.refId),
    ]);
  }

  /// Motoboy aceita entrega disponível
  Future<void> _aceitarEntrega(Entrega entrega) async {
    final confirmar = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Aceitar entrega?'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('🏪 ${entrega.pedido.lojaNome}'),
            const SizedBox(height: 4),
            Text('📍 ${entrega.pedido.enderecoEntrega}'),
            const SizedBox(height: 4),
            Text(
              '💰 R\$ ${entrega.pedido.valorTotal.toStringAsFixed(2)}',
              style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx, false),
            child: const Text('Cancelar'),
          ),
          ElevatedButton(
            onPressed: () => Navigator.pop(ctx, true),
            style: ElevatedButton.styleFrom(
              backgroundColor: const Color(0xFFFF6B00),
              foregroundColor: Colors.white,
            ),
            child: const Text('ACEITAR'),
          ),
        ],
      ),
    );

    if (confirmar != true) return;

    try {
      final auth = context.read<AuthService>();
      final service = context.read<EntregaService>();
      final aceito = await service.aceitarEntrega(auth.token, entrega.id);

      if (aceito) {
        _mostrarSnack('✅ Entrega aceita! Vá buscar na loja.');
        _tabController.animateTo(1); // Muda para aba "Minhas Entregas"
        await _carregarTudo();
      } else {
        _mostrarSnack('😔 Entrega já foi aceita por outro motoboy', erro: true);
        await service.carregarDisponiveis(auth.token);
      }
    } catch (e) {
      _mostrarSnack(e.toString(), erro: true);
    }
  }

  Color _corStatus(StatusEntrega status) {
    return switch (status) {
      StatusEntrega.DISPONIVEL => Colors.purple,
      StatusEntrega.ATRIBUIDA  => Colors.blue,
      StatusEntrega.COLETADA   => Colors.orange,
      StatusEntrega.FINALIZADA => Colors.green,
      StatusEntrega.CANCELADA  => Colors.red,
    };
  }

  String _labelStatus(StatusEntrega status) {
    return switch (status) {
      StatusEntrega.DISPONIVEL => 'Disponível',
      StatusEntrega.ATRIBUIDA  => 'A caminho da loja',
      StatusEntrega.COLETADA   => 'Em entrega',
      StatusEntrega.FINALIZADA => 'Finalizada',
      StatusEntrega.CANCELADA  => 'Cancelada',
    };
  }

  void _mostrarSnack(String msg, {bool erro = false}) {
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(
      content: Text(msg),
      backgroundColor: erro ? Colors.red : Colors.green,
    ));
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
            onPressed: _carregarTudo,
          ),
          PopupMenuButton<String>(
            icon: const Icon(Icons.more_vert),
            onSelected: (value) {
              if (value == 'trocar_senha') {
                Navigator.push(context,
                  MaterialPageRoute(builder: (_) => const TrocarSenhaScreen()));
              } else if (value == 'sair') {
                auth.logout();
              }
            },
            itemBuilder: (_) => [
              const PopupMenuItem(
                value: 'trocar_senha',
                child: ListTile(
                  leading: Icon(Icons.lock_reset),
                  title: Text('Trocar Senha'),
                  contentPadding: EdgeInsets.zero,
                ),
              ),
              const PopupMenuItem(
                value: 'sair',
                child: ListTile(
                  leading: Icon(Icons.logout, color: Colors.red),
                  title: Text('Sair', style: TextStyle(color: Colors.red)),
                  contentPadding: EdgeInsets.zero,
                ),
              ),
            ],
          ),
        ],
        bottom: TabBar(
          controller: _tabController,
          indicatorColor: Colors.white,
          labelColor: Colors.white,
          unselectedLabelColor: Colors.white70,
          tabs: [
            Tab(
              icon: Badge(
                isLabelVisible: service.disponiveis.isNotEmpty,
                label: Text('${service.disponiveis.length}'),
                child: const Icon(Icons.local_shipping),
              ),
              text: 'Disponíveis',
            ),
            const Tab(icon: Icon(Icons.history), text: 'Minhas Entregas'),
          ],
        ),
      ),
      body: service.carregando
          ? const Center(child: CircularProgressIndicator())
          : TabBarView(
              controller: _tabController,
              children: [
                // ── Tab 1: Entregas disponíveis para aceitar ──
                RefreshIndicator(
                  onRefresh: () async {
                    final auth = context.read<AuthService>();
                    await context.read<EntregaService>().carregarDisponiveis(auth.token);
                  },
                  child: service.disponiveis.isEmpty
                      ? const Center(
                          child: Column(
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              Icon(Icons.check_circle, size: 64, color: Colors.grey),
                              SizedBox(height: 12),
                              Text('Nenhuma entrega disponível no momento.',
                                  style: TextStyle(fontSize: 16, color: Colors.grey)),
                              SizedBox(height: 4),
                              Text('Aguarde — você será notificado!',
                                  style: TextStyle(fontSize: 13, color: Colors.grey)),
                            ],
                          ),
                        )
                      : ListView.builder(
                          padding: const EdgeInsets.all(12),
                          itemCount: service.disponiveis.length,
                          itemBuilder: (ctx, i) {
                            final e = service.disponiveis[i];
                            return Card(
                              margin: const EdgeInsets.only(bottom: 10),
                              elevation: 3,
                              shape: RoundedRectangleBorder(
                                borderRadius: BorderRadius.circular(12),
                                side: const BorderSide(color: Color(0xFFFF6B00), width: 1),
                              ),
                              child: Padding(
                                padding: const EdgeInsets.all(12),
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Row(
                                      children: [
                                        const Icon(Icons.store, color: Color(0xFFFF6B00), size: 20),
                                        const SizedBox(width: 6),
                                        Expanded(
                                          child: Text(e.pedido.lojaNome,
                                              style: const TextStyle(
                                                  fontWeight: FontWeight.bold, fontSize: 16)),
                                        ),
                                        Text(
                                          'R\$ ${e.pedido.valorTotal.toStringAsFixed(2)}',
                                          style: const TextStyle(
                                              fontSize: 18,
                                              fontWeight: FontWeight.bold,
                                              color: Color(0xFFFF6B00)),
                                        ),
                                      ],
                                    ),
                                    const SizedBox(height: 8),
                                    Row(
                                      children: [
                                        const Icon(Icons.person, size: 16, color: Colors.grey),
                                        const SizedBox(width: 4),
                                        Text(e.pedido.clienteNome),
                                      ],
                                    ),
                                    const SizedBox(height: 4),
                                    Row(
                                      children: [
                                        const Icon(Icons.location_on, size: 16, color: Colors.grey),
                                        const SizedBox(width: 4),
                                        Expanded(
                                          child: Text(e.pedido.enderecoEntrega,
                                              maxLines: 2, overflow: TextOverflow.ellipsis),
                                        ),
                                      ],
                                    ),
                                    const SizedBox(height: 12),
                                    SizedBox(
                                      width: double.infinity,
                                      height: 44,
                                      child: ElevatedButton.icon(
                                        onPressed: () => _aceitarEntrega(e),
                                        icon: const Icon(Icons.check_circle),
                                        label: const Text('ACEITAR ENTREGA',
                                            style: TextStyle(fontSize: 15, fontWeight: FontWeight.bold)),
                                        style: ElevatedButton.styleFrom(
                                          backgroundColor: const Color(0xFFFF6B00),
                                          foregroundColor: Colors.white,
                                          shape: RoundedRectangleBorder(
                                              borderRadius: BorderRadius.circular(10)),
                                        ),
                                      ),
                                    ),
                                  ],
                                ),
                              ),
                            );
                          },
                        ),
                ),

                // ── Tab 2: Minhas entregas (ativas + historial) ──
                RefreshIndicator(
                  onRefresh: _carregarTudo,
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
                                        ).then((_) => _carregarTudo())
                                    : null,
                              ),
                            );
                          },
                        ),
                ),
              ],
            ),
    );
  }
}
