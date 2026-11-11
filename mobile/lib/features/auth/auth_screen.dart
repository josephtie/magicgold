import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'auth_controller.dart';

/// Écran de connexion Keycloak (OIDC PKCE).
class AuthScreen extends ConsumerWidget {
  const AuthScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final state = ref.watch(authControllerProvider);
    final isLoggedIn = state is AuthAuthenticated;

    return Scaffold(
      appBar: AppBar(title: const Text('Connexion')),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text(isLoggedIn ? 'Connecté' : 'Connexion Keycloak'),
            const SizedBox(height: 12),
            FilledButton(
              onPressed: () => isLoggedIn
                  ? ref.read(authControllerProvider.notifier).logout()
                  : ref.read(authControllerProvider.notifier).login(),
              child: Text(isLoggedIn ? 'Se déconnecter' : 'Se connecter'),
            ),
          ],
        ),
      ),
    );
  }
}
