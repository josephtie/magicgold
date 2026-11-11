import 'package:flutter_appauth/flutter_appauth.dart';

import '../config/app_config.dart';
import 'oauth_flow.dart';

/// Implémentation Keycloak du flux OIDC Authorization Code + PKCE.
class KeycloakOAuthFlow implements OAuthFlow {
  KeycloakOAuthFlow([FlutterAppAuth? appAuth])
      : _appAuth = appAuth ?? FlutterAppAuth();

  final FlutterAppAuth _appAuth;

  @override
  Future<OAuthTokenResult> authorize() async {
    final response = await _appAuth.authorizeAndExchangeCode(
      AuthorizationTokenRequest(
        AppConfig.keycloakClientId,
        AppConfig.redirectUri,
        issuer: AppConfig.keycloakIssuer,
        scopes: const ['openid', 'profile', 'email'],
      ),
    );

    final accessToken = response.accessToken;
    if (accessToken == null) {
      throw StateError('Authentification annulée ou jeton manquant');
    }

    return OAuthTokenResult(
      accessToken: accessToken,
      refreshToken: response.refreshToken,
      expiresAt: response.accessTokenExpirationDateTime,
    );
  }
}
