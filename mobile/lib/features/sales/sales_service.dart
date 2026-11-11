import 'dart:convert';

import '../../core/sync/sync_queue.dart';
import '../../shared/models/order.dart';
import 'order_repository.dart';

/// Soumet une vente : en ligne si possible, sinon mise en file (offline-first).
class SalesService {
  SalesService(this._orderRepository, this._syncQueue);

  final OrderRepository _orderRepository;
  final SyncQueue _syncQueue;

  /// Retourne true si la vente a été créée en ligne, false si mise en file.
  Future<bool> submit(OrderRequest request) async {
    try {
      await _orderRepository.createOrder(request);
      return true;
    } on Exception {
      await _syncQueue.enqueue(
        entityType: 'order',
        entityId: DateTime.now().microsecondsSinceEpoch.toString(),
        operationType: 'CREATE',
        payload: jsonEncode(request.toJson()),
      );
      return false;
    }
  }
}
