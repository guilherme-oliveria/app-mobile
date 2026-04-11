import 'package:flutter/material.dart';
// import 'package:firebase_core/firebase_core.dart'; // TODO: descomentar quando configurar Firebase
import 'package:provider/provider.dart';
import 'services/auth_service.dart';
import 'services/pedido_service.dart';
import 'screens/login_screen.dart';
import 'screens/pedidos_screen.dart';
import 'screens/trocar_senha_screen.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  // await Firebase.initializeApp(); // TODO: descomentar quando configurar Firebase (google-services.json)
  runApp(const StoreApp());
}

class StoreApp extends StatelessWidget {
  const StoreApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => AuthService()),
        ChangeNotifierProvider(create: (_) => PedidoService()),
      ],
      child: MaterialApp(
        title: 'Loja App',
        debugShowCheckedModeBanner: false,
        theme: ThemeData(
          colorScheme: ColorScheme.fromSeed(seedColor: const Color(0xFF2563EB)),
          useMaterial3: true,
        ),
        home: Consumer<AuthService>(
          builder: (ctx, auth, _) {
            if (!auth.isLoggedIn) return const LoginScreen();
            if (auth.usuario!.deveAlterarSenha) {
              return const TrocarSenhaScreen(obrigatoria: true);
            }
            return const PedidosScreen();
          },
        ),
      ),
    );
  }
}
