import 'package:dio/dio.dart';
import 'package:drift/native.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:magic_gold/core/storage/app_database.dart';
import 'package:magic_gold/features/products/product_list_screen.dart';
import 'package:magic_gold/features/products/product_repository.dart';
import 'package:magic_gold/features/products/products_controller.dart';
import 'package:magic_gold/shared/models/product.dart';

class _FakeProductRepository extends ProductRepository {
  _FakeProductRepository() : super(AppDatabase(NativeDatabase.memory()), Dio());

  @override
  Future<List<Product>> fetchRemote() async => const [
        Product(id: '1', name: 'Riz 25kg', unitPrice: 18500, currency: 'XOF'),
        Product(id: '2', name: 'Huile 5L', unitPrice: 7200, currency: 'XOF'),
      ];

  @override
  Future<void> cache(List<Product> products) async {}
}

void main() {
  testWidgets('affiche la liste des produits', (tester) async {
    await tester.pumpWidget(
      ProviderScope(
        overrides: [
          productRepositoryProvider.overrideWithValue(_FakeProductRepository()),
        ],
        child: const MaterialApp(home: ProductListScreen()),
      ),
    );
    await tester.pump();
    await tester.pump();

    expect(find.text('Riz 25kg'), findsOneWidget);
    expect(find.text('Huile 5L'), findsOneWidget);
    expect(find.text('18500.0 XOF'), findsOneWidget);
  });
}
