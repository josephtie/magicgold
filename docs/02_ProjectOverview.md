# MAGIC GOLD --- Projet SaaS Nectux Ingénieries

**Version : 1.0 --- Septembre 2026**\
**Éditeur : Nectux Ingénieries**\
**Positionnement : SaaS mobile de gestion commerciale, stock, caisse et
paiements digitaux pour les commerces de proximité et commerces de
détail.**

------------------------------------------------------------------------

# 1. Vision du projet

**Magic Gold** est une application SaaS mobile-first destinée aux
commerçants, boutiques, magasins, restaurants, maquis, bars, petites
supérettes et autres commerces de détail.

L'objectif est de permettre au commerçant de gérer son activité
quotidienne depuis son téléphone ou une tablette :

-   vendre ;
-   gérer les produits et les stocks ;
-   suivre les achats et fournisseurs ;
-   gérer la caisse ;
-   accepter les paiements digitaux ;
-   suivre les créances clients ;
-   contrôler les vendeurs ;
-   consulter les résultats de son commerce ;
-   recevoir des alertes ;
-   piloter un ou plusieurs points de vente.

Magic Gold ne cherche pas à concurrencer directement les ERP complexes
des grandes surfaces. Il vise prioritairement les commerces qui
utilisent encore des cahiers, Excel, des applications dispersées ou des
solutions trop complexes.

## Promesse

> **MAGIC GOLD --- Votre commerce dans votre téléphone.**

**Vendez. Gérez votre stock. Encaissez. Suivez votre argent.**

------------------------------------------------------------------------

# 2. Problème à résoudre

Une grande partie des petits et moyens commerces doit gérer
simultanément :

-   ventes ;
-   stock ;
-   achats ;
-   caisse ;
-   espèces ;
-   Mobile Money ;
-   crédits clients ;
-   fournisseurs ;
-   vendeurs ;
-   inventaires ;
-   dépenses ;
-   résultats.

Ces opérations sont souvent réalisées avec plusieurs outils :

``` text
Cahier
   +
Calculatrice
   +
WhatsApp
   +
Excel
   +
Applications de paiement
   +
Mémoire du propriétaire
```

Cela provoque :

-   erreurs de stock ;
-   pertes de produits ;
-   difficultés de rapprochement ;
-   absence de visibilité sur la trésorerie ;
-   difficultés à contrôler les vendeurs ;
-   impossibilité de connaître rapidement le résultat réel ;
-   mélange entre argent personnel et argent du commerce.

Magic Gold doit réunir ces fonctions dans une expérience simple.

------------------------------------------------------------------------

# 3. Cible commerciale

## 3.1 Cœur de cible

Magic Gold cible en priorité :

-   boutiques de quartier ;
-   magasins de détail ;
-   supérettes indépendantes ;
-   magasins alimentaires ;
-   boutiques de vêtements ;
-   boutiques de chaussures ;
-   magasins de cosmétiques ;
-   quincailleries ;
-   magasins d'électroménager ;
-   magasins de pièces détachées ;
-   dépôts ;
-   petits grossistes/distributeurs ;
-   restaurants ;
-   maquis ;
-   bars ;
-   cafés ;
-   fast-food ;
-   pâtisseries ;
-   glaciers.

## 3.2 Cible secondaire

-   commerces avec 2 à 5 points de vente ;
-   commerces avec plusieurs vendeurs ;
-   magasins spécialisés ;
-   petits réseaux de boutiques ;
-   commerces disposant de plusieurs caisses.

## 3.3 Cible non prioritaire au lancement

Ne pas chercher initialement à remplacer :

-   les ERP des grandes surfaces ;
-   les systèmes des hypermarchés ;
-   les systèmes de chaînes nationales ;
-   les SI fortement intégrés ;
-   les solutions nécessitant des architectures retail très complexes.

Magic Gold pourra toutefois évoluer vers des commerces plus importants
via une offre **Magic Gold Pro / Network**.

------------------------------------------------------------------------

# 4. Positionnement

Magic Gold n'est pas simplement :

> un logiciel de caisse.

Ce n'est pas non plus :

> un ERP lourd.

Le positionnement recherché est :

> **Une application mobile de gestion commerciale et d'encaissement pour
> le commerce de proximité.**

Le cœur fonctionnel est :

``` text
PRODUITS
   ↓
STOCK
   ↓
VENTE
   ↓
ENCAISSEMENT
   ↓
PAIEMENT
   ↓
TRÉSORERIE
   ↓
RAPPORTS
```

------------------------------------------------------------------------

# 5. Principe produit : Mobile First

Magic Gold doit être conçu d'abord pour :

-   smartphone Android ;
-   tablette Android ;
-   éventuellement iPhone/iPad selon la stratégie produit.

Le commerçant ne doit pas avoir besoin d'un ordinateur pour les
opérations quotidiennes.

## Principe UX

L'application doit être :

-   simple ;
-   rapide ;
-   lisible ;
-   utilisable par un utilisateur peu technophile ;
-   adaptée aux écrans mobiles ;
-   capable de fonctionner dans des conditions réseau dégradées ;
-   orientée vers les actions quotidiennes.

L'objectif est que le commerçant puisse commencer à vendre après une
courte prise en main.

------------------------------------------------------------------------

# 6. Modules fonctionnels

## 6.1 Tableau de bord

Le tableau de bord affiche notamment :

-   chiffre d'affaires du jour ;
-   nombre de ventes ;
-   montant encaissé ;
-   paiements digitaux ;
-   espèces ;
-   crédits ;
-   dépenses ;
-   marge estimée ;
-   produits en stock faible ;
-   anomalies ;
-   activité des vendeurs.

Exemple :

``` text
MAGIC GOLD

CA aujourd'hui
625 000 FCFA

Ventes
87

Paiements digitaux
410 000 FCFA

Espèces
180 000 FCFA

Crédits
35 000 FCFA

Stock faible
7 produits

Anomalies
2
```

------------------------------------------------------------------------

# 7. Gestion des produits

Fonctions :

-   création produit ;
-   modification ;
-   suppression/archivage ;
-   catégorie ;
-   sous-catégorie ;
-   marque ;
-   référence ;
-   code-barres ;
-   unité ;
-   prix d'achat ;
-   prix de vente ;
-   seuil d'alerte ;
-   stock initial ;
-   fournisseur principal ;
-   photo produit ;
-   variantes ;
-   produits actifs/inactifs.

## Code-barres

Magic Gold doit permettre l'utilisation de la caméra du téléphone pour :

-   scanner un code-barres ;
-   rechercher un produit ;
-   ajouter rapidement un produit à une vente ;
-   effectuer un inventaire.

------------------------------------------------------------------------

# 8. Gestion des stocks

Fonctions principales :

-   stock par produit ;
-   stock par magasin ;
-   entrées ;
-   sorties ;
-   ajustements ;
-   inventaires ;
-   pertes ;
-   casse ;
-   produits expirés ;
-   transferts entre magasins ;
-   seuil minimum ;
-   alertes de rupture ;
-   historique des mouvements.

## Principe

Chaque mouvement doit être traçable.

``` text
Produit
   ↓
Mouvement de stock
   ↓
Utilisateur
   ↓
Date / heure
   ↓
Motif
```

------------------------------------------------------------------------

# 9. Gestion des ventes

La vente doit être extrêmement rapide.

Flux :

``` text
Recherche produit
       ↓
Ajout au panier
       ↓
Quantité
       ↓
Remise éventuelle
       ↓
Total
       ↓
Mode de paiement
       ↓
Confirmation
       ↓
Reçu
       ↓
Mise à jour du stock
```

Modes de paiement :

-   espèces ;
-   paiement digital ;
-   QR ;
-   carte si disponible ;
-   crédit client ;
-   paiement mixte.

------------------------------------------------------------------------

# 10. Paiements digitaux

Le paiement digital est une composante stratégique de Magic Gold.

Il ne doit pas être ajouté comme une fonction secondaire.

## Architecture cible

``` text
                    MAGIC GOLD
                         │
                    PAYMENT CORE
                         │
             ┌───────────┼───────────┐
             │           │           │
          Mobile        QR          API
          Money
             │
      ┌──────┼───────┐
      │      │       │
   Orange   MTN     Moov
   Money    MoMo    Money
      │      │       │
      └──────┼───────┘
             │
       autres rails
       / partenaires
```

L'architecture devra rester agnostique vis-à-vis du fournisseur de
paiement.

Magic Gold doit pouvoir intégrer un ou plusieurs agrégateurs et/ou
opérateurs selon les contrats, APIs et conditions disponibles.

------------------------------------------------------------------------

# 11. Fonctionnement d'un paiement

Exemple :

``` text
TOTAL : 4 000 FCFA

[ ESPÈCES ]
[ PAIEMENT DIGITAL ]
[ QR CODE ]
[ CRÉDIT CLIENT ]
```

Si le client choisit le paiement digital :

``` text
Création transaction
        ↓
Référence Magic Gold
        ↓
Demande de paiement
        ↓
Client paie
        ↓
Confirmation fournisseur
        ↓
Webhook
        ↓
Vérification transaction
        ↓
Paiement CONFIRMÉ
        ↓
Vente finalisée
```

La vente ne doit pas être considérée comme payée uniquement parce que le
client a quitté l'écran de paiement.

La confirmation doit être vérifiée côté serveur.

------------------------------------------------------------------------

# 12. Réconciliation financière

La réconciliation constitue une fonction stratégique.

Exemple :

``` text
VENTES DU JOUR

Espèces             180 000
Orange Money        245 000
MTN MoMo             95 000
Autres               70 000
Crédit client        35 000
--------------------------------
TOTAL               625 000
```

Magic Gold doit pouvoir rapprocher :

-   ventes ;
-   paiements ;
-   espèces ;
-   transactions digitales ;
-   remboursements ;
-   crédits ;
-   commissions éventuelles ;
-   écarts de caisse.

## Objectif

Le propriétaire doit pouvoir répondre rapidement à :

> Combien ai-je vendu ?

> Combien ai-je réellement encaissé ?

> Combien ai-je reçu en digital ?

> Combien dois-je retrouver dans ma caisse ?

> Y a-t-il un écart ?

------------------------------------------------------------------------

# 13. Paiement mixte

Magic Gold doit prévoir les paiements mixtes.

Exemple :

``` text
Total : 25 000 FCFA

Espèces       10 000
Mobile Money  15 000
--------------------
Total         25 000
```

Chaque composante doit être enregistrée séparément.

------------------------------------------------------------------------

# 14. Gestion de la caisse

Fonctions :

-   ouverture de caisse ;
-   fonds initial ;
-   ventes ;
-   dépenses ;
-   retraits ;
-   dépôts ;
-   clôture ;
-   comptage ;
-   écart ;
-   historique.

## Clôture

À la fin de la journée :

``` text
CA théorique       625 000
Espèces attendues  180 000
Espèces comptées   175 000

Écart               -5 000
```

Une anomalie doit être enregistrée et éventuellement soumise à
validation.

------------------------------------------------------------------------

# 15. Gestion des clients

Fonctions :

-   fiche client ;
-   téléphone ;
-   historique des achats ;
-   crédits ;
-   paiements ;
-   solde ;
-   limites éventuelles ;
-   reçus ;
-   fidélité dans une version future.

------------------------------------------------------------------------

# 16. Crédit client

Le crédit doit être contrôlé.

Fonctions :

-   vente à crédit ;
-   plafond ;
-   échéance ;
-   historique ;
-   paiement partiel ;
-   remboursement ;
-   solde ;
-   rappels.

Exemple :

``` text
Client : KOFFI Jean

Dette initiale     75 000
Paiement           25 000
--------------------------
Solde              50 000 FCFA
```

------------------------------------------------------------------------

# 17. Gestion des fournisseurs

Fonctions :

-   fournisseurs ;
-   contacts ;
-   achats ;
-   commandes ;
-   dettes fournisseurs ;
-   paiements ;
-   historique ;
-   produits fournis.

------------------------------------------------------------------------

# 18. Gestion des achats

Flux :

``` text
Commande
   ↓
Réception
   ↓
Contrôle
   ↓
Entrée en stock
   ↓
Dette fournisseur
   ↓
Paiement
```

------------------------------------------------------------------------

# 19. Dépenses

Le commerçant doit pouvoir enregistrer :

-   loyer ;
-   électricité ;
-   eau ;
-   transport ;
-   salaires ;
-   achats divers ;
-   maintenance ;
-   petites dépenses ;
-   autres charges.

Les dépenses doivent être séparées des achats de marchandises.

------------------------------------------------------------------------

# 20. Utilisateurs et vendeurs

Profils possibles :

### Propriétaire / Administrateur

Accès complet.

### Gérant

Gestion du magasin et supervision.

### Caissier

Ventes et caisse.

### Vendeur

Ventes selon les autorisations.

### Gestionnaire de stock

Stock, inventaires et mouvements.

Les droits doivent être configurables.

------------------------------------------------------------------------

# 21. Contrôle et traçabilité

Magic Gold doit enregistrer les opérations sensibles :

-   annulation ;
-   suppression ;
-   modification de prix ;
-   remise ;
-   remboursement ;
-   correction de stock ;
-   ouverture de caisse ;
-   clôture ;
-   paiement ;
-   changement d'utilisateur.

Chaque événement doit contenir :

``` text
Utilisateur
Action
Date
Heure
Objet
Ancienne valeur
Nouvelle valeur
```

------------------------------------------------------------------------

# 22. Gestion multi-magasins

Pour les offres supérieures :

``` text
ENTREPRISE
   │
   ├── Magasin A
   │     ├── Caisse 1
   │     └── Caisse 2
   │
   ├── Magasin B
   │     └── Caisse 1
   │
   └── Magasin C
         └── Caisse 1
```

Le propriétaire doit pouvoir :

-   voir chaque magasin ;
-   voir le total ;
-   comparer les magasins ;
-   transférer du stock ;
-   gérer les utilisateurs ;
-   contrôler les caisses.

------------------------------------------------------------------------

# 23. Mode hors ligne

Le mode offline est important pour le marché cible.

Magic Gold doit pouvoir continuer à réaliser certaines opérations
lorsque la connexion est temporairement indisponible :

-   consultation du catalogue local ;
-   ventes ;
-   mouvements locaux ;
-   caisse ;
-   reçus locaux.

Les opérations nécessitant une confirmation externe de paiement devront
respecter un mécanisme spécifique.

## Synchronisation

``` text
Téléphone
   ↓
Base locale
   ↓
File de synchronisation
   ↓
Connexion disponible
   ↓
API Magic Gold
   ↓
Serveur
```

Le système doit gérer les conflits et éviter les doublons.

------------------------------------------------------------------------

# 24. Reçus

Après une vente :

-   reçu numérique ;
-   impression thermique optionnelle ;
-   partage WhatsApp ;
-   SMS selon intégration ;
-   PDF dans une version appropriée.

Exemple :

``` text
MAGIC GOLD
Chez Koffi

Vente #MG-20260922-002845

Produit             Montant
--------------------------------
Produit A             5 000
Produit B             2 500
--------------------------------
TOTAL                 7 500

Paiement : Mobile Money

Merci pour votre achat.
```

------------------------------------------------------------------------

# 25. Rapports

Rapports minimum :

-   CA journalier ;
-   CA hebdomadaire ;
-   CA mensuel ;
-   ventes par produit ;
-   ventes par catégorie ;
-   ventes par vendeur ;
-   ventes par magasin ;
-   stock ;
-   rotation des stocks ;
-   achats ;
-   dépenses ;
-   créances ;
-   dettes fournisseurs ;
-   caisse ;
-   paiements digitaux ;
-   marges estimées.

------------------------------------------------------------------------

# 26. Alertes

Magic Gold doit proposer des alertes utiles :

### Stock

> Coca-Cola : stock faible.

### Crédit

> Client KOFFI : échéance dépassée.

### Caisse

> Écart de caisse détecté.

### Paiement

> Paiement digital en attente.

### Activité

> Aucun encaissement depuis 3 heures.

### Stock

> Produit X bientôt en rupture.

------------------------------------------------------------------------

# 27. Mode métier

Le moteur Magic Gold doit rester commun.

Mais l'interface peut adapter certaines fonctions au secteur.

## Magic Gold Boutique

-   produits ;
-   stock ;
-   ventes ;
-   caisse ;
-   clients.

## Magic Gold Restaurant

-   tables ;
-   commandes ;
-   cuisine ;
-   additions ;
-   produits/ingrédients ;
-   caisse.

## Magic Gold Maquis

-   tables ;
-   commandes ;
-   boissons ;
-   cuisine ;
-   addition ;
-   paiement.

## Magic Gold Bar

-   produits ;
-   bouteilles ;
-   stock ;
-   ventes ;
-   caisse.

## Magic Gold Magasin

-   catalogue ;
-   stock ;
-   fournisseurs ;
-   ventes ;
-   inventaire.

------------------------------------------------------------------------

# 28. Offres SaaS

Les tarifs définitifs devront être validés après étude terrain.

Proposition initiale à tester :

## MAGIC GOLD START

**5 000 FCFA / mois**

Pour petite boutique.

Fonctions :

-   produits ;
-   ventes ;
-   stock ;
-   caisse ;
-   rapports simples ;
-   paiement digital.

------------------------------------------------------------------------

## MAGIC GOLD BUSINESS

**10 000 FCFA / mois**

Pour commerce structuré.

Ajouts :

-   clients ;
-   crédit ;
-   fournisseurs ;
-   achats ;
-   dépenses ;
-   utilisateurs ;
-   rapports avancés.

------------------------------------------------------------------------

## MAGIC GOLD PRO

**25 000 FCFA / mois**

Pour commerce plus important.

Ajouts :

-   multi-caisses ;
-   multi-utilisateurs ;
-   multi-magasins ;
-   transferts ;
-   supervision ;
-   tableaux de bord avancés.

------------------------------------------------------------------------

## MAGIC GOLD NETWORK

**À partir de 50 000 FCFA / mois**

Pour réseaux de magasins.

Ajouts :

-   centralisation ;
-   administration multi-sites ;
-   stock central ;
-   reporting consolidé ;
-   API ;
-   intégrations spécifiques.

Les prix sont des hypothèses de travail et doivent être validés par des
tests commerciaux.

------------------------------------------------------------------------

# 29. Revenus complémentaires

Le modèle économique peut combiner :

``` text
Abonnement SaaS
+
Services d'installation
+
Formation
+
Matériel éventuel
+
Services premium
+
Intégrations
+
Services de paiement selon le modèle contractuel
```

Nectux doit distinguer clairement :

-   revenus SaaS ;
-   frais de service ;
-   commissions éventuellement reversées/partagées ;
-   coûts des prestataires de paiement.

------------------------------------------------------------------------

# 30. Stratégie commerciale

## Premier marché

**Abidjan.**

Approche terrain :

1.  identifier les commerces ;
2.  proposer une démonstration ;
3.  installer Magic Gold ;
4.  importer les produits ;
5.  former le propriétaire ;
6.  accompagner pendant les premiers jours ;
7.  convertir en abonnement.

## Argument commercial

Ne pas commencer par une longue présentation technique.

Commencer par :

> « Combien faites-vous de ventes par jour ? »

Puis :

> « Comment savez-vous exactement combien vous avez vendu aujourd'hui ?
> »

Puis :

> « Comment savez-vous combien il vous reste en stock ? »

Puis :

> « Vos paiements Mobile Money sont-ils rapprochés automatiquement avec
> vos ventes ? »

Magic Gold doit répondre à ces problèmes.

------------------------------------------------------------------------

# 31. Acquisition terrain

Canaux possibles :

-   commerciaux terrain ;
-   démonstrations ;
-   partenariats ;
-   grossistes ;
-   distributeurs ;
-   comptables ;
-   fournisseurs de matériel de caisse ;
-   intégrateurs ;
-   associations de commerçants ;
-   réseaux professionnels ;
-   campagnes digitales.

------------------------------------------------------------------------

# 32. Démonstration commerciale

La démonstration idéale doit durer environ 10 minutes.

## Scénario

``` text
1. Créer un produit
2. Ajouter du stock
3. Faire une vente
4. Choisir Mobile Money
5. Confirmer le paiement
6. Montrer le reçu
7. Montrer le stock restant
8. Montrer le CA
9. Montrer la caisse
10. Montrer le rapprochement
```

Le prospect doit voir immédiatement la valeur.

------------------------------------------------------------------------

# 33. Architecture technique cible

Architecture SaaS multi-tenant.

``` text
                    MAGIC GOLD
                       MOBILE
                         │
                    API Gateway
                         │
                 Backend Application
                         │
        ┌────────────────┼────────────────┐
        │                │                │
     Commerce          Stock           Payment
        │                │                │
        └────────────────┼────────────────┘
                         │
                    PostgreSQL
                         │
                ┌────────┴────────┐
                │                 │
             Redis          Object Storage
                │
         Jobs / Notifications
```

------------------------------------------------------------------------

# 34. Multi-tenancy

Chaque commerce est un tenant.

Concept :

``` text
Tenant
   │
   ├── Users
   ├── Stores
   ├── Products
   ├── Stocks
   ├── Sales
   ├── Payments
   ├── Customers
   ├── Suppliers
   └── Reports
```

Toutes les données métier doivent être isolées par tenant.

------------------------------------------------------------------------

# 35. Sécurité

Exigences :

-   authentification sécurisée ;
-   gestion des rôles ;
-   contrôle d'accès ;
-   chiffrement TLS ;
-   secrets hors du code ;
-   journaux d'audit ;
-   sauvegardes ;
-   protection des APIs ;
-   idempotence des paiements ;
-   protection contre les doubles transactions ;
-   validation serveur des webhooks ;
-   limitation de débit ;
-   surveillance.

------------------------------------------------------------------------

# 36. Paiements : règles critiques

Un paiement doit posséder un identifiant unique.

Exemple :

``` text
MG-PAY-20260922-000001
```

États :

``` text
CREATED
PENDING
SUCCESS
FAILED
CANCELLED
REFUNDED
```

Une même transaction ne doit jamais être comptabilisée deux fois.

Les webhooks doivent être idempotents.

------------------------------------------------------------------------

# 37. Données principales

Entités minimales :

``` text
Tenant
User
Role
Store
Register
Product
Category
Stock
StockMovement
Supplier
Purchase
PurchaseItem
Customer
Sale
SaleItem
Payment
PaymentMethod
CashSession
CashMovement
Expense
Credit
CreditPayment
Inventory
InventoryItem
Transfer
AuditLog
Notification
```

------------------------------------------------------------------------

# 38. API principales

Exemples :

``` text
POST   /api/auth/login

GET    /api/products
POST   /api/products
PUT    /api/products/{id}

GET    /api/stocks
POST   /api/stocks/adjustments

POST   /api/sales
GET    /api/sales/{id}

POST   /api/payments
GET    /api/payments/{id}

POST   /api/payments/webhook/{provider}

POST   /api/cash/open
POST   /api/cash/close

GET    /api/dashboard

GET    /api/reports/sales
GET    /api/reports/stock
GET    /api/reports/payments
```

------------------------------------------------------------------------

# 39. Notifications

Canaux possibles :

-   notification push ;
-   SMS ;
-   WhatsApp selon disponibilité/intégration ;
-   email.

Cas :

-   paiement confirmé ;
-   paiement échoué ;
-   stock faible ;
-   crédit arrivé à échéance ;
-   clôture de caisse ;
-   anomalie ;
-   rapport quotidien.

------------------------------------------------------------------------

# 40. Administration SaaS Nectux

Nectux doit disposer d'un back-office permettant :

-   gestion des tenants ;
-   abonnements ;
-   plans ;
-   utilisateurs ;
-   paiements SaaS ;
-   activation/désactivation ;
-   statistiques ;
-   support ;
-   logs ;
-   supervision ;
-   facturation ;
-   gestion des intégrations.

------------------------------------------------------------------------

# 41. Indicateurs SaaS

Le back-office doit suivre :

-   nombre de commerces inscrits ;
-   commerces actifs ;
-   utilisateurs actifs ;
-   ventes ;
-   volume de transactions ;
-   volume de paiements ;
-   MRR ;
-   ARR ;
-   churn ;
-   conversion essai → abonnement ;
-   coût d'acquisition ;
-   revenu moyen par client ;
-   utilisation par module.

------------------------------------------------------------------------

# 42. MVP

Le MVP doit rester volontairement limité.

## MVP V1

### Authentification

-   inscription ;
-   connexion ;
-   tenant ;
-   utilisateur ;
-   rôle.

### Produits

-   catalogue ;
-   catégories ;
-   prix ;
-   stock.

### Ventes

-   panier ;
-   vente ;
-   reçu ;
-   historique.

### Caisse

-   ouverture ;
-   mouvements ;
-   clôture.

### Paiements

-   espèces ;
-   un premier moyen de paiement digital ;
-   statut paiement ;
-   confirmation ;
-   historique.

### Dashboard

-   CA ;
-   ventes ;
-   caisse ;
-   stock faible.

### Offline

-   catalogue local ;
-   ventes locales ;
-   synchronisation.

------------------------------------------------------------------------

# 43. V2

-   multi-utilisateurs ;
-   fournisseurs ;
-   achats ;
-   inventaire ;
-   crédits clients ;
-   dépenses ;
-   multi-caisses ;
-   QR ;
-   deuxième/troisième moyen de paiement ;
-   rapports avancés.

------------------------------------------------------------------------

# 44. V3

-   multi-magasins ;
-   transferts ;
-   fidélité ;
-   promotions ;
-   commandes ;
-   statistiques avancées ;
-   API ;
-   intégrations ;
-   comptabilité simplifiée.

------------------------------------------------------------------------

# 45. V4

-   Magic Gold Restaurant ;
-   Magic Gold Maquis ;
-   Magic Gold Bar ;
-   gestion avancée des tables ;
-   cuisine ;
-   commandes ;
-   impression cuisine ;
-   gestion des menus.

------------------------------------------------------------------------

# 46. Roadmap indicative

``` text
PHASE 1
Conception + UX
        ↓
PHASE 2
MVP Commerce
        ↓
PHASE 3
Paiement digital
        ↓
PHASE 4
Pilote terrain Abidjan
        ↓
PHASE 5
Corrections / simplification
        ↓
PHASE 6
Commercialisation
        ↓
PHASE 7
Multi-magasins
        ↓
PHASE 8
Restaurant / Maquis
        ↓
PHASE 9
Expansion Côte d'Ivoire
        ↓
PHASE 10
Expansion UEMOA
```

------------------------------------------------------------------------

# 47. Critères de réussite du MVP

Le MVP ne doit pas être considéré comme réussi uniquement parce qu'il
fonctionne techniquement.

Il doit démontrer que :

-   un commerçant comprend l'application ;
-   il peut enregistrer ses produits ;
-   il peut vendre rapidement ;
-   il peut encaisser ;
-   le stock est automatiquement mis à jour ;
-   le paiement est correctement rapproché ;
-   il comprend son tableau de bord ;
-   il utilise réellement le produit après 30 jours ;
-   il accepte de payer l'abonnement.

------------------------------------------------------------------------

# 48. Principes UX non négociables

1.  Peu d'écrans.
2.  Actions rapides.
3.  Gros boutons pour les opérations de caisse.
4.  Recherche produit immédiate.
5.  Scan code-barres.
6.  Fonctionnement réseau dégradé.
7.  Messages d'erreur compréhensibles.
8.  Confirmation claire des paiements.
9.  Historique accessible.
10. Aucune complexité comptable inutile dans le MVP.

------------------------------------------------------------------------

# 49. Différenciation recherchée

Magic Gold ne doit pas chercher à gagner uniquement par le nombre de
fonctionnalités.

Différenciateurs recherchés :

### 1. Mobile-first

Le téléphone est le terminal principal.

### 2. Simplicité

Le commerçant peut commencer rapidement.

### 3. Paiement intégré

Vente et paiement sont liés.

### 4. Réconciliation

Le commerçant rapproche ventes et encaissements.

### 5. Offline

L'activité continue malgré les problèmes de connexion.

### 6. Commerce local

Le produit est conçu pour les réalités du commerce ivoirien et africain.

### 7. Multi-commerce

Le même noyau peut servir boutique, restaurant, maquis, bar et magasin.

------------------------------------------------------------------------

# 50. Relation avec les autres produits Nectux

Magic Gold doit pouvoir devenir une brique du portefeuille SaaS Nectux.

``` text
                     NECTUX SAAS
                          │
       ┌──────────────────┼──────────────────┐
       │                  │                  │
  MAGIC GOLD          ÉMERAUDE              GDO
  Commerce             Église          Organisation
       │                  │                  │
       └──────────────────┼──────────────────┘
                          │
                      NECTUX PAY
                          │
                     Paiements
```

À terme, les services communs peuvent inclure :

-   authentification ;
-   notifications ;
-   paiement ;
-   facturation SaaS ;
-   SMS ;
-   reporting ;
-   stockage documentaire ;
-   API.

------------------------------------------------------------------------

# 51. Vision long terme

Magic Gold doit évoluer d'une application de caisse vers une plateforme
de gestion du commerce de proximité.

Vision :

``` text
              MAGIC GOLD
                   │
      ┌────────────┼────────────┐
      │            │            │
   Commerce      Paiement     Données
      │            │            │
      ├────────────┼────────────┤
      │            │            │
    Stock        Wallet       Analytics
    Vente          QR          BI
    Caisse        API        Prévisions
    Clients       Mobile     Alertes
```

À terme, Magic Gold pourrait permettre au commerçant de comprendre :

> ce qu'il vend ;

> ce qu'il possède ;

> ce qu'il doit ;

> ce qu'on lui doit ;

> ce qu'il encaisse ;

> ce qui fonctionne ;

> et où il perd de l'argent.

------------------------------------------------------------------------

# 52. Vision produit finale

La vision n'est donc pas :

> **« créer une petite application de caisse ».**

La vision est :

> **Créer le système d'exploitation commercial du petit et moyen
> commerce africain.**

Magic Gold doit devenir le point central entre :

``` text
COMMERÇANT
     │
     ├── Produits
     ├── Stock
     ├── Clients
     ├── Fournisseurs
     ├── Vendeurs
     ├── Ventes
     ├── Caisse
     ├── Paiements
     └── Trésorerie
```

avec une expérience :

**mobile + simple + rapide + accessible + connectée aux paiements
digitaux.**

------------------------------------------------------------------------

# 53. Décisions produit actuelles

  Sujet              Décision
  ------------------ ------------------------------------
  Positionnement     SaaS de gestion commerciale mobile
  Cible principale   Commerce de proximité / détail
  Grande surface     Non prioritaire
  Mobile             Priorité absolue
  Stock              Cœur du produit
  Vente              Cœur du produit
  Caisse             Cœur du produit
  Paiement digital   Brique stratégique
  Réconciliation     Fonction différenciante
  Offline            Prioritaire
  Multi-magasin      Version Pro
  Restaurant         Extension
  Maquis             Extension
  Bar                Extension
  ERP lourd          Hors périmètre initial
  SaaS               Modèle principal
  Marché initial     Côte d'Ivoire
  Expansion          UEMOA / Afrique francophone

------------------------------------------------------------------------

# 54. Principe directeur

> **Magic Gold doit être assez simple pour qu'un petit commerçant puisse
> l'utiliser immédiatement, mais assez solide pour accompagner son
> commerce lorsqu'il grandit.**

**Nectux Ingénieries**

*Magic Gold --- Votre commerce dans votre téléphone.*
