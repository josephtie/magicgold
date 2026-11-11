import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../shared/models/order.dart';
import '../../shared/models/product.dart';
import '../products/products_controller.dart';
import 'sales_providers.dart';

class SalesScreen extends ConsumerStatefulWidget {
  const SalesScreen({super.key});

  @override
  ConsumerState<SalesScreen> createState() => _SalesScreenState();
}

class _SalesScreenState extends ConsumerState<SalesScreen> {
  final Map<String, int> _cart = {};

  double _total(List<Product> products) {
    var total = 0.0;
    _cart.forEach((id, qty) {
      final product = products.firstWhere((p) => p.id == id);
      total += product.unitPrice * qty;
    });
    return total;
  }

  Future<void> _sell(List<Product> products) async {
    if (_cart.isEmpty) {
      return;
    }
    final items = _cart.entries
        .map((e) => OrderItemRequest(productId: e.key, quantity: e.value))
        .toList();
    final ok = await ref.read(salesServiceProvider).submit(OrderRequest(items: items));
    if (!mounted) {
      return;
    }
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(ok ? 'Vente enregistrée' : 'Hors ligne — vente mise en file'),
      ),
    );
    setState(_cart.clear);
  }

  @override
  Widget build(BuildContext context) {
    final productsAsync = ref.watch(productsControllerProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('Vendre')),
      body: productsAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (error, stackTrace) => Center(child: Text('Erreur : $error')),
        data: (products) {
          if (products.isEmpty) {
            return const Center(child: Text('Aucun produit'));
          }
          return Column(
            children: [
              Expanded(
                child: ListView.builder(
                  itemCount: products.length,
                  itemBuilder: (context, index) {
                    final product = products[index];
                    final qty = _cart[product.id] ?? 0;
                    return ListTile(
                      title: Text(product.name),
                      subtitle: Text('${product.unitPrice} ${product.currency}'),
                      trailing: Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          IconButton(
                            icon: const Icon(Icons.remove_circle_outline),
                            onPressed: () => setState(() {
                              final current = _cart[product.id] ?? 0;
                              if (current > 1) {
                                _cart[product.id] = current - 1;
                              } else {
                                _cart.remove(product.id);
                              }
                            }),
                          ),
                          Text('$qty'),
                          IconButton(
                            icon: const Icon(Icons.add_circle_outline),
                            onPressed: () => setState(() {
                              _cart[product.id] = (_cart[product.id] ?? 0) + 1;
                            }),
                          ),
                        ],
                      ),
                    );
                  },
                ),
              ),
              SafeArea(
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: Row(
                    children: [
                      Text(
                        'Total : ${_total(products)} XOF',
                        style: Theme.of(context).textTheme.titleMedium,
                      ),
                      const Spacer(),
                      FilledButton(
                        onPressed: _cart.isEmpty ? null : () => _sell(products),
                        child: const Text('Vendre'),
                      ),
                    ],
                  ),
                ),
              ),
            ],
          );
        },
      ),
    );
  }
}
