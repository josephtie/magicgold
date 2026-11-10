import 'package:flutter/material.dart';

/// Écran d'authentification Keycloak (OIDC PKCE) — à câbler sur le flux réel.
class AuthScreen extends StatelessWidget {
  const AuthScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Connexion')),
      body: const Center(child: Text('Connexion Keycloak (à implémenter)')),
    );
  }
}
