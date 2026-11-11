import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'products_controller.dart';

class ProductListScreen extends ConsumerWidget {
  const ProductListScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final products = ref.watch(productsControllerProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('Produits')),
      body: products.when(
        data: (list) => list.isEmpty
            ? const Center(child: Text('Aucun produit'))
            : ListView.separated(
                itemCount: list.length,
                separatorBuilder: (context, index) => const Divider(height: 1),
                itemBuilder: (context, index) {
                  final product = list[index];
                  return ListTile(
                    title: Text(product.name),
                    subtitle: Text('${product.unitPrice} ${product.currency}'),
                  );
                },
              ),
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (error, stackTrace) => Center(child: Text('Erreur : $error')),
      ),
    );
  }
}
