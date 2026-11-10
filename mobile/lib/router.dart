import 'package:go_router/go_router.dart';

import 'features/auth/auth_screen.dart';
import 'features/dashboard/dashboard_screen.dart';
import 'features/products/product_list_screen.dart';
import 'features/sales/sales_screen.dart';

final GoRouter appRouter = GoRouter(
  initialLocation: '/',
  routes: [
    GoRoute(path: '/', builder: (context, state) => const DashboardScreen()),
    GoRoute(path: '/products', builder: (context, state) => const ProductListScreen()),
    GoRoute(path: '/sales', builder: (context, state) => const SalesScreen()),
    GoRoute(path: '/auth', builder: (context, state) => const AuthScreen()),
  ],
);
