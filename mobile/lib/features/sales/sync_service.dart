import 'dart:convert';

import '../../core/storage/app_database.dart';
import '../../core/sync/sync_queue.dart';
import '../../shared/models/order.dart';
import 'order_repository.dart';

/// Rejoue la file de synchronisation vers le backend (offline-first).
/// Chaque opération réussie est acquittée ; une opération en échec reste en attente.
class SyncService {
  SyncService(this._queue, this._orderRepository);

  final SyncQueue _queue;
  final OrderRepository _orderRepository;

  Future<int> flush() async {
    var replayed = 0;
    final pending = await _queue.pending();
    for (final op in pending) {
      try {
        await _replay(op);
        await _queue.markDone(op.id);
        replayed++;
      } catch (_) {
        // garde l'opération en attente pour un prochain flush
      }
    }
    return replayed;
  }

  Future<void> _replay(SyncOperation op) async {
    if (op.entityType == 'order' && op.operationType == 'CREATE') {
      final request =
          OrderRequest.fromJson(jsonDecode(op.payload) as Map<String, dynamic>);
      await _orderRepository.createOrder(request);
    } else {
      throw UnsupportedError(
          'Opération non rejouable : ${op.operationType} ${op.entityType}');
    }
  }
}
