# MAGIC GOLD — Order Management Specification

**Fichier :** `10_OrderManagement.md`
**Version :** 1.0
**Statut :** Specification
**Produit :** MAGIC GOLD
**Éditeur :** Nectux Ingénieries

---

# 1. Objectif

Ce document définit les règles fonctionnelles, métier et techniques relatives à la **gestion des commandes** dans MAGIC GOLD.

Le module Order Management couvre :

* création d'une commande ;
* panier et lignes de commande ;
* calcul des montants ;
* application des prix ;
* frais éventuels ;
* validation ;
* paiement ;
* annulation ;
* remboursement ;
* suivi du traitement ;
* historique ;
* notifications ;
* gestion des commandes côté client ;
* gestion des commandes côté marchand ;
* traçabilité et audit.

La commande constitue le **pivot commercial** entre l'acheteur et le marchand.

---

# 2. Principe fondamental

Une commande n'est pas un paiement.

```text
CUSTOMER
   │
   ▼
CATALOGUE
   │
   ▼
PANIER
   │
   ▼
COMMANDE
   │
   ├──────────────► PAIEMENT
   │                    │
   │                    ▼
   │                 WALLET
   │                    │
   │                    ▼
   │                  LEDGER
   │
   ▼
TRAITEMENT MARCHAND
   │
   ▼
LIVRAISON / EXÉCUTION
   │
   ▼
COMPLÉTION
```

Les responsabilités doivent rester séparées.

| Domaine      | Responsabilité               |
| ------------ | ---------------------------- |
| Product      | Produit/service vendu        |
| Cart         | Sélection temporaire         |
| Order        | Engagement commercial        |
| Payment      | Paiement de la commande      |
| Wallet       | Solde financier              |
| Ledger       | Trace comptable/financière   |
| Merchant     | Traitement de la commande    |
| Delivery     | Livraison/exécution          |
| Notification | Information de l'utilisateur |
| Audit        | Traçabilité                  |

---

# 3. Acteurs

Les acteurs concernés sont :

* CUSTOMER
* MERCHANT
* MERCHANT_OPERATOR
* SUPPORT
* FINANCE
* AUDITOR
* ADMIN
* SUPER_ADMIN
* SYSTEM

---

# 4. Concepts

## 4.1 Cart

Le panier représente une sélection temporaire de produits ou services.

Il peut être modifié avant création de la commande.

## 4.2 Order

La commande représente l'engagement commercial créé à partir du panier.

Une commande possède un identifiant unique.

Exemple :

```text
MG-2026-00001245
```

## 4.3 Order Item

Chaque ligne de commande représente un produit ou service.

Exemple :

```text
Produit : Sac premium
Quantité : 2
Prix unitaire : 15 000 XOF
Total : 30 000 XOF
```

---

# 5. Identifiant de commande

Chaque commande doit posséder :

* `id` technique ;
* `orderNumber` public ;
* `customerId` ;
* `merchantId` ;
* `createdAt`.

`orderNumber` doit être :

* unique ;
* non prédictible si exposé publiquement ;
* lisible ;
* stable ;
* immuable.

---

# 6. Cycle de vie

Le cycle principal est :

```text
DRAFT
   │
   ▼
PENDING_PAYMENT
   │
   ├────► CANCELLED
   │
   ▼
PAID
   │
   ▼
CONFIRMED
   │
   ▼
PROCESSING
   │
   ▼
READY
   │
   ▼
COMPLETED
```

Des branches exceptionnelles existent :

```text
PENDING_PAYMENT ──► PAYMENT_FAILED
PAID ─────────────► REFUND_PENDING
PAID ─────────────► CANCELLED
CONFIRMED ────────► CANCELLED
PROCESSING ───────► CANCELLED
```

---

# 7. États de commande

## 7.1 DRAFT

Commande en construction.

Caractéristiques :

* non définitive ;
* modifiable ;
* non payable ;
* non visible comme commande définitive au marchand.

---

## 7.2 PENDING_PAYMENT

La commande est créée mais le paiement n'est pas encore confirmé.

Elle contient un montant payable déterminé.

---

## 7.3 PAYMENT_FAILED

Le paiement associé a échoué.

La commande peut :

* être repassée en paiement ;
* être annulée ;
* expirer automatiquement.

---

## 7.4 PAID

Le paiement a été confirmé.

La commande ne doit pas être considérée comme annulable automatiquement sans appliquer les règles métier correspondantes.

---

## 7.5 CONFIRMED

Le marchand a accepté la commande.

---

## 7.6 PROCESSING

Le marchand traite la commande.

---

## 7.7 READY

La commande est prête pour livraison ou retrait.

---

## 7.8 COMPLETED

La commande a été exécutée avec succès.

---

## 7.9 CANCELLED

La commande a été annulée.

Une commande annulée ne doit pas être réactivée directement.

Une nouvelle commande doit être créée si nécessaire.

---

## 7.10 REFUND_PENDING

Le remboursement est en cours.

---

## 7.11 REFUNDED

Le remboursement a été effectué.

---

# 8. Matrice des transitions

| État actuel     | Action           | État suivant               |
| --------------- | ---------------- | -------------------------- |
| DRAFT           | Submit           | PENDING_PAYMENT            |
| PENDING_PAYMENT | Payment success  | PAID                       |
| PENDING_PAYMENT | Payment failure  | PAYMENT_FAILED             |
| PENDING_PAYMENT | Cancel           | CANCELLED                  |
| PAYMENT_FAILED  | Retry            | PENDING_PAYMENT            |
| PAID            | Merchant confirm | CONFIRMED                  |
| PAID            | Cancel           | CANCELLED / REFUND_PENDING |
| CONFIRMED       | Start processing | PROCESSING                 |
| PROCESSING      | Ready            | READY                      |
| READY           | Complete         | COMPLETED                  |
| PAID            | Refund           | REFUND_PENDING             |
| REFUND_PENDING  | Refund success   | REFUNDED                   |

Toute transition non prévue doit être refusée.

---

# 9. Règle d'or des transitions

Le backend est la seule autorité capable de changer l'état d'une commande.

Le frontend ne doit jamais envoyer :

```json
{
  "status": "COMPLETED"
}
```

pour forcer un changement.

Le frontend doit demander une action :

```http
POST /api/orders/{id}/complete
```

Le backend vérifie :

1. identité ;
2. permission ;
3. propriétaire ;
4. état courant ;
5. conditions métier ;
6. paiement ;
7. rôle du marchand ;
8. éventuelle livraison ;
9. audit.

---

# 10. Panier

Le panier est associé au client.

Il contient :

```text
Cart
 ├── customer
 ├── merchant
 ├── items
 ├── subtotal
 ├── fees
 ├── discount
 └── total
```

Une commande peut être créée à partir du panier.

---

# 11. Ligne de commande

Chaque ligne doit conserver un snapshot commercial.

```text
OrderItem
-------------------------
id
orderId
productId
productNameSnapshot
skuSnapshot
unitPrice
quantity
discount
tax
lineTotal
createdAt
```

Le nom et le prix historiques ne doivent pas dépendre du catalogue actuel.

---

# 12. Snapshot commercial

Lors de la création de la commande, MAGIC GOLD doit conserver :

* nom du produit ;
* référence ;
* prix ;
* quantité ;
* remise ;
* taxes éventuelles ;
* frais ;
* devise ;
* informations nécessaires à l'historique.

Si le marchand modifie ensuite le produit, la commande historique ne doit pas changer.

---

# 13. Prix

Le prix final d'une commande doit être calculé côté serveur.

```text
subtotal
- discounts
+ taxes
+ fees
----------------
total
```

Le client ne doit jamais être autorisé à imposer le montant final.

---

# 14. Calcul du montant

Exemple :

```text
Produit A
2 × 10 000 = 20 000 XOF

Produit B
1 × 15 000 = 15 000 XOF

Sous-total = 35 000 XOF

Frais = 1 000 XOF

Total = 36 000 XOF
```

Le montant doit être recalculé avant validation.

---

# 15. Devise

La devise doit être explicite.

Exemple :

```text
currency = XOF
```

Les montants doivent être stockés sous forme entière.

Exemple :

```text
amount = 36000
currency = XOF
```

Éviter les calculs financiers en `float` ou `double`.

---

# 16. Création d'une commande

Flux :

```text
CUSTOMER
   │
   ▼
Panier
   │
   ▼
Validation
   │
   ▼
Backend
   │
   ├── Vérifier produit
   ├── Vérifier disponibilité
   ├── Vérifier prix
   ├── Calculer montant
   ├── Appliquer règles
   └── Créer commande
   │
   ▼
PENDING_PAYMENT
```

---

# 17. Vérifications avant création

Le backend vérifie :

* client actif ;
* marchand actif ;
* produit actif ;
* disponibilité ;
* prix actuel ;
* quantité autorisée ;
* devise ;
* restrictions éventuelles ;
* règles commerciales.

---

# 18. Stock

La gestion complète de stock peut être indépendante du module Order Management.

MAGIC GOLD doit néanmoins prévoir :

```text
availableQuantity
reservedQuantity
```

Une commande peut provoquer une réservation.

---

# 19. Réservation

Lorsque nécessaire :

```text
AVAILABLE
    │
    ▼
RESERVED
    │
    ├── Order cancelled → RELEASED
    │
    └── Order completed → CONSUMED
```

La réservation doit être atomique.

---

# 20. Paiement

Une commande peut être associée à un ou plusieurs objets de paiement selon le modèle retenu.

Relation :

```text
ORDER 1 ───── N PAYMENT
```

Le paiement doit référencer :

```text
orderId
amount
currency
paymentMethod
paymentStatus
providerReference
```

Le montant payé doit être vérifié côté serveur.

---

# 21. Condition de paiement

Une commande ne peut passer à `PAID` que lorsqu'un paiement valide et confirmé existe.

```text
payment.status == SUCCESS
```

Le simple retour d'un frontend :

```text
success = true
```

ne constitue jamais une preuve de paiement.

---

# 22. Paiement partiel

Par défaut, MAGIC GOLD doit considérer :

```text
PAYMENT_REQUIRED = TOTAL_ORDER_AMOUNT
```

Le paiement partiel n'est activé que si le métier le prévoit explicitement.

---

# 23. Paiement multiple

Une commande peut éventuellement supporter :

```text
Wallet
+
Digital Payment
```

mais cette capacité doit être explicitement activée.

Le moteur doit éviter :

```text
paidAmount > orderTotal
```

sauf cas contrôlé de remboursement/surplus.

---

# 24. Annulation avant paiement

Une commande `PENDING_PAYMENT` peut être annulée selon les règles métier.

```text
PENDING_PAYMENT
       │
       ▼
CANCELLED
```

Aucune opération financière de remboursement n'est nécessaire si aucun paiement n'a été encaissé.

---

# 25. Annulation après paiement

Une commande déjà payée nécessite une logique financière.

```text
PAID
 │
 ▼
Cancellation request
 │
 ▼
Business validation
 │
 ▼
Refund
 │
 ▼
REFUNDED
```

L'annulation commerciale et le remboursement financier doivent rester deux opérations distinctes.

---

# 26. Remboursement

Un remboursement doit référencer :

```text
orderId
paymentId
refundId
amount
reason
actor
createdAt
```

Le montant remboursé ne doit jamais dépasser le montant effectivement payé et non déjà remboursé.

---

# 27. Remboursement partiel

Un remboursement partiel peut être supporté :

```text
orderTotal = 100 000
refund = 30 000
remainingRefundable = 70 000
```

La somme des remboursements ne doit jamais dépasser le montant remboursable.

---

# 28. Commande et Wallet

Si le client paie avec le Wallet :

```text
ORDER
  │
  ▼
PAYMENT
  │
  ▼
WALLET DEBIT
  │
  ▼
LEDGER
```

Le module Order ne doit pas modifier directement le solde du Wallet.

---

# 29. Atomicité paiement/commande

Une commande payée doit être cohérente avec son paiement.

Le système doit éviter :

```text
Order = PAID
Payment = FAILED
```

ou :

```text
Order = PENDING_PAYMENT
Payment = SUCCESS
```

sans traitement de synchronisation.

Les incohérences doivent être détectées et réconciliées.

---

# 30. Commande et marchand

Chaque commande doit identifier son marchand :

```text
merchantId
```

Le marchand ne peut accéder qu'aux commandes relevant de son périmètre.

---

# 31. Commandes multi-marchands

Pour la V1, il est recommandé qu'une commande appartienne à **un seul marchand**.

```text
Order
 └── Merchant
      └── Items
```

Une commande multi-marchands pourrait être introduite ultérieurement via :

```text
ParentOrder
   ├── MerchantOrder A
   ├── MerchantOrder B
   └── MerchantOrder C
```

Cette complexité n'est pas nécessaire pour le socle V1.

---

# 32. Règles CUSTOMER

Le CUSTOMER peut :

* consulter ses commandes ;
* créer une commande ;
* modifier son panier ;
* soumettre une commande ;
* payer ;
* annuler selon les règles ;
* demander un remboursement ;
* consulter l'historique ;
* recevoir les notifications ;
* consulter le détail d'une commande.

Le CUSTOMER ne peut pas :

* modifier directement le statut ;
* modifier le prix ;
* modifier le total ;
* confirmer une commande comme marchand ;
* modifier le paiement ;
* modifier le Ledger.

---

# 33. Règles MERCHANT

Le MERCHANT peut :

* consulter ses commandes ;
* confirmer une commande ;
* refuser une commande selon les règles ;
* démarrer le traitement ;
* signaler qu'elle est prête ;
* terminer l'exécution ;
* consulter les paiements associés ;
* consulter les remboursements autorisés.

Le MERCHANT ne peut pas :

* modifier le montant payé ;
* créditer directement son Wallet ;
* modifier le Ledger ;
* supprimer une commande ;
* déclarer arbitrairement un paiement réussi.

---

# 34. Règles MERCHANT_OPERATOR

Le MERCHANT_OPERATOR dispose uniquement des permissions explicitement attribuées.

Exemple :

```text
ORDER_VIEW
ORDER_CONFIRM
ORDER_PROCESS
ORDER_COMPLETE
```

Une permission financière telle que :

```text
PAYMENT_REFUND
```

ne doit pas être implicitement accordée.

---

# 35. Règles SUPPORT

SUPPORT peut :

* rechercher une commande ;
* consulter son historique ;
* consulter les erreurs ;
* consulter les paiements associés ;
* assister le client ;
* ouvrir un ticket.

SUPPORT ne peut pas :

* modifier financièrement une commande ;
* modifier le Ledger ;
* forcer un paiement ;
* forcer un remboursement sans permission spécifique.

---

# 36. Règles FINANCE

FINANCE peut :

* consulter les commandes financières ;
* consulter les paiements ;
* consulter les remboursements ;
* effectuer les opérations financières autorisées ;
* réconcilier commandes et paiements ;
* analyser les anomalies.

Toute correction financière doit être :

* autorisée ;
* justifiée ;
* auditée ;
* traçable.

---

# 37. Règles AUDITOR

AUDITOR est en lecture seule.

Il peut consulter :

* commandes ;
* transitions ;
* paiements ;
* remboursements ;
* acteurs ;
* dates ;
* audit ;
* références financières.

Il ne doit pas modifier les données.

---

# 38. Règles ADMIN

ADMIN peut gérer :

* paramètres de commande ;
* règles configurables ;
* limites ;
* catalogue selon permissions ;
* utilisateurs selon permissions.

ADMIN ne doit pas obtenir automatiquement des droits financiers.

---

# 39. Règles SUPER_ADMIN

SUPER_ADMIN peut réaliser certaines opérations critiques explicitement autorisées.

Toute opération exceptionnelle doit enregistrer :

```text
actor
action
reason
timestamp
resource
before
after
correlationId
```

---

# 40. Règles SYSTEM

SYSTEM peut :

* créer des événements ;
* mettre à jour certaines transitions automatiques ;
* expirer une commande ;
* déclencher des remboursements selon workflow ;
* synchroniser les paiements ;
* lancer la réconciliation ;
* envoyer des notifications.

SYSTEM ne doit jamais contourner les règles de sécurité.

---

# 41. Expiration

Une commande `PENDING_PAYMENT` peut avoir une durée de validité.

Exemple configurable :

```text
orderPaymentExpirationMinutes = N
```

À expiration :

```text
PENDING_PAYMENT
       │
       ▼
CANCELLED
```

La valeur exacte doit être configurable.

---

# 42. Idempotence

Les opérations critiques doivent être idempotentes.

Exemples :

```text
Create Order
Confirm Order
Pay Order
Cancel Order
Refund Order
Complete Order
```

Chaque opération critique peut accepter :

```http
Idempotency-Key: <unique-key>
```

Une répétition de la même requête ne doit pas créer une deuxième opération commerciale ou financière.

---

# 43. Concurrence

Le système doit empêcher :

```text
Deux confirmations simultanées
Deux annulations simultanées
Deux remboursements simultanés
Deux consommations de stock simultanées
```

Les mécanismes possibles sont :

* optimistic locking ;
* versioning ;
* database transaction ;
* contraintes SQL ;
* verrouillage ciblé.

---

# 44. Historique des statuts

Chaque changement de statut doit être conservé.

```text
OrderStatusHistory
-------------------------
id
orderId
fromStatus
toStatus
actorId
actorType
reason
createdAt
correlationId
```

L'historique est immuable.

---

# 45. Audit

Les événements importants doivent être audités :

* création ;
* modification ;
* paiement ;
* confirmation ;
* annulation ;
* remboursement ;
* changement de statut ;
* intervention administrative.

---

# 46. Notifications

Les événements Order doivent alimenter le système de notifications.

Exemples :

```text
ORDER_CREATED
ORDER_PAYMENT_SUCCESS
ORDER_PAYMENT_FAILED
ORDER_CONFIRMED
ORDER_PROCESSING
ORDER_READY
ORDER_COMPLETED
ORDER_CANCELLED
ORDER_REFUND_INITIATED
ORDER_REFUNDED
```

Le module Order génère l'événement.

Le module Notification décide du canal.

---

# 47. Deep links

Les notifications peuvent contenir :

```text
/app/orders/{orderId}
```

ou une route équivalente.

Le backend doit vérifier l'autorisation avant d'afficher les données.

---

# 48. API — Customer

### Liste

```http
GET /api/orders
```

### Détail

```http
GET /api/orders/{id}
```

### Création

```http
POST /api/orders
```

### Annulation

```http
POST /api/orders/{id}/cancel
```

### Demande de remboursement

```http
POST /api/orders/{id}/refund-request
```

---

# 49. API — Merchant

```http
GET /api/merchant/orders
```

```http
GET /api/merchant/orders/{id}
```

```http
POST /api/merchant/orders/{id}/confirm
```

```http
POST /api/merchant/orders/{id}/processing
```

```http
POST /api/merchant/orders/{id}/ready
```

```http
POST /api/merchant/orders/{id}/complete
```

---

# 50. API — Finance

```http
GET /api/finance/orders
```

```http
GET /api/finance/orders/{id}
```

```http
GET /api/finance/orders/{id}/payments
```

```http
GET /api/finance/orders/{id}/refunds
```

---

# 51. API — Administration

```http
GET /api/admin/orders
```

```http
GET /api/admin/orders/{id}/audit
```

Les permissions doivent être vérifiées côté backend.

---

# 52. Modèle de données

Entité principale :

```text
Order
-------------------------
id
orderNumber
customerId
merchantId
status
currency
subtotal
discountAmount
taxAmount
feeAmount
totalAmount
paidAmount
refundedAmount
expiresAt
createdAt
updatedAt
completedAt
version
```

---

# 53. OrderItem

```text
OrderItem
-------------------------
id
orderId
productId
productNameSnapshot
skuSnapshot
quantity
unitPrice
discountAmount
taxAmount
lineTotal
createdAt
```

---

# 54. OrderStatusHistory

```text
OrderStatusHistory
-------------------------
id
orderId
fromStatus
toStatus
actorId
actorType
reason
createdAt
correlationId
```

---

# 55. OrderPaymentSummary

La commande peut conserver un résumé calculé :

```text
totalAmount
paidAmount
refundedAmount
remainingAmount
```

Mais les données financières détaillées restent dans le domaine Payment.

---

# 56. Intégrité financière

Règles :

```text
paidAmount <= totalAmount
```

et :

```text
refundedAmount <= paidAmount
```

et :

```text
remainingRefundable = paidAmount - refundedAmount
```

Toute violation doit déclencher une erreur critique.

---

# 57. Suppression

Une commande financière ne doit pas être supprimée physiquement.

Même une commande annulée doit rester historisée.

Privilégier :

```text
status = CANCELLED
```

plutôt que :

```text
DELETE
```

---

# 58. Confidentialité

Un CUSTOMER ne doit voir que ses propres commandes.

Un MERCHANT ne doit voir que ses propres commandes.

SUPPORT, FINANCE, AUDITOR et ADMIN accèdent aux données selon leurs permissions et périmètres.

---

# 59. Pagination

Les listes doivent être paginées.

Exemple :

```http
GET /api/orders?page=0&size=20
```

Le backend doit imposer une taille maximale.

---

# 60. Filtres

Le système doit prévoir :

```text
status
dateFrom
dateTo
merchant
customer
orderNumber
paymentStatus
```

Les filtres administratifs doivent être protégés.

---

# 61. Recherche

La recherche par `orderNumber` doit être optimisée.

Index recommandé :

```text
UNIQUE(order_number)
```

---

# 62. Performance

Les pages de commandes ne doivent pas charger :

* toutes les lignes ;
* toutes les notifications ;
* tous les audits ;
* tous les paiements ;

en une seule requête.

Utiliser :

* pagination ;
* projections ;
* chargement ciblé ;
* requêtes optimisées.

---

# 63. Machine à états

La logique de transition doit être centralisée.

Exemple conceptuel :

```text
OrderStateMachine
```

Elle doit contrôler :

```text
currentState
action
actor
permissions
businessRules
nextState
```

---

# 64. Événements métier

Le module peut publier :

```text
ORDER_CREATED
ORDER_SUBMITTED
ORDER_PAYMENT_PENDING
ORDER_PAID
ORDER_CONFIRMED
ORDER_PROCESSING
ORDER_READY
ORDER_COMPLETED
ORDER_CANCELLED
ORDER_REFUND_REQUESTED
ORDER_REFUNDED
ORDER_EXPIRED
```

---

# 65. Outbox

Les événements importants doivent utiliser le pattern Outbox.

```text
Order Transaction
      │
      ├── Update Order
      │
      └── Insert Outbox Event
                  │
                  ▼
             Event Publisher
                  │
       ┌──────────┼──────────┐
       ▼          ▼          ▼
   Payment    Notification  Reporting
```

L'événement ne doit pas être publié avant que la transaction métier soit correctement enregistrée.

---

# 66. Gestion des erreurs

Exemples :

```text
ORDER_NOT_FOUND
ORDER_INVALID_STATE
ORDER_NOT_OWNER
MERCHANT_NOT_AUTHORIZED
PRODUCT_UNAVAILABLE
INVALID_QUANTITY
PRICE_CHANGED
PAYMENT_REQUIRED
PAYMENT_NOT_CONFIRMED
REFUND_NOT_ALLOWED
REFUND_AMOUNT_EXCEEDED
ORDER_EXPIRED
```

---

# 67. Réponse API standard

Exemple succès :

```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "orderNumber": "MG-2026-00001245",
    "status": "PENDING_PAYMENT",
    "totalAmount": 36000,
    "currency": "XOF"
  }
}
```

Exemple erreur :

```json
{
  "success": false,
  "error": {
    "code": "ORDER_INVALID_STATE",
    "message": "Cette commande ne peut pas être annulée dans son état actuel."
  }
}
```

---

# 68. Interface client

L'écran commande doit présenter :

```text
Numéro commande
Date
Marchand
Articles
Quantités
Sous-total
Frais
Total
Paiement
Statut
Historique
Actions disponibles
```

---

# 69. Interface marchand

Le marchand doit disposer de :

```text
Commandes reçues
Commandes à traiter
Commandes en cours
Commandes prêtes
Commandes terminées
Commandes annulées
```

Avec filtres et recherche.

---

# 70. Timeline

Le client et le marchand peuvent voir une timeline :

```text
✓ Commande créée
✓ Paiement confirmé
✓ Commande confirmée
✓ Préparation
● Prête
○ Terminée
```

La timeline doit être basée sur les événements réels.

---

# 71. Commandes et notifications

Le système doit éviter les notifications dupliquées.

Exemple :

```text
ORDER_PAID
```

ne doit pas provoquer deux SMS identiques à cause d'un retry technique.

L'idempotence du système Notification doit être utilisée.

---

# 72. Reporting

Le module doit alimenter les indicateurs :

```text
Nombre de commandes
Commandes payées
Commandes annulées
Commandes complétées
Montant des commandes
Montant payé
Montant remboursé
Panier moyen
Taux d'annulation
Taux de complétion
```

Ces indicateurs sont des données dérivées.

Ils ne doivent pas devenir la source financière primaire.

---

# 73. Réconciliation

Des jobs peuvent comparer :

```text
Order
vs
Payment
vs
Wallet
vs
Ledger
```

Exemple :

```text
Order total      = 50 000
Payment success  = 50 000
Wallet debit     = 50 000
Ledger debit     = 50 000
```

Toute divergence doit être signalée.

---

# 74. Sécurité

Les endpoints doivent appliquer :

```text
Authentication
      ↓
User active
      ↓
Permission
      ↓
Merchant / Customer scope
      ↓
Resource ownership
      ↓
Order state
      ↓
Business rules
      ↓
Action
      ↓
Audit
```

---

# 75. Règle de séparation des responsabilités

Le code ne doit pas transformer :

```text
OrderService
```

en un service qui gère :

* paiement ;
* wallet ;
* ledger ;
* notifications ;
* catalogue ;
* livraison ;

directement.

Préférer :

```text
OrderService
   │
   ├── Product/Catalog service
   ├── Payment service
   ├── Wallet service
   ├── Notification service
   └── Delivery service
```

via interfaces et événements appropriés.

---

# 76. Tests unitaires

Tester notamment :

* création ;
* calcul du total ;
* remise ;
* taxes ;
* frais ;
* quantité ;
* transitions ;
* expiration ;
* annulation ;
* remboursement ;
* permissions ;
* ownership.

---

# 77. Tests d'intégration

Tester :

```text
Order → Payment
Order → Wallet
Order → Notification
Order → Merchant
Order → Refund
```

---

# 78. Tests de concurrence

Tester :

```text
Double submit
Double payment callback
Double cancellation
Double refund
Double completion
```

Le système doit rester cohérent.

---

# 79. Tests de sécurité

Tester :

* accès à une commande d'un autre client ;
* accès à une commande d'un autre marchand ;
* modification directe du statut ;
* falsification du montant ;
* remboursement excessif ;
* contournement des permissions ;
* replay d'une requête ;
* absence d'Idempotency-Key.

---

# 80. Tests de réconciliation

Simuler :

```text
Order = PAID
Payment = SUCCESS
Wallet = DEBITED
Ledger = missing
```

Le système doit détecter l'anomalie.

---

# 81. Definition of Done

Le module Order Management est considéré terminé lorsque :

* [ ] le panier peut créer une commande ;
* [ ] le total est calculé côté serveur ;
* [ ] les snapshots produits sont conservés ;
* [ ] les transitions sont contrôlées ;
* [ ] les commandes sont liées au client ;
* [ ] les commandes sont liées au marchand ;
* [ ] le paiement est séparé de la commande ;
* [ ] le Wallet est séparé de la commande ;
* [ ] les remboursements sont contrôlés ;
* [ ] l'idempotence est implémentée ;
* [ ] la concurrence est maîtrisée ;
* [ ] l'audit est disponible ;
* [ ] les événements Outbox sont disponibles ;
* [ ] les notifications sont intégrées ;
* [ ] les permissions sont appliquées ;
* [ ] les tests unitaires sont présents ;
* [ ] les tests d'intégration sont présents ;
* [ ] les tests de sécurité sont présents ;
* [ ] les tests de concurrence sont présents ;
* [ ] la réconciliation est possible.

---

# 82. Règles Hermes

Hermes doit considérer ce document comme la référence fonctionnelle du module Order Management.

Hermes doit respecter les règles suivantes :

### Règle 1

Ne jamais confondre :

```text
Order
Payment
Wallet
Ledger
```

### Règle 2

Ne jamais permettre au frontend de modifier directement le statut.

### Règle 3

Ne jamais accepter le montant fourni par le client comme source de vérité.

### Règle 4

Ne jamais modifier directement le solde Wallet depuis Order.

### Règle 5

Ne jamais supprimer physiquement une commande financière.

### Règle 6

Toute transition doit être explicitement autorisée.

### Règle 7

Toute opération financière doit être idempotente.

### Règle 8

Toute correction doit être traçable.

### Règle 9

Toute donnée historique commerciale doit être conservée sous forme de snapshot.

### Règle 10

Les règles de sécurité backend restent obligatoires même si le frontend masque une action.

---

# 83. Relations avec les autres spécifications

Le module Order Management doit respecter :

```text
04_DataModel.md
        │
        ▼
05_API_Specification.md
        │
        ▼
06_SecurityArchitecture.md
        │
        ▼
07_UI_UX_Specification.md
        │
        ▼
08_Notifications.md
        │
        ▼
09_PaymentArchitecture.md
        │
        ▼
10_WalletSpecification.md
        │
        ▼
11_OrderManagement.md
```

Les documents suivants doivent exploiter Order Management :

```text
12_MerchantSpecification.md
13_CustomerSpecification.md
14_Roles_Permissions_Audit.md
15_Reporting.md
16_IntegrationSpecification.md
17_TestStrategy.md
18_Deployment.md
```

---

# 84. Architecture cible

```text
                    MAGIC GOLD
                        │
        ┌───────────────┼────────────────┐
        │               │                │
        ▼               ▼                ▼
    CUSTOMER        MERCHANT          ADMIN
        │               │
        └───────┬───────┘
                ▼
             ORDER
                │
       ┌────────┼─────────┐
       │        │         │
       ▼        ▼         ▼
   PRODUCT   PAYMENT   DELIVERY
                │
                ▼
             WALLET
                │
                ▼
              LEDGER
                │
                ▼
           RECONCILIATION
```

---

# 85. Principe final

La commande représente **l'engagement commercial**.

Le paiement représente **le règlement financier**.

Le Wallet représente **le compte de valeur utilisable**.

Le Ledger représente **la trace financière immuable**.

La livraison représente **l'exécution de l'engagement**.

La notification représente **l'information de l'utilisateur**.

Ces responsabilités doivent rester séparées.

**MAGIC GOLD doit privilégier une architecture où chaque domaine possède une responsabilité claire, une source de vérité identifiable et des règles d'accès explicites.**
