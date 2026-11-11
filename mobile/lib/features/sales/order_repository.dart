import 'package:dio/dio.dart';

import '../../shared/models/order.dart';

/// Création de commandes côté backend (/api/v1/orders).
class OrderRepository {
  OrderRepository(this._dio);

  final Dio _dio;

  Future<Map<String, dynamic>> createOrder(OrderRequest request) async {
    final response = await _dio.post<Map<String, dynamic>>(
      '/api/v1/orders',
      data: request.toJson(),
    );
    return response.data ?? <String, dynamic>{};
  }
}
