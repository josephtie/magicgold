/// Modèles de rapports (alignés sur /api/v1/reports/*).
class SalesReport {
  const SalesReport({
    required this.orderCount,
    required this.revenue,
    required this.ordersByStatus,
  });

  final int orderCount;
  final double revenue;
  final Map<String, int> ordersByStatus;

  factory SalesReport.fromJson(Map<String, dynamic> json) => SalesReport(
        orderCount: (json['orderCount'] as num?)?.toInt() ?? 0,
        revenue: (json['revenue'] as num?)?.toDouble() ?? 0,
        ordersByStatus: (json['ordersByStatus'] as Map<String, dynamic>? ?? const {})
            .map((key, value) => MapEntry(key, (value as num).toInt())),
      );
}

class ReconciliationReport {
  const ReconciliationReport({
    required this.salesTotal,
    required this.cashTotal,
    required this.digitalTotal,
    required this.outstanding,
  });

  final double salesTotal;
  final double cashTotal;
  final double digitalTotal;
  final double outstanding;

  factory ReconciliationReport.fromJson(Map<String, dynamic> json) =>
      ReconciliationReport(
        salesTotal: (json['salesTotal'] as num?)?.toDouble() ?? 0,
        cashTotal: (json['cashTotal'] as num?)?.toDouble() ?? 0,
        digitalTotal: (json['digitalTotal'] as num?)?.toDouble() ?? 0,
        outstanding: (json['outstanding'] as num?)?.toDouble() ?? 0,
      );
}
