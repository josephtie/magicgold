/// Configuration globale de l'application MAGIC GOLD.
class AppConfig {
  AppConfig._();

  /// URL de base de l'API backend (surchargeable via --dart-define=API_BASE_URL=...).
  /// 10.0.2.2 pointe vers localhost depuis l'émulateur Android.
  static const String apiBaseUrl = String.fromEnvironment(
    'API_BASE_URL',
    defaultValue: 'http://10.0.2.2:8080',
  );

  static const String currency = 'XOF';

  /// Émetteur Keycloak (surchargeable via --dart-define=KEYCLOAK_ISSUER=...).
  static const String keycloakIssuer = String.fromEnvironment(
    'KEYCLOAK_ISSUER',
    defaultValue: 'http://10.0.2.2:8083/realms/magicgold',
  );

  static const String keycloakClientId = 'magic-gold-mobile';

  /// URI de redirection (schéma déclaré côté Android/iOS pour le flux PKCE).
  static const String redirectUri = 'magicgold://callback';
}
