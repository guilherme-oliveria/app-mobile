import 'package:flutter/services.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import 'package:store_app/main.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  const secureStorageChannel = MethodChannel(
    'plugins.it_nomads.com/flutter_secure_storage',
  );

  setUpAll(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(secureStorageChannel, (call) async {
          if (call.method == 'read') return null;
          return null;
        });
  });

  tearDownAll(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(secureStorageChannel, null);
  });

  testWidgets('Renderiza a tela de login ao iniciar deslogado', (
    WidgetTester tester,
  ) async {
    await tester.pumpWidget(const StoreApp());
    await tester.pumpAndSettle();

    expect(find.text('Loja App'), findsOneWidget);
    expect(find.text('Entrar'), findsOneWidget);
    expect(find.byIcon(Icons.storefront), findsOneWidget);
  });
}
