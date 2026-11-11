import 'dart:ffi';

import 'package:dio/dio.dart';
import 'package:drift/native.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:magic_gold/core/storage/app_database.dart';
import 'package:magic_gold/features/products/product_repository.dart';
import 'package:magic_gold/shared/models/product.dart';
import 'package:sqlite3/open.dart';

void main() {
  open.overrideFor(OperatingSystem.linux, () => DynamicLibrary.open('libsqlite3.so.0'));

  test('cache puis lit les produits depuis la base locale', () async {
    final db = AppDatabase(NativeDatabase.memory());
    addTearDown(db.close);
    final repository = ProductRepository(db, Dio());

    await repository.cache([
      const Product(id: '1', name: 'Riz 25kg', unitPrice: 18500, currency: 'XOF'),
      const Product(id: '2', name: 'Huile 5L', unitPrice: 7200, currency: 'XOF'),
    ]);

    final cached = await repository.loadCached();

    expect(cached, hasLength(2));
    expect(cached.first.name, 'Riz 25kg');
    expect(cached.last.currency, 'XOF');
  });
}
