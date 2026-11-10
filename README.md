# MAGIC GOLD

**SaaS mobile-first de gestion commerciale pour les commerces de proximité** (Côte d'Ivoire, puis Afrique).
*« Simple pour commencer. Puissant pour grandir. »*

Éditeur : **Nectux Ingénieries**

---

## Documentation de référence

Les 21 documents de spécification sont dans [`docs/`](docs/). Le **`docs/00_Master.md`** est
l'autorité suprême (vocabulaire, périmètre V1/V2/V3, états authoritative, décisions techniques).
Toute implémentation doit y rester conforme.

## Stack (décisions tranchées — 00_Master.md §5)

| Brique | Choix |
|---|---|
| Backend | Java 17 · Spring Boot 3.5.x · modular monolith |
| Base de données | PostgreSQL 15 (source de vérité) · Flyway |
| Cache / idempotence | Redis 7 |
| IAM | Keycloak (OAuth 2.0 / OIDC) |
| Mobile | Flutter (offline-first, SQLite/Drift) |
| Monnaie | XOF (DECIMAL(19,4), jamais FLOAT) |
| Multi-tenant | `Merchant = Tenant`, isolation par `merchantId` |

## Structure du dépôt

```
magicgold/
├── docs/                 # 21 documents de spécification (00 → 20)
├── backend/              # Spring Boot (modular monolith)
├── mobile/               # Flutter (à venir)
├── keycloak/             # realm initial (realm-magicgold.json)
└── docker-compose.yml    # postgres + redis + keycloak + backend
```

## Backend — organisation

```
com.nectux.magicgold
├── shared/        # BaseEntity, TenantEntity, Money (XOF), enums, ReferenceGenerator
├── security/      # Keycloak resource server + isolation multi-tenant (TenantContext)
├── common/        # gestion d'erreurs (ApiError, GlobalExceptionHandler)
├── identity/      # User, Role, UserRole
├── merchant/      # Merchant, MerchantOutlet, MerchantUser
├── catalog/       # Product, ProductCategory
├── customer/      # Customer
├── order/         # Order, OrderItem, OrderStatusHistory + machine à états
├── payment/       # Payment, PaymentProvider, WebhookEvent
├── cash/          # Register, CashSession, CashMovement
├── stock/         # StockMovement
├── audit/         # AuditLog
└── config/        # Configuration
```

## Construire et tester

Le build se fait dans un conteneur Maven (aucun JDK requis en local) :

```bash
docker run --rm -v "$PWD/backend":/app -w /app \
  -v mg_maven_repo:/root/.m2 \
  maven:3.9-eclipse-temurin-17 mvn -B test
```

## Lancer l'ensemble (dev)

```bash
docker compose up -d postgres redis keycloak backend
# puis importer le realm : keycloak/realm-magicgold.json
# API : http://localhost:8080  ·  Swagger : http://localhost:8080/swagger-ui.html
# Keycloak : http://localhost:8083  (admin/admin par défaut, à changer)
```

## État d'avancement

- ✅ Phase 1 — documents de spécification 00 → 20 (complet)
- ✅ Phase 2 — scaffold backend : 20 entités V1, migration Flyway (validée sur PostgreSQL 15 réel),
  sécurité Keycloak + isolation multi-tenant, API de référence (Merchant, Product, Order)
- ✅ Phase 3 — paiements : port `PaymentProvider` + adaptateur mock + webhooks idempotents
- ✅ Phase 4 — caisse : ouverture/clôture de session, écart attendu vs compté
- ✅ Reporting — CA, encaissements par méthode, réconciliation
- ✅ Mobile Flutter — squelette (Riverpod + go_router + dio + secure storage)
- ⏳ Paiements réels (CinetPay/FeexPay) — en attente des clés sandbox
- ⏳ Mobile : écrans complets, auth Keycloak PKCE, offline-first (Drift + file de synchro)

**Vérification** : `mvn test` → 10 tests verts · migration Flyway OK sur PostgreSQL ·
`flutter analyze` → clean · `flutter test` → vert.

## Notes de conception

- **États authoritative** : les enums (`OrderStatus`, `PaymentStatus`, `MerchantStatus`) suivent
  le `00_Master.md` §4 (et non les listes divergentes du `04_DataModel.md`).
- **`Order` n'a pas de `payment_status` séparé** : le Master unifie le paiement dans les états
  d'Order (§4.2), évitant deux sources de vérité.
- **Montants** : calcul/arrondi centralisés dans `shared/Money` (HALF_UP, XOF sans décimale).
- **`ReferenceGenerator`** : compteur en mémoire (suffisant en MVP mono-instance) — à remplacer
  par une séquence PostgreSQL pour un déploiement multi-instance.
