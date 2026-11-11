import 'dart:async';

/// Jeton OIDC obtenu après le flux PKCE.
class OAuthTokenResult {
  const OAuthTokenResult({
    required this.accessToken,
    required this.refreshToken,
    required this.expiresAt,
  });

  final String accessToken;
  final String? refreshToken;
  final DateTime? expiresAt;

  bool get isExpired =>
      expiresAt != null && expiresAt!.isBefore(DateTime.now().toUtc());
}

/// Port du flux d'authentification OIDC (implémenté par KeycloakOAuthFlow).
abstract class OAuthFlow {
  Future<OAuthTokenResult> authorize();
}
