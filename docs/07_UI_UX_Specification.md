# MAGIC GOLD — UI/UX Specification

**Version:** 1.0
**Document:** `07_UI_UX_Specification.md`
**Projet:** MAGIC GOLD
**Statut:** Référence UI/UX
**Audience:** UX/UI, Frontend, Mobile, Backend, QA, DevOps, Hermes

---

# 1. Objectif

Ce document définit les règles d'interface et d'expérience utilisateur de MAGIC GOLD.

Il constitue la référence pour :

* le design des écrans ;
* la navigation ;
* les composants UI ;
* les parcours utilisateurs ;
* le responsive design ;
* les états d'interface ;
* les droits d'affichage selon les rôles ;
* l'accessibilité ;
* la cohérence visuelle ;
* l'implémentation frontend.

Toute interface MAGIC GOLD doit respecter cette spécification sauf décision documentée contraire.

---

# 2. Principes UX

MAGIC GOLD doit privilégier :

```text
Simple
Rapide
Clair
Cohérent
Mobile First
Accessible
Sécurisé
Orienté action
```

Le système doit éviter :

* les écrans surchargés ;
* les menus excessivement profonds ;
* les informations inutiles ;
* les formulaires inutilement longs ;
* les actions critiques sans confirmation ;
* les erreurs incompréhensibles.

---

# 3. Utilisateurs principaux

L'interface doit prendre en compte plusieurs catégories :

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

Chaque rôle ne voit que les fonctionnalités nécessaires à son activité.

---

# 4. Principe de navigation par rôle

Exemple :

```text
CUSTOMER
├── Accueil
├── Produits
├── Commandes
├── Wallet
├── Fidélité
├── Notifications
└── Profil
```

```text
MERCHANT
├── Dashboard
├── Produits
├── Commandes
├── Clients
├── Paiements
├── Fidélité
├── Rapports
└── Paramètres
```

```text
ADMIN
├── Dashboard
├── Utilisateurs
├── Marchands
├── Produits
├── Commandes
├── Paiements
├── Wallet
├── Fidélité
├── Audit
├── Configuration
└── Sécurité
```

La navigation exacte dépend des permissions attribuées.

---

# 5. Architecture globale

MAGIC GOLD doit utiliser une structure d'application cohérente :

```text
┌──────────────────────────────────────────────┐
│ Logo | Recherche | Notifications | Profil  │
├──────────────┬───────────────────────────────┤
│              │                               │
│ Navigation   │         Main Content          │
│              │                               │
│ Dashboard    │                               │
│ Produits     │                               │
│ Commandes    │                               │
│ Wallet       │                               │
│ Fidélité     │                               │
│ ...          │                               │
│              │                               │
└──────────────┴───────────────────────────────┘
```

Sur mobile, la navigation doit être adaptée.

---

# 6. Responsive Design

L'interface doit fonctionner au minimum sur :

```text
Mobile
Tablet
Desktop
Large Desktop
```

Breakpoints indicatifs :

```text
Mobile:       < 768px
Tablet:       768–1023px
Desktop:      1024–1439px
Large:        ≥ 1440px
```

Les breakpoints peuvent être adaptés au framework utilisé.

---

# 7. Mobile First

Les parcours critiques doivent être parfaitement utilisables sur mobile :

```text
Connexion
Recherche produit
Commande
Paiement
Wallet
Consultation commande
QR/code
Notifications
Profil
```

Les boutons principaux doivent être facilement accessibles au doigt.

---

# 8. Design System

MAGIC GOLD doit utiliser un Design System centralisé.

Il doit contenir :

```text
Colors
Typography
Spacing
Buttons
Inputs
Cards
Tables
Badges
Modals
Dropdowns
Tabs
Alerts
Toasts
Pagination
Loaders
Empty states
Error states
```

Les composants doivent être réutilisables.

---

# 9. Identité visuelle

MAGIC GOLD doit avoir une identité visuelle premium, moderne et technologique.

Direction visuelle :

```text
Premium
Digital
Trust
Finance
Commerce
Modern
```

Éviter une interface excessivement décorative.

L'information et les actions doivent rester prioritaires.

---

# 10. Palette de couleurs

Palette de référence :

```text
Primary
Gold / Metallic Gold

Secondary
Dark / Charcoal

Background
White / Light Neutral

Success
Green

Warning
Amber

Danger
Red

Info
Blue
```

Les valeurs hexadécimales définitives doivent être centralisées dans les design tokens.

---

# 11. Gold

Le doré doit être utilisé principalement pour :

```text
Primary CTA
Highlights
Branding
Premium elements
Important metrics
Selected states
```

Il ne doit pas être utilisé pour tous les éléments simultanément.

---

# 12. Dark

Le dark/charcoal peut être utilisé pour :

```text
Header
Sidebar
Footer
Primary text
Premium surfaces
```

La lisibilité doit rester prioritaire.

---

# 13. Typography

La typographie doit être :

```text
Readable
Modern
Professional
Responsive
```

Hiérarchie :

```text
H1
H2
H3
H4
Body
Small
Caption
```

Les tailles doivent être définies dans les design tokens.

---

# 14. Spacing

Utiliser une échelle de spacing cohérente.

Exemple :

```text
4
8
12
16
24
32
48
64
```

Éviter les espacements arbitraires.

---

# 15. Border Radius

Les composants doivent utiliser un nombre limité de rayons.

Exemple :

```text
Small
Medium
Large
Pill
```

Les tokens doivent être centralisés.

---

# 16. Shadows

Les ombres doivent rester légères.

Utilisation :

```text
Cards
Dropdowns
Modals
Floating elements
```

Éviter les ombres excessives.

---

# 17. Iconographie

Les icônes doivent être cohérentes.

Utiliser une seule famille principale.

Les icônes doivent :

* avoir une signification claire ;
* être accompagnées d'un texte lorsqu'elles ne sont pas évidentes ;
* ne pas remplacer seules les informations critiques.

---

# 18. Logo

Le logo MAGIC GOLD doit être visible sur :

```text
Login
Desktop Header
Mobile Header
Loading/Splash
Emails lorsque nécessaire
```

Le logo ne doit pas être déformé.

---

# 19. Header

Le header desktop peut contenir :

```text
Logo
Search
Notifications
Help
Profile
```

Le header mobile doit rester compact.

---

# 20. Sidebar

Sur desktop :

```text
Dashboard
Commerce
Finance
Fidélité
Clients
Rapports
Administration
```

Les menus doivent être regroupés logiquement.

---

# 21. Navigation Mobile

La navigation mobile doit utiliser une approche simple.

Exemple :

```text
┌───────────────────────────┐
│ Header                    │
├───────────────────────────┤
│                           │
│ Content                   │
│                           │
├───────────────────────────┤
│ Home Orders Wallet Profile│
└───────────────────────────┘
```

Les fonctions secondaires peuvent être accessibles depuis un menu.

---

# 22. Dashboard

Le dashboard doit présenter les informations les plus importantes immédiatement.

Structure :

```text
Dashboard
│
├── KPI
│
├── Actions rapides
│
├── Activité récente
│
├── Graphiques
│
└── Alertes
```

---

# 23. KPI

Les KPI doivent être présentés sous forme de cards.

Exemples :

```text
Chiffre d'affaires
Commandes
Paiements
Clients
Wallet
Points fidélité
```

Les KPI doivent afficher :

```text
Valeur
Libellé
Période
Variation lorsque pertinente
```

---

# 24. Graphiques

Les graphiques doivent servir une décision ou une compréhension.

Exemples :

```text
Sales over time
Orders over time
Payment methods
Top products
Customer growth
Wallet activity
```

Éviter les graphiques purement décoratifs.

---

# 25. Dashboard Customer

Le client doit voir notamment :

```text
Bonjour, [Prénom]

Solde Wallet
Points fidélité
Commandes en cours
Dernières commandes
Offres disponibles
Produits recommandés
```

---

# 26. Dashboard Merchant

Le marchand doit voir :

```text
Revenue
Orders
Pending orders
Products
Customers
Payment status
Top products
Low stock
```

---

# 27. Dashboard Admin

L'administrateur doit avoir une vue système :

```text
Users
Merchants
Transactions
Payments
Orders
Security alerts
System health
Audit activity
```

---

# 28. Login

Écran :

```text
Logo MAGIC GOLD

Email / Téléphone
Mot de passe

[ Se connecter ]

Mot de passe oublié ?

Créer un compte
```

Si OAuth/social login est activé, les options sont affichées clairement.

---

# 29. Login States

Prévoir :

```text
Loading
Success
Invalid credentials
Account locked
Account disabled
Network error
Server error
```

Le message d'erreur ne doit pas révéler d'information sensible.

---

# 30. Registration

Le formulaire doit demander uniquement les informations nécessaires.

Exemple :

```text
Prénom
Nom
Téléphone
Email
Mot de passe
Confirmation
```

Selon le modèle métier, certaines informations peuvent être facultatives.

---

# 31. Password Reset

Parcours :

```text
Forgot Password
       ↓
Identity verification
       ↓
Reset
       ↓
Success
```

Les informations sensibles ne doivent pas être affichées.

---

# 32. Profile

Le profil contient :

```text
Personal information
Contact
Security
Preferences
Notifications
Sessions
```

Les fonctions sensibles peuvent demander une réauthentification.

---

# 33. Products

La liste produit doit permettre :

```text
Search
Filter
Sort
Category
Price
Availability
Merchant
```

---

# 34. Product Card

Une card produit peut contenir :

```text
Image
Name
Merchant
Price
Discount
Rating when applicable
Availability
CTA
```

---

# 35. Product Detail

Structure :

```text
Image gallery

Product name
Description
Price
Discount
Availability
Merchant

Quantity

[ Ajouter ]
[ Acheter ]
```

Les informations doivent être claires avant l'achat.

---

# 36. Cart

Le panier affiche :

```text
Products
Quantity
Unit price
Discount
Subtotal
Taxes/fees when applicable
Total
```

Le total doit être recalculé côté backend.

---

# 37. Checkout

Étapes :

```text
1. Cart
2. Delivery / fulfillment
3. Payment
4. Confirmation
```

Afficher clairement l'étape actuelle.

---

# 38. Checkout Security UX

Avant validation :

```text
Total
Currency
Payment method
Merchant
Order summary
```

Le client doit comprendre ce qu'il valide.

---

# 39. Payment Screen

Afficher :

```text
Montant
Devise
Commande
Méthode de paiement
Statut
```

Ne jamais afficher les secrets techniques.

---

# 40. Payment States

```text
PENDING
PROCESSING
SUCCESS
FAILED
CANCELLED
EXPIRED
REFUNDED
```

Chaque statut doit avoir une représentation visuelle claire.

---

# 41. Payment Success

Après paiement réussi :

```text
✓ Paiement réussi

Commande #MG-XXXX

Montant
Date
Méthode

[ Voir la commande ]
[ Retour à l'accueil ]
```

---

# 42. Payment Failure

Le message doit être compréhensible.

Exemple :

```text
Le paiement n'a pas pu être finalisé.

Aucun débit confirmé n'est affiché comme réussi.

[ Réessayer ]
[ Modifier le moyen de paiement ]
```

Le système doit éviter d'affirmer qu'aucun débit n'a eu lieu tant que le statut provider n'est pas connu.

---

# 43. Orders

Liste :

```text
Order ID
Date
Merchant
Amount
Status
Payment status
Action
```

---

# 44. Order Detail

Afficher :

```text
Order information
Products
Amounts
Payment
Fulfillment
Timeline
Invoice when applicable
```

---

# 45. Order Timeline

Exemple :

```text
Commande créée
      ↓
Paiement confirmé
      ↓
Préparation
      ↓
Expédition
      ↓
Livraison
```

---

# 46. Wallet

Le wallet doit présenter :

```text
Balance
Available balance
Pending amount
Transactions
Top-up
Transfer
Withdrawal when supported
```

---

# 47. Wallet Dashboard

Exemple :

```text
┌────────────────────────────┐
│ Solde disponible           │
│                            │
│       125 000 XOF          │
│                            │
│ [Ajouter] [Transférer]     │
└────────────────────────────┘

Transactions récentes
```

---

# 48. Wallet Transaction

Afficher :

```text
Reference
Type
Amount
Date
Status
Description
```

---

# 49. Wallet Confirmation

Pour une opération sensible :

```text
Montant
Destinataire
Frais éventuels
Total
```

Puis :

```text
[ Confirmer ]
[ Annuler ]
```

Une confirmation supplémentaire peut être exigée selon le risque.

---

# 50. Loyalty

L'espace fidélité doit afficher :

```text
Points disponibles
Points gagnés
Points utilisés
Historique
Récompenses
```

---

# 51. Loyalty Reward

Une récompense doit présenter :

```text
Nom
Description
Nombre de points
Disponibilité
Conditions
Date d'expiration
[ Utiliser ]
```

---

# 52. Redemption Confirmation

Avant consommation :

```text
Vous allez utiliser :

500 points

Récompense :
...

Solde après opération :
1 250 points

[ Confirmer ]
[ Annuler ]
```

---

# 53. Coupons

L'utilisateur peut consulter :

```text
Available
Used
Expired
```

Une validation doit indiquer clairement les conditions.

---

# 54. Notifications

Types :

```text
Payment
Order
Wallet
Loyalty
Promotion
Security
System
```

---

# 55. Notification Center

Structure :

```text
Toutes
Non lues
Sécurité
Commandes
Paiements
```

Chaque notification doit permettre une action lorsque nécessaire.

---

# 56. Toasts

Les toasts doivent être réservés aux événements courts.

Exemples :

```text
Produit ajouté
Profil mis à jour
Coupon appliqué
```

Les informations critiques doivent utiliser une interface plus persistante.

---

# 57. Alerts

Les alertes sont utilisées pour :

```text
Security
Payment issue
Account status
Important system information
```

---

# 58. Modals

Les modals doivent être utilisées avec modération.

Actions critiques :

```text
Delete
Refund
Transfer
Deactivate
Role change
```

doivent demander confirmation.

---

# 59. Destructive Actions

Les actions destructives doivent :

```text
être clairement identifiées
expliquer la conséquence
demander confirmation
```

Exemple :

```text
Supprimer ce produit ?

Cette action ne pourra pas être annulée.

[ Annuler ] [ Supprimer ]
```

---

# 60. Tables

Les tables doivent :

```text
être filtrables
être triables lorsque pertinent
supporter pagination
être responsive
```

Sur mobile, elles peuvent devenir des cards.

---

# 61. Search

La recherche doit :

```text
répondre rapidement
tolérer les fautes raisonnables
afficher l'état loading
afficher empty state
```

Les résultats doivent être pertinents.

---

# 62. Filters

Les filtres doivent être :

```text
visibles
compréhensibles
réinitialisables
persistants lorsque pertinent
```

Afficher les filtres actifs.

---

# 63. Pagination

Pour les grandes listes :

```text
pagination
```

ou :

```text
infinite scroll
```

selon le cas d'utilisation.

Pour les interfaces administratives, la pagination est généralement préférable.

---

# 64. Loading State

Chaque opération asynchrone doit avoir un état de chargement.

Exemples :

```text
Skeleton
Spinner
Progress indicator
Button loading
```

Ne pas laisser l'utilisateur sans indication.

---

# 65. Skeleton

Pour les pages riches :

```text
Dashboard
Product list
Order list
Profile
```

utiliser des skeletons lorsque cela améliore la perception de performance.

---

# 66. Empty State

Exemple :

```text
Aucune commande

Vous n'avez encore passé aucune commande.

[ Découvrir les produits ]
```

L'empty state doit proposer une action pertinente lorsque possible.

---

# 67. Error State

Exemple :

```text
Impossible de charger les données.

Vérifiez votre connexion puis réessayez.

[ Réessayer ]
```

---

# 68. Offline State

Sur mobile :

```text
Connexion perdue
```

doit être clairement indiqué.

Les opérations financières ne doivent pas être considérées comme réussies hors connexion.

---

# 69. Accessibility

L'interface doit viser une bonne conformité aux principes WCAG.

Prévoir notamment :

```text
Keyboard navigation
Focus visible
Color contrast
ARIA labels
Readable typography
Alternative text
Screen reader compatibility
```

---

# 70. Color Accessibility

La couleur seule ne doit jamais être l'unique moyen de communiquer un état.

Incorrect :

```text
rouge = erreur
vert = succès
```

sans texte ou icône.

Préférer :

```text
🔴 Échec
🟢 Réussi
```

avec une représentation accessible.

---

# 71. Forms

Les formulaires doivent :

```text
être regroupés logiquement
avoir des labels
valider les champs
indiquer les erreurs
préserver les données lorsque possible
```

---

# 72. Form Validation

Les erreurs doivent apparaître près du champ concerné.

Exemple :

```text
Téléphone

[ +225 ... ]

⚠ Numéro de téléphone invalide.
```

---

# 73. Password Input

Le champ mot de passe doit permettre :

```text
show/hide
```

mais ne doit jamais afficher le mot de passe par défaut.

---

# 74. Confirmation Before Financial Action

Toute action financière sensible doit avoir une étape de confirmation.

Exemple :

```text
Transfer

Amount: 50 000 XOF
Recipient: ...
Fee: ...
Total: ...

[ Confirm ]
```

---

# 75. Security UX

Pour les opérations sensibles :

```text
réauthentification
OTP
MFA
confirmation
```

peuvent être demandés selon la politique de sécurité.

---

# 76. Role-Based UI

Le frontend doit masquer ou désactiver les fonctions non autorisées.

Mais :

> Le masquage frontend n'est jamais une mesure de sécurité suffisante.

Le backend doit toujours vérifier les permissions.

---

# 77. Permission-Aware Components

Exemple conceptuel :

```text
if user.can("PRODUCT_CREATE")
    show "Ajouter produit"
```

Mais l'API doit également vérifier :

```text
PRODUCT_CREATE
```

---

# 78. Admin UI

L'administration doit privilégier :

```text
Information density
Search
Filters
Bulk operations
Auditability
Clear statuses
```

---

# 79. Admin Users

Liste :

```text
User
Role
Status
Last login
Created
Actions
```

Actions sensibles :

```text
Disable
Reset
Role change
```

doivent être protégées.

---

# 80. Merchant UI

Le marchand doit pouvoir gérer :

```text
Profile
Products
Orders
Inventory when supported
Customers
Payments
Reports
Promotions
```

---

# 81. Merchant Product Management

Actions :

```text
Create
Edit
Deactivate
Delete when allowed
```

Les suppressions irréversibles doivent être utilisées avec prudence.

---

# 82. Customer Experience

Le parcours client doit minimiser les étapes :

```text
Discover
→ Select
→ Cart
→ Checkout
→ Pay
→ Track
```

---

# 83. Checkout Principle

Le checkout doit éviter les distractions.

Une fois dans le checkout :

```text
Order
Payment
Confirmation
```

doivent être prioritaires.

---

# 84. Notifications UX

Les notifications doivent être :

```text
actionable
concise
prioritized
non-intrusive
```

Les notifications de sécurité doivent avoir une priorité supérieure aux promotions.

---

# 85. Internationalization

L'architecture frontend doit permettre l'internationalisation.

Prévoir :

```text
FR
EN
```

si le périmètre le nécessite.

Les textes ne doivent pas être codés en dur dans les composants lorsque l'i18n est activé.

---

# 86. Currency

Les montants doivent toujours afficher leur devise.

Exemple :

```text
125 000 XOF
```

Ne jamais afficher uniquement :

```text
125 000
```

pour une donnée financière.

---

# 87. Number Formatting

Les nombres doivent être formatés selon le contexte régional.

Les valeurs utilisées dans les calculs ne doivent pas dépendre du formatage visuel.

---

# 88. Dates

Afficher des dates compréhensibles :

```text
22 sept. 2026
```

et heures lorsque nécessaire :

```text
22 sept. 2026 à 14:32
```

Les données internes doivent rester au format standard approprié.

---

# 89. Status Badges

Les statuts doivent utiliser des badges cohérents.

Exemple :

```text
SUCCESS
PENDING
FAILED
CANCELLED
```

Les libellés affichés à l'utilisateur doivent être compréhensibles.

---

# 90. QR Code

Si MAGIC GOLD utilise des QR codes :

```text
large enough
high contrast
quiet zone
clear purpose
```

Une alternative textuelle ou manuelle doit exister lorsque nécessaire.

---

# 91. Camera / Scanner

Sur mobile :

```text
permission request
camera loading
scan success
scan failure
retry
```

doivent être gérés.

---

# 92. Performance UX

Objectifs :

```text
Fast initial render
Lazy loading
Image optimization
Code splitting
API pagination
Caching where appropriate
```

Les pages ne doivent pas charger inutilement toutes les données.

---

# 93. Image Optimization

Les images produits doivent :

```text
être compressées
avoir des dimensions adaptées
utiliser un format moderne lorsque possible
supporter lazy loading
```

---

# 94. API Loading Strategy

Éviter :

```text
10 appels API simultanés
```

pour afficher une simple page.

Préférer :

```text
aggregated endpoints
parallel requests when useful
caching
pagination
```

selon l'architecture backend.

---

# 95. Optimistic UI

L'optimistic UI peut être utilisée pour les opérations à faible risque.

Exemple :

```text
mark notification as read
```

Elle doit être évitée ou soigneusement contrôlée pour :

```text
payment
wallet transfer
refund
financial transaction
```

---

# 96. Error Recovery

L'interface doit permettre :

```text
Retry
Refresh
Back
Cancel
Resume
```

lorsque pertinent.

---

# 97. Network Timeout

Une requête trop longue doit avoir un comportement contrôlé.

Ne pas laisser un bouton bloqué indéfiniment.

---

# 98. Double Click Protection

Les actions critiques doivent empêcher les doubles soumissions côté UX :

```text
button disabled
loading state
```

Mais la vraie protection doit rester côté backend avec l'idempotence.

---

# 99. Mobile Touch Targets

Les éléments interactifs doivent être suffisamment grands pour une utilisation tactile confortable.

Éviter les boutons minuscules ou trop rapprochés.

---

# 100. Navigation State

Lorsqu'un utilisateur revient en arrière :

```text
filters
search
pagination
scroll
```

doivent être conservés lorsque cela améliore l'expérience.

---

# 101. Breadcrumbs

Les breadcrumbs sont utiles principalement sur desktop pour les zones administratives.

Exemple :

```text
Administration
→ Merchants
→ Merchant detail
```

---

# 102. Deep Linking

Les pages importantes doivent pouvoir être ouvertes directement via une URL.

Exemple :

```text
/orders/123
/products/456
/merchants/789
```

L'accès doit toujours être vérifié côté backend.

---

# 103. Unauthorized Page

Lorsqu'un utilisateur n'a pas le droit :

```text
403
```

L'interface doit afficher une page claire :

```text
Accès non autorisé.

Vous ne disposez pas des permissions nécessaires.
```

---

# 104. Not Found

Pour une ressource inexistante :

```text
404
```

Afficher :

```text
Cette ressource n'existe pas ou n'est plus disponible.
```

---

# 105. Session Expired

Lorsque la session expire :

```text
Votre session a expiré.

Veuillez vous reconnecter.
```

Ne jamais exposer de détails techniques.

---

# 106. Security Notifications

Exemples :

```text
Nouvelle connexion
Mot de passe modifié
MFA activé
Nouvel appareil
Transaction sensible
```

Ces notifications doivent être clairement identifiées.

---

# 107. Design Tokens

Les valeurs visuelles doivent être centralisées.

Exemple :

```text
colors.primary
colors.secondary
colors.background
colors.success
colors.warning
colors.danger

spacing.xs
spacing.sm
spacing.md
spacing.lg
spacing.xl

radius.sm
radius.md
radius.lg

font.size.sm
font.size.md
font.size.lg
```

---

# 108. Component Architecture

Les composants doivent être organisés :

```text
components/
├── ui/
├── forms/
├── navigation/
├── commerce/
├── payment/
├── wallet/
├── loyalty/
├── admin/
└── charts/
```

La structure exacte peut être adaptée au framework.

---

# 109. Reusable Components

Priorité à la réutilisation.

Exemples :

```text
AppButton
AppInput
AppModal
AppTable
AppCard
AppBadge
AppAlert
AppPagination
AppEmptyState
AppErrorState
AppSkeleton
```

---

# 110. Business Components

Exemples :

```text
ProductCard
OrderStatus
PaymentStatus
WalletBalance
LoyaltyPoints
MerchantCard
TransactionList
```

---

# 111. Frontend Architecture Principle

Le frontend doit séparer :

```text
UI
State
API
Business presentation
Authentication
Authorization
```

Éviter de mélanger toute la logique dans les composants visuels.

---

# 112. API Error Mapping

Le frontend doit mapper les erreurs backend vers des messages utilisateur.

Exemple :

```text
HTTP 401
→ Session expirée

HTTP 403
→ Accès non autorisé

HTTP 404
→ Ressource introuvable

HTTP 409
→ Conflit

HTTP 422
→ Données invalides

HTTP 429
→ Trop de tentatives

HTTP 500
→ Erreur temporaire
```

---

# 113. Accessibility Testing

Avant release :

```text
Keyboard test
Screen reader test
Contrast test
Focus test
Mobile accessibility test
```

---

# 114. Browser Support

Le support minimum doit être défini avant production.

Cible recommandée :

```text
Chrome
Edge
Safari
Firefox
```

sur versions modernes supportées.

---

# 115. Mobile Application

Si une application mobile native ou cross-platform est développée, elle doit réutiliser :

```text
API
Authentication
Authorization
Design tokens
Business rules
```

Le mobile ne doit pas réimplémenter les règles financières côté client.

---

# 116. Frontend Security Rules

Hermes doit respecter :

1. Ne jamais mettre de secret backend dans le frontend.
2. Ne jamais considérer le frontend comme autorité.
3. Ne jamais calculer définitivement un montant financier uniquement côté frontend.
4. Ne jamais stocker inutilement des tokens sensibles.
5. Ne jamais exposer les réponses API brutes contenant des données internes.
6. Ne jamais utiliser `v-html` avec du contenu non fiable.
7. Ne jamais désactiver les contrôles de permission pour contourner un problème.
8. Ne jamais faire confiance aux paramètres d'URL.
9. Toujours gérer les erreurs API.
10. Toujours prévoir loading/empty/error/success.

---

# 117. UX Definition of Done

Une fonctionnalité frontend est terminée lorsque :

```text
[ ] Desktop OK
[ ] Mobile OK
[ ] Loading OK
[ ] Empty state OK
[ ] Error state OK
[ ] Success state OK
[ ] Validation OK
[ ] Permissions OK
[ ] Accessibility OK
[ ] API errors handled
[ ] No sensitive data exposed
[ ] No duplicate submission
[ ] Responsive
```

---

# 118. QA UI Checklist

```text
[ ] Login
[ ] Logout
[ ] Session expiration
[ ] Navigation
[ ] Responsive
[ ] Forms
[ ] Validation
[ ] Search
[ ] Filters
[ ] Pagination
[ ] Tables
[ ] Modals
[ ] Toasts
[ ] Notifications
[ ] Payments
[ ] Orders
[ ] Wallet
[ ] Loyalty
[ ] Admin
```

---

# 119. Visual Regression

Les composants principaux devraient faire l'objet de tests visuels lorsque l'infrastructure le permet.

Priorité :

```text
Login
Dashboard
Product
Checkout
Payment
Wallet
Order
Admin
```

---

# 120. UX Principle for Hermes

Avant de créer un écran, Hermes doit identifier :

```text
Actor
Goal
Permissions
Data required
Primary action
Secondary actions
Success state
Error state
Loading state
Empty state
Security requirements
Mobile behavior
```

---

# 121. Règle finale Hermes

Hermes ne doit pas créer une interface uniquement parce qu'une API existe.

Chaque écran doit répondre à :

```text
Qui utilise cet écran ?
Pourquoi ?
Que doit-il voir ?
Que peut-il faire ?
Que lui est-il interdit de faire ?
Que se passe-t-il en cas d'erreur ?
Que se passe-t-il sur mobile ?
```

---

# 122. Documents liés

```text
00_ProjectVision.md
01_PRD.md
02_FunctionalSpecification.md
03_TechnicalArchitecture.md
04_DataModel.md
05_API_Specification.md
06_SecurityArchitecture.md
08_Notifications.md
15_Roles_Permissions_Audit.md
18_TestStrategy.md
```

---

# 123. Source de vérité

La hiérarchie UI/UX est :

```text
UI/UX Specification
        ↓
Design System
        ↓
User Flows
        ↓
Components
        ↓
Pages
        ↓
Implementation
        ↓
Visual + Functional Tests
```

Toute divergence importante doit être documentée.

---

# 124. Conclusion

L'objectif de l'interface MAGIC GOLD est de fournir une expérience :

```text
Simple
Rapide
Premium
Fiable
Accessible
Sécurisée
Cohérente
```

L'interface doit rendre les opérations complexes compréhensibles sans masquer les informations importantes.

Le principe directeur est :

> **L'utilisateur doit toujours comprendre où il se trouve, ce qu'il peut faire, ce qu'il va confirmer et ce qui s'est réellement passé.**
# 126. Matrice Écrans — Rôles — Permissions

Cette matrice définit les droits d'accès aux écrans MAGIC GOLD.

Elle constitue la référence frontend pour :

* afficher ou masquer les menus ;
* protéger les routes ;
* afficher ou masquer les actions ;
* contrôler les boutons ;
* définir les tests d'autorisation ;
* aligner l'interface avec l'API.

> **Important :** le frontend utilise cette matrice pour l'expérience utilisateur, mais **l'autorisation réelle doit toujours être vérifiée par le backend**.

---

# 127. Légende

| Code | Signification                    |
| ---- | -------------------------------- |
| `V`  | Peut consulter l'écran           |
| `C`  | Peut créer                       |
| `M`  | Peut modifier                    |
| `S`  | Peut supprimer/désactiver        |
| `A`  | Peut effectuer une action métier |
| `—`  | Aucun accès                      |

Pour les écrans complexes, `V` signifie uniquement accès à l'écran. Les actions sont contrôlées séparément par permission.

---

# 128. Rôles principaux

| Code                | Rôle                   |
| ------------------- | ---------------------- |
| `CUSTOMER`          | Client                 |
| `MERCHANT`          | Marchand               |
| `MERCHANT_OPERATOR` | Opérateur du marchand  |
| `SUPPORT`           | Support client         |
| `FINANCE`           | Équipe financière      |
| `AUDITOR`           | Auditeur               |
| `ADMIN`             | Administrateur         |
| `SUPER_ADMIN`       | Administrateur système |

---

# 129. Matrice principale

| Écran / Fonction   | CUSTOMER | MERCHANT | OPERATOR | SUPPORT | FINANCE | AUDITOR |   ADMIN | SUPER_ADMIN |
| ------------------ | -------: | -------: | -------: | ------: | ------: | ------: | ------: | ----------: |
| Login              |        V |        V |        V |       V |       V |       V |       V |           V |
| Dashboard          |        V |        V |        V |       V |       V |       V |       V |           V |
| Profil             |        V |        V |        V |       V |       V |       V |       V |           V |
| Notifications      |        V |        V |        V |       V |       V |       V |       V |           V |
| Recherche produits |        V |        V |        V |       V |       V |       V |       V |           V |
| Liste produits     |        V |        V |        V |       V |       V |       V |       V |           V |
| Détail produit     |        V |        V |        V |       V |       V |       V |       V |           V |
| Gestion produits   |        — |  V/C/M/S |     V/M* |       — |       — |       V | V/C/M/S |     V/C/M/S |
| Panier             |    V/C/M |        — |        — |       V |       — |       — |       V |           V |
| Checkout           |      V/A |        — |        — |       V |       — |       — |       V |           V |
| Paiement           |      V/A |        V |        V |       V |     V/A |       V |     V/A |         V/A |
| Commandes client   |        V |        — |        — |       V |       V |       V |       V |           V |
| Commandes marchand |        — |    V/M/A |    V/M/A |       V |       V |       V |       V |           V |
| Clients            |        — |        V |        V |     V/M |       V |       V |       V |           V |
| Wallet             |      V/A |        V |        V |       V |     V/A |       V |     V/A |         V/A |
| Transactions       |        V |        V |        V |       V |   V/M/A |       V |     V/A |         V/A |
| Fidélité           |      V/A |    V/M/A |      V/A |       V |       — |       V |     V/A |         V/A |
| Coupons            |      V/A |  V/C/M/S |      V/A |       V |       — |       V | V/C/M/S |     V/C/M/S |
| Promotions         |        V |  V/C/M/S |      V/A |       V |       — |       V | V/C/M/S |     V/C/M/S |
| Rapports           |       V* |        V |       V* |      V* |     V/C |       V |       V |           V |
| Utilisateurs       |        — |        — |        — |      V* |       — |       V | V/C/M/S |     V/C/M/S |
| Marchands          |        — |       V* |        — |    V/M* |       V |       V | V/C/M/S |     V/C/M/S |
| Administration     |        — |        — |        — |       — |       — |       — |       V |           V |
| Audit              |        — |        — |        — |      V* |      V* |       V |       V |           V |
| Configuration      |        — |        — |        — |       — |       — |       — |     V/M |         V/M |
| Sécurité           |       V* |       V* |       V* |      V* |      V* |       V |     V/M |         V/M |
| API / intégrations |        — |       V* |        — |       — |      V* |       V |     V/M |         V/M |
| Système            |        — |        — |        — |       — |       — |      V* |       V |           V |

`*` = accès limité au périmètre autorisé.

---

# 130. Matrice détaillée des écrans Customer

| Écran              | Route indicative       | Permission minimale            |
| ------------------ | ---------------------- | ------------------------------ |
| Dashboard          | `/dashboard`           | `DASHBOARD_VIEW`               |
| Profil             | `/profile`             | `PROFILE_VIEW`                 |
| Modifier profil    | `/profile/edit`        | `PROFILE_UPDATE`               |
| Sécurité           | `/profile/security`    | `SECURITY_SELF_MANAGE`         |
| Produits           | `/products`            | `PRODUCT_VIEW`                 |
| Produit            | `/products/:id`        | `PRODUCT_VIEW`                 |
| Panier             | `/cart`                | `CART_VIEW`                    |
| Checkout           | `/checkout`            | `ORDER_CREATE`                 |
| Commandes          | `/orders`              | `ORDER_SELF_VIEW`              |
| Commande           | `/orders/:id`          | `ORDER_SELF_VIEW`              |
| Paiement           | `/payments/:id`        | `PAYMENT_SELF_VIEW`            |
| Wallet             | `/wallet`              | `WALLET_SELF_VIEW`             |
| Transaction wallet | `/wallet/transactions` | `WALLET_TRANSACTION_SELF_VIEW` |
| Fidélité           | `/loyalty`             | `LOYALTY_SELF_VIEW`            |
| Récompenses        | `/loyalty/rewards`     | `LOYALTY_REWARD_VIEW`          |
| Notifications      | `/notifications`       | `NOTIFICATION_SELF_VIEW`       |

---

# 131. Permissions Customer

```text
DASHBOARD_VIEW

PROFILE_VIEW
PROFILE_UPDATE
SECURITY_SELF_MANAGE

PRODUCT_VIEW

CART_VIEW
CART_UPDATE

ORDER_CREATE
ORDER_SELF_VIEW
ORDER_SELF_CANCEL

PAYMENT_SELF_VIEW
PAYMENT_SELF_CREATE

WALLET_SELF_VIEW
WALLET_TRANSACTION_SELF_VIEW
WALLET_SELF_TOPUP
WALLET_SELF_TRANSFER

LOYALTY_SELF_VIEW
LOYALTY_REWARD_VIEW
LOYALTY_REWARD_REDEEM

NOTIFICATION_SELF_VIEW
NOTIFICATION_SELF_UPDATE
```

---

# 132. Matrice Merchant

| Écran                    | Permission                  |
| ------------------------ | --------------------------- |
| Merchant Dashboard       | `MERCHANT_DASHBOARD_VIEW`   |
| Profil marchand          | `MERCHANT_PROFILE_VIEW`     |
| Modifier profil          | `MERCHANT_PROFILE_UPDATE`   |
| Produits                 | `PRODUCT_MANAGE`            |
| Créer produit            | `PRODUCT_CREATE`            |
| Modifier produit         | `PRODUCT_UPDATE`            |
| Désactiver produit       | `PRODUCT_DEACTIVATE`        |
| Commandes                | `ORDER_MERCHANT_VIEW`       |
| Commande                 | `ORDER_MERCHANT_VIEW`       |
| Modifier statut commande | `ORDER_STATUS_UPDATE`       |
| Clients                  | `CUSTOMER_MERCHANT_VIEW`    |
| Paiements                | `PAYMENT_MERCHANT_VIEW`     |
| Transactions             | `TRANSACTION_MERCHANT_VIEW` |
| Fidélité                 | `LOYALTY_MERCHANT_MANAGE`   |
| Coupons                  | `COUPON_MANAGE`             |
| Promotions               | `PROMOTION_MANAGE`          |
| Rapports                 | `REPORT_MERCHANT_VIEW`      |

---

# 133. Permissions Merchant

```text
MERCHANT_DASHBOARD_VIEW

MERCHANT_PROFILE_VIEW
MERCHANT_PROFILE_UPDATE

PRODUCT_VIEW
PRODUCT_CREATE
PRODUCT_UPDATE
PRODUCT_DEACTIVATE

ORDER_MERCHANT_VIEW
ORDER_STATUS_UPDATE

CUSTOMER_MERCHANT_VIEW

PAYMENT_MERCHANT_VIEW
TRANSACTION_MERCHANT_VIEW

LOYALTY_MERCHANT_VIEW
LOYALTY_MERCHANT_MANAGE

COUPON_VIEW
COUPON_CREATE
COUPON_UPDATE
COUPON_DEACTIVATE

PROMOTION_VIEW
PROMOTION_CREATE
PROMOTION_UPDATE
PROMOTION_DEACTIVATE

REPORT_MERCHANT_VIEW
```

---

# 134. Merchant Operator

`MERCHANT_OPERATOR` dispose uniquement des permissions explicitement attribuées par le marchand.

Exemple de profil standard :

```text
DASHBOARD_VIEW

PRODUCT_VIEW
PRODUCT_UPDATE

ORDER_MERCHANT_VIEW
ORDER_STATUS_UPDATE

CUSTOMER_MERCHANT_VIEW

PAYMENT_MERCHANT_VIEW

LOYALTY_MERCHANT_VIEW
LOYALTY_REWARD_REDEEM

REPORT_LIMITED_VIEW
```

Par défaut :

```text
PRODUCT_CREATE       = NON
PRODUCT_DELETE       = NON
PAYMENT_REFUND       = NON
MERCHANT_SETTINGS    = NON
USER_ROLE_MANAGE     = NON
```

---

# 135. Support

Le rôle `SUPPORT` dispose d'un accès limité aux informations nécessaires au traitement des demandes.

```text
SUPPORT_DASHBOARD_VIEW

CUSTOMER_VIEW
CUSTOMER_SUPPORT_VIEW

ORDER_SUPPORT_VIEW
PAYMENT_SUPPORT_VIEW

WALLET_SUPPORT_VIEW

TICKET_VIEW
TICKET_CREATE
TICKET_UPDATE

NOTIFICATION_SUPPORT_VIEW

AUDIT_SUPPORT_VIEW
```

Le support ne doit pas pouvoir :

```text
modifier un solde wallet
effectuer librement un remboursement
modifier une transaction
modifier les rôles
modifier la configuration système
```

Les opérations sensibles doivent être transmises à un rôle autorisé.

---

# 136. Finance

Permissions principales :

```text
FINANCE_DASHBOARD_VIEW

PAYMENT_VIEW
PAYMENT_RECONCILE
PAYMENT_VALIDATE

TRANSACTION_VIEW
TRANSACTION_RECONCILE

REFUND_VIEW
REFUND_CREATE
REFUND_VALIDATE

WALLET_TRANSACTION_VIEW

FINANCIAL_REPORT_VIEW
FINANCIAL_REPORT_EXPORT

SETTLEMENT_VIEW
SETTLEMENT_VALIDATE
```

Les permissions financières doivent être séparées des permissions administratives.

---

# 137. Auditor

L'auditeur dispose principalement d'un accès en lecture.

```text
AUDIT_DASHBOARD_VIEW

USER_AUDIT_VIEW
MERCHANT_AUDIT_VIEW

ORDER_AUDIT_VIEW
PAYMENT_AUDIT_VIEW
WALLET_AUDIT_VIEW

SECURITY_AUDIT_VIEW

AUDIT_LOG_VIEW
AUDIT_LOG_EXPORT

REPORT_AUDIT_VIEW
REPORT_AUDIT_EXPORT
```

Par défaut :

```text
CREATE = NON
UPDATE = NON
DELETE = NON
FINANCIAL_ACTION = NON
ROLE_CHANGE = NON
```

---

# 138. Admin

L'administrateur peut gérer les fonctions opérationnelles selon ses permissions.

```text
ADMIN_DASHBOARD_VIEW

USER_VIEW
USER_CREATE
USER_UPDATE
USER_DISABLE

ROLE_VIEW
ROLE_ASSIGN
ROLE_UPDATE

MERCHANT_VIEW
MERCHANT_CREATE
MERCHANT_UPDATE
MERCHANT_DISABLE

PRODUCT_VIEW
PRODUCT_CREATE
PRODUCT_UPDATE
PRODUCT_DISABLE

ORDER_VIEW
ORDER_UPDATE

PAYMENT_VIEW
PAYMENT_ACTION

WALLET_VIEW
WALLET_ACTION

LOYALTY_MANAGE

COUPON_MANAGE
PROMOTION_MANAGE

REPORT_VIEW
REPORT_EXPORT

AUDIT_VIEW

SYSTEM_CONFIGURATION_VIEW
SYSTEM_CONFIGURATION_UPDATE
```

---

# 139. Super Admin

`SUPER_ADMIN` possède les permissions système les plus élevées.

```text
Toutes les permissions ADMIN

+

SYSTEM_CONFIGURATION_UPDATE
SECURITY_POLICY_MANAGE
ROLE_MANAGE
PERMISSION_MANAGE
TENANT_MANAGE
INTEGRATION_MANAGE
API_CONFIGURATION_MANAGE
AUDIT_CONFIGURATION_MANAGE
```

Même le `SUPER_ADMIN` doit être soumis à :

```text
Audit
MFA
Réauthentification
Journalisation
```

pour les opérations critiques.

---

# 140. Matrice des actions critiques

| Action                         | Customer | Merchant | Operator | Support | Finance | Auditor | Admin | Super Admin |
| ------------------------------ | -------: | -------: | -------: | ------: | ------: | ------: | ----: | ----------: |
| Créer commande                 |        ✓ |        — |        — |       — |       — |       — |    ✓* |           ✓ |
| Annuler commande               |       ✓* |       ✓* |       ✓* |       — |       — |       — |     ✓ |           ✓ |
| Effectuer paiement             |        ✓ |        — |        — |       — |      ✓* |       — |    ✓* |          ✓* |
| Rembourser                     |        — |        — |        — |       — |       ✓ |       — |    ✓* |           ✓ |
| Modifier wallet                |        — |        — |        — |       — |      ✓* |       — |    ✓* |           ✓ |
| Transférer wallet              |       ✓* |       ✓* |        — |       — |      ✓* |       — |    ✓* |           ✓ |
| Modifier produit               |        — |        ✓ |       ✓* |       — |       — |       — |     ✓ |           ✓ |
| Supprimer produit              |        — |       ✓* |        — |       — |       — |       — |    ✓* |           ✓ |
| Changer rôle                   |        — |        — |        — |       — |       — |       — |    ✓* |           ✓ |
| Modifier permission            |        — |        — |        — |       — |       — |       — |     — |           ✓ |
| Modifier configuration système |        — |        — |        — |       — |       — |       — |    ✓* |           ✓ |
| Consulter audit                |        — |        — |        — |      ✓* |      ✓* |       ✓ |     ✓ |           ✓ |

`*` = soumis à permissions supplémentaires, périmètre ou validation.

---

# 141. Permissions par domaine

Les permissions doivent être organisées par domaine afin d'éviter une liste plate difficile à maintenir.

```text
AUTH
USER
ROLE
PERMISSION

CUSTOMER
MERCHANT

PRODUCT
CATEGORY
INVENTORY

CART
ORDER

PAYMENT
REFUND
SETTLEMENT

WALLET
WALLET_TRANSACTION

LOYALTY
REWARD
COUPON
PROMOTION

NOTIFICATION

REPORT
AUDIT

SYSTEM
INTEGRATION
SECURITY
```

---

# 142. Convention de nommage

Format recommandé :

```text
<DOMAINE>_<ACTION>
```

Exemples :

```text
PRODUCT_VIEW
PRODUCT_CREATE
PRODUCT_UPDATE
PRODUCT_DELETE

ORDER_VIEW
ORDER_CREATE
ORDER_UPDATE
ORDER_CANCEL

PAYMENT_VIEW
PAYMENT_CREATE
PAYMENT_REFUND

USER_VIEW
USER_CREATE
USER_UPDATE
USER_DISABLE
```

Pour les ressources personnelles :

```text
ORDER_SELF_VIEW
PAYMENT_SELF_VIEW
WALLET_SELF_VIEW
PROFILE_SELF_UPDATE
```

---

# 143. Permission vs Role

Le frontend ne doit pas coder :

```text
if role == "ADMIN"
```

pour chaque bouton.

Préférer :

```text
if hasPermission("PRODUCT_CREATE")
```

Le rôle sert à déterminer les permissions.

La permission détermine l'accès à l'action.

---

# 144. Route Guards

Chaque route protégée doit définir sa permission minimale.

Exemple conceptuel :

```text
/orders
    requiredPermission:
    ORDER_SELF_VIEW | ORDER_MERCHANT_VIEW
```

```text
/admin/users
    requiredPermission:
    USER_VIEW
```

```text
/admin/users/create
    requiredPermission:
    USER_CREATE
```

---

# 145. Action Guards

Une page peut être accessible sans que toutes ses actions le soient.

Exemple :

```text
Page Produit
    │
    ├── Voir          PRODUCT_VIEW
    ├── Modifier      PRODUCT_UPDATE
    ├── Désactiver    PRODUCT_DEACTIVATE
    └── Supprimer     PRODUCT_DELETE
```

Le bouton correspondant ne doit être affiché que si la permission est présente.

---

# 146. Backend Authority

Architecture obligatoire :

```text
Frontend
   │
   │ Permission UX
   ▼
API
   │
   │ Authorization
   ▼
Backend
   │
   ▼
Database
```

La décision finale appartient au backend.

Un utilisateur ne doit jamais pouvoir obtenir un droit simplement en modifiant le JavaScript ou une requête HTTP.

---

# 147. Tenant / Merchant Scope

Lorsqu'un utilisateur appartient à un marchand :

```text
MERCHANT_ID
```

doit être contrôlé côté backend.

Un utilisateur du marchand A ne doit jamais pouvoir consulter les données du marchand B simplement en modifiant :

```text
/merchants/{id}
```

ou :

```text
?merchantId=...
```

---

# 148. Resource Ownership

Pour les ressources personnelles :

```text
CUSTOMER
   ↓
ORDER
   ↓
CUSTOMER_ID
```

Le backend doit vérifier que :

```text
authenticatedUser.id
==
order.customerId
```

avant d'autoriser l'accès.

---

# 149. Permission Evaluation

Ordre recommandé :

```text
Authentication
      ↓
User active ?
      ↓
Role / Permission
      ↓
Tenant scope
      ↓
Resource ownership
      ↓
Business rules
      ↓
Action
      ↓
Audit
```

---

# 150. UI Decision Tree

Hermes doit appliquer :

```text
Utilisateur authentifié ?
        │
       NON
        ↓
      Login

       OUI
        ↓
Permission présente ?
        │
      NON ─────→ Masquer / 403
        │
       OUI
        ↓
Scope autorisé ?
        │
      NON ─────→ 403
        │
       OUI
        ↓
Afficher écran/action
```

---

# 151. Matrice minimale pour les tests

Chaque combinaison importante doit être testée.

Exemple :

```text
CUSTOMER → PRODUCT_VIEW       = ALLOW
CUSTOMER → PRODUCT_CREATE     = DENY

MERCHANT → PRODUCT_VIEW       = ALLOW
MERCHANT → PRODUCT_CREATE     = ALLOW

OPERATOR → PRODUCT_UPDATE     = selon permission
OPERATOR → PRODUCT_DELETE     = DENY par défaut

SUPPORT → CUSTOMER_VIEW       = ALLOW
SUPPORT → WALLET_MODIFY       = DENY

FINANCE → PAYMENT_RECONCILE   = ALLOW
FINANCE → ROLE_MANAGE         = DENY

AUDITOR → AUDIT_LOG_VIEW      = ALLOW
AUDITOR → USER_UPDATE         = DENY

ADMIN → USER_UPDATE           = selon permission
ADMIN → PERMISSION_MANAGE     = DENY par défaut

SUPER_ADMIN → PERMISSION_MANAGE = ALLOW
```

---

# 152. Règle de sécurité finale

Les règles suivantes sont obligatoires :

```text
UI visibility ≠ Security
Role ≠ Permission
Permission ≠ Ownership
Authentication ≠ Authorization
```

Le système doit vérifier les quatre niveaux lorsque nécessaire :

```text
1. Authentification
2. Permission
3. Scope
4. Ownership
```

---

# 153. Référence pour Hermes

Avant de créer une nouvelle page, Hermes doit obligatoirement définir :

```text
Screen
Route
Actor
Required permission
Allowed actions
Forbidden actions
Resource scope
Loading state
Empty state
Error state
Success state
Mobile behavior
Audit requirement
```

Aucune nouvelle fonctionnalité UI ne doit contourner cette matrice.

---

# 154. Relation avec les documents d'architecture

Cette matrice doit rester cohérente avec :

```text
04_DataModel.md
        ↓
05_API_Specification.md
        ↓
06_SecurityArchitecture.md
        ↓
07_UI_UX_Specification.md
        ↓
Tests
```

En cas de divergence :

1. identifier la source du conflit ;
2. ne pas inventer une règle ;
3. documenter la décision ;
4. mettre à jour les documents concernés ;
5. ajouter ou modifier les tests correspondants.

---

# 155. Statut

**Cette matrice est la référence UI/UX des accès fonctionnels MAGIC GOLD.**

Toute nouvelle fonctionnalité doit être ajoutée à :

```text
Écran
→ Route
→ Permission
→ Rôle
→ Scope
→ Action
→ Test
```

avant son implémentation finale.
