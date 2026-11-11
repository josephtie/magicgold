import 'package:drift/drift.dart';

import '../storage/app_database.dart';

/// File de synchronisation : enregistre les opérations locales en attente
/// d'envoi vers le backend, et les marque comme traitées une fois rejouées.
class SyncQueue {
  SyncQueue(this._db);

  final AppDatabase _db;

  Future<int> enqueue({
    required String entityType,
    required String entityId,
    required String operationType,
    required String payload,
  }) {
    return _db.into(_db.syncOperations).insert(
          SyncOperationsCompanion.insert(
            entityType: entityType,
            entityId: entityId,
            operationType: operationType,
            payload: payload,
          ),
        );
  }

  Future<List<SyncOperation>> pending() {
    final query = _db.select(_db.syncOperations)
      ..where((t) => t.status.equals('PENDING'))
      ..orderBy([(t) => OrderingTerm.asc(t.createdAt)]);
    return query.get();
  }

  Future<void> markDone(int id) async {
    await (_db.update(_db.syncOperations)..where((t) => t.id.equals(id)))
        .write(const SyncOperationsCompanion(status: Value('DONE')));
  }
}
