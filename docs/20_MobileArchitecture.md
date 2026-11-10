# MAGIC GOLD — Mobile Architecture

**Document :** `20_MobileArchitecture.md`
**Version :** 1.0
**Statut :** Référence technique
**Éditeur :** Nectux Ingénieries
**Projet :** MAGIC GOLD
**Audience :** Mobile, Backend, QA, DevOps, Hermes

---

# 1. Objet

Ce document définit l'architecture de l'application mobile **MAGIC GOLD**, développée en **Flutter**.

Il constitue la référence pour :

- la structure du projet Flutter ;
- la gestion d'état ;
- la navigation ;
- l'injection de dépendances ;
- le stockage local offline-first (SQLite/Drift) ;
- l'intégration au protocole de synchronisation (`19_SynchronizationProtocol.md`) ;
- l'authentification mobile via Keycloak OIDC (flux PKCE + refresh tokens) ;
- les notifications push ;
- le scan de codes-barres ;
- le client API REST (interceptors, retry, file d'attente offline) ;
- la stratégie de test mobile.

Ce document respecte le vocabulaire officiel et les décisions tranchées du `00_Master.md`, qui prime en cas de contradiction.

---

# 2. Références

| Document | Rôle pour ce document |
|---|---|
| `00_Master.md` | Autorité suprême : vocabulaire, périmètre V1/V2/V3, états authoritative, monnaie XOF, multi-tenant |
| `03_TechnicalArchitecture.md` | Architecture cible, principes Mobile First / Offline First / API First |
| `06_SecurityArchitecture.md` | Keycloak, PKCE, refresh tokens, autorisation, sécurité des données |
| `07_UI_UX_Specification.md` | Écrans, navigation mobile, états d'interface, accessibilité |
| `08_Notifications.md` | Canal PUSH, gestion des appareils, rotation des tokens |
| `16_RedisSpecification.md` | Rappel : Redis n'est jamais la source de vérité côté serveur |
| `19_SynchronizationProtocol.md` | Protocole de synchronisation offline (autorité pour le moteur de sync mobile) |

> Le protocole détaillé de synchronisation (API sync, idempotence, résolution de conflits, ordre de traitement) est défini dans `19_SynchronizationProtocol.md`. Ce document n'en décrit que **le versant mobile**.

---

# 3. Principes architecturaux mobiles

## MOB-001 — Offline First

Le mobile ne considère jamais la connexion Internet comme permanente.

Les opérations compatibles (vente au comptoir, mouvement de caisse, ajustement de stock, création de Customer) doivent pouvoir être effectuées localement puis synchronisées.

```text
Utilisateur
    |
    v
Application Flutter
    |
    +---- ONLINE  ----------> API REST MAGIC GOLD
    |
    +---- OFFLINE ----------> Base locale (Drift/SQLite)
                                  |
                                  v
                           File de synchronisation
                                  |
                                  v
                           Sync Engine (doc 19)
```

## MOB-002 — Mobile First

L'application principale est conçue pour smartphone. Les opérations fréquentes doivent être accessibles rapidement :

```text
VENDRE
ENCAISSER
CONSULTER STOCK
VOIR CA
```

## MOB-003 — Backend comme source de vérité

Le mobile n'est jamais une autorité de vérité pour :

```text
prix
montant total
statut de paiement digital
stock définitif
permissions
```

Le prix final d'une Order est recalculé et snapshoté côté serveur (`00_Master.md` §5.3). La confirmation d'un paiement digital provient du provider, pas du frontend.

## MOB-004 — Sécurité par défaut

- tokens stockés dans un stockage sécurisé, jamais en clair ;
- aucun secret client confidentiel embarqué (client public Keycloak + PKCE) ;
- aucune donnée d'un Merchant accessible à un autre (filtrage `merchantId`) ;
- les opérations financières sensibles exigent une confirmation utilisateur (`07_UI_UX_Specification.md` §74).

## MOB-005 — Simplicité modulaire

Le mobile applique la même philosophie que le backend :

```text
SIMPLE AU DÉMARRAGE
    +
MODULAIRE
    +
SÉCURISÉ
    +
OFFLINE-FIRST
```

Pas de sur-ingénierie : un framework d'état unique, une navigation unique, un client HTTP unique.

---

# 4. Stack mobile

| Domaine | Choix | Justification |
|---|---|---|
| Framework | **Flutter** (stable) + **Dart** | Une base de code Android/iOS, accès caméra/scanner, offline |
| Gestion d'état + DI | **Riverpod** (`flutter_riverpod`) | Réactif, testable, intégré à Drift, pas de couplage BuildContext |
| Navigation | **go_router** | Routes déclaratives, deep links, redirections d'authentification |
| HTTP | **dio** | Interceptors, retry, timeouts, cancellation |
| Base locale | **Drift** (+ `sqlite3_flutter_libs`) | SQLite type-safe, réactif (streams), migrations versionnées |
| Stockage sécurisé | **flutter_secure_storage** | Refresh token, access token (Keychain / Keystore) |
| OIDC / PKCE | **flutter_appauth** (ou équivalent PKCE) | Authorization Code + PKCE avec Keycloak |
| Push | **firebase_messaging** (Android/FCM), **APNs** (iOS) | Canal PUSH défini dans `08_Notifications.md` |
| Scan codes-barres | **mobile_scanner** | EAN-13/UPC/Code128, retour caméra natif |
| Connectivité | **connectivity_plus** | Détection ONLINE/OFFLINE |
| Formatage | **intl** | Montants XOF, dates |
| Modèles | **freezed** + **json_serializable** | Sérialisation immuable, type-safe |

Les versions définitives sont figées dans le `pubspec.yaml` avec contraintes hautes (`>=X,<Y`).

---

# 5. Structure du projet Flutter

La structure étend celle définie dans `03_TechnicalArchitecture.md` §8.

```text
lib/
├── main.dart                       # Point d'entrée, bootstrap
├── app/
│   ├── app.dart                    # MaterialApp.router, thème, routes
│   ├── router.dart                 # go_router : routes + guards
│   ├── theme/                      # design tokens (07_UI_UX_Specification)
│   └── providers.dart              # providers racine (auth, sync, db)
│
├── core/
│   ├── config/                     # endpoints, feature flags, env
│   ├── network/                    # dio client, interceptors, retry, offline queue
│   ├── storage/                    # secure storage, key-value
│   ├── database/                   # Drift database + tables + migrations
│   ├── security/                   # token handling, cert pinning, obfuscation
│   ├── synchronization/            # sync engine (versant mobile, doc 19)
│   ├── connectivity/               # ONLINE/OFFLINE/SYNCING/SYNC_ERROR
│   ├── barcode/                    # scanner wrapper
│   ├── push/                       # FCM/APNs wrapper
│   ├── money/                      # formatage XOF (jamais double/float)
│   ├── error/                      # erreurs applicatives
│   └── utils/
│
├── features/
│   ├── authentication/             # login, PKCE, refresh, logout
│   ├── dashboard/                  # KPIs, actions rapides, alertes
│   ├── products/                   # catalogue, recherche, scan
│   ├── categories/                 # ProductCategory
│   ├── orders/                     # Order, OrderItem, panier, encaissement
│   ├── payments/                   # Payment, providers (initiation côté serveur)
│   ├── cash/                       # Register, CashSession, CashMovement
│   ├── customers/                  # Customer
│   ├── inventory/                  # StockMovement, ajustements
│   ├── notifications/              # centre de notifications in-app
│   └── reports/                    # CA, rapports (lecture)
│
└── shared/
    ├── widgets/                    # composants réutilisables (07 §108-110)
    └── models/                     # modèles partagés (freezed)
```

## 5.1 Périmètre V1 des features

Conformément au `00_Master.md` §3 :

| Feature | V1 | Statut |
|---|---|---|
| authentication, dashboard, products, categories, orders, payments, cash, customers, inventory, notifications, reports | ✅ | Développée |
| purchases (Supplier/Purchase), expenses | ❌ | V2 — non développée |
| wallet, loyalty, coupons | ❌ | V2/V3 — non développée |

Aucune entité V2/V3 ne doit être implémentée en V1.

## 5.2 Règles de structure

1. Un feature = un sous-domaine ; pas de logique métier croisée entre features.
2. `core/` ne dépend jamais d'un feature.
3. `shared/` contient uniquement du code réutilisable sans dépendance métier.
4. Chaque feature suit la même segmentation :

```text
features/<feature>/
├── presentation/       # widgets, écrans, contrôleurs UI
├── application/        # cas d'usage, orchestration
├── domain/             # entités, interfaces (ports)
└── data/               # repositories (API, base locale)
```

---

# 6. Architecture en couches

```text
┌─────────────────────────────────────────────┐
│  Presentation (widgets, écrans)             │
│  Riverpod Consumers / Controllers           │
└──────────────────┬──────────────────────────┘
                   │
┌──────────────────▼──────────────────────────┐
│  Application (cas d'usage)                  │
│  Coordinateurs de flux (ex: EncaisserOrder) │
└──────────────────┬──────────────────────────┘
                   │
┌──────────────────▼──────────────────────────┐
│  Domain (entités, ports)                    │
│  Order, Payment, Product, OrderRepository   │
└───────┬──────────────────────────┬──────────┘
        │                          │
┌───────▼─────────┐      ┌─────────▼─────────┐
│  Data — remote  │      │  Data — local     │
│  (dio → API)    │      │  (Drift → SQLite) │
└─────────────────┘      └───────────────────┘
```

- Les entités du domaine sont **immuables** (freezed) et indépendantes des DTO réseau et des tables Drift.
- Les `repositories` combinent remote et local : lecture locale d'abord (offline-first), écriture locale + mise en file de synchronisation.

---

# 7. Gestion d'état

## MOB-DEC-001 — Riverpod comme gestion d'état unique

**Décision :** Riverpod (`flutter_riverpod`) est l'unique mécanisme de gestion d'état et d'injection de dépendances.

**Raison :**

- réactif et testable sans dépendre de `BuildContext` ;
- `StreamProvider` s'intègre nativement aux requêtes Drift réactives ;
- l'état de synchronisation (`ONLINE/OFFLINE/SYNCING/SYNC_ERROR`) est exposé comme un provider global observé par toute l'UI ;
- pas de mélange de patterns (Bloc + Provider + setState) — une seule façon de faire.

## 7.1 Catégories de providers

```text
Providers d'infrastructure (singletons)
    databaseProvider
    dioProvider
    secureStorageProvider
    syncEngineProvider
    connectivityProvider

Providers de feature (état)
    authControllerProvider      (AsyncNotifier)
    orderControllerProvider     (AsyncNotifier)
    productListProvider         (StreamProvider<Drift>)
    syncStatusProvider          (StreamProvider<SyncStatus>)
```

## 7.2 Règles

1. La logique métier ne vit jamais dans un widget ; elle vit dans un `Notifier`/`AsyncNotifier`.
2. Les widgets lisent l'état via `ref.watch` ; les actions passent par `ref.read(...).method()`.
3. L'état financier (montant d'une Order, statut d'un Payment) est affiché depuis l'état local persisté, jamais recomputé à la volée côté UI.
4. L'optimistic UI est réservée aux opérations à faible risque (`07_UI_UX_Specification.md` §95). Jamais pour un paiement ou un transfert.

---

# 8. Injection de dépendances

Riverpod assure l'injection de dépendances : les dépendances sont déclarées comme providers, surchargées dans les tests.

```dart
final databaseProvider = Provider<AppDatabase>((ref) {
  final db = AppDatabase();
  ref.onDispose(db.close);
  return db;
});

final dioProvider = Provider<Dio>((ref) {
  return ApiClientFactory(
    baseUrl: ref.watch(envProvider).apiBaseUrl,
    secureStorage: ref.watch(secureStorageProvider),
  ).create();
});
```

**Règles :**

1. Aucune dépendance n'est instanciée dans un widget.
2. Les implémentations externes (HTTP, scanner, push, Keycloak) sont définies derrière des interfaces (`BarcodeScannerPort`, `PushPort`, `OidcPort`) pour être substituées en test.
3. Les secrets (URLs d'environnement, clés) proviennent de la configuration compilée, jamais en dur dans le code (`06_SecurityArchitecture.md` §61-62).

---

# 9. Navigation

## MOB-DEC-002 — go_router avec deep links

```dart
GoRouter(
  initialLocation: '/dashboard',
  redirect: (context, state) => authRedirect(state), // vers /login si non authentifié
  routes: [
    GoRoute(path: '/login', builder: ...),
    ShellRoute(                          // shell avec barre de navigation
      builder: (context, state, child) => HomeShell(child: child),
      routes: [
        GoRoute(path: '/dashboard', builder: ...),
        GoRoute(path: '/products', builder: ...),
        GoRoute(path: '/orders', builder: ...),
        GoRoute(path: '/cash', builder: ...),
        GoRoute(path: '/profile', builder: ...),
      ],
    ),
    GoRoute(path: '/orders/:orderId', builder: ...),
    GoRoute(path: '/products/:productId', builder: ...),
  ],
);
```

## 9.1 Navigation par rôle

La barre de navigation mobile reprend `07_UI_UX_Specification.md` §21 :

```text
┌───────────────────────────┐
│ Header                    │
├───────────────────────────┤
│ Content                   │
├───────────────────────────┤
│ Dashboard  Vendre  Caisse  Profil │
└───────────────────────────┘
```

Les entrées sont filtrées par permissions (`07_UI_UX_Specification.md` §76). Le masquage frontend n'est **jamais** une mesure de sécurité : le backend vérifie toujours les permissions.

## 9.2 Deep links

Les notifications push ouvrent une ressource via deep link (`08_Notifications.md` §22) :

```text
/orders/{orderId}
/products/{productId}
/orders/{orderId}/payment
```

Les destinations sont construites à partir de données sûres (pas d'URL arbitraire reçue du backend).

## 9.3 Redirection d'authentification

- Non authentifié → toute route privée redirige vers `/login`.
- Session expirée (401 via refresh échoué) → redirection `/login` avec message « Votre session a expiré » (`07_UI_UX_Specification.md` §105).

---

# 10. Client API REST

## 10.1 Choix

**Décision :** `dio` comme unique client HTTP. Tous les appels passent par un `ApiClient` centralisé.

## 10.2 Interceptors

Ordre d'exécution :

```text
Request
  → AuthInterceptor          (attache Bearer access token)
  → MerchantInterceptor      (attache contexte merchantId si requis)
  → IdempotencyInterceptor   (Idempotency-Key pour les opérations financières)
  → RetryInterceptor         (retry sur 401, 429, 5xx transitoires)
  → OfflineQueueInterceptor  (capture hors-ligne)
Response
  → ErrorInterceptor         (normalise les erreurs → DomainError)
```

### AuthInterceptor

- attache `Authorization: Bearer <access_token>` ;
- en cas de `401` et de refresh token valide, déclenche le refresh puis rejoue la requête (single-flight : un seul refresh concurrent).

### MerchantInterceptor

- attache l'identifiant du contexte marchand actif (`merchantId`) lorsque l'API l'exige ; le tenant est toujours dérivé du token/du contexte authentifié, jamais choisi librement par le client.

### IdempotencyInterceptor

- génère et attache `Idempotency-Key` (UUID) pour toute opération financière susceptible d'être répétée (`06_SecurityArchitecture.md` §27).

### RetryInterceptor

- `429` : respecte `Retry-After` avec backoff ;
- `5xx` transitoires : backoff exponentiel borné ;
- `401` : tente un refresh token unique ;
- les erreurs `4xx` définitives (400, 403, 404, 422) ne sont **pas** retentées.

## 10.3 Timeouts

```text
connectTimeout: 10 s
receiveTimeout: 30 s
```

Aucun bouton ne reste bloqué indéfiniment (`07_UI_UX_Specification.md` §97).

## 10.4 Gestion des erreurs

`ErrorInterceptor` convertit les réponses en erreurs applicatives typées :

```text
NetworkError        → pas de connectivité
AuthError           → 401 non résolu
ForbiddenError      → 403 (accès non autorisé)
NotFoundError       → 404
ValidationError     → 400/422
ServerError         → 5xx
```

Les messages affichés à l'utilisateur sont compréhensibles et ne révèlent pas de détail technique (`06_SecurityArchitecture.md` §78).

## 10.5 Base URL et versionnement

```text
https://<domaine>/api/v1/...
```

Toutes les API sont versionnées (`03_TechnicalArchitecture.md` §13).

---

# 11. Stockage local offline-first (Drift / SQLite)

## 11.1 Choix

**Décision :** **Drift** (ORM réactif au-dessus de SQLite) comme base locale unique, avec `sqlite3_flutter_libs`.

**Raison :**

- schéma type-safe et migrations versionnées ;
- requêtes réactives (streams) consommées directement par `StreamProvider` ;
- transactionnel (indispensable pour la cohérence d'une Order et de ses OrderItem).

## 11.2 Séparation des données locales

Conformément à `03_TechnicalArchitecture.md` §9 :

```text
MASTER DATA        → produits, catégories, référentiels (lecture, synchronisés pull)
TRANSACTION DATA   → Order, OrderItem, Payment, CashSession, StockMovement (créés localement)
SYNC QUEUE         → sync_operation (opérations en attente d'envoi)
LOCAL SETTINGS     → préférences, état de session, contexte marchand
```

## 11.3 Tables locales (indicatif)

```text
local_product          (cache MASTER DATA, snapshot id + version)
local_category
local_customer
local_order
local_order_item
local_payment
local_cash_session
local_cash_movement
local_stock_movement

sync_operation         (file de synchronisation)
sync_conflict          (conflits à résoudre, réf. doc 19)
```

## 11.4 Colonnes de traçabilité

Chaque opération locale porte les champs définis dans `03_TechnicalArchitecture.md` §10 :

```text
id
clientOperationId     (UUID généré côté mobile)
deviceId
merchantId            (tenantId)
userId
createdAt
updatedAt
syncStatus            (PENDING / SYNCING / SYNCED / FAILED)
```

## 11.5 Montants

Les montants sont stockés **en centimes entiers** (`BIGINT`) côté mobile, jamais en `double`/`float`, conformément au `00_Master.md` §5.5 (XOF). Le formatage visuel (`125 000 XOF`) est appliqué uniquement à l'affichage via `intl` (`07_UI_UX_Specification.md` §86).

## 11.6 Migrations

Les migrations Drift sont versionnées et testées. Toute migration doit préserver les données locales non synchronisées (file d'attente) — une migration ne vide jamais la sync queue sans stratégie documentée.

---

# 12. Intégration au protocole de synchronisation (doc 19)

Le protocole détaillé est défini dans `19_SynchronizationProtocol.md`. Le versant mobile expose :

## 12.1 Sync Engine (mobile)

```text
SyncEngine
├── PushWorker      → envoie les sync_operation PENDING (ordre FIFO)
├── PullWorker      → récupère les changements serveur (delta)
├── ConflictResolver→ applique les règles de doc 19
└── SyncStatus      → ONLINE / OFFLINE / SYNCING / SYNC_ERROR
```

## 12.2 File d'attente

Toute écriture métier locale est d'abord persistée en base, puis enregistrée comme `sync_operation` :

```text
Transaction locale
    ├── INSERT/UPDATE local_<entité>
    └── INSERT sync_operation (type, payload, syncStatus = PENDING)
```

Une écriture locale n'est jamais considérée comme synchronisée tant que `syncStatus != SYNCED`.

## 12.3 Idempotence côté client

Chaque opération porte un `clientOperationId` (UUID) unique. Le backend garantit l'idempotence (`Idempotency-Key` + `clientOperationId`). Le mobile rejoue sans jamais créer de doublon (`03_TechnicalArchitecture.md` §12).

## 12.4 États de synchronisation (UI)

Reprise de `03_TechnicalArchitecture.md` §42 :

```text
🟢 Synchronisé
🟠 Synchronisation en cours
🔴 3 opérations à synchroniser
```

## 12.5 Conflits

Les règles de résolution (stock, prix, client, vente, modification concurrente) sont définies dans `19_SynchronizationProtocol.md`. Règle mobile absolue :

> Une Order locale validée ne doit jamais être silencieusement écrasée.

## 12.6 Opérations hors-ligne autorisées vs interdites

| Opération | Hors-ligne | Règle |
|---|---|---|
| Créer une Order (vente au comptoir, espèce) | ✅ | Mise en file + sync ultérieure |
| Mouvement de caisse (CashMovement) | ✅ | Mise en file |
| Ajustement de stock (StockMovement) | ✅ | Mise en file |
| Créer un Customer | ✅ | Mise en file |
| Lecture du catalogue (produits, prix) | ✅ | Depuis MASTER DATA local |
| **Paiement digital** (Mobile Money, carte) | ❌ | Nécessite le réseau ; la confirmation vient du provider |
| Consultation CA / rapports serveur | ⚠️ | Lecture locale partielle ; données serveur en ligne |

Un paiement digital n'est **jamais** marqué `SUCCESS` hors-ligne (`07_UI_UX_Specification.md` §68). Une Order hors-ligne reste `PENDING_PAYMENT` jusqu'à synchronisation et confirmation serveur.

---

# 13. Authentification mobile — Keycloak OIDC (PKCE + refresh tokens)

## 13.1 Modèle

Conformément à `06_SecurityArchitecture.md` §6-15 :

- Identity Provider : **Keycloak** (realm dédié `magic-gold`) ;
- client mobile : **`magicgold-mobile`**, client **public** (pas de secret embarqué) ;
- flux : **Authorization Code + PKCE**.

```text
Application Flutter
    |  (1) code_challenge / PKCE
    v
Keycloak (authorize)
    |  (2) code d'autorisation
    v
Application Flutter
    |  (3) code_verifier + code
    v
Keycloak (token)
    |  (4) access_token + refresh_token (+ id_token)
    v
Application Flutter → stockage sécurisé
```

## 13.2 Flux PKCE

1. Génération d'un `code_verifier` aléatoire côté app ; calcul du `code_challenge` (S256).
2. Ouverture de Keycloak (`flutter_appauth`) dans un navigateur sécurisé (Chrome Custom Tabs / SFSafariViewController).
3. Récupération du code d'autorisation via la redirection `redirect_uri`.
4. Échange du code + `code_verifier` contre `access_token` + `refresh_token`.
5. Le `code_verifier` reste côté client et n'est jamais transmis à un tiers (`06_SecurityArchitecture.md` §11).

## 13.3 Stockage des tokens

| Élément | Stockage |
|---|---|
| `access_token` | `flutter_secure_storage` (Keychain / Keystore) |
| `refresh_token` | `flutter_secure_storage` — traité comme un secret |
| `id_token` | non persisté si non nécessaire |

Les tokens ne sont **jamais** : loggés, affichés, stockés dans une URL, ni transmis à une API tierce non autorisée (`06_SecurityArchitecture.md` §15, §50).

## 13.4 Refresh

- À l'expiration de l'access token (ou sur `401`), l'`AuthInterceptor` utilise le refresh token pour obtenir un nouvel access token.
- **Single-flight** : un seul refresh concurrent ; les autres requêtes attendent.
- Si le refresh échoue (token révoqué, expiré) : déconnexion et redirection vers `/login`.
- La rotation des refresh tokens est gérée par Keycloak ; le mobile persiste le nouveau refresh token à chaque renouvellement.

## 13.5 Session offline

L'utilisateur reste authentifié **localement** hors-ligne : les données locales et la file de synchronisation restent accessibles. Les appels réseau nécessitant une session valide sont mis en attente jusqu'au retour du réseau.

## 13.6 Logout

Le logout invalide la session locale (effacement sécurisé des tokens) et déclenche le logout Keycloak. Les données non synchronisées ne sont **pas** supprimées par un simple logout (protection contre la perte de données).

## 13.7 Autorisation

Les rôles/permissions proviennent des claims du token (`userId`, `merchantId`, `roles`, `permissions`). L'autorisation réelle est vérifiée côté backend (`03_TechnicalArchitecture.md` §15). Le mobile masque uniquement les entrées non autorisées (UX, pas sécurité).

---

# 14. Notifications push

## 14.1 Fournisseurs

```text
Android → Firebase Cloud Messaging (FCM)
iOS     → Apple Push Notification service (APNs)
```

Le canal PUSH est défini dans `08_Notifications.md` §9.4. Le backend abstrait l'envoi derrière un `PushProvider` (`08_Notifications.md` §46).

## 14.2 Enregistrement de l'appareil

Le mobile enregistre l'appareil auprès du backend (`08_Notifications.md` §73) :

```text
Device
├── id
├── userId
├── platform          (ANDROID / IOS)
├── pushToken
├── appVersion
├── lastSeenAt
├── active
└── createdAt
```

## 14.3 Cycle de vie du token

- Récupération du token FCM/APNs au démarrage et à chaque renouvellement (`onTokenRefresh`).
- Envoi du token au backend (`/api/v1/devices`) à chaque changement.
- Gestion de l'invalidation/rotation (`08_Notifications.md` §75).

## 14.4 Traitement des messages

- **Foreground** : affichage d'une notification in-app (toast/bannière) — le payload contient `title`, `body`, `data` (deep link).
- **Background / terminated** : notification système + tap → deep link vers la ressource (`08_Notifications.md` §22).
- Les données financières sensibles ne sont **pas** affichées sur l'écran verrouillé sans préférence explicite (`08_Notifications.md` §71-72).

## 14.5 Notification Center

Le centre de notifications in-app est fourni par la feature `notifications` (lecture, marquage lu, filtres, deep link) conformément à `08_Notifications.md` §20.

---

# 15. Scan de codes-barres

## 15.1 Choix

**Décision :** `mobile_scanner` (caméra native, Android + iOS), encapsulé derrière `BarcodeScannerPort` pour testabilité.

## 15.2 Formats

```text
EAN-13
EAN-8
UPC-A
UPC-E
Code 128
Code 39
QR (si utilisé ultérieurement)
```

## 15.3 Parcours

```text
Scanner ouvert
    → permission caméra
    → lecture du code
    → recherche produit (local MASTER DATA d'abord)
        ├── trouvé → ajout à l'Order
        └── introuvable → saisie manuelle / création (si permission)
```

## 15.4 États gérés

Conformément à `07_UI_UX_Specification.md` §91 :

```text
permission request
camera loading
scan success
scan failure
retry
```

## 15.5 Règles

1. La recherche produit est **locale d'abord** (offline-first), puis distante si nécessaire.
2. Le prix affiché après scan provient de la donnée métier locale (MASTER DATA) ; le prix définitif est recalculé côté serveur (`00_Master.md` §5.3).
3. La lecture d'un code inconnu ne bloque jamais la vente : saisie manuelle possible.
4. Aucun code scanné n'est stocké inutilement ni transmis à un tiers.

---

# 16. Sécurité mobile

Complète `06_SecurityArchitecture.md` pour le versant mobile :

```text
✅ HTTPS/TLS obligatoire (pas de trafic HTTP en production)
✅ Tokens en stockage sécurisé (Keychain / Keystore)
✅ Pas de secret client confidentiel (client public + PKCE)
✅ Certificate pinning sur les domaines MAGIC GOLD
✅ Obfuscation / minification des builds release (Android R8, Dart)
✅ Secrets hors code source (config compilée, jamais Git)
✅ Validation des deep links (pas d'URL arbitraire)
✅ Zéro donnée d'un autre Merchant accessible (filtrage merchantId)
✅ Journalisation sans données sensibles (tokens, OTP, secrets)
```

Le stockage local SQLite n'est pas considéré comme un coffre-fort : les données sensibles au repos doivent respecter la politique de sécurité applicable ; les tokens ne sont jamais stockés dans SQLite.

---

# 17. Stratégie de test mobile

Complète `15_TestStrategy.md` pour le mobile.

## 17.1 Niveaux de test

| Niveau | Outil | Cible |
|---|---|---|
| Unit tests | `flutter_test` | entités, cas d'usage, formatage XOF, résolveurs |
| Widget tests | `flutter_test` | écrans, états (loading/empty/error), guards |
| Tests Drift | `drift` + `sqflite_common_ffi` | DAO, transactions, migrations |
| Tests du client API | `dio` + `http_mock_adapter` | interceptors, retry, 401/refresh, offline queue |
| Tests d'intégration | `integration_test` | flux réel sur appareil/émulateur |
| Tests de sync | doubles du SyncEngine | push/pull, idempotence, conflits (réf. doc 19) |

## 17.2 Cas critiques obligatoires

### Authentification

```text
PKCE complet (code → tokens)
refresh token unique (single-flight)
401 → refresh → rejeu
refresh échoué → déconnexion
logout → tokens effacés
```

### Offline / synchronisation

```text
création Order hors-ligne → mise en file
retour réseau → envoi FIFO
rejeu avec même clientOperationId → pas de doublon
syncStatus transitions correctes (PENDING → SYNCING → SYNCED / FAILED)
conflit → résolution sans écrasement silencieux
```

### Client API

```text
intercepteur Bearer présent
retry sur 429/5xx (backoff)
pas de retry sur 4xx définitives
Idempotency-Key sur opérations financières
timeout → erreur compréhensible
```

### Paiement

```text
jamais SUCCESS hors-ligne
montant affiché depuis état persisté (jamais double/float)
confirmation depuis le provider (jamais le frontend)
```

### Scan

```text
permission refusée → message
code trouvé / introuvable
recherche locale puis distante
```

### Push

```text
enregistrement device
rotation du token
tap → deep link → bonne ressource
```

### UI

```text
états loading / empty / error / offline
confirmation avant action financière (07 §74)
double-tap protégé (07 §98)
```

## 17.3 Double-click et idempotence

L'UI désactive les boutons pendant les actions critiques, mais la vraie protection reste l'idempotence backend (`07_UI_UX_Specification.md` §98).

## 17.4 CI

Le pipeline CI exécute : `flutter analyze`, `flutter test`, tests Drift, et (sur environnement dédié) `integration_test`. Les migrations Drift et le formatage XOF sont testés en non-régression.

---

# 18. Règles Hermes

Avant d'implémenter une fonctionnalité mobile, Hermes doit vérifier :

```text
1.  La fonctionnalité est-elle V1 ? (00_Master §3)
2.  Le vocabulaire officiel est-il respecté ? (Merchant, Order, Payment, ...)
3.  Fonctionne-t-elle hors-ligne ? Si non, quel est le comportement dégradé ?
4.  Les montants sont-ils en centimes entiers (jamais double/float) ?
5.  La confirmation financière vient-elle du serveur/provider, pas du frontend ?
6.  Le sync respecte-t-il le protocole 19_SynchronizationProtocol.md ?
7.  Les tokens sont-ils stockés en secure storage, jamais loggés ?
8.  Les permissions sont-elles vérifiées côté backend (le masquage UI ne suffit pas) ?
9.  L'Idempotency-Key est-il présent sur les opérations financières ?
10. Un test couvre-t-il le cas (offline, retry, conflit, paiement) ?
```

---

# 19. Documents liés

```text
00_Master.md                       — autorité suprême
03_TechnicalArchitecture.md        — architecture cible
04_DataModel.md                    — modèle de données (référence des tables locales)
05_API_Specification.md            — endpoints REST consommés
06_SecurityArchitecture.md         — Keycloak, PKCE, sécurité
07_UI_UX_Specification.md          — écrans et états
08_Notifications.md                — canal PUSH et devices
15_TestStrategy.md                 — stratégie de test globale
16_RedisSpecification.md           — rappel : Redis jamais source de vérité
19_SynchronizationProtocol.md      — protocole de synchronisation (versant serveur + règles)
```

---

# 20. Conclusion

L'application mobile MAGIC GOLD est une application **Flutter offline-first** :

```text
MOBILE FIRST
    +
OFFLINE FIRST
    +
RIVERPOD (état + DI)
    +
DRIFT (base locale)
    +
PKCE + REFRESH (Keycloak)
    +
SYNC ENGINE (doc 19)
    +
SÉCURITÉ PAR DÉFAUT
    =
APPLICATION MOBILE MAGIC GOLD
```

Le principe directeur :

> **Le mobile doit permettre de vendre et d'encaisser même sans réseau, sans jamais devenir la source de vérité d'un paiement ou d'un montant.**

**Nectux Ingénieries — MAGIC GOLD**
