import 'package:flutter_test/flutter_test.dart';
import 'package:magic_gold/core/auth/auth_repository.dart';
import 'package:magic_gold/core/auth/oauth_flow.dart';
import 'package:magic_gold/core/auth/token_store.dart';

class _FakeOAuthFlow implements OAuthFlow {
  @override
  Future<OAuthTokenResult> authorize() async => const OAuthTokenResult(
        accessToken: 'access-token',
        refreshToken: 'refresh-token',
        expiresAt: null,
      );
}

class _InMemoryTokenStore implements TokenStore {
  OAuthTokenResult? token;

  @override
  Future<void> save(OAuthTokenResult token) async => this.token = token;

  @override
  Future<OAuthTokenResult?> read() async => token;

  @override
  Future<void> clear() async => token = null;
}

void main() {
  test('login stocke le jeton et le restitue', () async {
    final store = _InMemoryTokenStore();
    final repository = AuthRepository(_FakeOAuthFlow(), store);

    final accessToken = await repository.login();

    expect(accessToken, 'access-token');
    expect(await repository.currentAccessToken(), 'access-token');
  });

  test('logout efface le jeton', () async {
    final store = _InMemoryTokenStore();
    final repository = AuthRepository(_FakeOAuthFlow(), store);
    await repository.login();

    await repository.logout();

    expect(await repository.currentAccessToken(), isNull);
  });

  test('un jeton expiré est considéré invalide', () async {
    final store = _InMemoryTokenStore()
      ..token = OAuthTokenResult(
        accessToken: 'stale',
        refreshToken: null,
        expiresAt: DateTime.now().toUtc().subtract(const Duration(minutes: 1)),
      );
    final repository = AuthRepository(_FakeOAuthFlow(), store);

    expect(await repository.currentAccessToken(), isNull);
  });
}
