import 'package:flutter_secure_storage/flutter_secure_storage.dart';

import 'oauth_flow.dart';

/// Stockage persistant des jetons OIDC (accès + refresh + expiration).
abstract class TokenStore {
  Future<void> save(OAuthTokenResult token);

  Future<OAuthTokenResult?> read();

  Future<void> clear();
}

class SecureTokenStore implements TokenStore {
  SecureTokenStore([FlutterSecureStorage? storage])
      : _storage = storage ?? const FlutterSecureStorage();

  final FlutterSecureStorage _storage;

  static const String _accessKey = 'access_token';
  static const String _refreshKey = 'refresh_token';
  static const String _expiresKey = 'access_token_expires_at';

  @override
  Future<void> save(OAuthTokenResult token) async {
    await _storage.write(key: _accessKey, value: token.accessToken);
    final refresh = token.refreshToken;
    if (refresh != null) {
      await _storage.write(key: _refreshKey, value: refresh);
    }
    final expiresAt = token.expiresAt;
    if (expiresAt != null) {
      await _storage.write(key: _expiresKey, value: expiresAt.toIso8601String());
    }
  }

  @override
  Future<OAuthTokenResult?> read() async {
    final access = await _storage.read(key: _accessKey);
    if (access == null) {
      return null;
    }
    final refresh = await _storage.read(key: _refreshKey);
    final expiresRaw = await _storage.read(key: _expiresKey);
    return OAuthTokenResult(
      accessToken: access,
      refreshToken: refresh,
      expiresAt: expiresRaw != null ? DateTime.tryParse(expiresRaw) : null,
    );
  }

  @override
  Future<void> clear() async {
    await _storage.delete(key: _accessKey);
    await _storage.delete(key: _refreshKey);
    await _storage.delete(key: _expiresKey);
  }
}
