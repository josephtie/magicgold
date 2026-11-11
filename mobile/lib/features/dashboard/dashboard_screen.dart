import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import 'dashboard_controller.dart';

class DashboardScreen extends ConsumerWidget {
  const DashboardScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final data = ref.watch(dashboardControllerProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('MAGIC GOLD')),
      body: data.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (error, stackTrace) => Center(child: Text('Erreur : $error')),
        data: (d) => ListView(
          padding: const EdgeInsets.all(16),
          children: [
            Text("Aujourd'hui", style: Theme.of(context).textTheme.titleLarge),
            const SizedBox(height: 12),
            _kpi(context, "Chiffre d'affaires", _money(d.reconciliation.salesTotal)),
            _kpi(context, 'Encaissements',
                _money(d.reconciliation.cashTotal + d.reconciliation.digitalTotal)),
            _kpi(context, 'Espèces', _money(d.reconciliation.cashTotal)),
            _kpi(context, 'Paiements digitaux', _money(d.reconciliation.digitalTotal)),
            _kpi(context, 'Impayés / écarts', _money(d.reconciliation.outstanding)),
            _kpi(context, 'Commandes', '${d.sales.orderCount}'),
            const SizedBox(height: 24),
            FilledButton(
              onPressed: () => context.go('/sales'),
              child: const Text('Vendre'),
            ),
            const SizedBox(height: 12),
            OutlinedButton(
              onPressed: () => context.go('/products'),
              child: const Text('Produits'),
            ),
          ],
        ),
      ),
    );
  }

  Widget _kpi(BuildContext context, String label, String value) {
    return Card(
      child: ListTile(
        title: Text(label),
        trailing: Text(value, style: Theme.of(context).textTheme.titleMedium),
      ),
    );
  }

  String _money(double value) => '${value.toStringAsFixed(0)} XOF';
}
