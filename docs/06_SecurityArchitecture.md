# MAGIC GOLD — Security Architecture

**Version:** 1.0
**Document:** `06_SecurityArchitecture.md`
**Projet:** MAGIC GOLD
**Statut:** Référence sécurité
**Audience:** Backend, Frontend, Mobile, DevOps, QA, Administration, Sécurité, Hermes

---

# 1. Objectif

Ce document définit l'architecture de sécurité de MAGIC GOLD.

Il constitue la référence pour :

* l'authentification ;
* l'autorisation ;
* la gestion des utilisateurs ;
* les rôles et permissions ;
* la sécurité des APIs ;
* la protection des données ;
* la sécurité des paiements ;
* la sécurité du wallet ;
* la sécurité des webhooks ;
* la gestion des secrets ;
* l'audit ;
* la journalisation ;
* la protection contre les attaques ;
* la sécurité de l'infrastructure ;
* la gestion des incidents ;
* les contrôles avant mise en production.

Toute implémentation de MAGIC GOLD doit respecter ce document.

---

# 2. Principes de sécurité

MAGIC GOLD applique les principes suivants :

```text
Zero Trust
Least Privilege
Defense in Depth
Secure by Default
Fail Secure
Separation of Duties
Data Minimization
Auditability
Explicit Authorization
```

Principe fondamental :

> Être authentifié ne signifie pas être autorisé.

---

# 3. Architecture générale

```text
                         INTERNET
                            │
                            ▼
                    ┌───────────────┐
                    │ WAF / Reverse │
                    │ Proxy / TLS   │
                    └───────┬───────┘
                            │
                            ▼
                    ┌───────────────┐
                    │ API Backend   │
                    │ MAGIC GOLD    │
                    └───────┬───────┘
                            │
             ┌──────────────┼──────────────┐
             │              │              │
             ▼              ▼              ▼
        PostgreSQL       Keycloak       Redis
             │              │
             │              │
             ▼              ▼
          Storage       Identity
             
             │
             ├──────────────► Payment Providers
             │
             ├──────────────► SMS / Email
             │
             └──────────────► External APIs
```

---

# 4. Modèle Zero Trust

Aucune requête ne doit être considérée comme fiable uniquement parce qu'elle provient :

* du frontend ;
* du mobile ;
* d'un réseau interne ;
* d'une IP connue ;
* d'un service interne.

Chaque requête sensible doit être vérifiée.

```text
Request
   ↓
Authentication
   ↓
Authorization
   ↓
Resource Ownership
   ↓
Business Validation
   ↓
Execution
```

---

# 5. Security Boundaries

Les principales frontières de sécurité sont :

```text
Internet
   │
   ▼
Reverse Proxy
   │
   ▼
API
   │
   ├── Database
   ├── Identity Provider
   ├── Cache
   ├── Storage
   └── External Providers
```

Chaque frontière doit être protégée indépendamment.

---

# 6. Identity Provider

MAGIC GOLD utilise un fournisseur d'identité compatible :

```text
OAuth 2.0
OpenID Connect
JWT
```

L'implémentation cible recommandée est :

```text
Keycloak
```

Keycloak est responsable notamment de :

* l'identité ;
* l'authentification ;
* les utilisateurs ;
* les groupes ;
* les rôles ;
* les sessions ;
* les tokens ;
* la gestion des clients OAuth/OIDC.

Le backend reste responsable de l'autorisation métier.

---

# 7. Architecture Keycloak

Architecture recommandée :

```text
                 ┌──────────────┐
                 │   Keycloak   │
                 └──────┬───────┘
                        │
          ┌─────────────┼─────────────┐
          │             │             │
          ▼             ▼             ▼
      Web Client    Mobile Client   Admin Client
          │             │
          └──────┬──────┘
                 ▼
             MAGIC GOLD API
```

---

# 8. Realm

Un realm dédié doit être utilisé pour MAGIC GOLD.

Exemple :

```text
magic-gold
```

Ne pas utiliser le realm `master` pour les utilisateurs applicatifs.

---

# 9. Clients Keycloak

Les clients doivent être séparés selon les usages.

Exemple :

```text
magicgold-web
magicgold-mobile
magicgold-api
magicgold-admin
```

Les noms définitifs peuvent être adaptés à l'environnement.

---

# 10. Client public

Une application frontend ou mobile ne doit pas embarquer un secret client confidentiel.

Pour les clients publics :

```text
Authorization Code Flow
+
PKCE
```

doit être privilégié.

---

# 11. PKCE

Le frontend/mobile doit utiliser :

```text
Authorization Code
+
PKCE
```

Le `code_verifier` doit rester côté client.

Le code d'autorisation ne doit jamais être envoyé à un serveur tiers non prévu.

---

# 12. Client confidentiel

Les services backend à backend peuvent utiliser des clients confidentiels lorsque nécessaire.

Le secret doit être stocké :

```text
Environment variable
Secret manager
Vault
Docker secret
Kubernetes secret
```

et jamais dans Git.

---

# 13. Access Token

L'API reçoit :

```http
Authorization: Bearer <access_token>
```

Le backend vérifie :

```text
signature
issuer
audience
expiration
not-before
algorithm
```

---

# 14. JWT

Les tokens doivent être courts autant que possible.

Le système doit éviter des access tokens inutilement longs.

Un token compromis doit avoir une fenêtre d'exploitation limitée.

---

# 15. Refresh Token

Les refresh tokens doivent être traités comme des secrets.

Ils ne doivent jamais être :

* loggés ;
* affichés ;
* stockés dans une URL ;
* transmis à une API tierce non autorisée.

Pour les applications web, une stratégie de stockage sûre doit être retenue.

---

# 16. Validation JWT

Le backend ne doit jamais accepter un JWT uniquement parce qu'il est correctement décodable.

Il doit vérifier la signature cryptographique.

Il doit également vérifier :

```text
iss
aud
exp
nbf
```

et les claims nécessaires.

---

# 17. Algorithmes cryptographiques

L'API doit accepter uniquement les algorithmes explicitement configurés.

Le backend ne doit pas accepter dynamiquement un algorithme annoncé par un token sans contrôle.

Les algorithmes faibles ou obsolètes sont interdits.

---

# 18. Authentication vs Authorization

## Authentication

Répond à :

```text
Qui êtes-vous ?
```

## Authorization

Répond à :

```text
Avez-vous le droit de faire cela ?
```

MAGIC GOLD doit implémenter les deux séparément.

---

# 19. RBAC

Le système utilise Role-Based Access Control.

Exemples :

```text
CUSTOMER
MERCHANT
MERCHANT_OPERATOR
SUPPORT
FINANCE
AUDITOR
ADMIN
SUPER_ADMIN
```

La liste définitive doit être synchronisée avec le document de rôles et permissions.

---

# 20. Least Privilege

Chaque utilisateur doit avoir uniquement les permissions nécessaires.

Exemple :

```text
MERCHANT_OPERATOR
```

peut :

```text
voir commandes
modifier statut commande
voir produits
```

mais ne doit pas automatiquement pouvoir :

```text
modifier wallet
voir tous les clients
modifier les rôles
consulter les secrets
```

---

# 21. Permissions

Les permissions doivent être suffisamment granulaires.

Exemples :

```text
CUSTOMER_READ
CUSTOMER_UPDATE

MERCHANT_READ
MERCHANT_UPDATE

PRODUCT_READ
PRODUCT_CREATE
PRODUCT_UPDATE
PRODUCT_DELETE

ORDER_READ
ORDER_CREATE
ORDER_UPDATE
ORDER_CANCEL

PAYMENT_READ
PAYMENT_CREATE
PAYMENT_REFUND

WALLET_READ
WALLET_TRANSFER

LOYALTY_READ
LOYALTY_REDEEM

AUDIT_READ

USER_MANAGE
ROLE_MANAGE
SYSTEM_CONFIG
```

La matrice officielle doit être maintenue dans le document dédié aux rôles et permissions.

---

# 22. Resource Ownership

Les permissions ne suffisent pas.

Un utilisateur autorisé à consulter ses commandes ne doit pas consulter celles d'un autre utilisateur.

Exemple :

```text
CUSTOMER
    │
    └── Order A → autorisé

CUSTOMER
    │
    └── Order B appartenant à quelqu'un d'autre
                       ↓
                    REFUSÉ
```

---

# 23. Protection IDOR

Toutes les APIs utilisant des identifiants doivent vérifier l'appartenance de la ressource.

Exemple :

```http
GET /api/v1/orders/{orderId}
```

Le backend doit vérifier :

```text
order.customerId == authenticatedUser.customerId
```

ou une permission administrative appropriée.

---

# 24. Backend comme source de vérité

Le frontend ne doit jamais être considéré comme une autorité de sécurité.

Le backend doit recalculer ou vérifier :

```text
price
discount
tax
total
permissions
wallet balance
loyalty points
stock
coupon eligibility
reward availability
```

---

# 25. Sécurité des commandes

Lorsqu'une commande est créée :

```text
Client
  ↓
Authentication
  ↓
Authorization
  ↓
Product validation
  ↓
Stock validation
  ↓
Price calculation
  ↓
Coupon validation
  ↓
Total calculation
  ↓
Transaction
```

Les montants transmis par le frontend sont indicatifs et ne constituent pas une source de vérité.

---

# 26. Sécurité des paiements

Les paiements sont une zone critique.

Les règles suivantes sont obligatoires :

```text
HTTPS
Idempotency
Signature verification
Webhook validation
Server-side amount calculation
Audit
Transaction consistency
Provider reconciliation
```

---

# 27. Idempotency-Key

Toute opération financière susceptible d'être répétée doit supporter l'idempotence.

Exemple :

```http
POST /api/v1/payments
Idempotency-Key: 8b2f...
```

Si la même requête est répétée :

```text
première requête
→ paiement créé

deuxième requête
→ résultat précédent
```

et non :

```text
deux paiements
```

---

# 28. Paiement et état de commande

Un paiement réussi doit provoquer une transition d'état contrôlée.

Exemple :

```text
ORDER_CREATED
       ↓
PAYMENT_PENDING
       ↓
PAYMENT_SUCCEEDED
       ↓
ORDER_PAID
```

Les transitions interdites doivent être rejetées.

---

# 29. Webhook Security

Les webhooks des fournisseurs externes doivent être vérifiés.

Selon le fournisseur :

```text
HMAC
signature
secret
timestamp
nonce
IP restriction
```

peuvent être utilisés.

Ne jamais considérer un webhook comme fiable uniquement parce qu'il appelle une URL connue.

---

# 30. Replay Attack

Un webhook capturé ne doit pas pouvoir être rejoué indéfiniment.

Mécanismes :

```text
eventId unique
timestamp
nonce
signature
processed flag
```

---

# 31. Webhook Idempotence

Chaque événement externe doit avoir un identifiant unique.

Exemple :

```text
providerEventId
```

Une contrainte unique doit empêcher le double traitement.

---

# 32. Wallet Security

Le wallet est une zone financière critique.

Le solde ne doit pas être modifié directement par un contrôleur.

Incorrect :

```text
wallet.balance += 10000
```

Préférer :

```text
WalletTransaction
       ↓
Ledger
       ↓
Calculated/updated balance
```

---

# 33. Wallet Ledger

Chaque mouvement financier doit avoir une transaction :

```text
CREDIT
DEBIT
TRANSFER
REFUND
ADJUSTMENT
```

avec :

```text
transactionId
walletId
amount
currency
type
direction
status
reference
createdAt
```

---

# 34. Double Spending

Le système doit empêcher :

```text
solde = 10 000

requête A → débit 8 000
requête B → débit 8 000
```

Les mécanismes possibles :

```text
database transaction
row locking
optimistic locking
ledger validation
unique idempotency key
```

---

# 35. Wallet Adjustment

Toute correction manuelle doit :

```text
être autorisée
être justifiée
être auditée
identifier l'opérateur
identifier l'approbateur lorsque nécessaire
```

Une correction silencieuse du solde est interdite.

---

# 36. Loyalty Security

Les points de fidélité doivent être traités comme une valeur métier.

Ils ne doivent pas être modifiables directement depuis le frontend.

Exemple :

```text
Purchase completed
       ↓
Loyalty rule
       ↓
Points transaction
       ↓
Balance update
```

---

# 37. Reward Redemption

Une récompense doit être vérifiée avant consommation :

```text
customer active
reward active
points sufficient
quantity available
date valid
eligibility valid
```

La consommation doit être atomique.

---

# 38. Coupon Security

Un coupon ne doit pas être consommé uniquement lors d'une simple validation.

Séparer :

```text
VALIDATE
```

et :

```text
REDEEM / CONSUME
```

La consommation définitive doit être liée à l'opération commerciale concernée.

---

# 39. Brute Force

Les endpoints sensibles doivent avoir des limites.

Exemples :

```text
login
OTP
password reset
coupon validation
wallet transfer
payment
```

Le système doit appliquer :

```text
rate limiting
temporary lock
progressive delay
monitoring
```

selon le contexte.

---

# 40. Account Lockout

Les tentatives d'authentification répétées doivent être surveillées.

Le mécanisme exact peut être géré par Keycloak.

Le backend ne doit pas réimplémenter inutilement une seconde authentification concurrente.

---

# 41. OTP

Si OTP est utilisé :

```text
OTP à durée courte
usage unique
limite de tentatives
limite d'envoi
journalisation
```

Un OTP ne doit jamais apparaître dans les logs.

---

# 42. Password Security

Lorsque MAGIC GOLD délègue les mots de passe à Keycloak :

```text
MAGIC GOLD API
      ↓
Keycloak
```

Le backend MAGIC GOLD ne doit pas stocker les mots de passe utilisateurs.

---

# 43. Session Security

Les sessions doivent avoir :

```text
expiration
rotation
revocation strategy
idle timeout
```

selon le type de client.

Les sessions administratives doivent avoir des contrôles plus stricts.

---

# 44. Admin Security

Les comptes administrateurs sont particulièrement sensibles.

Recommandations :

```text
MFA
strong authentication
shorter session
audit
least privilege
separate administrative client
```

---

# 45. MFA

Le MFA doit être disponible ou obligatoire pour les comptes à privilèges élevés selon la politique de sécurité retenue.

Priorité :

```text
SUPER_ADMIN
ADMIN
FINANCE
```

---

# 46. Separation of Duties

Les opérations critiques doivent éviter qu'un seul utilisateur puisse :

```text
initier
+
valider
+
corriger
```

une opération financière sensible lorsque le niveau de risque l'exige.

Exemple :

```text
Operator
   ↓
Initiates refund

Finance/Admin
   ↓
Approves refund
```

---

# 47. Audit Trail

Les opérations sensibles doivent être auditées.

Exemples :

```text
login
logout
failed login
role change
permission change
merchant modification
payment
refund
wallet adjustment
wallet transfer
reward redemption
coupon usage
configuration change
```

---

# 48. Audit Log

Un audit doit au minimum permettre d'identifier :

```text
actor
action
entity
entityId
timestamp
result
requestId
correlationId
IP lorsque pertinent
userAgent lorsque pertinent
```

---

# 49. Audit Immutability

Les logs d'audit ne doivent pas être modifiables par les utilisateurs standards.

Les privilèges d'administration doivent eux-mêmes être audités.

---

# 50. Logging Security

Les logs applicatifs ne doivent jamais contenir :

```text
password
OTP
JWT
refresh token
API secret
private key
full payment credential
```

Les données personnelles doivent être minimisées.

---

# 51. PII

Les données personnelles doivent être identifiées et protégées.

Exemples :

```text
nom
prénom
téléphone
email
adresse
identifiants
```

L'accès doit être limité au besoin métier.

---

# 52. Data Minimization

Une API ne doit retourner que les données nécessaires.

Exemple :

```text
GET /customers/me
```

ne doit pas retourner :

```text
internal security metadata
password metadata
provider secrets
internal audit fields
```

---

# 53. Encryption in Transit

Toutes les communications externes doivent utiliser :

```text
HTTPS / TLS
```

En production :

```text
HTTP → redirection ou blocage
HTTPS → obligatoire
```

---

# 54. TLS

Les versions obsolètes de TLS doivent être désactivées.

La configuration doit privilégier des versions modernes et sûres.

---

# 55. Encryption at Rest

Les données sensibles doivent être protégées au repos selon le niveau de risque.

Cela peut inclure :

```text
database encryption
disk encryption
encrypted backups
encrypted object storage
```

---

# 56. Database Security

La base de données doit :

```text
ne pas être exposée directement à Internet
utiliser un compte applicatif dédié
utiliser le principe de moindre privilège
utiliser des credentials forts
être sauvegardée
être surveillée
```

---

# 57. Database User

L'application ne doit pas utiliser :

```text
postgres superuser
```

comme compte applicatif en production.

Créer un utilisateur spécifique.

Exemple :

```text
magicgold_app
```

avec uniquement les privilèges nécessaires.

---

# 58. Database Network

Architecture :

```text
Internet
   X
   │
PostgreSQL

Internet
   │
   ▼
API
   │
   ▼
Private Network
   │
   ▼
PostgreSQL
```

La base doit rester sur un réseau privé lorsque l'infrastructure le permet.

---

# 59. Redis Security

Redis ne doit pas être exposé publiquement.

Utiliser :

```text
private network
authentication
TLS lorsque nécessaire
firewall
```

Les données temporaires sensibles doivent avoir des TTL.

---

# 60. Cache Security

Ne jamais mettre dans un cache partagé des données privées sans clé d'isolation appropriée.

Incorrect :

```text
customer:profile
```

Préférer :

```text
customer:{customerId}:profile
```

---

# 61. Secrets Management

Les secrets incluent :

```text
database password
Keycloak client secret
payment API keys
webhook secrets
SMTP credentials
JWT signing keys
encryption keys
```

Ils doivent être gérés hors du code source.

---

# 62. Git Security

Interdiction de commiter :

```text
.env
production credentials
private keys
API secrets
database passwords
JWT secrets
```

Un `.gitignore` approprié doit être présent.

---

# 63. Secret Rotation

Les secrets critiques doivent pouvoir être renouvelés.

Exemples :

```text
database password
provider API key
webhook secret
client secret
```

L'architecture ne doit pas dépendre d'un secret permanent impossible à changer.

---

# 64. Environment Separation

Les environnements doivent être séparés :

```text
DEV
STAGING
PRODUCTION
```

Ils doivent avoir :

```text
credentials différents
database différentes
Keycloak realms/environnements adaptés
payment credentials adaptés
```

Ne jamais utiliser les credentials production en développement.

---

# 65. Production Payment Mode

Les credentials de paiement de test ne doivent jamais être confondus avec ceux de production.

Le système doit avoir un mécanisme explicite :

```text
PAYMENT_ENVIRONMENT=TEST
```

ou :

```text
PAYMENT_ENVIRONMENT=PRODUCTION
```

avec contrôles appropriés.

---

# 66. Input Validation

Toutes les entrées externes sont non fiables.

Sources :

```text
HTTP parameters
JSON
multipart files
headers
webhooks
query strings
path variables
```

Toutes doivent être validées.

---

# 67. SQL Injection

L'application doit utiliser :

```text
prepared statements
JPA parameter binding
parameterized queries
```

Les requêtes SQL construites par concaténation de données utilisateur sont interdites.

---

# 68. XSS

Les données utilisateurs ne doivent pas être rendues comme HTML non contrôlé.

Le frontend doit :

```text
escape
sanitize when necessary
avoid v-html for untrusted data
```

---

# 69. CSRF

Si l'authentification repose sur des cookies, une stratégie CSRF doit être mise en place.

Si l'application utilise uniquement :

```text
Authorization: Bearer
```

sans authentification cookie, le modèle de risque est différent mais doit tout de même être documenté.

---

# 70. CORS

Les origines autorisées doivent être explicitement configurées.

Interdit en production sans justification :

```text
*
```

---

# 71. Security Headers

Le reverse proxy ou backend doit considérer :

```text
Strict-Transport-Security
Content-Security-Policy
X-Content-Type-Options
Referrer-Policy
Permissions-Policy
```

selon l'architecture frontend et les besoins de compatibilité.

---

# 72. Clickjacking

Les interfaces sensibles doivent être protégées contre l'intégration non autorisée dans des frames.

Une politique appropriée doit être configurée.

---

# 73. File Upload Security

Les uploads doivent contrôler :

```text
size
extension
MIME type
content
filename
storage path
```

Le nom fourni par l'utilisateur ne doit pas déterminer directement le chemin filesystem.

---

# 74. Malware Scanning

Si MAGIC GOLD accepte des documents ou images provenant de sources non fiables, un mécanisme antivirus/antimalware peut être intégré selon le niveau de risque.

---

# 75. SSRF

Les APIs qui acceptent des URLs externes doivent empêcher les accès arbitraires à :

```text
localhost
127.0.0.1
private networks
cloud metadata endpoints
internal services
```

---

# 76. Open Redirect

Les URLs de redirection reçues du client doivent être validées.

Ne jamais permettre :

```text
redirect=https://attacker.example
```

sans validation.

---

# 77. API Enumeration

Les endpoints sensibles doivent éviter de révéler inutilement si une ressource existe.

Exemple :

```text
user enumeration
account enumeration
coupon enumeration
```

Les messages doivent être conçus en conséquence.

---

# 78. Error Handling

Les erreurs doivent être utiles mais minimales.

Ne pas retourner :

```text
NullPointerException
SQLState
database hostname
filesystem path
stack trace
```

en production.

---

# 79. Dependency Security

Les dépendances doivent être régulièrement contrôlées.

Exemples :

```text
Spring Boot
Spring Security
Keycloak libraries
Jackson
database drivers
frontend dependencies
mobile dependencies
```

Les vulnérabilités critiques doivent être évaluées rapidement.

---

# 80. Dependency Scanning

Le pipeline CI/CD devrait intégrer :

```text
dependency vulnerability scan
secret scan
SAST
container scan
```

selon les outils retenus.

---

# 81. Container Security

Les containers doivent :

```text
utiliser des images maintenues
éviter root lorsque possible
minimiser les packages
ne pas contenir de secrets
être scannés
```

---

# 82. Docker

Éviter :

```dockerfile
USER root
```

en runtime lorsque ce n'est pas nécessaire.

Les images doivent être reproductibles et versionnées.

---

# 83. Network Security

Les services doivent être segmentés.

Exemple :

```text
PUBLIC
  │
  ▼
Reverse Proxy
  │
  ▼
API Network
  │
  ├── Database Network
  ├── Redis
  └── Internal Services
```

---

# 84. Firewall

Le firewall doit autoriser uniquement les ports nécessaires.

Exemple conceptuel :

```text
80/443 → public
5432   → private only
6379   → private only
8080   → private/application
```

Les ports réels dépendent du déploiement.

---

# 85. Admin Interfaces

Les interfaces d'administration doivent être protégées.

Ne pas exposer directement sur Internet :

```text
database admin
Redis admin
container management
Keycloak administration
server SSH
```

sans protection adaptée.

---

# 86. SSH

L'accès serveur doit privilégier :

```text
SSH keys
```

plutôt que des mots de passe.

Désactiver le login root direct lorsque possible.

---

# 87. Backup Security

Les backups doivent être :

```text
automatiques
chiffrés
testés
isolés
versionnés
```

Un backup non testé n'est pas considéré comme une stratégie de restauration fiable.

---

# 88. Restore Test

Des tests périodiques doivent vérifier :

```text
backup exists
backup readable
database restorable
application restartable
data consistency
```

---

# 89. Disaster Recovery

Le système doit définir :

```text
RPO
RTO
backup frequency
retention
restore procedure
```

Les valeurs exactes doivent être définies dans l'architecture d'exploitation.

---

# 90. Monitoring Security

Surveiller notamment :

```text
failed logins
rate limit violations
permission denied
payment failures
webhook failures
wallet anomalies
unusual admin activity
database errors
API error spikes
```

---

# 91. Alerting

Des alertes doivent être configurées pour les événements critiques.

Exemples :

```text
multiple admin login failures
unusual payment failure rate
repeated webhook signature failures
unexpected wallet adjustments
mass permission changes
database outage
```

---

# 92. Incident Response

En cas d'incident :

```text
Detect
  ↓
Contain
  ↓
Investigate
  ↓
Eradicate
  ↓
Recover
  ↓
Review
```

Chaque incident critique doit être documenté.

---

# 93. Compromised Credentials

En cas de compromission :

```text
revoke credential
rotate secret
invalidate sessions if necessary
investigate access
review logs
restore secure configuration
```

---

# 94. Compromised User Account

Mesures possibles :

```text
disable account
revoke sessions
reset credentials
force MFA
review transactions
review audit trail
```

---

# 95. Payment Incident

En cas d'anomalie de paiement :

```text
freeze affected operation if necessary
verify provider status
reconcile
check webhook events
check transaction ledger
audit changes
```

Ne jamais corriger manuellement uniquement le solde sans trace comptable.

---

# 96. Wallet Incident

En cas d'anomalie wallet :

```text
identify transaction
lock affected operation if necessary
verify ledger
verify idempotency
verify provider
reconcile balance
audit correction
```

---

# 97. Security Testing

Avant production :

```text
SAST
DAST
dependency scan
container scan
API security tests
authorization tests
penetration testing
```

selon le niveau de criticité.

---

# 98. Authorization Test Matrix

Les tests doivent vérifier :

```text
CUSTOMER → own resource
CUSTOMER → other customer's resource

MERCHANT → own merchant
MERCHANT → other merchant

OPERATOR → permitted operation
OPERATOR → forbidden operation

ADMIN → administrative operation
NON-ADMIN → administrative operation
```

---

# 99. Security Regression

Toute correction de sécurité doit produire un test de non-régression.

Exemple :

```text
Bug:
Customer A pouvait voir Order B.

Fix:
Ownership check.

Test:
Customer A → Order B → 403/404
```

---

# 100. Penetration Testing

Un test de pénétration doit notamment examiner :

```text
authentication
authorization
IDOR
SQL injection
XSS
CSRF
SSRF
file upload
rate limiting
JWT validation
webhooks
payment flows
wallet
admin APIs
```

---

# 101. Privacy

Les données personnelles doivent être traitées selon la réglementation applicable au contexte de MAGIC GOLD.

Le système doit prévoir :

```text
data minimization
access control
retention
deletion/anonymization where applicable
audit
consent/notice where applicable
```

---

# 102. Data Retention

Les durées de conservation doivent être définies par type de donnée :

```text
account
transactions
audit
logs
notifications
temporary data
backups
```

Les durées définitives doivent être définies avec les exigences métier et réglementaires.

---

# 103. Data Deletion

La suppression d'un compte ne doit pas entraîner automatiquement la suppression d'informations financières ou d'audit qui doivent légalement ou fonctionnellement être conservées.

Une stratégie peut utiliser :

```text
anonymization
pseudonymization
retention
```

selon les obligations applicables.

---

# 104. Pseudonymisation

Lorsque l'identité complète n'est pas nécessaire, préférer :

```text
customerId
internal reference
hashed/pseudonymous identifier
```

aux données personnelles directement identifiantes.

---

# 105. API Security Checklist

```text
[ ] HTTPS
[ ] JWT validation
[ ] issuer validation
[ ] audience validation
[ ] expiration validation
[ ] RBAC
[ ] permission checks
[ ] ownership checks
[ ] input validation
[ ] output filtering
[ ] rate limiting
[ ] CORS
[ ] security headers
[ ] audit
[ ] error sanitization
```

---

# 106. Payment Security Checklist

```text
[ ] Server-side amount calculation
[ ] Idempotency
[ ] Webhook signature
[ ] Replay protection
[ ] Provider event ID
[ ] Transaction consistency
[ ] Reconciliation
[ ] Audit
[ ] No payment secrets in frontend
[ ] No payment credentials in logs
```

---

# 107. Wallet Security Checklist

```text
[ ] Ledger
[ ] Transaction ID
[ ] Idempotency
[ ] Concurrency protection
[ ] Balance validation
[ ] Audit
[ ] Manual adjustment control
[ ] Reconciliation
```

---

# 108. Admin Security Checklist

```text
[ ] MFA
[ ] Least privilege
[ ] Separate admin permissions
[ ] Session timeout
[ ] Audit
[ ] IP/network restrictions when appropriate
[ ] No shared administrator accounts
```

---

# 109. DevSecOps Pipeline

Pipeline recommandé :

```text
Git Push
   ↓
Lint
   ↓
Unit Tests
   ↓
SAST
   ↓
Dependency Scan
   ↓
Secret Scan
   ↓
Build
   ↓
Container Scan
   ↓
Integration Tests
   ↓
API Security Tests
   ↓
Deploy Staging
   ↓
DAST
   ↓
Approval
   ↓
Production
```

---

# 110. Security Gates

Le pipeline doit pouvoir bloquer une livraison en cas de :

```text
critical vulnerability
secret detected
failed security test
failed authorization test
unsafe dependency
container critical vulnerability
```

Les exceptions doivent être documentées et approuvées.

---

# 111. Threat Model

Les principales menaces sont :

```text
Account takeover
Credential theft
Brute force
IDOR
Privilege escalation
SQL injection
XSS
CSRF
SSRF
Webhook forgery
Replay attack
Payment duplication
Double spending
Coupon abuse
Reward abuse
Data leakage
Secret leakage
DDoS
Insider misuse
```

---

# 112. Security Priority

Priorité critique :

```text
Authentication
Authorization
Payments
Wallet
Secrets
Personal data
Admin
Webhooks
```

Priorité élevée :

```text
Orders
Coupons
Loyalty
Files
Notifications
```

---

# 113. Règles obligatoires pour Hermes

Hermes doit respecter impérativement :

1. Lire `06_SecurityArchitecture.md` avant toute implémentation de sécurité.
2. Ne jamais désactiver Spring Security pour faciliter un test.
3. Ne jamais mettre une route sensible en `permitAll` sans justification documentée.
4. Ne jamais exposer les entités JPA directement.
5. Ne jamais accepter les montants financiers du frontend comme source de vérité.
6. Ne jamais modifier directement le solde wallet.
7. Toujours utiliser l'idempotence pour les opérations financières concernées.
8. Vérifier l'ownership des ressources.
9. Vérifier les permissions côté backend.
10. Ne jamais stocker de secret dans Git.
11. Ne jamais logger les tokens, mots de passe ou OTP.
12. Ne jamais désactiver la validation JWT pour résoudre un problème local.
13. Ne jamais contourner la signature des webhooks.
14. Ajouter un test lorsqu'une vulnérabilité est corrigée.
15. Documenter toute dérogation à cette spécification.
16. Ne jamais supprimer une protection de sécurité sans analyse d'impact.
17. Ne pas remplacer une vraie correction par une protection uniquement frontend.

---

# 114. Règle de modification

Toute modification de sécurité doit suivre :

```text
Analyse
   ↓
Modification
   ↓
Tests
   ↓
Security Review
   ↓
Documentation
```

Les changements suivants nécessitent une revue particulière :

```text
Keycloak
JWT
Spring Security
Roles
Permissions
Payments
Wallet
Webhooks
Secrets
Database access
Network exposure
Admin APIs
```

---

# 115. Security Definition of Done

Une fonctionnalité n'est considérée comme terminée que si :

```text
[ ] Authentication définie
[ ] Authorization définie
[ ] Ownership vérifié
[ ] Validation implémentée
[ ] Erreurs sécurisées
[ ] Audit si nécessaire
[ ] Tests unitaires
[ ] Tests d'intégration
[ ] Tests authorization
[ ] Tests sécurité
[ ] Documentation API
[ ] Aucun secret exposé
[ ] Aucun bypass de sécurité
```

---

# 116. Documents liés

Cette architecture doit être utilisée avec :

```text
00_ProjectVision.md
01_PRD.md
02_FunctionalSpecification.md
03_TechnicalArchitecture.md
04_DataModel.md
05_API_Specification.md
07_UI_UX_Specification.md
15_Roles_Permissions_Audit.md
18_TestStrategy.md
20_Data_Protection_Compliance.md
```

---

# 117. Source de vérité sécurité

La hiérarchie de référence est :

```text
Security Architecture
        ↓
Roles & Permissions
        ↓
API Specification
        ↓
Implementation
        ↓
Tests
        ↓
Security Audit
```

Toute divergence entre le code et cette documentation doit être identifiée, justifiée et documentée.

---

# 118. Conclusion

La sécurité de MAGIC GOLD ne doit pas être considérée comme une fonctionnalité ajoutée à la fin du développement.

Elle doit être intégrée dès la conception :

```text
Identity
   +
Authorization
   +
Data Protection
   +
Financial Security
   +
Audit
   +
Infrastructure Security
   +
Secure Development
```

Le principe directeur est :

> **Aucune confiance implicite, aucun privilège inutile, aucune opération financière sans traçabilité.**
