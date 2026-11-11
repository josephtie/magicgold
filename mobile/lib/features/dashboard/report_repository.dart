import 'package:dio/dio.dart';

import 'report_models.dart';

/// Accès aux rapports (/api/v1/reports/*).
class ReportRepository {
  ReportRepository(this._dio);

  final Dio _dio;

  Future<SalesReport> sales(DateTime from, DateTime to) async {
    final response = await _dio.get<Map<String, dynamic>>(
      '/api/v1/reports/sales',
      queryParameters: {
        'from': from.toIso8601String(),
        'to': to.toIso8601String(),
      },
    );
    return SalesReport.fromJson(response.data ?? const <String, dynamic>{});
  }

  Future<ReconciliationReport> reconciliation(DateTime from, DateTime to) async {
    final response = await _dio.get<Map<String, dynamic>>(
      '/api/v1/reports/reconciliation',
      queryParameters: {
        'from': from.toIso8601String(),
        'to': to.toIso8601String(),
      },
    );
    return ReconciliationReport.fromJson(response.data ?? const <String, dynamic>{});
  }
}
