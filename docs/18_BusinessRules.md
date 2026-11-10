# MAGIC GOLD — Business Rules

**Document :** `18_BusinessRules.md`
**Version :** 1.0
**Statut :** Référence normative V1
**Éditeur :** Nectux Ingénieries
**Projet :** MAGIC GOLD

> **Ce document est subordonné à `00_Master.md`.** En cas de contradiction, le Master fait foi.
> Il ne définit ni vocabulaire ni états nouveaux : il rassemble et précise les règles métier
> transversales, en respectant strictement le vocabulaire officiel et les états authoritative du Master.

---

# 1. Objet

Ce document consolide les règles métier de MAGIC GOLD applicables au périmètre **V1** (commerce de détail).
Il est la référence normative pour l'implémentation backend (Spring Boot 3.5.x / Java 17) et pour les tests.

Il couvre :

* le calcul des montants et l'arrondi en **XOF** ;
* la **TVA / taxes** le cas échéant ;
* le **cycle de vie des Order** et toutes les transitions autorisées / interdites ;
* les **règles de paiement** (confirmation par provider, idempotence, réconciliation, remboursements) ;
* les **règles de caisse** (ouverture / clôture de CashSession, écarts, CashMovement) ;
* les **règles de stock** (StockMovement traçable, snapshot produit) ;
* les **règles clients** (ANONYMOUS / IDENTIFIED / REGISTERED, crédits V2) ;
* les **validations et contraintes métier** transversales.

Toute règle de ce document qui s'écarterait du `00_Master.md` doit être signalée et corrigée.

---

# 2. Hiérarchie de référence

L'ordre de prévalence documentaire est :

```text
00_Master.md          (autorité suprême : vocabulaire, périmètres, états)
      ↓
04_DataModel.md       (entités, types, contraintes d'intégrité)
      ↓
09_PaymentArchitecture.md
10_OrderManagement.md
11_MerchantSpecification.md
12_CustomerSpecification.md
13_Reporting.md
      ↓
18_BusinessRules.md   (ce document : consolidation normative)
      ↓
Code / tests
```

En cas de conflit entre ce document et le Master, **le Master prime**. En cas de conflit entre ce document
et un document subordonné au Master, ce document prime en tant que consolidation explicite.

---

# 3. Règles de calcul des montants et arrondi en XOF

## 3.1 Précision de stockage

```text
Tout montant monétaire : DECIMAL(19,4)  ou  BIGINT (centimes)
Devise par défaut     : XOF (Franc CFA)
```

Règles impératives (issues de `00_Master.md` §5.5 et `04_DataModel.md` §41) :

* ❌ jamais `FLOAT` ni `DOUBLE` pour les montants ;
* la devise est **explicitement associée** à chaque montant (`amount` + `currency = XOF`) ;
* en Java, utilisation de `BigDecimal` ;
* aucun montant financier ne doit être calculé côté frontend comme source de vérité.

## 3.2 Unité monétaire du XOF

Le franc CFA ne circule pas en subdivisions : la plus petite unité usuelle est **1 FCFA** (0 décimale).

Règles :

* le montant final facturé / débité est toujours un **entier de FCFA** ;
* la précision interne (4 décimales) est conservée pendant les calculs intermédiaires afin d'éviter
  l'accumulation d'erreurs d'arrondi ;
* l'arrondi final est appliqué **une seule fois**, au niveau du montant total (et des remboursements).

## 3.3 Méthode d'arrondi

```text
Arrondi : HALF_UP (au plus proche ; 0,5 → arrondi supérieur)
```

Exemple :

```text
1250,50 XOF  →  1251 XOF
1250,49 XOF  →  1250 XOF
1250,00 XOF  →  1250 XOF
```

## 3.4 Ordre de calcul d'une Order

```text
lineTotal   = quantity × unitPrice                    (précision pleine)
subtotal    = Σ lineTotal                             (précision pleine)
total       = subtotal − discountAmount + taxAmount (+ fees le cas échéant)
total       = roundHalfUp(total, 0)                   → entier XOF
```

Contraintes :

```text
subtotal       >= 0
discountAmount >= 0
taxAmount      >= 0
totalAmount    >= 0
lineTotal      >= 0
quantity       >  0
unitPrice      >= 0
```

## 3.5 Règles de non-régression

* Le **backend recalcule toujours** les montants (`subtotal`, `discountAmount`, `taxAmount`, `totalAmount`).
  Il ne fait **jamais confiance** au total transmis par le frontend.
* Une modification ultérieure du prix d'un Product **ne modifie jamais** une Order existante
  (voir §8 — snapshots).
* Le montant d'un Payment est **figé** au moment de son initiation ; une évolution du prix ou de la
  commande ne doit pas modifier rétroactivement un Payment existant (`09_PaymentArchitecture.md` §5).

## 3.6 Remboursements et arrondi

* Chaque remboursement (partiel ou total) est arrondi `HALF_UP` à 0 décimale.
* La contrainte de plafond (§6.7) est vérifiée sur les montants arrondis, jamais sur des montants flottants.

---

# 4. TVA / taxes

## 4.1 Principe V1

Le `00_Master.md` **n'impose pas** de taux de TVA obligatoire en V1.
La `09_PaymentArchitecture.md` §45 indique que « la règle fiscale exacte doit être définie séparément si nécessaire ».

Règles retenues pour V1 :

* la taxe est représentée par le champ `Order.tax_amount` (et `OrderItem.taxAmount` le cas échéant) ;
* aucun taux de TVA fixe n'est appliqué par défaut : `tax_amount` vaut `0` sauf configuration explicite ;
* si une TVA est appliquée, elle est **configurable par Merchant** (et non codée en dur) ;
* le taux ou le montant de taxe est un **snapshot** : il est figé dans l'OrderItem au moment de la commande ;
* une modification de la règle fiscale ne modifie **jamais** une Order ou un Payment historique.

## 4.2 Règle de calcul de la taxe (le cas échéant)

```text
baseTaxable   = subtotal − discountAmount
taxAmount     = roundHalfUp(baseTaxable × taux, 0)     (si taux configuré)
total         = subtotal − discountAmount + taxAmount (+ fees)
```

Si la taxe est exprimée en montant forfaitaire (et non en taux), `tax_amount` est pris tel quel,
après validation `tax_amount >= 0`.

---

# 5. Cycle de vie des Order — états authoritative

Les états d'Order sont **fixés par `00_Master.md` §4.2** et ne peuvent être modifiés que par une mise à jour du Master.

## 5.1 Liste officielle des états

```text
DRAFT
PENDING_PAYMENT
PAYMENT_FAILED
PAID
CONFIRMED
PROCESSING
READY
COMPLETED
CANCELLED
REFUND_PENDING
REFUNDED
```

## 5.2 Chemin principal

```text
DRAFT
   ↓
PENDING_PAYMENT
   ↓
PAID
   ↓
CONFIRMED
   ↓
PROCESSING
   ↓
READY
   ↓
COMPLETED
```

## 5.3 Branches alternatives (authoritative)

```text
PENDING_PAYMENT → PAYMENT_FAILED
PENDING_PAYMENT → CANCELLED
PAYMENT_FAILED  → PENDING_PAYMENT   (retry)
PAID            → REFUND_PENDING
PAID            → CANCELLED
CONFIRMED       → CANCELLED
PROCESSING      → CANCELLED
REFUND_PENDING  → REFUNDED
```

## 5.4 Matrice des transitions autorisées

| État actuel      | Transitions autorisées vers            | Déclencheur / action                    |
| ---------------- | -------------------------------------- | --------------------------------------- |
| DRAFT            | PENDING_PAYMENT                        | Soumission (submit)                     |
| PENDING_PAYMENT  | PAID                                   | Paiement confirmé (provider)            |
| PENDING_PAYMENT  | PAYMENT_FAILED                         | Échec de paiement                       |
| PENDING_PAYMENT  | CANCELLED                              | Annulation / expiration                 |
| PAYMENT_FAILED   | PENDING_PAYMENT                        | Nouvelle tentative (retry)              |
| PAYMENT_FAILED   | CANCELLED                              | Abandon / expiration                    |
| PAID             | CONFIRMED                              | Confirmation marchand                   |
| PAID             | REFUND_PENDING                         | Demande de remboursement                |
| PAID             | CANCELLED                              | Annulation + remboursement (workflow)   |
| CONFIRMED        | PROCESSING                             | Début de préparation                    |
| CONFIRMED        | CANCELLED                              | Annulation (avec remboursement requis)  |
| PROCESSING       | READY                                  | Commande prête                          |
| PROCESSING       | CANCELLED                              | Annulation (avec remboursement requis)  |
| READY            | COMPLETED                              | Exécution / remise terminée             |
| REFUND_PENDING   | REFUNDED                               | Remboursement confirmé                  |

## 5.5 Transitions interdites (liste non exhaustive mais normative)

Sont **interdites** et doivent être rejetées côté backend :

```text
Toute transition vers un état antérieur du chemin principal (ex. PAID → PENDING_PAYMENT,
CONFIRMED → PAID, PROCESSING → CONFIRMED, COMPLETED → READY, ...)

CANCELLED  → (tout autre état)        // une Order annulée n'est jamais réactivée
REFUNDED   → (tout autre état)        // état terminal
COMPLETED  → (tout autre état)        // état terminal du chemin nominal
DRAFT      → (tout état sauf PENDING_PAYMENT)
```

## 5.6 Règles associées aux transitions

* **Une Order annulée ne doit pas être réactivée.** Si nécessaire, une nouvelle Order est créée
  (`00_Master.md` §4.2).
* **Le backend est la seule autorité** capable de changer l'état d'une Order. Le frontend n'envoie jamais
  un `status` brut ; il demande une **action** (`POST /api/orders/{id}/confirm`, `.../cancel`, etc.).
* Toute transition est enregistrée dans **OrderStatusHistory** (`fromStatus`, `toStatus`, `actorId`,
  `actorType`, `reason`, `createdAt`, `correlationId`). L'historique est **immuable**.
* Le passage à `PAID` exige un `Payment.status == SUCCESS` vérifié (`10_OrderManagement.md` §21).

## 5.7 États terminaux vs annulation avec impact financier

```text
Annulation avant paiement (PENDING_PAYMENT → CANCELLED) : aucune opération de remboursement.
Annulation après paiement (PAID / CONFIRMED / PROCESSING → CANCELLED) : nécessite un remboursement
  (REFUND_PENDING → REFUNDED) selon les règles du §6.
```

L'annulation commerciale et le remboursement financier restent **deux opérations distinctes**.

---

# 6. Règles de paiement

Les états de Payment sont **fixés par `00_Master.md` §4.1**.

## 6.1 États authoritative de Payment

```text
CREATED
PENDING
PROCESSING
SUCCESS
FAILED
CANCELLED
REFUND_PENDING
PARTIALLY_REFUNDED
REFUNDED
```

## 6.2 Chemin nominal et branches

```text
CREATED → PENDING → PROCESSING → SUCCESS

PENDING     → FAILED
PENDING     → CANCELLED
PROCESSING  → FAILED
SUCCESS     → REFUND_PENDING
REFUND_PENDING → PARTIALLY_REFUNDED
REFUND_PENDING → REFUNDED
PARTIALLY_REFUNDED → REFUNDED
```

## 6.3 Transitions interdites de Payment

```text
SUCCESS → PENDING
SUCCESS → CREATED
SUCCESS → FAILED
REFUNDED → SUCCESS
```

Toute autre transition non listée au §6.2 est interdite et rejetée côté backend.

## 6.4 Confirmation par le provider

* Un Payment ne passe à `SUCCESS` **que** si MAGIC GOLD détient une **preuve vérifiée** du succès,
  émise par le provider (`09_PaymentArchitecture.md` §5.3 du Master / §29).
* La confirmation provient du **provider** (webhook vérifié et/ou interrogation `getPaymentStatus`),
  **jamais** du frontend.
* Avant d'appliquer un webhook `SUCCESS`, le backend vérifie au minimum :

```text
payment exists
provider matches
reference matches
amount matches
currency matches
external transaction id valide
état courant autorisant la transition
```

* Un webhook **non vérifié** (signature/secrét invalidée) est **rejeté**.
* Un état inconnu (`UNKNOWN / REQUIRES_RECONCILIATION`) ne doit **jamais** être converti automatiquement
  en `FAILED` ; il est traité en attente de réconciliation.

## 6.5 Protection contre le double paiement

* Avant toute création de Payment, vérifier l'absence de Payment `SUCCESS` sur la même Order.
* Si `order.paymentStatus == PAID`, une nouvelle tentative est refusée sauf scénario explicitement prévu.
* Contrainte : `SUM(payment.amount SUCCESS) <= order.totalAmount` (sauf surplus contrôlé de remboursement).

## 6.6 Idempotence

Les opérations suivantes sont **idempotentes** (`04_DataModel.md` §37) :

```text
Payment
WebhookEvent
Refund (demande de remboursement)
```

Mécanismes :

```text
Payment.idempotency_key  : UNIQUE
WebhookEvent             : UNIQUE(provider, external_event_id)
Idempotency-Key          : en-tête HTTP supporté sur les endpoints critiques
```

Règles :

* deux appels avec la même `Idempotency-Key` produisent **le même résultat logique** (jamais deux débits/crédits) ;
* une même clé ne doit pas être réutilisée avec une requête **différente** (hash de requête conservé) ;
* un même `external_event_id` reçu plusieurs fois → traitement une seule fois, les suivants sont des doublons ignorés.

## 6.7 Remboursements partiels / totaux

Règles :

```text
Le remboursement est une nouvelle opération financière (jamais une suppression d'historique).

Plafond (Refund Ceiling) :
    SUM(refunds.confirmed)  <=  payment.successAmount

Remboursement total   : SUM(refunds) == payment.successAmount  →  Payment = REFUNDED
Remboursement partiel : SUM(refunds)  <  payment.successAmount  →  Payment = PARTIALLY_REFUNDED

remainingRefundable = successAmount − SUM(refunds.confirmed)
```

* le remboursement est **idempotent** : une même demande ne génère jamais deux remboursements ;
* un remboursement ne doit jamais dépasser le montant **effectivement encaissé et non déjà remboursé** ;
* un Payment `SUCCESS` ne peut être « annulé » pour effacer son historique : on passe par le processus de remboursement ;
* le cumul des remboursements d'une Order respecte `refundedAmount <= paidAmount` (`10_OrderManagement.md` §56).

## 6.8 Réconciliation

* La réconciliation compare les sources **MAGIC GOLD ↔ PAYMENT PROVIDER ↔ BANK/SETTLEMENT**.
* Statuts de rapprochement :

```text
MATCHED
MISMATCHED
MISSING_INTERNAL
MISSING_PROVIDER
AMOUNT_MISMATCH
DUPLICATE
PENDING
```

* Une divergence (ex. attendu 100 000 / provider 95 000 → `AMOUNT_MISMATCH`) **crée une entrée de réconciliation**
  et **ne corrige jamais silencieusement** le montant historique.
* Toute correction produit : transaction d'origine + écriture d'ajustement + motif + acteur + horodatage + audit.

## 6.9 Règles d'immutabilité financière

```text
Interdit : DELETE payment / DELETE ledger entry / DELETE settlement / DELETE webhook traité
Recommandé : reversal / adjustment / refund / correction (nouvelles écritures)
```

---

# 7. Règles de caisse (Register / CashSession / CashMovement)

> Les états de caisse ne figurent **pas** dans la liste authoritative du Master (qui ne couvre que
> payment / order / merchant). Ils sont définis ici de façon minimale et cohérente avec le vocabulaire
> officiel (Register, CashSession, CashMovement) et ne peuvent pas contredire le Master.

## 7.1 Entités concernées (V1)

```text
Register      — caisse physique rattachée à une MerchantOutlet
CashSession   — session d'ouverture / fermeture d'une Register
CashMovement  — mouvement de caisse traçable et immuable
```

## 7.2 États de CashSession

```text
OPEN
CLOSED
```

Règles :

* une Register ne peut avoir **qu'une seule** CashSession `OPEN` à la fois ;
* une CashSession `CLOSED` est **terminale** (jamais rouverte) ;
* la fermeture enregistre le montant compté, calcule le montant attendu et l'écart.

## 7.3 Ouverture de CashSession

À l'ouverture :

```text
CashSession.OPEN
   + openingBalance (fond de caisse / fonds initial)
   + openingCashMovement (type OPENING)
```

* l'ouverture doit être tracée (utilisateur, date/heure, Register) ;
* un `CashMovement` de type `OPENING` est créé pour le fonds initial.

## 7.4 Clôture de CashSession et écart

À la clôture :

```text
expectedCash = openingBalance
             + Σ cash inflows  (ventes espèces)
             − Σ cash outflows (remboursements espèces)
             + Σ deposits     (dépôts)
             − Σ withdrawals  (retraits / dépenses de caisse)

variance = countedCash − expectedCash
```

Exemple (repris de `02_ProjectOverview.md` §14) :

```text
Espèces attendues   180 000 XOF
Espèces comptées    175 000 XOF
Écart               −5 000 XOF
```

Règles :

* l'écart (`variance`) est **toujours enregistré**, jamais ignoré ;
* un écart non nul génère une **anomalie** et peut exiger une **validation** (manager) selon un seuil configurable ;
* la clôture produit un `CashMovement` de type `CLOSING` (ou un état d'écart documenté).

## 7.5 CashMovement — types et règles

Types de mouvements :

```text
OPENING      (fond initial)
SALE         (vente en espèces)
REFUND       (remboursement en espèces)
DEPOSIT      (dépôt)
WITHDRAWAL   (retrait)
EXPENSE      (dépense prélevée en caisse)
ADJUSTMENT   (correction)
CLOSING      (clôture)
```

Règles :

* chaque CashMovement est **traçable** : produit/opération, utilisateur, date/heure, motif, montant, devise ;
* un CashMovement est **immuable** (append-only) : pas de modification ni de suppression silencieuse ;
* une correction passe par un nouveau `CashMovement` de type `ADJUSTMENT` avec motif ;
* tout mouvement de caisse est rattaché à une `CashSession` (et donc à une `Register`) ;
* l'isolation est `merchantId` : un opérateur d'un Merchant ne voit que les caisses de son périmètre.

## 7.6 Règles d'accès caisse

```text
CASHIER        : sa Register / sa CashSession uniquement
MANAGER        : toutes les caisses de son MerchantOutlet / Merchant
MERCHANT_OWNER : toutes les caisses de son Merchant
```

Toute requête est filtrée par `merchantId` (et `outletId`) côté backend.

---

# 8. Règles de stock (StockMovement + snapshot)

## 8.1 Principe

* Le stock est géré par des **StockMovement** traçables ; la quantité de stock est **dérivée** de la somme des mouvements.
* **Aucun ajustement direct** du stock : toute variation passe par un StockMovement justifié.

## 8.2 StockMovement — types

```text
IN           (entrée : achat, réception)
OUT          (sortie : vente)
SALE_OUT     (consommation liée à une vente)
PURCHASE_IN  (entrée d'achat fournisseur — V2 pour les Purchase)
RETURN_IN    (retour en stock)
ADJUSTMENT   (correction d'inventaire)
LOSS         (perte)
DAMAGE       (casse)
TRANSFER     (transfert entre outlets — V2)
```

## 8.3 Règles de traçabilité

Chaque StockMovement doit conserver :

```text
productId
outletId / registerId (le cas échéant)
quantity        (signée : positive = entrée, négative = sortie)
type
reason / motif
userId          (acteur)
referenceId     (ex. orderId pour SALE_OUT)
createdAt
```

* un StockMovement est **immuable** (append-only) ;
* toute correction = nouveau mouvement `ADJUSTMENT` avec motif, jamais modification de l'ancien ;
* la réservation/déconsommation est **atomique** (éviter les doubles consommations de stock en concurrence).

## 8.4 Mise à jour du stock sur une vente

```text
Vente confirmée (Order COMPLETED ou PAYÉ selon la règle du module) :
    Stock avant vente : 50
    Vente : 2
    Stock après vente : 48   →  StockMovement(type = SALE_OUT, quantity = −2)
```

* la quantité déduite doit être contrôlée : `quantity > 0` et `quantity <= availableQuantity` (si suivi activé) ;
* le stock peut être suivi par MerchantOutlet (stock par point de vente) ou globalement selon le niveau V1 retenu.

## 8.5 Snapshot produit dans OrderItem

Lors de la création d'une Order, chaque OrderItem **fige** les valeurs commerciales du moment :

```text
productId
productNameSnapshot   (nom au moment de l'achat)
skuSnapshot
unitPrice             (prix au moment de l'achat)
quantity
discountAmount
taxAmount (le cas échéant)
lineTotal
```

Règles :

* une modification ultérieure du Product (nom, prix, SKU) **ne modifie jamais** l'OrderItem historique ;
* la règle s'applique aussi aux Payment (montant figé) et aux frais (snapshot des frais) ;
* l'historique commercial reste reconstituable sans dépendre du catalogue courant.

---

# 9. Règles clients

## 9.1 Types de Customer (authoritative)

Fixés par `00_Master.md` §4.4 :

```text
ANONYMOUS   — aucune information personnelle
IDENTIFIED  — téléphone et/ou nom associé
REGISTERED  — dispose d'un compte User MAGIC GOLD (optionnel)
```

## 9.2 Règles fondamentales

* Un **Customer n'est jamais obligatoirement un User** MAGIC GOLD.
* Le mode par défaut en commerce de détail est **ANONYMOUS** : aucune donnée personnelle n'est obligatoire pour acheter.
* `Order.customerId` peut être **null** (vente anonyme).
* Le téléphone d'un Customer **n'identifie pas un client global MAGIC GOLD en V1** : un même numéro peut exister
  chez plusieurs Merchants, sans partage automatique d'historique entre eux.
* Le Customer appartient au périmètre du Merchant : `customer.merchantId == authenticatedMerchantId`
  (isolation appliquée côté backend).

## 9.3 Données minimales

```text
ANONYMOUS  : aucune information requise
IDENTIFIED : phone (et/ou nom) — facultatifs pour une vente ordinaire
REGISTERED : rattaché à un User (optionnel, hors socle de vente au comptoir)
```

* le nom, l'email et le téléphone restent **facultatifs** pour une vente ordinaire ;
* fournir un téléphone pour un reçu ne vaut **pas** consentement marketing.

## 9.4 Crédits client — V2 (hors périmètre V1)

Les entités de crédit client sont **V2** (`00_Master.md` §3.3) :

```text
Credit
CreditPayment
```

Règles :

* ❌ **pas de crédit client en V1** ;
* les notions de vente à crédit, plafond, échéance, solde et paiement partiel (décrites dans
  `02_ProjectOverview.md` §16) sont **documentées pour V2** et ne doivent pas être implémentées dans le MVP ;
* en V1, une vente est soit payée immédiatement (espèces ou digital), soit non conclue ;
* toute implémentation de `Credit`/`CreditPayment` exige une mise à jour préalable du périmètre dans le Master.

---

# 10. Validations et contraintes métier transversales

## 10.1 Montants

```text
amount           >  0          (Payment)
totalAmount      >= 0          (Order)
subtotal         >= 0
discountAmount   >= 0
taxAmount        >= 0
unitPrice        >= 0
lineTotal        >= 0
quantity         >  0
refundAmount     >  0          (et <= remainingRefundable)
```

## 10.2 Identifiants et références

```text
customer_number   UNIQUE
merchant_number   UNIQUE
order_number      UNIQUE
payment_number    UNIQUE
idempotency_key   UNIQUE
UNIQUE(provider, external_transaction_id)  (lorsque le provider garantit l'unicité)
UNIQUE(provider, external_event_id)        (WebhookEvent)
UNIQUE(merchant_id, sku)                   (Product)
```

## 10.3 Contraintes d'intégrité Order / Payment

```text
paidAmount        <= totalAmount
refundedAmount    <= paidAmount
remainingRefundable = paidAmount − refundedAmount
SUM(refunds.confirmed) <= payment.successAmount
```

Toute violation déclenche une **erreur critique** (jamais une correction silencieuse).

## 10.4 Préconditions d'opération

Avant création d'une Order :

```text
Customer actif (ou anonyme autorisé)
Merchant ACTIVE
Product ACTIVE
disponibilité (stock si suivi activé)
prix recalculé côté serveur
quantité autorisée
devise = XOF (ou devise explicitement supportée)
```

Avant initiation d'un Payment :

```text
Order existe et appartient au Customer
Merchant ACTIVE
Order payable (PENDING_PAYMENT)
amount > 0
currency valide
payment method supporté
absence de Payment SUCCESS antérieur
```

## 10.5 Multi-tenant (Merchant = Tenant)

```text
MERCHANT = TENANT
```

* isolation par `merchantId` sur **toutes** les données métier ;
* filtrage **toujours** appliqué côté backend, jamais uniquement côté frontend ;
* exemples : `order.merchantId == authenticatedMerchantId`, `customer.merchantId == ...`, `product.merchantId == ...`.

## 10.6 Historisation et suppression

```text
Suppression physique interdite pour : Payment, WebhookEvent traité, StockMovement,
                                      CashMovement, Order (données financières / traçabilité)
Suppression logique privilégiée (deleted_at / status) pour : Customer, Merchant, Product, Notification
```

* une Order, même `CANCELLED`, reste historisée ;
* l'`OrderStatusHistory` et l'`AuditLog` sont **immuables**.

## 10.7 Concurrence

Doivent être protégés contre les doubles traitements :

```text
deux confirmations simultanées d'une Order
deux paiements simultanés
deux webhooks identiques
deux remboursements simultanés
deux consommations de stock simultanées
```

Mécanismes : optimistic locking / version, transaction BDD, contraintes UNIQUE, validation de transition d'état.

## 10.8 Source de vérité

```text
Order total    → Order + OrderItem (recalculé)
Payment state  → Payment (confirmé par provider)
Stock          → somme des StockMovement
Caisse         → CashSession + CashMovement
CA encaissé    → somme des Payment SUCCESS (jamais « commandes × prix »)
```

Le frontend ne constitue **jamais** une source de vérité métier.

---

# 11. Règles Hermes (non-négociables)

1. Ne jamais confondre **Order**, **Payment**, **Wallet**, **Ledger**, **Notification**.
2. Ne jamais laisser le frontend modifier directement un statut ou un montant.
3. Ne jamais utiliser `float`/`double` pour l'argent ; `BigDecimal` / `DECIMAL(19,4)` obligatoire.
4. Ne jamais considérer un paiement réussi sans preuve provider vérifiée.
5. Ne jamais dépasser le plafond de remboursement (`SUM(refunds) <= successAmount`).
6. Ne jamais supprimer physiquement une donnée financière ou de traçabilité.
7. Ne jamais modifier un snapshot (OrderItem / Payment) après création.
8. Ne jamais déduire un client global depuis un numéro de téléphone en V1.
9. Ne jamais implémenter de crédit client, wallet, fidélité ou settlement automatisé en V1 sans évolution du Master.
10. Toute transition d'état non prévue est rejetée ; tout écart (caisse, réconciliation) est enregistré, jamais corrigé silencieusement.

---

# 12. Documents liés

```text
00_Master.md                (autorité suprême — vocabulaire, périmètres, états)
04_DataModel.md             (entités, types, contraintes)
09_PaymentArchitecture.md   (paiements, webhooks, remboursements, réconciliation)
10_OrderManagement.md       (cycle de commande, transitions, snapshots)
11_MerchantSpecification.md (marchand, catalogue, opérateurs)
12_CustomerSpecification.md (client anonyme/identifié/enregistré)
13_Reporting.md             (indicateurs dérivés, CA)
```

---

**Nectux Ingénieries**  
**MAGIC GOLD — Simple pour commencer. Puissant pour grandir.**
