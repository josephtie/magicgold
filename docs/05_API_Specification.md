# MAGIC GOLD — API Specification

**Version:** 1.0
**Document:** `05_API_Specification.md`
**Projet:** MAGIC GOLD
**Statut:** Référence API
**Audience:** Backend, Frontend, Mobile, QA, DevOps, intégrations externes, Hermes

---

# 1. Objectif

Ce document définit le contrat API de MAGIC GOLD.

Il décrit :

* l'architecture API ;
* les conventions REST ;
* l'authentification ;
* l'autorisation ;
* les endpoints ;
* les paramètres ;
* les Request DTO ;
* les Response DTO ;
* les erreurs ;
* la pagination ;
* les filtres ;
* l'idempotence ;
* les webhooks ;
* la sécurité ;
* les règles de versionnement ;
* les exigences de tests.

L'API constitue le contrat entre le backend MAGIC GOLD et :

```text
Web Application
Mobile Application
Back-office
Merchant Portal
Payment Providers
Notification Providers
Applications partenaires
Services internes
```

---

# 2. Principe général

Architecture :

```text
                    ┌────────────────────┐
                    │   Mobile App       │
                    └─────────┬──────────┘
                              │
                    ┌─────────▼──────────┐
                    │   Web Frontend     │
                    └─────────┬──────────┘
                              │
                              ▼
                    ┌────────────────────┐
                    │    MAGIC GOLD API  │
                    │      Backend       │
                    └─────────┬──────────┘
                              │
          ┌───────────────────┼───────────────────┐
          │                   │                   │
          ▼                   ▼                   ▼
       Database          Identity Provider    Services externes
                            Keycloak          Paiement / SMS
```

Le frontend ne doit jamais accéder directement à la base de données.

---

# 3. Style API

MAGIC GOLD utilise une API :

```text
REST
JSON
HTTPS
UTF-8
OAuth 2.0 / OpenID Connect
JWT
```

Format recommandé :

```text
/api/v1/...
```

Exemple :

```text
GET /api/v1/customers/me
```

---

# 4. Base URL

La Base URL doit être configurable par environnement.

Exemple :

```text
Development
https://api-dev.magicgold.example

Staging
https://api-staging.magicgold.example

Production
https://api.magicgold.example
```

Les domaines réels seront définis dans la configuration de déploiement.

Le frontend ne doit jamais coder en dur l'URL de production.

---

# 5. Versionnement

La version doit être présente dans l'URL :

```text
/api/v1/
```

Exemple :

```text
/api/v1/customers
```

Une modification incompatible nécessite une nouvelle version :

```text
/api/v2/customers
```

Les changements compatibles peuvent rester dans la même version.

---

# 6. Headers standards

## Requête

```http
Authorization: Bearer <JWT>
Content-Type: application/json
Accept: application/json
X-Request-ID: <uuid>
X-Correlation-ID: <uuid>
```

## Pour opérations idempotentes

```http
Idempotency-Key: <unique-key>
```

---

# 7. Authentication

L'authentification est basée sur OAuth 2.0 / OpenID Connect.

Le fournisseur d'identité recommandé est :

```text
Keycloak
```

Architecture :

```text
Client
   │
   │ Login
   ▼
Keycloak
   │
   │ Access Token
   ▼
MAGIC GOLD API
```

Le backend doit vérifier :

```text
signature
issuer
audience
expiration
not-before
roles/scopes
```

---

# 8. JWT

Le JWT doit permettre au backend d'identifier :

```text
user_id
roles
scopes
issuer
audience
expiration
```

Le backend ne doit jamais faire confiance à un identifiant utilisateur envoyé arbitrairement dans le body lorsque l'utilisateur connecté peut être déterminé à partir du token.

Exemple incorrect :

```json
{
  "customerId": "someone-else"
}
```

pour une opération `/customers/me`.

Le backend doit déterminer le client à partir de l'identité authentifiée.

---

# 9. Autorisation

L'autorisation repose sur :

```text
Authentication
+
Role
+
Permission
+
Resource ownership
```

Exemple :

```text
CUSTOMER
→ accès à ses propres commandes

MERCHANT
→ accès aux données de son commerce

MERCHANT_OPERATOR
→ accès limité aux opérations autorisées

ADMIN
→ administration

FINANCE
→ opérations financières autorisées

AUDITOR
→ consultation des données d'audit
```

---

# 10. Règle fondamentale de sécurité

Une permission ne doit jamais être contrôlée uniquement dans le frontend.

Incorrect :

```text
if (user.role === "ADMIN") {
   afficher bouton
}
```

Le bouton peut être masqué côté frontend, mais le backend doit toujours effectuer le contrôle.

---

# 11. Format de réponse standard

Réponse de succès :

```json
{
  "success": true,
  "data": {},
  "meta": {
    "requestId": "uuid"
  }
}
```

Réponse paginée :

```json
{
  "success": true,
  "data": [],
  "meta": {
    "page": 0,
    "size": 20,
    "totalElements": 125,
    "totalPages": 7,
    "requestId": "uuid"
  }
}
```

---

# 12. Format d'erreur

```json
{
  "success": false,
  "error": {
    "code": "RESOURCE_NOT_FOUND",
    "message": "Resource not found",
    "details": null
  },
  "meta": {
    "requestId": "uuid"
  }
}
```

Le message destiné à l'utilisateur ne doit pas exposer :

```text
SQL
stack trace
secret
token
nom de table
information interne
```

---

# 13. Codes HTTP

| Code | Usage                      |
| ---: | -------------------------- |
|  200 | Succès                     |
|  201 | Ressource créée            |
|  202 | Traitement accepté         |
|  204 | Succès sans contenu        |
|  400 | Requête invalide           |
|  401 | Non authentifié            |
|  403 | Accès interdit             |
|  404 | Ressource inexistante      |
|  409 | Conflit                    |
|  422 | Validation métier          |
|  429 | Trop de requêtes           |
|  500 | Erreur interne             |
|  502 | Erreur fournisseur externe |
|  503 | Service indisponible       |

---

# 14. Codes d'erreur métier

Les codes doivent être stables même si le texte du message change.

Exemples :

```text
AUTHENTICATION_REQUIRED
ACCESS_DENIED
RESOURCE_NOT_FOUND
VALIDATION_ERROR
DUPLICATE_RESOURCE
INVALID_STATUS_TRANSITION
INSUFFICIENT_BALANCE
PAYMENT_FAILED
PAYMENT_PENDING
PAYMENT_ALREADY_PROCESSED
IDEMPOTENCY_CONFLICT
INVALID_COUPON
COUPON_EXPIRED
REWARD_UNAVAILABLE
INSUFFICIENT_POINTS
MERCHANT_INACTIVE
PRODUCT_UNAVAILABLE
ORDER_ALREADY_CANCELLED
WEBHOOK_ALREADY_PROCESSED
RATE_LIMIT_EXCEEDED
INTERNAL_ERROR
```

---

# 15. Pagination

Les endpoints de collection doivent supporter :

```text
page
size
sort
```

Exemple :

```text
GET /api/v1/orders?page=0&size=20&sort=createdAt,desc
```

Valeur maximale recommandée :

```text
size <= 100
```

Le backend doit empêcher des tailles excessives.

---

# 16. Filtrage

Exemple :

```text
GET /api/v1/orders?
status=COMPLETED
&merchantId=...
&from=2026-01-01
&to=2026-01-31
```

Les paramètres disponibles doivent être documentés par endpoint.

---

# 17. Recherche

Pour les recherches textuelles :

```text
GET /api/v1/products?search=boisson
```

La recherche doit être effectuée côté backend.

Le frontend ne doit pas télécharger toutes les données pour effectuer une recherche locale lorsque le volume peut devenir important.

---

# 18. CUSTOMER API

## 18.1 Profil courant

```http
GET /api/v1/customers/me
```

### Response

```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "customerNumber": "MG-CUS-000001",
    "firstName": "Jean",
    "lastName": "Kouassi",
    "phoneNumber": "+2250700000000",
    "email": "jean@example.com",
    "status": "ACTIVE"
  }
}
```

---

# 19. Modifier le profil

```http
PATCH /api/v1/customers/me
```

### Request

```json
{
  "firstName": "Jean",
  "lastName": "Kouassi",
  "email": "jean@example.com"
}
```

Le backend doit valider les champs modifiables.

Les champs sensibles ou administratifs ne doivent pas être modifiables via cet endpoint.

---

# 20. Adresses client

```http
GET /api/v1/customers/me/addresses
```

```http
POST /api/v1/customers/me/addresses
```

```http
PATCH /api/v1/customers/me/addresses/{addressId}
```

```http
DELETE /api/v1/customers/me/addresses/{addressId}
```

---

# 21. Devices

```http
POST /api/v1/customers/me/devices
```

```json
{
  "deviceIdentifier": "device-id",
  "platform": "ANDROID",
  "appVersion": "1.0.0",
  "pushToken": "token"
}
```

---

# 22. MERCHANT API

## Liste commerçants

```http
GET /api/v1/merchants
```

Filtres :

```text
search
city
district
businessType
status
```

---

# 23. Détail commerçant

```http
GET /api/v1/merchants/{merchantId}
```

---

# 24. Création commerçant

```http
POST /api/v1/merchants
```

### Request

```json
{
  "legalName": "Entreprise Exemple SARL",
  "commercialName": "MAGIC SHOP",
  "businessType": "RETAIL",
  "registrationNumber": "CI-XXXX",
  "taxNumber": "XXXX",
  "phoneNumber": "+2250700000000",
  "email": "contact@example.com",
  "address": "Abidjan"
}
```

---

# 25. Merchant outlets

```http
GET /api/v1/merchants/{merchantId}/outlets
```

```http
POST /api/v1/merchants/{merchantId}/outlets
```

```http
GET /api/v1/merchants/{merchantId}/outlets/{outletId}
```

```http
PATCH /api/v1/merchants/{merchantId}/outlets/{outletId}
```

---

# 26. PRODUCT API

## Liste produits

```http
GET /api/v1/products
```

Filtres :

```text
merchantId
categoryId
status
search
minPrice
maxPrice
```

---

# 27. Produit

```http
GET /api/v1/products/{productId}
```

---

# 28. Création produit

```http
POST /api/v1/merchants/{merchantId}/products
```

### Request

```json
{
  "categoryId": "uuid",
  "sku": "SKU-001",
  "name": "Produit Exemple",
  "description": "Description",
  "unitPrice": 5000,
  "currency": "XOF",
  "stockQuantity": 100
}
```

Le backend doit vérifier que l'utilisateur est autorisé à créer un produit pour ce commerçant.

---

# 29. Modification produit

```http
PATCH /api/v1/products/{productId}
```

---

# 30. ORDER API

## Création commande

```http
POST /api/v1/orders
```

### Header

```http
Idempotency-Key: unique-key
```

### Request

```json
{
  "merchantId": "uuid",
  "outletId": "uuid",
  "items": [
    {
      "productId": "uuid",
      "quantity": 2
    }
  ],
  "couponCode": "WELCOME10"
}
```

Le frontend ne doit pas envoyer comme source de vérité :

```text
unitPrice
subtotal
tax
total
```

Le backend doit recalculer ces valeurs.

---

# 31. Récupérer une commande

```http
GET /api/v1/orders/{orderId}
```

---

# 32. Commandes du client

```http
GET /api/v1/customers/me/orders
```

Filtres :

```text
status
from
to
merchantId
```

---

# 33. Commandes commerçant

```http
GET /api/v1/merchants/{merchantId}/orders
```

Filtres :

```text
status
outletId
from
to
customerId
```

L'autorisation doit empêcher l'accès aux commandes d'un autre commerçant.

---

# 34. Confirmation commande

```http
POST /api/v1/orders/{orderId}/confirm
```

---

# 35. Annulation commande

```http
POST /api/v1/orders/{orderId}/cancel
```

### Request

```json
{
  "reason": "Customer request"
}
```

Le backend vérifie la transition d'état.

---

# 36. PAYMENT API

## Initier un paiement

```http
POST /api/v1/payments
```

### Header

```http
Idempotency-Key: payment-request-unique-id
```

### Request

```json
{
  "orderId": "uuid",
  "providerCode": "CINETPAY",
  "paymentMethod": "MOBILE_MONEY",
  "returnUrl": "https://app.example/payment/result"
}
```

Le montant est calculé par le backend à partir de la commande.

Le client ne doit pas pouvoir imposer :

```json
{
  "amount": 100
}
```

pour payer une commande de 100 000 XOF.

---

# 37. Response paiement

```json
{
  "success": true,
  "data": {
    "paymentId": "uuid",
    "paymentNumber": "MG-PAY-000001",
    "status": "PENDING",
    "amount": 10000,
    "currency": "XOF",
    "paymentMethod": "MOBILE_MONEY",
    "checkoutUrl": "https://provider.example/checkout"
  }
}
```

---

# 38. Consulter paiement

```http
GET /api/v1/payments/{paymentId}
```

---

# 39. Paiements d'une commande

```http
GET /api/v1/orders/{orderId}/payments
```

---

# 40. Remboursement

```http
POST /api/v1/payments/{paymentId}/refund
```

### Request

```json
{
  "amount": 5000,
  "reason": "Approved refund"
}
```

Le remboursement doit être soumis à une permission appropriée.

---

# 41. PAYMENT WEBHOOK

Les prestataires externes appellent :

```http
POST /api/v1/webhooks/payments/{providerCode}
```

Exemple :

```http
POST /api/v1/webhooks/payments/cinetpay
```

Le système doit :

```text
1. recevoir
2. vérifier la signature
3. identifier l'événement
4. vérifier l'idempotence
5. enregistrer WebhookEvent
6. traiter la transaction
7. mettre à jour Payment
8. mettre à jour Order
9. mettre à jour Wallet si nécessaire
10. générer AuditLog
11. envoyer Notification
```

---

# 42. Idempotence paiement

Si le même webhook arrive deux fois :

```text
Webhook #1
→ traitement

Webhook #2
→ déjà traité
→ aucun nouveau crédit
→ aucun nouveau débit
```

La réponse peut rester un succès technique afin d'éviter les retries inutiles du fournisseur.

---

# 43. WALLET API

## Portefeuille courant

```http
GET /api/v1/wallet
```

### Response

```json
{
  "success": true,
  "data": {
    "walletNumber": "MG-WAL-000001",
    "currency": "XOF",
    "availableBalance": 25000,
    "pendingBalance": 0,
    "status": "ACTIVE"
  }
}
```

---

# 44. Transactions wallet

```http
GET /api/v1/wallet/transactions
```

Filtres :

```text
type
direction
status
from
to
```

---

# 45. Dépôt wallet

Le dépôt peut être déclenché via un paiement externe :

```http
POST /api/v1/wallet/deposits
```

Le traitement financier doit utiliser le même mécanisme d'idempotence que les paiements.

---

# 46. Transfert wallet

```http
POST /api/v1/wallet/transfers
```

### Request

```json
{
  "recipient": "MG-CUS-000002",
  "amount": 5000,
  "currency": "XOF"
}
```

### Règles

Le backend vérifie :

```text
wallet actif
montant positif
solde suffisant
destinataire valide
limites transactionnelles
idempotence
```

---

# 47. LOYALTY API

## Compte fidélité

```http
GET /api/v1/loyalty/account
```

### Response

```json
{
  "success": true,
  "data": {
    "pointsBalance": 1250,
    "lifetimePointsEarned": 5000,
    "lifetimePointsSpent": 3750,
    "tier": "GOLD",
    "status": "ACTIVE"
  }
}
```

---

# 48. Historique fidélité

```http
GET /api/v1/loyalty/transactions
```

Filtres :

```text
type
from
to
```

---

# 49. REWARD API

## Liste récompenses

```http
GET /api/v1/rewards
```

---

# 50. Détail récompense

```http
GET /api/v1/rewards/{rewardId}
```

---

# 51. Utiliser une récompense

```http
POST /api/v1/rewards/{rewardId}/redeem
```

### Header

```http
Idempotency-Key: unique-redemption-key
```

### Request

```json
{}
```

Le backend vérifie :

```text
reward ACTIVE
date valide
quantité disponible
points suffisants
client actif
```

---

# 52. Coupons

## Vérifier coupon

```http
POST /api/v1/coupons/validate
```

### Request

```json
{
  "code": "WELCOME10",
  "merchantId": "uuid",
  "cartAmount": 25000
}
```

La validation ne doit pas nécessairement consommer le coupon.

---

# 53. Utilisation coupon

La consommation définitive doit intervenir au moment approprié du processus de commande.

Elle doit être atomique avec la commande afin d'éviter :

```text
usage_count dépassé
coupon utilisé deux fois
```

---

# 54. CAMPAIGN API

## Liste campagnes

```http
GET /api/v1/campaigns
```

## Créer campagne

```http
POST /api/v1/campaigns
```

## Modifier campagne

```http
PATCH /api/v1/campaigns/{campaignId}
```

## Activer campagne

```http
POST /api/v1/campaigns/{campaignId}/activate
```

## Désactiver campagne

```http
POST /api/v1/campaigns/{campaignId}/deactivate
```

---

# 55. NOTIFICATION API

## Mes notifications

```http
GET /api/v1/notifications
```

## Marquer comme lue

```http
POST /api/v1/notifications/{notificationId}/read
```

## Tout marquer comme lu

```http
POST /api/v1/notifications/read-all
```

---

# 56. ADMIN API

Les APIs d'administration doivent être protégées par des permissions élevées.

Exemples :

```text
GET /api/v1/admin/users
GET /api/v1/admin/merchants
GET /api/v1/admin/payments
GET /api/v1/admin/audit-logs
GET /api/v1/admin/configurations
```

---

# 57. User administration

```http
GET /api/v1/admin/users
```

```http
GET /api/v1/admin/users/{userId}
```

```http
PATCH /api/v1/admin/users/{userId}/status
```

```http
POST /api/v1/admin/users/{userId}/roles
```

```http
DELETE /api/v1/admin/users/{userId}/roles/{roleId}
```

Toutes les opérations sensibles doivent générer un audit.

---

# 58. Audit API

```http
GET /api/v1/admin/audit-logs
```

Filtres :

```text
userId
action
entityType
entityId
from
to
```

L'accès doit être réservé aux rôles autorisés.

---

# 59. Configuration API

```http
GET /api/v1/admin/configurations
```

```http
PATCH /api/v1/admin/configurations/{key}
```

Les secrets ne doivent pas être retournés par cette API.

---

# 60. File API

## Upload

```http
POST /api/v1/files
```

Content-Type :

```text
multipart/form-data
```

Réponse :

```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "originalName": "logo.png",
    "mimeType": "image/png",
    "size": 12345
  }
}
```

Les types et tailles de fichiers autorisés doivent être configurables.

---

# 61. DTO — principe

Les entités de persistence ne doivent pas être exposées directement comme Response DTO.

Architecture :

```text
Entity
   ↓
Mapper
   ↓
Response DTO
```

Exemple :

```text
CustomerEntity
      ↓
CustomerMapper
      ↓
CustomerResponse
```

---

# 62. Request DTO

Les Request DTO doivent :

* contenir uniquement les champs nécessaires ;
* utiliser validation ;
* ne pas accepter les champs calculés ;
* ne pas accepter les champs système ;
* ne pas accepter les identifiants appartenant implicitement à l'utilisateur connecté.

---

# 63. Response DTO

Les Response DTO doivent :

* exposer uniquement les données nécessaires ;
* masquer les données sensibles ;
* être stables ;
* éviter les références circulaires ;
* éviter d'exposer directement les entités JPA.

---

# 64. Validation

Les validations doivent être effectuées au backend.

Exemples :

```text
@NotNull
@NotBlank
@Email
@Positive
@PositiveOrZero
@Size
@Pattern
```

Les validations complexes doivent être implémentées dans la couche métier.

---

# 65. Validation métier

Exemple :

```text
POST /orders
```

Le backend doit vérifier :

```text
customer actif
merchant actif
outlet actif
product actif
stock disponible
prix actuel
coupon valide
commande cohérente
```

Puis calculer :

```text
subtotal
discount
tax
total
```

---

# 66. Transactionnalité

Les opérations suivantes doivent être transactionnelles lorsqu'elles impliquent plusieurs modifications cohérentes :

```text
création commande
paiement
remboursement
wallet transaction
transfert wallet
gain de points
utilisation de points
redemption reward
utilisation coupon
```

---

# 67. Concurrence

Les opérations sensibles doivent gérer les accès concurrents.

Exemples :

```text
deux paiements simultanés
deux redemptions du même reward
deux utilisations du même coupon
deux retraits wallet
```

Les mécanismes possibles :

```text
database transaction
optimistic locking
pessimistic locking
unique constraints
idempotency
```

Le mécanisme choisi doit être documenté dans l'architecture technique.

---

# 68. Rate limiting

Les endpoints sensibles doivent être protégés contre l'abus.

Priorité élevée :

```text
login
OTP
payment
wallet transfer
coupon validation
reward redemption
webhooks
```

Une réponse de dépassement :

```http
429 Too Many Requests
```

---

# 69. CORS

Le backend doit limiter les origines autorisées.

En production :

```text
NE PAS utiliser
Access-Control-Allow-Origin: *
```

sauf justification documentée.

---

# 70. CORS — environnements

Exemple :

```text
Development
http://localhost:3000

Staging
https://staging.magicgold.example

Production
https://app.magicgold.example
```

Les valeurs doivent être configurables.

---

# 71. Journalisation API

Chaque requête importante doit pouvoir être corrélée grâce à :

```text
X-Request-ID
X-Correlation-ID
```

Les logs ne doivent jamais contenir :

```text
password
JWT complet
API secret
carte bancaire complète
OTP
données sensibles inutiles
```

---

# 72. Observabilité

Les métriques API doivent permettre de suivre :

```text
request_count
error_count
latency
status_code
endpoint
payment_success_rate
payment_failure_rate
webhook_processing
```

---

# 73. Health endpoints

## Liveness

```http
GET /actuator/health/liveness
```

## Readiness

```http
GET /actuator/health/readiness
```

Ces endpoints ne doivent pas exposer de secrets ou informations internes inutiles.

---

# 74. OpenAPI

L'API doit être documentée avec OpenAPI.

Exemple :

```text
/api-docs
/swagger-ui
```

Les URLs exactes dépendent de l'implémentation.

OpenAPI doit décrire :

```text
endpoints
parameters
request bodies
responses
authentication
errors
schemas
```

---

# 75. Exemple OpenAPI conceptuel

```yaml
paths:
  /api/v1/orders:
    post:
      summary: Create an order
      security:
        - bearerAuth: []
      requestBody:
        required: true
      responses:
        '201':
          description: Order created
        '400':
          description: Invalid request
        '401':
          description: Unauthorized
        '409':
          description: Conflict
```

Le fichier OpenAPI généré par le backend doit rester synchronisé avec le code.

---

# 76. API publique vs API interne

Les endpoints doivent être classés :

```text
PUBLIC
AUTHENTICATED
MERCHANT
ADMIN
INTERNAL
WEBHOOK
```

Exemple :

```text
GET /products
→ PUBLIC ou AUTHENTICATED selon décision produit

GET /customers/me
→ AUTHENTICATED

GET /admin/audit-logs
→ ADMIN/AUDITOR

POST /webhooks/payments/...
→ WEBHOOK
```

---

# 77. Ownership

Pour toute ressource appartenant à un utilisateur :

```text
customer_id
```

doit être contrôlé côté backend.

Exemple :

```http
GET /api/v1/orders/{orderId}
```

Un client ne doit pas pouvoir consulter une commande qui appartient à un autre client simplement en connaissant son UUID.

---

# 78. Prévention IDOR

Chaque accès par ID doit vérifier :

```text
authenticated user
+
role
+
permission
+
resource ownership
```

Ne jamais considérer :

```text
UUID valide = autorisation
```

---

# 79. Pagination — Response

Exemple :

```json
{
  "success": true,
  "data": [
    {
      "id": "uuid",
      "name": "Produit A"
    }
  ],
  "meta": {
    "page": 0,
    "size": 20,
    "totalElements": 125,
    "totalPages": 7,
    "hasNext": true,
    "hasPrevious": false,
    "requestId": "uuid"
  }
}
```

---

# 80. Tri

Format recommandé :

```text
sort=createdAt,desc
```

Plusieurs tris :

```text
sort=status,asc&sort=createdAt,desc
```

Le backend doit limiter les champs pouvant être utilisés pour le tri.

---

# 81. Filtrage temporel

Format recommandé :

```text
from=2026-01-01T00:00:00Z
to=2026-01-31T23:59:59Z
```

Le backend doit valider :

```text
from <= to
```

---

# 82. Transactions externes

Toute intégration externe doit être encapsulée derrière un service interne.

Exemple :

```text
PaymentController
      ↓
PaymentService
      ↓
PaymentProviderService
      ↓
CinetPayAdapter
```

Le contrôleur ne doit pas contenir directement la logique spécifique à CinetPay, Wave, Orange Money ou autre fournisseur.

---

# 83. Payment Provider Adapter

Architecture :

```text
PaymentService
      │
      ├── CinetPayAdapter
      ├── FeexPayAdapter
      ├── OtherProviderAdapter
      └── MockPaymentAdapter
```

Cela permet de changer ou ajouter un fournisseur sans réécrire le domaine métier.

---

# 84. Webhook Security

Chaque fournisseur doit être validé selon son mécanisme de sécurité :

```text
signature
HMAC
token
IP filtering lorsque pertinent
timestamp
nonce
```

Le système ne doit pas considérer une requête webhook comme fiable uniquement parce qu'elle utilise une URL secrète.

---

# 85. Webhook Retry

Un événement échoué doit pouvoir être retraité lorsque cela est approprié.

Exemple :

```text
RECEIVED
   ↓
PROCESSING
   ↓
PROCESSED
```

ou :

```text
RECEIVED
   ↓
FAILED
   ↓
RETRY
   ↓
PROCESSED
```

Le retry doit rester idempotent.

---

# 86. API de reporting

Les rapports lourds ne doivent pas nécessairement utiliser les mêmes endpoints transactionnels.

Exemple :

```text
GET /api/v1/admin/reports/sales
GET /api/v1/admin/reports/payments
GET /api/v1/admin/reports/loyalty
```

Les réponses peuvent être agrégées.

Les requêtes coûteuses doivent être contrôlées et éventuellement exécutées de manière asynchrone.

---

# 87. Export

Exemple :

```http
POST /api/v1/admin/reports/sales/export
```

Le système peut retourner :

```json
{
  "success": true,
  "data": {
    "jobId": "uuid",
    "status": "PENDING"
  }
}
```

Puis :

```http
GET /api/v1/admin/jobs/{jobId}
```

---

# 88. API asynchrone

Les traitements longs doivent utiliser :

```text
202 Accepted
```

Exemples :

```text
bulk import
large export
mass notification
reconciliation
large report
```

---

# 89. Bulk operations

Les opérations massives doivent être explicitement protégées.

Exemple :

```http
POST /api/v1/admin/notifications/bulk
```

Les limites doivent être définies.

---

# 90. API mobile

L'application mobile doit utiliser les mêmes APIs métier principales.

Architecture :

```text
Mobile
   ↓
REST API
   ↓
Business Services
   ↓
Database
```

Le mobile ne doit pas disposer d'un accès privilégié simplement parce qu'il utilise une application officielle.

---

# 91. Offline

Si une fonctionnalité offline est introduite ultérieurement :

```text
local operation
     ↓
sync queue
     ↓
API
     ↓
idempotency
     ↓
server reconciliation
```

Elle ne doit pas permettre de contourner les règles financières du serveur.

---

# 92. Sécurité des fichiers

Pour les uploads :

```text
extension
mime type
size
checksum
content validation
```

doivent être contrôlés.

Les fichiers ne doivent pas être servis directement depuis un chemin filesystem prévisible.

---

# 93. API de support

Le support peut disposer d'APIs spécifiques :

```text
GET /api/v1/support/customers/{customerId}
GET /api/v1/support/orders/{orderId}
GET /api/v1/support/payments/{paymentId}
```

Les informations sensibles doivent être masquées.

---

# 94. API de réconciliation

Pour les paiements :

```http
POST /api/v1/admin/payments/reconciliation
```

Cette fonctionnalité permet de comparer :

```text
MAGIC GOLD
vs
Payment Provider
```

Les opérations de correction doivent être auditées.

---

# 95. Compatibilité frontend

Les APIs doivent éviter les changements cassants non versionnés.

Ne pas :

```text
supprimer brutalement un champ
changer le type d'un champ
changer une valeur enum sans migration
```

sans stratégie de compatibilité.

---

# 96. Enum API

Les enums doivent être documentés.

Exemple :

```json
{
  "status": "PENDING"
}
```

Le frontend doit gérer les valeurs inconnues de manière robuste.

---

# 97. Nullabilité

La documentation OpenAPI doit préciser les champs :

```text
required
nullable
optional
```

Exemple :

```text
email
→ optional

customerNumber
→ required

completedAt
→ nullable
```

---

# 98. API Contract Testing

Les tests doivent vérifier :

```text
status code
schema
required fields
field types
enum values
security
authorization
error format
pagination
```

---

# 99. Tests API minimum

## Authentication

```text
[ ] accès sans token refusé
[ ] token expiré refusé
[ ] token invalide refusé
[ ] rôle insuffisant refusé
```

## Customer

```text
[ ] client peut voir son profil
[ ] client ne peut pas voir celui d'un autre
```

## Orders

```text
[ ] commande valide créée
[ ] produit inexistant refusé
[ ] stock insuffisant refusé
[ ] total recalculé côté serveur
[ ] coupon invalide refusé
```

## Payments

```text
[ ] paiement créé
[ ] idempotence
[ ] webhook validé
[ ] webhook dupliqué ignoré
[ ] paiement échoué traité
```

## Wallet

```text
[ ] solde visible
[ ] débit impossible si solde insuffisant
[ ] transaction créée
[ ] double débit impossible
```

## Loyalty

```text
[ ] points crédités
[ ] points débités
[ ] solde insuffisant refusé
[ ] double redemption impossible
```

---

# 100. Performance API

Les endpoints critiques doivent être surveillés.

Priorités :

```text
authentication
products
orders
payments
wallet
loyalty
```

Éviter :

```text
N+1 queries
large payloads
full table scans
unbounded collections
```

---

# 101. Caching

Les données peu volatiles peuvent être mises en cache :

```text
product categories
public merchant information
reward catalog
configuration publique
```

Les données criti
