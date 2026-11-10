# MAGIC GOLD — Redis Specification

**Fichier :** `16_RedisSpecification.md`
**Version :** 1.0
**Statut :** Référence technique
**Projet :** MAGIC GOLD
**Organisation :** Nectux Ingénieries

---

## 1. Objectif

Cette spécification définit l'utilisation de **Redis** dans MAGIC GOLD.

Redis est utilisé comme infrastructure de :

* cache ;
* sessions techniques ;
* OTP et données temporaires ;
* rate limiting ;
* idempotency temporaire ;
* verrous distribués ;
* files ou événements techniques si nécessaire ;
* données temporaires liées aux paiements ;
* mécanismes de performance.

Redis **ne constitue pas la source de vérité métier** de MAGIC GOLD.

---

# 2. Principe fondamental

```text
PostgreSQL
    │
    ├── Source de vérité métier
    ├── Produits
    ├── Ventes
    ├── Commandes
    ├── Paiements
    ├── Clients
    ├── Marchands
    ├── Utilisateurs
    ├── Audit
    └── Reporting

Redis
    │
    ├── Cache
    ├── Sessions temporaires
    ├── OTP
    ├── Rate limiting
    ├── Idempotency
    ├── Locks
    └── Données temporaires
```

### Règle absolue

> Si Redis est perdu, MAGIC GOLD doit pouvoir redémarrer sans perdre les données métier persistantes.

Redis peut accélérer le système.

Redis ne doit pas être nécessaire pour reconstituer l'historique financier.

---

# 3. Technologies

Version cible indicative :

```text
Redis 7.x
```

Déploiement recommandé :

```text
MAGIC GOLD Backend
        │
        ▼
     Redis
        │
        └── PostgreSQL reste la source de vérité
```

Client Java recommandé :

```text
Spring Data Redis
```

avec :

```text
Lettuce
```

---

# 4. Cas d'utilisation Redis

Redis V1 est autorisé pour les usages suivants :

| Fonction                 | Redis              |
| ------------------------ | ------------------ |
| Cache produits           | OUI                |
| Cache catégories         | OUI                |
| Cache configuration      | OUI                |
| OTP                      | OUI                |
| Rate limiting            | OUI                |
| Sessions techniques      | OUI                |
| Idempotency              | OUI                |
| Distributed lock         | OUI                |
| Paiement temporaire      | OUI                |
| État temporaire checkout | OUI                |
| Panier temporaire        | OUI, si nécessaire |
| File technique           | OPTIONNEL          |
| Vente définitive         | NON                |
| Commande définitive      | NON                |
| Paiement définitif       | NON                |
| Client définitif         | NON                |
| Marchand définitif       | NON                |
| Audit définitif          | NON                |
| Ledger financier         | NON                |
| Wallet                   | NON                |

---

# 5. Architecture Redis

MAGIC GOLD doit utiliser une abstraction Redis centralisée.

```text
RedisService
    │
    ├── CacheService
    ├── OtpService
    ├── RateLimitService
    ├── IdempotencyService
    ├── LockService
    └── TemporaryStateService
```

Les domaines métier ne doivent pas manipuler directement Redis partout dans le code.

### Mauvais

```java
redisTemplate.opsForValue().set(...)
```

dans chaque service métier.

### Recommandé

```java
cacheService.get(...)
otpService.create(...)
rateLimitService.check(...)
idempotencyService.acquire(...)
lockService.execute(...)
```

Cela permet de changer l'implémentation sans modifier les domaines métier.

---

# 6. Convention de clés

Toutes les clés MAGIC GOLD doivent utiliser un namespace.

Format :

```text
mg:{environment}:{domain}:{identifier}
```

Exemple :

```text
mg:prod:otp:phone:+2250700000000
```

En développement :

```text
mg:dev:cache:product:12345
```

En production :

```text
mg:prod:cache:product:12345
```

---

# 7. Prévention des collisions

Les clés doivent être suffisamment spécifiques.

### Incorrect

```text
user:123
```

### Correct

```text
mg:prod:session:user:123
```

### Pour un marchand

```text
mg:prod:merchant:123:products
```

### Pour un produit

```text
mg:prod:cache:product:456
```

---

# 8. TTL obligatoire

Les données temporaires Redis doivent avoir un TTL.

### Règle

> Aucun stockage temporaire MAGIC GOLD ne doit être créé sans politique d'expiration.

Exemple :

```text
OTP                 5 minutes
Rate limit          selon règle
Checkout temporaire 15 minutes
Idempotency         24 heures
Lock                quelques secondes
Cache produit       5–30 minutes
Cache configuration 5–60 minutes
```

Les TTL exacts doivent être configurables.

---

# 9. Cache

Redis peut mettre en cache les données fréquemment consultées.

Exemples :

```text
product
category
merchant profile
store
configuration
payment provider configuration
```

Exemple :

```text
mg:prod:cache:product:{productId}
```

---

# 10. Cache produit

Lorsqu'un produit est demandé :

```text
API
 │
 ▼
Redis
 │
 ├── HIT → retourner le cache
 │
 └── MISS
       │
       ▼
   PostgreSQL
       │
       ▼
     Redis
```

Pseudo-code :

```java
Product getProduct(UUID id) {

    Product cached = cache.get(id);

    if (cached != null) {
        return cached;
    }

    Product product = repository.findById(id);

    cache.put(id, product);

    return product;
}
```

---

# 11. Invalidation du cache

Lorsqu'une donnée change :

```text
UPDATE PostgreSQL
        │
        ▼
INVALIDATE Redis
```

Exemple :

```text
Produit modifié
      │
      ├── PostgreSQL UPDATE
      │
      └── Redis DELETE
```

Ne jamais mettre à jour uniquement Redis.

---

# 12. Cache et prix

Le prix est critique.

Le prix utilisé lors d'une vente doit provenir d'une donnée métier fiable.

Redis ne doit jamais être considéré comme l'autorité finale du prix.

Lors de la création d'une vente :

```text
Client
   ↓
Backend
   ↓
PostgreSQL / service métier
   ↓
Validation prix
   ↓
Snapshot du prix
   ↓
Vente
```

Le prix final doit être enregistré dans la vente.

---

# 13. OTP

Redis est adapté au stockage temporaire des OTP.

Clé :

```text
mg:prod:otp:{purpose}:{identifier}
```

Exemple :

```text
mg:prod:otp:login:+2250700000000
```

Valeur :

```text
{
  "hash": "...",
  "attempts": 0,
  "createdAt": "...",
  "purpose": "LOGIN"
}
```

TTL :

```text
300 secondes
```

### Règles

* ne jamais stocker l'OTP en clair ;
* nombre de tentatives limité ;
* expiration obligatoire ;
* invalidation après utilisation ;
* rate limiting ;
* audit de l'opération ;
* protection contre le brute force.

---

# 14. Rate Limiting

Redis doit permettre de protéger les endpoints sensibles.

Exemples :

```text
/login
/otp/request
/otp/verify
/payment/initiate
/payment/refund
/password/reset
```

Clé :

```text
mg:prod:ratelimit:{operation}:{identifier}
```

Identifier peut être :

```text
IP
userId
phone
deviceId
merchantId
```

selon le contexte.

---

# 15. Exemple Rate Limit OTP

Politique indicative :

```text
5 demandes / 15 minutes / numéro
```

et éventuellement :

```text
20 demandes / heure / IP
```

La politique doit être configurable.

---

# 16. Idempotency

Redis peut être utilisé pour empêcher le traitement simultané d'une même requête.

Particulièrement important pour :

```text
création de vente
création de commande
initiation paiement
webhook paiement
remboursement
```

Clé :

```text
mg:prod:idempotency:{operation}:{idempotencyKey}
```

Exemple :

```text
mg:prod:idempotency:payment:7f3e...
```

---

# 17. Règle d'idempotence

Une requête avec :

```http
Idempotency-Key: abc123
```

doit produire un résultat cohérent si elle est répétée.

Redis peut servir de verrou ou de stockage temporaire du résultat.

Mais la persistance finale doit rester dans PostgreSQL.

---

# 18. Webhooks de paiement

Les webhooks des fournisseurs de paiement peuvent être reçus plusieurs fois.

Architecture :

```text
Provider
   │
   ▼
Webhook API
   │
   ▼
Validate signature
   │
   ▼
Idempotency
   │
   ▼
Payment Service
   │
   ▼
PostgreSQL
```

Redis peut empêcher le traitement concurrent :

```text
mg:prod:lock:webhook:{provider}:{eventId}
```

Cependant :

> L'idempotence métier doit également être garantie par PostgreSQL.

Redis seul ne suffit pas.

---

# 19. Distributed Lock

Redis peut fournir des verrous courts.

Exemples :

```text
stock adjustment
payment processing
refund processing
merchant activation
scheduled job
```

Clé :

```text
mg:prod:lock:{resource}:{id}
```

Exemple :

```text
mg:prod:lock:payment:PAY-0000123
```

TTL obligatoire.

---

# 20. Stock

Pour une future gestion de stock concurrente, Redis peut aider à éviter certaines collisions.

Mais le stock réel reste dans PostgreSQL.

Architecture :

```text
Redis Lock
     │
     ▼
Stock Service
     │
     ▼
PostgreSQL
```

Redis ne doit jamais devenir le stock officiel.

---

# 21. Panier temporaire

MAGIC GOLD peut utiliser Redis pour un panier temporaire.

Exemple :

```text
mg:prod:cart:{cartId}
```

TTL :

```text
15–60 minutes
```

Mais lors de la validation :

```text
Cart Redis
    ↓
Validation produit/prix/stock
    ↓
Création Vente/Commande PostgreSQL
    ↓
Paiement
```

Le panier n'est donc pas la source de vérité de la vente.

---

# 22. Vente de détail

Le parcours de caisse doit rester robuste même si Redis est indisponible lorsque cela est possible.

```text
CAISSE
  ↓
Produit
  ↓
Prix
  ↓
Vente
  ↓
PostgreSQL
  ↓
Paiement
  ↓
Reçu
```

Redis peut accélérer :

```text
recherche produit
cache catalogue
session caisse
rate limiting
idempotence
```

mais ne doit pas conditionner inutilement la création définitive d'une vente.

---

# 23. Sessions

Si MAGIC GOLD utilise des sessions serveur :

```text
mg:prod:session:{sessionId}
```

Redis peut stocker :

```text
userId
merchantId
roles
expiration
device information
```

Toute information sensible doit respecter la politique de sécurité.

Pour une architecture stateless JWT :

```text
Access Token
     ↓
Backend
```

Redis n'est pas obligatoirement nécessaire pour chaque requête.

Il peut toutefois être utilisé pour :

```text
token revocation
refresh token state
device session
logout
security events
```

---

# 24. Token et sécurité

Ne jamais considérer Redis comme l'autorité principale des permissions.

Les permissions sont définies par :

```text
Identity
+
Role
+
Permission
+
Merchant scope
+
Resource ownership
```

Redis peut seulement accélérer certaines vérifications.

---

# 25. Données financières

Les données suivantes ne doivent jamais avoir Redis comme seule persistance :

```text
Sale
Order
Payment
Refund
Transaction
Invoice
Customer financial history
Merchant financial history
Audit
```

Exemple interdit :

```text
Redis
 └── payment:PAY-001
```

sans enregistrement durable PostgreSQL.

---

# 26. Pas de Wallet V1

MAGIC GOLD V1 ne possède pas de :

```text
wallet
balance
wallet transaction
internal money transfer
internal ledger
```

Redis ne doit donc jamais être utilisé pour simuler un solde.

### Interdit

```text
Redis balance = 150000 XOF
```

comme source financière.

---

# 27. Résilience

Redis doit être considéré comme une dépendance pouvant devenir temporairement indisponible.

Chaque fonctionnalité doit définir son comportement.

| Fonction        | Redis indisponible                                    |
| --------------- | ----------------------------------------------------- |
| Cache produit   | fallback PostgreSQL                                   |
| Cache catégorie | fallback PostgreSQL                                   |
| OTP             | fonctionnalité temporairement dégradée/refusée        |
| Rate limit      | stratégie de sécurité définie                         |
| Session         | fallback selon architecture                           |
| Vente           | doit rester possible si Redis n'est pas indispensable |
| Paiement        | ne jamais perdre l'état PostgreSQL                    |
| Audit           | PostgreSQL                                            |
| Reporting       | PostgreSQL                                            |
| Historique      | PostgreSQL                                            |

---

# 28. Cache Failure Strategy

Un cache Redis indisponible ne doit pas provoquer une panne générale.

Exemple :

```text
try Redis
   │
   ├── success → cache
   │
   └── failure
          ↓
      PostgreSQL
```

Les erreurs Redis doivent être observables mais ne doivent pas être exposées inutilement au client.

---

# 29. Configuration

Variables d'environnement recommandées :

```env
REDIS_ENABLED=true

REDIS_HOST=redis
REDIS_PORT=6379

REDIS_USERNAME=
REDIS_PASSWORD=

REDIS_DATABASE=0

REDIS_TIMEOUT_MS=2000

REDIS_DEFAULT_TTL_SECONDS=900
```

Pour la production :

```env
REDIS_PASSWORD=<secret>
```

Le secret ne doit jamais être commité dans Git.

---

# 30. Spring Boot

Configuration conceptuelle :

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      username: ${REDIS_USERNAME:}
      password: ${REDIS_PASSWORD:}
      timeout: ${REDIS_TIMEOUT_MS:2000}ms
```

La configuration réelle doit être adaptée à la version Spring Boot utilisée par MAGIC GOLD.

---

# 31. Sérialisation

Éviter de stocker des objets Java sérialisés nativement.

Préférer :

```text
JSON
```

ou une représentation explicitement versionnée.

Exemple :

```json
{
  "version": 1,
  "productId": "123",
  "name": "Produit",
  "price": 2500
}
```

Cela facilite :

* migration ;
* debugging ;
* compatibilité ;
* observabilité.

---

# 32. Cache Stampede

Les caches fortement sollicités doivent être protégés contre le cache stampede.

Stratégies possibles :

```text
TTL avec jitter
distributed lock
single-flight
cache warming
```

Exemple :

```text
TTL = 900 secondes
jitter = ±60 secondes
```

---

# 33. Mémoire Redis

Redis doit être surveillé.

Indicateurs :

```text
used_memory
maxmemory
evicted_keys
expired_keys
connected_clients
hit_rate
miss_rate
commands_per_second
latency
blocked_clients
```

---

# 34. Politique d'éviction

La politique doit être explicitement configurée.

Pour un Redis principalement utilisé comme cache :

```text
allkeys-lru
```

peut être approprié.

Mais les données critiques temporaires comme :

```text
OTP
idempotency
locks
```

ne doivent pas dépendre d'une éviction arbitraire.

Ces données doivent avoir :

* TTL court ;
* namespace dédié ;
* stratégie de défaillance appropriée.

---

# 35. Séparation logique

Il est recommandé de séparer les namespaces :

```text
mg:prod:cache:*
mg:prod:otp:*
mg:prod:ratelimit:*
mg:prod:idempotency:*
mg:prod:lock:*
mg:prod:session:*
mg:prod:cart:*
```

Si nécessaire, utiliser plusieurs Redis databases ou plusieurs instances selon les contraintes de production.

---

# 36. Sécurité Redis

Redis ne doit pas être exposé directement à Internet.

Architecture :

```text
Internet
   │
   ▼
Reverse Proxy
   │
   ▼
MAGIC GOLD Backend
   │
   ▼
Private Network
   │
   ├── PostgreSQL
   └── Redis
```

Redis doit écouter sur le réseau privé.

---

# 37. Docker Compose

Architecture locale indicative :

```yaml
services:

  redis:
    image: redis:7-alpine
    container_name: magic-gold-redis
    restart: unless-stopped
    command:
      - redis-server
      - --appendonly
      - "yes"
      - --requirepass
      - ${REDIS_PASSWORD}
    volumes:
      - redis_data:/data
    networks:
      - magic-gold
```

Le mot de passe doit provenir d'un secret ou d'une variable d'environnement sécurisée.

---

# 38. Persistence Redis

La persistance Redis n'a pas pour objectif de remplacer PostgreSQL.

AOF/RDB peuvent être activés pour améliorer la récupération des données temporaires.

Mais :

```text
Redis persistence ≠ database métier
```

Même avec AOF :

> Une donnée métier critique doit être persistée dans PostgreSQL.

---

# 39. Observabilité

Les logs doivent permettre d'identifier :

```text
Redis connection failure
Redis timeout
cache miss
cache hit
lock acquisition failure
rate limit rejection
OTP failure
idempotency conflict
```

Ne jamais logger :

```text
OTP
password
Redis password
payment secrets
access tokens
personal data inutile
```

---

# 40. Health Check

Le backend doit exposer un état technique permettant de déterminer si Redis est disponible.

Exemple :

```text
GET /actuator/health
```

Résultat conceptuel :

```json
{
  "redis": {
    "status": "UP"
  }
}
```

La disponibilité Redis ne doit toutefois pas nécessairement rendre toute l'application `DOWN` si Redis est uniquement utilisé pour le cache.

---

# 41. Tests

## 41.1 Cache

Tester :

```text
cache hit
cache miss
cache expiration
cache invalidation
Redis unavailable
fallback PostgreSQL
```

## 41.2 OTP

Tester :

```text
création
expiration
mauvais OTP
nombre maximal de tentatives
réutilisation impossible
rate limiting
```

## 41.3 Idempotency

Tester :

```text
requête unique
requête répétée
requêtes concurrentes
expiration
Redis indisponible
```

## 41.4 Lock

Tester :

```text
acquisition
concurrence
expiration
release
lock perdu
```

---

# 42. Tests de résilience

Scénario :

```text
Redis STOP
    ↓
MAGIC GOLD
    ↓
création vente
```

Le système doit appliquer le comportement défini par domaine.

Une panne Redis ne doit jamais entraîner :

```text
perte d'une vente PostgreSQL
```

ou :

```text
perte d'un paiement confirmé
```

---

# 43. Tests de sécurité

Vérifier :

* Redis inaccessible depuis Internet ;
* authentification Redis activée ;
* secrets hors Git ;
* aucun secret dans les logs ;
* séparation des namespaces ;
* TTL sur données temporaires ;
* absence de données financières comme source unique ;
* isolation multi-tenant au niveau applicatif ;
* protection contre l'injection via clés Redis.

---

# 44. Règles Hermes

Hermes doit impérativement respecter les règles suivantes :

### Règle 1

Redis n'est jamais la source de vérité métier.

### Règle 2

Ne jamais stocker définitivement une vente uniquement dans Redis.

### Règle 3

Ne jamais stocker définitivement un paiement uniquement dans Redis.

### Règle 4

Ne jamais implémenter un wallet avec Redis.

### Règle 5

Ne jamais utiliser Redis comme ledger financier.

### Règle 6

Tout cache doit avoir une stratégie d'invalidation.

### Règle 7

Toute donnée temporaire doit avoir un TTL.

### Règle 8

Toute opération critique doit rester cohérente même en cas de perte de Redis.

### Règle 9

Redis doit être encapsulé derrière des services techniques.

### Règle 10

Les erreurs Redis doivent être observables et gérées explicitement.

### Règle 11

Le prix final d'une vente doit être persisté dans PostgreSQL.

### Règle 12

Le statut final d'un paiement doit être persisté dans PostgreSQL.

### Règle 13

Les informations financières ne doivent jamais dépendre uniquement de Redis.

### Règle 14

Ne jamais ajouter une dépendance Redis à une fonctionnalité simplement parce que Redis est disponible.

---

# 45. Architecture cible

```text
                         MAGIC GOLD
                              │
                  ┌───────────┴───────────┐
                  │                       │
              PostgreSQL                Redis
                  │                       │
        SOURCE DE VÉRITÉ            SERVICES TECHNIQUES
                  │                       │
        ┌─────────┼─────────┐       ┌─────┼─────────┐
        │         │         │       │     │         │
      Vente    Paiement   Client   Cache  OTP    RateLimit
        │         │         │             │         │
      Order     Refund   Merchant       Lock   Idempotency
        │
      Audit
```

---

# 46. Principe final

MAGIC GOLD doit considérer Redis comme :

> **une mémoire technique rapide et temporaire au service de l'application, jamais comme la mémoire financière de l'entreprise.**

Le principe d'architecture est :

```text
POSTGRESQL = vérité métier
REDIS      = accélération + état temporaire
```

Cette séparation doit être maintenue dans toutes les implémentations futures.
