Oui. Je corrige le document pour que **MAGIC GOLD soit d'abord un outil de commerce de détail**, avec un **client/acheteur principalement anonyme**, et non une plateforme où chaque acheteur doit posséder un compte.

J'intègre aussi la distinction **Client commercial ≠ Utilisateur MAGIC GOLD**. Le compte utilisateur concerne surtout les acteurs qui exploitent la plateforme : marchand, caissier, opérateur, etc.

# MAGIC GOLD — Customer Specification

**Fichier :** `12_CustomerSpecification.md`
**Version :** 1.1
**Statut :** Specification V1 — corrigée
**Produit :** MAGIC GOLD
**Éditeur :** Nectux Ingénieries

---

# 1. Objectif

Ce document définit la notion de **Client/Acheteur** dans MAGIC GOLD.

MAGIC GOLD est avant tout un outil au service du **commerce de détail**.

Le Client est donc la personne qui achète un produit ou un service auprès d'un Merchant.

Exemples :

* achat de boissons ;
* achat de vêtements ;
* achat de chaussures ;
* achat de produits alimentaires ;
* achat de produits électroniques ;
* achat de produits de beauté ;
* achat de produits divers ;
* achat de services.

Le Client n'est **pas obligatoirement un utilisateur enregistré de MAGIC GOLD**.

---

# 2. Principe fondamental

Il faut distinguer :

```text
CLIENT COMMERCIAL
        ≠
UTILISATEUR MAGIC GOLD
```

Un Client peut acheter sans :

* créer de compte ;
* posséder un mot de passe ;
* posséder l'application ;
* fournir son nom ;
* fournir son téléphone ;
* fournir son email.

Le système doit permettre une vente rapide avec un **client anonyme**.

---

# 3. Vision V1

Le parcours commercial principal est :

```text
CLIENT
   │
   ▼
ACHAT
   │
   ▼
PANIER / VENTE
   │
   ▼
PAIEMENT
   │
   ▼
REÇU
```

Le Client peut rester totalement anonyme.

```text
Client anonyme
      │
      ▼
   Produit
      │
      ▼
     Vente
      │
      ▼
   Paiement
      │
      ▼
    Reçu
```

---

# 4. MAGIC GOLD au service du commerce de détail

Le cœur du système est le Merchant et son activité commerciale.

```text
                         MAGIC GOLD
                             │
          ┌──────────────────┼──────────────────┐
          │                  │                  │
          ▼                  ▼                  ▼
      PRODUITS             VENTES            PAIEMENTS
          │                  │                  │
          │                  │                  ▼
          │                  │             PROVIDERS
          │                  │
          ▼                  ▼
        STOCK             CLIENTS
```

Le Client intervient principalement dans le processus de vente.

---

# 5. Client anonyme

Le mode par défaut en commerce de détail doit pouvoir être :

```text
CUSTOMER = ANONYMOUS
```

Exemple :

```text
Client : Client anonyme

Produit :
Coca-Cola × 2
Polo × 1

Total :
11 000 XOF

Paiement :
Espèces
```

Aucune information personnelle n'est obligatoire.

---

# 6. Client identifié

Le Merchant peut, lorsque cela est utile, associer un contact à la vente.

Exemple :

```text
Client
Téléphone : 07 XX XX XX XX
```

Le téléphone peut être utilisé pour :

* envoyer un reçu ;
* contacter le client ;
* retrouver une vente ;
* assurer un suivi ;
* gérer certaines commandes ;
* éventuellement construire un historique commercial.

Le téléphone ne doit toutefois pas être obligatoire pour un achat ordinaire.

---

# 7. Client connu

Un Merchant peut connaître certains de ses clients réguliers.

Exemple :

```text
Client
-------------------------
Nom : Jean Kouassi
Téléphone : 07 XX XX XX XX
```

Le Merchant peut alors retrouver les achats associés à ce client selon les permissions et les règles de confidentialité.

---

# 8. Types de Client

MAGIC GOLD V1 peut utiliser :

```text
ANONYMOUS
IDENTIFIED
REGISTERED
```

### ANONYMOUS

Aucune information personnelle nécessaire.

### IDENTIFIED

Un ou plusieurs éléments de contact sont associés au client.

Exemple :

```text
phone
```

### REGISTERED

Le client dispose éventuellement d'un compte MAGIC GOLD.

Cette possibilité ne doit pas être nécessaire au fonctionnement du commerce de détail V1.

---

# 9. Compte MAGIC GOLD facultatif

Le compte MAGIC GOLD du Client est une fonctionnalité complémentaire.

Il ne doit jamais être une condition obligatoire pour :

```text
acheter
payer
recevoir un produit
obtenir un reçu
```

Le système doit fonctionner parfaitement avec :

```text
Client anonyme
```

---

# 10. Pas d'inscription obligatoire

Le Merchant ne doit pas être obligé de demander :

```text
nom
prénom
email
téléphone
mot de passe
```

pour chaque vente.

Pour une vente rapide :

```text
Produit
+
Quantité
+
Prix
+
Paiement
=
Vente
```

---

# 11. Données Client minimales

Le modèle Client peut être :

```text
Customer
-------------------------
id
merchantId
customerType
displayName
phone
email
createdAt
updatedAt
```

Les champs personnels doivent être facultatifs lorsqu'ils ne sont pas nécessaires.

---

# 12. Portée du Client

Un Client commercial appartient principalement au périmètre du Merchant.

Exemple :

```text
Merchant A
   │
   ├── Client A1
   ├── Client A2
   └── Client A3

Merchant B
   │
   ├── Client B1
   └── Client B2
```

Le Merchant A ne doit pas automatiquement accéder aux clients du Merchant B.

---

# 13. Client partagé

V1 ne doit pas supposer qu'un Client est nécessairement global à toute la plateforme.

Un même numéro de téléphone peut être utilisé chez plusieurs Merchants.

Exemple :

```text
07 XX XX XX XX

Merchant A → Client A
Merchant B → Client B
```

Le système ne doit pas automatiquement révéler à Merchant A les achats effectués chez Merchant B.

---

# 14. Client et Utilisateur

Un compte utilisateur MAGIC GOLD peut exister pour :

```text
MERCHANT
MERCHANT_OPERATOR
SUPPORT
FINANCE
AUDITOR
ADMIN
SUPER_ADMIN
```

Un Client commercial n'a pas besoin d'être un utilisateur MAGIC GOLD.

```text
Utilisateur MAGIC GOLD
        │
        ├── Merchant
        ├── Operator
        ├── Support
        └── Admin

Client commercial
        │
        └── Achat
```

---

# 15. Vente au comptoir

MAGIC GOLD doit être adapté au commerce de détail.

Exemple :

```text
Caissier
   │
   ▼
Nouvelle vente
   │
   ├── Coca × 2
   ├── Eau × 3
   └── T-shirt × 1
   │
   ▼
Total
   │
   ▼
Paiement
   │
   ▼
Vente terminée
```

Le Client peut ne jamais interagir directement avec MAGIC GOLD.

---

# 16. Vente rapide

Le système doit permettre une vente rapide avec un minimum d'étapes.

```text
SCAN / RECHERCHE PRODUIT
        ↓
QUANTITÉ
        ↓
PANIER
        ↓
TOTAL
        ↓
PAIEMENT
        ↓
REÇU
```

L'expérience doit être adaptée au travail du caissier.

---

# 17. Panier

Le panier représente les produits actuellement sélectionnés pour une vente.

```text
Cart
-------------------------
merchantId
operatorId
customerId nullable
items
subtotal
discountAmount
taxAmount
feeAmount
totalAmount
```

Le `customerId` peut être nul pour un client anonyme.

---

# 18. Exemple de panier anonyme

```text
Customer:
ANONYMOUS

Items:
-------------------------
Coca × 2       1 000 XOF
Polo × 1      10 000 XOF

Total          11 000 XOF
```

---

# 19. Prix

Le prix final doit être calculé côté serveur.

Le caissier ou le Client ne doit pas pouvoir imposer arbitrairement le prix transmis à l'API.

Le backend reste la source de vérité.

---

# 20. Quantité

Le système doit vérifier :

```text
quantity > 0
```

et, si la gestion du stock est activée :

```text
quantity <= availableQuantity
```

---

# 21. Stock

Lorsqu'une vente est confirmée, le système peut mettre à jour le stock selon les règles du module Stock.

Exemple :

```text
Stock Coca avant vente : 50
Vente : 2
Stock après vente : 48
```

La gestion détaillée du stock doit rester séparée du Customer.

---

# 22. Commande ou Vente

Pour le commerce de détail, il faut distinguer :

```text
VENTE
```

et

```text
COMMANDE
```

Une vente au comptoir peut être immédiatement payée :

```text
Vente
   ↓
Paiement
   ↓
Terminée
```

Une commande peut nécessiter un traitement :

```text
Commande
   ↓
Confirmation
   ↓
Préparation
   ↓
Livraison / Retrait
   ↓
Terminée
```

Le modèle final doit permettre les deux scénarios.

---

# 23. Relation avec Order Management

Le cycle des commandes est défini dans :

```text
10_OrderManagement.md
```

Le Client commercial peut être :

```text
customerId = null
```

pour une vente anonyme.

Il peut également être :

```text
customerId = <id>
```

lorsqu'un client est identifié.

---

# 24. Paiement

Le Client peut payer avec les moyens disponibles :

```text
CASH
MOBILE_MONEY
CARD
QR_PAYMENT
BANK_TRANSFER
OTHER
```

Le paiement est enregistré par MAGIC GOLD.

Lorsque le paiement est digital, l'exécution financière passe par un prestataire externe.

---

# 25. Espèces

MAGIC GOLD doit pouvoir enregistrer une vente payée en espèces.

Exemple :

```text
Total : 10 000 XOF
Montant reçu : 10 000 XOF
Mode : CASH
Statut : PAID
```

La gestion de caisse physique pourra être détaillée dans un module Caisse/POS spécifique.

---

# 26. Paiement digital

Exemple :

```text
Vente
  │
  ▼
Paiement MAGIC GOLD
  │
  ▼
Payment Provider
  │
  ▼
Mobile Money / Carte / QR
  │
  ▼
Confirmation
```

MAGIC GOLD ne détient pas les fonds.

---

# 27. Référence du paiement

Une vente peut conserver :

```text
paymentReference
paymentMethod
paymentProvider
providerReference
amount
currency
status
```

Les données sensibles du prestataire ne doivent pas être inutilement exposées au Merchant ou au Client.

---

# 28. Reçu

Après paiement, MAGIC GOLD peut générer un reçu.

Exemple :

```text
MAGIC GOLD
Boutique XYZ

Vente : MG-SALE-000125

Coca × 2        1 000 XOF
Polo × 1       10 000 XOF

TOTAL          11 000 XOF

Paiement : CASH
Statut : PAYÉ
```

Le reçu peut être :

* imprimé ;
* affiché ;
* envoyé par SMS ;
* envoyé par email ;
* partagé selon les fonctionnalités disponibles.

---

# 29. Contact facultatif pour le reçu

Si le Client fournit un téléphone :

```text
07 XX XX XX XX
```

MAGIC GOLD peut envoyer le reçu par SMS si le service est configuré.

Sans téléphone :

```text
Reçu imprimé / affiché
```

---

# 30. Contact et consentement

Le fait de fournir un numéro de téléphone pour recevoir un reçu ne signifie pas automatiquement :

```text
consentement marketing
```

Les communications commerciales doivent disposer de règles et consentements appropriés.

---

# 31. Historique Client

Lorsqu'un client est identifié, le Merchant peut consulter son historique commercial selon ses permissions.

Exemple :

```text
Jean Kouassi

Achats :
-------------------------
12/09  25 000 XOF
18/09  12 500 XOF
22/09   8 000 XOF
```

Pour un client anonyme :

```text
Client anonyme
```

La vente reste conservée sans profil personnel obligatoire.

---

# 32. Fidélisation

La fidélité n'est pas un composant V1.

Ne pas créer :

```text
points
wallet
cashback
niveau client
programme de fidélité
```

sans évolution explicite de la spécification.

---

# 33. Historique commercial

L'historique doit être attaché à la vente.

Une vente doit conserver les informations nécessaires :

```text
produit vendu
quantité
prix
remise
taxe
total
date
marchand
opérateur
paiement
client si identifié
```

---

# 34. Snapshot

La vente doit conserver les informations historiques nécessaires.

Si le produit change ensuite de prix :

```text
Ancienne vente
10 000 XOF
```

reste :

```text
10 000 XOF
```

même si le produit vaut maintenant :

```text
12 000 XOF
```

---

# 35. Anonymisation

Si un Client identifié demande la suppression ou l'anonymisation de ses données lorsque cela est applicable, MAGIC GOLD peut dissocier les données personnelles de l'historique commercial qui doit légalement être conservé.

Exemple :

```text
Avant :

Jean Kouassi
07 XX XX XX XX
Vente MG-SALE-125

Après anonymisation :

Client anonymisé
Vente MG-SALE-125
```

Les obligations de conservation doivent être respectées.

---

# 36. Protection des données

Le système doit collecter uniquement les données nécessaires.

Pour une vente ordinaire :

```text
Produit
Quantité
Prix
Paiement
```

peuvent suffire.

Ne pas transformer une vente de détail en procédure d'inscription complexe.

---

# 37. Données accessibles au Merchant

Le Merchant peut accéder aux données clients de son périmètre selon ses permissions.

Exemples :

```text
CUSTOMER_VIEW
CUSTOMER_SEARCH
CUSTOMER_HISTORY_VIEW
```

---

# 38. Données accessibles au Caissier

Le caissier doit accéder uniquement aux informations nécessaires à la vente.

Par défaut :

```text
Nom
Téléphone
Historique
```

ne sont pas nécessaires pour effectuer une vente anonyme.

---

# 39. Permissions

Exemples :

```text
CUSTOMER_VIEW
CUSTOMER_CREATE
CUSTOMER_UPDATE
CUSTOMER_SEARCH
CUSTOMER_HISTORY_VIEW
```

Pour une utilisation personnelle éventuelle :

```text
CUSTOMER_SELF_VIEW
CUSTOMER_SELF_UPDATE
```

Ces permissions sont différentes du rôle.

---

# 40. Ownership

Un Merchant doit être limité à son propre périmètre :

```text
customer.merchantId == authenticatedMerchantId
```

lorsque le Client est géré dans le périmètre du Merchant.

---

# 41. Client global

Si MAGIC GOLD décide ultérieurement de créer un référentiel client global, cette architecture devra être explicitement introduite.

V1 ne doit pas supposer automatiquement :

```text
un téléphone = un client global MAGIC GOLD
```

---

# 42. Recherche Client

Le Merchant peut rechercher un client identifié par :

```text
nom
téléphone
email
customerNumber
```

La recherche doit rester limitée à son périmètre.

---

# 43. Recherche rapide

Pour une utilisation en caisse :

```text
Téléphone
     ↓
Recherche
     ↓
Client trouvé
     ↓
Sélection
```

Si aucun client n'est trouvé :

```text
Continuer comme client anonyme
```

---

# 44. Création rapide d'un Client

Le caissier peut éventuellement créer un client avec uniquement :

```text
Téléphone
```

ou :

```text
Nom + Téléphone
```

selon les permissions.

Le système ne doit pas imposer la création d'un compte utilisateur MAGIC GOLD.

---

# 45. Client et application mobile

Un Client peut utiliser MAGIC GOLD de plusieurs manières.

### Scénario A — aucun accès

```text
Client
   ↓
Caissier
   ↓
MAGIC GOLD
```

### Scénario B — lien ou QR

```text
Client
   ↓
QR / lien
   ↓
Produit / commande
```

### Scénario C — application future

```text
Client
   ↓
Application MAGIC GOLD
   ↓
Compte facultatif
```

Ces scénarios ne doivent pas être confondus.

---

# 46. Client dans une commande à distance

Si MAGIC GOLD permet ultérieurement une commande à distance :

```text
Client
   ↓
Commande
   ↓
Contact facultatif
   ↓
Paiement
   ↓
Livraison / retrait
```

L'identification peut devenir nécessaire pour certains services, mais elle reste distincte du modèle de vente au comptoir.

---

# 47. Livraison

Lorsqu'une livraison est nécessaire, certaines informations peuvent devenir obligatoires :

```text
Nom destinataire
Téléphone
Adresse
```

Ces informations sont collectées uniquement parce qu'elles sont nécessaires à l'exécution de la livraison.

---

# 48. Commande de retrait

Pour un retrait en boutique, le système peut utiliser :

```text
Numéro de commande
QR Code
Code de retrait
Téléphone facultatif
```

Le Client n'a pas nécessairement besoin d'un compte.

---

# 49. Notifications Client

Pour un Client anonyme, les notifications peuvent être limitées.

Exemple :

```text
Reçu SMS
Confirmation de commande
Notification de retrait
```

uniquement si un moyen de contact a été fourni.

---

# 50. Pas de notification obligatoire

Un Client qui ne fournit aucun contact doit néanmoins pouvoir acheter.

```text
Client anonyme
   ↓
Vente
   ↓
Paiement
   ↓
Reçu local
```

---

# 51. Support

Si le Client contacte le support, MAGIC GOLD peut utiliser :

```text
Numéro de vente
Numéro de commande
Référence paiement
Téléphone
```

pour retrouver l'opération.

Le support ne doit pas avoir besoin d'un compte client pour retrouver une vente.

---

# 52. Dashboard Client

V1 ne nécessite pas obligatoirement un dashboard client.

Le dashboard :

```text
Mes commandes
Mes paiements
Mes notifications
Mon profil
```

est une fonctionnalité optionnelle liée à un éventuel compte client.

Le commerce de détail doit fonctionner sans ce dashboard.

---

# 53. API Client commercial

## Recherche

```http
GET /api/merchant/customers
```

## Création

```http
POST /api/merchant/customers
```

## Consultation

```http
GET /api/merchant/customers/{id}
```

## Modification

```http
PUT /api/merchant/customers/{id}
```

## Historique

```http
GET /api/merchant/customers/{id}/sales
```

---

# 54. API Vente avec Client anonyme

Une vente doit pouvoir être créée sans `customerId`.

Exemple conceptuel :

```json
{
  "customerId": null,
  "items": [
    {
      "productId": "P001",
      "quantity": 2
    }
  ],
  "paymentMethod": "CASH"
}
```

Le backend calcule les montants.

---

# 55. API Vente avec Client identifié

```json
{
  "customerId": "C001",
  "items": [
    {
      "productId": "P001",
      "quantity": 2
    }
  ],
  "paymentMethod": "MOBILE_MONEY"
}
```

Le Client reste un élément commercial de la vente et non nécessairement un utilisateur authentifié.

---

# 56. Erreurs métier

Exemples :

```text
CUSTOMER_NOT_FOUND
CUSTOMER_NOT_OWNED
CUSTOMER_BLOCKED
INVALID_CUSTOMER_DATA
PRODUCT_NOT_AVAILABLE
INVALID_QUANTITY
INSUFFICIENT_STOCK
SALE_NOT_FOUND
PAYMENT_FAILED
PAYMENT_PENDING
```

---

# 57. Tests fonctionnels

Tester :

* vente avec Client anonyme ;
* vente avec téléphone uniquement ;
* vente avec nom + téléphone ;
* vente avec Client connu ;
* recherche client ;
* historique client ;
* modification client ;
* vente sans Customer ;
* reçu sans contact ;
* reçu avec téléphone ;
* paiement espèces ;
* paiement digital.

---

# 58. Tests de sécurité

Tester :

```text
Merchant A → Client B
Merchant A → Historique Client B
Operator A → Client Merchant B
```

Les accès non autorisés doivent être refusés.

---

# 59. Tests de confidentialité

Vérifier :

* aucun téléphone obligatoire pour une vente anonyme ;
* aucune donnée personnelle inutile dans les logs ;
* aucune donnée client d'un Merchant exposée à un autre Merchant ;
* aucun partage automatique de l'identité client entre Merchants ;
* données personnelles minimisées.

---

# 60. Definition of Done

Le module Client est terminé lorsque :

* [ ] une vente peut être réalisée sans Client enregistré ;
* [ ] le Client anonyme fonctionne ;
* [ ] un téléphone peut être associé facultativement ;
* [ ] un nom peut être associé facultativement ;
* [ ] un Client peut être retrouvé par téléphone selon les permissions ;
* [ ] l'historique commercial peut être consulté selon les permissions ;
* [ ] les ventes conservent leurs données historiques ;
* [ ] les paiements espèces fonctionnent ;
* [ ] les paiements digitaux fonctionnent ;
* [ ] un reçu peut être produit sans compte client ;
* [ ] un reçu peut être envoyé avec un contact fourni ;
* [ ] l'isolation des données entre Merchants est assurée ;
* [ ] les données personnelles sont minimisées ;
* [ ] aucun compte client n'est obligatoire pour acheter ;
* [ ] aucun wallet client n'est créé ;
* [ ] aucun programme de fidélité n'est introduit en V1.

---

# 61. Règles Hermes

Hermes doit respecter impérativement :

### Règle 1

Le Client commercial n'est pas nécessairement un utilisateur MAGIC GOLD.

### Règle 2

Un achat doit être possible avec un **Client anonyme**.

### Règle 3

Le téléphone du Client est facultatif pour une vente ordinaire.

### Règle 4

L'email du Client est facultatif.

### Règle 5

Le nom du Client est facultatif pour une vente ordinaire.

### Règle 6

Ne jamais imposer la création d'un compte pour effectuer un achat au détail.

### Règle 7

Un numéro de téléphone ne doit pas automatiquement identifier un Client global MAGIC GOLD.

### Règle 8

Les données Client sont principalement rattachées au périmètre commercial du Merchant en V1.

### Règle 9

Un Merchant ne voit jamais automatiquement les clients ou achats d'un autre Merchant.

### Règle 10

Le Client ne possède aucun Wallet MAGIC GOLD V1.

### Règle 11

Ne pas introduire de fidélité, points ou cashback en V1.

### Règle 12

Le système doit privilégier la rapidité de vente en commerce de détail.

### Règle 13

Une vente doit pouvoir être réalisée avec :

```text
Produit
+
Quantité
+
Prix calculé
+
Paiement
```

sans collecte obligatoire de données personnelles.

### Règle 14

Le Client commercial et le compte utilisateur MAGIC GOLD doivent rester deux concepts distincts.

---

# 62. Relations avec les autres spécifications

Le module Customer dépend notamment de :

```text
04_DataModel.md
05_API_Specification.md
06_SecurityArchitecture.md
07_UI_UX_Specification.md
08_Notifications.md
09_PaymentArchitecture.md
10_OrderManagement.md
11_MerchantSpecification.md
```

Il doit également être cohérent avec les futurs modules :

```text
13_Roles_Permissions_Audit.md
14_Reporting.md
15_IntegrationSpecification.md
16_TestStrategy.md
17_Deployment.md
```

Le modèle **Vente / POS / Caisse** devra être pris en compte dans les spécifications suivantes si MAGIC GOLD confirme son orientation commerce de détail.

---

# 63. Architecture cible

```text
                         MAGIC GOLD
                              │
                  ┌───────────┴───────────┐
                  │                       │
               MERCHANT               CUSTOMER
                  │                       │
          ┌───────┼───────┐               │
          │       │       │               │
          ▼       ▼       ▼               │
       PRODUCTS  STOCK   CAISSE            │
          │       │       │               │
          └───────┼───────┘               │
                  ▼                       │
                VENTE ◄──────────────────┘
                  │
          ┌───────┴────────┐
          │                │
          ▼                ▼
       PAYMENT           RECEIPT
          │
          ▼
   PAYMENT PROVIDER
```

---

# 64. Principe final

Dans MAGIC GOLD, le Client est avant tout :

> **une personne qui achète auprès d'un commerce.**

Il peut être :

```text
ANONYME
    ↓
IDENTIFIÉ
    ↓
CLIENT RÉGULIER
```

sans que ces états nécessitent obligatoirement un compte MAGIC GOLD.

Le principe V1 est :

```text
ACHETER DOIT ÊTRE SIMPLE.
```

MAGIC GOLD doit donc permettre au commerçant de :

> **chercher le produit → vendre → encaisser → produire le reçu → éventuellement associer un contact client.**

Le compte client, l'application client, la fidélité et les services avancés restent des extensions possibles et ne doivent pas complexifier le commerce de détail V1.
