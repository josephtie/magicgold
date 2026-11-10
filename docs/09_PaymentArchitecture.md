# MAGIC GOLD — Payment Architecture

**Version:** 1.0
**Document:** `09_PaymentArchitecture.md`
**Projet:** MAGIC GOLD
**Statut:** Référence fonctionnelle et technique
**Audience:** Product, Backend, Frontend, Mobile, Finance, QA, DevOps, Hermes

---

# 1. Objectif

Ce document définit l'architecture fonctionnelle, métier et technique du système de paiement MAGIC GOLD.

Il couvre :

* l'initiation d'un paiement ;
* l'autorisation ;
* la confirmation ;
* la capture lorsque nécessaire ;
* l'échec ;
* l'annulation ;
* le remboursement ;
* les frais ;
* les commissions ;
* le settlement ;
* la réconciliation ;
* les webhooks ;
* l'idempotence ;
* la gestion des erreurs ;
* la sécurité ;
* l'audit ;
* les notifications ;
* les règles métier par acteur.

Le système doit permettre d'intégrer plusieurs fournisseurs de paiement sans coupler le domaine métier à un fournisseur particulier.

---

# 2. Principes fondamentaux

MAGIC GOLD applique les principes suivants :

```text
Payment is stateful
Payment is auditable
Payment is idempotent
Payment is asynchronous when required
Provider is not the source of business truth
Ledger is immutable
Frontend is never trusted
Webhook is never blindly trusted
Financial operations are never silently overwritten
```

---

# 3. Architecture générale

```text
                    CUSTOMER
                        │
                        ▼
                 MAGIC GOLD APP
                        │
                        ▼
                 Payment API
                        │
                        ▼
                Payment Service
                        │
          ┌─────────────┼─────────────┐
          ▼             ▼             ▼
       Order        Risk Engine   Payment Rules
          │             │             │
          └─────────────┼─────────────┘
                        ▼
                Payment Orchestrator
                        │
              ┌─────────┼─────────┐
              ▼         ▼         ▼
          Provider A Provider B Provider C
              │         │         │
              └─────────┼─────────┘
                        ▼
                  Payment Events
                        │
             ┌──────────┼──────────┐
             ▼          ▼          ▼
         Ledger     Settlement  Notification
             │          │          │
             └──────────┼──────────┘
                        ▼
                       Audit
```

---

# 4. Domaines concernés

Le paiement ne doit pas être confondu avec :

```text
Order
Payment
Wallet
Ledger
Settlement
Refund
Notification
```

Chaque domaine possède sa responsabilité.

### Order

Détermine ce qui doit être payé.

### Payment

Détermine l'état de l'opération de paiement.

### Wallet

Gère un solde lorsque le modèle MAGIC GOLD le prévoit.

### Ledger

Conserve les mouvements financiers de manière immuable.

### Settlement

Gère le règlement financier entre MAGIC GOLD, les marchands et les fournisseurs.

### Refund

Gère le retour de fonds.

### Notification

Informe les acteurs du résultat.

---

# 5. Règle fondamentale

Une commande et un paiement sont deux objets différents.

```text
ORDER
   │
   └── amountDue = 25 000 XOF

PAYMENT
   │
   └── amount = 25 000 XOF
```

Le paiement doit conserver le montant réellement attendu au moment de son initiation.

Une modification ultérieure du produit ou du prix ne doit pas modifier rétroactivement le paiement.

---

# 6. Acteurs

Les acteurs concernés sont :

```text
CUSTOMER
MERCHANT
MERCHANT_OPERATOR
SUPPORT
FINANCE
AUDITOR
ADMIN
SUPER_ADMIN
PAYMENT_PROVIDER
SYSTEM
```

Le `PAYMENT_PROVIDER` est un système externe et non un rôle utilisateur MAGIC GOLD.

---

# 7. Règles métier par acteur

## 7.1 CUSTOMER

Le CUSTOMER peut :

* initier un paiement pour une commande autorisée ;
* consulter ses propres paiements ;
* consulter le statut de ses paiements ;
* recevoir les notifications ;
* demander un remboursement lorsque les règles l'autorisent.

Le CUSTOMER ne peut jamais :

```text
modifier payment.status directement
modifier payment.amount
modifier providerTransactionId
modifier settlement
modifier ledger
confirmer artificiellement un paiement
```

---

## 7.2 MERCHANT

Le MERCHANT peut :

* consulter les paiements de son périmètre ;
* consulter les montants collectés ;
* consulter les remboursements autorisés ;
* consulter les settlements ;
* demander un remboursement selon les règles métier.

Le MERCHANT ne peut pas transformer :

```text
FAILED → SUCCESS
PENDING → SUCCESS
```

manuellement.

---

## 7.3 MERCHANT_OPERATOR

L'opérateur agit uniquement dans le périmètre du marchand.

Par défaut :

```text
Payment view = possible
Payment modification = forbidden
Refund = according to permission
Settlement = forbidden
Ledger = read-only or forbidden
```

---

## 7.4 SUPPORT

SUPPORT peut :

* rechercher un paiement ;
* consulter son état ;
* consulter les erreurs ;
* consulter la commande associée ;
* créer un ticket ;
* expliquer le statut au client.

SUPPORT ne doit pas modifier directement les états financiers.

---

## 7.5 FINANCE

FINANCE peut :

* consulter les transactions ;
* consulter les remboursements ;
* contrôler les commissions ;
* effectuer les rapprochements ;
* analyser les settlements ;
* traiter les anomalies financières ;
* déclencher les opérations correctives autorisées.

Toute correction doit conserver l'historique.

---

## 7.6 AUDITOR

AUDITOR est principalement en lecture.

Il peut consulter :

```text
Payments
Refunds
Settlements
Ledger
Audit logs
Provider events
Reconciliation
```

Il ne doit pas modifier les données financières.

---

## 7.7 ADMIN

ADMIN peut gérer la configuration opérationnelle autorisée.

Il ne doit pas automatiquement avoir le pouvoir de :

```text
modifier ledger
forcer SUCCESS
modifier settlement confirmé
supprimer payment
```

Les opérations financières critiques doivent rester séparées.

---

## 7.8 SUPER_ADMIN

SUPER_ADMIN peut intervenir sur les configurations globales et les opérations exceptionnelles explicitement autorisées.

Toute opération critique doit être :

```text
authenticated
authorized
reasoned
audited
traceable
```

---

## 7.9 PAYMENT_PROVIDER

Le fournisseur peut fournir :

* une autorisation ;
* une confirmation ;
* un identifiant externe ;
* un statut ;
* un callback/webhook ;
* un résultat de remboursement.

MAGIC GOLD doit néanmoins vérifier et interpréter les informations reçues.

---

# 8. Payment Intent

Avant de réaliser une transaction, MAGIC GOLD peut créer un `PaymentIntent`.

```text
PaymentIntent
├── id
├── orderId
├── customerId
├── merchantId
├── amount
├── currency
├── paymentMethod
├── status
├── expiresAt
├── idempotencyKey
└── metadata
```

Le Payment Intent représente l'intention de payer.

---

# 9. Payment Transaction

La transaction représente l'opération de paiement.

```text
Payment
├── id
├── paymentIntentId
├── orderId
├── customerId
├── merchantId
├── amount
├── currency
├── paymentMethod
├── provider
├── providerTransactionId
├── status
├── failureCode
├── createdAt
├── updatedAt
└── metadata
```

---

# 10. Monnaie

MAGIC GOLD doit stocker les montants avec une représentation non ambiguë.

Pour le XOF :

```text
currency = XOF
```

Le système doit éviter les calculs financiers avec des nombres flottants.

Préférer :

```text
integer / decimal exact
```

selon l'architecture de persistance.

Exemple :

```text
25 000 XOF
```

doit rester exactement :

```text
25000
```

et jamais :

```text
24999.999999
```

---

# 11. Devise

Chaque paiement doit posséder explicitement :

```text
currency
```

Exemple :

```text
amount = 25000
currency = XOF
```

Le système ne doit jamais déduire la devise uniquement depuis le pays du client.

---

# 12. Payment Methods

L'architecture doit être extensible.

Exemples :

```text
MOBILE_MONEY
CARD
BANK_TRANSFER
WALLET
QR
OTHER
```

La liste réelle dépendra des fournisseurs intégrés.

Le domaine métier ne doit pas dépendre d'un nom commercial particulier.

---

# 13. Provider Abstraction

Architecture :

```text
PaymentService
      │
      ▼
PaymentProvider
      │
      ├── ProviderAdapterA
      ├── ProviderAdapterB
      └── ProviderAdapterC
```

Interface conceptuelle :

```text
createPayment()
authorizePayment()
capturePayment()
cancelPayment()
refundPayment()
getPaymentStatus()
verifyWebhook()
```

---

# 14. Payment Lifecycle

Cycle général :

```text
CREATED
   ↓
PENDING
   ↓
PROCESSING
   ↓
SUCCESS
```

Alternative :

```text
PENDING
   ↓
FAILED
```

ou :

```text
PENDING
   ↓
CANCELLED
```

---

# 15. Payment States

États recommandés :

```text
CREATED
PENDING
PROCESSING
AUTHORIZED
SUCCESS
FAILED
CANCELLED
EXPIRED
REFUND_PENDING
PARTIALLY_REFUNDED
REFUNDED
```

Le modèle final doit éviter les états redondants.

---

# 16. State Transition Rules

Exemple :

```text
CREATED
   ↓
PENDING
   ↓
PROCESSING
   ↓
SUCCESS
```

Une transaction :

```text
SUCCESS → PENDING
SUCCESS → CREATED
SUCCESS → FAILED
```

ne doit jamais être autorisée comme transition normale.

---

# 17. Payment Initiation

Flux :

```text
Customer
   ↓
Order
   ↓
Create PaymentIntent
   ↓
Validate amount
   ↓
Validate merchant
   ↓
Validate order
   ↓
Risk checks
   ↓
Create Payment
   ↓
Provider
```

---

# 18. Préconditions

Avant l'initiation :

```text
Customer active
Order exists
Order belongs to customer
Merchant active
Order payable
Amount > 0
Currency valid
Payment method supported
```

---

# 19. Protection contre le double paiement

Une commande ne doit pas être payée deux fois accidentellement.

Avant de créer un nouveau paiement :

```text
Check existing successful payment
```

Si :

```text
order.paymentStatus = PAID
```

une nouvelle tentative doit être refusée sauf scénario métier explicitement prévu.

---

# 20. Idempotency Key

Toute initiation de paiement doit supporter une clé d'idempotence.

Exemple :

```text
Idempotency-Key: 4f7d...
```

Deux appels avec la même clé doivent produire le même résultat logique.

---

# 21. Idempotency Storage

Conserver :

```text
idempotencyKey
requestHash
userId
endpoint
response
status
createdAt
expiresAt
```

Une même clé ne doit pas pouvoir être réutilisée avec une requête différente.

---

# 22. Provider Request

MAGIC GOLD doit transmettre uniquement les données nécessaires.

Le provider doit recevoir :

```text
amount
currency
reference
customer information when required
callback/webhook information
metadata when supported
```

Les secrets internes ne doivent jamais être transmis inutilement.

---

# 23. Redirect / Hosted Payment

Si le fournisseur utilise une page externe :

```text
MAGIC GOLD
   ↓
Provider
   ↓
Customer completes payment
   ↓
Provider
   ↓
MAGIC GOLD callback
```

Le retour frontend ne constitue pas à lui seul une preuve de succès.

---

# 24. Frontend Return

Le frontend peut recevoir :

```text
SUCCESS
FAILED
CANCELLED
PENDING
```

mais doit ensuite demander au backend le statut officiel.

```text
Frontend result
      ↓
Backend payment status
      ↓
Official state
```

---

# 25. Webhook

Le webhook est une source importante de confirmation asynchrone.

Flux :

```text
Provider
   ↓
Webhook
   ↓
Signature verification
   ↓
Replay protection
   ↓
Idempotency
   ↓
Payment lookup
   ↓
Business validation
   ↓
State transition
   ↓
Ledger / Order
   ↓
Notification
```

---

# 26. Webhook Security

Vérifier selon les capacités du fournisseur :

```text
Signature
Secret
Timestamp
Nonce
Replay protection
Provider transaction ID
Amount
Currency
Reference
```

Un webhook non vérifié doit être rejeté.

---

# 27. Webhook Idempotency

Un même webhook peut être reçu plusieurs fois.

Exemple :

```text
WEBHOOK-123
WEBHOOK-123
WEBHOOK-123
```

Le système doit traiter l'événement une seule fois.

---

# 28. Vérification métier du webhook

Ne jamais appliquer aveuglément :

```text
provider.status = SUCCESS
```

Vérifier au minimum :

```text
payment exists
provider matches
reference matches
amount matches
currency matches
transaction identifier valid
current state allows transition
```

---

# 29. Paiement SUCCESS

Un paiement devient `SUCCESS` uniquement après validation selon le mécanisme retenu.

Exemple :

```text
Provider confirmation
+
Webhook verification
+
Amount validation
+
Currency validation
+
Reference validation
+
Valid state transition
```

---

# 30. Paiement FAILED

Un paiement peut échouer pour :

```text
Insufficient funds
Provider rejection
Timeout
Invalid payment method
Fraud/risk rejection
Technical failure
Customer cancellation
```

Le code d'erreur interne doit être normalisé.

---

# 31. Error Mapping

Ne pas exposer directement les erreurs techniques du fournisseur.

Exemple :

```text
Provider:
ERR_4587_X

MAGIC GOLD:
PAYMENT_DECLINED
```

Les détails techniques restent dans les logs internes.

---

# 32. Payment Timeout

Si le fournisseur ne répond pas :

```text
Payment = PROCESSING
```

peut être préférable à :

```text
Payment = FAILED
```

tant que le résultat n'est pas définitivement connu.

Cela évite de déclarer un paiement échoué alors qu'il peut avoir été débité.

---

# 33. Unknown State

Un état inconnu doit être traité comme :

```text
UNKNOWN / REQUIRES_RECONCILIATION
```

selon le modèle final.

Il ne faut jamais transformer automatiquement un état inconnu en `FAILED`.

---

# 34. Cancellation

Une annulation peut être effectuée si :

```text
payment.status = PENDING
```

ou un autre état explicitement autorisé.

Une opération déjà confirmée ne doit pas être "annulée" techniquement pour effacer son historique.

Elle doit utiliser le processus de remboursement lorsque nécessaire.

---

# 35. Refund

Le remboursement est une nouvelle opération financière.

```text
Original Payment
       ↓
Refund Request
       ↓
Refund Processing
       ↓
Provider
       ↓
Refund Confirmed
```

---

# 36. Refund Model

```text
Refund
├── id
├── paymentId
├── orderId
├── merchantId
├── customerId
├── amount
├── currency
├── reason
├── status
├── providerRefundId
├── requestedBy
├── approvedBy
├── createdAt
└── completedAt
```

---

# 37. Full Refund

Exemple :

```text
Payment = 25 000 XOF

Refund = 25 000 XOF
```

Après confirmation :

```text
refundStatus = REFUNDED
```

---

# 38. Partial Refund

Exemple :

```text
Payment = 25 000 XOF
Refund = 10 000 XOF
```

Il reste :

```text
15 000 XOF
```

remboursables si les règles métier l'autorisent.

---

# 39. Refund Ceiling

Le cumul des remboursements ne doit jamais dépasser le montant réellement encaissé.

```text
SUM(refunds.confirmed)
    <=
payment.successAmount
```

---

# 40. Refund Idempotency

Un remboursement doit également être idempotent.

Une même demande ne doit jamais générer deux remboursements.

---

# 41. Refund Authorization

Selon le montant et les règles métier :

```text
CUSTOMER
   ↓
REQUEST

MERCHANT
   ↓
REVIEW

FINANCE
   ↓
APPROVAL when required

PROVIDER
   ↓
EXECUTION
```

Les niveaux d'approbation doivent être configurables.

---

# 42. Commission

Si MAGIC GOLD prélève une commission :

```text
grossAmount
    ↓
commission
    ↓
netAmount
```

Exemple :

```text
Gross = 25 000 XOF
Commission = 500 XOF
Net = 24 500 XOF
```

Les montants doivent être conservés séparément.

---

# 43. Frais

Les frais peuvent être :

```text
CUSTOMER_FEE
MERCHANT_FEE
PROVIDER_FEE
PLATFORM_FEE
```

Le modèle final doit distinguer :

```text
grossAmount
fees
taxes
netAmount
```

---

# 44. Snapshot des frais

Les frais appliqués à une transaction doivent être conservés dans la transaction.

Une modification ultérieure de la configuration tarifaire ne doit pas modifier rétroactivement les transactions historiques.

---

# 45. Taxes

Si des taxes sont applicables :

```text
amount
+
fees
+
taxes
=
total according to business rule
```

La règle fiscale exacte doit être définie séparément si nécessaire.

---

# 46. Ledger

Le Ledger est différent du Payment.

```text
Payment
    ↓
Financial Event
    ↓
Ledger Entries
```

Le ledger doit être immuable.

Une correction doit créer une nouvelle écriture.

---

# 47. Exemple Ledger

Paiement :

```text
Customer payment
25 000 XOF
```

Écritures conceptuelles :

```text
Customer/Payment source
        -25 000

Merchant payable
        +24 500

Platform commission
        +500
```

Le modèle comptable exact doit être validé avant implémentation.

---

# 48. Settlement

Le settlement représente le règlement financier effectif.

```text
Payment SUCCESS
      ↓
Settlement pending
      ↓
Settlement processing
      ↓
Settlement completed
```

Le paiement réussi et le settlement réussi ne sont donc pas synonymes.

---

# 49. Settlement States

```text
PENDING
PROCESSING
COMPLETED
FAILED
RECONCILIATION_REQUIRED
CANCELLED
```

---

# 50. Settlement Rule

Un marchand peut avoir :

```text
Payment = SUCCESS
Settlement = PENDING
```

C'est un état normal si le règlement est différé.

---

# 51. Reconciliation

La réconciliation compare les sources.

```text
MAGIC GOLD
    ↕
PAYMENT PROVIDER
    ↕
BANK / SETTLEMENT
```

Comparer :

```text
reference
amount
currency
date
provider transaction ID
settlement ID
fees
net amount
```

---

# 52. Reconciliation Status

```text
MATCHED
MISMATCHED
MISSING_INTERNAL
MISSING_PROVIDER
AMOUNT_MISMATCH
DUPLICATE
PENDING
```

---

# 53. Financial Discrepancy

Une anomalie doit créer une entrée de rapprochement.

Exemple :

```text
Expected: 100 000
Provider: 95 000
```

Statut :

```text
AMOUNT_MISMATCH
```

Ne jamais corriger silencieusement le montant historique.

---

# 54. Reconciliation Correction

Une correction doit produire :

```text
Original transaction
+
Adjustment
+
Reason
+
Actor
+
Timestamp
+
Audit
```

---

# 55. Payment and Order

Le statut de commande peut dépendre du paiement, mais les deux statuts restent distincts.

Exemple :

```text
Payment SUCCESS
      ↓
Order PAID
```

Mais :

```text
Payment FAILED
      ↓
Order PAYMENT_FAILED
```

selon les règles métier.

---

# 56. Event Publishing

Après confirmation :

```text
PAYMENT_SUCCESS
```

peut générer :

```text
Order update
Ledger entry
Settlement record
Notification
Audit event
```

L'ordre exact doit respecter les transactions et garanties de cohérence.

---

# 57. Outbox Pattern

Pour les événements critiques :

```text
Payment Transaction
       │
       ├── Payment state
       ├── Ledger operation
       └── Outbox event
                  ↓
                Worker
                  ↓
        Notifications / integrations
```

---

# 58. Payment Notifications

S'appuyer sur `08_Notifications.md`.

Événements possibles :

```text
PAYMENT_INITIATED
PAYMENT_SUCCESS
PAYMENT_FAILED
PAYMENT_CANCELLED
PAYMENT_REFUND_REQUESTED
PAYMENT_REFUNDED
SETTLEMENT_COMPLETED
```

---

# 59. Payment Success Notification

Exemple :

```text
PAYMENT_SUCCESS
   ├── IN_APP
   ├── PUSH
   └── EMAIL
```

SMS selon les règles définies.

---

# 60. Payment Failure Notification

Exemple :

```text
PAYMENT_FAILED
   ├── IN_APP
   ├── PUSH
   └── EMAIL
```

Le message utilisateur doit être compréhensible.

Ne pas exposer :

```text
stack trace
provider secret
internal error
database details
```

---

# 61. Payment Security

Les données de paiement doivent être protégées.

Ne jamais stocker inutilement :

```text
Full card number
CVV
PIN
Provider secrets
Authentication credentials
Private keys
```

La conception doit minimiser le périmètre PCI lorsque des cartes sont utilisées.

---

# 62. Tokenization

Lorsque le fournisseur le permet, utiliser des tokens plutôt que de conserver des données sensibles.

Exemple :

```text
Customer
   ↓
Provider token
   ↓
MAGIC GOLD
```

MAGIC GOLD ne doit pas stocker une donnée sensible simplement parce que le fournisseur peut la retourner.

---

# 63. Access Control

Permissions conceptuelles :

```text
PAYMENT_SELF_VIEW
PAYMENT_CREATE
PAYMENT_VIEW
PAYMENT_REFUND
PAYMENT_REFUND_APPROVE

SETTLEMENT_VIEW
SETTLEMENT_MANAGE

RECONCILIATION_VIEW
RECONCILIATION_MANAGE

FINANCIAL_REPORT_VIEW
```

Les permissions finales doivent rester cohérentes avec `07_UI_UX_Specification.md`.

---

# 64. Ownership

Pour un CUSTOMER :

```text
payment.customerId
==
authenticatedUser.id
```

Pour un MERCHANT :

```text
payment.merchantId
==
authenticatedMerchant.id
```

La permission seule ne suffit jamais.

---

# 65. Multi-Tenant Security

Aucun marchand ne doit pouvoir accéder aux paiements d'un autre marchand.

Chaque requête doit appliquer :

```text
tenant / merchant scope
```

côté backend.

---

# 66. Payment API

Endpoints conceptuels :

```text
POST /api/payments/intents
GET  /api/payments/{id}
GET  /api/payments
POST /api/payments/{id}/confirm
POST /api/payments/{id}/cancel

POST /api/payments/{id}/refund
GET  /api/payments/{id}/refunds
```

---

# 67. Customer Payment API

Le CUSTOMER peut utiliser :

```text
POST /api/payments/intents
GET /api/payments/{id}
GET /api/me/payments
POST /api/payments/{id}/refund-request
```

selon les règles métier.

---

# 68. Merchant Payment API

Le MERCHANT peut avoir :

```text
GET /api/merchant/payments
GET /api/merchant/payments/{id}
GET /api/merchant/refunds
GET /api/merchant/settlements
```

---

# 69. Finance API

FINANCE peut utiliser :

```text
GET /api/finance/payments
GET /api/finance/refunds
GET /api/finance/settlements
GET /api/finance/reconciliation
POST /api/finance/reconciliation/{id}/resolve
```

Les endpoints doivent être protégés par permissions spécifiques.

---

# 70. Provider Webhook API

Endpoint conceptuel :

```text
POST /api/payment-providers/{provider}/webhook
```

Cet endpoint doit :

```text
authenticate
verify signature
validate payload
deduplicate
process event
return appropriate response
```

---

# 71. Provider Status API

MAGIC GOLD peut interroger un fournisseur lorsque nécessaire :

```text
GET provider payment status
```

Cette fonction doit être utilisée avec parcimonie afin d'éviter les appels inutiles.

---

# 72. Retry Strategy

Pour une erreur temporaire :

```text
Attempt 1
↓
Backoff
↓
Attempt 2
↓
Backoff
↓
Attempt 3
```

Le nombre de tentatives doit être configurable.

---

# 73. Circuit Breaker

Si un fournisseur devient indisponible :

```text
Provider A
   ↓
Failures exceed threshold
   ↓
Circuit OPEN
```

Le système peut :

```text
stop repeated calls
wait
test recovery
close circuit
```

---

# 74. Provider Failover

Si plusieurs fournisseurs sont configurés, le système peut prévoir un mécanisme de fallback.

Mais un paiement déjà initié ne doit pas être automatiquement envoyé à un second fournisseur sans contrôle d'idempotence.

Risque :

```text
Provider A
   ↓
Payment actually processed
   ↓
Timeout
   ↓
Provider B
   ↓
Second payment
```

Le failover financier doit donc être conçu avec prudence.

---

# 75. Risk Controls

Avant certains paiements, le système peut vérifier :

```text
velocity
amount limits
account status
device risk
IP risk
payment method risk
merchant risk
transaction frequency
```

Les règles doivent être configurables.

---

# 76. Payment Limits

Exemples :

```text
minimum transaction amount
maximum transaction amount
daily customer limit
daily merchant limit
velocity limit
```

Ces limites doivent être contrôlées côté backend.

---

# 77. Velocity

Exemple :

```text
10 payment attempts
en 2 minutes
```

peut déclencher une règle de risque.

Le système peut alors :

```text
allow
challenge
delay
block
manual review
```

selon la politique définie.

---

# 78. Payment Expiration

Un Payment Intent peut expirer.

Exemple :

```text
CREATED
   ↓
PENDING
   ↓
EXPIRED
```

Une tentative sur un paiement expiré doit être refusée.

---

# 79. Concurrency

Deux demandes simultanées ne doivent pas pouvoir confirmer deux fois le même paiement.

Utiliser selon l'architecture :

```text
Optimistic locking
Database transaction
Unique constraint
Idempotency
State transition validation
```

---

# 80. Database Constraints

Prévoir notamment :

```text
unique(provider, providerTransactionId)

unique(idempotencyKey, userId)

unique(providerEventId)

foreign keys

positive amount constraint

valid currency
```

Les contraintes exactes seront définies dans `04_DataModel.md`.

---

# 81. Audit

Les événements suivants doivent être audités :

```text
Payment created
Payment initiated
Payment status changed
Payment confirmed
Payment failed
Payment cancelled
Refund requested
Refund approved
Refund executed
Settlement created
Settlement completed
Reconciliation resolved
Manual financial intervention
```

---

# 82. Audit Record

Exemple :

```text
actorId
actorRole
action
resourceType
resourceId
oldState
newState
reason
ip
userAgent
timestamp
correlationId
```

---

# 83. Manual Intervention

Toute intervention manuelle financière doit nécessiter :

```text
reason
actor
timestamp
reference
approval when required
audit
```

Jamais :

```text
UPDATE payments SET status = 'SUCCESS'
```

sans workflow métier.

---

# 84. Financial Immutability

Les données historiques financières ne doivent pas être supprimées.

À éviter :

```text
DELETE payment
DELETE ledger entry
DELETE settlement
```

Préférer :

```text
reversal
adjustment
refund
correction
```

---

# 85. Payment History

Le client doit pouvoir consulter un historique compréhensible :

```text
Date
Reference
Amount
Currency
Status
Merchant
Payment method
```

Les informations sensibles sont masquées.

---

# 86. Merchant Dashboard

Le marchand peut consulter :

```text
Total collected
Successful payments
Failed payments
Refunds
Pending settlements
Completed settlements
Fees
Net revenue
```

Les indicateurs doivent être calculés à partir des données financières officielles.

---

# 87. Finance Dashboard

FINANCE peut consulter :

```text
Gross volume
Net volume
Fees
Refund volume
Settlement volume
Pending reconciliation
Mismatches
Provider failures
```

---

# 88. Payment Search

La recherche peut utiliser :

```text
paymentId
orderReference
providerTransactionId
customer
merchant
status
date range
amount
currency
```

Les champs accessibles dépendent du rôle.

---

# 89. Payment Reconciliation Job

Un job périodique peut :

```text
retrieve provider transactions
      ↓
normalize
      ↓
match
      ↓
detect discrepancy
      ↓
create reconciliation records
      ↓
notify finance
```

---

# 90. Reconciliation Notification

Une anomalie critique peut générer :

```text
RECONCILIATION_MISMATCH
```

vers :

```text
FINANCE
ADMIN
```

selon les règles.

---

# 91. Observability

Métriques :

```text
payments_created_total
payments_success_total
payments_failed_total
payments_cancelled_total
payments_refunded_total

payment_success_rate
payment_failure_rate
payment_latency

provider_timeout_total
provider_error_total
webhook_received_total
webhook_duplicate_total

refund_total
refund_failure_total

reconciliation_mismatch_total
```

---

# 92. Logs

Les logs doivent permettre de reconstruire une opération sans exposer de données sensibles.

Utiliser :

```text
correlationId
paymentId
orderId
provider
providerTransactionId
eventId
```

Ne jamais logger :

```text
card credentials
CVV
OTP
access tokens
provider secrets
```

---

# 93. Alerting

Alertes possibles :

```text
Payment failure rate high
Provider unavailable
Webhook backlog
Webhook signature failures
Duplicate payment attempts
Refund failure rate high
Settlement failure
Reconciliation mismatch
```

---

# 94. Tests Unitaires

Tester :

```text
amount validation
currency validation
state transitions
refund limits
commission calculation
fee calculation
idempotency
ownership
permission
risk rules
```

---

# 95. Tests d'intégration

Tester :

```text
Order → Payment
Payment → Provider
Provider → Webhook
Payment → Ledger
Payment → Notification
Payment → Settlement
Refund → Provider
Refund → Ledger
Reconciliation
```

---

# 96. Tests de sécurité

Tester :

```text
Unauthorized payment access
Cross-customer access
Cross-merchant access
Webhook forgery
Webhook replay
Idempotency bypass
Amount manipulation
Currency manipulation
Payment state manipulation
Refund over-limit
Privilege escalation
```

---

# 97. Tests de concurrence

Scénarios :

```text
Two payment requests
Two webhooks
Two refund requests
Payment + cancellation simultaneously
Payment + refund simultaneously
```

Le résultat doit rester financièrement cohérent.

---

# 98. Tests de panne

Simuler :

```text
Provider timeout
Provider unavailable
Network timeout
Webhook delayed
Webhook duplicated
Database retry
Worker restart
Queue outage
```

---

# 99. Tests de réconciliation

Scénarios :

```text
Perfect match
Missing provider transaction
Missing internal transaction
Amount mismatch
Currency mismatch
Duplicate provider transaction
Late settlement
```

---

# 100. Definition of Done

Le module Payment est considéré terminé lorsque :

```text
[ ] Payment domain implemented
[ ] Payment states defined
[ ] State transitions protected
[ ] Idempotency implemented
[ ] Provider abstraction implemented
[ ] Provider adapter implemented
[ ] Webhook verification implemented
[ ] Webhook idempotency implemented
[ ] Order/payment relationship implemented
[ ] Refund implemented
[ ] Partial refund supported if required
[ ] Commission model implemented
[ ] Fee model implemented
[ ] Ledger integration implemented
[ ] Settlement model implemented
[ ] Reconciliation implemented
[ ] Notifications integrated
[ ] Audit implemented
[ ] Security tests passed
[ ] Concurrency tests passed
[ ] Failure tests passed
[ ] Observability implemented
```

---

# 101. Règles Hermes

Avant toute implémentation de paiement, Hermes doit obligatoirement vérifier :

```text
1. Quel acteur initie l'opération ?
2. Quelle commande est concernée ?
3. Quel montant doit être payé ?
4. Quelle devise ?
5. Quel payment intent ?
6. Quelle clé d'idempotence ?
7. Quel fournisseur ?
8. Quel état actuel ?
9. Quelle transition est autorisée ?
10. Quelle preuve confirme le paiement ?
11. Quel événement doit être publié ?
12. Quel ledger impact ?
13. Quel settlement ?
14. Quelle notification ?
15. Quel audit ?
16. Quels scénarios de panne ?
17. Quels contrôles antifraude ?
18. Quels tests de concurrence ?
```

Hermes ne doit jamais implémenter un paiement en se basant uniquement sur le comportement du frontend.

---

# 102. Règle de non-contournement

Les règles suivantes sont interdites :

```text
Frontend → SUCCESS sans confirmation backend

Client → modification payment.status

Merchant → modification ledger

Support → modification financière directe

Webhook → SUCCESS sans vérification

Retry → nouveau paiement sans idempotence

Refund → montant supérieur au montant disponible

Admin → suppression d'une transaction historique
```

---

# 103. Relation avec les autres documents

```text
04_DataModel.md
      ↓
Payment entities

05_API_Specification.md
      ↓
Payment endpoints

06_SecurityArchitecture.md
      ↓
Authentication / Authorization

07_UI_UX_Specification.md
      ↓
Payment screens / permissions

08_Notifications.md
      ↓
Payment events / notifications

09_PaymentArchitecture.md
      ↓
Payment business + technical architecture
```

Les documents doivent rester cohérents.

---

# 104. Architecture finale

```text
                         CUSTOMER
                            │
                            ▼
                         ORDER
                            │
                            ▼
                     PAYMENT INTENT
                            │
                            ▼
                    PAYMENT SERVICE
                            │
             ┌──────────────┼──────────────┐
             ▼              ▼              ▼
          Security       Risk Rules     Idempotency
             │              │              │
             └──────────────┼──────────────┘
                            ▼
                    PAYMENT ORCHESTRATOR
                            │
                 ┌──────────┼──────────┐
                 ▼          ▼          ▼
             Provider A Provider B Provider C
                 │          │          │
                 └──────────┼──────────┘
                            ▼
                         WEBHOOK
                            │
                            ▼
                    PAYMENT CONFIRMATION
                            │
              ┌─────────────┼─────────────┐
              ▼             ▼             ▼
           ORDER         LEDGER       SETTLEMENT
                            │             │
                            └──────┬──────┘
                                   ▼
                            RECONCILIATION
                                   │
                                   ▼
                                AUDIT

Payment Event
      │
      ▼
Notification Service
      │
      ├── IN_APP
      ├── EMAIL
      ├── SMS
      └── PUSH
```

---

# 105. Règle finale

Le principe fondamental de MAGIC GOLD est :

> **Un paiement n'est considéré comme réussi que lorsque MAGIC GOLD possède une preuve valide et vérifiée de son succès.**

Et :

> **Aucun acteur, aucune interface et aucun webhook non vérifié ne doit pouvoir transformer artificiellement une opération financière en succès.**

Enfin :

```text
Payment
≠
Order

Payment
≠
Ledger

Payment
≠
Settlement

Payment
≠
Notification
```

Ces domaines doivent rester distincts tout en étant reliés par des événements métier fiables, idempotents et auditables.
