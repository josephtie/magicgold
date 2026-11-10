import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:magic_gold/features/dashboard/dashboard_screen.dart';

void main() {
  testWidgets('Dashboard affiche le titre et les actions', (tester) async {
    await tester.pumpWidget(const MaterialApp(home: DashboardScreen()));

    expect(find.text('MAGIC GOLD'), findsOneWidget);
    expect(find.text('Votre commerce dans votre téléphone.'), findsOneWidget);
    expect(find.text('Produits'), findsOneWidget);
    expect(find.text('Vendre'), findsOneWidget);
  });
}
