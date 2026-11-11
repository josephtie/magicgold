import 'oauth_flow.dart';
import 'token_store.dart';

/// Coordonne le flux OIDC et le stockage des jetons.
class AuthRepository {
  AuthRepository(this._flow, this._store);

  final OAuthFlow _flow;
  final TokenStore _store;

  Future<String> login() async {
    final token = await _flow.authorize();
    await _store.save(token);
    return token.accessToken;
  }

  Future<String?> currentAccessToken() async {
    final token = await _store.read();
    if (token == null || token.isExpired) {
      return null; // à raffiner : refresh token flow
    }
    return token.accessToken;
  }

  Future<void> logout() => _store.clear();
}
