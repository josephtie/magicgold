import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../features/auth/auth_controller.dart';
import 'api_client.dart';

/// Client HTTP avec jeton Keycloak injecté à chaque requête.
final apiClientProvider = Provider<ApiClient>((ref) {
  return ApiClient(
    tokenProvider: () => ref.read(authRepositoryProvider).currentAccessToken(),
  );
});
