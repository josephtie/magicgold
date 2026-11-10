import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'router.dart';

void main() {
  runApp(const ProviderScope(child: MagicGoldApp()));
}

class MagicGoldApp extends StatelessWidget {
  const MagicGoldApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp.router(
      title: 'MAGIC GOLD',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorSchemeSeed: const Color(0xFFC9A227),
        useMaterial3: true,
      ),
      routerConfig: appRouter,
    );
  }
}
