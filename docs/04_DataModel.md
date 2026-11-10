# MAGIC GOLD — Data Model

**Version:** 2.0
**Document:** 04_DataModel.md
**Projet:** MAGIC GOLD
**Statut:** Référence de conception
**Audience:** Architecte, développeurs Backend/Frontend/Mobile, QA, DevOps, IA Hermes

> **Ce document est subordonné à `00_Master.md`.**  
> En cas de contradiction, le Master fait foi.  
> Les entités sont classées en 3 phases : **V1 (MVP)**, **V2 (commerce étendu)**, **V3 (fidélité et marketing)**.  
> Seules les entités V1 doivent être implémentées dans le MVP.

---

# 1. Objectif

Ce document définit le modèle de données fonctionnel et logique de MAGIC GOLD.

Il constitue la référence pour :

* la conception de la base de données ;
* les entités métier ;
* les relations entre entités ;
* les contraintes d'intégrité ;
* les statuts métier ;
* les règles de traçabilité ;
* les APIs ;
* les tests ;
* les évolutions futures.

Toute implémentation doit respecter ce modèle sauf décision d'architecture explicitement documentée.

---

# 1.1 Répartition des entités par phase

## V1 — MVP Commerce de détail

```text
User, Role, UserRole
Merchant, MerchantOutlet, MerchantUser
Product, ProductCategory
Order, OrderItem, OrderStatusHistory
Payment, PaymentProvider, WebhookEvent
Customer
Register, CashSession, CashMovement
StockMovement
Notification, NotificationTemplate
AuditLog, Configuration, File
```

## V2 — Commerce étendu

```text
Wallet, WalletTransaction
Supplier, Purchase, PurchaseItem
Expense
Credit, CreditPayment
Inventory, InventoryItem, Transfer
Refund (entité séparée)
Settlement, Reconciliation, LedgerEntry
MerchantOnboarding (KYC)
```

## V3 — Fidélité et marketing

```text
LoyaltyAccount, LoyaltyTransaction
Reward, RewardRedemption
Campaign, Coupon, CouponUsage
```

### Règle de périmètre

> Les entités V2 et V3 sont **documentées** dans ce Data Model pour préparer l'architecture,  
> mais ne doivent **pas être implémentées** dans le MVP.  
> Toute implémentation d'une entité hors V1 doit être justifiée et validée.

---

# 2. Principes de conception

MAGIC GOLD doit respecter les principes suivants :

1. Les données métier importantes ne doivent jamais être stockées uniquement côté frontend.
2. Les identifiants techniques doivent être distincts des références métier.
3. Les opérations financières doivent être traçables.
4. Les opérations sensibles doivent être auditables.
5. Les suppressions physiques doivent être évitées pour les données métier critiques.
6. Les statuts doivent être explicites.
7. Les dates doivent être stockées avec une référence temporelle cohérente.
8. Les montants monétaires doivent utiliser un type décimal précis et non `float`.
9. Les transactions financières doivent être idempotentes.
10. Les données personnelles doivent être protégées.
11. Les relations doivent être contraintes par des clés étrangères.
12. Les changements importants doivent être historisés.

---

# 3. Architecture logique générale

Le modèle métier est organisé autour des domaines suivants :

```text
                    ┌──────────────────┐
                    │      USER        │
                    └────────┬─────────┘
                             │
                    ┌────────▼─────────┐
                    │     CUSTOMER     │
                    └────────┬─────────┘
                             │
          ┌──────────────────┼──────────────────┐
          │                  │                  │
          ▼                  ▼                  ▼
     WALLET              LOYALTY          NOTIFICATION
          │
          │
          ▼
     TRANSACTION
          │
     ┌────┴─────┐
     ▼          ▼
 PAYMENT      REWARD
     │
     ▼
 PAYMENT_PROVIDER


       MERCHANT
          │
     ┌────┼───────────┐
     ▼    ▼           ▼
  OUTLET PRODUCT   MERCHANT_USER
          │
          ▼
       ORDER
          │
          ▼
    ORDER_ITEM
```

---

# 4. Principales entités

Le système doit au minimum prévoir les entités suivantes, classées par phase de mise en œuvre :

| Phase | Domaine      | Entité               |
| ----- | ------------ | -------------------- |
| V1    | Identité     | User                 |
| V1    | Identité     | Role                 |
| V1    | Identité     | UserRole             |
| V1    | Client       | Customer             |
| V1    | Commerce     | Merchant             |
| V1    | Commerce     | MerchantOutlet       |
| V1    | Commerce     | MerchantUser         |
| V1    | Commerce     | Product              |
| V1    | Commerce     | ProductCategory      |
| V1    | Vente        | Order                |
| V1    | Vente        | OrderItem            |
| V1    | Vente        | OrderStatusHistory    |
| V1    | Paiement     | Payment              |
| V1    | Paiement     | PaymentProvider      |
| V1    | Caisse       | Register             |
| V1    | Caisse       | CashSession          |
| V1    | Caisse       | CashMovement         |
| V1    | Stock        | StockMovement        |
| V1    | Notification | Notification         |
| V1    | Notification | NotificationTemplate |
| V1    | Audit        | AuditLog             |
| V1    | Système      | Configuration        |
| V1    | Système      | File                 |
| V1    | Système      | WebhookEvent         |
| V2    | Client       | CustomerAddress      |
| V2    | Client       | CustomerDevice       |
| V2    | Finance      | Wallet               |
| V2    | Finance      | WalletTransaction    |
| V2    | Achats       | Supplier             |
| V2    | Achats       | Purchase              |
| V2    | Achats       | PurchaseItem          |
| V2    | Dépenses     | Expense               |
| V2    | Crédit       | Credit                |
| V2    | Crédit       | CreditPayment         |
| V2    | Stock        | Inventory             |
| V2    | Stock        | InventoryItem         |
| V2    | Stock        | Transfer              |
| V2    | Paiement     | Refund (entité séparée) |
| V2    | Paiement     | Settlement             |
| V2    | Paiement     | Reconciliation         |
| V2    | Paiement     | LedgerEntry            |
| V2    | Marchand     | MerchantOnboarding     |
| V3    | Fidélité     | LoyaltyAccount       |
| V3    | Fidélité     | LoyaltyTransaction   |
| V3    | Fidélité     | Reward               |
| V3    | Fidélité     | RewardRedemption     |
| V3    | Marketing    | Campaign             |
| V3    | Marketing    | Coupon               |
| V3    | Marketing    | CouponUsage          |

---

# 5. USER

Représente un compte utilisateur authentifié.

## Champs principaux

```text
User
- id
- external_id
- username
- email
- phone_number
- password_hash (si authentification locale)
- first_name
- last_name
- status
- last_login_at
- created_at
- updated_at
- deleted_at
```

## Statuts

```text
ACTIVE
INACTIVE
LOCKED
SUSPENDED
PENDING
```

L'authentification peut être externalisée vers Keycloak ou un autre Identity Provider.

Dans ce cas, `external_id` doit contenir l'identifiant du fournisseur d'identité.

---

# 6. ROLE

Définit les rôles applicatifs.

Exemples :

```text
SUPER_ADMIN
ADMIN
CUSTOMER
MERCHANT
MERCHANT_MANAGER
MERCHANT_OPERATOR
SUPPORT
FINANCE
AUDITOR
```

Un utilisateur peut posséder plusieurs rôles.

---

# 7. USER_ROLE

Table d'association :

```text
User
   │
   └──< UserRole >── Role
```

Champs :

```text
user_id
role_id
assigned_at
assigned_by
```

Une contrainte unique doit empêcher l'association multiple du même rôle au même utilisateur.

---

# 8. CUSTOMER

Représente le client/utilisateur final de MAGIC GOLD.

```text
Customer
- id
- user_id
- customer_number
- first_name
- last_name
- phone_number
- email
- date_of_birth
- gender
- status
- registration_source
- created_at
- updated_at
```

## customer_number

Le numéro client est une référence métier unique.

Exemple :

```text
MG-CUS-000001
```

Il ne doit pas être utilisé comme clé primaire technique.

---

# 9. CUSTOMER_ADDRESS

Permet de gérer plusieurs adresses.

```text
CustomerAddress
- id
- customer_id
- label
- address_line
- city
- district
- country
- latitude
- longitude
- is_default
- created_at
- updated_at
```

Un client peut avoir plusieurs adresses.

---

# 10. CUSTOMER_DEVICE

Permet de gérer les appareils utilisés par le client.

```text
CustomerDevice
- id
- customer_id
- device_identifier
- platform
- app_version
- push_token
- last_seen_at
- status
- created_at
- updated_at
```

## Platform

```text
ANDROID
IOS
WEB
OTHER
```

Les identifiants d'appareil doivent être traités comme des données sensibles.

---

# 11. MERCHANT

Représente un commerçant ou une entreprise partenaire.

```text
Merchant
- id
- merchant_number
- legal_name
- commercial_name
- business_type
- registration_number
- tax_number
- phone_number
- email
- address
- city
- country
- logo_file_id
- status
- created_at
- updated_at
```

## Statuts

```text
PENDING
ACTIVE
SUSPENDED
BLOCKED
CLOSED
```

---

# 12. MERCHANT_OUTLET

Un commerçant peut posséder plusieurs points de vente.

```text
Merchant
    │
    └──< MerchantOutlet
```

```text
MerchantOutlet
- id
- merchant_id
- outlet_number
- name
- address
- city
- district
- latitude
- longitude
- phone_number
- status
- created_at
- updated_at
```

---

# 13. MERCHANT_USER

Associe les utilisateurs aux commerçants.

```text
MerchantUser
- id
- merchant_id
- user_id
- outlet_id
- role
- status
- created_at
- updated_at
```

Cela permet par exemple :

```text
Merchant
   │
   ├── Manager
   ├── Cashier
   └── Operator
```

---

# 14. PRODUCT_CATEGORY

Catégories de produits/services.

```text
ProductCategory
- id
- parent_id
- name
- description
- status
- created_at
- updated_at
```

Le `parent_id` permet une hiérarchie :

```text
Alimentation
 ├── Boissons
 ├── Épicerie
 └── Produits frais
```

---

# 15. PRODUCT

Produit ou service proposé par un commerçant.

```text
Product
- id
- merchant_id
- category_id
- sku
- name
- description
- unit_price
- currency
- stock_quantity
- status
- image_file_id
- created_at
- updated_at
```

## Statuts

```text
DRAFT
ACTIVE
INACTIVE
OUT_OF_STOCK
ARCHIVED
```

---

# 16. ORDER

Représente une commande.

```text
Order
- id
- order_number
- customer_id
- merchant_id
- outlet_id
- subtotal
- discount_amount
- tax_amount
- total_amount
- currency
- status
- payment_status
- created_at
- updated_at
```

## Order status

```text
CREATED
CONFIRMED
PROCESSING
READY
COMPLETED
CANCELLED
REFUNDED
```

## Payment status

```text
UNPAID
PENDING
PAID
PARTIALLY_PAID
REFUNDED
```

---

# 17. ORDER_ITEM

Détail d'une commande.

```text
OrderItem
- id
- order_id
- product_id
- product_name_snapshot
- quantity
- unit_price
- discount_amount
- total_amount
```

Le nom et le prix du produit doivent être conservés comme **snapshot** au moment de la commande.

Une modification ultérieure du produit ne doit pas modifier l'historique d'une commande.

---

# 18. PAYMENT

Représente une tentative ou opération de paiement.

```text
Payment
- id
- payment_number
- order_id
- customer_id
- provider_id
- external_transaction_id
- idempotency_key
- amount
- currency
- payment_method
- status
- initiated_at
- completed_at
- failed_at
- failure_reason
- metadata
```

## Payment methods

```text
MOBILE_MONEY
CARD
BANK_TRANSFER
WALLET
CASH
QR_CODE
OTHER
```

## Payment status

```text
INITIATED
PENDING
SUCCESS
FAILED
CANCELLED
REFUNDED
PARTIALLY_REFUNDED
```

---

# 19. PAYMENT_PROVIDER

Référence les prestataires de paiement.

```text
PaymentProvider
- id
- code
- name
- type
- status
- configuration_reference
- created_at
- updated_at
```

Exemples possibles :

```text
CINETPAY
FEEXPAY
ORANGE_MONEY
MTN_MONEY
MOOV_MONEY
WAVE
CARD
BANK
```

Les secrets API ne doivent jamais être stockés en clair dans cette table.

---

# 20. WALLET *(V2 — hors MVP)*

> ⚠️ **Cette entité n'appartient pas au périmètre V1.**  
> Elle est documentée pour préparer l'architecture.  
> Ne pas implémenter dans le MVP.

Portefeuille électronique du client.

```text
Wallet
- id
- customer_id
- wallet_number
- currency
- available_balance
- pending_balance
- status
- created_at
- updated_at
```

## Règle critique

Le solde ne doit jamais être modifié arbitrairement depuis le frontend.

Toutes les variations doivent provenir d'une transaction financière enregistrée.

---

# 21. WALLET_TRANSACTION *(V2 — hors MVP)*

> ⚠️ **Cette entité n'appartient pas au périmètre V1.**

Journal financier du portefeuille.

```text
WalletTransaction
- id
- wallet_id
- transaction_number
- type
- direction
- amount
- currency
- balance_before
- balance_after
- reference_type
- reference_id
- status
- description
- created_at
```

## Types

```text
DEPOSIT
WITHDRAWAL
PAYMENT
REFUND
TRANSFER
REWARD
ADJUSTMENT
```

## Direction

```text
CREDIT
DEBIT
```

Le système doit garantir :

```text
balance_after =
    balance_before + CREDIT
    ou
    balance_before - DEBIT
```

---

# 22. LOYALTY_ACCOUNT *(V3 — hors MVP)*

> ⚠️ **Cette entité n'appartient pas au périmètre V1.**  
> Elle est documentée pour préparer l'architecture.  
> Ne pas implémenter dans le MVP.

Compte de fidélité du client.

```text
LoyaltyAccount
- id
- customer_id
- points_balance
- lifetime_points_earned
- lifetime_points_spent
- tier
- status
- created_at
- updated_at
```

## Tiers

Les niveaux doivent être configurables.

Exemple :

```text
STANDARD
SILVER
GOLD
PLATINUM
```

Le choix des seuils relève de la configuration métier.

---

# 23. LOYALTY_TRANSACTION *(V3 — hors MVP)*

> ⚠️ **Cette entité n'appartient pas au périmètre V1.**

Journal des mouvements de points.

```text
LoyaltyTransaction
- id
- loyalty_account_id
- transaction_number
- type
- points
- balance_before
- balance_after
- reference_type
- reference_id
- description
- created_at
```

## Types

```text
EARN
REDEEM
BONUS
EXPIRED
ADJUSTMENT
REVERSAL
```

Les points doivent être traités comme un ledger, de manière similaire aux transactions financières.

---

# 24. REWARD *(V3 — hors MVP)*

> ⚠️ **Cette entité n'appartient pas au périmètre V1.**

Récompense disponible.

```text
Reward
- id
- name
- description
- reward_type
- points_cost
- monetary_value
- currency
- quantity
- start_at
- end_at
- status
- created_at
- updated_at
```

## Types

```text
DISCOUNT
COUPON
CASHBACK
PRODUCT
SERVICE
VOUCHER
OTHER
```

---

# 25. REWARD_REDEMPTION *(V3 — hors MVP)*

> ⚠️ **Cette entité n'appartient pas au périmètre V1.**

Utilisation d'une récompense.

```text
RewardRedemption
- id
- customer_id
- reward_id
- loyalty_transaction_id
- redemption_number
- points_used
- status
- redeemed_at
- expires_at
- used_at
```

## Statuts

```text
CREATED
ACTIVE
USED
EXPIRED
CANCELLED
```

---

# 26. CAMPAIGN *(V3 — hors MVP)*

> ⚠️ **Cette entité n'appartient pas au périmètre V1.**  
> Elle est documentée pour préparer l'architecture.  
> Ne pas implémenter dans le MVP.

Campagne promotionnelle ou marketing.

```text
Campaign
- id
- name
- description
- campaign_type
- target_type
- start_at
- end_at
- budget
- status
- created_by
- created_at
- updated_at
```

Exemples :

```text
WELCOME
LOYALTY
SEASONAL
MERCHANT_PROMOTION
CASHBACK
REFERRAL
```

---

# 27. COUPON *(V3 — hors MVP)*

> ⚠️ **Cette entité n'appartient pas au périmètre V1.**

Coupon promotionnel.

```text
Coupon
- id
- campaign_id
- code
- discount_type
- discount_value
- minimum_order_amount
- maximum_discount_amount
- usage_limit
- usage_count
- start_at
- end_at
- status
```

## Discount type

```text
FIXED
PERCENTAGE
```

Le code coupon doit être unique.

---

# 28. COUPON_USAGE *(V3 — hors MVP)*

> ⚠️ **Cette entité n'appartient pas au périmètre V1.**

Historique d'utilisation.

```text
CouponUsage
- id
- coupon_id
- customer_id
- order_id
- discount_amount
- used_at
```

Une contrainte doit empêcher une utilisation interdite selon les règles de campagne.

---

# 29. NOTIFICATION

Notification envoyée ou à envoyer.

```text
Notification
- id
- user_id
- type
- channel
- title
- content
- status
- sent_at
- read_at
- created_at
```

## Channels

```text
PUSH
SMS
EMAIL
IN_APP
WHATSAPP
```

## Status

```text
PENDING
SENT
DELIVERED
READ
FAILED
```

---

# 30. NOTIFICATION_TEMPLATE

Templates centralisés.

```text
NotificationTemplate
- id
- code
- channel
- subject
- content_template
- language
- status
- created_at
- updated_at
```

Les messages ne doivent pas être dispersés dans le code source.

---

# 31. AUDIT_LOG

Toute opération sensible doit pouvoir être auditée.

```text
AuditLog
- id
- user_id
- action
- entity_type
- entity_id
- old_values
- new_values
- ip_address
- user_agent
- correlation_id
- created_at
```

## Actions

```text
CREATE
UPDATE
DELETE
LOGIN
LOGOUT
PASSWORD_CHANGE
ROLE_CHANGE
PAYMENT
REFUND
WALLET_ADJUSTMENT
LOYALTY_ADJUSTMENT
EXPORT
OTHER
```

Les logs d'audit ne doivent pas être modifiables par les utilisateurs standards.

---

# 32. FILE

Référence des documents et médias.

```text
File
- id
- original_name
- storage_key
- mime_type
- size
- checksum
- storage_provider
- uploaded_by
- created_at
```

Le fichier physique peut être stocké dans :

```text
Object Storage
S3 compatible storage
Local storage
Cloud storage
```

La base ne doit stocker que les métadonnées et la référence de stockage lorsque cela est pertinent.

---

# 33. WEBHOOK_EVENT

Permet de conserver les événements provenant des systèmes externes.

```text
WebhookEvent
- id
- provider
- external_event_id
- event_type
- payload
- signature
- status
- received_at
- processed_at
- error_message
```

`external_event_id` doit permettre l'idempotence.

---

# 34. CONFIGURATION

Paramètres métier configurables.

```text
Configuration
- id
- key
- value
- value_type
- environment
- description
- updated_by
- updated_at
```

Exemples :

```text
LOYALTY_POINTS_RATE
WELCOME_BONUS
MAX_WALLET_BALANCE
PAYMENT_TIMEOUT
COUPON_EXPIRATION
```

Les secrets ne doivent pas être stockés comme simples configurations.

---

# 35. Relations principales

## Client

```text
User 1 ─── 0..1 Customer

Customer 1 ─── N CustomerAddress

Customer 1 ─── N CustomerDevice

Customer 1 ─── 1 Wallet

Customer 1 ─── 1 LoyaltyAccount
```

## Commerce

```text
Merchant 1 ─── N MerchantOutlet

Merchant 1 ─── N MerchantUser

Merchant 1 ─── N Product

ProductCategory 1 ─── N Product
```

## Vente

```text
Customer 1 ─── N Order

Merchant 1 ─── N Order

Order 1 ─── N OrderItem

Product 1 ─── N OrderItem
```

## Paiement

```text
Order 1 ─── N Payment

PaymentProvider 1 ─── N Payment

Wallet 1 ─── N WalletTransaction
```

## Fidélité

```text
LoyaltyAccount 1 ─── N LoyaltyTransaction

Reward 1 ─── N RewardRedemption

Customer 1 ─── N RewardRedemption
```

---

# 36. Contraintes d'intégrité

Les contraintes suivantes sont obligatoires.

## Identifiants

Toutes les tables principales doivent avoir une clé primaire technique.

Les références métier doivent être uniques.

Exemples :

```text
customer_number UNIQUE
merchant_number UNIQUE
order_number UNIQUE
payment_number UNIQUE
transaction_number UNIQUE
coupon.code UNIQUE
```

---

# 37. Idempotence

Les opérations suivantes doivent être idempotentes :

```text
Payment
WebhookEvent
WalletTransaction
LoyaltyTransaction
RewardRedemption
```

Une même opération externe ne doit jamais générer deux crédits ou deux débits.

Exemple :

```text
provider = CINETPAY
external_transaction_id = ABC123
```

Si `ABC123` est reçu plusieurs fois :

```text
1er appel → traitement
2e appel → détection du doublon
3e appel → aucun nouveau mouvement financier
```

---

# 38. Transactions financières

Une opération financière doit suivre le principe :

```text
REQUEST
   ↓
VALIDATION
   ↓
TRANSACTION
   ↓
LEDGER
   ↓
BALANCE UPDATE
   ↓
AUDIT
   ↓
NOTIFICATION
```

Le système ne doit jamais :

```text
UPDATE wallet.balance
```

sans créer simultanément la transaction correspondante.

---

# 39. Historisation

Pour les données critiques, privilégier :

```text
status
created_at
updated_at
deleted_at
audit_log
```

plutôt qu'une suppression physique.

Les éléments financiers et d'audit ne doivent normalement jamais être supprimés.

---

# 40. Soft Delete

Lorsque nécessaire :

```text
deleted_at
```

sera utilisé.

Les requêtes métier doivent exclure par défaut les enregistrements supprimés logiquement.

---

# 41. Argent et précision

Tous les montants doivent utiliser un type décimal.

Exemple conceptuel :

```text
DECIMAL(19,4)
```

Ne jamais utiliser :

```text
FLOAT
DOUBLE
```

pour les montants financiers.

La devise doit être explicitement associée au montant.

Exemple :

```text
amount = 15000.00
currency = XOF
```

---

# 42. Dates et heures

Les timestamps doivent être stockés de manière cohérente, idéalement en UTC.

Les données d'affichage doivent être converties selon le fuseau de l'utilisateur ou de l'organisation.

Format logique :

```text
created_at
updated_at
completed_at
```

avec précision suffisante pour les transactions.

---

# 43. Données personnelles

Les informations suivantes sont considérées comme sensibles ou personnelles selon leur usage :

```text
nom
prénom
email
téléphone
adresse
géolocalisation
identifiant appareil
IP
données de paiement
```

Elles doivent être :

* protégées ;
* limitées aux utilisateurs autorisés ;
* non exposées inutilement par les APIs ;
* journalisées avec prudence ;
* supprimées ou anonymisées lorsque les règles applicables l'exigent.

---

# 44. Indexation

Des index doivent être prévus sur les colonnes fréquemment recherchées.

Exemples :

```text
User.email
User.phone_number

Customer.customer_number
Customer.phone_number

Merchant.merchant_number

Order.order_number
Order.customer_id
Order.merchant_id
Order.created_at
Order.status

Payment.payment_number
Payment.external_transaction_id
Payment.status

Wallet.wallet_number
WalletTransaction.transaction_number

LoyaltyAccount.customer_id

Notification.user_id
Notification.status

AuditLog.user_id
AuditLog.entity_type
AuditLog.entity_id
AuditLog.created_at
```

Les index doivent être validés avec les requêtes réelles et ne doivent pas être ajoutés systématiquement sans justification.

---

# 45. Multi-tenant / séparation des données

Si MAGIC GOLD évolue vers une architecture multi-tenant, le modèle doit pouvoir supporter :

```text
Tenant
   │
   ├── Users
   ├── Merchants
   ├── Customers
   ├── Orders
   ├── Payments
   └── Configuration
```

Le modèle actuel doit donc éviter les choix qui empêcheraient ultérieurement l'introduction d'un `tenant_id`.

Toute décision définitive de multi-tenancy doit être documentée dans l'architecture technique.

---

# 46. États métier

Les statuts constituent une machine d'état.

Exemple commande :

```text
CREATED
   ↓
CONFIRMED
   ↓
PROCESSING
   ↓
READY
   ↓
COMPLETED
```

Avec sorties possibles :

```text
CREATED ─────→ CANCELLED

CONFIRMED ───→ CANCELLED

PROCESSING ──→ CANCELLED

COMPLETED ───→ REFUNDED
```

Les transitions interdites doivent être rejetées côté backend.

---

# 47. Règle de source de vérité

Pour chaque information, une seule source de vérité doit être définie.

Exemples :

```text
Wallet balance
→ Wallet + WalletTransaction

Loyalty points
→ LoyaltyAccount + LoyaltyTransaction

Order total
→ Order + OrderItem

Payment state
→ Payment

User identity
→ Identity Provider
```

Le frontend ne constitue jamais une source de vérité métier.

---

# 48. Évolution du schéma

Toute évolution de base de données doit être réalisée par migration versionnée.

Exemple :

```text
V001__initial_schema.sql
V002__create_wallet.sql
V003__create_loyalty.sql
V004__add_campaigns.sql
```

Les migrations doivent être :

* versionnées ;
* reproductibles ;
* testées ;
* compatibles avec le processus CI/CD.

---

# 49. Règles pour Hermes

Hermes doit respecter les règles suivantes lors de l'implémentation :

1. Ne pas créer une nouvelle entité métier déjà couverte par ce document sans justification.
2. Ne pas supprimer une entité définie ici sans validation.
3. Ne pas modifier une relation critique sans mettre à jour ce document.
4. Ne pas déplacer une règle financière vers le frontend.
5. Ne pas utiliser `float` ou `double` pour l'argent.
6. Implémenter l'idempotence des paiements et webhooks.
7. Préserver les historiques financiers.
8. Ajouter les contraintes SQL nécessaires.
9. Ajouter les index justifiés par les requêtes.
10. Utiliser des migrations versionnées.
11. Ajouter les tests correspondant aux contraintes métier.
12. Toute divergence entre le code et ce document doit être documentée.

---

# 50. Modèle conceptuel simplifié

```text
                         USER
                          │
                    ┌─────┴─────┐
                    │           │
                 ROLE        CUSTOMER
                                │
             ┌──────────────────┼─────────────────┐
             │                  │                 │
          ADDRESS            DEVICE            WALLET
                                                  │
                                           WALLET_TRANSACTION
             │
             │
             ▼
           ORDER
             │
       ┌─────┴──────┐
       │            │
   ORDER_ITEM     PAYMENT
       │            │
       ▼            ▼
    PRODUCT    PAYMENT_PROVIDER
       │
       ▼
    MERCHANT
       │
   ┌───┴───────────┐
   │               │
OUTLET        MERCHANT_USER


CUSTOMER
   │
   ▼
LOYALTY_ACCOUNT
   │
   ▼
LOYALTY_TRANSACTION
   │
   ▼
REWARD_REDEMPTION
   │
   ▼
REWARD


CAMPAIGN
   │
   ▼
COUPON
   │
   ▼
COUPON_USAGE


USER
 │
 ▼
NOTIFICATION


USER
 │
 ▼
AUDIT_LOG
```

---

# 51. Priorités d'implémentation

## Phase 1 — Core (V1 — MVP)

```text
User
Role
UserRole
Customer
Merchant
MerchantOutlet
MerchantUser
Product
ProductCategory
Order
OrderItem
OrderStatusHistory
Payment
PaymentProvider
WebhookEvent
Register
CashSession
CashMovement
StockMovement
Notification
NotificationTemplate
AuditLog
Configuration
File
```

## Phase 2 — Commerce étendu (V2)

```text
CustomerAddress
CustomerDevice
Wallet
WalletTransaction
Supplier
Purchase
PurchaseItem
Expense
Credit
CreditPayment
Inventory
InventoryItem
Transfer
Refund (entité séparée)
Settlement
Reconciliation
LedgerEntry
MerchantOnboarding
```

## Phase 3 — Fidélité et marketing (V3)

```text
LoyaltyAccount
LoyaltyTransaction
Reward
RewardRedemption
Campaign
Coupon
CouponUsage
```

---

# 52. Critères de validation

Le modèle sera considéré comme valide lorsque :

* toutes les entités principales sont représentées ;
* toutes les relations principales sont définies ;
* les clés primaires sont définies ;
* les contraintes d'unicité sont définies ;
* les statuts sont documentés ;
* les opérations financières sont idempotentes ;
* les montants utilisent une précision décimale ;
* les opérations critiques sont auditables ;
* les migrations peuvent être versionnées ;
* les APIs peuvent être dérivées du modèle ;
* les tests peuvent vérifier les règles métier.

---

# 53. Documents liés

Ce document doit être utilisé conjointement avec :

```text
00_ProjectVision.md
01_PRD.md
02_FunctionalSpecification.md
03_TechnicalArchitecture.md
05_API_Specification.md
06_SecurityArchitecture.md
07_UI_UX_Specification.md
15_Roles_Permissions_Audit.md
18_TestStrategy.md
20_Data_Protection_Compliance.md
```

---

# 54. Règle finale

Le modèle de données est une **source de vérité fonctionnelle et logique**.

Toute implémentation de MAGIC GOLD doit partir de ce modèle et conserver une cohérence entre :

```text
PRD
  ↓
Functional Specification
  ↓
Data Model
  ↓
API
  ↓
Backend
  ↓
Frontend / Mobile
  ↓
Tests
  ↓
Production
```

Aucune couche ne doit introduire silencieusement une règle métier contradictoire avec ce modèle.
# 55. Dictionnaire de données

Ce dictionnaire complète le modèle conceptuel et logique défini dans les sections précédentes.

Il constitue la référence pour :

* les tables SQL ;
* les entités JPA/Hibernate ;
* les DTO ;
* les APIs REST ;
* les validations backend ;
* les formulaires frontend/mobile ;
* les migrations ;
* les tests ;
* la documentation technique.

## 55.1 Conventions

| Convention | Signification                            |
| ---------- | ---------------------------------------- |
| PK         | Clé primaire                             |
| FK         | Clé étrangère                            |
| NN         | NOT NULL / obligatoire                   |
| UQ         | UNIQUE                                   |
| IDX        | Index recommandé                         |
| ENUM       | Valeur appartenant à une liste contrôlée |
| DECIMAL    | Nombre décimal précis                    |
| TIMESTAMP  | Date + heure                             |
| JSON       | Données structurées                      |
| TEXT       | Texte libre                              |

---

# 56. USER

| Champ         | Type         | Contraintes | Description                                  |
| ------------- | ------------ | ----------- | -------------------------------------------- |
| id            | UUID         | PK, NN      | Identifiant technique                        |
| external_id   | VARCHAR(150) | UQ          | Identifiant provenant de l'Identity Provider |
| username      | VARCHAR(100) | UQ          | Nom de connexion                             |
| email         | VARCHAR(255) | UQ          | Adresse email                                |
| phone_number  | VARCHAR(30)  | IDX         | Numéro de téléphone                          |
| password_hash | VARCHAR(255) | NULL        | Hash local si authentification locale        |
| first_name    | VARCHAR(100) | NN          | Prénom                                       |
| last_name     | VARCHAR(100) | NN          | Nom                                          |
| status        | ENUM         | NN          | État du compte                               |
| last_login_at | TIMESTAMP    | NULL        | Dernière connexion                           |
| created_at    | TIMESTAMP    | NN          | Date de création                             |
| updated_at    | TIMESTAMP    | NN          | Dernière modification                        |
| deleted_at    | TIMESTAMP    | NULL        | Suppression logique                          |

### Valeurs `status`

```text
ACTIVE
INACTIVE
LOCKED
SUSPENDED
PENDING
```

---

# 57. ROLE

| Champ       | Type         | Contraintes | Description    |
| ----------- | ------------ | ----------- | -------------- |
| id          | UUID         | PK          | Identifiant    |
| code        | VARCHAR(50)  | UQ, NN      | Code technique |
| name        | VARCHAR(100) | NN          | Nom du rôle    |
| description | TEXT         | NULL        | Description    |
| status      | ENUM         | NN          | État           |
| created_at  | TIMESTAMP    | NN          | Création       |
| updated_at  | TIMESTAMP    | NN          | Modification   |

### Valeurs initiales

```text
SUPER_ADMIN
ADMIN
CUSTOMER
MERCHANT
MERCHANT_MANAGER
MERCHANT_OPERATOR
SUPPORT
FINANCE
AUDITOR
```

---

# 58. USER_ROLE

| Champ       | Type      | Contraintes | Description                        |
| ----------- | --------- | ----------- | ---------------------------------- |
| id          | UUID      | PK          | Identifiant                        |
| user_id     | UUID      | FK, NN      | Utilisateur                        |
| role_id     | UUID      | FK, NN      | Rôle                               |
| assigned_at | TIMESTAMP | NN          | Date d'attribution                 |
| assigned_by | UUID      | FK, NULL    | Utilisateur ayant attribué le rôle |

### Contrainte

```text
UNIQUE(user_id, role_id)
```

---

# 59. CUSTOMER

| Champ               | Type         | Contraintes | Description              |
| ------------------- | ------------ | ----------- | ------------------------ |
| id                  | UUID         | PK          | Identifiant technique    |
| user_id             | UUID         | FK, UQ, NN  | Compte utilisateur       |
| customer_number     | VARCHAR(30)  | UQ, NN, IDX | Référence client         |
| first_name          | VARCHAR(100) | NN          | Prénom                   |
| last_name           | VARCHAR(100) | NN          | Nom                      |
| phone_number        | VARCHAR(30)  | IDX         | Téléphone                |
| email               | VARCHAR(255) | IDX         | Email                    |
| date_of_birth       | DATE         | NULL        | Date de naissance        |
| gender              | VARCHAR(30)  | NULL        | Genre si nécessaire      |
| status              | ENUM         | NN          | État du client           |
| registration_source | VARCHAR(50)  | NULL        | Origine de l'inscription |
| created_at          | TIMESTAMP    | NN          | Création                 |
| updated_at          | TIMESTAMP    | NN          | Modification             |
| deleted_at          | TIMESTAMP    | NULL        | Suppression logique      |

### Exemple

```text
customer_number = MG-CUS-000001
```

---

# 60. CUSTOMER_ADDRESS

| Champ        | Type          | Contraintes | Description        |
| ------------ | ------------- | ----------- | ------------------ |
| id           | UUID          | PK          | Identifiant        |
| customer_id  | UUID          | FK, NN, IDX | Client             |
| label        | VARCHAR(50)   | NN          | Libellé            |
| address_line | VARCHAR(255)  | NN          | Adresse            |
| city         | VARCHAR(100)  | NN          | Ville              |
| district     | VARCHAR(100)  | NULL        | District/zone      |
| country      | VARCHAR(100)  | NN          | Pays               |
| latitude     | DECIMAL(10,7) | NULL        | Latitude           |
| longitude    | DECIMAL(10,7) | NULL        | Longitude          |
| is_default   | BOOLEAN       | NN          | Adresse principale |
| created_at   | TIMESTAMP     | NN          | Création           |
| updated_at   | TIMESTAMP     | NN          | Modification       |

---

# 61. CUSTOMER_DEVICE

| Champ             | Type         | Contraintes | Description          |
| ----------------- | ------------ | ----------- | -------------------- |
| id                | UUID         | PK          | Identifiant          |
| customer_id       | UUID         | FK, NN, IDX | Client               |
| device_identifier | VARCHAR(255) | NN          | Identifiant appareil |
| platform          | ENUM         | NN          | Plateforme           |
| app_version       | VARCHAR(30)  | NULL        | Version application  |
| push_token        | TEXT         | NULL        | Token notification   |
| last_seen_at      | TIMESTAMP    | NULL        | Dernière activité    |
| status            | ENUM         | NN          | État appareil        |
| created_at        | TIMESTAMP    | NN          | Création             |
| updated_at        | TIMESTAMP    | NN          | Modification         |

### Platform

```text
ANDROID
IOS
WEB
OTHER
```

---

# 62. MERCHANT

| Champ               | Type         | Contraintes | Description              |
| ------------------- | ------------ | ----------- | ------------------------ |
| id                  | UUID         | PK          | Identifiant              |
| merchant_number     | VARCHAR(30)  | UQ, NN, IDX | Référence commerçant     |
| legal_name          | VARCHAR(255) | NN          | Raison sociale           |
| commercial_name     | VARCHAR(255) | NN          | Nom commercial           |
| business_type       | VARCHAR(100) | NULL        | Type d'activité          |
| registration_number | VARCHAR(100) | UQ, NULL    | Numéro d'immatriculation |
| tax_number          | VARCHAR(100) | UQ, NULL    | Numéro fiscal            |
| phone_number        | VARCHAR(30)  | IDX         | Téléphone                |
| email               | VARCHAR(255) | NULL        | Email                    |
| address             | VARCHAR(255) | NULL        | Adresse                  |
| city                | VARCHAR(100) | NULL        | Ville                    |
| country             | VARCHAR(100) | NN          | Pays                     |
| logo_file_id        | UUID         | FK, NULL    | Logo                     |
| status              | ENUM         | NN          | État commerçant          |
| created_at          | TIMESTAMP    | NN          | Création                 |
| updated_at          | TIMESTAMP    | NN          | Modification             |

### Status

```text
PENDING
ACTIVE
SUSPENDED
BLOCKED
CLOSED
```

---

# 63. MERCHANT_OUTLET

| Champ         | Type          | Contraintes | Description              |
| ------------- | ------------- | ----------- | ------------------------ |
| id            | UUID          | PK          | Identifiant              |
| merchant_id   | UUID          | FK, NN, IDX | Commerçant               |
| outlet_number | VARCHAR(30)   | UQ, NN      | Référence point de vente |
| name          | VARCHAR(150)  | NN          | Nom du point de vente    |
| address       | VARCHAR(255)  | NN          | Adresse                  |
| city          | VARCHAR(100)  | NN          | Ville                    |
| district      | VARCHAR(100)  | NULL        | Zone                     |
| latitude      | DECIMAL(10,7) | NULL        | Latitude                 |
| longitude     | DECIMAL(10,7) | NULL        | Longitude                |
| phone_number  | VARCHAR(30)   | NULL        | Téléphone                |
| status        | ENUM          | NN          | État                     |
| created_at    | TIMESTAMP     | NN          | Création                 |
| updated_at    | TIMESTAMP     | NN          | Modification             |

---

# 64. MERCHANT_USER

| Champ       | Type      | Contraintes | Description    |
| ----------- | --------- | ----------- | -------------- |
| id          | UUID      | PK          | Identifiant    |
| merchant_id | UUID      | FK, NN      | Commerçant     |
| user_id     | UUID      | FK, NN      | Utilisateur    |
| outlet_id   | UUID      | FK, NULL    | Point de vente |
| role        | ENUM      | NN          | Fonction       |
| status      | ENUM      | NN          | État           |
| created_at  | TIMESTAMP | NN          | Création       |
| updated_at  | TIMESTAMP | NN          | Modification   |

### Rôles

```text
MANAGER
OPERATOR
CASHIER
```

---

# 65. PRODUCT_CATEGORY

| Champ       | Type         | Contraintes | Description       |
| ----------- | ------------ | ----------- | ----------------- |
| id          | UUID         | PK          | Identifiant       |
| parent_id   | UUID         | FK, NULL    | Catégorie parente |
| name        | VARCHAR(150) | NN          | Nom               |
| description | TEXT         | NULL        | Description       |
| status      | ENUM         | NN          | État              |
| created_at  | TIMESTAMP    | NN          | Création          |
| updated_at  | TIMESTAMP    | NN          | Modification      |

---

# 66. PRODUCT

| Champ          | Type          | Contraintes       | Description       |
| -------------- | ------------- | ----------------- | ----------------- |
| id             | UUID          | PK                | Identifiant       |
| merchant_id    | UUID          | FK, NN, IDX       | Commerçant        |
| category_id    | UUID          | FK, NULL          | Catégorie         |
| sku            | VARCHAR(100)  | UQ par commerçant | Référence produit |
| name           | VARCHAR(255)  | NN                | Nom               |
| description    | TEXT          | NULL              | Description       |
| unit_price     | DECIMAL(19,4) | NN                | Prix unitaire     |
| currency       | CHAR(3)       | NN                | Devise            |
| stock_quantity | DECIMAL(19,4) | NULL              | Stock             |
| status         | ENUM          | NN                | État              |
| image_file_id  | UUID          | FK, NULL          | Image             |
| created_at     | TIMESTAMP     | NN                | Création          |
| updated_at     | TIMESTAMP     | NN                | Modification      |

### Contrainte

```text
UNIQUE(merchant_id, sku)
```

---

# 67. ORDER

| Champ           | Type          | Contraintes | Description     |
| --------------- | ------------- | ----------- | --------------- |
| id              | UUID          | PK          | Identifiant     |
| order_number    | VARCHAR(40)   | UQ, NN, IDX | Numéro commande |
| customer_id     | UUID          | FK, NN, IDX | Client          |
| merchant_id     | UUID          | FK, NN, IDX | Commerçant      |
| outlet_id       | UUID          | FK, NULL    | Point de vente  |
| subtotal        | DECIMAL(19,4) | NN          | Sous-total      |
| discount_amount | DECIMAL(19,4) | NN          | Remise          |
| tax_amount      | DECIMAL(19,4) | NN          | Taxes           |
| total_amount    | DECIMAL(19,4) | NN          | Total           |
| currency        | CHAR(3)       | NN          | Devise          |
| status          | ENUM          | NN          | État commande   |
| payment_status  | ENUM          | NN          | État paiement   |
| created_at      | TIMESTAMP     | NN, IDX     | Création        |
| updated_at      | TIMESTAMP     | NN          | Modification    |

---

# 68. ORDER_ITEM

| Champ                 | Type          | Contraintes | Description               |
| --------------------- | ------------- | ----------- | ------------------------- |
| id                    | UUID          | PK          | Identifiant               |
| order_id              | UUID          | FK, NN, IDX | Commande                  |
| product_id            | UUID          | FK, NN      | Produit                   |
| product_name_snapshot | VARCHAR(255)  | NN          | Nom au moment de l'achat  |
| quantity              | DECIMAL(19,4) | NN          | Quantité                  |
| unit_price            | DECIMAL(19,4) | NN          | Prix au moment de l'achat |
| discount_amount       | DECIMAL(19,4) | NN          | Remise                    |
| total_amount          | DECIMAL(19,4) | NN          | Total ligne               |

---

# 69. PAYMENT

| Champ                   | Type          | Contraintes | Description        |
| ----------------------- | ------------- | ----------- | ------------------ |
| id                      | UUID          | PK          | Identifiant        |
| payment_number          | VARCHAR(40)   | UQ, NN      | Référence paiement |
| order_id                | UUID          | FK, NN, IDX | Commande           |
| customer_id             | UUID          | FK, NN      | Client             |
| provider_id             | UUID          | FK, NN      | Prestataire        |
| external_transaction_id | VARCHAR(255)  | IDX         | ID externe         |
| idempotency_key         | VARCHAR(255)  | UQ          | Clé idempotence    |
| amount                  | DECIMAL(19,4) | NN          | Montant            |
| currency                | CHAR(3)       | NN          | Devise             |
| payment_method          | ENUM          | NN          | Moyen paiement     |
| status                  | ENUM          | NN, IDX     | Statut             |
| initiated_at            | TIMESTAMP     | NN          | Initiation         |
| completed_at            | TIMESTAMP     | NULL        | Succès             |
| failed_at               | TIMESTAMP     | NULL        | Échec              |
| failure_reason          | TEXT          | NULL        | Motif échec        |
| metadata                | JSON          | NULL        | Métadonnées        |

### Règle

`external_transaction_id` doit être unique par prestataire lorsque le prestataire garantit cette unicité.

---

# 70. PAYMENT_PROVIDER

| Champ                   | Type         | Contraintes | Description             |
| ----------------------- | ------------ | ----------- | ----------------------- |
| id                      | UUID         | PK          | Identifiant             |
| code                    | VARCHAR(50)  | UQ, NN      | Code                    |
| name                    | VARCHAR(100) | NN          | Nom                     |
| type                    | VARCHAR(50)  | NN          | Type fournisseur        |
| status                  | ENUM         | NN          | État                    |
| configuration_reference | VARCHAR(255) | NULL        | Référence configuration |
| created_at              | TIMESTAMP    | NN          | Création                |
| updated_at              | TIMESTAMP    | NN          | Modification            |

Les secrets, clés privées et mots de passe ne doivent pas être stockés directement dans cette table.

---

# 71. WALLET

| Champ             | Type          | Contraintes | Description         |
| ----------------- | ------------- | ----------- | ------------------- |
| id                | UUID          | PK          | Identifiant         |
| customer_id       | UUID          | FK, UQ, NN  | Propriétaire        |
| wallet_number     | VARCHAR(40)   | UQ, NN      | Numéro portefeuille |
| currency          | CHAR(3)       | NN          | Devise              |
| available_balance | DECIMAL(19,4) | NN          | Solde disponible    |
| pending_balance   | DECIMAL(19,4) | NN          | Solde en attente    |
| status            | ENUM          | NN          | État                |
| created_at        | TIMESTAMP     | NN          | Création            |
| updated_at        | TIMESTAMP     | NN          | Modification        |

### Règle critique

Le solde est une donnée contrôlée par le backend.

Aucune API frontend ne doit pouvoir modifier directement `available_balance`.

---

# 72. WALLET_TRANSACTION

| Champ              | Type          | Contraintes | Description    |
| ------------------ | ------------- | ----------- | -------------- |
| id                 | UUID          | PK          | Identifiant    |
| wallet_id          | UUID          | FK, NN, IDX | Portefeuille   |
| transaction_number | VARCHAR(50)   | UQ, NN      | Référence      |
| type               | ENUM          | NN          | Type mouvement |
| direction          | ENUM          | NN          | CREDIT/DEBIT   |
| amount             | DECIMAL(19,4) | NN          | Montant        |
| currency           | CHAR(3)       | NN          | Devise         |
| balance_before     | DECIMAL(19,4) | NN          | Solde avant    |
| balance_after      | DECIMAL(19,4) | NN          | Solde après    |
| reference_type     | VARCHAR(50)   | NULL        | Type référence |
| reference_id       | UUID          | NULL        | ID référence   |
| status             | ENUM          | NN          | État           |
| description        | TEXT          | NULL        | Description    |
| created_at         | TIMESTAMP     | NN, IDX     | Date           |

---

# 73. LOYALTY_ACCOUNT

| Champ                  | Type        | Contraintes | Description     |
| ---------------------- | ----------- | ----------- | --------------- |
| id                     | UUID        | PK          | Identifiant     |
| customer_id            | UUID        | FK, UQ, NN  | Client          |
| points_balance         | BIGINT      | NN          | Solde points    |
| lifetime_points_earned | BIGINT      | NN          | Total gagné     |
| lifetime_points_spent  | BIGINT      | NN          | Total utilisé   |
| tier                   | VARCHAR(50) | NN          | Niveau fidélité |
| status                 | ENUM        | NN          | État            |
| created_at             | TIMESTAMP   | NN          | Création        |
| updated_at             | TIMESTAMP   | NN          | Modification    |

Les points doivent être des nombres entiers sauf règle métier contraire.

---

# 74. LOYALTY_TRANSACTION

| Champ              | Type        | Contraintes | Description      |
| ------------------ | ----------- | ----------- | ---------------- |
| id                 | UUID        | PK          | Identifiant      |
| loyalty_account_id | UUID        | FK, NN, IDX | Compte fidélité  |
| transaction_number | VARCHAR(50) | UQ, NN      | Référence        |
| type               | ENUM        | NN          | Type             |
| points             | BIGINT      | NN          | Nombre de points |
| balance_before     | BIGINT      | NN          | Solde avant      |
| balance_after      | BIGINT      | NN          | Solde après      |
| reference_type     | VARCHAR(50) | NULL        | Type opération   |
| reference_id       | UUID        | NULL        | ID opération     |
| description        | TEXT        | NULL        | Description      |
| created_at         | TIMESTAMP   | NN, IDX     | Date             |

---

# 75. REWARD

| Champ          | Type          | Contraintes | Description         |
| -------------- | ------------- | ----------- | ------------------- |
| id             | UUID          | PK          | Identifiant         |
| name           | VARCHAR(150)  | NN          | Nom                 |
| description    | TEXT          | NULL        | Description         |
| reward_type    | ENUM          | NN          | Type                |
| points_cost    | BIGINT        | NN          | Coût en points      |
| monetary_value | DECIMAL(19,4) | NULL        | Valeur monétaire    |
| currency       | CHAR(3)       | NULL        | Devise              |
| quantity       | INTEGER       | NULL        | Quantité disponible |
| start_at       | TIMESTAMP     | NULL        | Début               |
| end_at         | TIMESTAMP     | NULL        | Fin                 |
| status         | ENUM          | NN          | État                |
| created_at     | TIMESTAMP     | NN          | Création            |
| updated_at     | TIMESTAMP     | NN          | Modification        |

---

# 76. REWARD_REDEMPTION

| Champ                  | Type        | Contraintes | Description        |
| ---------------------- | ----------- | ----------- | ------------------ |
| id                     | UUID        | PK          | Identifiant        |
| customer_id            | UUID        | FK, NN      | Client             |
| reward_id              | UUID        | FK, NN      | Récompense         |
| loyalty_transaction_id | UUID        | FK, NN      | Transaction points |
| redemption_number      | VARCHAR(50) | UQ, NN      | Référence          |
| points_used            | BIGINT      | NN          | Points utilisés    |
| status                 | ENUM        | NN          | État               |
| redeemed_at            | TIMESTAMP   | NN          | Date utilisation   |
| expires_at             | TIMESTAMP   | NULL        | Expiration         |
| used_at                | TIMESTAMP   | NULL        | Date consommation  |

---

# 77. CAMPAIGN

| Champ         | Type          | Contraintes | Description       |
| ------------- | ------------- | ----------- | ----------------- |
| id            | UUID          | PK          | Identifiant       |
| name          | VARCHAR(150)  | NN          | Nom               |
| description   | TEXT          | NULL        | Description       |
| campaign_type | ENUM          | NN          | Type              |
| target_type   | VARCHAR(50)   | NN          | Population ciblée |
| start_at      | TIMESTAMP     | NN          | Début             |
| end_at        | TIMESTAMP     | NN          | Fin               |
| budget        | DECIMAL(19,4) | NULL        | Budget            |
| status        | ENUM          | NN          | État              |
| created_by    | UUID          | FK, NN      | Créateur          |
| created_at    | TIMESTAMP     | NN          | Création          |
| updated_at    | TIMESTAMP     | NN          | Modification      |

---

# 78. COUPON

| Champ                   | Type          | Contraintes | Description      |
| ----------------------- | ------------- | ----------- | ---------------- |
| id                      | UUID          | PK          | Identifiant      |
| campaign_id             | UUID          | FK, NULL    | Campagne         |
| code                    | VARCHAR(100)  | UQ, NN      | Code coupon      |
| discount_type           | ENUM          | NN          | Type réduction   |
| discount_value          | DECIMAL(19,4) | NN          | Valeur réduction |
| minimum_order_amount    | DECIMAL(19,4) | NULL        | Minimum commande |
| maximum_discount_amount | DECIMAL(19,4) | NULL        | Plafond          |
| usage_limit             | INTEGER       | NULL        | Nombre maximal   |
| usage_count             | INTEGER       | NN          | Nombre utilisé   |
| start_at                | TIMESTAMP     | NN          | Début            |
| end_at                  | TIMESTAMP     | NN          | Fin              |
| status                  | ENUM          | NN          | État             |

---

# 79. COUPON_USAGE

| Champ           | Type          | Contraintes | Description      |
| --------------- | ------------- | ----------- | ---------------- |
| id              | UUID          | PK          | Identifiant      |
| coupon_id       | UUID          | FK, NN      | Coupon           |
| customer_id     | UUID          | FK, NN      | Client           |
| order_id        | UUID          | FK, NN      | Commande         |
| discount_amount | DECIMAL(19,4) | NN          | Réduction        |
| used_at         | TIMESTAMP     | NN          | Date utilisation |

---

# 80. NOTIFICATION

| Champ      | Type         | Contraintes | Description       |
| ---------- | ------------ | ----------- | ----------------- |
| id         | UUID         | PK          | Identifiant       |
| user_id    | UUID         | FK, NN, IDX | Destinataire      |
| type       | VARCHAR(50)  | NN          | Type notification |
| channel    | ENUM         | NN          | Canal             |
| title      | VARCHAR(255) | NN          | Titre             |
| content    | TEXT         | NN          | Contenu           |
| status     | ENUM         | NN          | État              |
| sent_at    | TIMESTAMP    | NULL        | Envoi             |
| read_at    | TIMESTAMP    | NULL        | Lecture           |
| created_at | TIMESTAMP    | NN          | Création          |

---

# 81. NOTIFICATION_TEMPLATE

| Champ            | Type         | Contraintes | Description   |
| ---------------- | ------------ | ----------- | ------------- |
| id               | UUID         | PK          | Identifiant   |
| code             | VARCHAR(100) | UQ, NN      | Code template |
| channel          | ENUM         | NN          | Canal         |
| subject          | VARCHAR(255) | NULL        | Sujet         |
| content_template | TEXT         | NN          | Modèle        |
| language         | VARCHAR(10)  | NN          | Langue        |
| status           | ENUM         | NN          | État          |
| created_at       | TIMESTAMP    | NN          | Création      |
| updated_at       | TIMESTAMP    | NN          | Modification  |

---

# 82. AUDIT_LOG

| Champ          | Type         | Contraintes   | Description         |
| -------------- | ------------ | ------------- | ------------------- |
| id             | UUID         | PK            | Identifiant         |
| user_id        | UUID         | FK, NULL, IDX | Auteur              |
| action         | ENUM         | NN            | Action              |
| entity_type    | VARCHAR(100) | NN            | Type entité         |
| entity_id      | UUID         | NULL, IDX     | ID entité           |
| old_values     | JSON         | NULL          | Anciennes valeurs   |
| new_values     | JSON         | NULL          | Nouvelles valeurs   |
| ip_address     | VARCHAR(45)  | NULL          | IP                  |
| user_agent     | TEXT         | NULL          | Navigateur/appareil |
| correlation_id | VARCHAR(100) | NULL, IDX     | Traçabilité requête |
| created_at     | TIMESTAMP    | NN, IDX       | Date                |

---

# 83. FILE

| Champ            | Type         | Contraintes | Description   |
| ---------------- | ------------ | ----------- | ------------- |
| id               | UUID         | PK          | Identifiant   |
| original_name    | VARCHAR(255) | NN          | Nom original  |
| storage_key      | VARCHAR(500) | UQ, NN      | Clé stockage  |
| mime_type        | VARCHAR(100) | NN          | Type MIME     |
| size             | BIGINT       | NN          | Taille octets |
| checksum         | VARCHAR(128) | NULL        | Empreinte     |
| storage_provider | VARCHAR(50)  | NN          | Fournisseur   |
| uploaded_by      | UUID         | FK, NN      | Auteur        |
| created_at       | TIMESTAMP    | NN          | Date          |

---

# 84. WEBHOOK_EVENT

| Champ             | Type         | Contraintes | Description     |
| ----------------- | ------------ | ----------- | --------------- |
| id                | UUID         | PK          | Identifiant     |
| provider          | VARCHAR(100) | NN          | Fournisseur     |
| external_event_id | VARCHAR(255) | NN          | ID externe      |
| event_type        | VARCHAR(100) | NN          | Type événement  |
| payload           | JSON         | NN          | Payload reçu    |
| signature         | TEXT         | NULL        | Signature       |
| status            | ENUM         | NN          | État traitement |
| received_at       | TIMESTAMP    | NN          | Réception       |
| processed_at      | TIMESTAMP    | NULL        | Traitement      |
| error_message     | TEXT         | NULL        | Erreur          |

### Contrainte

```text
UNIQUE(provider, external_event_id)
```

---

# 85. CONFIGURATION

| Champ       | Type         | Contraintes | Description       |
| ----------- | ------------ | ----------- | ----------------- |
| id          | UUID         | PK          | Identifiant       |
| key         | VARCHAR(150) | UQ, NN      | Clé configuration |
| value       | TEXT         | NN          | Valeur            |
| value_type  | ENUM         | NN          | Type              |
| environment | VARCHAR(30)  | NN          | Environnement     |
| description | TEXT         | NULL        | Description       |
| updated_by  | UUID         | FK, NULL    | Modificateur      |
| updated_at  | TIMESTAMP    | NN          | Modification      |

### Value type

```text
STRING
INTEGER
DECIMAL
BOOLEAN
JSON
```

---

# 86. Tableau des identifiants métier

| Entité             | Référence     |
| ------------------ | ------------- |
| Customer           | MG-CUS-XXXXXX |
| Merchant           | MG-MER-XXXXXX |
| Outlet             | MG-OUT-XXXXXX |
| Order              | MG-ORD-XXXXXX |
| Payment            | MG-PAY-XXXXXX |
| Wallet             | MG-WAL-XXXXXX |
| WalletTransaction  | MG-WTX-XXXXXX |
| LoyaltyTransaction | MG-LTX-XXXXXX |
| RewardRedemption   | MG-RED-XXXXXX |

Les formats exacts peuvent évoluer, mais chaque référence métier doit être :

* unique ;
* lisible ;
* non utilisée comme clé primaire ;
* générée côté backend ;
* non prévisible lorsqu'une contrainte de sécurité l'exige.

---

# 87. Valeurs monétaires

La devise par défaut du marché initial doit être explicitement définie par la configuration du projet.

Pour la Côte d'Ivoire :

```text
currency = XOF
```

Le système doit néanmoins conserver le champ `currency` afin de permettre une évolution future.

### Règles

```text
amount >= 0
discount_amount >= 0
tax_amount >= 0
unit_price >= 0
```

Les opérations de crédit/débit sont déterminées par le type et la direction de transaction.

---

# 88. Règles de validation essentielles

## Customer

```text
phone_number → format valide
email → format valide si présent
customer_number → unique
```

## Merchant

```text
merchant_number → unique
registration_number → unique lorsqu'il est renseigné
```

## Product

```text
unit_price >= 0
quantity >= 0
merchant_id obligatoire
sku unique par merchant
```

## Order

```text
subtotal >= 0
discount_amount >= 0
tax_amount >= 0
total_amount >= 0
```

Le backend doit recalculer les montants et ne doit pas faire confiance au total envoyé par le frontend.

## Payment

```text
amount > 0
currency obligatoire
payment_number unique
idempotency_key unique
```

## Wallet

```text
available_balance >= 0
pending_balance >= 0
```

sauf si un découvert autorisé est explicitement introduit par une future règle métier.

## Loyalty

```text
points_balance >= 0
points_used > 0
```

---

# 89. Champs techniques communs

Les entités persistantes doivent utiliser autant que possible :

```text
id
created_at
updated_at
```

Les entités nécessitant une suppression logique peuvent également utiliser :

```text
deleted_at
```

Pour les opérations critiques :

```text
created_by
updated_by
```

peuvent être ajoutés lorsque cela apporte une valeur d'audit réelle.

---

# 90. Règles de nommage SQL

Utiliser :

```text
snake_case
```

Exemple :

```text
customer_id
created_at
payment_status
external_transaction_id
```

Les tables doivent être nommées au singulier ou au pluriel selon la convention retenue par le projet, mais une seule convention doit être utilisée partout.

Recommandation :

```text
users
customers
merchants
orders
payments
wallets
```

Les noms de colonnes doivent rester cohérents avec les noms Java/API.

---

# 91. Mapping Java recommandé

Exemple conceptuel :

```java
@Entity
@Table(name = "customers")
public class Customer {

    @Id
    private UUID id;

    @Column(name = "customer_number", nullable = false, unique = true)
    private String customerNumber;

    // ...
}
```

Les montants :

```java
BigDecimal
```

Les identifiants :

```java
UUID
```

Les dates :

```java
Instant
```

ou un type temporel explicitement choisi dans l'architecture.

Les enums métier doivent être contrôlés et versionnés.

---

# 92. Dictionnaire des données critiques

Les données suivantes nécessitent une attention particulière.

| Donnée              | Criticité      | Protection           |
| ------------------- | -------------- | -------------------- |
| password_hash       | Très élevée    | Hash + jamais exposé |
| payment metadata    | Très élevée    | Accès restreint      |
| wallet balance      | Très élevée    | Backend uniquement   |
| wallet transactions | Très élevée    | Ledger immuable      |
| loyalty balance     | Élevée         | Backend uniquement   |
| customer phone      | Élevée         | Donnée personnelle   |
| customer email      | Élevée         | Donnée personnelle   |
| device identifier   | Élevée         | Accès restreint      |
| IP address          | Moyenne/élevée | Audit                |
| audit logs          | Très élevée    | Lecture restreinte   |
| webhook payload     | Élevée         | Accès technique      |
| API secrets         | Critique       | Secret Manager       |

---

# 93. Données qui ne doivent jamais être exposées directement par les APIs

Les APIs publiques ne doivent jamais retourner directement :

```text
password_hash
API keys
API secrets
private keys
payment provider credentials
internal configuration secrets
security tokens
```

Les champs sensibles doivent être exclus des DTO publics.

---

# 94. Règle Snapshot

Pour les données dont la valeur peut changer après une transaction, conserver un snapshot.

Exemple :

```text
Product.name
Product.unit_price
```

Lorsqu'un produit est ajouté à une commande :

```text
OrderItem.product_name_snapshot
OrderItem.unit_price
```

doivent conserver les valeurs historiques.

Ainsi :

```text
Produit aujourd'hui = 5 000 XOF

Commande hier = 4 500 XOF
```

la commande historique reste à :

```text
4 500 XOF
```

---

# 95. Ledger financier

Le ledger doit être considéré comme une donnée historique critique.

Une transaction :

```text
WalletTransaction
```

ne doit pas être modifiée arbitrairement après validation.

En cas d'erreur :

```text
Transaction incorrecte
       ↓
Transaction de correction
       ↓
Nouvel état cohérent
```

et non :

```text
Modification silencieuse de l'ancienne transaction
```

---

# 96. Ledger fidélité

Le même principe s'applique aux points.

Exemple :

```text
+100 points
-50 points
+200 points bonus
```

doit produire un historique permettant de reconstruire le solde.

Une correction doit générer une transaction `ADJUSTMENT` ou `REVERSAL`.

---

# 97. Règles de suppression

## Suppression interdite ou fortement déconseillée

```text
Payment
WalletTransaction
LoyaltyTransaction
AuditLog
WebhookEvent traité
```

## Suppression logique privilégiée

```text
User
Customer
Merchant
Product
Campaign
Coupon
Reward
Notification
```

---

# 98. Checklist de conformité du modèle

Avant génération du schéma SQL, Hermes doit vérifier :

```text
[ ] Toutes les PK sont définies
[ ] Toutes les FK sont définies
[ ] Toutes les contraintes UNIQUE sont définies
[ ] Tous les champs obligatoires sont NOT NULL
[ ] Tous les montants utilisent DECIMAL
[ ] Les points utilisent un type entier
[ ] Les statuts sont contrôlés
[ ] Les paiements sont idempotents
[ ] Les webhooks sont idempotents
[ ] Les wallets utilisent un ledger
[ ] Les points utilisent un ledger
[ ] Les snapshots de commande existent
[ ] Les données sensibles sont protégées
[ ] Les suppressions critiques sont interdites
[ ] Les migrations sont versionnées
[ ] Les index principaux existent
[ ] Les contraintes de cohérence sont testées
```

---

# 99. Règle Hermes — aucune interprétation silencieuse

Lorsqu'une information manque dans le dictionnaire :

1. Hermes doit rechercher la règle dans `01_PRD.md`.
2. Puis dans `02_FunctionalSpecification.md`.
3. Puis dans `03_TechnicalArchitecture.md`.
4. Si aucune règle n'existe, Hermes doit **signaler l'ambiguïté**.
5. Hermes ne doit pas inventer une règle métier critique.
6. Toute décision nécessaire doit être ajoutée à la documentation avant de devenir une règle du système.

---

# 100. Source de vérité finale

L'ordre de référence documentaire est :

```text
00_Master  (autorité suprême)
       ↓
00_ProjectVision
       ↓
02_ProjectOverview
       ↓
03_TechnicalArchitecture
       ↓
04_DataModel
       ↓
05_API_Specification
       ↓
Code
```

Le `00_Master.md` définit le vocabulaire, les périmètres V1/V2/V3, les états authoritative et la numérotation officielle.

Le dictionnaire de données fait partie intégrante du `04_DataModel.md`.

Il doit être mis à jour lorsque :

* une nouvelle entité est créée ;
* un champ est ajouté ;
* un champ est supprimé ;
* une relation est modifiée ;
* un statut est ajouté ;
* une règle d'intégrité évolue ;
* une donnée devient sensible ;
* une nouvelle contrainte métier apparaît.
