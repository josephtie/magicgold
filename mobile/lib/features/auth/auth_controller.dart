import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/auth/auth_repository.dart';
import '../../core/auth/keycloak_oauth_flow.dart';
import '../../core/auth/oauth_flow.dart';
import '../../core/auth/token_store.dart';

sealed class AuthState {
  const AuthState();
}

class AuthUnknown extends AuthState {
  const AuthUnknown();
}

class AuthAuthenticated extends AuthState {
  const AuthAuthenticated(this.accessToken);

  final String accessToken;
}

class AuthUnauthenticated extends AuthState {
  const AuthUnauthenticated();
}

final oauthFlowProvider = Provider<OAuthFlow>((ref) => KeycloakOAuthFlow());
final tokenStoreProvider = Provider<TokenStore>((ref) => SecureTokenStore());
final authRepositoryProvider = Provider<AuthRepository>(
  (ref) => AuthRepository(ref.read(oauthFlowProvider), ref.read(tokenStoreProvider)),
);

final authControllerProvider =
    NotifierProvider<AuthController, AuthState>(AuthController.new);

class AuthController extends Notifier<AuthState> {
  @override
  AuthState build() => const AuthUnknown();

  Future<void> login() async {
    final accessToken = await ref.read(authRepositoryProvider).login();
    state = AuthAuthenticated(accessToken);
  }

  Future<void> logout() async {
    await ref.read(authRepositoryProvider).logout();
    state = const AuthUnauthenticated();
  }

  Future<void> restore() async {
    final accessToken = await ref.read(authRepositoryProvider).currentAccessToken();
    state = accessToken != null
        ? AuthAuthenticated(accessToken)
        : const AuthUnauthenticated();
  }
}
