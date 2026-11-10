# MAGIC GOLD — Deployment Guide

**Document :** `17_Deployment.md`
**Version :** 1.0
**Statut :** Référence de déploiement
**Éditeur :** Nectux Ingénieries
**Projet :** MAGIC GOLD

---

# 1. Objet

Ce document définit la méthode de déploiement et d'exploitation de MAGIC GOLD.

Il couvre :

* les environnements (DEV / STAGING / PRODUCTION) ;
* la topologie Docker Compose (backend Spring Boot, PostgreSQL 15, Redis 7, Keycloak, reverse proxy Nginx) ;
* les variables d'environnement et la gestion des secrets (`.env`) ;
* TLS/HTTPS et les certificats ;
* les sauvegardes et la restauration PostgreSQL ;
* les migrations de schéma (Flyway) ;
* le pipeline CI/CD ;
* le monitoring et les logs ;
* le durcissement sécurité.

Ce document est **directement actionnable** : chaque section fournit les chemins, commandes et exemples de configuration à appliquer.

**Références de cohérence :** `00_Master.md` (autorité suprême), `03_TechnicalArchitecture.md`, `05_API_Specification.md`, `06_SecurityArchitecture.md`, `16_RedisSpecification.md`.

> Règle absolue : la stack imposée par le Master (§5) est **Java 17, Spring Boot 3.5.x, modular monolith, PostgreSQL 15, Redis 7, Keycloak OAuth2/OIDC, Flutter mobile, Docker Compose, monnaie XOF, Merchant = Tenant**. Aucun déploiement ne doit s'en écarter.

---

# 2. Principes de déploiement

```text
SIMPLE AU DÉMARRAGE
        +
REPRODUCTIBLE (IaC / Compose / versions épinglées)
        +
SÉCURISÉ (secrets hors Git, TLS, moindre privilège)
        +
OBSERVABLE (logs, métriques, alertes)
        +
RESTAURABLE (sauvegardes testées)
        =
DÉPLOIEMENT MAGIC GOLD
```

Règles non négociables :

1. **Un seul artefact backend** : le modular monolith est livré comme un conteneur unique `magic-gold-api`. Pas de microservices en V1.
2. **Source de vérité = PostgreSQL**. Redis est un cache/temporaire (cf. `16_RedisSpecification.md`). Sa perte ne doit pas entraîner de perte métier.
3. **Secrets jamais dans Git** : tout secret passe par `.env` (ignoré) ou un secret manager. Les variables non secrètes passent par `application.yml` profilé ou `docker-compose.yml`.
4. **HTTPS obligatoire** en production. HTTP est redirigé ou bloqué.
5. **Compte applicatif dédié** en base : `magicgold_app`, jamais le superuser `postgres`.
6. **Une sauvegarde non testée n'est pas une sauvegarde.**
7. **Chaque environnement est isolé** : base, realm Keycloak, credentials de paiement et certificats distincts.

---

# 3. Environnements

## 3.1 Les trois environnements obligatoires

| Environnement | Objectif | Données | Paiement | Accès |
|---|---|---|---|---|
| **DEV** | Développement local, itération rapide | Données factices, resets fréquents | Mode `TEST` / MockPaymentAdapter | Équipe uniquement |
| **STAGING** | Tests, intégrations paiement, validation métier, démos | Données anonymisées ou de test | Mode `TEST` des providers | Équipe + testeurs + partenaires |
| **PRODUCTION** | Commerces réels | Données réelles | Mode `PRODUCTION` | Utilisateurs finaux |

## 3.2 Séparation stricte

```text
DEV        STAGING        PRODUCTION
  │            │              │
  ├── base PG dev        ├── base PG staging       ├── base PG prod
  ├── realm Keycloak dev ├── realm Keycloak staging├── realm Keycloak prod
  ├── .env.dev           ├── .env.staging          ├── .env.prod
  └── PAYMENT_ENV=TEST   └── PAYMENT_ENV=TEST      └── PAYMENT_ENV=PRODUCTION
```

Règles :

* Ne jamais utiliser les credentials production en dev ou staging.
* Ne jamais utiliser les credentials de paiement de test en production (`PAYMENT_ENVIRONMENT` explicite, cf. `06_SecurityArchitecture.md` §65).
* Les domaines publics sont distincts :

```text
DEV        https://api-dev.magicgold.example
STAGING    https://api-staging.magicgold.example
PRODUCTION https://api.magicgold.example
```

* Le frontend/mobile ne code jamais l'URL de production en dur ; elle est injectée par configuration de build.

## 3.3 Arborescence de déploiement

```text
magic-gold/
├── deploy/
│   ├── docker-compose.yml          # composition de référence (paramétrée)
│   ├── docker-compose.prod.yml     # surcharges production (limites, restart, logging)
│   ├── .env.example                # modèle de variables (committé, sans secrets)
│   ├── nginx/
│   │   ├── nginx.conf              # reverse proxy + TLS
│   │   └── conf.d/magic-gold.conf  # vhosts par domaine
│   ├── postgres/
│   │   └── init/                   # scripts d'init (rôles, extensions)
│   ├── keycloak/
│   │   └── realm/magic-gold.json   # export du realm (hors secrets)
│   └── backups/                    # cible locale des dumps (hors Git)
├── .github/workflows/
│   ├── ci.yml                      # build + tests + scan
│   └── deploy.yml                  # déploiement par environnement
└── backend/
    └── src/main/resources/db/migration/   # scripts Flyway
```

---

# 4. Topologie Docker Compose

## 4.1 Schéma de déploiement

```text
                            INTERNET
                               │
                         80 / 443 (TLS)
                               │
                    ┌──────────▼──────────┐
                    │  nginx  (reverse    │
                    │  proxy / TLS / WAF  │
                    └──────────┬──────────┘
                               │  (réseau interne uniquement)
            ┌──────────────────┼───────────────────┐
            │                  │                   │
   ┌────────▼────────┐  ┌──────▼──────┐   ┌────────▼────────┐
   │  magic-gold-api │  │  keycloak   │   │  (webhook       │
   │  Spring Boot    │  │  IAM/OIDC   │   │   providers)    │
   └────────┬────────┘  └─────────────┘   └─────────────────┘
            │
   ┌────────┴─────────┐
   │                  │
┌──▼─────────┐  ┌─────▼──────┐
│ postgres:15│  │  redis:7   │
│ (source de │  │ (cache/OTP/│
│  vérité)   │  │ rate-limit)│
└────────────┘  └────────────┘
```

**Réseaux :**

* `edge` : nginx ↔ API, nginx ↔ Keycloak (et nginx ↔ Internet).
* `internal` : API ↔ PostgreSQL, API ↔ Redis, API ↔ Keycloak (admin REST), Keycloak ↔ PostgreSQL.

PostgreSQL et Redis ne sont **jamais** exposés sur un port public. Aucun `ports:` vers l'extérieur pour `postgres`, `redis` ni l'interface d'admin Keycloak en production.

## 4.2 `deploy/docker-compose.yml`

```yaml
name: magic-gold

services:
  postgres:
    image: postgres:15-alpine
    container_name: mg-postgres
    restart: unless-stopped
    environment:
      POSTGRES_DB: ${POSTGRES_DB:-magicgold}
      POSTGRES_USER: ${POSTGRES_USER:-magicgold_app}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:?POSTGRES_PASSWORD requis}
      PGDATA: /var/lib/postgresql/data/pgdata
    volumes:
      - pg_data:/var/lib/postgresql/data
      - ./postgres/init:/docker-entrypoint-initdb.d:ro
    networks:
      - internal
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER:-magicgold_app} -d ${POSTGRES_DB:-magicgold}"]
      interval: 10s
      timeout: 5s
      retries: 5
    # Aucune directive ports: vers l'extérieur.

  redis:
    image: redis:7-alpine
    container_name: mg-redis
    restart: unless-stopped
    command: >
      redis-server
      --requirepass ${REDIS_PASSWORD:?REDIS_PASSWORD requis}
      --maxmemory ${REDIS_MAXMEMORY:-512mb}
      --maxmemory-policy allkeys-lru
      --appendonly yes
    volumes:
      - redis_data:/data
    networks:
      - internal
    healthcheck:
      test: ["CMD-SHELL", "redis-cli -a \"$${REDIS_PASSWORD}\" ping | grep PONG"]
      interval: 10s
      timeout: 5s
      retries: 5
    # Aucune directive ports: vers l'extérieur.

  keycloak:
    image: quay.io/keycloak/keycloak:25.0
    container_name: mg-keycloak
    restart: unless-stopped
    command:
      - start
      - --proxy-headers=xforwarded
      - --hostname=${KC_HOSTNAME}
      - --https-port=8443
    environment:
      KC_DB: postgres
      KC_DB_URL: jdbc:postgresql://postgres:5432/${KC_DB_NAME:-keycloak}
      KC_DB_USERNAME: ${KC_DB_USER:-keycloak}
      KC_DB_PASSWORD: ${KC_DB_PASSWORD:?KC_DB_PASSWORD requis}
      KEYCLOAK_ADMIN: ${KC_ADMIN_USER:-admin}
      KEYCLOAK_ADMIN_PASSWORD: ${KC_ADMIN_PASSWORD:?KC_ADMIN_PASSWORD requis}
      KC_HOSTNAME_STRICT: "false"
      KC_PROXY: edge
    volumes:
      - keycloak_data:/opt/keycloak/data
    networks:
      - internal
      - edge
    depends_on:
      postgres:
        condition: service_healthy
    healthcheck:
      test: ["CMD-SHELL", "exec 3<>/dev/tcp/127.0.0.1/8080 && echo -e 'GET /health/ready HTTP/1.1\\r\\nHost: localhost\\r\\n\\r\\n' >&3 && grep -q '\"status\": \"UP\"' <&3"]
      interval: 15s
      timeout: 5s
      retries: 10

  api:
    image: ${API_IMAGE:-registry.example.com/nectux/magic-gold-api:latest}
    container_name: mg-api
    restart: unless-stopped
    environment:
      SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE:-prod}
      # Base de données
      DB_URL: jdbc:postgresql://postgres:5432/${POSTGRES_DB:-magicgold}
      DB_USERNAME: ${POSTGRES_USER:-magicgold_app}
      DB_PASSWORD: ${POSTGRES_PASSWORD}
      # Redis
      REDIS_HOST: redis
      REDIS_PORT: "6379"
      REDIS_PASSWORD: ${REDIS_PASSWORD}
      # Keycloak / OIDC
      OIDC_ISSUER_URI: https://${KC_HOSTNAME}/realms/${KC_REALM:-magic-gold}
      OIDC_CLIENT_ID: ${OIDC_CLIENT_ID:-magicgold-api}
      OIDC_CLIENT_SECRET: ${OIDC_CLIENT_SECRET}
      # Paiement
      PAYMENT_ENVIRONMENT: ${PAYMENT_ENVIRONMENT:-TEST}
      # Divers
      APP_BASE_URL: ${APP_BASE_URL}
      JAVA_OPTS: ${JAVA_OPTS:--Xms512m -Xmx1g}
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy
    networks:
      - internal
      - edge
    healthcheck:
      test: ["CMD-SHELL", "wget -qO- http://127.0.0.1:8080/actuator/health/readiness | grep -q '\"status\":\"UP\"' || exit 1"]
      interval: 15s
      timeout: 5s
      retries: 10

  nginx:
    image: nginx:1.27-alpine
    container_name: mg-nginx
    restart: unless-stopped
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./nginx/conf.d:/etc/nginx/conf.d:ro
      - certbot_www:/var/www/certbot:ro
      - certbot_conf:/etc/letsencrypt:ro
    networks:
      - edge
    depends_on:
      - api
      - keycloak

  certbot:
    image: certbot/certbot:latest
    container_name: mg-certbot
    volumes:
      - certbot_www:/var/www/certbot
      - certbot_conf:/etc/letsencrypt
    entrypoint: "/bin/sh -c 'trap exit TERM; while :; do certbot renew; sleep 12h & wait $${!}; done;'"
    networks:
      - edge

volumes:
  pg_data:
  redis_data:
  keycloak_data:
  certbot_www:
  certbot_conf:

networks:
  edge:
  internal:
```

## 4.3 `deploy/docker-compose.prod.yml` (surcharge production)

```yaml
services:
  api:
    image: ${API_IMAGE}
    deploy:
      resources:
        limits:
          cpus: "2.0"
          memory: 2g
    logging:
      driver: json-file
      options:
        max-size: "20m"
        max-file: "5"

  postgres:
    logging:
      driver: json-file
      options:
        max-size: "20m"
        max-file: "5"

  redis:
    logging:
      driver: json-file
      options:
        max-size: "10m"
        max-file: "3"
```

---

# 5. Variables d'environnement et gestion des secrets

## 5.1 Principe

```text
Non-secret   → docker-compose.yml / application.yml profilé (committé)
Secret       → .env (IGNORÉ par Git) ou secret manager / Vault / Docker secret
```

`06_SecurityArchitecture.md` §61-62 impose : les secrets (mot de passe DB, secret client Keycloak, clés API paiement, secrets webhook, credentials SMTP, clés de signature JWT, clés de chiffrement) ne figurent **jamais** dans le code source ni dans Git.

## 5.2 `deploy/.env.example` (modèle committé — sans valeurs réelles)

```bash
# ============================================================
# MAGIC GOLD — Variables d'environnement (modèle)
# Copier vers .env et renseigner des valeurs réelles.
# .env ne doit JAMAIS être committé.
# ============================================================

# --- Domaine public / TLS ---
APP_BASE_URL=https://api.magicgold.example
KC_HOSTNAME=auth.magicgold.example
LETSENCRYPT_EMAIL=ops@nectux.example

# --- PostgreSQL ---
POSTGRES_DB=magicgold
POSTGRES_USER=magicgold_app
POSTGRES_PASSWORD=CHANGE_ME_STRONG_PASSWORD

# --- Keycloak (base dédiée) ---
KC_DB_NAME=keycloak
KC_DB_USER=keycloak
KC_DB_PASSWORD=CHANGE_ME_STRONG_PASSWORD
KC_ADMIN_USER=admin
KC_ADMIN_PASSWORD=CHANGE_ME_STRONG_PASSWORD
KC_REALM=magic-gold

# --- Redis ---
REDIS_PASSWORD=CHANGE_ME_STRONG_PASSWORD
REDIS_MAXMEMORY=512mb

# --- OIDC (client backend) ---
OIDC_CLIENT_ID=magicgold-api
OIDC_CLIENT_SECRET=CHANGE_ME_CLIENT_SECRET

# --- Paiement (TEST | PRODUCTION) ---
PAYMENT_ENVIRONMENT=TEST
PAYMENT_PROVIDER_CINETPAY_API_KEY=
PAYMENT_PROVIDER_CINETPAY_SITE_ID=
PAYMENT_PROVIDER_CINETPAY_SECRET_KEY=
PAYMENT_PROVIDER_FEEXPAY_API_KEY=
PAYMENT_WEBHOOK_SECRET=

# --- Image déployée ---
API_IMAGE=registry.example.com/nectux/magic-gold-api:1.0.0

# --- Runtime JVM ---
JAVA_OPTS=-Xms512m -Xmx1g

# --- Profil Spring ---
SPRING_PROFILES_ACTIVE=prod
```

## 5.3 Règles de manipulation

```bash
# Copier le modèle (une fois par environnement)
cp deploy/.env.example deploy/.env

# Restreindre les permissions (lecture par le propriétaire uniquement)
chmod 600 deploy/.env

# Vérifier que .env est bien ignoré
grep -n ".env" .gitignore || echo ".env" >> .gitignore
```

> Un `.gitignore` de projet doit contenir au minimum : `.env`, `deploy/.env`, `*.pem`, `*.key`, `backups/`, `certs/`.

## 5.4 Validation au démarrage

Le backend doit **refuser de démarrer** en production si un secret critique est absent ou trivial :

```yaml
# backend/src/main/resources/application-prod.yml (extrait)
spring:
  config:
    import: optional:file:.env[.properties]
```

En code, utiliser des propriétés `@Value("${DB_PASSWORD}")` et un `ApplicationRunner` qui vérifie :

```text
DB_PASSWORD présent et non vide
OIDC_CLIENT_SECRET présent
PAYMENT_ENVIRONMENT == PRODUCTION  → les clés provider sont présentes
REDIS_PASSWORD présent
```

En cas d'échec : `fail-fast` (le conteneur sort en erreur, l'orchestrateur le signale).

## 5.5 Mapping clair (secret vs config)

| Variable | Type | Emplacement |
|---|---|---|
| `POSTGRES_PASSWORD`, `REDIS_PASSWORD` | Secret | `.env` |
| `KC_ADMIN_PASSWORD`, `KC_DB_PASSWORD` | Secret | `.env` |
| `OIDC_CLIENT_SECRET`, `PAYMENT_*`, `PAYMENT_WEBHOOK_SECRET` | Secret | `.env` / secret manager |
| `POSTGRES_DB`, `POSTGRES_USER`, ports, noms de service | Non-secret | `docker-compose.yml` |
| `SPRING_PROFILES_ACTIVE`, timeouts, tailles de pool | Non-secret | `application-*.yml` |

---

# 6. TLS / HTTPS et certificats

## 6.1 Politique

* Production : **HTTPS obligatoire**. HTTP redirige vers HTTPS (301) ou est bloqué.
* TLS ≥ 1.2 (1.3 recommandé) ; versions obsolètes désactivées (`06_SecurityArchitecture.md` §54).
* Certificats : Let's Encrypt (ACME via certbot) pour l'usage courant ; certificats d'entreprise si exigence contractuelle.

## 6.2 Émission initiale du certificat

```bash
cd deploy

# Émission en mode webroot (nécessite nginx répondant sur 80)
docker compose run --rm certbot certonly \
  --webroot -w /var/www/certbot \
  -d api.magicgold.example \
  -d auth.magicgold.example \
  --email "${LETSENCRYPT_EMAIL}" \
  --agree-tos --no-eff-email
```

## 6.3 Renouvellement automatique

Le service `certbot` (voir `docker-compose.yml`) tourne en boucle et exécute `certbot renew` toutes les 12h, puis recharge Nginx :

```yaml
# Ajout dans le service certbot pour recharger nginx après renouvellement
command: >
  /bin/sh -c 'trap exit TERM; while :; do
    certbot renew --webroot -w /var/www/certbot --quiet;
    docker kill --signal=HUP mg-nginx || true;
    sleep 12h & wait $${!};
  done;'
```

> Astuce : monter la socket Docker dans certbot uniquement si l'on souhaite le rechargement automatique de Nginx (`/var/run/docker.sock:/var/run/docker.sock`). Sinon, planifier un `docker compose exec nginx nginx -s reload` en cron.

## 6.4 `deploy/nginx/conf.d/magic-gold.conf`

```nginx
# Redirection HTTP -> HTTPS
server {
    listen 80;
    server_name api.magicgold.example auth.magicgold.example;
    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }
    location / {
        return 301 https://$host$request_uri;
    }
}

# API
server {
    listen 443 ssl;
    http2 on;
    server_name api.magicgold.example;

    ssl_certificate     /etc/letsencrypt/live/api.magicgold.example/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/api.magicgold.example/privkey.pem;
    ssl_protocols       TLSv1.2 TLSv1.3;
    ssl_ciphers         HIGH:!aNULL:!MD5;

    # En-têtes de sécurité
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    add_header X-Content-Type-Options nosniff always;
    add_header X-Frame-Options DENY always;
    add_header Referrer-Policy strict-origin-when-cross-origin always;

    client_max_body_size 10m;

    location /api/ {
        proxy_pass http://api:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Request-ID $request_id;
    }

    # Endpoints techniques : limités à une IP d'administration
    location /actuator/ {
        allow 10.0.0.0/8;
        deny all;
        proxy_pass http://api:8080;
    }
}

# Keycloak
server {
    listen 443 ssl;
    http2 on;
    server_name auth.magicgold.example;

    ssl_certificate     /etc/letsencrypt/live/auth.magicgold.example/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/auth.magicgold.example/privkey.pem;
    ssl_protocols       TLSv1.2 TLSv1.3;

    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;

    location / {
        proxy_pass http://keycloak:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

## 6.5 Vérification TLS

```bash
# Vérifier le certificat servi
echo | openssl s_client -connect api.magicgold.example:443 -servername api.magicgold.example 2>/dev/null | openssl x509 -noout -dates -subject

# Vérifier le score TLS (à exécuter en recette)
# → objectif : grade A sur SSL Labs (https://www.ssllabs.com/ssltest/)
```

---

# 7. Migrations de schéma (Flyway)

## 7.1 Principe

Le schéma PostgreSQL est versionné et migré par **Flyway**, exécuté **au démarrage** de l'application Spring Boot (module `backend`).

```text
backend/src/main/resources/db/migration/
├── V1__init_schema.sql
├── V2__add_merchant_tables.sql
├── V3__add_order_payment.sql
└── ...
```

Règles :

* **Numérotation incrémentale immuable** : `V1__`, `V2__`, … Une fois appliquée, une migration ne doit plus être modifiée (sinon échec de checksum).
* Toute évolution de schéma = **nouvelle** migration, jamais un `UPDATE` de migration existante.
* Les migrations sont **idempotentes par conception** (Flyway garantit l'application unique).
* Ne pas utiliser `flyway.clean` en production.
* Les données financières ne doivent jamais être modifiées par une migration de type « reset ».

## 7.2 Configuration

```yaml
# backend/src/main/resources/application.yml (extrait)
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    validate-on-migrate: true
```

En production, préférer une stratégie explicite :

```yaml
# application-prod.yml
spring:
  flyway:
    baseline-on-migrate: false   # la base prod part toujours d'une base versionnée
    clean-disabled: true
    out-of-order: false
```

## 7.3 Lancement manuel (hors démarrage)

```bash
# Depuis le module backend, via Maven (utile en recette)
mvn -Dspring.profiles.active=staging flyway:migrate
```

## 7.4 Règles de migration en environnement

```text
DEV        → auto-migrate au démarrage (rapide)
STAGING    → auto-migrate + vérification du contenu (pré-prod)
PRODUCTION → migration planifiée pendant une fenêtre de maintenance,
             précédée d'un pg_dump de sécurité et d'un test sur staging
```

**Procédure de migration en production :**

```bash
# 1. Sauvegarde de sécurité
docker compose exec -T postgres pg_dump -U magicgold_app -d magicgold -Fc \
  > deploy/backups/pre-migration-$(date +%Y%m%d-%H%M%S).dump

# 2. Déploiement du nouvel artefact (Flyway s'exécute au démarrage)
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d api

# 3. Vérification
docker compose logs -f api | grep -i flyway
curl -fsS https://api.magicgold.example/actuator/health/readiness
```

---

# 8. Sauvegardes et restauration PostgreSQL

## 8.1 Politique de sauvegarde

Objectifs (cf. `06_SecurityArchitecture.md` §87-89) :

```text
RPO  ≤ 24 h   (perte maximale acceptée)
RTO  ≤ 4 h    (temps de restauration cible)
Fréquence     1 dump complet / jour + WAL archiving (si configuré)
Rétention     30 jours (quotidiens), 12 mensuels, 1 annuel
Stockage      hors serveur principal (S3/autre VPS), chiffré
```

Une sauvegarde doit être : **automatique, chiffrée, isolée, versionnée, testée**.

## 8.2 Script de sauvegarde `deploy/scripts/backup.sh`

```bash
#!/usr/bin/env bash
set -euo pipefail

# Usage : ./backup.sh  (à planifier en cron)
COMPOSE_DIR="$(cd "$(dirname "$0")/.." && pwd)"
BACKUP_DIR="${BACKUP_DIR:-$COMPOSE_DIR/backups}"
TS="$(date +%Y%m%d-%H%M%S)"
DUMP_FILE="${BACKUP_DIR}/magicgold-${TS}.dump"

mkdir -p "$BACKUP_DIR"

# 1. Dump format custom (compressé)
docker compose -f "$COMPOSE_DIR/docker-compose.yml" exec -T postgres \
  pg_dump -U "${POSTGRES_USER:-magicgold_app}" -d "${POSTGRES_DB:-magicgold}" -Fc \
  > "$DUMP_FILE"

# 2. Chiffrement de la sauvegarde (AES-256-GCM via openssl)
PASS_FILE="${BACKUP_DIR}/.backup-pass"   # généré une fois, hors Git
[ -f "$PASS_FILE" ] || openssl rand -base64 32 > "$PASS_FILE" && chmod 600 "$PASS_FILE"
openssl enc -aes-256-cbc -salt -pbkdf2 -pass file:"$PASS_FILE" \
  -in "$DUMP_FILE" -out "${DUMP_FILE}.enc"
rm -f "$DUMP_FILE"

# 3. Transfert hors serveur (exemple : rclone vers S3 compatible)
rclone copy "${DUMP_FILE}.enc" "s3:magicgold-backups/$(date +%Y/%m)/" || {
  echo "ÉCHEC du transfert hors serveur" >&2; exit 1; }

# 4. Purge locale selon rétention (30 jours)
find "$BACKUP_DIR" -name "*.enc" -mtime +30 -delete

echo "Sauvegarde OK : ${DUMP_FILE}.enc"
```

## 8.3 Planification cron (sur l'hôte)

```bash
# /etc/cron.d/magicgold-backup
# Sauvegarde quotidienne à 02h30 UTC (03h30 locale Abidjan)
30 2 * * * root /home/deploy/magic-gold/deploy/scripts/backup.sh >> /var/log/mg-backup.log 2>&1
```

## 8.4 Restauration `deploy/scripts/restore.sh`

```bash
#!/usr/bin/env bash
set -euo pipefail

# Usage : ./restore.sh <fichier.dump.enc>
COMPOSE_DIR="$(cd "$(dirname "$0")/.." && pwd)"
BACKUP_DIR="${BACKUP_DIR:-$COMPOSE_DIR/backups}"
PASS_FILE="${BACKUP_DIR}/.backup-pass"
ENC_FILE="$1"
DEC_FILE="${ENC_FILE%.enc}"

# 1. Déchiffrement
openssl enc -d -aes-256-cbc -pbkdf2 -pass file:"$PASS_FILE" \
  -in "$ENC_FILE" -out "$DEC_FILE"

# 2. Restauration
docker compose -f "$COMPOSE_DIR/docker-compose.yml" exec -T postgres \
  pg_restore -U "${POSTGRES_USER:-magicgold_app}" -d "${POSTGRES_DB:-magicgold}" \
  --clean --if-exists "$DEC_FILE"

rm -f "$DEC_FILE"
echo "Restauration terminée depuis : $ENC_FILE"
```

> En cas de restauration complète, arrêter l'API au préalable pour éviter les écritures concurrentes :
> `docker compose stop api`.

## 8.5 Test périodique de restauration (obligatoire)

Chaque trimestre (et avant tout changement majeur), restaurer un dump récent sur l'environnement **staging** et vérifier :

```text
[ ] le dump existe et est lisible (déchiffrement OK)
[ ] pg_restore s'exécute sans erreur
[ ] l'application redémarre sur la base restaurée
[ ] les données financières de référence sont cohérentes (comptages/checksums)
```

---

# 9. Pipeline CI/CD

## 9.1 Principes

```text
main (ou release/*)  →  CI : build + tests + scans  →  artefact taggé
                              │
                              ▼
                    STAGING (auto ou manuel)
                              │
                              ▼
                    PRODUCTION (approbation manuelle obligatoire)
```

* Aucun déploiement en production sans **validation humaine**.
* L'image Docker est **versionnée et immuable** (tag = version applicative, jamais `latest` en prod).
* Les secrets de déploiement vivent dans les *secrets* du CI, jamais dans le dépôt.

## 9.2 `backend/Dockerfile` (multi-stage)

```dockerfile
# ---- Build ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

# ---- Runtime ----
FROM eclipse-temurin:17-jre
RUN addgroup --system app && adduser --system --ingroup app app
WORKDIR /app
COPY --from=build /app/target/magic-gold-api.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

## 9.3 `.github/workflows/ci.yml`

```yaml
name: CI
on:
  push:
    branches: [main]
  pull_request:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'
          cache: maven

      - name: Build & tests
        run: mvn -B verify
        working-directory: backend

      - name: Scan dépendances (OWASP)
        run: mvn -B org.owasp:dependency-check-maven:check
        working-directory: backend

      - name: Scan secrets (gitleaks)
        uses: gitleaks/gitleaks-action@v2
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}

      - name: Build & push image
        if: github.ref == 'refs/heads/main'
        run: |
          docker build -t ${{ vars.REGISTRY }}/nectux/magic-gold-api:${{ github.sha }} backend/
          docker push ${{ vars.REGISTRY }}/nectux/magic-gold-api:${{ github.sha }}
        env:
          REGISTRY: ${{ vars.REGISTRY }}
```

## 9.4 `.github/workflows/deploy.yml`

```yaml
name: Deploy
on:
  push:
    tags: ['v*']

jobs:
  deploy-staging:
    runs-on: ubuntu-latest
    environment: staging
    steps:
      - uses: actions/checkout@v4
      - name: Deploy to staging
        uses: appleboy/ssh-action@v1
        with:
          host: ${{ secrets.STAGING_HOST }}
          username: deploy
          key: ${{ secrets.STAGING_SSH_KEY }}
          script: |
            cd /home/deploy/magic-gold
            API_IMAGE=${{ vars.REGISTRY }}/nectux/magic-gold-api:${{ github.ref_name }} \
              docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d api

  deploy-prod:
    runs-on: ubuntu-latest
    environment: production      # exige une approbation manuelle
    needs: deploy-staging
    steps:
      - uses: actions/checkout@v4
      - name: Deploy to production
        uses: appleboy/ssh-action@v1
        with:
          host: ${{ secrets.PROD_HOST }}
          username: deploy
          key: ${{ secrets.PROD_SSH_KEY }}
          script: |
            cd /home/deploy/magic-gold
            API_IMAGE=${{ vars.REGISTRY }}/nectux/magic-gold-api:${{ github.ref_name }} \
              docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d api
```

## 9.5 Rollback

```bash
# Revenir à la version précédente (image immuable taguée)
cd /home/deploy/magic-gold
API_IMAGE=registry.example.com/nectux/magic-gold-api:<VERSION_PRÉCÉDENTE> \
  docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d api
```

> Un rollback de schéma n'est pas automatique. Si la migration précédente a modifié le schéma de façon non rétro-compatible, restaurer d'abord la base (dump pré-migration) puis redéployer l'image précédente.

---

# 10. Monitoring et logs

## 10.1 Endpoints de santé (Spring Boot Actuator)

```text
GET /actuator/health            → état global
GET /actuator/health/liveness   → le process vit
GET /actuator/health/readiness  → prêt à servir (DB, Redis, Keycloak joignables)
GET /actuator/metrics           → métriques (protégé)
```

Ces endpoints ne doivent pas exposer de secrets ni de détail interne (`05_API_Specification.md` §73). En production, les exposer **uniquement** au reverse proxy interne / à l'IP d'administration (voir §6.4).

## 10.2 Métriques à surveiller (au minimum)

```text
CPU / RAM / Disk            (hôte + conteneurs)
API latency / error rate    (p95, p99, HTTP 5xx)
Database                    (connexions, locks, temps de requête)
Redis                       (mémoire, hit rate, latence)
Payment success/failure rate
Webhook processing          (retries, échecs de signature)
Sync error rate             (offline)
Taille des files            (outbox, sync)
Tenants actifs
```

## 10.3 Alertes critiques

```text
DATABASE_UNAVAILABLE
DISK_LOW
PAYMENT_FAILURE_RATE_HIGH
SYNC_FAILURE_RATE_HIGH
API_ERROR_RATE_HIGH
WEBHOOK_SIGNATURE_FAILURE (répété)
REDIS_UNAVAILABLE
```

Canal d'alerte recommandé : e-mail + Telegram/WhatsApp d'astreinte. Toute alerte financière (paiement) est prioritaire P0.

## 10.4 Stack de monitoring recommandée (léger, V1)

```yaml
# Compose additionnel (facultatif, environnement non productif en V1)
services:
  prometheus:
    image: prom/prometheus:latest
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml:ro
    ports: ["9090:9090"]

  grafana:
    image: grafana/grafana:latest
    environment:
      GF_SECURITY_ADMIN_PASSWORD: ${GRAFANA_ADMIN_PASSWORD}
    ports: ["3000:3000"]
```

Le backend expose les métriques via `micrometer-registry-prometheus` (`/actuator/prometheus`), protégé derrière le réseau interne.

## 10.5 Logs applicatifs

```yaml
# application-prod.yml (extrait)
logging:
  level:
    root: INFO
    com.nectux.magicgold: INFO
  pattern:
    console: "%d{ISO8601} %-5level [%thread] %logger{36} - %msg %X{requestId} %X{correlationId}%n"
```

* Chaque requête est corrélée via `X-Request-ID` / `X-Correlation-ID` (recommandé `%X{requestId}` dans le pattern).
* Les logs ne doivent **jamais** contenir : mot de passe, OTP, JWT complet, refresh token, secret API, clé privée, numéro de carte complet (`06_SecurityArchitecture.md` §50).
* Collecte centralisée : `docker logs` → rsyslog / Loki / Graylog selon l'outillage Nectux. Rotation via `logging` Docker (`max-size`, `max-file`).

## 10.6 Vérifications de routine

```bash
# Santé des services
docker compose ps
docker compose logs --tail=100 api

# Espace disque
df -h /var/lib/docker

# Dernière sauvegarde
ls -lt deploy/backups/ | head -5
```

---

# 11. Durcissement sécurité (checklist de mise en production)

## 11.1 Base de données

```text
[ ] PostgreSQL jamais exposé sur un port public (aucun ports: externe)
[ ] Compte applicatif dédié magicgold_app (pas superuser)
[ ] Mot de passe fort, rotation possible
[ ] Privilèges limités au schéma applicatif
[ ] Sauvegardes automatiques, chiffrées, hors serveur, testées
```

Script d'initialisation `deploy/postgres/init/01-init.sql` :

```sql
-- Exécuté au premier démarrage (docker-entrypoint-initdb.d)
CREATE ROLE magicgold_app LOGIN PASSWORD 'CHANGED_BY_ENV';
CREATE DATABASE magicgold OWNER magicgold_app;
REVOKE ALL ON DATABASE magicgold FROM PUBLIC;
```

## 11.2 Redis

```text
[ ] Redis jamais exposé publiquement
[ ] Authentification obligatoire (requirepass)
[ ] TTL sur les données temporaires sensibles (OTP, idempotency)
[ ] maxmemory + politique d'éviction allkeys-lru
```

## 11.3 Keycloak

```text
[ ] Realm dédié magic-gold (jamais master pour les utilisateurs applicatifs)
[ ] Clients séparés : magicgold-web, magicgold-mobile, magicgold-api, magicgold-admin
[ ] Clients publics : Authorization Code + PKCE uniquement
[ ] MFA obligatoire pour SUPER_ADMIN / ADMIN / FINANCE
[ ] Compte admin Keycloak distinct par environnement
```

## 11.4 Réseau et hôte

```text
[ ] SSH par clés uniquement, login root direct désactivé
[ ] Firewall : 80/443 publics ; 5432, 6379, 8080 internes uniquement
[ ] Interfaces d'admin (DB, Redis, Keycloak admin, Docker) non exposées sur Internet
[ ] Mises à jour de sécurité de l'hôte appliquées régulièrement
```

## 11.5 Conteneurs

```text
[ ] Images de base maintenues et épinglées (postgres:15-alpine, redis:7-alpine, nginx:1.27-alpine)
[ ] Exécution sans root (USER app dans le Dockerfile backend)
[ ] Scan des images (Trivy/Grype) dans le CI
[ ] Aucun secret dans les images ni dans les Dockerfile
```

## 11.6 Application

```text
[ ] HTTPS obligatoire, HSTS activé
[ ] JWT : vérification signature + iss + aud + exp + nbf + alg
[ ] RBAC + ownership checks (anti-IDOR) côté backend
[ ] Rate limiting sur login, OTP, paiement, transfert, webhooks
[ ] CORS restreint (jamais * en production)
[ ] En-têtes de sécurité (HSTS, X-Content-Type-Options, X-Frame-Options, Referrer-Policy)
[ ] Erreurs non verboses (pas de stack trace / SQL en production)
[ ] Audit des opérations sensibles (paiements, rôles, configuration)
```

## 11.7 Contrôles avant go-live (gate de production)

```text
[ ] SAST + DAST + scan dépendances exécutés et clean (critique)
[ ] Test de pénétration sur les flux sensibles (auth, paiement, IDOR)
[ ] Test de restauration réussi sur staging
[ ] Migrations Flyway validées sur staging
[ ] Certificats TLS valides, score SSL Labs ≥ A
[ ] Alertes monitoring opérationnelles
[ ] Procédure d'incident et de rollback documentée
[ ] Compte admin de secours (break-glass) configuré et audité
```

---

# 12. Procédures opérationnelles récapitulatives

## 12.1 Premier déploiement d'un environnement

```bash
cd /home/deploy/magic-gold
cp deploy/.env.example deploy/.env && chmod 600 deploy/.env
# éditer .env avec les valeurs de l'environnement
vim deploy/.env

# Démarrer la stack
docker compose -f deploy/docker-compose.yml up -d

# Vérifier la santé
docker compose -f deploy/docker-compose.yml ps
curl -fsS http://localhost:8080/actuator/health/readiness   # via le réseau interne
```

## 12.2 Mise à jour applicative (rollout)

```bash
cd /home/deploy/magic-gold
export API_IMAGE=registry.example.com/nectux/magic-gold-api:1.0.0
docker compose -f deploy/docker-compose.yml -f deploy/docker-compose.prod.yml up -d api
docker compose logs -f api
```

## 12.3 Sauvegarde + restauration

```bash
deploy/scripts/backup.sh                    # sauvegarde chiffrée + hors serveur
deploy/scripts/restore.sh backups/<file>.enc # restauration
```

---

# 13. Références et cohérence

| Sujet | Document de référence |
|---|---|
| Décisions techniques (Java 17, stack, Merchant=Tenant, XOF) | `00_Master.md` |
| Architecture, modular monolith, topologie | `03_TechnicalArchitecture.md` |
| Contrat API, health endpoints, erreurs | `05_API_Specification.md` |
| Sécurité, secrets, TLS, sauvegardes, durcissement | `06_SecurityArchitecture.md` |
| Usage et limites de Redis | `16_RedisSpecification.md` |

Toute contradiction avec `00_Master.md` doit être signalée et corrigée au profit du Master.

---

**Nectux Ingénieries**  
**MAGIC GOLD — Simple pour commencer. Puissant pour grandir.**
