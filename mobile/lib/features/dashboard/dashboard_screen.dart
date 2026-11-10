import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

class DashboardScreen extends StatelessWidget {
  const DashboardScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('MAGIC GOLD')),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Text('Votre commerce dans votre téléphone.'),
            const SizedBox(height: 24),
            FilledButton(
              onPressed: () => context.go('/products'),
              child: const Text('Produits'),
            ),
            const SizedBox(height: 12),
            FilledButton(
              onPressed: () => context.go('/sales'),
              child: const Text('Vendre'),
            ),
          ],
        ),
      ),
    );
  }
}
