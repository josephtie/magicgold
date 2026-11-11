import 'package:dio/dio.dart';

import '../../core/storage/app_database.dart';
import '../../shared/models/product.dart';

/// Accès aux produits : API distante + cache local Drift (offline-first).
class ProductRepository {
  ProductRepository(this._db, this._dio);

  final AppDatabase _db;
  final Dio _dio;

  Future<List<Product>> fetchRemote() async {
    final response = await _dio.get<List<dynamic>>('/api/v1/products');
    final data = response.data ?? const <dynamic>[];
    return data
        .map((e) => Product.fromJson(e as Map<String, dynamic>))
        .toList();
  }

  Future<void> cache(List<Product> products) async {
    await _db.batch((batch) {
      batch.deleteAll(_db.localProducts);
      batch.insertAll(
        _db.localProducts,
        products
            .map((p) => LocalProductsCompanion.insert(
                  id: p.id,
                  name: p.name,
                  unitPrice: p.unitPrice,
                  currency: p.currency,
                  updatedAt: DateTime.now().toUtc(),
                ))
            .toList(),
      );
    });
  }

  Future<List<Product>> loadCached() async {
    final rows = await _db.select(_db.localProducts).get();
    return rows
        .map((r) => Product(
              id: r.id,
              name: r.name,
              unitPrice: r.unitPrice,
              currency: r.currency,
            ))
        .toList();
  }
}
