import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/api/api_client_provider.dart';
import '../../core/storage/database_provider.dart';
import '../../shared/models/product.dart';
import 'product_repository.dart';

final productRepositoryProvider = Provider<ProductRepository>((ref) {
  return ProductRepository(
    ref.watch(databaseProvider),
    ref.watch(apiClientProvider).dio,
  );
});

final productsControllerProvider =
    AsyncNotifierProvider<ProductsController, List<Product>>(ProductsController.new);

/// Charge les produits : API d'abord, repli sur le cache local hors connexion.
class ProductsController extends AsyncNotifier<List<Product>> {
  @override
  Future<List<Product>> build() async {
    final repository = ref.read(productRepositoryProvider);
    try {
      final remote = await repository.fetchRemote();
      await repository.cache(remote);
      return remote;
    } on Exception {
      return repository.loadCached();
    }
  }
}
