# MAGIC GOLD — Project Vision

**Document:** `01_ProjectVision.md`  
**Version:** 1.0  
**Statut:** Draft de référence produit  
**Éditeur:** Nectux Ingénieries  
**Produit:** Magic Gold  
**Marché initial:** Côte d'Ivoire  
**Modèle:** SaaS Mobile-First

---

## 1. Vision

**Magic Gold** est une plateforme SaaS mobile de gestion commerciale destinée prioritairement aux commerces de proximité et aux commerces de détail.

Magic Gold permet au commerçant de gérer, depuis un smartphone ou une tablette :

- produits ;
- stocks ;
- ventes ;
- caisse ;
- clients ;
- fournisseurs ;
- dépenses ;
- paiements digitaux ;
- crédits clients ;
- trésorerie ;
- rapports et indicateurs.

> **MAGIC GOLD — Votre commerce dans votre téléphone.**

La vision de Magic Gold est de devenir progressivement un **système de gestion commercial simple, accessible et connecté aux paiements digitaux pour les petits et moyens commerces africains**.

---

# 2. Le problème

De nombreux commerces fonctionnent encore avec une combinaison d'outils peu intégrés :

```text
Cahier
   +
Calculatrice
   +
Excel
   +
WhatsApp
   +
Applications Mobile Money
   +
Mémoire du propriétaire
```

Cela peut provoquer :

- erreurs de stock ;
- écarts de caisse ;
- pertes de produits ;
- difficulté à suivre les ventes ;
- mauvaise visibilité sur la trésorerie ;
- difficultés de rapprochement des paiements digitaux ;
- difficulté à contrôler les vendeurs ;
- dépendance au propriétaire pour connaître la situation réelle.

Magic Gold doit transformer cette situation en une expérience simple :

```text
PRODUIT
   ↓
VENTE
   ↓
PAIEMENT
   ↓
CAISSE
   ↓
STOCK
   ↓
TRÉSORERIE
   ↓
RAPPORT
```

---

# 3. Opportunité

Le commerce de détail a besoin de solutions :

- simples à déployer ;
- accessibles financièrement ;
- utilisables sur mobile ;
- adaptées aux réalités locales ;
- capables de fonctionner avec une connectivité variable ;
- compatibles avec les paiements digitaux ;
- suffisamment puissantes pour accompagner la croissance.

Magic Gold doit répondre à cette opportunité sans chercher à reproduire la complexité des ERP destinés aux grandes surfaces.

---

# 4. Positionnement

Magic Gold est :

> **une application mobile de gestion commerciale, de caisse et d'encaissement digital pour le commerce de proximité.**

Magic Gold n'est pas conçu initialement comme :

- un ERP de grande surface ;
- un système lourd de gestion industrielle ;
- un logiciel comptable complet ;
- un système bancaire ;
- une plateforme de paiement autonome.

Le produit reste centré sur le quotidien du commerçant.

---

# 5. Cible principale

## 5.1 Commerce de proximité

- boutiques de quartier ;
- magasins de détail ;
- supérettes indépendantes ;
- magasins alimentaires ;
- boutiques de vêtements ;
- boutiques de chaussures ;
- magasins de cosmétiques ;
- quincailleries ;
- magasins d'électroménager ;
- magasins de pièces détachées ;
- dépôts ;
- petits distributeurs ;
- petits grossistes.

## 5.2 Restauration et commerce assimilé

- restaurants ;
- maquis ;
- bars ;
- cafés ;
- fast-food ;
- pâtisseries ;
- glaciers.

Ces secteurs pourront utiliser des modes métier spécifiques construits sur le même noyau commercial.

---

# 6. Segment secondaire

Magic Gold doit pouvoir accompagner :

- les commerces avec plusieurs vendeurs ;
- plusieurs caisses ;
- 2 à 5 points de vente ;
- petits réseaux de magasins ;
- entrepreneurs qui commencent avec une boutique puis développent plusieurs établissements.

Le produit doit être **simple au démarrage et extensible lorsque le commerce grandit**.

---

# 7. Segment non prioritaire au lancement

Les grandes surfaces et chaînes de distribution ne constituent pas la cible initiale.

Elles peuvent disposer d'environnements plus complexes : nombreuses caisses, entrepôts, achats centralisés, logistique, comptabilité, fidélité et intégrations multiples.

Magic Gold ne cherche pas initialement à reproduire cette complexité.

La priorité est de résoudre très bien le problème du **commerce de proximité et du commerce de détail indépendant**.

---

# 8. Utilisateur cible

L'utilisateur principal est le propriétaire ou responsable d'un commerce.

Il doit pouvoir répondre rapidement à :

> Combien ai-je vendu aujourd'hui ?

> Combien ai-je encaissé ?

> Combien ai-je reçu en paiement digital ?

> Combien ai-je en caisse ?

> Quels produits me restent-ils ?

> Quels produits vont bientôt manquer ?

> Quels clients me doivent de l'argent ?

> Quel vendeur a réalisé quelles ventes ?

> Quel est le résultat de mon activité ?

---

# 9. Promesse produit

La promesse de Magic Gold repose sur six idées :

1. **Vendre** — enregistrer rapidement les ventes.
2. **Gérer** — connaître produits, stocks, achats et clients.
3. **Encaisser** — accepter plusieurs moyens de paiement.
4. **Contrôler** — suivre caisses, vendeurs, stocks et anomalies.
5. **Comprendre** — consulter les indicateurs du commerce.
6. **Grandir** — passer d'une boutique à plusieurs points de vente.

---

# 10. Paiement digital

Le paiement digital est une composante stratégique de Magic Gold.

Le flux cible est :

```text
VENTE
  ↓
CHOIX DU MODE DE PAIEMENT
  ↓
PAIEMENT
  ↓
CONFIRMATION
  ↓
VENTE FINALISÉE
  ↓
MISE À JOUR STOCK
  ↓
MISE À JOUR CAISSE
  ↓
RÉCONCILIATION
```

Magic Gold doit pouvoir intégrer progressivement différents moyens de paiement via des fournisseurs ou partenaires appropriés.

L'architecture doit rester indépendante d'un fournisseur particulier.

---

# 11. Vision du paiement

Selon les intégrations disponibles :

- espèces ;
- Mobile Money ;
- QR ;
- carte ;
- paiement par lien ;
- paiement mixte ;
- crédit client.

Le système doit distinguer :

```text
Vente
Paiement
Confirmation
Remboursement
Réconciliation
```

Une vente ne doit pas être considérée comme payée simplement parce qu'une demande de paiement a été initiée.

---

# 12. Réconciliation

Magic Gold doit permettre de rapprocher :

```text
Ventes
   +
Espèces
   +
Paiements digitaux
   +
Crédits
   +
Remboursements
   =
Situation financière attendue
```

Exemple :

```text
CA du jour                    625 000 FCFA

Espèces                       180 000 FCFA
Paiements digitaux            410 000 FCFA
Ventes à crédit                35 000 FCFA
```

Le système doit détecter les écarts entre ventes, paiements confirmés, caisse physique, crédits et remboursements.

---

# 13. Mobile-First

Magic Gold est conçu avec une approche **Mobile-First**.

Le téléphone doit couvrir les principales opérations quotidiennes :

```text
Accueil
  ↓
Vendre
  ↓
Encaisser
  ↓
Consulter le stock
  ↓
Consulter la caisse
  ↓
Consulter les résultats
```

Principes UX :

- peu d'étapes ;
- boutons suffisamment grands ;
- recherche rapide ;
- scan code-barres ;
- informations essentielles ;
- navigation simple ;
- feedback immédiat.

---

# 14. Offline-First pour les opérations appropriées

Les commerces ciblés peuvent rencontrer des problèmes de connectivité.

Magic Gold doit prévoir un fonctionnement local pour les opérations pouvant être exécutées hors connexion.

```text
Application mobile
       ↓
Base locale
       ↓
File de synchronisation
       ↓
Connexion disponible
       ↓
Serveur Magic Gold
       ↓
Synchronisation
```

Les opérations de paiement nécessitant une confirmation externe devront respecter des règles spécifiques.

---

# 15. Simplicité avant complexité

> **Magic Gold doit résoudre les problèmes essentiels avant d'ajouter des fonctionnalités avancées.**

Toute nouvelle fonctionnalité doit apporter une valeur réelle, réduire une tâche manuelle ou une erreur, améliorer le contrôle, faciliter l'encaissement, améliorer la visibilité ou accompagner la croissance du commerce.

---

# 16. Architecture métier cible

```text
                    COMMERCE
                       │
       ┌───────────────┼────────────────┐
       │               │                │
    PRODUITS         CLIENTS         FOURNISSEURS
       │               │                │
       ↓               ↓                ↓
     STOCK           CRÉDITS          ACHATS
       │               │                │
       └───────────────┼────────────────┘
                       ↓
                      VENTE
                       ↓
                   PAIEMENT
                       ↓
              ┌────────┴────────┐
              ↓                 ↓
           CAISSE          DIGITAL
              │                 │
              └────────┬────────┘
                       ↓
                 RÉCONCILIATION
                       ↓
                    RAPPORTS
```

---

# 17. Vision multi-commerce

Le même moteur doit pouvoir s'adapter à plusieurs métiers :

```text
                 MAGIC GOLD CORE
                        │
       ┌────────────────┼────────────────┐
       │                │                │
    Boutique         Restaurant        Maquis
       │                │                │
    Magasin             Bar          Fast-food
```

Le cœur commun reste :

- produits ;
- ventes ;
- stock ;
- caisse ;
- paiements ;
- clients ;
- fournisseurs ;
- reporting.

Les différences métier doivent être implémentées sous forme de modules ou modes spécialisés.

---

# 18. Vision multi-magasins

```text
Entreprise
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

Le propriétaire doit pouvoir consulter le total, chaque magasin, chaque caisse, les stocks, les ventes, les paiements, les utilisateurs et les écarts.

---

# 19. Vision SaaS

Magic Gold est conçu nativement comme un SaaS multi-tenant.

```text
Magic Gold SaaS
      │
      ├── Tenant A
      │     ├── Users
      │     ├── Stores
      │     ├── Products
      │     ├── Sales
      │     └── Payments
      │
      ├── Tenant B
      │     └── ...
      │
      └── Tenant C
            └── ...
```

Le modèle SaaS doit permettre :

- abonnement mensuel ;
- activation rapide ;
- évolution de formule ;
- gestion des utilisateurs ;
- supervision ;
- support ;
- facturation ;
- évolutivité.

---

# 20. Modèle économique

Le modèle recherché est :

> **Abonnement SaaS + services complémentaires + écosystème de paiement.**

Segmentation initiale à tester :

```text
START
Petite boutique

BUSINESS
Commerce structuré

PRO
Commerce multi-utilisateurs / multi-caisses

NETWORK
Commerce multi-sites
```

Les prix ne sont pas figés dans ce document.

---

# 21. Distribution

Canaux possibles :

### Terrain
- commerciaux ;
- démonstrations ;
- installation ;
- formation ;
- accompagnement.

### Digital
- site web ;
- démonstration en ligne ;
- réseaux sociaux ;
- contenu ;
- référencement ;
- acquisition digitale.

### Partenariats
- fournisseurs de matériel ;
- grossistes ;
- distributeurs ;
- comptables ;
- associations professionnelles ;
- partenaires de paiement ;
- intégrateurs.

---

# 22. Stratégie de lancement

Marché initial : **Côte d'Ivoire**.

Trajectoire de validation :

```text
MVP
 ↓
Pilote terrain
 ↓
10 premiers commerces
 ↓
Analyse utilisation
 ↓
Corrections
 ↓
50 commerces
 ↓
100 commerces
 ↓
Industrialisation commerciale
 ↓
1 000+ commerces
```

Ces chiffres constituent une trajectoire de validation et non une prévision garantie.

---

# 23. Critères de succès produit

Le produit doit démontrer que :

1. le commerçant comprend rapidement l'application ;
2. il peut créer ou importer ses produits ;
3. il peut réaliser une vente rapidement ;
4. le stock est correctement mis à jour ;
5. le paiement est correctement enregistré ;
6. la caisse reflète les opérations ;
7. les paiements digitaux peuvent être rapprochés ;
8. le propriétaire peut suivre son activité à distance ;
9. l'application reste utile après la période d'essai ;
10. le commerçant accepte de payer pour continuer à l'utiliser.

---

# 24. Indicateurs stratégiques

- commerces inscrits ;
- commerces actifs ;
- utilisateurs actifs ;
- ventes enregistrées ;
- volume de transactions ;
- volume des paiements digitaux ;
- valeur totale des transactions ;
- fréquence d'utilisation ;
- conversion essai → abonnement ;
- rétention ;
- churn ;
- MRR ;
- revenu moyen par commerce ;
- coût d'acquisition ;
- utilisation des fonctionnalités.

---

# 25. Différenciation recherchée

### Simplicité
Compréhensible par un commerçant non spécialiste.

### Mobile
Le téléphone est l'outil principal.

### Paiement
Le paiement est directement relié à la vente.

### Réconciliation
Le commerçant peut rapprocher ventes, paiements et caisse.

### Offline
Le produit tient compte des contraintes de connectivité.

### Localisation
Le produit est pensé pour les réalités des commerces ivoiriens et africains.

### Évolutivité
Un commerce peut commencer petit puis évoluer vers plusieurs utilisateurs et points de vente.

---

# 26. Principes non négociables

## P1 — Simplicité
Une fonctionnalité complexe doit être simplifiée avant d'être exposée à l'utilisateur.

## P2 — Intégrité financière
Toute opération financière doit être traçable et cohérente.

## P3 — Traçabilité
Les opérations sensibles doivent être auditables.

## P4 — Isolation
Les données d'un commerce ne doivent jamais être accessibles à un autre commerce.

## P5 — Résilience
Une panne réseau temporaire ne doit pas provoquer une perte d'opérations locales autorisées.

## P6 — Idempotence
Les paiements et synchronisations doivent être conçus pour éviter les doublons.

## P7 — Évolutivité
L'architecture doit permettre l'évolution du produit sans réécriture complète.

## P8 — Source of Truth
Les règles métier documentées constituent la référence du comportement attendu du système.

---

# 27. Hors périmètre initial

Ne sont pas prioritaires pour le MVP :

- ERP complet de grande surface ;
- WMS avancé ;
- comptabilité générale complète ;
- paie ;
- gestion industrielle ;
- logistique complexe ;
- marketplace ;
- crédit financier ;
- services bancaires ;
- fonctionnalités nécessitant des agréments réglementaires spécifiques sans partenaire approprié.

Ces sujets pourront faire l'objet d'évolutions ou d'intégrations ultérieures.

---

# 28. Relation avec l'écosystème Nectux

```text
                       NECTUX
                         │
                    NECTUX HUB
                         │
        ┌────────────────┼────────────────┐
        │                │                │
     Identity         Payment         Notification
        │                │                │
        ├────────────────┼────────────────┤
        │                │                │
  MAGIC GOLD         ÉMERAUDE            GDO
  Commerce            Église        Organisation
```

Les services mutualisables peuvent inclure :

- identité ;
- authentification ;
- autorisation ;
- paiements ;
- notifications ;
- audit ;
- fichiers ;
- facturation SaaS ;
- observabilité ;
- APIs.

La mutualisation ne doit pas empêcher Magic Gold de rester un produit autonome.

---

# 29. Vision long terme

Magic Gold doit évoluer d'une application de gestion commerciale vers une plateforme permettant au commerçant de piloter son activité.

```text
                    MAGIC GOLD
                         │
       ┌─────────────────┼─────────────────┐
       │                 │                 │
    COMMERCE          PAIEMENT          DONNÉES
       │                 │                 │
       ├── Vente         ├── Mobile       ├── Dashboard
       ├── Stock         ├── QR           ├── Analytics
       ├── Caisse        ├── Lien         ├── Alertes
       ├── Clients       └── API          └── Prévisions
       └── Achats
```

Le système doit progressivement aider le commerçant à passer de :

> **« Je sais combien j'ai vendu. »**

à :

> **« Je comprends mon commerce et je peux prendre de meilleures décisions. »**

---

# 30. Vision finale

Magic Gold n'a pas pour objectif initial de devenir le logiciel de gestion des plus grandes surfaces.

Son ambition est différente :

> **Devenir une infrastructure SaaS simple et accessible pour la gestion du commerce de proximité et du commerce de détail en Afrique.**

Le produit doit réunir :

```text
GESTION
   +
VENTE
   +
STOCK
   +
CAISSE
   +
PAIEMENT DIGITAL
   +
TRÉSORERIE
   +
DONNÉES
```

avec une philosophie :

> **Simple pour commencer. Puissant pour grandir.**

---

# 31. Phrase directrice du projet

> **Magic Gold permet au commerçant de vendre, gérer son stock, encaisser ses clients et comprendre son activité depuis son téléphone.**

Cette phrase constitue la référence principale de positionnement du produit.

---

# 32. Relation avec les documents suivants

Ce document définit la vision et les principes directeurs.

Les documents suivants détailleront les exigences :

```text
01_PRD.md
02_FunctionalSpecification.md
03_TechnicalArchitecture.md
04_DataModel.md
05_SynchronizationProtocol.md
06_API.md
07_SecurityArchitecture.md
08_HUBArchitecture.md
09_EventArchitecture.md
10_RedisStrategy.md
11_MobileArchitecture.md
12_PWAArchitecture.md
13_Deployment.md
14_TestStrategy.md
15_PaymentArchitecture.md
16_MultiTenancy.md
17_BusinessRules.md
```

Toute décision détaillée doit rester cohérente avec cette vision.

---

# 33. Statut

**Document de référence produit — Vision**

Les documents ultérieurs doivent :

- respecter cette vision ;
- expliciter les décisions ;
- identifier les hypothèses ;
- éviter les fonctionnalités non justifiées ;
- ne pas modifier silencieusement les principes fondamentaux.

Toute modification importante de la vision doit être explicitement documentée et versionnée.

---

**Nectux Ingénieries**  
**MAGIC GOLD — Votre commerce dans votre téléphone.**
