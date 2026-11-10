Oui. Pour **Magic Gold**, je vous conseille de figer maintenant une architecture qui reste **simple pour le MVP**, mais qui permet ensuite de passer à plusieurs milliers de commerces sans refaire le produit.

Voici le contenu proposé pour `03_TechnicalArchitecture.md`.

````markdown
# MAGIC GOLD — Technical Architecture

**Document:** `03_TechnicalArchitecture.md`  
**Version:** 1.0  
**Statut:** Architecture de référence  
**Produit:** Magic Gold  
**Éditeur:** Nectux Ingénieries  
**Modèle:** SaaS Multi-Tenant — Mobile-First — Offline-First  
**Marché initial:** Côte d'Ivoire / Afrique de l'Ouest

---

# 1. Objet

Ce document définit l'architecture technique cible de Magic Gold.

L'architecture doit répondre aux contraintes suivantes :

- application mobile simple pour les commerçants ;
- fonctionnement fiable avec une connexion Internet instable ;
- gestion commerciale ;
- gestion des stocks ;
- gestion des caisses ;
- paiements digitaux ;
- SaaS multi-tenant ;
- multi-utilisateurs ;
- multi-magasins ;
- sécurité forte ;
- traçabilité des opérations ;
- capacité d'évolution vers plusieurs milliers de commerces.

L'objectif n'est pas de construire une architecture excessivement complexe dès le départ.

Magic Gold doit commencer avec une architecture **modulaire, robuste et exploitable par une petite équipe**, puis évoluer progressivement.

---

# 2. Principes architecturaux

## ARCH-001 — Mobile First

L'application principale est conçue prioritairement pour smartphone.

Les opérations fréquentes doivent être accessibles rapidement :

```text
VENDRE
ENCAISSER
CONSULTER STOCK
VOIR CA
````

---

## ARCH-002 — Offline First

La connexion Internet ne doit pas être considérée comme permanente.

Les opérations compatibles doivent pouvoir être effectuées localement.

```text
Utilisateur
    |
    v
Application Mobile
    |
    +---- Internet disponible ----> API
    |
    +---- Internet indisponible --> Stockage local
                                      |
                                      v
                                  Synchronisation
```

---

## ARCH-003 — API First

Toutes les fonctionnalités métier importantes doivent être accessibles via une API sécurisée.

```text
Mobile
PWA
Backoffice
Partenaires
        |
        v
      API
        |
        v
   Services métier
```

---

## ARCH-004 — Multi-Tenant

Magic Gold est un SaaS.

Un même backend héberge plusieurs commerces.

Chaque organisation constitue un tenant logique.

```text
Magic Gold
│
├── Tenant A
│   ├── Magasin A1
│   └── Magasin A2
│
├── Tenant B
│   └── Magasin B1
│
└── Tenant C
    ├── Magasin C1
    ├── Magasin C2
    └── Magasin C3
```

Aucune donnée d'un tenant ne doit être accessible à un autre tenant.

---

# 3. Architecture générale

Architecture cible :

```text
                    INTERNET
                       |
              +--------+--------+
              |                 |
           Mobile             Web/PWA
              |                 |
              +--------+--------+
                       |
                       v
                API Gateway /
                Reverse Proxy
                       |
                       v
              +------------------+
              |  Magic Gold API  |
              +------------------+
                       |
        +--------------+--------------+
        |              |              |
        v              v              v
   Identity        Business       Payment
   & Access         Services       Services
        |              |              |
        |        +-----+-----+        |
        |        |     |     |        |
        v        v     v     v        v
     Keycloak  Sales Stock Cash     Providers
                       |
                       v
                  PostgreSQL
                       |
              +--------+--------+
              |                 |
             Redis          Object Storage
              |                 |
              v                 v
          Cache/Queue      Receipts/Files
```

---

# 4. Stack technique cible

## 4.1 Backend

Technologie recommandée :

```text
Java 21+
Spring Boot 3.x
Spring Security
Spring Data JPA
Hibernate
Flyway
REST API
OpenAPI
```

Le backend est organisé par domaines fonctionnels.

---

# 5. Architecture modulaire du backend

Le backend ne doit pas être développé comme un ensemble de contrôleurs sans séparation métier.

Structure cible :

```text
magic-gold-backend/

├── identity/
├── tenant/
├── store/
├── user/
├── product/
├── category/
├── inventory/
├── sale/
├── payment/
├── cash/
├── customer/
├── supplier/
├── purchase/
├── expense/
├── reporting/
├── notification/
├── subscription/
├── synchronization/
├── audit/
└── shared/
```

Chaque module doit contenir autant que possible :

```text
domain
application
infrastructure
api
```

Exemple :

```text
sale/
├── domain/
├── application/
├── infrastructure/
└── api/
```

---

# 6. Architecture hexagonale / modulaire

Le domaine métier ne doit pas dépendre directement des fournisseurs externes.

Exemple :

```text
                 +----------------+
                 | Payment Domain |
                 +----------------+
                         |
                    PaymentPort
                         |
          +--------------+--------------+
          |                             |
          v                             v
   CinetPayAdapter               OtherProvider
```

Le métier connaît :

```text
PaymentProvider
PaymentMethod
PaymentIntent
PaymentTransaction
```

mais ne doit pas être fortement couplé à un fournisseur spécifique.

---

# 7. Frontend mobile

Deux options sont possibles.

## Option principale

Application mobile :

```text
Flutter
Dart
```

Avantages :

* Android ;
* iOS ;
* une base de code ;
* bonne expérience mobile ;
* accès aux fonctions natives ;
* possibilité d'intégrer caméra et scanner ;
* fonctionnement offline.

---

# 8. Architecture mobile

Structure :

```text
lib/

├── core/
│   ├── config/
│   ├── network/
│   ├── storage/
│   ├── security/
│   └── synchronization/
│
├── features/
│   ├── authentication/
│   ├── dashboard/
│   ├── products/
│   ├── inventory/
│   ├── sales/
│   ├── payments/
│   ├── cash/
│   ├── customers/
│   ├── purchases/
│   ├── expenses/
│   └── reports/
│
└── shared/
```

---

# 9. Architecture Offline

Le mobile possède une base locale.

Technologie possible :

```text
SQLite
```

ou une abstraction locale adaptée à Flutter.

Les données locales sont séparées en :

```text
MASTER DATA
TRANSACTION DATA
SYNC QUEUE
LOCAL SETTINGS
```

---

# 10. Données locales

Exemple :

```text
local_product
local_category
local_customer
local_sale
local_sale_item
local_payment
local_cash_session
sync_operation
sync_conflict
```

Chaque opération locale possède :

```text
id
clientOperationId
deviceId
tenantId
userId
createdAt
updatedAt
syncStatus
```

---

# 11. Synchronisation

La synchronisation est un composant central de Magic Gold.

Architecture :

```text
                 MOBILE
                    |
              Local Database
                    |
              Sync Queue
                    |
               INTERNET
                    |
                    v
              Sync API
                    |
              Validation
                    |
             Idempotency
                    |
             Business Rules
                    |
                    v
               PostgreSQL
```

La spécification détaillée est définie dans :

```text
05_SynchronizationProtocol.md
```

---

# 12. Identifiants distribués

Les opérations mobiles doivent pouvoir être identifiées de manière unique avant leur synchronisation.

Exemple :

```text
clientOperationId
```

Ce mécanisme permet d'éviter :

```text
VENTE A
VENTE A
```

lorsqu'une requête est envoyée deux fois.

---

# 13. API

L'API principale est REST.

Exemples :

```text
/api/v1/auth
/api/v1/tenants
/api/v1/stores
/api/v1/products
/api/v1/inventory
/api/v1/sales
/api/v1/payments
/api/v1/cash
/api/v1/customers
/api/v1/purchases
/api/v1/expenses
/api/v1/reports
/api/v1/sync
```

Toutes les API doivent être versionnées.

---

# 14. Authentification

Architecture recommandée :

```text
Mobile
   |
   v
Authentication
   |
   v
Keycloak
   |
   v
JWT
   |
   v
Magic Gold API
```

Keycloak gère notamment :

* utilisateurs ;
* authentification ;
* sessions ;
* rôles ;
* tokens ;
* renouvellement ;
* éventuellement OTP selon l'évolution du produit.

---

# 15. Autorisation

L'autorisation doit être effectuée côté serveur.

Le JWT peut contenir :

```text
userId
tenantId
roles
permissions
```

Le backend vérifie :

```text
Utilisateur
    |
    +--> Tenant ?
    |
    +--> Magasin ?
    |
    +--> Rôle ?
    |
    +--> Permission ?
    |
    v
Autoriser / Refuser
```

---

# 16. Isolation multi-tenant

Chaque donnée métier sensible doit être associée au tenant.

Exemple :

```text
Product
 └── tenantId

Sale
 └── tenantId

Payment
 └── tenantId

Customer
 └── tenantId
```

Une requête doit toujours être filtrée par le contexte du tenant.

---

# 17. Base de données

Base principale :

```text
PostgreSQL
```

PostgreSQL constitue la source de vérité du système.

Domaines principaux :

```text
tenant
store
user
product
inventory
sale
payment
cash
customer
supplier
purchase
expense
subscription
audit
```

---

# 18. Principe Source of Truth

Le système distingue :

```text
SOURCE OF TRUTH
PostgreSQL

CACHE
Redis

LOCAL CACHE
Mobile

FILES
Object Storage
```

Redis et le stockage local ne doivent jamais devenir la source définitive des données financières.

---

# 19. Redis

Redis peut être utilisé pour :

* cache ;
* sessions techniques ;
* rate limiting ;
* OTP ;
* verrous distribués ;
* files légères ;
* idempotency keys ;
* données temporaires.

La stratégie détaillée est définie dans :

```text
10_RedisStrategy.md
```

---

# 20. Paiements digitaux

Les paiements sont isolés dans un module dédié.

```text
Magic Gold
     |
     v
Payment Service
     |
     +---- Provider A
     |
     +---- Provider B
     |
     +---- Provider C
     |
     +---- Mobile Money
     |
     +---- QR
```

Le système ne doit pas dépendre d'un seul fournisseur.

---

# 21. Payment Adapter

Interface conceptuelle :

```text
PaymentProvider

+ initiatePayment()
+ getPaymentStatus()
+ verifyWebhook()
+ refundPayment()
```

Chaque fournisseur implémente cette interface.

Exemple :

```text
CinetPayAdapter
FeexPayAdapter
OtherProviderAdapter
```

Les fournisseurs effectivement retenus seront définis lors de l'étude d'intégration et des contrats disponibles.

---

# 22. Cycle d'un paiement

```text
CREATE PAYMENT
      |
      v
INITIATED
      |
      v
PENDING
      |
      +------> FAILED
      |
      +------> CANCELLED
      |
      v
CONFIRMED
      |
      v
RECONCILIATION
```

Un webhook ne doit jamais être accepté sans validation.

---

# 23. Idempotence des paiements

Un même événement fournisseur peut être reçu plusieurs fois.

Exemple :

```text
Webhook #1
Webhook #1
Webhook #1
```

Le système doit produire :

```text
1 transaction métier
```

et non :

```text
3 transactions
```

L'idempotence doit être garantie au niveau serveur.

---

# 24. Architecture événementielle

Les événements métier importants peuvent être publiés.

Exemples :

```text
SaleConfirmed
PaymentConfirmed
PaymentFailed
StockLow
StockAdjusted
CashOpened
CashClosed
UserCreated
SubscriptionChanged
```

Architecture :

```text
Business Service
      |
      v
Domain Event
      |
      v
Event Publisher
      |
      +---- Notification
      +---- Reporting
      +---- Audit
      +---- Synchronization
```

La stratégie complète est définie dans :

```text
09_EventArchitecture.md
```

---

# 25. Gestion du stock

Le stock doit être dérivé de mouvements traçables.

Exemple :

```text
STOCK INITIAL
     +
ACHAT
     -
VENTE
     +
RETOUR
     -
PERTE
     ±
AJUSTEMENT
     =
STOCK COURANT
```

Chaque mouvement doit avoir une référence.

---

# 26. Architecture caisse

La caisse est liée à :

```text
tenant
store
register
user
cashSession
```

Une session :

```text
OPEN
  |
  +-- SALES
  +-- CASH IN
  +-- CASH OUT
  |
CLOSING
  |
CLOSED
```

Les opérations financières ne doivent pas être supprimées physiquement.

---

# 27. Reporting

Les rapports peuvent initialement être calculés à partir de PostgreSQL.

Pour les besoins importants :

```text
PostgreSQL
     |
     v
Aggregations
     |
     v
Reporting Views
     |
     v
Dashboard
```

Une infrastructure analytique séparée n'est pas nécessaire pour le MVP.

Elle pourra être ajoutée ultérieurement.

---

# 28. Notifications

Architecture :

```text
Business Event
      |
      v
Notification Service
      |
      +---- Push
      +---- SMS
      +---- Email
      +---- WhatsApp
```

Les notifications ne doivent pas bloquer la transaction métier principale.

---

# 29. Stockage de fichiers

Les fichiers tels que :

* photos produits ;
* justificatifs ;
* reçus ;
* documents commerciaux ;

doivent être stockés dans un Object Storage.

Exemple :

```text
S3 compatible Object Storage
```

La base de données conserve uniquement les métadonnées et références.

---

# 30. Sécurité

Principes :

```text
HTTPS obligatoire
JWT
RBAC
Tenant Isolation
Audit
Rate Limiting
Validation serveur
Idempotence
Secrets hors code source
Encryption at rest si disponible
Backups
```

Les détails sont définis dans :

```text
07_SecurityArchitecture.md
```

---

# 31. Audit

Les opérations sensibles génèrent des événements d'audit.

Exemple :

```text
USER
ACTION
ENTITY
ENTITY_ID
TENANT
TIMESTAMP
IP
DEVICE
OLD_VALUE
NEW_VALUE
```

Les logs d'audit ne doivent pas être modifiables par les utilisateurs ordinaires.

---

# 32. Architecture des environnements

Trois environnements minimum :

```text
DEV
STAGING
PRODUCTION
```

### DEV

Utilisé pour le développement local.

### STAGING

Utilisé pour :

* tests ;
* intégrations paiement ;
* validation métier ;
* démonstrations techniques.

### PRODUCTION

Environnement des commerces réels.

---

# 33. Déploiement

Architecture initiale :

```text
Internet
   |
Reverse Proxy
   |
Magic Gold API
   |
PostgreSQL
   |
Redis
```

Les composants peuvent être conteneurisés avec Docker.

---

# 34. Architecture Docker

Exemple :

```text
docker-compose.yml

services:

  api:
    magic-gold-api

  postgres:
    postgres

  redis:
    redis

  keycloak:
    keycloak

  nginx:
    reverse-proxy
```

Le découpage exact pourra évoluer.

---

# 35. Pourquoi ne pas commencer directement en microservices ?

Magic Gold ne doit pas commencer avec une dizaine de microservices indépendants.

Le MVP doit privilégier :

```text
MODULAR MONOLITH
```

avec des modules fortement séparés.

Exemple :

```text
Magic Gold API
│
├── Sales
├── Stock
├── Payments
├── Cash
├── Customers
├── Reports
└── Users
```

Cela permet :

* développement plus rapide ;
* déploiement plus simple ;
* débogage plus facile ;
* coûts réduits ;
* transactions cohérentes ;
* moins d'infrastructure.

Les modules pouvant être fortement sollicités pourront ensuite être extraits.

---

# 36. Architecture évolutive

Evolution possible :

```text
PHASE 1
Modular Monolith
     |
     v
PHASE 2
Modular Monolith + Workers
     |
     v
PHASE 3
Services spécialisés
     |
     v
PHASE 4
Architecture distribuée
```

Extraction potentielle :

```text
Payment Service
Notification Service
Reporting Service
Synchronization Service
```

Il ne faut pas extraire un service simplement parce qu'il existe fonctionnellement.

---

# 37. Scalabilité

Le backend doit être stateless autant que possible.

Architecture :

```text
             Load Balancer
              /        \
             /          \
       API Instance 1   API Instance 2
             \          /
              \        /
               PostgreSQL
                    |
                  Redis
```

Cela permettra d'ajouter des instances lorsque la charge augmente.

---

# 38. Sauvegardes

PostgreSQL doit bénéficier de :

* sauvegardes régulières ;
* rétention configurable ;
* sauvegarde hors serveur principal ;
* tests périodiques de restauration.

Une sauvegarde non testée ne doit pas être considérée comme une stratégie de récupération fiable.

---

# 39. Observabilité

Le système doit fournir :

```text
Application Logs
Access Logs
Audit Logs
Metrics
Health Checks
Error Tracking
```

Endpoints techniques possibles :

```text
/actuator/health
/actuator/metrics
```

Les endpoints sensibles ne doivent pas être exposés publiquement sans protection.

---

# 40. Monitoring

Surveiller au minimum :

```text
CPU
RAM
Disk
Database
API latency
HTTP errors
Payment errors
Sync errors
Queue size
Active tenants
```

Alertes importantes :

```text
DATABASE_UNAVAILABLE
DISK_LOW
PAYMENT_FAILURE_RATE_HIGH
SYNC_FAILURE_RATE_HIGH
API_ERROR_RATE_HIGH
```

---

# 41. Performance

Priorités :

### P0

Vente.

### P0

Paiement.

### P0

Consultation stock.

### P1

Dashboard.

### P1

Recherche produit.

### P2

Rapports lourds.

Les rapports complexes ne doivent pas ralentir les opérations de caisse.

---

# 42. Résilience réseau

Le mobile doit distinguer :

```text
ONLINE
OFFLINE
SYNCING
SYNC_ERROR
```

L'utilisateur doit comprendre clairement l'état de synchronisation.

Exemple :

```text
🟢 Synchronisé
🟠 Synchronisation en cours
🔴 3 opérations à synchroniser
```

---

# 43. Gestion des conflits

Les conflits peuvent concerner :

* stock ;
* prix ;
* client ;
* vente ;
* modification concurrente.

Les règles précises sont définies dans :

```text
05_SynchronizationProtocol.md
```

Une vente locale validée ne doit pas être silencieusement écrasée.

---

# 44. Architecture des données financières

Les données financières doivent être immuables autant que possible.

Au lieu de :

```text
UPDATE payment SET amount = ...
```

préférer :

```text
Payment
PaymentAdjustment
Refund
AuditEvent
```

Cela permet une meilleure traçabilité.

---

# 45. Transactions métier

Certaines opérations doivent être atomiques.

Exemple :

```text
CONFIRM SALE

BEGIN
    create sale
    create sale items
    update stock
    create payment reference
    create audit
COMMIT
```

En cas d'échec :

```text
ROLLBACK
```

Une opération partiellement enregistrée ne doit pas devenir une vente confirmée.

---

# 46. Transaction paiement externe

Un paiement externe ne doit pas être traité comme une transaction PostgreSQL classique.

```text
Magic Gold
    |
    | initiate
    v
Provider externe
    |
    | confirmation
    v
Webhook
    |
    v
Magic Gold
    |
    v
Payment CONFIRMED
```

La confirmation fournisseur est la source nécessaire pour considérer le paiement digital comme confirmé.

---

# 47. API externe

Magic Gold pourra exposer ultérieurement des API partenaires :

```text
/api/v1/public/*
```

Ces API seront séparées des API privées.

Elles devront utiliser :

* API keys ;
* OAuth2 selon le besoin ;
* rate limiting ;
* scopes ;
* audit.

---

# 48. Administration SaaS

Un espace d'administration global pourra gérer :

```text
Tenants
Subscriptions
Plans
Payment Providers
System Configuration
Support
Audit
```

Cet espace est séparé de l'administration d'un commerce.

---

# 49. Architecture logique finale

```text
                           MAGIC GOLD
                               |
          +--------------------+--------------------+
          |                    |                    |
       MOBILE                 PWA              ADMIN
          |                    |                    |
          +--------------------+--------------------+
                               |
                         API / AUTH
                               |
        +----------------------+----------------------+
        |          |           |          |           |
       SALES      STOCK     PAYMENTS     CASH       CRM
        |          |           |          |           |
        +----------+-----------+----------+-----------+
                               |
                         DOMAIN EVENTS
                               |
          +--------------------+--------------------+
          |                    |                    |
     NOTIFICATIONS          AUDIT              REPORTING
                               |
                               v
                         POSTGRESQL
                               |
                  +------------+------------+
                  |                         |
                REDIS                 OBJECT STORAGE
```

---

# 50. Architecture cible à long terme

Lorsque Magic Gold atteindra une masse importante :

```text
                    LOAD BALANCER
                          |
             +------------+------------+
             |            |            |
            API          API          API
             |            |            |
             +------------+------------+
                          |
                     MESSAGE BUS
                          |
       +------------------+------------------+
       |                  |                  |
   SALES SERVICE     PAYMENT SERVICE    SYNC SERVICE
       |                  |                  |
       +------------------+------------------+
                          |
                    DATA PLATFORM
```

Cette architecture n'est pas requise pour le MVP.

---

# 51. Décisions techniques

| ID           | Décision                                                                 |
| ------------ | ------------------------------------------------------------------------ |
| ARCH-DEC-001 | Architecture Modular Monolith au démarrage                               |
| ARCH-DEC-002 | Backend Spring Boot                                                      |
| ARCH-DEC-003 | PostgreSQL comme base principale                                         |
| ARCH-DEC-004 | Redis pour cache et données temporaires                                  |
| ARCH-DEC-005 | Keycloak pour IAM                                                        |
| ARCH-DEC-006 | Flutter pour l'application mobile                                        |
| ARCH-DEC-007 | REST API versionnée                                                      |
| ARCH-DEC-008 | Offline First                                                            |
| ARCH-DEC-009 | SaaS Multi-Tenant                                                        |
| ARCH-DEC-010 | Paiements derrière une couche d'abstraction                              |
| ARCH-DEC-011 | Docker pour le déploiement                                               |
| ARCH-DEC-012 | Object Storage pour les fichiers                                         |
| ARCH-DEC-013 | Audit des opérations sensibles                                           |
| ARCH-DEC-014 | Architecture événementielle progressive                                  |
| ARCH-DEC-015 | Microservices uniquement lorsque la charge ou l'organisation le justifie |

---

# 52. Priorité d'implémentation

## Sprint 1 — Foundation

```text
Project
Tenant
Authentication
Users
Stores
Database
```

## Sprint 2 — Catalogue

```text
Products
Categories
Barcode
Pricing
```

## Sprint 3 — Stock

```text
Inventory
Stock Movements
Inventory Adjustment
Low Stock
```

## Sprint 4 — Vente

```text
Cart
Sale
Sale Items
Receipt
```

## Sprint 5 — Caisse

```text
Cash Register
Cash Session
Cash Movements
Closing
```

## Sprint 6 — Paiements

```text
Payment Intent
Provider Adapter
Webhook
Confirmation
Reconciliation
```

## Sprint 7 — Offline

```text
Local Database
Sync Queue
Sync API
Idempotency
Conflict Management
```

## Sprint 8 — Dashboard

```text
KPIs
Reports
Notifications
```

---

# 53. Documents dépendants

Cette architecture doit être complétée par :

```text
04_DataModel.md
05_SynchronizationProtocol.md
06_API.md
07_SecurityArchitecture.md
08_HUBArchitecture.md
09_EventArchitecture.md
10_RedisStrategy.md
11_MobileArchitecture.md
12_PWAArchitecture.md
13_Deployment.md
14_TestStrategy.md
15_PaymentArchitecture.md
16_MultiTenancy.md
17_BusinessRules.md
```

---

# 54. Règle fondamentale

Magic Gold doit rester techniquement simple à exploiter tout en étant suffisamment structuré pour évoluer.

La règle d'architecture est :

```text
SIMPLE AU DÉMARRAGE
        +
MODULAIRE
        +
SÉCURISÉ
        +
OFFLINE-FIRST
        +
PAYMENT-READY
        =
MAGIC GOLD
```

**Nectux Ingénieries — Magic Gold**

````

### Point important pour votre projet

Je garderais vraiment le choix **Modular Monolith + PostgreSQL + Redis + Keycloak + Flutter + Docker** pour le démarrage. Pour Magic Gold, partir directement sur des microservices serait surtout une source de complexité.

Et surtout, dans votre cas, **le paiement digital doit être une brique d'architecture de premier niveau**, pas simplement un bouton ajouté à la fin. Le flux central devient :

```text
PRODUIT
   ↓
VENTE
   ↓
PAIEMENT
   ↓
CONFIRMATION
   ↓
CAISSE / TRANSACTION
   ↓
STOCK
   ↓
RAPPROCHEMENT
   ↓
DASHBOARD
````

C'est cette chaîne qui peut donner à Magic Gold une vraie identité de produit SaaS plutôt qu'une simple application de gestion de stock.
