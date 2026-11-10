# MAGIC GOLD — Integration Specification

**Version : 1.1**
**Statut : Référence d’implémentation V1**
**Projet : MAGIC GOLD**
**Architecture : Commerce de détail + orchestration des paiements**

---

# 1. Objectif

Ce document définit l’architecture d’intégration de MAGIC GOLD avec :

* les prestataires de paiement ;
* les services SMS ;
* les services Email ;
* les notifications Push ;
* le fournisseur d’identité ;
* les services de stockage ;
* les Webhooks ;
* les systèmes externes futurs ;
* le système interne d’événements ;
* les mécanismes d’asynchronisme et de reprise.

L’objectif est de construire une architecture :

* découplée ;
* sécurisée ;
* observable ;
* idempotente ;
* résiliente ;
* testable ;
* extensible ;
* compatible avec une évolution future vers Kafka.

---

# 2. Principe architectural fondamental

MAGIC GOLD V1 ne doit pas être fortement couplé à un fournisseur externe.

Le domaine métier ne doit jamais dépendre directement d’un SDK spécifique.

Architecture :

```text
                    MAGIC GOLD
                         │
          ┌──────────────┼──────────────┐
          │              │              │
     Business       Integration     Event Layer
      Domain           Layer
          │              │              │
          │       ┌──────┼──────┐       │
          │       │      │      │       │
          ▼       ▼      ▼      ▼       ▼
       PostgreSQL  Payment SMS Email   Outbox
                         Push Identity   │
                                         ▼
                                  Event Dispatcher
                                         │
                              ┌──────────┼──────────┐
                              ▼          ▼          ▼
                        Notification Reporting Integration
```

Le domaine métier ne connaît pas :

* CinetPay ;
* FeexPay ;
* Orange Money ;
* MTN MoMo ;
* Wave ;
* Twilio ;
* SendGrid ;
* Firebase ;
* Kafka ;
* Redis comme source métier.

Il connaît uniquement des interfaces métier/techniques abstraites.

---

# 3. Périmètre des intégrations V1

## 3.1 Paiement

MAGIC GOLD doit pouvoir intégrer plusieurs Payment Providers.

Exemples :

* Mobile Money ;
* carte bancaire ;
* QR Payment ;
* virement bancaire ;
* autres moyens de paiement supportés.

Le choix du fournisseur ne doit pas modifier le domaine `Payment`.

---

# 4. Payment Provider Abstraction

Interface conceptuelle :

```java
interface PaymentProvider {

    PaymentInitiationResult initiate(PaymentRequest request);

    PaymentVerificationResult verify(String providerReference);

    RefundResult refund(RefundRequest request);

    PaymentStatus getStatus(String providerReference);

    boolean validateWebhook(WebhookRequest request);

    PaymentWebhookResult parseWebhook(WebhookRequest request);
}
```

Chaque fournisseur possède son propre adapter :

```text
PaymentProvider
      │
      ├── ProviderAAdapter
      ├── ProviderBAdapter
      ├── ProviderCAdapter
      └── FutureProviderAdapter
```

Le domaine métier ne doit jamais appeler directement un SDK fournisseur.

---

# 5. Flux de paiement

```text
Client
  │
  ▼
MAGIC GOLD
  │
  ▼
Payment Service
  │
  ▼
PaymentProvider
  │
  ▼
Prestataire externe
  │
  ├── réponse synchrone
  │
  └── Webhook asynchrone
         │
         ▼
   Webhook Endpoint
         │
         ▼
Signature Validation
         │
         ▼
Idempotency Check
         │
         ▼
Payment Service
         │
         ▼
PostgreSQL
         │
         ▼
Transactional Outbox
```

La confirmation finale d’un paiement ne doit jamais être déterminée uniquement par le frontend.

---

# 6. Webhooks

Tous les Webhooks externes doivent être traités comme des entrées non fiables jusqu’à validation.

Pipeline obligatoire :

```text
Webhook
   ↓
HTTPS
   ↓
Authentification / Signature
   ↓
Validation du format
   ↓
Identification du provider
   ↓
Idempotency
   ↓
Validation métier
   ↓
Mise à jour transactionnelle
   ↓
Outbox Event
   ↓
Réponse HTTP
```

Le système doit gérer :

* doublons ;
* Webhooks reçus plusieurs fois ;
* Webhooks hors ordre ;
* Webhooks retardés ;
* signatures invalides ;
* payloads invalides ;
* provider indisponible ;
* événements inconnus.

---

# 7. Idempotence

Les opérations suivantes doivent supporter l’idempotence :

* création de vente ;
* création de commande ;
* initiation paiement ;
* confirmation paiement ;
* remboursement ;
* traitement Webhook ;
* création d’événement ;
* notification.

Une clé d’idempotence doit être conservée suffisamment longtemps pour empêcher une duplication métier.

Exemple :

```text
Idempotency-Key:
MG-PAY-8f8d9d...
```

Une même requête répétée ne doit pas créer :

* deux paiements ;
* deux remboursements ;
* deux ventes ;
* deux commandes ;
* deux notifications critiques.

---

# 8. Transactional Outbox

## 8.1 Objectif

MAGIC GOLD V1 utilise le pattern **Transactional Outbox** pour éviter les pertes d’événements.

Lorsqu’une opération métier modifie PostgreSQL, l’événement correspondant doit être enregistré dans la même transaction.

Exemple :

```text
BEGIN TRANSACTION

INSERT sale
INSERT payment
INSERT outbox_event

COMMIT
```

Si la transaction échoue :

```text
ROLLBACK

sale       → rollback
payment    → rollback
outbox     → rollback
```

Il ne doit jamais être possible d'avoir :

```text
Paiement enregistré
+
Événement perdu
```

---

# 9. Modèle Outbox

Table conceptuelle :

```text
outbox_event
```

Champs minimaux :

```text
id
eventId
aggregateType
aggregateId
eventType
eventVersion
payload
occurredAt
status
attempts
nextAttemptAt
processedAt
lastError
correlationId
createdAt
updatedAt
```

Statuts :

```text
PENDING
PROCESSING
PROCESSED
FAILED
DEAD_LETTER
```

`eventId` doit être unique.

---

# 10. Événements métier

Un événement représente un fait métier qui s’est produit.

Exemples :

```text
SALE_CREATED
SALE_COMPLETED

ORDER_CREATED
ORDER_CONFIRMED
ORDER_COMPLETED
ORDER_CANCELLED

PAYMENT_INITIATED
PAYMENT_SUCCEEDED
PAYMENT_FAILED
PAYMENT_CANCELLED

REFUND_REQUESTED
REFUND_COMPLETED
REFUND_FAILED

CUSTOMER_CREATED

MERCHANT_ACTIVATED

STOCK_CHANGED

NOTIFICATION_REQUESTED
```

Tous les changements techniques de base de données ne doivent pas devenir automatiquement des événements métier.

---

# 11. Event Dispatcher

V1 utilise un composant `EventDispatcher`.

Responsabilités :

1. récupérer les événements `PENDING` ;
2. verrouiller les événements ;
3. les publier vers les consommateurs ;
4. gérer les erreurs ;
5. effectuer les retries ;
6. marquer les événements traités ;
7. déplacer les événements définitivement échoués vers `DEAD_LETTER`.

Architecture :

```text
PostgreSQL
    │
    ▼
outbox_event
    │
    ▼
Outbox Worker
    │
    ▼
EventDispatcher
    │
    ├── Notification
    ├── Reporting
    ├── Integration
    └── autres consommateurs
```

---

# 12. Event Bus Abstraction

MAGIC GOLD doit introduire une abstraction d’Event Bus.

Exemple conceptuel :

```java
interface EventBus {

    void publish(DomainEvent event);

    void publishBatch(List<DomainEvent> events);
}
```

Le domaine métier ne doit pas dépendre d’un transport particulier.

---

# 13. Transport V1

V1 ne nécessite pas Kafka.

Le transport initial peut être :

```text
PostgreSQL Outbox
       ↓
Worker
       ↓
EventDispatcher
       ↓
Consumers
```

Cette architecture permet de rester simple tout en préparant l'évolution.

---

# 14. Kafka — évolution future

Kafka pourra être ajouté ultérieurement derrière la même abstraction.

Architecture future :

```text
PostgreSQL
    │
    ▼
Transactional Outbox
    │
    ▼
Outbox Dispatcher
    │
    ▼
Kafka
    │
    ├── Notification
    ├── Reporting
    ├── Analytics
    ├── Data Warehouse
    ├── Fraud Detection
    ├── External Integrations
    └── Data Lake
```

Le domaine métier ne doit pas être réécrit lors de cette évolution.

Exemple :

```text
EventBus
   │
   ├── InternalEventBus       V1
   └── KafkaEventBus          V2/V3
```

Kafka ne doit jamais remplacer PostgreSQL comme source de vérité transactionnelle.

---

# 15. Livraison des événements

La garantie V1 est :

```text
AT-LEAST-ONCE DELIVERY
```

Les consommateurs doivent donc être idempotents.

MAGIC GOLD ne doit pas promettre :

```text
EXACTLY-ONCE
```

au niveau global du système.

---

# 16. Ordonnancement des événements

MAGIC GOLD ne doit pas imposer un ordre global des événements.

Lorsque l’ordre est nécessaire, il doit être garanti au niveau d’un agrégat.

Exemple :

```text
aggregateType = PAYMENT
aggregateId   = PAY-001
```

Les événements d’un même paiement peuvent nécessiter un ordre logique :

```text
PAYMENT_INITIATED
        ↓
PAYMENT_SUCCEEDED
        ↓
REFUND_REQUESTED
        ↓
REFUND_COMPLETED
```

---

# 17. Versionnement des événements

Chaque événement doit posséder :

```text
eventType
eventVersion
```

Exemple :

```text
PAYMENT_SUCCEEDED
version = 1
```

Une évolution incompatible doit créer une nouvelle version ou un mécanisme explicite de compatibilité.

Un consommateur ne doit pas supposer qu'un payload restera éternellement identique.

---

# 18. Sécurité des événements

Les événements ne doivent pas contenir inutilement :

* mots de passe ;
* tokens ;
* secrets ;
* données bancaires sensibles ;
* OTP ;
* informations d’authentification ;
* données personnelles non nécessaires.

Les payloads doivent appliquer le principe de minimisation des données.

Exemple préférable :

```json
{
  "eventType": "PAYMENT_SUCCEEDED",
  "paymentId": "PAY-123",
  "orderId": "ORD-456",
  "amount": 15000,
  "currency": "XOF",
  "provider": "PROVIDER_A"
}
```

plutôt que d’envoyer toutes les données du client.

---

# 19. Retry et Backoff

En cas d'échec :

```text
1er échec
   ↓
retry
   ↓
2e échec
   ↓
retry
   ↓
3e échec
   ↓
retry
   ↓
échec définitif
   ↓
DEAD_LETTER
```

Le délai doit être configurable.

Exemple :

```text
10 s
30 s
1 min
5 min
15 min
```

Le système doit éviter les boucles infinies.

---

# 20. Dead Letter

Un événement ne pouvant pas être traité après le nombre maximum de tentatives doit être conservé.

Informations minimales :

```text
eventId
eventType
aggregateId
attempts
lastError
firstFailedAt
lastAttemptAt
payload
```

Une intervention administrative peut permettre :

```text
RETRY
IGNORE
ARCHIVE
```

Ces actions doivent être auditées.

---

# 21. Notifications

Les notifications utilisent une abstraction :

```java
interface NotificationProvider {

    SendResult send(NotificationRequest request);
}
```

Canaux :

```text
IN_APP
SMS
EMAIL
PUSH
```

Providers possibles :

```text
SmsProvider
EmailProvider
PushProvider
```

La logique métier ne doit pas connaître le fournisseur.

---

# 22. SMS

Interface :

```java
interface SmsProvider {

    SendResult sendSms(
        String destination,
        String message
    );
}
```

Le système doit gérer :

* timeout ;
* erreur provider ;
* retry ;
* statut ;
* coût éventuel ;
* limitation de débit ;
* traçabilité.

---

# 23. Email

Interface :

```java
interface EmailProvider {

    SendResult sendEmail(
        EmailRequest request
    );
}
```

Les templates doivent être séparés du code métier.

---

# 24. Push

L’intégration Push doit être abstraite :

```java
interface PushProvider {

    SendResult sendPush(PushRequest request);
}
```

Les tokens de device ne doivent jamais être exposés dans les logs.

---

# 25. Identity Provider

MAGIC GOLD peut utiliser un fournisseur d’identité externe ou dédié.

L’application doit isoler cette dépendance derrière une couche d’identité.

Exemple :

```text
IdentityProvider
       │
       └── Keycloak
```

Le domaine métier ne doit pas dépendre directement des classes internes du fournisseur.

---

# 26. Stockage

Les fichiers éventuels doivent être manipulés via une abstraction :

```java
interface StorageProvider {

    StorageResult upload(FileRequest request);

    void delete(String objectKey);

    Resource download(String objectKey);
}
```

Exemples futurs :

* S3 ;
* MinIO ;
* stockage VPS ;
* stockage cloud.

Les données métier doivent conserver uniquement les références nécessaires.

---

# 27. Secrets

Les secrets de fournisseurs ne doivent jamais être :

* dans Git ;
* dans le frontend ;
* dans les fichiers versionnés ;
* dans les logs.

Utiliser :

```text
Environment Variables
Secret Manager
Docker Secrets
Vault
```

selon l’environnement.

---

# 28. Configuration des Providers

Chaque provider doit disposer d’une configuration isolée :

```text
provider
enabled
environment
baseUrl
credentials
timeout
retryPolicy
webhookConfig
```

Les secrets sont externalisés.

---

# 29. Normalisation des erreurs

Les erreurs externes doivent être transformées en erreurs internes normalisées.

Exemple :

```text
PROVIDER_TIMEOUT
PROVIDER_UNAVAILABLE
PROVIDER_REJECTED
INVALID_PROVIDER_RESPONSE
INVALID_WEBHOOK
SIGNATURE_INVALID
RATE_LIMITED
UNKNOWN_PROVIDER_ERROR
```

Le frontend ne doit pas recevoir directement les messages techniques du provider.

---

# 30. Timeout

Toutes les intégrations réseau doivent avoir un timeout explicite.

Aucune requête externe ne doit pouvoir bloquer indéfiniment un thread applicatif.

---

# 31. Circuit Breaker

Pour les providers critiques, un mécanisme de Circuit Breaker peut être utilisé :

```text
CLOSED
   ↓
erreurs répétées
   ↓
OPEN
   ↓
temps d'attente
   ↓
HALF_OPEN
   ↓
succès → CLOSED
```

Le système doit éviter les cascades d’échecs.

---

# 32. Fallback Provider

Le fallback doit être possible lorsque le métier l’autorise.

Exemple :

```text
Provider A indisponible
        ↓
Provider B
```

Mais un paiement déjà initié auprès d’un provider ne doit pas être arbitrairement déplacé vers un autre provider sans règle métier explicite.

---

# 33. Corrélation

Chaque opération distribuée doit utiliser un :

```text
correlationId
```

Exemple :

```text
HTTP Request
   ↓
Order
   ↓
Payment
   ↓
Webhook
   ↓
Outbox
   ↓
Notification
```

Le même contexte de corrélation doit être conservé lorsque cela est possible.

---

# 34. Observabilité

Les intégrations doivent produire :

* logs structurés ;
* métriques ;
* temps de réponse ;
* taux d’erreur ;
* nombre de retries ;
* nombre de Webhooks ;
* nombre d’échecs ;
* taille de l’outbox ;
* âge du plus ancien événement non traité ;
* état des providers.

---

# 35. Audit

Les actions sensibles doivent être auditées :

* modification d’un provider ;
* activation/désactivation ;
* changement de configuration ;
* remboursement ;
* retry manuel ;
* traitement d’un événement `DEAD_LETTER` ;
* modification des paramètres d’intégration.

Ne jamais enregistrer de secret dans l’audit.

---

# 36. Environnements

Chaque intégration doit supporter :

```text
LOCAL
TEST
STAGING
PRODUCTION
```

Les credentials et URLs doivent être différents selon l’environnement.

Les providers disposant d’un Sandbox doivent être utilisés en développement/test.

---

# 37. Contract Testing

Chaque adapter externe doit disposer de tests de contrat.

Exemples :

```text
PaymentProvider contract
SmsProvider contract
EmailProvider contract
StorageProvider contract
```

Les mocks ne doivent pas être la seule forme de test.

---

# 38. Test des événements

Les tests doivent vérifier :

* création atomique de l’outbox ;
* absence d’événement après rollback ;
* publication ;
* retry ;
* idempotence ;
* doublons ;
* ordre par agrégat ;
* événement invalide ;
* Dead Letter ;
* reprise après crash ;
* absence de perte d’événement.

---

# 39. Migration future vers Kafka

Le passage à Kafka doit respecter :

```text
Business Domain
      ↓
EventBus
      ↓
Transport
```

V1 :

```text
EventBus → Internal Dispatcher
```

V2/V3 :

```text
EventBus → Kafka
```

Le domaine métier ne doit pas connaître Kafka.

---

# 40. Critères de déclenchement de Kafka

Kafka pourra être envisagé lorsque MAGIC GOLD aura notamment :

* plusieurs consommateurs indépendants d’événements ;
* besoins importants d’analytics temps réel ;
* data lake ;
* data warehouse alimenté en continu ;
* intégrations nombreuses ;
* volumes d’événements importants ;
* besoin de replay d’événements ;
* architecture distribuée à grande échelle.

Il ne doit pas être ajouté uniquement pour « faire moderne ».

---

# 41. Règles Hermes AI

Hermes doit respecter strictement :

1. Ne jamais coupler le domaine métier à un provider.
2. Ne jamais mettre de credentials dans le code.
3. Toujours valider les Webhooks.
4. Toujours gérer l’idempotence.
5. Ne jamais confirmer un paiement depuis le frontend.
6. Utiliser PostgreSQL comme source de vérité.
7. Utiliser Transactional Outbox pour les événements métier persistants.
8. Ne jamais considérer Redis comme source financière.
9. Utiliser `EventBus`/`EventDispatcher` plutôt qu’un appel direct à Kafka.
10. Ne pas installer Kafka en V1 sans décision explicite.
11. Implémenter les retries avec limites.
12. Utiliser Dead Letter pour les événements définitivement échoués.
13. Ne jamais exposer les secrets.
14. Ne jamais mettre inutilement de PII dans les événements.
15. Rendre les consommateurs idempotents.
16. Versionner les événements.
17. Préserver `correlationId`.
18. Auditer les opérations sensibles.
19. Ne jamais supprimer silencieusement un événement métier.
20. Toute intégration externe doit être remplaçable.

---

# 42. Definition of Done

L’intégration est considérée comme terminée lorsque :

* les interfaces sont définies ;
* les adapters sont isolés ;
* les Webhooks sont sécurisés ;
* l’idempotence est implémentée ;
* les timeouts sont configurés ;
* les retries sont configurés ;
* les erreurs sont normalisées ;
* l’Outbox est transactionnelle ;
* l’EventDispatcher fonctionne ;
* les événements sont versionnés ;
* les consommateurs sont idempotents ;
* les Dead Letters sont gérées ;
* les logs et métriques sont disponibles ;
* les secrets sont externalisés ;
* les tests d’intégration passent ;
* aucun provider externe n’est directement utilisé par le domaine métier ;
* aucune dépendance Kafka n’est introduite en V1 sans décision d’architecture.

---

# 43. Principe final

> **MAGIC GOLD doit être intégré par des contrats, pas par du couplage.**

Et pour les événements :

> **PostgreSQL garantit la vérité, l’Outbox garantit la persistance de l’événement, l’EventDispatcher garantit sa distribution, et Kafka pourra devenir un transport futur sans modifier le domaine métier.**
