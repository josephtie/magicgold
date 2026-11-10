# MAGIC GOLD — Merchant Specification

**Fichier :** `11_MerchantSpecification.md`
**Version :** 1.0
**Statut :** Specification V1
**Produit :** MAGIC GOLD
**Éditeur :** Nectux Ingénieries

---

# 1. Objectif

Ce document définit les fonctionnalités, règles métier, données, permissions et comportements liés au **Marchand** dans MAGIC GOLD.

Le Merchant constitue l'acteur commercial qui utilise MAGIC GOLD pour :

* présenter ses produits ou services ;
* gérer son catalogue ;
* recevoir des commandes ;
* traiter les commandes ;
* suivre ses paiements ;
* suivre son activité commerciale ;
* gérer ses collaborateurs ;
* consulter ses statistiques ;
* gérer les paramètres de son activité.

---

# 2. Vision du Merchant

MAGIC GOLD doit permettre à un commerçant de disposer d'un espace commercial numérique simple.

```text
MERCHANT
   │
   ├── Profil entreprise
   │
   ├── Boutique
   │
   ├── Catalogue
   │    ├── Catégories
   │    └── Produits
   │
   ├── Commandes
   │
   ├── Paiements
   │
   ├── Clients
   │
   ├── Collaborateurs
   │
   ├── Rapports
   │
   └── Paramètres
```

---

# 3. Principe V1

MAGIC GOLD V1 ne constitue pas :

* une banque ;
* un portefeuille électronique interne ;
* un système comptable complet ;
* un ERP ;
* un système de fidélité ;
* une marketplace multi-marchands complexe.

Le Merchant utilise MAGIC GOLD pour gérer son activité commerciale et recevoir des paiements via les mécanismes de paiement intégrés.

---

# 4. Types de Merchant

Le modèle doit être extensible.

Types possibles :

```text
INDIVIDUAL
BUSINESS
COMPANY
ORGANIZATION
```

Le type exact peut être configuré selon les besoins commerciaux de MAGIC GOLD.

---

# 5. Identité du Merchant

Un Merchant doit posséder au minimum :

```text
Merchant
-------------------------
id
legalName
displayName
merchantType
phone
email
address
city
country
status
logo
createdAt
updatedAt
```

---

# 6. Merchant Number

Chaque marchand doit disposer d'un identifiant public.

Exemple :

```text
MG-MER-000125
```

Le numéro doit être :

* unique ;
* stable ;
* non modifiable ;
* utilisable par le support ;
* utilisable dans les rapports.

---

# 7. Statuts Merchant

Les statuts principaux sont :

```text
PENDING
ACTIVE
SUSPENDED
BLOCKED
CLOSED
```

---

# 8. PENDING

Le marchand est en cours d'inscription ou de validation.

Il peut avoir un accès limité.

Il ne doit pas pouvoir recevoir des paiements commerciaux tant que les conditions d'activation ne sont pas satisfaites.

---

# 9. ACTIVE

Le marchand peut utiliser normalement MAGIC GOLD :

* gérer son catalogue ;
* recevoir des commandes ;
* traiter les commandes ;
* utiliser les fonctionnalités de paiement autorisées.

---

# 10. SUSPENDED

Le compte est temporairement suspendu.

Les nouvelles opérations commerciales peuvent être bloquées.

L'accès administratif peut rester disponible selon les règles.

---

# 11. BLOCKED

Le marchand est bloqué.

Les opérations commerciales doivent être refusées.

---

# 12. CLOSED

Le compte est fermé.

Les données historiques doivent rester conservées selon les règles de conservation.

---

# 13. Cycle de vie

```text
PENDING
   │
   ▼
ACTIVE
   │
   ├────► SUSPENDED
   │          │
   │          ▼
   │        ACTIVE
   │
   └────► BLOCKED
              │
              ▼
           CLOSED
```

Une transition ne doit être possible que si elle est explicitement autorisée.

---

# 14. Activation

L'activation peut nécessiter :

* création du compte ;
* vérification de l'identité ;
* validation des informations ;
* acceptation des conditions ;
* configuration du paiement ;
* validation administrative éventuelle.

Le processus exact doit rester configurable.

---

# 15. KYC / Vérification

Si MAGIC GOLD exige une vérification du marchand, elle doit être séparée du profil commercial.

Exemple :

```text
Merchant
   │
   ├── Commercial Profile
   │
   └── Verification
```

La vérification peut avoir les états :

```text
NOT_STARTED
PENDING
VERIFIED
REJECTED
EXPIRED
```

Les informations sensibles doivent être protégées.

---

# 16. Boutique

Chaque Merchant peut posséder une boutique.

```text
Store
-------------------------
id
merchantId
name
slug
description
logo
coverImage
phone
email
address
status
createdAt
updatedAt
```

---

# 17. Nom de boutique

Le nom public peut être différent du nom légal.

Exemple :

```text
Legal Name:
Nectux Commerce SARL

Display Name:
Nectux Store
```

---

# 18. Slug

La boutique peut disposer d'un identifiant URL :

```text
nectux-store
```

Le slug doit être :

* unique ;
* stable ;
* contrôlé ;
* sécurisé.

---

# 19. Profil public

Le profil public du marchand peut afficher :

* logo ;
* nom ;
* description ;
* catégorie ;
* localisation ;
* horaires ;
* moyens de contact ;
* produits ;
* conditions commerciales.

Les informations privées ne doivent jamais être exposées.

---

# 20. Catalogue

Le Merchant gère son catalogue.

```text
Merchant
   │
   ▼
Catalog
   │
   ├── Categories
   │
   └── Products
```

---

# 21. Catégories

Une catégorie peut contenir :

```text
id
merchantId
name
description
image
status
sortOrder
createdAt
updatedAt
```

---

# 22. Produit

Un produit doit pouvoir contenir :

```text
Product
-------------------------
id
merchantId
categoryId
name
description
sku
price
currency
image
status
stockTrackingEnabled
createdAt
updatedAt
```

---

# 23. Statuts produit

```text
DRAFT
ACTIVE
INACTIVE
OUT_OF_STOCK
ARCHIVED
```

---

# 24. Prix

Le prix est géré côté serveur.

Exemple :

```text
price = 15000
currency = XOF
```

Le Merchant peut modifier son prix s'il dispose de la permission correspondante.

Les commandes existantes ne doivent jamais être recalculées avec le nouveau prix.

---

# 25. Stock

V1 peut prévoir un stock simple.

```text
stockTrackingEnabled
availableQuantity
```

La gestion avancée du stock pourra être introduite ultérieurement.

---

# 26. Publication d'un produit

Un produit peut être :

```text
DRAFT
   │
   ▼
ACTIVE
```

Pour être visible par les clients, il doit notamment être :

```text
status = ACTIVE
```

et appartenir à un Merchant actif.

---

# 27. Commandes

Le Merchant dispose d'une vue de ses commandes.

```text
Merchant
   │
   ▼
Orders
   │
   ├── New
   ├── Confirmed
   ├── Processing
   ├── Ready
   ├── Completed
   └── Cancelled
```

Le détail du cycle de commande est défini dans :

```text
10_OrderManagement.md
```

---

# 28. Accès aux commandes

Le Merchant ne doit pouvoir consulter que :

```text
orders.merchantId == authenticatedMerchant.id
```

Cette règle doit être appliquée côté backend.

---

# 29. Commande

Le Merchant peut :

* voir les détails ;
* confirmer ;
* traiter ;
* préparer ;
* marquer comme prête ;
* terminer ;
* consulter le paiement associé ;
* gérer les actions autorisées.

---

# 30. Paiements

Le Merchant peut consulter les informations nécessaires au suivi commercial :

```text
paymentReference
paymentMethod
amount
currency
status
date
```

Les informations sensibles du prestataire ne doivent pas être exposées inutilement.

---

# 31. Pas de modification financière directe

Le Merchant ne peut jamais :

```text
modifier payment.status
modifier payment.amount
modifier providerReference
créditer un solde
modifier le ledger
```

Les opérations financières doivent passer par les services spécialisés.

---

# 32. Remboursement

Un remboursement peut être :

* demandé par le client ;
* traité par le marchand selon ses permissions ;
* traité par FINANCE ;
* exécuté via le prestataire de paiement.

Flux :

```text
CUSTOMER
   │
   ▼
Refund Request
   │
   ▼
Merchant / Finance
   │
   ▼
Payment Service
   │
   ▼
Payment Provider
```

---

# 33. Client

Le Merchant peut consulter les informations client nécessaires à l'exécution de la commande.

Il ne doit pas accéder aux données privées non nécessaires.

Principe :

> Need-to-know.

---

# 34. Données client accessibles

Selon le contexte, le Merchant peut accéder à :

* nom ;
* prénom ;
* téléphone nécessaire à la livraison ;
* adresse nécessaire à la livraison ;
* commandes ;
* informations nécessaires au support.

Les données sensibles doivent être minimisées.

---

# 35. Collaborateurs

Un Merchant peut gérer des collaborateurs.

```text
Merchant
   │
   └── Operators
         ├── Operator A
         ├── Operator B
         └── Operator C
```

Les collaborateurs ne sont pas des Merchants indépendants.

---

# 36. Merchant Operator

Un Operator appartient à un Merchant.

Il peut disposer de permissions comme :

```text
ORDER_VIEW
ORDER_CONFIRM
ORDER_PROCESS
ORDER_COMPLETE
PRODUCT_VIEW
PRODUCT_CREATE
PRODUCT_UPDATE
REPORT_VIEW
```

---

# 37. Isolation des Operators

Un Operator du Merchant A ne doit jamais accéder aux données du Merchant B.

La règle d'isolation doit être appliquée au backend.

---

# 38. Permissions Merchant

Exemples :

```text
MERCHANT_VIEW
MERCHANT_UPDATE

STORE_VIEW
STORE_UPDATE

PRODUCT_VIEW
PRODUCT_CREATE
PRODUCT_UPDATE
PRODUCT_DELETE

CATEGORY_VIEW
CATEGORY_CREATE
CATEGORY_UPDATE
CATEGORY_DELETE

ORDER_VIEW
ORDER_CONFIRM
ORDER_PROCESS
ORDER_COMPLETE
ORDER_CANCEL

PAYMENT_VIEW
REFUND_REQUEST

CUSTOMER_VIEW

REPORT_VIEW

OPERATOR_VIEW
OPERATOR_CREATE
OPERATOR_UPDATE
OPERATOR_DISABLE
```

---

# 39. Rôle ≠ Permission

Un rôle ne doit pas être utilisé comme seule sécurité.

Exemple :

```text
MERCHANT_OPERATOR
```

ne signifie pas automatiquement :

```text
REFUND
```

La permission réelle doit être vérifiée.

---

# 40. Merchant Dashboard

Le tableau de bord doit afficher :

```text
Aujourd'hui
-------------------------
Commandes
Ventes
Paiements
Commandes en attente
Commandes à traiter
```

---

# 41. Indicateurs

Exemples :

```text
CA du jour
CA de la semaine
CA du mois
Nombre de commandes
Panier moyen
Commandes terminées
Commandes annulées
Paiements réussis
Paiements échoués
```

Les indicateurs financiers doivent provenir des données de paiement fiables.

---

# 42. Catalogue Dashboard

Le Merchant peut visualiser :

```text
Produits actifs
Produits inactifs
Produits en rupture
Produits brouillons
```

---

# 43. Gestion du profil

Le Merchant peut modifier selon ses permissions :

* nom commercial ;
* description ;
* téléphone ;
* email ;
* adresse ;
* logo ;
* informations publiques.

Les données légales vérifiées peuvent nécessiter une procédure spécifique.

---

# 44. Modification des données vérifiées

Une donnée validée ne doit pas être modifiée sans contrôle si elle influence :

* identité ;
* paiement ;
* facturation ;
* conformité.

Une nouvelle vérification peut être déclenchée.

---

# 45. Horaires

La boutique peut définir ses horaires.

Exemple :

```text
Lundi     08:00 - 18:00
Mardi     08:00 - 18:00
Mercredi  08:00 - 18:00
...
```

Ces horaires peuvent être utilisés pour informer le client.

Ils ne doivent pas automatiquement bloquer une commande sauf si cette règle est activée.

---

# 46. Livraison

Le Merchant peut définir les paramètres commerciaux liés à la livraison :

* livraison disponible ;
* retrait en magasin ;
* zones desservies ;
* frais éventuels ;
* délais indicatifs.

La gestion logistique avancée pourra être un module séparé.

---

# 47. Conditions commerciales

Le Merchant peut définir :

* minimum de commande ;
* frais ;
* délais ;
* conditions d'annulation ;
* conditions de remboursement.

Les règles doivent être validées et appliquées côté backend.

---

# 48. Notifications Merchant

Le Merchant peut recevoir des notifications :

```text
NEW_ORDER
PAYMENT_CONFIRMED
ORDER_CANCELLED
REFUND_REQUESTED
REFUND_COMPLETED
PRODUCT_ALERT
ACCOUNT_ALERT
```

Les préférences sont gérées par le module Notifications.

---

# 49. Sécurité Merchant

Le Merchant doit bénéficier de :

* authentification sécurisée ;
* gestion des sessions ;
* contrôle des permissions ;
* journalisation ;
* limitation des actions sensibles ;
* éventuellement MFA selon configuration.

---

# 50. Actions sensibles

Les actions sensibles peuvent nécessiter une confirmation supplémentaire :

* modification des informations de paiement ;
* remboursement ;
* changement d'identité ;
* ajout d'un opérateur ;
* suppression d'un opérateur ;
* fermeture du compte.

---

# 51. Audit Merchant

Auditer notamment :

```text
PRODUCT_CREATED
PRODUCT_UPDATED
PRODUCT_DELETED

ORDER_CONFIRMED
ORDER_PROCESSING
ORDER_COMPLETED
ORDER_CANCELLED

REFUND_REQUESTED

OPERATOR_CREATED
OPERATOR_UPDATED
OPERATOR_DISABLED

MERCHANT_UPDATED
```

---

# 52. API Merchant

## Profil

```http
GET /api/merchant/profile
```

```http
PUT /api/merchant/profile
```

## Boutique

```http
GET /api/merchant/store
```

```http
PUT /api/merchant/store
```

## Produits

```http
GET /api/merchant/products
POST /api/merchant/products
GET /api/merchant/products/{id}
PUT /api/merchant/products/{id}
DELETE /api/merchant/products/{id}
```

## Catégories

```http
GET /api/merchant/categories
POST /api/merchant/categories
PUT /api/merchant/categories/{id}
DELETE /api/merchant/categories/{id}
```

---

# 53. API Commandes

```http
GET /api/merchant/orders
GET /api/merchant/orders/{id}
POST /api/merchant/orders/{id}/confirm
POST /api/merchant/orders/{id}/processing
POST /api/merchant/orders/{id}/ready
POST /api/merchant/orders/{id}/complete
```

---

# 54. API Opérateurs

```http
GET /api/merchant/operators
POST /api/merchant/operators
PUT /api/merchant/operators/{id}
POST /api/merchant/operators/{id}/disable
```

---

# 55. API Reporting

```http
GET /api/merchant/reports/sales
GET /api/merchant/reports/orders
GET /api/merchant/reports/payments
```

Les données retournées doivent respecter le périmètre du Merchant.

---

# 56. Modèle Merchant

```text
Merchant
-------------------------
id
merchantNumber
legalName
displayName
merchantType
phone
email
address
city
country
status
logoUrl
createdAt
updatedAt
version
```

---

# 57. Modèle Store

```text
Store
-------------------------
id
merchantId
name
slug
description
logoUrl
coverUrl
phone
email
address
status
createdAt
updatedAt
```

---

# 58. Relation

```text
Merchant 1
    │
    ├──── 1 Store
    │
    ├──── N Categories
    │
    ├──── N Products
    │
    ├──── N Orders
    │
    └──── N Operators
```

---

# 59. Isolation multi-tenant

MAGIC GOLD doit considérer chaque Merchant comme un périmètre logique.

```text
Merchant A
   ├── Products A
   ├── Orders A
   ├── Operators A
   └── Reports A

Merchant B
   ├── Products B
   ├── Orders B
   ├── Operators B
   └── Reports B
```

Une requête d'un Merchant doit systématiquement être filtrée par son périmètre.

---

# 60. Protection contre IDOR

Il ne suffit pas de vérifier :

```http
GET /api/merchant/orders/{id}
```

Le backend doit vérifier :

```text
order.merchantId == authenticatedMerchantId
```

Même si l'utilisateur connaît l'UUID d'une autre commande.

---

# 61. Suppression

Les données commerciales historiques ne doivent pas être supprimées physiquement lorsque cela détruirait la traçabilité.

Pour un produit :

```text
status = ARCHIVED
```

est généralement préférable à une suppression destructive.

---

# 62. Désactivation Merchant

Lorsqu'un Merchant est suspendu :

* nouvelles commandes : bloquées selon politique ;
* catalogue : éventuellement masqué ;
* commandes existantes : conservées ;
* paiements existants : conservés ;
* historique : conservé.

---

# 63. Fermeture Merchant

La fermeture ne doit pas supprimer :

* commandes ;
* paiements ;
* remboursements ;
* audits ;
* données nécessaires à la traçabilité.

---

# 64. Performance

Les écrans Merchant doivent utiliser :

* pagination ;
* filtres ;
* recherche ;
* projections ;
* chargement ciblé.

Ne pas charger l'ensemble du catalogue ou de l'historique en une seule requête.

---

# 65. Recherche Merchant

Le Merchant peut rechercher :

```text
Produit
SKU
Commande
Client
Numéro de commande
```

La recherche doit rester limitée au périmètre du Merchant.

---

# 66. Import de produits

Une fonctionnalité d'import pourra être prévue :

```text
CSV
Excel
API
```

Mais elle doit être traitée comme une fonctionnalité contrôlée.

Pour V1, l'import CSV peut être considéré comme optionnel.

---

# 67. Export

Le Merchant peut exporter selon ses permissions :

* produits ;
* commandes ;
* ventes ;
* paiements ;
* rapports.

Les exports doivent respecter le périmètre du Merchant.

---

# 68. Limites

MAGIC GOLD peut définir des limites configurables :

```text
Nombre maximum de produits
Nombre maximum d'opérateurs
Taille maximale des images
Nombre maximum de commandes
```

Les limites ne doivent pas être codées en dur si elles sont susceptibles d'évoluer commercialement.

---

# 69. Statistiques

Les statistiques Merchant doivent être dérivées des données réelles.

Exemple :

```text
Total Sales
=
Somme des paiements commerciaux confirmés
```

et non simplement :

```text
Nombre de commandes × prix actuel
```

---

# 70. Séparation chiffre d'affaires / commandes

Une commande créée n'est pas nécessairement une vente encaissée.

Exemple :

```text
Commande créée      = 100 000 XOF
Paiement échoué     = 100 000 XOF
CA encaissé         = 0 XOF
```

Les dashboards doivent respecter cette distinction.

---

# 71. Réconciliation

MAGIC GOLD doit permettre de comparer :

```text
Commandes
    │
    ▼
Paiements
    │
    ▼
Prestataire
```

Les divergences doivent être signalées.

---

# 72. Erreurs métier

Exemples :

```text
MERCHANT_NOT_ACTIVE
MERCHANT_NOT_AUTHORIZED
PRODUCT_NOT_FOUND
PRODUCT_NOT_OWNED
ORDER_NOT_OWNED
OPERATOR_NOT_OWNED
STORE_NOT_FOUND
PRODUCT_ALREADY_ARCHIVED
INVALID_PRICE
INVALID_STATUS
```

---

# 73. Tests fonctionnels

Tester :

* création Merchant ;
* activation ;
* suspension ;
* fermeture ;
* création boutique ;
* création catégorie ;
* création produit ;
* modification produit ;
* archivage produit ;
* création commande ;
* traitement commande ;
* opérateur ;
* permissions.

---

# 74. Tests de sécurité

Tester impérativement :

```text
Merchant A → accès Product B
Merchant A → accès Order B
Merchant A → accès Customer B
Operator A → accès Merchant B
```

Tous doivent être refusés.

---

# 75. Tests de permissions

Tester chaque permission :

```text
PRODUCT_CREATE
PRODUCT_UPDATE
PRODUCT_DELETE
ORDER_VIEW
ORDER_CONFIRM
ORDER_PROCESS
ORDER_COMPLETE
PAYMENT_VIEW
REFUND_REQUEST
OPERATOR_CREATE
REPORT_VIEW
```

---

# 76. Definition of Done

Le module Merchant est terminé lorsque :

* [ ] le Merchant peut être créé ;
* [ ] le cycle de vie est implémenté ;
* [ ] le profil est géré ;
* [ ] la boutique est gérée ;
* [ ] les catégories sont gérées ;
* [ ] les produits sont gérés ;
* [ ] les commandes sont accessibles ;
* [ ] les paiements sont consultables ;
* [ ] les collaborateurs sont gérés ;
* [ ] les permissions sont appliquées ;
* [ ] l'isolation multi-tenant est assurée ;
* [ ] les statistiques sont disponibles ;
* [ ] l'audit est disponible ;
* [ ] les exports respectent le périmètre ;
* [ ] les tests de sécurité sont présents ;
* [ ] les tests de permissions sont présents.

---

# 77. Règles Hermes

Hermes doit respecter impérativement :

### Règle 1

Un Merchant ne voit jamais les données d'un autre Merchant.

### Règle 2

L'isolation doit être appliquée côté backend.

### Règle 3

Ne jamais considérer le rôle comme une autorisation suffisante.

### Règle 4

Le Merchant ne modifie jamais directement les données financières.

### Règle 5

Le Merchant ne possède aucun Wallet interne MAGIC GOLD en V1.

### Règle 6

Le Merchant ne peut pas déclarer lui-même un paiement réussi.

### Règle 7

Les commandes existantes conservent leurs snapshots historiques.

### Règle 8

La fermeture d'un Merchant ne supprime pas l'historique financier.

### Règle 9

Les opérations sensibles sont auditées.

### Règle 10

Ne pas introduire de marketplace multi-marchands complexe en V1 sans demande explicite.

---

# 78. Relations avec les autres spécifications

Le module Merchant dépend notamment de :

```text
04_DataModel.md
05_API_Specification.md
06_SecurityArchitecture.md
07_UI_UX_Specification.md
08_Notifications.md
09_PaymentArchitecture.md
10_OrderManagement.md
```

Il sera utilisé par :

```text
12_CustomerSpecification.md
13_Roles_Permissions_Audit.md
14_Reporting.md
15_IntegrationSpecification.md
16_TestStrategy.md
17_Deployment.md
```

---

# 79. Architecture cible

```text
                         MERCHANT
                            │
              ┌─────────────┼─────────────┐
              │             │             │
              ▼             ▼             ▼
           PROFILE        STORE       OPERATORS
                            │
                     ┌──────┴──────┐
                     │             │
                     ▼             ▼
                 CATEGORIES     PRODUCTS
                     │             │
                     └──────┬──────┘
                            ▼
                         ORDERS
                            │
                            ▼
                         PAYMENT
                            │
                            ▼
                    PAYMENT PROVIDER
```

---

# 80. Principe final

Le Merchant est le **cœur de l'offre commerciale de MAGIC GOLD**.

Il utilise la plateforme pour :

> **présenter → vendre → recevoir des commandes → suivre les paiements → traiter → analyser.**

MAGIC GOLD doit lui fournir une expérience commerciale simple tout en maintenant une séparation stricte entre :

```text
MERCHANT
PRODUCT
ORDER
PAYMENT
PAYMENT PROVIDER
CUSTOMER
AUDIT
REPORTING
```

Cette séparation constitue le socle de l'architecture V1 et permettra d'ajouter ultérieurement d'autres services sans remettre en cause le cœur du système.


Oui. Pour **MAGIC GOLD**, je te conseille de bien séparer **inscription marchand**, **activation commerciale** et **configuration des paiements**. L’activation ne doit surtout pas être un simple `status = ACTIVE`.

### Parcours d’activation marchand V1

```text
                ┌──────────────────────┐
                │ 1. INSCRIPTION       │
                │ Création du compte   │
                └──────────┬───────────┘
                           ↓
                ┌──────────────────────┐
                │ 2. PROFIL MARCHAND   │
                │ Identité / entreprise│
                │ Coordonnées          │
                └──────────┬───────────┘
                           ↓
                ┌──────────────────────┐
                │ 3. VÉRIFICATION      │
                │ Email / téléphone    │
                │ + KYC si nécessaire  │
                └──────────┬───────────┘
                           ↓
                ┌──────────────────────┐
                │ 4. CONFIGURATION     │
                │ Boutique             │
                │ Produits             │
                │ Paiements            │
                └──────────┬───────────┘
                           ↓
                ┌──────────────────────┐
                │ 5. VALIDATION        │
                │ Automatique ou Admin │
                └──────────┬───────────┘
                           ↓
                ┌──────────────────────┐
                │ 6. ACTIVATION        │
                │ MERCHANT = ACTIVE    │
                └──────────┬───────────┘
                           ↓
                ┌──────────────────────┐
                │ 7. EXPLOITATION      │
                │ Produits → Commandes │
                │ → Paiements          │
                └──────────────────────┘
```

### Les états devraient être plus précis

Je modifierais légèrement ce qui était dans `11_MerchantSpecification.md` :

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
READY_FOR_ACTIVATION
    ↓
ACTIVE
```

Avec des sorties exceptionnelles :

```text
VERIFICATION_PENDING
        ↓
     REJECTED
        ↓
  PROFILE_CORRECTION
        ↓
VERIFICATION_PENDING
```

Et après activation :

```text
ACTIVE
 ├── SUSPENDED
 │      ↓
 │   ACTIVE
 │
 ├── BLOCKED
 │
 └── CLOSED
```

### Mais attention à une distinction importante

Je ferais **deux statuts différents**.

**1. Statut du marchand**

```text
PENDING
ACTIVE
SUSPENDED
BLOCKED
CLOSED
```

**2. État d'onboarding**

```text
REGISTERED
PROFILE_INCOMPLETE
VERIFICATION_PENDING
VERIFIED
PAYMENT_CONFIGURATION_PENDING
READY
```

Pourquoi ? Parce qu'un marchand peut être :

```text
merchant.status = PENDING
onboarding.status = PAYMENT_CONFIGURATION_PENDING
```

Cela évite de transformer le champ `status` du marchand en fourre-tout.

### Ce que le marchand doit fournir

**Personne physique :**

* nom/prénom
* téléphone
* email
* adresse
* pièce d'identité si nécessaire
* informations commerciales
* boutique
* produits/services

**Entreprise :**

* raison sociale
* nom commercial
* numéro d'identification applicable
* représentant
* téléphone
* email
* adresse
* documents justificatifs si nécessaires
* boutique
* produits/services

Le **KYC doit rester configurable**, car tous les marchands ne nécessiteront pas forcément le même niveau de vérification.

### Configuration paiement

C'est ici qu'il faut être très clair avec notre architecture précédente.

Le marchand **ne reçoit pas d'argent dans un wallet MAGIC GOLD**.

Il configure simplement les moyens de paiement que MAGIC GOLD pourra utiliser via les **prestataires externes**.

Par exemple :

```text
Marchand
   │
   ├── Mobile Money
   ├── Carte
   ├── QR
   └── Autre provider
          │
          ↓
   Payment Provider
          │
          ↓
      Transaction
```

MAGIC GOLD conserve :

```text
paymentId
orderId
merchantId
provider
providerReference
amount
currency
status
createdAt
```

mais **pas un solde marchand interne**.

### Quand peut-on réellement passer à `ACTIVE` ?

Je proposerais cette règle :

```text
ACTIVE =
    compte utilisateur actif
    AND profil marchand complet
    AND coordonnées vérifiées
    AND vérification requise validée
    AND boutique configurée
    AND au moins un produit/service publiable
    AND configuration paiement valide
    AND conditions MAGIC GOLD acceptées
```

Selon le niveau de contrôle choisi, certaines conditions peuvent être facultatives.

Par exemple, un marchand peut être autorisé à préparer sa boutique avant validation :

```text
PENDING
   ↓
peut configurer sa boutique
   ↓
ne peut PAS recevoir de commande
   ↓
validation
   ↓
ACTIVE
   ↓
peut recevoir des commandes
```

C'est beaucoup plus propre pour l'expérience utilisateur.

### Et surtout : ne pas confondre activation et publication

Un marchand peut être `ACTIVE`, mais avoir :

```text
store.status = INACTIVE
```

ou aucun produit actif.

Donc :

```text
MERCHANT ACTIVE
       ≠
STORE ACTIVE
       ≠
PRODUCT ACTIVE
       ≠
PAYMENT ENABLED
```

C'est une séparation importante pour éviter des bugs métier plus tard.

**Pour `11_MerchantSpecification.md`, je recommande donc de formaliser l'onboarding comme un sous-domaine avec sa propre machine d'état**, plutôt que de laisser seulement `PENDING → ACTIVE`. C'est plus robuste pour Hermes et pour l'implémentation Spring Boot/Vue/Keycloak.
