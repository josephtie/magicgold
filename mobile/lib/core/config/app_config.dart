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
}
