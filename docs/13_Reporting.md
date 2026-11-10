# MAGIC GOLD — Reporting Specification

**Fichier :** `13_Reporting.md`
**Version :** 1.0
**Statut :** Référence fonctionnelle et technique
**Projet :** MAGIC GOLD
**Organisation :** Nectux Ingénieries

---

## 1. Objectif

Le module Reporting permet à MAGIC GOLD de transformer les données métier persistées en indicateurs exploitables par les différents acteurs.

Le reporting doit permettre notamment de suivre :

* ventes ;
* chiffre d'affaires ;
* paiements ;
* remboursements ;
* produits ;
* stock ;
* clients ;
* activité des caisses ;
* activité des utilisateurs ;
* performance commerciale ;
* anomalies ;
* activité des marchands.

---

# 2. Principe fondamental

Le reporting est calculé à partir des données métier persistées.

```text
PostgreSQL
    │
    ├── Ventes
    ├── Commandes
    ├── Paiements
    ├── Remboursements
    ├── Produits
    ├── Stock
    ├── Clients
    ├── Marchands
    └── Utilisateurs
             │
             ▼
         Reporting
```

Redis peut éventuellement accélérer certaines requêtes, mais ne constitue jamais la source du reporting financier.

---

# 3. Principes

Le reporting doit être :

* exact ;
* traçable ;
* cohérent ;
* filtrable ;
* sécurisé ;
* multi-tenant ;
* reproductible ;
* adapté au rôle de l'utilisateur.

Les montants financiers doivent être issus des transactions persistées.

---

# 4. Périmètre V1

## Inclus

* tableau de bord marchand ;
* ventes ;
* chiffre d'affaires ;
* paiements ;
* remboursements ;
* produits ;
* catégories ;
* clients ;
* activité des utilisateurs ;
* activité caisse ;
* rapports par période ;
* exports CSV/XLSX/PDF selon besoin ;
* filtres ;
* agrégation journalière, hebdomadaire, mensuelle.

## Hors périmètre V1

* comptabilité générale complète ;
* consolidation multi-entreprises complexe ;
* BI avancée ;
* prévisions IA ;
* scoring client ;
* programme de fidélité ;
* comptabilité bancaire complète.

---

# 5. Acteurs

| Acteur            | Reporting                          |
| ----------------- | ---------------------------------- |
| MERCHANT_OWNER    | Tous les rapports de son périmètre |
| MERCHANT_ADMIN    | Rapports autorisés                 |
| CASHIER           | Rapports de sa caisse/session      |
| MERCHANT_OPERATOR | Rapports autorisés                 |
| FINANCE           | Paiements/remboursements           |
| SUPPORT           | Consultation limitée               |
| AUDITOR           | Lecture/audit                      |
| ADMIN             | Supervision globale                |
| SUPER_ADMIN       | Supervision complète               |

Un acheteur anonyme n'est pas un acteur de reporting authentifié.

---

# 6. Dashboard marchand

Le dashboard peut afficher :

```text
CA du jour
Nombre de ventes
Panier moyen
Paiements
Remboursements
Produits vendus
Top produits
Top catégories
Clients identifiés
Ventes anonymes
```

Exemple :

```text
Aujourd'hui

Ventes                 148
Chiffre d'affaires     1 845 000 XOF
Panier moyen             12 466 XOF
Paiements digitaux     1 230 000 XOF
Espèces                  615 000 XOF
Remboursements            25 000 XOF
```

---

# 7. Chiffre d'affaires

Le CA doit être défini explicitement.

Pour V1 :

```text
CA brut = somme des ventes validées
```

Selon le besoin métier, les indicateurs suivants peuvent être séparés :

```text
CA brut
Remises
Taxes
Frais
Remboursements
CA net
```

Ne jamais mélanger ces notions dans une seule métrique ambiguë.

---

# 8. Ventes

Indicateurs :

* nombre de ventes ;
* montant total ;
* panier moyen ;
* ventes annulées ;
* ventes remboursées ;
* ventes par période ;
* ventes par produit ;
* ventes par catégorie ;
* ventes par utilisateur ;
* ventes par caisse.

---

# 9. Paiements

Indicateurs :

* montant payé ;
* nombre de paiements ;
* paiements réussis ;
* paiements échoués ;
* paiements en attente ;
* remboursements ;
* paiements par méthode ;
* paiements par fournisseur ;
* paiements par période.

Exemple :

```text
MOBILE_MONEY
CARD
QR_PAYMENT
CASH
BANK_TRANSFER
OTHER
```

---

# 10. Paiements par fournisseur

Exemple :

```text
Provider A
Provider B
Provider C
```

Les rapports doivent permettre de comparer les volumes sans modifier les données financières.

---

# 11. Produits

Indicateurs :

* produits vendus ;
* quantités vendues ;
* chiffre d'affaires par produit ;
* produits sans vente ;
* produits en rupture ;
* produits les plus vendus ;
* catégories les plus vendues.

---

# 12. Clients

Le reporting doit respecter le modèle :

```text
Client commercial ≠ utilisateur MAGIC GOLD
```

Indicateurs :

* nombre de clients identifiés ;
* nombre d'acheteurs anonymes ;
* clients avec téléphone ;
* clients avec historique ;
* fréquence d'achat lorsque l'identification le permet.

Ne pas prétendre connaître un client anonyme.

---

# 13. Rapport acheteurs anonymes

Indicateur possible :

```text
Ventes anonymes
```

Exemple :

```text
Ventes totales        1 000
Ventes identifiées      420
Ventes anonymes         580
```

Cet indicateur est statistique et ne doit pas permettre de réidentifier inutilement les personnes.

---

# 14. Caisse

Si le module Caisse est activé :

* sessions ouvertes ;
* sessions fermées ;
* ventes par caisse ;
* ventes par caissier ;
* encaissements ;
* écarts de caisse ;
* annulations ;
* remboursements.

---

# 15. Rapport journalier de caisse

Structure indicative :

```text
Date
Caisse
Caissier
Heure ouverture
Heure fermeture
Fond initial
Ventes espèces
Ventes digitales
Remboursements
Mouvements
Montant attendu
Montant déclaré
Écart
```

---

# 16. Rapport par période

Filtres :

```text
date début
date fin
marchand
boutique
caisse
utilisateur
produit
catégorie
méthode de paiement
fournisseur
statut
```

Le backend doit toujours contrôler les limites du périmètre.

---

# 17. Comparaison temporelle

Le système peut comparer :

```text
Aujourd'hui vs hier
Cette semaine vs semaine précédente
Ce mois vs mois précédent
Période A vs période B
```

Les comparaisons doivent afficher les valeurs brutes avant toute interprétation.

---

# 18. Agrégations

Niveaux :

```text
Jour
Semaine
Mois
Année
```

Les dates doivent respecter le fuseau horaire configuré pour le marchand.

---

# 19. Devise

V1 cible principalement :

```text
XOF
```

Les montants doivent être stockés et agrégés avec une précision exacte.

Ne jamais utiliser `float` ou `double` pour les calculs financiers.

---

# 20. Export

Formats possibles :

```text
CSV
XLSX
PDF
```

Chaque export doit respecter :

* permissions ;
* périmètre marchand ;
* filtres appliqués ;
* période ;
* horodatage ;
* utilisateur ayant demandé l'export.

---

# 21. Audit des exports

Un export sensible doit pouvoir être audité :

```text
actorId
merchantId
reportType
filters
format
requestedAt
resultCount
```

---

# 22. API

Exemples :

```http
GET /api/v1/reports/dashboard
GET /api/v1/reports/sales
GET /api/v1/reports/payments
GET /api/v1/reports/products
GET /api/v1/reports/customers
GET /api/v1/reports/cash
GET /api/v1/reports/refunds
```

Exports :

```http
POST /api/v1/reports/sales/export
POST /api/v1/reports/payments/export
```

---

# 23. Sécurité

Chaque requête suit :

```text
Authentication
    ↓
User active
    ↓
Permission
    ↓
Merchant scope
    ↓
Resource scope
    ↓
Filters
    ↓
Query
```

Un marchand ne peut jamais demander les statistiques d'un autre marchand.

---

# 24. Performance

Les requêtes de reporting lourdes ne doivent pas bloquer les transactions opérationnelles.

Pour les gros volumes :

```text
PostgreSQL
    ↓
indexes
    ↓
aggregation queries
    ↓
materialized views éventuelles
    ↓
reporting
```

Une base analytique séparée pourra être introduite ultérieurement.

---

# 25. Temps réel

Le dashboard peut être quasi temps réel.

Il ne faut toutefois pas sacrifier la cohérence financière pour obtenir une actualisation instantanée.

Priorité :

```text
Exactitude > fraîcheur extrême
```

---

# 26. Données sensibles

Le reporting ne doit exposer que les données nécessaires.

Exemple :

Un caissier peut voir :

```text
ses ventes
sa caisse
ses sessions
```

sans avoir accès aux informations financières globales du marchand si sa permission ne l'autorise pas.

---

# 27. Tests

Tester :

* calcul CA ;
* calcul panier moyen ;
* paiements par méthode ;
* remboursements ;
* ventes anonymes ;
* filtres ;
* périodes ;
* permissions ;
* multi-tenant ;
* exports ;
* fuseaux horaires ;
* gros volumes ;
* données concurrentes.

---

# 28. DoD

Le module Reporting est considéré terminé lorsque :

* les KPI principaux sont définis ;
* les calculs sont documentés ;
* les données proviennent de PostgreSQL ;
* les permissions sont appliquées ;
* l'isolation marchand est garantie ;
* les exports fonctionnent ;
* les rapports financiers sont traçables ;
* les tests automatisés couvrent les calculs critiques.

---

# 29. Règles Hermes

Hermes doit :

1. Ne jamais inventer une métrique.
2. Ne jamais modifier une transaction pour produire un rapport.
3. Ne jamais utiliser Redis comme source financière.
4. Toujours respecter `merchantId`.
5. Toujours appliquer les permissions.
6. Documenter toute définition de KPI.
7. Séparer CA brut, remises, taxes, frais et remboursements.
8. Ne jamais réidentifier artificiellement un acheteur anonyme.
9. Ne jamais présenter une estimation comme une donnée comptable.
10. Préserver la traçabilité des exports.
