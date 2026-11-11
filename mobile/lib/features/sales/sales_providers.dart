import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/api/api_client_provider.dart';
import '../../core/storage/database_provider.dart';
import 'order_repository.dart';
import 'sales_service.dart';
import 'sync_service.dart';

final orderRepositoryProvider = Provider<OrderRepository>(
  (ref) => OrderRepository(ref.watch(apiClientProvider).dio),
);

final syncServiceProvider = Provider<SyncService>(
  (ref) => SyncService(
    ref.watch(syncQueueProvider),
    ref.watch(orderRepositoryProvider),
  ),
);

final salesServiceProvider = Provider<SalesService>(
  (ref) => SalesService(
    ref.watch(orderRepositoryProvider),
    ref.watch(syncQueueProvider),
  ),
);
