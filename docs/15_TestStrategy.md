# MAGIC GOLD — Test Strategy

**Version : 1.1**
**Statut : Référence d’implémentation V1**
**Projet : MAGIC GOLD**

---

# 1. Objectif

Cette stratégie définit la méthode de validation de MAGIC GOLD avant chaque livraison.

Elle couvre :

* métier ;
* API ;
* sécurité ;
* paiements ;
* ventes ;
* commandes ;
* clients anonymes ;
* caisse ;
* stock ;
* notifications ;
* intégrations ;
* événements ;
* Transactional Outbox ;
* Redis ;
* reporting ;
* performance ;
* résilience ;
* déploiement.

L’objectif est d’empêcher qu’une modification technique crée une régression métier ou financière.

---

# 2. Principes

MAGIC GOLD doit être testé selon les principes suivants :

1. Tester le métier avant l’implémentation technique.
2. Les montants doivent être testés avec une précision exacte.
3. Les paiements doivent être testés contre les doublons.
4. Les Webhooks doivent être considérés comme non fiables jusqu’à validation.
5. Les données d’un marchand ne doivent jamais être visibles par un autre.
6. Un client peut acheter sans compte.
7. PostgreSQL est la source de vérité.
8. Redis ne doit jamais être considéré comme source financière.
9. Les événements doivent être persistés transactionnellement.
10. Les consommateurs d’événements doivent être idempotents.
11. Les tests de sécurité sont obligatoires.
12. Une fonctionnalité n’est pas terminée si son scénario d’échec n’est pas testé.

---

# 3. Pyramide de tests

```text
             E2E
              ▲
          API Tests
              ▲
     Integration Tests
              ▲
         Unit Tests
```

Tests complémentaires :

```text
Security
Performance
Resilience
Migration
Contract
Regression
```

---

# 4. Tests Unitaires

Les tests unitaires couvrent les règles métier isolées.

Domaines :

* calcul des totaux ;
* prix ;
* remises ;
* taxes ;
* frais ;
* arrondis ;
* quantités ;
* stock ;
* statuts ;
* transitions ;
* permissions ;
* validation ;
* idempotence ;
* mapping provider ;
* génération des références.

Exemple :

```text
10000 × 2 = 20000 XOF
```

Le système ne doit jamais produire :

```text
19999.99
```

---

# 5. Tests monétaires

Tester :

* XOF ;
* montants zéro ;
* montants maximum ;
* décimales interdites si non pertinentes ;
* arrondis ;
* remises ;
* taxes ;
* frais ;
* remboursements ;
* remboursement partiel ;
* remboursement total ;
* cumul des remboursements.

Invariant :

```text
totalRefunded <= paidAmount
```

---

# 6. Tests Vente / Caisse

Scénarios obligatoires :

### Vente anonyme

```text
Produit
→ Quantité
→ Total
→ Paiement
→ Vente terminée
→ Reçu
```

Sans :

* compte client ;
* téléphone ;
* email ;
* nom.

### Client identifié

```text
Produit
→ Client
→ Vente
→ Paiement
→ Reçu
→ Historique client
```

### Client téléphone uniquement

```text
Produit
→ Téléphone
→ Vente
→ Paiement
→ Reçu SMS éventuel
```

---

# 7. Tests Commande

Tester :

```text
DRAFT
→ PENDING_PAYMENT
→ PAID
→ CONFIRMED
→ PROCESSING
→ READY
→ COMPLETED
```

Et les chemins d’erreur :

```text
PENDING_PAYMENT
→ PAYMENT_FAILED

PENDING_PAYMENT
→ CANCELLED

PAID
→ REFUND_PENDING
→ REFUNDED
```

Les transitions interdites doivent être rejetées.

---

# 8. Tests Paiement

Tester :

* initiation ;
* paiement réussi ;
* paiement échoué ;
* paiement en attente ;
* timeout ;
* provider indisponible ;
* paiement déjà traité ;
* paiement inconnu ;
* vérification serveur ;
* remboursement ;
* remboursement partiel ;
* remboursement total.

---

# 9. Tests Webhook

Pour chaque provider :

### Cas nominal

```text
Webhook valide
→ signature valide
→ paiement mis à jour
```

### Doublon

```text
Webhook A
Webhook A
```

Résultat :

```text
une seule modification métier
```

### Signature invalide

Résultat :

```text
HTTP error
Aucune modification métier
```

### Payload invalide

Résultat :

```text
rejet
Aucune modification métier
```

### Webhook hors ordre

Le système doit appliquer les règles métier et ne pas régresser un paiement vers un état incohérent.

---

# 10. Tests Idempotence

Tester au minimum :

```text
CREATE_SALE
CREATE_ORDER
INITIATE_PAYMENT
PAYMENT_WEBHOOK
REFUND
NOTIFICATION
```

Exemple :

```text
Request A
Request A
Request A
```

Résultat :

```text
1 opération métier
```

---

# 11. Tests Transactional Outbox

C’est un ensemble de tests obligatoire.

## 11.1 Transaction réussie

```text
BEGIN
INSERT business data
INSERT outbox event
COMMIT
```

Résultat :

```text
business data = présente
outbox event = présente
```

## 11.2 Transaction échouée

```text
BEGIN
INSERT business data
INSERT outbox event
ROLLBACK
```

Résultat :

```text
business data = absente
outbox event = absent
```

Aucun événement orphelin ne doit être créé.

---

# 12. Tests Outbox Dispatcher

Tester :

* récupération des événements `PENDING` ;
* verrouillage ;
* traitement ;
* passage à `PROCESSED` ;
* échec ;
* retry ;
* backoff ;
* passage à `DEAD_LETTER` ;
* reprise après redémarrage ;
* concurrence entre workers.

---

# 13. Tests de doublons d’événements

Simuler :

```text
Event A
Event A
```

Le consommateur doit produire une seule opération métier.

Exemple :

```text
PAYMENT_SUCCEEDED
PAYMENT_SUCCEEDED
```

Le montant payé ne doit pas être comptabilisé deux fois.

---

# 14. Tests de perte d’événement

Scénario :

```text
Transaction métier
       ↓
Outbox
       ↓
Crash application
```

Après redémarrage :

```text
Outbox Worker
       ↓
reprend l’événement
```

Aucun événement validé ne doit être silencieusement perdu.

---

# 15. Tests d’événements invalides

Tester :

* payload corrompu ;
* type inconnu ;
* version inconnue ;
* aggregateId inexistant ;
* données obligatoires absentes.

Résultat attendu :

```text
event rejected
+
log
+
audit si nécessaire
+
retry contrôlé ou DEAD_LETTER
```

---

# 16. Tests Event Versioning

Tester :

```text
PAYMENT_SUCCEEDED v1
PAYMENT_SUCCEEDED v2
```

Les consommateurs compatibles doivent continuer à fonctionner.

Une modification incompatible ne doit pas casser silencieusement les consommateurs existants.

---

# 17. Tests Event Ordering

Lorsque l’ordre est nécessaire pour un agrégat :

```text
PAYMENT_INITIATED
→ PAYMENT_SUCCEEDED
→ REFUND_REQUESTED
→ REFUND_COMPLETED
```

Tester :

* ordre normal ;
* événement retardé ;
* événement dupliqué ;
* événement hors ordre.

Il ne faut pas imposer un ordre global à tous les événements.

---

# 18. Tests futurs Kafka

Même sans Kafka en V1, l’architecture doit être testable contre le contrat :

```text
EventBus
```

Créer des tests de contrat permettant de vérifier qu’un futur :

```text
KafkaEventBus
```

respectera le même comportement attendu que le dispatcher V1.

Le domaine métier ne doit pas changer lors du changement de transport.

---

# 19. Tests Notifications

Tester :

* SMS ;
* Email ;
* Push ;
* In-App ;
* provider indisponible ;
* timeout ;
* retry ;
* doublon ;
* mauvais numéro ;
* mauvais email ;
* préférences utilisateur ;
* notification obligatoire ;
* notification marketing.

Une notification ne doit pas devenir la source de vérité d’une transaction.

---

# 20. Tests Redis

Tester :

* cache hit ;
* cache miss ;
* expiration ;
* invalidation ;
* OTP ;
* rate limiting ;
* idempotency ;
* lock ;
* panier temporaire ;
* état temporaire de paiement.

---

# 21. Redis indisponible

Simuler :

```text
MAGIC GOLD
   ↓
Redis DOWN
```

Le système doit appliquer une stratégie définie selon la fonction.

Exemples :

```text
Cache → fallback PostgreSQL
OTP → comportement contrôlé
Rate limiting → stratégie de secours
Session → comportement contrôlé
```

Aucune donnée financière définitive ne doit être perdue parce que Redis est indisponible.

---

# 22. Tests Sécurité

Tester obligatoirement :

* authentification ;
* autorisation ;
* expiration token ;
* rôles ;
* permissions ;
* IDOR ;
* multi-tenant isolation ;
* accès merchant ;
* accès support ;
* accès finance ;
* accès admin ;
* accès audit ;
* injection SQL ;
* XSS ;
* CSRF selon architecture ;
* rate limiting ;
* brute force ;
* secrets ;
* Webhooks.

---

# 23. Test Multi-Tenant

Créer :

```text
Merchant A
Merchant B
```

Puis vérifier :

```text
Merchant A
   ↓
ne peut jamais lire
   ↓
Merchant B
```

Tester :

* clients ;
* produits ;
* ventes ;
* commandes ;
* paiements ;
* rapports ;
* stock ;
* caisse ;
* utilisateurs.

Le filtrage ne doit pas dépendre uniquement du frontend.

---

# 24. Tests Permissions

Tester séparément :

```text
ROLE
PERMISSION
OWNERSHIP
SCOPE
```

Un utilisateur possédant un rôle ne doit obtenir que les permissions effectivement attribuées.

Exemple :

```text
CASHIER
```

ne doit pas pouvoir :

```text
modifier configuration provider
```

simplement parce qu’il est authentifié.

---

# 25. Tests Client Anonyme

Tester :

```text
customerId = null
```

et vérifier :

* vente possible ;
* paiement possible ;
* reçu possible ;
* aucun compte créé automatiquement ;
* historique personnel impossible sans identification ;
* aucune obligation de téléphone ;
* aucune obligation d’email.

---

# 26. Tests Confidentialité

Vérifier :

* minimisation PII ;
* masquage des données ;
* absence de secrets dans les logs ;
* absence d’OTP dans les logs ;
* absence de credentials provider ;
* contrôle d’accès à l’historique client ;
* anonymisation éventuelle.

---

# 27. Tests API

Pour chaque endpoint :

* HTTP status ;
* payload ;
* validation ;
* authentication ;
* authorization ;
* erreurs ;
* idempotency ;
* pagination ;
* filtres ;
* tri ;
* limites ;
* performance.

Tester :

```text
200
201
400
401
403
404
409
422
429
500
```

selon le contrat API.

---

# 28. Tests d’Intégration

Utiliser autant que possible des environnements proches de la production.

Technologies possibles :

```text
PostgreSQL
Redis
Testcontainers
Mock providers
Sandbox providers
```

Tester les interactions réelles entre :

```text
API
→ Service
→ PostgreSQL
→ Redis
→ Outbox
→ Dispatcher
→ Provider
```

---

# 29. Tests de concurrence

Tester :

* deux caissiers vendant le même stock ;
* deux paiements simultanés ;
* double clic ;
* double Webhook ;
* deux remboursements simultanés ;
* deux modifications du même produit ;
* deux workers Outbox.

Le système doit empêcher les doubles opérations métier.

---

# 30. Tests Stock

Tester :

```text
stock = 10
vente = 3
→ stock = 7
```

Puis :

```text
deux ventes simultanées de 7
```

Le système doit empêcher la vente de plus que le stock disponible selon les règles de concurrence définies.

---

# 31. Tests Reporting

Vérifier que les rapports utilisent les données PostgreSQL de référence.

Tester :

* CA ;
* ventes ;
* paiements ;
* remboursements ;
* produits ;
* stock ;
* clients ;
* caisse ;
* utilisateurs.

Les ventes anonymes doivent être comptabilisées sans tentative de réidentification.

---

# 32. Tests Cohérence financière

Invariants :

```text
refundedAmount <= paidAmount
```

```text
paidAmount <= totalAmount
```

selon le scénario métier.

Les montants doivent rester cohérents entre :

```text
Sale
Order
Payment
Refund
Reporting
```

---

# 33. Tests intégration Provider

Chaque provider doit être testé avec :

* succès ;
* refus ;
* timeout ;
* réponse invalide ;
* doublon ;
* Webhook ;
* signature ;
* remboursement ;
* indisponibilité.

Les adapters doivent être testés indépendamment du domaine.

---

# 34. Tests Résilience

Simuler :

```text
PostgreSQL DOWN
Redis DOWN
Payment Provider DOWN
SMS Provider DOWN
Email Provider DOWN
Event Dispatcher DOWN
```

Vérifier que le système :

* échoue proprement ;
* ne perd pas les données critiques ;
* reprend après rétablissement ;
* n’effectue pas de double transaction ;
* produit les logs nécessaires.

---

# 35. Tests Performance

Tester :

* temps de réponse API ;
* création vente ;
* création commande ;
* paiement ;
* recherche client ;
* recherche produit ;
* reporting ;
* traitement Outbox.

Tester progressivement :

```text
100 utilisateurs
500 utilisateurs
1000 utilisateurs
5000 utilisateurs
```

selon les objectifs réels de charge.

---

# 36. Tests Outbox sous charge

Tester :

```text
100
1 000
10 000
100 000
```

événements selon la capacité cible.

Mesurer :

* débit ;
* latence ;
* backlog ;
* âge du plus ancien événement ;
* retries ;
* Dead Letters.

---

# 37. Tests Migration

Toute migration de base doit être testée sur :

```text
base vide
base existante
base avec données réalistes
```

Vérifier :

* contraintes ;
* index ;
* données ;
* compatibilité ;
* rollback lorsque prévu ;
* performances.

---

# 38. Tests E2E

Scénario principal :

```text
Connexion vendeur
      ↓
Ouverture caisse
      ↓
Sélection produit
      ↓
Client anonyme
      ↓
Création vente
      ↓
Paiement
      ↓
Confirmation
      ↓
Reçu
      ↓
Reporting
```

Scénario client identifié :

```text
Client
→ vente
→ paiement
→ reçu
→ historique
```

Scénario commande :

```text
Commande
→ paiement
→ confirmation
→ préparation
→ livraison/exécution
→ clôture
```

---

# 39. Tests Régression

Toute correction d’un bug doit produire :

```text
1 test reproduisant le bug
+
1 correction
+
1 validation
```

Le test doit rester dans la suite de régression.

---

# 40. CI/CD

Pipeline minimal :

```text
Commit
 ↓
Compile
 ↓
Unit Tests
 ↓
Static Analysis
 ↓
Integration Tests
 ↓
Security Tests
 ↓
API Tests
 ↓
Build
 ↓
E2E
 ↓
Deploy
```

Une étape critique en échec doit empêcher la livraison selon les règles du projet.

---

# 41. Fixtures et données de test

Prévoir des jeux de données :

```text
Merchant A
Merchant B

Client anonyme
Client téléphone
Client identifié

Produit actif
Produit inactif
Produit épuisé

Paiement réussi
Paiement échoué
Paiement pending

Vente
Commande
Remboursement
```

Les données de test ne doivent pas contenir de vraies données personnelles.

---

# 42. Matrice minimale

| Domaine         | Unit | Integration | API | E2E | Security |
| --------------- | ---: | ----------: | --: | --: | -------: |
| Auth            |    ✓ |           ✓ |   ✓ |   ✓ |        ✓ |
| Merchant        |    ✓ |           ✓ |   ✓ |   ✓ |        ✓ |
| Customer        |    ✓ |           ✓ |   ✓ |   ✓ |        ✓ |
| Sale/Caisse     |    ✓ |           ✓ |   ✓ |   ✓ |        ✓ |
| Order           |    ✓ |           ✓ |   ✓ |   ✓ |        ✓ |
| Payment         |    ✓ |           ✓ |   ✓ |   ✓ |        ✓ |
| Refund          |    ✓ |           ✓ |   ✓ |   ✓ |        ✓ |
| Stock           |    ✓ |           ✓ |   ✓ |   ✓ |        ✓ |
| Notification    |    ✓ |           ✓ |   ✓ |   ✓ |        ✓ |
| Outbox          |    ✓ |           ✓ |   ✓ |   ✓ |        ✓ |
| EventDispatcher |    ✓ |           ✓ |   ✓ |   ✓ |        ✓ |
| Redis           |    ✓ |           ✓ |   ✓ |   - |        ✓ |
| Reporting       |    ✓ |           ✓ |   ✓ |   ✓ |        ✓ |
| Integration     |    ✓ |           ✓ |   ✓ |   ✓ |        ✓ |

---

# 43. Critères de sortie

Une version ne doit être considérée comme prête que si :

* les tests critiques passent ;
* aucun bug bloquant n’est ouvert ;
* aucun problème critique de sécurité n’est ouvert ;
* les scénarios de paiement passent ;
* les scénarios de remboursement passent ;
* les tests d’idempotence passent ;
* les tests multi-tenant passent ;
* les tests client anonyme passent ;
* les tests Outbox passent ;
* les tests Dispatcher passent ;
* les tests de résilience critiques passent ;
* les migrations passent ;
* les E2E critiques passent.

---

# 44. Definition of Done

Une fonctionnalité est terminée lorsque :

```text
Code
+
Tests unitaires
+
Tests intégration
+
Tests API
+
Tests sécurité
+
Tests E2E si nécessaire
+
Documentation
+
Observabilité
+
Gestion des erreurs
+
Gestion de l’idempotence
```

sont présents selon le niveau de criticité.

---

# 45. Règles Hermes AI

Hermes doit respecter strictement :

1. Toute fonctionnalité métier importante doit avoir des tests.
2. Ne jamais considérer uniquement le scénario nominal.
3. Toujours tester les doublons sur les opérations financières.
4. Toujours tester les Webhooks plusieurs fois.
5. Toujours tester les permissions et l’isolation marchand.
6. Toujours tester les clients anonymes.
7. Ne jamais utiliser Redis comme vérité financière.
8. Tester l’atomicité PostgreSQL + Outbox.
9. Tester la reprise après crash du Dispatcher.
10. Tester les retries.
11. Tester les Dead Letters.
12. Tester l’idempotence des consommateurs.
13. Tester les événements hors ordre lorsque pertinent.
14. Tester les versions d’événements.
15. Ne pas introduire Kafka en V1 simplement pour satisfaire un test technique.
16. Préparer des tests de contrat pour un futur `KafkaEventBus`.
17. Toute régression corrigée doit produire un test.
18. Ne jamais désactiver un test critique pour faire passer le build.
19. Les tests de sécurité ne doivent pas être facultatifs.
20. Les données de test doivent rester fictives.

---

# 46. Principe final

> **MAGIC GOLD n’est pas validé parce que le scénario nominal fonctionne. Il est validé lorsque les erreurs, doublons, reprises, permissions, paiements et événements sont maîtrisés.**

La priorité de test est :

```text
ARGENT
  ↓
INTÉGRITÉ DES DONNÉES
  ↓
SÉCURITÉ
  ↓
VENTE / COMMANDE
  ↓
PAIEMENT
  ↓
ÉVÉNEMENTS
  ↓
NOTIFICATIONS
  ↓
REPORTING
  ↓
PERFORMANCE
```
