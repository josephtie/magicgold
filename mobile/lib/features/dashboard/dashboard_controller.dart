import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/api/api_client_provider.dart';
import 'report_models.dart';
import 'report_repository.dart';

class DashboardData {
  const DashboardData({required this.sales, required this.reconciliation});

  final SalesReport sales;
  final ReconciliationReport reconciliation;
}

final reportRepositoryProvider = Provider<ReportRepository>(
  (ref) => ReportRepository(ref.watch(apiClientProvider).dio),
);

final dashboardControllerProvider =
    AsyncNotifierProvider<DashboardController, DashboardData>(DashboardController.new);

/// Charge les KPIs du jour (CA, encaissements, impayés, volume).
class DashboardController extends AsyncNotifier<DashboardData> {
  @override
  Future<DashboardData> build() async {
    final repository = ref.read(reportRepositoryProvider);
    final now = DateTime.now().toUtc();
    final from = DateTime.utc(now.year, now.month, now.day);

    final sales = await repository.sales(from, now);
    final reconciliation = await repository.reconciliation(from, now);

    return DashboardData(sales: sales, reconciliation: reconciliation);
  }
}
