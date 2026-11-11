import 'package:dio/dio.dart';

import '../config/app_config.dart';

/// Client HTTP (REST) vers le backend MAGIC GOLD.
/// Ajoute le jeton Bearer (Keycloak) à chaque requête via un intercepteur ;
/// la résolution du jeton est injectée pour rester testable.
class ApiClient {
  ApiClient({required Future<String?> Function() tokenProvider})
      : _dio = _build(tokenProvider);

  static Dio _build(Future<String?> Function() tokenProvider) {
    final dio = Dio(BaseOptions(baseUrl: AppConfig.apiBaseUrl));
    dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: (options, handler) async {
          final token = await tokenProvider();
          if (token != null) {
            options.headers['Authorization'] = 'Bearer $token';
          }
          options.headers['Accept'] = 'application/json';
          handler.next(options);
        },
      ),
    );
    return dio;
  }

  final Dio _dio;

  Dio get dio => _dio;
}
