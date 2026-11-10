-- =====================================================================
-- MAGIC GOLD — V1 : schéma initial (MVP commerce de détail)
-- Cohérent avec 00_Master.md (vocabulaire, états authoritative, V1)
-- et 04_DataModel.md (champs des entités). Merchant = Tenant.
-- =====================================================================

-- Identité -----------------------------------------------------------------
CREATE TABLE users (
    id              uuid PRIMARY KEY,
    external_id     varchar(255) UNIQUE,
    username        varchar(255) NOT NULL UNIQUE,
    email           varchar(255),
    phone_number    varchar(64),
    first_name      varchar(255),
    last_name       varchar(255),
    status          varchar(32) NOT NULL,
    last_login_at   timestamptz,
    created_at      timestamptz,
    updated_at      timestamptz
);

CREATE TABLE roles (
    id              uuid PRIMARY KEY,
    name            varchar(64) NOT NULL UNIQUE,
    description     varchar(255),
    created_at      timestamptz,
    updated_at      timestamptz
);

CREATE TABLE user_roles (
    id              uuid PRIMARY KEY,
    user_id         uuid NOT NULL,
    role_id         uuid NOT NULL,
    assigned_at     timestamptz,
    assigned_by     uuid,
    created_at      timestamptz,
    updated_at      timestamptz,
    CONSTRAINT uk_user_roles_user_role UNIQUE (user_id, role_id)
);

-- Commerce -----------------------------------------------------------------
CREATE TABLE merchants (
    id                  uuid PRIMARY KEY,
    merchant_number     varchar(32) NOT NULL UNIQUE,
    legal_name          varchar(255),
    commercial_name     varchar(255) NOT NULL,
    business_type       varchar(64),
    registration_number varchar(128),
    tax_number          varchar(128),
    phone_number        varchar(64),
    email               varchar(255),
    address             varchar(255),
    city                varchar(128),
    country             varchar(128),
    logo_file_id        uuid,
    status              varchar(32) NOT NULL,
    created_at          timestamptz,
    updated_at          timestamptz
);

CREATE TABLE merchant_outlets (
    id              uuid PRIMARY KEY,
    merchant_id     uuid NOT NULL,
    outlet_number   varchar(32) NOT NULL UNIQUE,
    name            varchar(255) NOT NULL,
    address         varchar(255),
    city            varchar(128),
    district        varchar(128),
    latitude        numeric(10,7),
    longitude       numeric(10,7),
    phone_number    varchar(64),
    status          varchar(32) NOT NULL,
    created_at      timestamptz,
    updated_at      timestamptz
);

CREATE TABLE merchant_users (
    id              uuid PRIMARY KEY,
    merchant_id     uuid NOT NULL,
    user_id         uuid NOT NULL,
    outlet_id       uuid,
    role            varchar(32) NOT NULL,
    status          varchar(32) NOT NULL,
    created_at      timestamptz,
    updated_at      timestamptz
);

-- Catalogue -----------------------------------------------------------------
CREATE TABLE product_categories (
    id              uuid PRIMARY KEY,
    merchant_id     uuid NOT NULL,
    parent_id       uuid,
    name            varchar(255) NOT NULL,
    description     varchar(255),
    status          varchar(32) NOT NULL,
    created_at      timestamptz,
    updated_at      timestamptz
);

CREATE TABLE products (
    id              uuid PRIMARY KEY,
    merchant_id     uuid NOT NULL,
    category_id     uuid,
    sku             varchar(128) NOT NULL,
    name            varchar(255) NOT NULL,
    description     varchar(1024),
    unit_price      numeric(19,4) NOT NULL,
    currency        varchar(3) NOT NULL,
    stock_quantity  numeric(19,4),
    status          varchar(32) NOT NULL,
    image_file_id   uuid,
    created_at      timestamptz,
    updated_at      timestamptz
);

-- Client -------------------------------------------------------------------
CREATE TABLE customers (
    id                  uuid PRIMARY KEY,
    merchant_id         uuid NOT NULL,
    user_id             uuid,
    customer_number     varchar(32) NOT NULL UNIQUE,
    first_name          varchar(255),
    last_name           varchar(255),
    phone_number        varchar(64),
    email               varchar(255),
    date_of_birth       date,
    gender              varchar(16),
    type                varchar(32) NOT NULL,
    registration_source varchar(32),
    status              varchar(32) NOT NULL,
    created_at          timestamptz,
    updated_at          timestamptz
);

-- Vente --------------------------------------------------------------------
CREATE TABLE orders (
    id              uuid PRIMARY KEY,
    merchant_id     uuid NOT NULL,
    order_number    varchar(32) NOT NULL UNIQUE,
    customer_id     uuid,
    outlet_id       uuid,
    subtotal        numeric(19,4) NOT NULL,
    discount_amount numeric(19,4),
    tax_amount      numeric(19,4),
    total_amount    numeric(19,4) NOT NULL,
    currency        varchar(3) NOT NULL,
    status          varchar(32) NOT NULL,
    created_at      timestamptz,
    updated_at      timestamptz
);

CREATE TABLE order_items (
    id                    uuid PRIMARY KEY,
    merchant_id           uuid NOT NULL,
    order_id              uuid NOT NULL,
    product_id            uuid,
    product_name_snapshot varchar(255) NOT NULL,
    quantity              numeric(19,4) NOT NULL,
    unit_price            numeric(19,4) NOT NULL,
    discount_amount       numeric(19,4),
    total_amount          numeric(19,4) NOT NULL,
    created_at            timestamptz,
    updated_at            timestamptz
);

CREATE TABLE order_status_history (
    id              uuid PRIMARY KEY,
    merchant_id     uuid NOT NULL,
    order_id        uuid NOT NULL,
    from_status     varchar(32),
    to_status       varchar(32) NOT NULL,
    changed_by      uuid,
    changed_at      timestamptz NOT NULL,
    note            varchar(1024),
    created_at      timestamptz,
    updated_at      timestamptz
);

-- Paiement -----------------------------------------------------------------
CREATE TABLE payments (
    id                      uuid PRIMARY KEY,
    merchant_id             uuid NOT NULL,
    payment_number          varchar(32) NOT NULL UNIQUE,
    order_id                uuid,
    customer_id             uuid,
    provider_id             uuid,
    external_transaction_id varchar(255),
    idempotency_key         varchar(255) UNIQUE,
    amount                  numeric(19,4) NOT NULL,
    currency                varchar(3) NOT NULL,
    payment_method          varchar(32) NOT NULL,
    status                  varchar(32) NOT NULL,
    initiated_at            timestamptz,
    completed_at            timestamptz,
    failed_at               timestamptz,
    failure_reason          varchar(1024),
    metadata                text,
    created_at              timestamptz,
    updated_at              timestamptz
);

CREATE TABLE payment_providers (
    id                      uuid PRIMARY KEY,
    code                    varchar(32) NOT NULL UNIQUE,
    name                    varchar(255) NOT NULL,
    type                    varchar(32),
    status                  varchar(32) NOT NULL,
    configuration_reference varchar(255),
    created_at              timestamptz,
    updated_at              timestamptz
);

CREATE TABLE webhook_events (
    id                uuid PRIMARY KEY,
    provider_id       uuid,
    event_type        varchar(64) NOT NULL,
    external_event_id varchar(255) NOT NULL UNIQUE,
    payload           text,
    status            varchar(32) NOT NULL,
    received_at       timestamptz,
    processed_at      timestamptz,
    created_at        timestamptz,
    updated_at        timestamptz
);

-- Caisse -------------------------------------------------------------------
CREATE TABLE registers (
    id              uuid PRIMARY KEY,
    merchant_id     uuid NOT NULL,
    outlet_id       uuid,
    register_number varchar(32) NOT NULL UNIQUE,
    name            varchar(255) NOT NULL,
    status          varchar(32) NOT NULL,
    created_at      timestamptz,
    updated_at      timestamptz
);

CREATE TABLE cash_sessions (
    id               uuid PRIMARY KEY,
    merchant_id      uuid NOT NULL,
    register_id      uuid NOT NULL,
    user_id          uuid NOT NULL,
    opened_at        timestamptz NOT NULL,
    closed_at        timestamptz,
    opening_balance  numeric(19,4),
    closing_balance  numeric(19,4),
    expected_cash    numeric(19,4),
    counted_cash     numeric(19,4),
    difference       numeric(19,4),
    status           varchar(32) NOT NULL,
    created_at       timestamptz,
    updated_at       timestamptz
);

CREATE TABLE cash_movements (
    id              uuid PRIMARY KEY,
    merchant_id     uuid NOT NULL,
    cash_session_id uuid NOT NULL,
    type            varchar(16) NOT NULL,
    amount          numeric(19,4) NOT NULL,
    reason          varchar(255),
    reference_type  varchar(32),
    reference_id    uuid,
    created_at      timestamptz,
    updated_at      timestamptz
);

-- Stock --------------------------------------------------------------------
CREATE TABLE stock_movements (
    id              uuid PRIMARY KEY,
    merchant_id     uuid NOT NULL,
    product_id      uuid NOT NULL,
    outlet_id       uuid,
    type            varchar(32) NOT NULL,
    quantity        numeric(19,4) NOT NULL,
    reference_type  varchar(32),
    reference_id    uuid,
    note            varchar(1024),
    created_at      timestamptz,
    updated_at      timestamptz
);

-- Audit / Configuration -----------------------------------------------------
CREATE TABLE audit_logs (
    id          uuid PRIMARY KEY,
    merchant_id uuid,
    user_id     uuid,
    action      varchar(64) NOT NULL,
    entity      varchar(64) NOT NULL,
    entity_id   uuid,
    old_value   text,
    new_value   text,
    ip_address  varchar(64),
    device      varchar(128),
    created_at  timestamptz,
    updated_at  timestamptz
);

CREATE TABLE configurations (
    id          uuid PRIMARY KEY,
    merchant_id uuid,
    config_key  varchar(255) NOT NULL,
    config_value text,
    value_type  varchar(32),
    description varchar(255),
    created_at  timestamptz,
    updated_at  timestamptz
);

-- Index d'isolation multi-tenant (toutes les données métier filtrées par merchant_id)
CREATE INDEX idx_merchant_outlets_merchant   ON merchant_outlets (merchant_id);
CREATE INDEX idx_merchant_users_merchant     ON merchant_users (merchant_id);
CREATE INDEX idx_product_categories_merchant ON product_categories (merchant_id);
CREATE INDEX idx_products_merchant           ON products (merchant_id);
CREATE INDEX idx_customers_merchant          ON customers (merchant_id);
CREATE INDEX idx_orders_merchant             ON orders (merchant_id);
CREATE INDEX idx_order_items_merchant        ON order_items (merchant_id);
CREATE INDEX idx_order_items_order           ON order_items (order_id);
CREATE INDEX idx_order_status_history_order  ON order_status_history (order_id);
CREATE INDEX idx_payments_merchant           ON payments (merchant_id);
CREATE INDEX idx_registers_merchant          ON registers (merchant_id);
CREATE INDEX idx_cash_sessions_merchant      ON cash_sessions (merchant_id);
CREATE INDEX idx_cash_movements_merchant     ON cash_movements (merchant_id);
CREATE INDEX idx_stock_movements_merchant    ON stock_movements (merchant_id);
CREATE INDEX idx_stock_movements_product     ON stock_movements (merchant_id, product_id);
CREATE INDEX idx_audit_logs_merchant         ON audit_logs (merchant_id);
