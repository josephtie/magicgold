# MAGIC GOLD — Master Reference Document

**Document :** `00_Master.md`  
**Version :** 1.0  
**Statut :** Référence de cohérence — **Autorité suprême**  
**Projet :** MAGIC GOLD  
**Éditeur :** Nectux Ingénieries  

---

> **Ce document prime sur tous les autres en cas de contradiction.**  
> Toute divergence détectée entre un document et ce Master doit être signalée et corrigée.  
> Le Master définit le vocabulaire, les périmètres, les états authoritative et la numérotation officielle.

---

# 1. Objet

Ce document centralise les décisions de référence qui doivent être respectées par tous les documents du projet MAGIC GOLD.

Il résout les contradictions introduites par la production itérative des spécifications et fixe :

- le vocabulaire officiel ;
- les entités V1 vs V2/V3 ;
- les états authoritative (paiement, commande, marchand) ;
- le modèle multi-tenant ;
- la numérotation officielle des documents ;
- les décisions techniques tranchées.

---

# 2. Vocabulaire officiel

## 2.1 Règle générale

Un seul terme par concept. Tout document utilisant un synonyme doit être corrigé.

## 2.2 Termes réservés

| Terme officiel | Synonymes interdits | Définition |
|---|---|---|
| **Merchant** | Tenant, Commerçant, Marchand | L'entité commerciale qui utilise MAGIC GOLD. Un Merchant = un tenant logique. |
| **MerchantOutlet** | Store, Boutique, Magasin, Point de vente | Un point de vente physique appartenant à un Merchant. |
| **Register** | Caisse, Terminal de caisse | Une caisse physique rattachée à un MerchantOutlet. |
| **CashSession** | Session de caisse, Session | Une session d'ouverture/fermeture d'une Register. |
| **Product** | Article | Un produit ou service vendu par un Merchant. |
| **ProductCategory** | Catégorie | Une catégorie de produits (hiérarchique). |
| **Order** | Commande, Vente | L'engagement commercial entre un acheteur et un Merchant. |
| **OrderItem** | Ligne de commande, SaleItem | Une ligne détaillée d'une Order. |
| **Payment** | Transaction de paiement | L'opération de paiement d'une Order. |
| **Customer** | Client, Acheteur | La personne qui achète. Peut être anonyme. N'est pas un User. |
| **User** | Utilisateur, Compte utilisateur | Un acteur authentifié de MAGIC GOLD (Merchant, Operator, Admin...). |
| **MerchantUser** | Operator, Collaborateur, Caissier, Vendeur | Un User rattaché à un Merchant avec un rôle. |

## 2.3 Sale vs Order

Dans MAGIC GOLD, **une vente au comptoir est une Order**.

```text
SALE = ORDER avec paiement immédiat
```

Le système utilise **uniquement** l'entité `Order` et `OrderItem`.  
Il n'existe pas d'entité séparée `Sale` / `SaleItem`.  

Une Order de type « vente rapide » se distingue par :
- `customerId = null` (client anonyme) ;
- transition rapide `PENDING_PAYMENT → PAID → COMPLETED` ;
- pas de traitement logistique.

## 2.4 Tenant = Merchant

```text
MERCHANT = TENANT
```

Il n'existe pas d'entité `Tenant` séparée du `Merchant`.  
Chaque Merchant constitue un tenant logique. Toutes les données métier sont isolées par `merchantId`.  

L'entité `Tenant` mentionnée dans `01_ProjectVision.md` fait référence au concept SaaS, implémenté via `Merchant`.

---

# 3. Périmètre V1 / V2 / V3

## 3.1 Principe

Le périmètre V1 est volontairement limité. Les entités hors V1 ne doivent pas être développées.

## 3.2 V1 — MVP Commerce de détail

| Domaine | Entités V1 |
|---|---|
| Identité | User, Role, UserRole |
| Commerce | Merchant, MerchantOutlet, MerchantUser, Product, ProductCategory |
| Vente | Order, OrderItem, OrderStatusHistory |
| Paiement | Payment, PaymentProvider, WebhookEvent |
| Caisse | Register, CashSession, CashMovement |
| Client | Customer (anonyme ou identifié, sans compte User obligatoire) |
| Stock | StockMovement (mouvements traçables) |
| Système | Notification, NotificationTemplate, AuditLog, Configuration, File |

### Règles V1

- ❌ Pas de Wallet (client ou marchand)
- ❌ Pas de LoyaltyAccount, Reward, Campaign, Coupon
- ❌ Pas de Ledger comptable séparé (les données financières sont dans Payment + Order)
- ❌ Pas de Settlement automatisé (le règlement se fait côté provider)
- ❌ Pas de fidélité, points, cashback
- ❌ Pas de compte client obligatoire pour acheter
- ❌ Pas de marketplace multi-marchands

## 3.3 V2 — Commerce étendu

| Domaine | Entités V2 |
|---|---|
| Finance | Wallet, WalletTransaction, Refund (entité séparée) |
| Achats | Supplier, Purchase, PurchaseItem |
| Dépenses | Expense |
| Crédit | Credit, CreditPayment |
| Stock avancé | Inventory, InventoryItem, Transfer |
| Paiement | Settlement, Reconciliation, LedgerEntry |
| Marchand | MerchantOnboarding (sous-domaine KYC) |

## 3.4 V3 — Fidélité et marketing

| Domaine | Entités V3 |
|---|---|
| Fidélité | LoyaltyAccount, LoyaltyTransaction, Reward, RewardRedemption |
| Marketing | Campaign, Coupon, CouponUsage |

---

# 4. États authoritative

## 4.1 Payment states

```text
CREATED
   ↓
PENDING
   ↓
PROCESSING
   ↓
SUCCESS
```

Branches alternatives :

```text
PENDING     → FAILED
PENDING     → CANCELLED
PROCESSING  → FAILED
SUCCESS     → REFUND_PENDING
REFUND_PENDING → PARTIALLY_REFUNDED
REFUND_PENDING → REFUNDED
PARTIALLY_REFUNDED → REFUNDED
```

**Liste officielle :**

```text
CREATED
PENDING
PROCESSING
SUCCESS
FAILED
CANCELLED
REFUND_PENDING
PARTIALLY_REFUNDED
REFUNDED
```

Transitions interdites :

```text
SUCCESS → PENDING
SUCCESS → CREATED
SUCCESS → FAILED
REFUNDED → SUCCESS
```

## 4.2 Order states

```text
DRAFT
   ↓
PENDING_PAYMENT
   ↓
PAID
   ↓
CONFIRMED
   ↓
PROCESSING
   ↓
READY
   ↓
COMPLETED
```

Branches alternatives :

```text
PENDING_PAYMENT → PAYMENT_FAILED
PENDING_PAYMENT → CANCELLED
PAYMENT_FAILED  → PENDING_PAYMENT  (retry)
PAID            → REFUND_PENDING
PAID            → CANCELLED
CONFIRMED       → CANCELLED
PROCESSING      → CANCELLED
REFUND_PENDING  → REFUNDED
```

**Liste officielle :**

```text
DRAFT
PENDING_PAYMENT
PAYMENT_FAILED
PAID
CONFIRMED
PROCESSING
READY
COMPLETED
CANCELLED
REFUND_PENDING
REFUNDED
```

Une commande annulée ne doit pas être réactivée. Une nouvelle commande doit être créée.

## 4.3 Merchant states

### Statut marchand

```text
PENDING
   ↓
ACTIVE
   ├── SUSPENDED → ACTIVE
   ├── BLOCKED
   └── CLOSED
```

**Liste officielle :**

```text
PENDING
ACTIVE
SUSPENDED
BLOCKED
CLOSED
```

### Onboarding (V2 — sous-domaine séparé)

```text
REGISTERED
   ↓
PROFILE_INCOMPLETE
   ↓
PROFILE_COMPLETED
   ↓
VERIFICATION_PENDING
   ↓
VERIFIED
   ↓
PAYMENT_CONFIGURATION_PENDING
   ↓
READY
```

L'onboarding est un sous-domaine V2 avec sa propre machine d'état.  
Le `merchant.status` ne doit pas devenir un fourre-tout.

## 4.4 Customer types

```text
ANONYMOUS     — aucune information personnelle
IDENTIFIED    — téléphone et/ou nom associé
REGISTERED    — dispose d'un compte User MAGIC GOLD (optionnel)
```

Un Customer n'est jamais obligatoirement un User.  
Le téléphone d'un Customer n'identifie pas un client global MAGIC GOLD en V1.

---

# 5. Décisions techniques tranchées

## 5.1 Version Java

```text
Java 17
Spring Boot 3.5.x
```

**Raison :** Cohérence avec les autres produits Nectux (RHPAIE, E-COLLECTE).  
Le `03_TechnicalArchitecture.md` mentionnait Java 21+ — cette décision est **annulée** au profit de Java 17.

## 5.2 Architecture

```text
Modular Monolith (Spring Boot)
PostgreSQL (source de vérité)
Redis 7.x (cache, OTP, rate limiting, idempotency)
Keycloak (OAuth 2.0 / OIDC)
Flutter (mobile)
Docker / Docker Compose
```

Pas de microservices en V1. Pas de Kafka en V1.  
L'évolution vers Kafka est préparée via l'abstraction `EventBus` + Transactional Outbox.

## 5.3 Paiements

- Les paiements passent par des **providers externes** (CinetPay, FeexPay, etc.)
- Le domaine métier ne connaît que l'interface `PaymentProvider`
- MAGIC GOLD ne détient pas les fonds
- Pas de Wallet marchand V1
- Le montant du paiement est **calculé côté serveur** à partir de l'Order
- Les webhooks sont **validés, idempotents et audités**
- La confirmation vient du **provider**, pas du frontend

## 5.4 Multi-tenant

```text
Merchant = Tenant
```

Isolation par `merchantId` sur toutes les données métier.  
Le filtrage est appliqué côté backend, jamais côté frontend uniquement.

## 5.5 Monnaie

```text
XOF (Franc CFA)
```

Stockage : `DECIMAL(19,4)` ou `BIGINT` (centimes).  
Jamais `FLOAT` ou `DOUBLE`.

## 5.6 Identifiants

- Technique : `UUID` (clé primaire)
- Métier : références uniques (`MG-ORD-2026-00001245`, `MG-PAY-000001`, `MG-MER-000125`, `MG-CUS-000001`)

---

# 6. Numérotation officielle des documents

| N° | Fichier | Titre | Statut |
|---|---|---|---|
| 00 | `00_Master.md` | Master Reference | ✅ Présent |
| 01 | `01_ProjectVision.md` | Project Vision | ✅ Présent |
| 02 | `02_ProjectOverview.md` | Project Overview | ✅ Présent |
| 03 | `03_TechnicalArchitecture.md` | Technical Architecture | ✅ Présent |
| 04 | `04_DataModel.md` | Data Model | ✅ Présent (corrigé) |
| 05 | `05_API_Specification.md` | API Specification | ✅ Présent |
| 06 | `06_SecurityArchitecture.md` | Security Architecture | ✅ Présent |
| 07 | `07_UI_UX_Specification.md` | UI/UX Specification | ✅ Présent |
| 08 | `08_Notifications.md` | Notification Specification | ✅ Présent |
| 09 | `09_PaymentArchitecture.md` | Payment Architecture | ✅ Présent |
| 10 | `10_OrderManagement.md` | Order Management | ✅ Présent |
| 11 | `11_MerchantSpecification.md` | Merchant Specification | ✅ Présent |
| 12 | `12_CustomerSpecification.md` | Customer Specification | ✅ Présent |
| 13 | `13_Reporting.md` | Reporting Specification | ✅ Présent |
| 14 | `14_IntegrationSpecification.md` | Integration Specification | ✅ Présent |
| 15 | `15_TestStrategy.md` | Test Strategy | ✅ Présent |
| 16 | `16_RedisSpecification.md` | Redis Specification | ✅ Présent |

### Documents à créer ultérieurement

| N° | Fichier | Titre | Priorité |
|---|---|---|---|
| 17 | `17_Deployment.md` | Deployment Guide | Moyenne |
| 18 | `18_BusinessRules.md` | Business Rules | Moyenne |
| 19 | `19_SynchronizationProtocol.md` | Offline Sync Protocol | Haute (V1) |
| 20 | `20_MobileArchitecture.md` | Mobile Architecture | Haute (V1) |

---

# 7. Règles de cohérence

1. En cas de contradiction entre deux documents, le `00_Master.md` fait foi.
2. Tout nouveau document doit référencer le Master et respecter le vocabulaire officiel.
3. Toute évolution du périmètre V1 doit être documentée dans le Master avant implémentation.
4. Les états (payment, order, merchant) ne peuvent être modifiés que par une mise à jour du Master.
5. Le Data Model (`04_DataModel.md`) doit respecter la répartition V1/V2/V3 définie ici.
6. Un document = un sujet. Les documents hybrides doivent être scindés.

---

# 8. Glossaire

| Terme | Définition |
|---|---|
| Merchant | Entité commerciale utilisant MAGIC GOLD. Équivalent à un tenant SaaS. |
| MerchantOutlet | Point de vente physique d'un Merchant. |
| Register | Caisse physique rattachée à un MerchantOutlet. |
| CashSession | Session d'ouverture/fermeture d'une Register. |
| Order | Engagement commercial (vente ou commande). |
| OrderItem | Ligne détaillée d'une Order avec snapshot produit. |
| Payment | Opération de paiement d'une Order via un provider. |
| Customer | Acheteur. Peut être anonyme. N'est pas un User. |
| User | Acteur authentifié (Merchant, Operator, Admin...). |
| MerchantUser | User rattaché à un Merchant avec un rôle (Manager, Cashier, Operator). |
| PaymentProvider | Prestataire de paiement externe (CinetPay, FeexPay...). |
| WebhookEvent | Événement reçu d'un système externe, stocké pour idempotence. |
| OutboxEvent | Événement métier persisté transactionnellement avec les données. |
| Idempotency-Key | Clé unique empêchant le double traitement d'une opération. |
| Snapshot | Conservation des valeurs historiques (prix, nom produit) au moment d'une Order. |
| XOF | Franc CFA Ouest-Africain. Devise par défaut de MAGIC GOLD. |

---

# 9. Statut

**Document de référence — Autorité suprême du projet MAGIC GOLD.**

Toute modification de ce document doit être :
- justifiée ;
- validée par Joseph ;
- versionnée ;
- propagée aux documents dépendants.

---

**Nectux Ingénieries**  
**MAGIC GOLD — Simple pour commencer. Puissant pour grandir.**
