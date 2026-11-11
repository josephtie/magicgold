import 'dart:convert';
import 'dart:ffi';

import 'package:dio/dio.dart';
import 'package:drift/native.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:magic_gold/core/storage/app_database.dart';
import 'package:magic_gold/core/sync/sync_queue.dart';
import 'package:magic_gold/features/sales/order_repository.dart';
import 'package:magic_gold/features/sales/sales_service.dart';
import 'package:magic_gold/features/sales/sync_service.dart';
import 'package:magic_gold/shared/models/order.dart';
import 'package:sqlite3/open.dart';

class _RecordingOrderRepository extends OrderRepository {
  _RecordingOrderRepository() : super(Dio());

  final List<OrderRequest> created = [];

  @override
  Future<Map<String, dynamic>> createOrder(OrderRequest request) async {
    created.add(request);
    return <String, dynamic>{};
  }
}

class _ThrowingOrderRepository extends OrderRepository {
  _ThrowingOrderRepository() : super(Dio());

  @override
  Future<Map<String, dynamic>> createOrder(OrderRequest request) async {
    throw Exception('réseau indisponible');
  }
}

void main() {
  open.overrideFor(OperatingSystem.linux, () => DynamicLibrary.open('libsqlite3.so.0'));

  test('SyncService rejoue et acquitte les ventes en attente', () async {
    final db = AppDatabase(NativeDatabase.memory());
    addTearDown(db.close);
    final queue = SyncQueue(db);
    final repository = _RecordingOrderRepository();
    final syncService = SyncService(queue, repository);

    await queue.enqueue(
      entityType: 'order',
      entityId: 'o1',
      operationType: 'CREATE',
      payload: jsonEncode({
        'items': [
          {'productId': 'p1', 'quantity': 2},
        ],
      }),
    );

    final replayed = await syncService.flush();

    expect(replayed, 1);
    expect(repository.created, hasLength(1));
    expect(repository.created.first.items.first.productId, 'p1');
    expect(await queue.pending(), isEmpty);
  });

  test('SalesService met en file quand la création échoue', () async {
    final db = AppDatabase(NativeDatabase.memory());
    addTearDown(db.close);
    final queue = SyncQueue(db);
    final salesService = SalesService(_ThrowingOrderRepository(), queue);

    final ok = await salesService.submit(
      OrderRequest(items: [const OrderItemRequest(productId: 'p1', quantity: 1)]),
    );

    expect(ok, isFalse);
    final pending = await queue.pending();
    expect(pending, hasLength(1));
    expect(pending.first.operationType, 'CREATE');
    expect(pending.first.entityType, 'order');
  });
}
