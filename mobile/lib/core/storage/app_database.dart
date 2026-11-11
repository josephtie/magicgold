import 'package:drift/drift.dart';

part 'app_database.g.dart';

/// File de synchronisation (offline-first, cf. docs/19_SynchronizationProtocol.md).
/// Chaque opération locale (CREATE/UPDATE/DELETE) est enregistrée ici, puis
/// rejouée vers le backend quand la connectivité revient.
class SyncOperations extends Table {
  IntColumn get id => integer().autoIncrement()();

  TextColumn get entityType => text()();

  TextColumn get entityId => text()();

  TextColumn get operationType => text()();

  TextColumn get payload => text()();

  TextColumn get status => text().withDefault(const Constant('PENDING'))();

  DateTimeColumn get createdAt => dateTime().withDefault(currentDateAndTime)();
}

/// Cache local des produits (MASTER DATA répliquée hors connexion).
class LocalProducts extends Table {
  TextColumn get id => text()();

  TextColumn get name => text()();

  RealColumn get unitPrice => real()();

  TextColumn get currency => text()();

  DateTimeColumn get updatedAt => dateTime()();

  @override
  Set<Column> get primaryKey => {id};
}

@DriftDatabase(tables: [SyncOperations, LocalProducts])
class AppDatabase extends _$AppDatabase {
  AppDatabase(super.e);

  @override
  int get schemaVersion => 1;
}
