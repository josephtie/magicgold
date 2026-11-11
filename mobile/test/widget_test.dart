import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:magic_gold/features/dashboard/dashboard_controller.dart';
import 'package:magic_gold/features/dashboard/dashboard_screen.dart';
import 'package:magic_gold/features/dashboard/report_models.dart';
import 'package:magic_gold/features/dashboard/report_repository.dart';

class _FakeReportRepository extends ReportRepository {
  _FakeReportRepository() : super(Dio());

  @override
  Future<SalesReport> sales(DateTime from, DateTime to) async =>
      const SalesReport(orderCount: 3, revenue: 625, ordersByStatus: {});

  @override
  Future<ReconciliationReport> reconciliation(DateTime from, DateTime to) async =>
      const ReconciliationReport(
        salesTotal: 625,
        cashTotal: 180,
        digitalTotal: 410,
        outstanding: 35,
      );
}

void main() {
  testWidgets('Dashboard affiche les KPIs et les actions', (tester) async {
    await tester.pumpWidget(
      ProviderScope(
        overrides: [
          reportRepositoryProvider.overrideWithValue(_FakeReportRepository()),
        ],
        child: const MaterialApp(home: DashboardScreen()),
      ),
    );
    await tester.pump();
    await tester.pump();

    expect(find.text('MAGIC GOLD'), findsOneWidget);
    expect(find.text('625 XOF'), findsOneWidget); // CA
    expect(find.text('590 XOF'), findsOneWidget); // 180 + 410 encaissements
    expect(find.text('35 XOF'), findsOneWidget); // impayés
    expect(find.text('Vendre'), findsOneWidget);
    expect(find.text('Produits'), findsOneWidget);
  });
}
