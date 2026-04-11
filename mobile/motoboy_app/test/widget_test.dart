import 'package:flutter_test/flutter_test.dart';
import 'package:flutter/widgets.dart';

import 'package:motoboy_app/main.dart';

void main() {
  test('MotoboyApp smoke test', () {
    const app = MotoboyApp();
    expect(app, isA<StatelessWidget>());
  });
}

