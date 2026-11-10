# MAGIC GOLD — Protocole de Synchronisation (Offline Sync Protocol)

**Document :** `19_SynchronizationProtocol.md`
**Version :** 1.0
**Statut :** Référence de conception
**Éditeur :** Nectux Ingénieries
**Projet :** MAGIC GOLD
**Audience :** Architecte, développeurs Backend/Mobile, QA, DevOps, Hermes

---

> **Ce document est subordonné à `00_Master.md`.** En cas de contradiction, le Master fait foi.
> Il met en œuvre les principes ARCH-002 (Offline First), ARCH-008 (Offline First) et le Sprint 7 (Offline) de `03_TechnicalArchitecture.md`, et s'appuie sur `04_DataModel.md`, `05_API_Specification.md` et `16_RedisSpecification.md`.

---

# 1. Objet

Ce document définit le protocole de synchronisation de MAGIC GOLD : le mécanisme qui permet à une application mobile **offline-first** de fonctionner avec une connectivité variable (marchés africains, réseaux mobiles instables) tout en garantissant que **PostgreSQL reste la source de vérité unique** et qu'**aucune règle financière ne puisse être contournée hors connexion**.

Il couvre :

1. le modèle offline-first et les opérations autorisées hors connexion ;
2. la base locale mobile (SQLite via **Drift** pour Flutter) ;
3. la file de synchronisation (queue) ;
4. les clés d'idempotence (`Idempotency-Key`) ;
5. les numéros de séquence et le versioning ;
6. le pattern **outbox** côté client et côté serveur ;
7. la gestion de la connectivité (détection, retry, backoff) ;
8. les règles spécifiques au **paiement hors ligne** (confirmation externe obligatoire) ;
9. le partitionnement des données par Merchant ;
10. les flux montant (client → serveur) et descendant (serveur → client) ;
11. l'intégrité et la reprise après panne.

---

# 2. Principes fondamentaux

## 2.1 Source de vérité

```text
POSTGRESQL  = vérité métier (source of truth)
REDIS       = accélération + état temporaire (jamais source de vérité)
SQLITE (Drift) = cache local mobile (jamais source de vérité)
```

> Une donnée n'est **définitive** que lorsqu'elle est acceptée et persistée par le backend dans PostgreSQL.
> Le frontend mobile ne constitue jamais une source de vérité métier (`04_DataModel.md` §47).

## 2.2 Offline-first

La connexion Internet n'est pas considérée comme permanente. Les opérations compatibles s'exécutent localement puis sont **répliquées** vers le serveur quand la connexion revient.

```text
Utilisateur
    ↓
Application Mobile
    ├── Internet disponible ──► API ──► PostgreSQL
    └── Internet indisponible ─► SQLite (Drift) ─► Sync Queue ─► (repli) API
```

## 2.3 Règles invariantes

| # | Invariant |
|---|---|
| SYNC-001 | Le serveur est l'unique arbitre de toute écriture métier. |
| SYNC-002 | Une vente (Order) validée localement ne doit jamais être silencieusement écrasée. |
| SYNC-003 | Aucune opération financière n'est considérée comme **réussie** hors connexion. |
| SYNC-004 | Le montant d'un paiement est toujours calculé côté serveur à partir de l'Order. |
| SYNC-005 | Toute écriture synchronisée est idempotente (rejouable sans double effet). |
| SYNC-006 | L'isolation par Merchant est appliquée côté backend, jamais côté frontend uniquement. |
| SYNC-007 | Les données financières sont immuables : on ajoute des mouvements/ajustements, on ne met jamais à jour un montant en place. |
| SYNC-008 | Redis peut accélérer la synchronisation mais n'en est jamais le garant. |

---

# 3. Modèle offline-first

## 3.1 Classification des données locales

Le stockage local (SQLite/Drift) est séparé en quatre compartiments :

```text
MASTER DATA       — données de référence (produits, catégories, clients…)
TRANSACTION DATA  — données d'activité (Order, OrderItem, Payment, CashSession, StockMovement…)
SYNC QUEUE        — opérations en attente d'envoi (outbox client)
LOCAL SETTINGS    — préférences et état de synchronisation (curseurs, jetons)
```

## 3.2 Opérations autorisées hors connexion

| Domaine | Opération | Hors connexion | Remarque |
|---|---|---|---|
| Catalogue | Consulter un Product | ✅ OUI | Lecture du cache local (MASTER DATA) |
| Catalogue | Créer / modifier un Product | ✅ OUI | Mise en file, réconciliation serveur |
| Vente | Créer une Order (vente au comptoir) | ✅ OUI | `customerId` null accepté (vente rapide) |
| Vente | Ajouter un OrderItem (snapshot prix/nom) | ✅ OUI | Snapshot calculé localement, revalidé serveur |
| Paiement | Paiement **CASH** (espèces) | ✅ OUI | Espèces reçues physiquement → enregistré, puis synchronisé |
| Paiement | Paiement **digital** (Mobile Money, CARD, QR, BANK) | ❌ NON | Nécessite le provider → **jamais confirmé hors connexion** (cf. §11) |
| Stock | Enregistrer un StockMovement (ajustement) | ✅ OUI | Rejoué serveur, stock recalculé côté serveur |
| Caisse | Ouvrir / fermer une CashSession | ✅ OUI | Horodaté localement, validé serveur |
| Caisse | Enregistrer un CashMovement | ✅ OUI | Rejoué dans l'ordre |
| Client | Créer / modifier un Customer | ✅ OUI | Fusion/résolution de conflits (cf. §9) |
| Rapports | Consulter le dashboard | ⚠️ PARTIEL | Sur données locales seulement ; indiquer la fraîcheur |
| Connexion | Se connecter (auth) | ⚠️ LIMITÉ | Session/token locaux, renouvellement requiert le réseau |

> Toute opération non listée ci-dessus est considérée **en ligne uniquement**.

## 3.3 Règle d'or de l'offline

```text
OPÉRATION LOCALE
   ↓
Validation locale (règles métier embarquées)
   ↓
Écriture SQLite transactionnelle (données + outbox)
   ↓
File de synchronisation
   ↓
(connexion) Envoi → Validation serveur → Idempotence → PostgreSQL
```

La validation locale est une **première passe** de confort ; la validation serveur reste **l'autorité**. Une opération refusée par le serveur devient un **conflit** (jamais une perte silencieuse).

---

# 4. Base locale mobile — SQLite via Drift

## 4.1 Choix technique

```text
Flutter + Dart
      ↓
Drift (OR/M type-safe sur SQLite)
```

Drift fournit des requêtes typées, des streams réactifs et des transactions atomiques — requis pour l'écriture conjointe « données + outbox » dans la même transaction SQLite.

## 4.2 Tables locales

Les tables locales reflètent les entités V1 du Data Model, préfixées `local_` :

```text
local_product
local_product_category
local_customer
local_order
local_order_item
local_payment
local_cash_session
local_cash_movement
local_stock_movement
local_merchant_outlet
local_register
```

Chaque table locale porte les colonnes de synchronisation suivantes (héritées de `03_TechnicalArchitecture.md` §10) :

```text
id                 UUID            — identifiant technique (généré côté client)
clientOperationId  VARCHAR(64)     — identifiant unique de l'opération d'origine
deviceId           VARCHAR(64)     — identifiant stable de l'appareil
merchantId         UUID            — tenant (Merchant)
userId             UUID            — MerchantUser auteur
registerId         UUID            — Register d'origine (le cas échéant)
createdAt          TIMESTAMP (UTC) — création locale
updatedAt          TIMESTAMP (UTC) — dernière modification locale
baseVersion        INTEGER         — version serveur sur laquelle l'édition est fondée
syncStatus         ENUM            — état de synchronisation de l'enregistrement
```

## 4.3 Tables techniques de synchronisation

```text
sync_operation     — la file de synchronisation (outbox client)  (cf. §5)
sync_conflict      — conflits détectés, en attente de résolution  (cf. §9)
change_tracking    — curseurs de synchronisation (cf. §7)         (serveur: lastServerSeq)
local_metadata     — schéma version, migration locale, deviceId
```

## 4.4 Identifiants locaux vs serveur

```text
Identifiant technique  : UUID (généré côté client, stable pendant toute la vie de l'entité)
Référence métier       : MG-XXX-… attribuée CÔTÉ SERVEUR (ex. MG-ORD-2026-00001245)
```

> La référence métier n'est **jamais** générée côté client. Une Order créée hors connexion reçoit une référence provisoire locale ; le serveur attribue la référence définitive au moment de l'acceptation et la renvoie dans l'acquittement.

## 4.5 Migration locale

Le schéma SQLite est versionné (`local_metadata.schemaVersion`). Toute évolution passe par une migration Drift versionnée, reproductible et testée — symétrique des migrations Flyway côté PostgreSQL.

---

# 5. File de synchronisation (Sync Queue)

## 5.1 Entité `sync_operation`

```text
sync_operation
- id                 UUID PK
- clientOperationId  VARCHAR(64)  UQ          — clé d'idempotence de l'opération (cf. §6)
- deviceId           VARCHAR(64)
- merchantId         UUID
- userId             UUID
- entityType         VARCHAR(50)              — ORDER, ORDER_ITEM, PAYMENT, PRODUCT, CUSTOMER, CASH_SESSION, CASH_MOVEMENT, STOCK_MOVEMENT…
- entityId           UUID                     — id local de l'enregistrement concerné
- operationType      ENUM                     — CREATE, UPDATE, DELETE, STATE_CHANGE
- payload            TEXT (JSON)              — corps sérialisé de l'opération
- baseVersion        INTEGER                  — version de base (concurrence optimiste)
- state              ENUM                     — PENDING, IN_FLIGHT, SYNCED, CONFLICT, FAILED, CANCELLED
- attemptCount       INTEGER
- nextAttemptAt      TIMESTAMP
- lastError          TEXT
- createdAt          TIMESTAMP
- updatedAt          TIMESTAMP
- syncedAt           TIMESTAMP
```

## 5.2 États de la file

```text
PENDING ──► IN_FLIGHT ──► SYNCED
   │            │
   │            ├──► FAILED ──► PENDING (retry, backoff)
   │            └──► CONFLICT (résolution humaine)
   └──► CANCELLED (opération annulée localement avant envoi)
```

## 5.3 Ordre de traitement

La file est consommée en **FIFO par (`merchantId`, `deviceId`)** pour préserver la causalité des opérations d'un même appareil :

```text
1. Les opérations d'un même device sont envoyées dans l'ordre de creation (clientOperationId ordonné).
2. Les dépendances (Order avant OrderItem avant Payment) sont respectées par l'ordre d'émission.
3. Une opération dépendante n'est pas envoyée tant que son parent n'est pas SYNCED.
```

## 5.4 Backpressure et taille de file

```text
Taille de batch max par envoi : 50 opérations (configurable).
La file locale est bornée : un quota par (merchantId, deviceId) déclenche un avertissement.
Une file vide = état « Synchronisé » (🟢).
```

---

# 6. Clés d'idempotence (`Idempotency-Key`)

## 6.1 Principe

Chaque opération de synchronisation porte une clé unique `clientOperationId` générée **côté client** (UUID v4) et **stable** à travers toutes les tentatives de renvoi de la même opération logique.

```text
Vente A (hors connexion)  → clientOperationId = 7f3e…a1b2
   ├── tentative 1 (échec réseau)
   ├── tentative 2 (réessai)          → même clientOperationId
   └── tentative 3 (acquittée)        → même clientOperationId
```

Le serveur garantit que **N renvois de la même clé produisent exactement une seule écriture métier**.

## 6.2 Usage côté serveur

```text
1. Réception d'une opération avec clientOperationId = K
2. Vérification dans PostgreSQL (table idempotency_record) puis Redis (fast-path)
   - déjà traité  → renvoyer la réponse mémorisée, ne rien réécrire
   - en cours     → 409 / verrou distribué (mg:prod:lock:sync:{merchantId}:{K})
   - inconnu      → traiter dans une transaction, enregistrer le résultat
3. Persistance du résultat dans PostgreSQL (jamais Redis seul)
```

## 6.3 Structure serveur

```text
idempotency_record
- id                UUID PK
- merchant_id       UUID
- idempotency_key   VARCHAR(64)  UQ      — le clientOperationId
- operation_type    VARCHAR(50)
- entity_type       VARCHAR(50)
- entity_id         UUID                  — identifiant serveur résultant
- business_reference VARCHAR(40)          — ex. MG-ORD-…, MG-PAY-…
- response_payload  JSON                  — acquittement à rejouer
- status            ENUM                  — PROCESSED
- created_at        TIMESTAMP
- expires_at        TIMESTAMP
```

TTL indicative de rétention : **24 h** (configurable). La clé Redis correspondante suit la même durée (`mg:prod:idempotency:sync:{key}`).

## 6.4 Cohérence avec le paiement

Le champ `Payment.idempotency_key` (`04_DataModel.md` §69) reste distinct : il couvre l'idempotence **auprès du provider**. Le `clientOperationId` couvre l'idempotence **de la synchronisation**. Les deux mécanismes coexistent.

---

# 7. Numéros de séquence / versioning

## 7.1 Séquence serveur globale

Le serveur maintient une séquence de changement **globale par Merchant** (ou globale tout court, indexée) :

```text
change_log
- serverSeq    BIGSERIAL          — numéro de séquence monotone global
- merchantId   UUID
- entityType   VARCHAR(50)
- entityId     UUID
- operationType VARCHAR(20)       — CREATE, UPDATE, DELETE
- updatedAt    TIMESTAMP
```

Chaque mutation acceptée reçoit un `serverSeq` croissant. Ce journal alimente le **flux descendant** (delta).

## 7.2 Version d'enregistrement (concurrence optimiste)

Chaque enregistrement serveur porte un `version` (entier) incrémenté à chaque modification.

```text
Client (édition) :
    lit l'enregistrement avec version = V (baseVersion = V)

Serveur (à l'application) :
    si version actuelle == baseVersion  → appliquer, version := V + 1
    sinon                                → CONFLIT (cf. §9)
```

## 7.3 Curseur client

Le client mémorise le dernier `serverSeq` consommé :

```text
change_tracking
- merchantId      UUID
- lastServerSeq   BIGINT    — curseur du flux descendant
- lastPushAt      TIMESTAMP
- schemaVersion   INTEGER
```

Le flux descendant est donc **incrémental** : le client ne re-télécharge que les changements postérieurs à `lastServerSeq`.

## 7.4 Horloges

Tous les horodatages sont stockés en **UTC**. Pour tolérer la dérive d'horloge entre appareils, l'ordre de causalité repose sur les **séquences** (serveur) et l'ordre d'émission (client), **jamais sur l'horloge locale seule** comme juge final.

---

# 8. Pattern Outbox

## 8.1 Outbox client

Chaque mutation locale écrit, **dans la même transaction SQLite** :

```text
BEGIN (transaction Drift)
    INSERT/UPDATE local_<entity>        (données métier)
    INSERT sync_operation               (outbox)
COMMIT
```

L'entrée `sync_operation` est l'outbox : elle ne disparaît qu'après acquittement serveur (`SYNCED`).

```text
Écriture locale
      ↓
[ transaction SQLite atomique ]
      ├── données métier
      └── sync_operation (outbox)
      ↓
Sync Engine (consomme l'outbox)
      ↓
API (push)
```

## 8.2 Outbox serveur (Transactional Outbox)

Côté serveur, le pattern **Transactional Outbox** garantit la cohérence entre la mutation métier et l'émission d'événements (sans Kafka en V1 — via l'abstraction `EventBus` prévue par `00_Master.md` §5.2) :

```text
BEGIN (transaction PostgreSQL)
    mutation métier (Order, Payment, StockMovement…)
    INSERT outbox_event (OrderCreated, PaymentConfirmed, StockAdjusted…)
COMMIT

Relay (worker in-process)
    lit outbox_event non publié
    publie vers EventBus (in-process, extensible vers Kafka)
    marque published
```

```text
outbox_event
- id           UUID PK
- merchantId   UUID
- eventType    VARCHAR(100)
- aggregateId  UUID
- payload      JSON
- status       ENUM       — PENDING, PUBLISHED, FAILED
- createdAt    TIMESTAMP
- publishedAt  TIMESTAMP
```

L'outbox serveur garantit : **aucun événement émis sans mutation persistée**, et réciproquement.

---

# 9. Résolution de conflits

## 9.1 Principe général

Un conflit survient quand le `baseVersion` envoyé par le client ne correspond plus à la version serveur (modification concurrente par un autre appareil, un backoffice, ou une règle serveur).

```text
CONFLIT détecté
      ↓
Jamais d'écrasement silencieux
      ↓
Classer le conflit par type d'entité (politique dédiée)
      ↓
Résoudre automatiquement OU remonter à l'utilisateur (sync_conflict)
```

## 9.2 Politiques par entité

| Entité | Politique | Justification |
|---|---|---|
| Order, OrderItem | **Serveur fait autorité, conflit remonté** — jamais de fusion automatique | Données financières immuables (SYNC-002, SYNC-007) |
| Payment | **Serveur fait autorité** — le client n'a jamais le dernier mot sur un statut de paiement | Confirmation = provider, pas frontend |
| Product (prix/nom) | **Last-Write-Wins** sur métadonnées, **serveur autorité** sur le prix appliqué en vente (snapshot) | Le prix de vente est recalculé/validé serveur |
| Product (autres champs) | Fusion champ-à-champ si champs disjoints ; LWW sinon | Confort de saisie multi-appareils |
| Customer | **Last-Write-Wins** avec fusion des champs non conflictuels | Données non financières |
| Stock / StockMovement | **Serveur fait autorité** — le stock est dérivé des mouvements rejoués dans l'ordre | `03_TechnicalArchitecture.md` §25 |
| CashSession / CashMovement | **Serveur fait autorité** — rejeu ordonné, totaux recalculés serveur | Traçabilité de caisse |

## 9.3 Stratégies disponibles

```text
SERVER_WINS      — la version serveur est conservée, l'opération client est rejetée (avec motif).
CLIENT_WINS      — l'opération client écrase (réservée aux entités non financières, jamais Order/Payment).
LAST_WRITE_WINS  — comparaison par (updatedAt, serverSeq) ; gagnant = plus récent.
FIELD_MERGE      — fusion champ-à-champ pour les champs non conflictuels.
MANUAL           — remonté à l'utilisateur via sync_conflict pour décision.
```

## 9.4 Entité `sync_conflict`

```text
sync_conflict
- id              UUID PK
- merchantId      UUID
- clientOperationId VARCHAR(64)
- entityType      VARCHAR(50)
- entityId        UUID
- localPayload    TEXT (JSON)   — version locale
- serverPayload   TEXT (JSON)   — version serveur
- baseVersion     INTEGER
- serverVersion   INTEGER
- resolution      ENUM         — PENDING, RESOLVED_LOCAL, RESOLVED_SERVER, DISCARDED
- resolvedAt      TIMESTAMP
- resolutionNote  TEXT
- createdAt       TIMESTAMP
```

L'interface affiche les conflits en attente (badge 🔴 « N conflits ») et propose à l'utilisateur de choisir entre la version locale et la version serveur.

---

# 10. Gestion de la connectivité

## 10.1 Détection d'état

Le mobile distingue quatre états (`03_TechnicalArchitecture.md` §42) :

```text
ONLINE      — connecté, synchronisation possible
OFFLINE     — déconnecté, opérations locales uniquement
SYNCING     — synchronisation en cours
SYNC_ERROR  — échec de synchronisation (à réessayer)
```

## 10.2 Détection technique

```text
- Sonde de connectivité (ping de l'API / health check) avec timeout court.
- Surveillance des changements réseau (connectivity_plus sous Flutter).
- Une défaillance réseau pendant un envoi → bascule SYNC_ERROR, pas de perte de données (outbox intacte).
```

## 10.3 Retry et backoff

```text
Retry exponentiel avec jitter :
    tentative 1 → délai  1 s  (± jitter)
    tentative 2 → délai  2 s
    tentative 3 → délai  4 s
    tentative 4 → délai  8 s
    … plafonné à 5 minutes entre tentatives

+ Retry immédiat déclenché par le retour de la connectivité (ONLINE).
+ Nombre de tentatives non borné pour les écritures métier (l'outbox ne doit jamais abandonner),
  mais escalade observabilité (alerte) au-delà d'un seuil.
+ Les erreurs 4xx (rejet métier) ne sont PAS réessayées → CONFLICT / FAILED avec motif.
```

## 10.4 Expérience utilisateur

```text
🟢 Synchronisé
🟠 Synchronisation en cours
🔴 3 opérations à synchroniser
```

Les opérations financières non synchronisées sont signalées explicitement, jamais présentées comme définitives (cf. `07_UI_UX_Specification.md` §68).

---

# 11. Règles spécifiques — Paiement hors ligne

## 11.1 Rappel de l'autorité

```text
La confirmation d'un paiement digital vient du PROVIDER, pas du frontend.
Le montant du paiement est calculé CÔTÉ SERVEUR à partir de l'Order.
```

## 11.2 Paiement en espèces (CASH) hors connexion

Le paiement en espèces est **autorisé hors connexion** car l'argent est physiquement reçu :

```text
1. Order créée localement (PENDING_PAYMENT)
2. Paiement CASH enregistré localement (Payment.status = SUCCESS localement ? NON — voir règle)
3. Écriture locale : Order + Payment(CASH) + CashMovement dans la même transaction
4. Mise en file (outbox)
5. Synchronisation → validation serveur → PostgreSQL (source de vérité)
```

> **Règle :** même en espèces, le statut définitif `SUCCESS`/`PAID` est confirmé par le serveur. Localement, l'ordre est marqué « en attente de synchronisation ». L'argent est toutefois considéré comme encaissé dans la caisse locale (CashMovement) pour la tenue de caisse.

## 11.3 Paiement digital hors connexion — INTERDIT de confirmer

```text
MOBILE MONEY / CARD / QR / BANK_TRANSFER
        ↓
NÉCESSITE le provider externe (réseau obligatoire)
        ↓
Hors connexion : AUCUNE confirmation possible
```

Comportement imposé :

```text
1. La création de l'Order est possible hors connexion (PENDING_PAYMENT).
2. L'INITIATION d'un paiement digital est refusée hors connexion
   (aucun Payment digital en SUCCESS/PROCESSING ne peut être produit localement).
3. Le mobile ne crée qu'une « intention de paiement » (Payment.status = CREATED)
   qui sera INITIÉE une fois la connexion revenue.
4. L'interface affiche clairement « paiement non confirmé » (jamais « payé »).
5. La confirmation (SUCCESS) provient exclusivement du webhook provider,
   validé, idempotent et audité (cf. 16_RedisSpecification.md §18).
```

```text
Cycle digital correct :
   Order (PENDING_PAYMENT) ──connexion──► Initiation provider ──► PENDING ──► SUCCESS (webhook)
```

## 11.4 Transition Order ↔ Payment

La transition `PENDING_PAYMENT → PAID` ne peut être déclenchée que par un `Payment.status = SUCCESS` validé côté serveur (jamais par le mobile seul). La transition `PENDING_PAYMENT → PAYMENT_FAILED` est possible côté serveur (échec provider).

---

# 12. Partitionnement des données par Merchant

## 12.1 Isolation

```text
MERCHANT = TENANT  (00_Master.md §2.4)
```

Toute donnée métier porte `merchantId`. Le partitionnement de synchronisation s'opère sur ce champ :

```text
- Chaque appareil est rattaché à UN Merchant (via MerchantUser + Register + MerchantOutlet).
- Le flux montant est scoped par merchantId extrait du JWT (jamais fourni par le client).
- Le flux descendant ne renvoie que les changements du merchantId du jeton.
- La file locale et les curseurs sont par (merchantId, deviceId).
```

## 12.2 Règle anti-fuite

```text
Le client ne peut NI lire NI écrire les données d'un autre Merchant.
Le filtrage est appliqué côté backend (00_Master.md §5.4).
Un clientOperationId d'un Merchant A ne peut jamais modifier un enregistrement du Merchant B.
```

## 12.3 Clés Redis

Les clés temporaires de synchronisation sont namespacées par merchant :

```text
mg:prod:sync:lock:{merchantId}:{clientOperationId}
mg:prod:idempotency:sync:{merchantId}:{clientOperationId}
mg:prod:sync:queue:{merchantId}:{deviceId}
```

---

# 13. Flux montant (client → serveur)

## 13.1 Endpoint

```http
POST /api/v1/sync/push
Authorization: Bearer <JWT>
Idempotency-Key: <clientOperationId>
Content-Type: application/json
```

Le `merchantId` et le `userId` sont dérivés du JWT, **jamais** du corps de la requête.

## 13.2 Corps de requête (batch)

```json
{
  "deviceId": "7f3e…",
  "operations": [
    {
      "clientOperationId": "7f3e…a1b2",
      "entityType": "ORDER",
      "operationType": "CREATE",
      "baseVersion": null,
      "payload": { "…": "…" }
    }
  ]
}
```

## 13.3 Traitement serveur

```text
1. Authentification + scope merchantId (JWT)
2. Par opération, dans l'ordre :
     a. Idempotence (clientOperationId) → réponse mémorisée ou traitement
     b. Validation métier (règles serveur, transitions d'état autorisées)
     c. Contrôle de version (baseVersion vs version actuelle) → conflit éventuel
     d. Transaction PostgreSQL : mutation + outbox_event + change_log
3. Réponse d'acquittement par opération
```

## 13.4 Réponse d'acquittement

```json
{
  "operations": [
    {
      "clientOperationId": "7f3e…a1b2",
      "status": "ACCEPTED",
      "serverEntityId": "8a2c…",
      "businessReference": "MG-ORD-2026-00001245",
      "serverVersion": 3,
      "serverSeq": 10241
    },
    {
      "clientOperationId": "7f3e…c3d4",
      "status": "CONFLICT",
      "reason": "VERSION_MISMATCH",
      "serverVersion": 7
    },
    {
      "clientOperationId": "7f3e…e5f6",
      "status": "REJECTED",
      "reason": "INVALID_STATE_TRANSITION"
    }
  ]
}
```

## 13.5 Statuts d'acquittement

```text
ACCEPTED   — écriture appliquée, identifiant/référence serveur renvoyés
CONFLICT   — divergence de version (cf. §9)
REJECTED   — rejet métier (transition interdite, validation échouée)
DUPLICATE  — clientOperationId déjà traité (réponse mémorisée rejouée)
```

Le client met alors à jour `sync_operation.state` (SYNCED / CONFLICT / FAILED) et, sur ACCEPTED, écrit la référence métier définitive dans sa table locale.

---

# 14. Flux descendant (serveur → client)

## 14.1 Endpoint

```http
GET /api/v1/sync/pull?since=<lastServerSeq>&limit=200
Authorization: Bearer <JWT>
```

Le curseur `lastServerSeq` provient du client ; le serveur renvoie les changements postérieurs (delta).

## 14.2 Réponse

```json
{
  "changes": [
    {
      "serverSeq": 10242,
      "entityType": "PRODUCT",
      "entityId": "8a2c…",
      "operationType": "UPDATE",
      "version": 4,
      "payload": { "…": "…" },
      "updatedAt": "2026-11-10T09:12:00Z"
    }
  ],
  "latestServerSeq": 10242,
  "hasMore": false
}
```

## 14.3 Snapshot initial

Au premier lancement (ou après réinstallation), le client effectue un **snapshot initial** :

```text
GET /api/v1/sync/snapshot  →  MASTER DATA complet du merchantId
   (produits, catégories, clients, outlets, registers, configuration)
   + latestServerSeq pour initialiser le curseur
```

Le snapshot est paginé et reprend là où il s'est arrêté en cas d'interruption.

## 14.4 Application locale des changements

```text
1. Pour chaque change, dans l'ordre de serverSeq :
     - si l'entité n'a pas de modification locale en attente → appliquer directement ;
     - sinon → appliquer et marquer la divergence éventuelle (sync_conflict).
2. Mettre à jour change_tracking.lastServerSeq = latestServerSeq.
```

---

# 15. Intégrité et reprise après panne

## 15.1 Atomicité locale

```text
Chaque mutation locale = UNE transaction SQLite (données + outbox).
Un crash au milieu ne laisse jamais une donnée sans entrée d'outbox,
ni une entrée d'outbox sans donnée.
```

## 15.2 Reprise après crash

```text
Crash pendant l'envoi :
    - sync_operation reste IN_FLIGHT
    - au redémarrage, l'IN_FLIGHT est remis en PENDING (sauf si l'acquittement était reçu)
    - le renvoi est idempotent (clientOperationId) → pas de double écriture

Crash côté serveur (après commit, avant réponse) :
    - la mutation est persistée ; la réponse mémorisée (idempotency_record)
      est rejouée au renvoi → cohérence
```

## 15.3 Vérifications d'intégrité

```text
- Checksum du payload de chaque opération (détection de corruption locale).
- Contrôle de cohérence « outbox vs données » au démarrage (audit local).
- Réconciliation périodique : le serveur peut renvoyer un condensé (count/hash par entité)
  que le client compare à son état local pour détecter une divergence silencieuse.
```

## 15.4 Garanties de livraison

```text
Au moins une fois (at-least-once) pour le flux montant,
compensé par l'idempotence serveur (exactement-une-fois du point de vue métier).

Au moins une fois pour le flux descendant,
compensé par le curseur serverSeq et l'application idempotente locale.
```

## 15.5 Données financières

```text
Jamais de suppression physique des données financières.
On applique : ajout de mouvements, ajustements, annulations — pas d'UPDATE de montants en place.
Les éléments financiers et d'audit ne sont normalement jamais supprimés (04_DataModel.md §39).
```

---

# 16. Sécurité de la synchronisation

```text
- Authentification : JWT (Keycloak), scope merchantId vérifié côté serveur.
- Le corps de requête ne peut pas fournir merchantId/userId (dérivés du JWT).
- HTTPS obligatoire.
- Rate limiting sur /api/v1/sync/* (par deviceId/merchantId).
- Idempotence pour éviter le rejeu abusif.
- Journal d'audit des opérations synchronisées (AuditLog).
- Les payloads sensibles ne sont pas loggés en clair (pas de données de paiement).
- La base locale peut être chiffrée (encryption at rest) si le device le permet.
```

---

# 17. Observabilité et monitoring

```text
Indicateurs :
    sync_queue_size          — taille de la file locale
    sync_push_latency        — latence d'un push
    sync_success_rate        — taux d'opérations acceptées
    sync_conflict_rate       — taux de conflits
    sync_retry_rate          — taux de retries
    outbox_lag               — retard de publication de l'outbox serveur
    idempotency_hit_rate     — doublons détectés

Alertes :
    SYNC_FAILURE_RATE_HIGH
    OUTBOX_BACKLOG_HIGH
    IDEMPOTENCY_CONFLICT_SPIKE
```

Les erreurs de synchronisation sont observables mais non exposées inutilement à l'utilisateur.

---

# 18. Tests

```text
18.1 Idempotence
      - envoi unique, envoi répété, envois concurrents (même clientOperationId)
      - expiration de la clé d'idempotence

18.2 Conflits
      - version mismatch sur Product / Customer / Order / Stock
      - politique SERVER_WINS / LWW / FIELD_MERGE / MANUAL
      - aucun écrasement silencieux d'une Order

18.3 Reprise après panne
      - crash avant envoi, pendant l'envoi, après commit serveur
      - redémarrage de l'app → reprise de la file sans perte ni doublon

18.4 Connectivité
      - perte réseau pendant un push, retour du réseau → reprise
      - backoff exponentiel + jitter (pas de tempête de retries)

18.5 Paiement hors ligne
      - paiement digital refusé hors connexion (jamais SUCCESS local)
      - paiement CASH synchronisé et confirmé par le serveur
      - confirmation exclusivement par webhook provider

18.6 Isolation Merchant
      - un clientOperationId du Merchant A ne peut pas écrire sur le Merchant B
      - le flux descendant ne renvoie que le merchantId du jeton

18.7 Flux montant / descendant
      - FIFO par device, dépendances Order → OrderItem → Payment
      - delta depuis lastServerSeq, snapshot initial, hasMore paginé
```

---

# 19. Règles Hermes

Hermes doit impérativement respecter :

1. **PostgreSQL est la seule source de vérité.** Jamais SQLite ni Redis.
2. **Jamais de confirmation de paiement digital hors connexion** (le statut `SUCCESS` vient du provider).
3. **Jamais de génération de référence métier côté client** (MG-XXX-… est attribuée par le serveur).
4. **Jamais d'écrasement silencieux** d'une Order, d'un Payment ou d'un StockMovement.
5. **Toute écriture synchronisée est idempotente** via `clientOperationId`.
6. **Le montant d'un paiement est calculé côté serveur** à partir de l'Order.
7. **L'isolation par Merchant est appliquée côté backend**, jamais côté frontend uniquement.
8. **Écriture locale atomique** : données + entrée d'outbox dans la même transaction SQLite.
9. **Outbox serveur transactionnelle** : aucune émission d'événement sans mutation persistée.
10. **Redis ne garantit jamais l'idempotence à lui seul** — la persistance est dans PostgreSQL.
11. **Les données financières sont immuables** (mouvements/ajustements, pas d'UPDATE de montants).
12. **Le vocabulaire du Master est respecté** (Merchant, MerchantOutlet, Register, Order, OrderItem, Payment, Customer, MerchantUser).
13. **Les transitions d'état autorisées sont celles du Master** (`00_Master.md` §4.1 et §4.2) — rejetées sinon.
14. **Aucun montant en `float`/`double`** — `DECIMAL(19,4)` ou centimes `BIGINT`, devise XOF explicite.

---

# 20. Règles de cohérence

1. Ce document est subordonné à `00_Master.md`.
2. Le vocabulaire officiel du Master est utilisé partout (pas de « tenant », « store », « sale »).
3. Les états Order/Payment cités ici sont ceux du Master ; toute évolution passe par le Master.
4. En cas de contradiction avec `03_TechnicalArchitecture.md` (ex. `tenantId`), le Master (`merchantId`) fait foi.
5. La spécification détaillée du mobile (composants, structure `lib/`) est hors périmètre : voir `20_MobileArchitecture.md`.

---

# 21. Statut

**Document de référence — Protocole de synchronisation offline-first de MAGIC GOLD.**

Toute modification doit être :
- justifiée ;
- versionnée ;
- propagée aux documents dépendants (`03_TechnicalArchitecture.md`, `04_DataModel.md`, `05_API_Specification.md`, `16_RedisSpecification.md`, `20_MobileArchitecture.md`).

---

**Nectux Ingénieries**
**MAGIC GOLD — Simple pour commencer. Puissant pour grandir.**
