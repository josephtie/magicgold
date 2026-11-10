# MAGIC GOLD — Notification Specification

**Version:** 1.0
**Document:** `08_Notifications.md`
**Projet:** MAGIC GOLD
**Statut:** Référence fonctionnelle et technique
**Audience:** Product, Backend, Frontend, Mobile, QA, DevOps, Hermes

---

# 1. Objectif

Ce document définit l'architecture fonctionnelle et technique du système de notifications MAGIC GOLD.

Le système doit permettre d'informer les utilisateurs au bon moment, via le canal approprié, sans générer de notifications inutiles ou répétitives.

Les canaux peuvent inclure :

```text
IN_APP
EMAIL
SMS
PUSH
```

Le système doit également assurer :

* la traçabilité ;
* la gestion des préférences ;
* la gestion des priorités ;
* les retries ;
* la gestion des échecs ;
* la sécurité ;
* l'idempotence ;
* l'audit ;
* la supervision.

---

# 2. Principes

Le système suit les principes :

```text
Useful
Relevant
Secure
Reliable
Traceable
Non-intrusive
User-controlled
```

Une notification doit avoir une raison métier identifiable.

---

# 3. Architecture générale

```text
Business Event
      │
      ▼
Notification Service
      │
      ├── Preference Engine
      │
      ├── Template Engine
      │
      ├── Priority Engine
      │
      ├── Delivery Queue
      │
      └── Audit
             │
             ▼
      ┌──────┼────────┬────────┐
      ▼      ▼        ▼        ▼
   In-App   Email     SMS     Push
```

---

# 4. Événement métier

Une notification doit normalement être déclenchée par un événement métier.

Exemples :

```text
ORDER_CREATED
ORDER_CONFIRMED
ORDER_CANCELLED

PAYMENT_INITIATED
PAYMENT_SUCCESS
PAYMENT_FAILED
PAYMENT_REFUNDED

WALLET_CREDITED
WALLET_DEBITED
WALLET_TRANSFER_COMPLETED

LOYALTY_POINTS_EARNED
LOYALTY_REWARD_REDEEMED

ACCOUNT_CREATED
PASSWORD_CHANGED
LOGIN_DETECTED

MERCHANT_APPROVED
MERCHANT_SUSPENDED
```

---

# 5. Event → Notification

Principe :

```text
Business Event
      ↓
Determine recipients
      ↓
Determine notification type
      ↓
Check preferences
      ↓
Select channels
      ↓
Render template
      ↓
Queue
      ↓
Deliver
      ↓
Track result
```

---

# 6. Notification Types

Les notifications sont regroupées en catégories.

```text
TRANSACTIONAL
ORDER
PAYMENT
WALLET
LOYALTY
SECURITY
ACCOUNT
MERCHANT
PROMOTION
SYSTEM
SUPPORT
```

---

# 7. Priorités

Chaque notification possède une priorité.

```text
CRITICAL
HIGH
NORMAL
LOW
```

### CRITICAL

Réservé notamment à :

```text
Security incident
Account compromise
Critical financial event
Mandatory system notification
```

### HIGH

Exemples :

```text
Payment result
Wallet transaction
Order cancellation
Security change
```

### NORMAL

Exemples :

```text
Order update
Loyalty points
Merchant activity
```

### LOW

Exemples :

```text
Promotional notification
Recommendation
Non-critical reminder
```

---

# 8. Règle de priorité

Une notification de sécurité ou financière ne doit jamais être masquée par une notification promotionnelle.

Ordre :

```text
CRITICAL
   ↓
HIGH
   ↓
NORMAL
   ↓
LOW
```

---

# 9. Canaux

## 9.1 In-App

Le canal principal pour les événements nécessitant une visibilité dans l'application.

Exemples :

```text
Order update
Payment status
Wallet
Loyalty
System
```

---

## 9.2 Email

Utilisé pour :

```text
Account
Security
Receipts
Invoices
Important order information
Reports
Selected marketing communication
```

---

## 9.3 SMS

Le SMS doit être réservé aux notifications nécessitant une forte probabilité de lecture ou lorsque l'utilisateur n'est pas connecté.

Exemples :

```text
OTP
Security alert
Critical payment notification
Important transaction
```

Les notifications marketing par SMS doivent respecter les préférences utilisateur et les règles applicables.

---

## 9.4 Push

Utilisé principalement pour :

```text
Order status
Payment status
Promotions
Loyalty
Security
Reminders
```

---

# 10. Matrice événement-canal

| Événement             | In-App |     Email |             SMS | Push |
| --------------------- | -----: | --------: | --------------: | ---: |
| ACCOUNT_CREATED       |      ✓ |         ✓ |               — |    ✓ |
| PASSWORD_CHANGED      |      ✓ |         ✓ |       Optionnel |    ✓ |
| LOGIN_DETECTED        |      ✓ |         ✓ |    Selon risque |    ✓ |
| ORDER_CREATED         |      ✓ |         ✓ |               — |    ✓ |
| ORDER_CONFIRMED       |      ✓ |         ✓ |               — |    ✓ |
| ORDER_CANCELLED       |      ✓ |         ✓ |       Optionnel |    ✓ |
| PAYMENT_INITIATED     |      ✓ |         — |               — |    ✓ |
| PAYMENT_SUCCESS       |      ✓ |         ✓ |       Optionnel |    ✓ |
| PAYMENT_FAILED        |      ✓ |         ✓ |       Optionnel |    ✓ |
| PAYMENT_REFUNDED      |      ✓ |         ✓ |               — |    ✓ |
| WALLET_CREDITED       |      ✓ |         ✓ |       Optionnel |    ✓ |
| WALLET_DEBITED        |      ✓ |         ✓ |       Optionnel |    ✓ |
| WALLET_TRANSFER       |      ✓ |         ✓ |       Optionnel |    ✓ |
| LOYALTY_POINTS_EARNED |      ✓ | Optionnel |               — |    ✓ |
| REWARD_REDEEMED       |      ✓ | Optionnel |               — |    ✓ |
| MERCHANT_APPROVED     |      ✓ |         ✓ |               — |    ✓ |
| MERCHANT_SUSPENDED    |      ✓ |         ✓ |       Optionnel |    ✓ |
| PROMOTION             |      ✓ | Optionnel |               — |    ✓ |
| SYSTEM_ALERT          |      ✓ |         ✓ | Selon criticité |    ✓ |

`Optionnel` signifie que le canal peut être activé selon les règles métier, le niveau de risque ou les préférences.

---

# 11. Notification Model

Structure logique :

```text
Notification
├── id
├── eventType
├── type
├── priority
├── recipientId
├── title
├── body
├── data
├── channels
├── status
├── createdAt
├── scheduledAt
├── expiresAt
└── metadata
```

---

# 12. Notification Status

Statuts globaux :

```text
CREATED
QUEUED
PROCESSING
SENT
DELIVERED
READ
FAILED
CANCELLED
EXPIRED
```

Le statut global ne doit pas masquer le statut propre à chaque canal.

---

# 13. Channel Delivery Status

Chaque canal possède son propre état.

```text
ChannelDelivery
├── notificationId
├── channel
├── status
├── provider
├── providerMessageId
├── attempts
├── lastAttemptAt
├── deliveredAt
├── failedAt
└── errorCode
```

Exemple :

```text
Notification
    │
    ├── IN_APP → READ
    ├── EMAIL  → DELIVERED
    ├── SMS    → FAILED
    └── PUSH   → SENT
```

---

# 14. Recipient

Une notification doit identifier précisément son destinataire.

```text
recipientId
recipientType
```

Exemples :

```text
CUSTOMER
MERCHANT
MERCHANT_OPERATOR
SUPPORT
FINANCE
ADMIN
```

---

# 15. User Preferences

Chaque utilisateur peut gérer ses préférences lorsqu'elles sont applicables.

Exemple :

```text
Notification Preferences
├── Orders
├── Payments
├── Wallet
├── Loyalty
├── Promotions
├── Security
└── System
```

---

# 16. Préférences par canal

Exemple :

```text
ORDER
├── IN_APP = ON
├── EMAIL = ON
├── SMS = OFF
└── PUSH = ON
```

Mais certaines catégories ne doivent pas être désactivables.

---

# 17. Notifications obligatoires

Les notifications de sécurité et certaines notifications transactionnelles peuvent être obligatoires.

Exemples :

```text
Password changed
MFA changed
Security alert
Critical wallet event
Critical payment event
Account suspension
```

Une préférence marketing ne doit pas désactiver une notification de sécurité obligatoire.

---

# 18. Marketing Preferences

Les notifications promotionnelles doivent disposer d'une gestion séparée.

```text
MARKETING_EMAIL
MARKETING_SMS
MARKETING_PUSH
```

L'utilisateur doit pouvoir modifier ces préférences lorsque la réglementation et les règles métier le permettent.

---

# 19. Quiet Hours

Le système peut supporter une plage horaire silencieuse.

Exemple :

```text
22:00 → 07:00
```

Pendant cette période :

```text
Promotion → différée
Recommendation → différée
Security → immédiate
Critical payment → immédiate
OTP → immédiat
```

---

# 20. Notification Center

L'application doit fournir un centre de notifications.

Fonctions :

```text
View
Read
Unread
Mark as read
Mark all as read
Filter
Open linked resource
```

---

# 21. Notification Badge

Le header peut afficher :

```text
🔔 3
```

Le nombre correspond aux notifications non lues pertinentes.

---

# 22. Deep Link

Une notification peut pointer vers une ressource.

Exemple :

```text
notification
    ↓
ORDER_PAYMENT_SUCCESS
    ↓
/orders/123
```

Le backend ne doit pas générer aveuglément des URLs arbitraires.

Les destinations doivent être construites à partir de données sûres et validées côté frontend/backend.

---

# 23. Notification Payload

Exemple logique :

```json
{
  "notificationId": "uuid",
  "type": "PAYMENT_SUCCESS",
  "priority": "HIGH",
  "recipientId": "uuid",
  "title": "Paiement confirmé",
  "body": "Votre paiement a été confirmé.",
  "data": {
    "orderId": "uuid"
  }
}
```

Ne jamais inclure :

```text
password
secret
private key
access token
full payment credentials
sensitive internal information
```

---

# 24. Templates

Les messages doivent utiliser des templates versionnés.

Exemple :

```text
PAYMENT_SUCCESS
    ├── FR
    └── EN
```

---

# 25. Template Structure

Un template contient :

```text
Template
├── code
├── channel
├── language
├── subject
├── title
├── body
├── version
├── status
└── variables
```

---

# 26. Template Example

Code :

```text
PAYMENT_SUCCESS_EMAIL_FR
```

Variables :

```text
customerName
amount
currency
orderReference
date
```

Le moteur de template doit refuser les variables inconnues.

---

# 27. Template Versioning

Les templates doivent être versionnés.

Exemple :

```text
PAYMENT_SUCCESS_EMAIL_FR
v1
v2
v3
```

Une nouvelle version ne doit pas modifier rétroactivement les notifications déjà envoyées.

---

# 28. Localization

Langues initiales :

```text
FR
EN
```

L'architecture doit permettre d'en ajouter d'autres.

La langue préférée de l'utilisateur doit être utilisée lorsqu'elle est disponible.

---

# 29. Fallback Language

Si une traduction n'existe pas :

```text
User language
      ↓
Fallback language
      ↓
FR
```

Le système ne doit jamais envoyer un message vide.

---

# 30. Email

Structure recommandée :

```text
MAGIC GOLD

Titre

Message

Résumé de l'événement

[ Voir dans MAGIC GOLD ]

Footer
Support
Privacy
```

---

# 31. SMS

Le SMS doit être court.

Exemple conceptuel :

```text
MAGIC GOLD : Paiement de 25 000 XOF confirmé.
Commande MG-12345.
```

Ne jamais mettre de secret dans le SMS.

---

# 32. Push

Structure :

```text
Title
Body
Data
Deep link
```

Exemple :

```text
Paiement confirmé

Votre paiement de 25 000 XOF a été confirmé.

→ Ouvrir la commande
```

---

# 33. In-App

L'in-app peut être plus détaillé :

```text
Title
Description
Date
Status
Action
```

---

# 34. OTP

L'OTP possède un traitement spécifique.

Événements :

```text
LOGIN_OTP
PAYMENT_OTP
PASSWORD_RESET_OTP
PHONE_VERIFICATION_OTP
```

---

# 35. OTP Security

Les OTP doivent respecter :

```text
Expiration
Attempt limit
Rate limit
Single use
Secure generation
Audit
```

Le code ne doit jamais être stocké en clair dans les logs.

---

# 36. OTP Channel Priority

Exemple :

```text
SMS
Push when supported
Email when appropriate
```

Le canal dépend du parcours et du niveau de sécurité.

---

# 37. Notification Queue

Les notifications doivent être traitées de manière asynchrone lorsque possible.

```text
Application
    ↓
Event
    ↓
Queue
    ↓
Notification Worker
    ↓
Provider
```

Cela évite de bloquer une transaction métier à cause d'un fournisseur de notification indisponible.

---

# 38. Transaction vs Notification

Une notification ne doit pas déterminer seule le succès d'une transaction.

Exemple :

```text
Payment
   ↓
Payment SUCCESS
   ↓
Commit transaction
   ↓
Publish event
   ↓
Send notification
```

Si l'envoi échoue :

```text
Payment = SUCCESS
Notification = FAILED
```

La transaction financière reste indépendante.

---

# 39. Outbox Pattern

Pour les événements critiques, utiliser de préférence un mécanisme de type Outbox.

```text
Business Transaction
        │
        ├── Business Data
        │
        └── Outbox Event
                ↓
             Worker
                ↓
          Notification
```

Cela réduit le risque de perdre un événement entre la transaction et l'envoi.

---

# 40. Idempotency

Une notification critique doit être idempotente.

Clé possible :

```text
eventId
+
recipientId
+
notificationType
+
channel
```

Le système doit éviter l'envoi accidentel de doublons.

---

# 41. Duplicate Detection

Exemple :

```text
EVENT-123
CUSTOMER-456
PAYMENT_SUCCESS
EMAIL
```

ne doit pas être envoyé deux fois simplement parce qu'un worker a été relancé.

---

# 42. Retry

Les erreurs temporaires doivent être retentées.

Exemple :

```text
Attempt 1
   ↓
30 sec
   ↓
Attempt 2
   ↓
2 min
   ↓
Attempt 3
   ↓
10 min
   ↓
Attempt 4
```

Le nombre exact doit être configurable.

---

# 43. Exponential Backoff

Le système doit privilégier un backoff progressif.

Exemple :

```text
delay = baseDelay × 2^attempt
```

avec éventuellement un jitter.

---

# 44. Permanent Failure

Certaines erreurs ne doivent pas être retentées.

Exemples :

```text
Invalid email
Invalid phone
Invalid recipient
Template invalid
Provider rejects permanently
```

Le système doit enregistrer l'échec.

---

# 45. Dead Letter Queue

Les messages qui échouent définitivement peuvent être placés dans une DLQ.

```text
Notification Queue
       ↓
Worker
       ↓
Retry
       ↓
Failure
       ↓
DLQ
```

La DLQ doit être supervisée.

---

# 46. Provider Abstraction

Le code métier ne doit pas dépendre directement d'un fournisseur.

Architecture :

```text
NotificationService
       ↓
ChannelProvider
       ├── EmailProvider
       ├── SmsProvider
       └── PushProvider
```

Cela permet de remplacer un fournisseur sans réécrire le domaine métier.

---

# 47. Provider Status

Le système doit conserver :

```text
provider
providerMessageId
providerStatus
providerErrorCode
providerTimestamp
```

lorsque disponible.

---

# 48. Webhooks Provider

Les fournisseurs peuvent envoyer des callbacks.

Exemples :

```text
EMAIL_DELIVERED
EMAIL_BOUNCED

SMS_DELIVERED
SMS_FAILED

PUSH_DELIVERED
```

Les webhooks doivent être authentifiés et idempotents.

---

# 49. Webhook Security

Un webhook ne doit jamais être accepté uniquement parce qu'il arrive sur une URL connue.

Prévoir selon le fournisseur :

```text
Signature verification
Secret
Timestamp validation
Replay protection
IP restrictions when appropriate
Idempotency
```

---

# 50. Rate Limiting

Les notifications doivent être protégées contre les abus.

Exemples :

```text
OTP per phone
OTP per account
Password reset
Push notifications
SMS
Email
```

---

# 51. Anti-Spam

Le système doit limiter les notifications répétitives.

Exemple :

```text
5 notifications promotionnelles
en 1 minute
```

ne doit pas être possible simplement parce que plusieurs événements ont été générés.

---

# 52. Aggregation

Certains événements peuvent être regroupés.

Exemple :

Au lieu de :

```text
Vous avez gagné 10 points
Vous avez gagné 15 points
Vous avez gagné 20 points
```

envoyer :

```text
Vous avez gagné 45 points aujourd'hui.
```

L'agrégation est surtout adaptée aux notifications LOW/NORMAL.

---

# 53. Notification Deduplication

Le système peut regrouper des événements identiques sur une fenêtre configurable.

Exemple :

```text
ORDER_STATUS_UPDATE
Window: 30 sec
```

Cela évite les notifications répétitives causées par plusieurs changements techniques.

---

# 54. Expiration

Certaines notifications doivent expirer.

Exemple :

```text
Promotion
Coupon
Temporary offer
```

Une notification expirée ne doit plus être proposée comme action active.

---

# 55. Notification Read State

Le statut de lecture est propre à l'utilisateur.

```text
UNREAD
READ
```

Une notification système globale ne doit pas être marquée comme lue pour tous les utilisateurs parce qu'un seul utilisateur l'a ouverte.

---

# 56. Read Timestamp

Conserver :

```text
readAt
```

lorsque disponible.

---

# 57. Notification Audit

Les événements critiques doivent être auditables.

Exemple :

```text
Notification created
Notification queued
Notification sent
Notification delivered
Notification failed
Notification read
```

---

# 58. Audit Data

L'audit peut contenir :

```text
notificationId
eventId
recipientId
channel
status
provider
timestamp
errorCode
correlationId
```

Ne jamais enregistrer le contenu secret des OTP.

---

# 59. Correlation ID

Une notification doit pouvoir être reliée à son événement métier.

Exemple :

```text
Payment
  correlationId = CORR-123

Notification
  correlationId = CORR-123
```

Cela facilite le diagnostic.

---

# 60. Observability

Le système doit exposer des métriques.

Exemples :

```text
notifications_created_total
notifications_sent_total
notifications_failed_total
notifications_delivered_total

notification_latency
notification_retry_total

email_failure_rate
sms_failure_rate
push_failure_rate
```

---

# 61. Dashboard Notifications

L'administration peut disposer de :

```text
Total notifications
Success rate
Failure rate
Delivery rate
By channel
By type
By provider
By period
```

---

# 62. Alerting

Une alerte doit pouvoir être déclenchée si :

```text
Provider failure rate > threshold
Queue backlog too high
DLQ growing
SMS provider unavailable
Email provider unavailable
Push provider unavailable
```

Les seuils sont configurables.

---

# 63. Notification API

Exemples d'API conceptuelles :

```text
GET    /api/notifications
GET    /api/notifications/{id}
PATCH  /api/notifications/{id}/read
POST   /api/notifications/read-all

GET    /api/notification-preferences
PUT    /api/notification-preferences
```

Les endpoints internes de dispatch ne doivent pas être exposés inutilement aux clients.

---

# 64. Admin Notification API

Exemples :

```text
GET /api/admin/notifications
GET /api/admin/notifications/stats
GET /api/admin/notifications/failures
GET /api/admin/notifications/dlq
```

Les endpoints d'administration nécessitent des permissions spécifiques.

---

# 65. Notification Permissions

Exemples :

```text
NOTIFICATION_SELF_VIEW
NOTIFICATION_SELF_UPDATE

NOTIFICATION_ADMIN_VIEW
NOTIFICATION_ADMIN_SEND

NOTIFICATION_TEMPLATE_VIEW
NOTIFICATION_TEMPLATE_CREATE
NOTIFICATION_TEMPLATE_UPDATE

NOTIFICATION_PROVIDER_VIEW
NOTIFICATION_PROVIDER_MANAGE

NOTIFICATION_AUDIT_VIEW
```

---

# 66. Notification Sending Permission

Une fonctionnalité permettant à un administrateur d'envoyer manuellement une notification doit être fortement protégée.

Exemple :

```text
NOTIFICATION_ADMIN_SEND
```

L'action doit être auditée.

---

# 67. Manual Notification

Si activée :

```text
Recipient
Channel
Type
Priority
Subject
Message
Schedule
Expiration
```

doivent être définis.

Les messages marketing doivent respecter les préférences correspondantes.

---

# 68. Scheduled Notification

Le système peut supporter :

```text
scheduledAt
expiresAt
```

Exemple :

```text
Promotion
scheduledAt = 2026-10-01 08:00
expiresAt   = 2026-10-07 23:59
```

---

# 69. Cancellation

Une notification programmée peut être annulée avant traitement.

Statut :

```text
CANCELLED
```

Une notification déjà envoyée ne peut évidemment pas être "désenvoyée".

---

# 70. Notification Security

Les notifications ne doivent pas révéler :

```text
Passwords
OTP in logs
Access tokens
API keys
Private keys
Full card data
Internal secrets
```

---

# 71. Financial Privacy

Un message financier doit limiter les données exposées sur un écran verrouillé.

Exemple push :

```text
Paiement confirmé
Ouvrez MAGIC GOLD pour voir les détails.
```

peut être préférable à l'affichage d'informations financières détaillées selon les paramètres de confidentialité.

---

# 72. Sensitive Notification Settings

L'utilisateur peut éventuellement choisir :

```text
Show details on lock screen
Hide financial details
Hide preview
```

La sécurité doit rester prioritaire.

---

# 73. User Device Management

Pour les push notifications, conserver les appareils autorisés :

```text
Device
├── id
├── userId
├── platform
├── pushToken
├── appVersion
├── lastSeenAt
├── active
└── createdAt
```

---

# 74. Device Revocation

Un utilisateur peut retirer un appareil.

Lorsqu'un appareil est révoqué :

```text
active = false
```

Les notifications push ne doivent plus être envoyées à cet appareil.

---

# 75. Push Token Rotation

Le système doit gérer la rotation ou l'invalidation des tokens push.

Les tokens invalides doivent être désactivés.

---

# 76. Multi-Device

Un utilisateur peut avoir plusieurs appareils.

Exemple :

```text
User
 ├── iPhone
 ├── Android
 └── Web
```

Une notification push peut être envoyée aux appareils actifs selon la stratégie définie.

---

# 77. Preference Priority

Ordre de décision :

```text
Mandatory security rule
        ↓
Business rule
        ↓
User preference
        ↓
Channel availability
        ↓
Provider availability
```

Une préférence utilisateur ne doit pas contredire une obligation de sécurité.

---

# 78. Failure Strategy

Si un canal échoue :

```text
Primary channel
       ↓
Failure
       ↓
Fallback when permitted
       ↓
Retry
       ↓
Final failure
```

Exemple :

```text
Push failed
   ↓
Email fallback
```

Le fallback doit être défini par type de notification et ne doit pas être appliqué aveuglément.

---

# 79. Example — Payment Success

```text
PAYMENT_SUCCESS
       │
       ├── In-App
       ├── Push
       └── Email
```

SMS uniquement si les règles métier le prévoient.

---

# 80. Example — Security Alert

```text
SECURITY_ALERT
       │
       ├── In-App
       ├── Push
       ├── Email
       └── SMS selon criticité
```

Les préférences marketing ne doivent pas empêcher l'alerte.

---

# 81. Example — Promotion

```text
PROMOTION
       │
       ├── In-App
       ├── Push
       └── Email
```

uniquement selon les préférences marketing.

---

# 82. Example — OTP

```text
OTP_REQUESTED
       │
       └── Selected secure channel
```

Le système doit empêcher les demandes abusives.

---

# 83. Notification Lifecycle

```text
CREATED
   ↓
QUEUED
   ↓
PROCESSING
   ↓
SENT
   ↓
DELIVERED
   ↓
READ
```

Alternative :

```text
PROCESSING
   ↓
FAILED
   ↓
RETRY
   ↓
PROCESSING
```

ou :

```text
FAILED
   ↓
DLQ
```

---

# 84. Business Event Lifecycle

```text
Business operation
       ↓
Commit
       ↓
Event
       ↓
Notification service
       ↓
Preferences
       ↓
Template
       ↓
Queue
       ↓
Provider
       ↓
Delivery status
       ↓
Audit
```

---

# 85. Testing

Chaque notification critique doit être testée sur :

```text
Event
Recipient
Permission
Preference
Template
Channel
Provider
Success
Failure
Retry
Duplicate
Expiration
Read state
Audit
```

---

# 86. Tests de sécurité

Tester notamment :

```text
Unauthorized notification access
Cross-user notification access
Cross-merchant access
OTP exposure
Webhook forgery
Replay attack
Rate limit bypass
Template injection
Provider credential exposure
```

---

# 87. Tests d'idempotence

Simuler :

```text
Same event twice
Same webhook twice
Worker restart
Network timeout
Provider timeout
```

Le système ne doit pas générer de doubles opérations ou doubles notifications critiques.

---

# 88. Tests de préférences

Exemples :

```text
Marketing OFF
→ Promotion email = blocked

Security OFF
→ Security notification = still sent

SMS OFF
→ OTP = selon politique du parcours

Push OFF
→ In-App remains available
```

---

# 89. Tests Multi-Device

Vérifier :

```text
1 user
2 devices
3 devices
revoked device
expired token
invalid token
```

---

# 90. Tests de charge

Tester :

```text
High order volume
High payment volume
Mass notification campaign
Provider outage
Queue backlog
Recovery
```

---

# 91. Definition of Done

Le système de notifications est considéré comme terminé lorsque :

```text
[ ] Event model defined
[ ] Notification model implemented
[ ] Preferences implemented
[ ] Templates implemented
[ ] Localization implemented
[ ] In-App implemented
[ ] Email implemented
[ ] SMS implemented when required
[ ] Push implemented when required
[ ] Queue implemented
[ ] Retry implemented
[ ] Idempotency implemented
[ ] DLQ implemented where required
[ ] Audit implemented
[ ] Metrics implemented
[ ] Rate limiting implemented
[ ] Security tests passed
[ ] Integration tests passed
[ ] Mobile behavior validated
```

---

# 92. Règles Hermes

Avant d'implémenter une notification, Hermes doit déterminer :

```text
1. Quel événement la déclenche ?
2. Qui est le destinataire ?
3. Quel est le niveau de priorité ?
4. Quels canaux sont autorisés ?
5. La notification est-elle obligatoire ?
6. Quelles préférences utilisateur s'appliquent ?
7. Quel template utiliser ?
8. Quelles données peuvent être exposées ?
9. Faut-il un retry ?
10. Quel fallback est autorisé ?
11. Quel événement doit être audité ?
12. Comment éviter les doublons ?
```

---

# 93. Règle importante

Une notification est une **conséquence d'un événement métier**, et non l'événement métier lui-même.

Par exemple :

```text
PAYMENT_SUCCESS
```

doit être déterminé par le système de paiement.

Puis :

```text
PAYMENT_SUCCESS
      ↓
Notification
```

La notification ne doit jamais être utilisée pour décider qu'un paiement est réussi.

---

# 94. Architecture cible

```text
                    ┌─────────────────────┐
                    │   Business Services │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │    Domain Events    │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │ Notification Service│
                    └──────────┬──────────┘
                               │
             ┌─────────────────┼─────────────────┐
             ▼                 ▼                 ▼
      Preference Engine  Template Engine   Priority Engine
             │                 │                 │
             └─────────────────┼─────────────────┘
                               ▼
                         Message Queue
                               │
                 ┌─────────────┼─────────────┐
                 ▼             ▼             ▼
              Email           SMS           Push
                 │             │             │
                 └─────────────┼─────────────┘
                               ▼
                          Delivery Status
                               │
                               ▼
                             Audit
```

---

# 95. Source de vérité

Les notifications doivent rester cohérentes avec :

```text
04_DataModel.md
05_API_Specification.md
06_SecurityArchitecture.md
07_UI_UX_Specification.md
```

Les événements financiers et métier doivent provenir des services correspondants.

---

# 96. Règle finale

MAGIC GOLD doit pouvoir répondre à tout moment à quatre questions :

```text
Pourquoi cette notification a-t-elle été générée ?

À qui a-t-elle été envoyée ?

Par quel canal ?

Quel a été le résultat ?
```

Pour les événements critiques, le système doit également pouvoir répondre :

```text
Quel événement métier l'a déclenchée ?

Quand ?

Quelle version du template ?

Quel fournisseur ?

Combien de tentatives ?

Quel statut final ?

Quel correlationId ?
```

---

# 97. Conclusion

Le système de notifications MAGIC GOLD doit être :

```text
Fiable
Asynchrone
Idempotent
Sécurisé
Traçable
Configurable
Multi-canal
Multi-langue
Orienté événements
```

Le principe directeur est :

> **Une notification informe l'utilisateur d'un événement réel ; elle ne doit jamais être la source de vérité de cet événement.**


# MAGIC GOLD

## Règles métier par acteur

### 1. Objectif

Cette spécification définit les règles métier applicables à chaque acteur de MAGIC GOLD.

Elle complète :

* `04_DataModel.md`
* `05_API_Specification.md`
* `06_SecurityArchitecture.md`
* `07_UI_UX_Specification.md`
* `08_Notifications.md`

Une **permission technique** autorise l'accès à une action.

Une **règle métier** détermine si cette action est réellement autorisée dans le contexte donné.

Exemple :

```text
PAYMENT_REFUND
        +
paiement.status = SUCCESS
        +
utilisateur autorisé
        +
règles de délai respectées
        +
montant remboursable > 0
        +
règles financières respectées
        ↓
REMBOURSEMENT AUTORISÉ
```

Le backend est toujours la source d'autorité.

---

# 2. Acteurs MAGIC GOLD

Les acteurs principaux sont :

| Acteur            | Responsabilité principale                              |
| ----------------- | ------------------------------------------------------ |
| CUSTOMER          | Utilise les services, effectue des achats et paiements |
| MERCHANT          | Gère son activité commerciale                          |
| MERCHANT_OPERATOR | Exécute les opérations quotidiennes du marchand        |
| SUPPORT           | Assiste les utilisateurs et traite les incidents       |
| FINANCE           | Supervise les opérations financières                   |
| AUDITOR           | Contrôle et analyse les opérations et journaux         |
| ADMIN             | Administre la plateforme                               |
| SUPER_ADMIN       | Administration globale et opérations critiques         |

---

# 3. Règles communes à tous les acteurs

## 3.1 Compte actif

Un utilisateur ne peut effectuer aucune opération métier lorsque son compte est :

```text
SUSPENDED
DISABLED
DELETED
LOCKED
```

Exception :

```text
consultation limitée
procédure de récupération
opération administrative explicitement autorisée
```

---

## 3.2 Authentification

Toute opération sensible nécessite une authentification valide.

Pour les opérations à risque élevé, un mécanisme supplémentaire peut être exigé :

* OTP
* confirmation utilisateur
* authentification forte
* validation secondaire
* contrôle antifraude

---

## 3.3 Autorisation

Toute opération doit respecter :

```text
Authentication
        ↓
Account status
        ↓
Permission
        ↓
Tenant / Merchant scope
        ↓
Ownership
        ↓
Business rules
        ↓
Risk rules
        ↓
Action
        ↓
Audit
```

---

## 3.4 Isolation des données

Un acteur ne doit jamais pouvoir accéder aux données d'un autre périmètre auquel il n'est pas rattaché.

Exemple :

```text
MERCHANT A
    ↓
Orders A
Payments A
Customers A
Products A

≠

MERCHANT B
```

---

# 4. CUSTOMER

## 4.1 Gestion du compte

Le CUSTOMER peut :

* créer son compte ;
* compléter son profil ;
* modifier ses informations autorisées ;
* modifier son mot de passe ;
* gérer ses appareils ;
* gérer ses préférences de notification ;
* demander la fermeture de son compte.

Le CUSTOMER ne peut pas :

* modifier son rôle ;
* modifier son statut ;
* accéder aux données internes ;
* accéder aux données financières d'un marchand ;
* consulter les données d'un autre CUSTOMER.

---

## 4.2 Consultation des produits

Le CUSTOMER peut consulter uniquement les produits :

```text
product.status = ACTIVE
```

ou ceux explicitement rendus visibles.

Un produit :

```text
DRAFT
ARCHIVED
DISABLED
```

ne doit pas être achetable.

---

## 4.3 Commande

Le CUSTOMER peut créer une commande uniquement si :

* le produit est disponible ;
* le marchand est actif ;
* le prix est valide ;
* la quantité est autorisée ;
* les règles commerciales sont respectées.

Une commande ne peut être validée avec :

```text
quantity <= 0
price < 0
merchant = INACTIVE
product = INACTIVE
```

---

## 4.4 Paiement

Le CUSTOMER peut initier un paiement pour une commande autorisée.

Le montant payé doit correspondre au montant attendu, sauf règle métier explicitement définie.

Une commande ne doit jamais être considérée comme payée uniquement parce que le CUSTOMER a reçu une confirmation côté frontend.

La confirmation doit provenir du backend et/ou du prestataire de paiement vérifié.

---

## 4.5 Annulation

Le CUSTOMER peut demander l'annulation d'une commande uniquement lorsque son état permet l'annulation.

Exemple :

```text
PENDING
CONFIRMED
```

selon les règles commerciales.

Une commande :

```text
COMPLETED
CANCELLED
REFUNDED
```

ne peut pas être annulée une seconde fois.

---

## 4.6 Remboursement

Le CUSTOMER peut demander un remboursement lorsqu'une règle métier l'autorise.

La demande doit vérifier :

* paiement existant ;
* paiement confirmé ;
* montant remboursable ;
* commande éligible ;
* absence de remboursement déjà effectué ;
* éventuel délai de remboursement.

Le CUSTOMER ne peut jamais directement modifier :

```text
payment.status
refund.status
settlement.status
wallet.balance
ledger entries
```

---

# 5. MERCHANT

## 5.1 Compte marchand

Le MERCHANT peut gérer son activité dans son propre périmètre.

Il peut :

* gérer son profil commercial ;
* gérer ses produits ;
* gérer ses prix ;
* gérer ses stocks si le module existe ;
* consulter ses commandes ;
* consulter ses paiements ;
* consulter ses revenus ;
* gérer ses opérateurs ;
* consulter ses rapports.

---

## 5.2 Produits

Le MERCHANT peut créer un produit.

Un produit doit obligatoirement respecter les règles minimales :

```text
name != null
price >= 0
merchant actif
```

Un produit peut évoluer :

```text
DRAFT
ACTIVE
SUSPENDED
ARCHIVED
```

Un produit ARCHIVED ne doit plus pouvoir recevoir de nouvelles commandes.

---

## 5.3 Prix

Le MERCHANT peut modifier le prix d'un produit selon les règles commerciales.

Une modification de prix ne doit pas modifier rétroactivement le montant d'une commande déjà validée.

Exemple :

```text
Produit : 10 000 XOF
Commande : 10 000 XOF

Modification produit :
15 000 XOF

Commande existante :
10 000 XOF
```

Le prix utilisé dans la commande doit être conservé comme **snapshot transactionnel**.

---

## 5.4 Commandes

Le MERCHANT peut consulter les commandes de son périmètre.

Il peut modifier l'état d'une commande uniquement lorsque la transition est autorisée.

Exemple :

```text
PENDING
   ↓
CONFIRMED
   ↓
PROCESSING
   ↓
COMPLETED
```

Une transition interdite doit être rejetée par le backend.

---

## 5.5 Paiements

Le MERCHANT peut consulter les paiements associés à son activité.

Il ne peut pas :

* créer artificiellement un paiement SUCCESS ;
* modifier un paiement confirmé ;
* modifier le solde financier directement ;
* supprimer une transaction ;
* modifier une écriture comptable.

---

## 5.6 Remboursement

Le MERCHANT peut demander ou initier un remboursement lorsque la politique commerciale l'autorise.

Un remboursement financier doit toujours être traité comme une opération indépendante et auditée.

```text
PAYMENT
   ↓
REFUND_REQUEST
   ↓
REFUND_PROCESSING
   ↓
REFUNDED
```

---

# 6. MERCHANT_OPERATOR

Le MERCHANT_OPERATOR agit au nom du MERCHANT.

## 6.1 Périmètre

L'opérateur est limité au :

```text
merchant_id
```

auquel il est rattaché.

Il ne peut jamais accéder à un autre marchand.

---

## 6.2 Actions possibles

Selon ses permissions, il peut :

* consulter les produits ;
* gérer les produits ;
* consulter les commandes ;
* traiter les commandes ;
* consulter certaines informations de paiement ;
* assister les clients.

---

## 6.3 Restrictions financières

Par défaut, l'opérateur ne peut pas :

* modifier le solde marchand ;
* effectuer un remboursement financier sensible ;
* modifier les commissions ;
* modifier les paramètres de règlement ;
* modifier les coordonnées bancaires ;
* supprimer des transactions.

Ces opérations nécessitent une permission financière explicite.

---

# 7. SUPPORT

Le SUPPORT traite les demandes d'assistance.

## 7.1 Consultation

Le SUPPORT peut consulter les informations nécessaires à la résolution d'un incident.

L'accès doit respecter le principe :

```text
Need to know
```

Il ne doit pas accéder inutilement aux données sensibles.

---

## 7.2 Assistance

Le SUPPORT peut :

* rechercher un utilisateur ;
* rechercher une commande ;
* consulter le statut d'un paiement ;
* consulter l'historique d'un incident ;
* créer un ticket ;
* ajouter une note ;
* communiquer avec l'utilisateur.

---

## 7.3 Actions sensibles

Le SUPPORT ne doit pas pouvoir directement :

```text
modifier wallet balance
modifier ledger
modifier payment SUCCESS
supprimer transaction
modifier rôle ADMIN
```

Toute action exceptionnelle doit être explicitement autorisée et auditée.

---

# 8. FINANCE

Le rôle FINANCE supervise les opérations financières.

## 8.1 Consultation

FINANCE peut consulter :

* paiements ;
* remboursements ;
* commissions ;
* settlements ;
* transactions ;
* rapprochements ;
* rapports financiers.

---

## 8.2 Réconciliation

FINANCE peut effectuer des opérations de rapprochement entre :

```text
MAGIC GOLD
      ↕
PAYMENT PROVIDER
      ↕
BANK / SETTLEMENT
```

Une différence doit produire une anomalie.

Exemple :

```text
Expected = 100 000 XOF
Provider = 100 000 XOF
MAGIC GOLD = 100 000 XOF

→ MATCH
```

contre :

```text
Expected = 100 000 XOF
Provider = 95 000 XOF
MAGIC GOLD = 100 000 XOF

→ DISCREPANCY
```

---

## 8.3 Correction financière

FINANCE ne doit pas modifier directement une transaction historique.

Une correction doit créer une opération corrective :

```text
TRANSACTION_ORIGIN
        +
ADJUSTMENT
        +
AUDIT
```

L'historique original doit rester conservé.

---

# 9. AUDITOR

L'AUDITOR possède principalement un rôle de contrôle.

## 9.1 Accès

L'AUDITOR peut consulter :

* utilisateurs ;
* commandes ;
* paiements ;
* remboursements ;
* changements de statut ;
* journaux d'audit ;
* opérations administratives ;
* anomalies.

---

## 9.2 Principe READ ONLY

Par défaut :

```text
AUDITOR = READ ONLY
```

Il ne doit pas pouvoir modifier les données métier.

---

## 9.3 Audit

Toute opération critique doit permettre de retrouver :

```text
who
what
when
where
resource
before
after
reason
correlation_id
```

---

# 10. ADMIN

ADMIN assure l'administration opérationnelle de MAGIC GOLD.

## 10.1 Utilisateurs

ADMIN peut, selon ses permissions :

* créer des utilisateurs ;
* suspendre un compte ;
* réactiver un compte ;
* affecter un rôle ;
* gérer certaines permissions ;
* gérer les marchands.

Les opérations sensibles doivent être journalisées.

---

## 10.2 Configuration

ADMIN peut gérer certaines configurations :

* paramètres commerciaux ;
* catalogues ;
* paramètres de notification ;
* paramètres opérationnels ;
* règles non financières.

---

## 10.3 Limitation

ADMIN ne doit pas automatiquement avoir accès aux opérations financières critiques.

Exemple :

```text
ADMIN
≠
FINANCE
```

Une séparation des responsabilités doit être maintenue.

---

# 11. SUPER_ADMIN

SUPER_ADMIN possède le niveau d'administration le plus élevé.

Il peut notamment :

* gérer les administrateurs ;
* gérer la configuration globale ;
* gérer les rôles système ;
* gérer les permissions critiques ;
* intervenir sur les configurations de sécurité ;
* gérer les intégrations globales.

Les opérations critiques doivent obligatoirement être auditées.

---

# 12. Séparation des responsabilités

MAGIC GOLD doit éviter qu'un même acteur puisse réaliser seul une chaîne financière critique.

Exemple recommandé :

```text
MERCHANT
    ↓
demande remboursement

FINANCE
    ↓
contrôle

PAYMENT PROVIDER
    ↓
exécution

SYSTEM
    ↓
confirmation

AUDITOR
    ↓
contrôle a posteriori
```

---

# 13. Matrice métier par acteur

| Fonction                      | CUSTOMER |   MERCHANT |         OPERATOR | SUPPORT | FINANCE | AUDITOR |            ADMIN | SUPER_ADMIN |
| ----------------------------- | -------: | ---------: | ---------------: | ------: | ------: | ------: | ---------------: | ----------: |
| Gérer son profil              |        ✓ |          ✓ |                ✓ |       ✓ |       ✓ |       ✓ |                ✓ |           ✓ |
| Acheter                       |        ✓ |          — |                — |       — |       — |       — |                — |           — |
| Créer produit                 |        — |          ✓ | Selon permission |       — |       — |       — |                ✓ |           ✓ |
| Gérer commande                |        ✓ |          ✓ |                ✓ | Lecture |       — | Lecture |                ✓ |           ✓ |
| Consulter paiement            |     Self |          ✓ |           Limité |       ✓ |       ✓ |       ✓ | Selon permission |           ✓ |
| Initier remboursement         |  Demande |          ✓ |           Limité |       — |       ✓ |       — | Selon permission |           ✓ |
| Exécuter opération financière |        — |     Limité |                — |       — |       ✓ |       — |                — |           ✓ |
| Réconciliation                |        — |          — |                — |       — |       ✓ |       ✓ |                — |           ✓ |
| Consulter audit               |     Self |     Limité |           Limité |  Limité |       ✓ |       ✓ |                ✓ |           ✓ |
| Gérer utilisateurs            |     Self | Opérateurs |                — |       — |       — |       — |                ✓ |           ✓ |
| Gérer rôles                   |        — |          — |                — |       — |       — |       — | Selon permission |           ✓ |
| Configuration globale         |        — |          — |                — |       — |       — |       — |                ✓ |           ✓ |

`✓` signifie que l'acteur peut potentiellement réaliser l'action, sous réserve des permissions et règles métier.

`Limité` signifie que l'action est soumise à un périmètre ou à une permission spécifique.

---

# 14. Règles de transition des statuts

Aucun acteur ne doit pouvoir modifier directement un statut vers n'importe quelle valeur.

Exemple :

```text
PENDING
   ↓
CONFIRMED
   ↓
PROCESSING
   ↓
COMPLETED
```

Une transition :

```text
COMPLETED → PENDING
```

doit être interdite sauf procédure exceptionnelle explicitement définie.

Même principe pour :

* commandes ;
* paiements ;
* remboursements ;
* settlements ;
* comptes utilisateurs ;
* produits.

---

# 15. Règle d'idempotence

Toute opération métier sensible doit être idempotente lorsqu'elle peut être répétée.

Exemple :

```text
POST /payments/{id}/refund
Idempotency-Key: abc-123
```

Deux appels identiques ne doivent pas produire deux remboursements.

---

# 16. Règle d'audit

Les opérations suivantes doivent obligatoirement être auditées :

* création de compte ;
* changement de rôle ;
* suspension ;
* activation ;
* création/modification produit ;
* modification prix ;
* commande ;
* paiement ;
* remboursement ;
* modification financière ;
* changement de configuration ;
* changement de permission ;
* intervention administrative.

---

# 17. Règle de non-contournement

Le frontend ne doit jamais être considéré comme une couche de sécurité.

Interdire un bouton dans Vue.js ne suffit pas.

Exemple incorrect :

```text
if (user.role === "FINANCE") {
   afficher bouton remboursement
}
```

La règle doit également être vérifiée côté backend :

```text
hasPermission(PAYMENT_REFUND)
AND
merchantScopeValid()
AND
paymentRefundable()
AND
businessRulesValid()
```

---

# 18. Règle de propriété

Pour les ressources appartenant à un utilisateur :

```text
CUSTOMER
    ↓
resource.customer_id == authenticated_user.id
```

Pour les ressources appartenant à un marchand :

```text
MERCHANT
    ↓
resource.merchant_id == authenticated_merchant.id
```

Une permission seule ne suffit donc pas.

---

# 19. Règle de concurrence

Deux opérations simultanées ne doivent pas provoquer un état financier incohérent.

Exemple :

```text
Wallet balance = 100 000 XOF

Transaction A = -80 000
Transaction B = -80 000
```

Le système doit empêcher que les deux opérations soient validées si le solde disponible ne le permet pas.

Les mécanismes possibles sont :

* transaction SQL ;
* verrouillage optimiste ;
* verrouillage pessimiste ;
* contrôle atomique ;
* idempotency key.

---

# 20. Règle de source de vérité

Pour chaque domaine :

| Domaine      | Source de vérité        |
| ------------ | ----------------------- |
| Utilisateur  | User service / database |
| Produit      | Product domain          |
| Commande     | Order domain            |
| Paiement     | Payment domain          |
| Solde        | Wallet/Ledger           |
| Notification | Notification domain     |
| Audit        | Audit log               |

Une notification, un écran frontend ou un cache ne doit jamais devenir la source de vérité d'une opération métier.

---

# 21. Règle fondamentale pour Hermes

Lors de l'implémentation, Hermes doit toujours distinguer :

```text
ROLE
≠
PERMISSION
≠
OWNERSHIP
≠
BUSINESS RULE
≠
SECURITY RULE
```

Toute nouvelle fonctionnalité doit documenter :

1. les acteurs concernés ;
2. les permissions nécessaires ;
3. les ressources accessibles ;
4. les règles de propriété ;
5. les règles métier ;
6. les transitions de statut ;
7. les contrôles financiers éventuels ;
8. les notifications ;
9. les événements d'audit ;
10. les cas d'erreur.

Avant de coder une fonctionnalité, Hermes doit vérifier les documents existants et ne doit pas inventer une règle métier contradictoire.

---

# 22. Definition of Done — Règles métier

Une fonctionnalité impliquant un acteur est considérée terminée uniquement si :

* [ ] l'acteur est identifié ;
* [ ] les permissions sont définies ;
* [ ] le périmètre d'accès est défini ;
* [ ] l'ownership est contrôlé ;
* [ ] les règles métier sont implémentées côté backend ;
* [ ] les transitions de statut sont contrôlées ;
* [ ] les opérations critiques sont idempotentes ;
* [ ] les opérations sensibles sont auditées ;
* [ ] les erreurs métier sont explicites ;
* [ ] les notifications nécessaires sont intégrées ;
* [ ] les tests positifs sont présents ;
* [ ] les tests de refus sont présents ;
* [ ] les tests de sécurité sont présents ;
* [ ] aucun contrôle critique ne repose uniquement sur le frontend.

---

## Règle finale

Dans MAGIC GOLD :

> **L'acteur déclenche une intention, la permission autorise une capacité, mais seule la règle métier permet réellement l'exécution.**

Le backend reste l'autorité finale pour toutes les opérations métier et financières.
