import 'dart:ffi';

import 'package:drift/native.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:magic_gold/core/storage/app_database.dart';
import 'package:magic_gold/core/sync/sync_queue.dart';
import 'package:sqlite3/open.dart';

void main() {
  // Sur l'hôte Linux, la lib est exposée en libsqlite3.so.0 (pas de symlink .so).
  open.overrideFor(OperatingSystem.linux, () => DynamicLibrary.open('libsqlite3.so.0'));

  test('la file de synchro enregistre puis acquitte les opérations', () async {
    final db = AppDatabase(NativeDatabase.memory());
    addTearDown(db.close);
    final queue = SyncQueue(db);

    await queue.enqueue(
      entityType: 'order',
      entityId: 'o1',
      operationType: 'CREATE',
      payload: '{}',
    );

    final pending = await queue.pending();
    expect(pending, hasLength(1));
    expect(pending.first.entityType, 'order');
    expect(pending.first.status, 'PENDING');

    await queue.markDone(pending.first.id);

    expect(await queue.pending(), isEmpty);
  });
}
