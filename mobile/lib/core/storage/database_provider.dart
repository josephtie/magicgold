import 'package:drift_flutter/drift_flutter.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'app_database.dart';
import '../sync/sync_queue.dart';

/// Base locale Drift (SQLite) — ouverte une fois, fermée à la destruction du provider.
final databaseProvider = Provider<AppDatabase>((ref) {
  final db = AppDatabase(driftDatabase(name: 'magic_gold'));
  ref.onDispose(db.close);
  return db;
});

/// File de synchronisation adossée à la base locale.
final syncQueueProvider = Provider<SyncQueue>(
  (ref) => SyncQueue(ref.watch(databaseProvider)),
);
