import 'package:dio/dio.dart';

import '../config/app_config.dart';

/// Client HTTP (REST) vers le backend MAGIC GOLD.
/// À étendre : intercepteur d'authentification (Bearer), header merchant,
/// idempotency-key, retry et file hors-ligne (cf. docs/20_MobileArchitecture.md).
class ApiClient {
  ApiClient() : _dio = Dio(BaseOptions(baseUrl: AppConfig.apiBaseUrl));

  final Dio _dio;

  Dio get dio => _dio;
}
