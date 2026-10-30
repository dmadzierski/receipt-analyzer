CREATE TABLE app_user
(
    id            UUID         NOT NULL,
    created_date  TIMESTAMP(6) NOT NULL,
    modified_date TIMESTAMP(6),
    user_sub      VARCHAR(255) NOT NULL,
    CONSTRAINT pk_app_user PRIMARY KEY (id),
    CONSTRAINT uq_app_user_user_sub UNIQUE (user_sub)
);

CREATE TABLE wallet
(
    id            UUID         NOT NULL,
    created_date  TIMESTAMP(6) NOT NULL,
    modified_date TIMESTAMP(6),
    name          VARCHAR(255),
    user_id       UUID,
    CONSTRAINT pk_wallet PRIMARY KEY (id),
    CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES app_user (id)
);

CREATE TABLE store_brand
(
    id            UUID         NOT NULL,
    created_date  TIMESTAMP(6) NOT NULL,
    modified_date TIMESTAMP(6),
    name          VARCHAR(255),
    CONSTRAINT pk_store_brand PRIMARY KEY (id)
);

CREATE TABLE store
(
    id            UUID         NOT NULL,
    created_date  TIMESTAMP(6) NOT NULL,
    modified_date TIMESTAMP(6),
    address       VARCHAR(255),
    city          VARCHAR(255),
    postal_code   VARCHAR(255),
    country       VARCHAR(255),
    brand_id      UUID,
    CONSTRAINT pk_store PRIMARY KEY (id),
    CONSTRAINT fk_store_brand FOREIGN KEY (brand_id) REFERENCES store_brand (id)
);

CREATE TABLE product
(
    id            UUID         NOT NULL,
    created_date  TIMESTAMP(6) NOT NULL,
    modified_date TIMESTAMP(6),
    name          VARCHAR(255) NOT NULL,
    CONSTRAINT pk_product PRIMARY KEY (id),
    CONSTRAINT uq_product_name UNIQUE (name)
);

CREATE TABLE product_alias
(
    id            UUID         NOT NULL,
    created_date  TIMESTAMP(6) NOT NULL,
    modified_date TIMESTAMP(6),
    name          VARCHAR(255) NOT NULL,
    product_id    UUID         NOT NULL,
    CONSTRAINT pk_product_alias PRIMARY KEY (id),
    CONSTRAINT uq_product_alias_name UNIQUE (name),
    CONSTRAINT fk_product_alias_product FOREIGN KEY (product_id) REFERENCES product (id)
);

CREATE TABLE product_category
(
    id            UUID         NOT NULL,
    created_date  TIMESTAMP(6) NOT NULL,
    modified_date TIMESTAMP(6),
    name          VARCHAR(255) NOT NULL,
    user_id       UUID,
    CONSTRAINT pk_product_category PRIMARY KEY (id),
    CONSTRAINT uq_product_category_name UNIQUE (name),
    CONSTRAINT fk_product_category_user FOREIGN KEY (user_id) REFERENCES app_user (id)
);

CREATE TABLE product_category_mapping
(
    category_id UUID         NOT NULL,
    product_id  UUID         NOT NULL,
    CONSTRAINT pk_product_category_mapping PRIMARY KEY (category_id, product_id),
    CONSTRAINT fk_product_category_mapping_category FOREIGN KEY (category_id) REFERENCES product_category (id),
    CONSTRAINT fk_product_category_mapping_product FOREIGN KEY (product_id) REFERENCES product (id)
);

CREATE TABLE receipt
(
    id            UUID         NOT NULL,
    created_date  TIMESTAMP(6) NOT NULL,
    modified_date TIMESTAMP(6),
    name          VARCHAR(255),
    description   VARCHAR(255),
    wallet_id     UUID,
    store_id      UUID,
    CONSTRAINT pk_receipt PRIMARY KEY (id),
    CONSTRAINT fk_receipt_wallet FOREIGN KEY (wallet_id) REFERENCES wallet (id),
    CONSTRAINT fk_receipt_store FOREIGN KEY (store_id) REFERENCES store (id)
);

CREATE TABLE receipt_file_group
(
    id            UUID         NOT NULL,
    created_date  TIMESTAMP(6) NOT NULL,
    modified_date TIMESTAMP(6),
    file_type     VARCHAR(255),
    is_original   BOOLEAN,
    receipt_id    UUID,
    CONSTRAINT pk_receipt_file_group PRIMARY KEY (id),
    CONSTRAINT ck_receipt_file_group_file_type CHECK (file_type IN ('PDF', 'JSON', 'JPEG', 'PNG', 'WEBP')),
    CONSTRAINT fk_receipt_file_group_receipt FOREIGN KEY (receipt_id) REFERENCES receipt (id)
);

CREATE TABLE receipt_file
(
    id            UUID         NOT NULL,
    created_date  TIMESTAMP(6) NOT NULL,
    modified_date TIMESTAMP(6),
    path          VARCHAR(255),
    part_number   INTEGER,
    raw_data      TEXT,
    file_group_id UUID,
    CONSTRAINT pk_receipt_file PRIMARY KEY (id),
    CONSTRAINT fk_receipt_file_file_group FOREIGN KEY (file_group_id) REFERENCES receipt_file_group (id)
);

CREATE TABLE receipt_revision
(
    id                         UUID         NOT NULL,
    created_date               TIMESTAMP(6) NOT NULL,
    modified_date              TIMESTAMP(6),
    name                       VARCHAR(255),
    revision                   VARCHAR(255),
    resolver                   VARCHAR(255),
    total_price                NUMERIC(38, 2),
    payment_date               TIMESTAMP(6),
    is_preferred_revision      BOOLEAN,
    is_correct                 BOOLEAN,
    receipt_id                 UUID,
    parent_receipt_revision_id UUID,
    source_file_group_id       UUID,
    CONSTRAINT pk_receipt_revision PRIMARY KEY (id),
    CONSTRAINT ck_receipt_revision_resolver CHECK (resolver IN ('BIEDRONKA', 'USER', 'BIEDRONKA_JSON')),
    CONSTRAINT fk_receipt_revision_receipt FOREIGN KEY (receipt_id) REFERENCES receipt (id),
    CONSTRAINT fk_receipt_revision_parent FOREIGN KEY (parent_receipt_revision_id) REFERENCES receipt_revision (id),
    CONSTRAINT fk_receipt_revision_source_file_group FOREIGN KEY (source_file_group_id) REFERENCES receipt_file_group (id)
);

CREATE TABLE receipt_item
(
    id                  UUID         NOT NULL,
    created_date        TIMESTAMP(6) NOT NULL,
    modified_date       TIMESTAMP(6),
    name                VARCHAR(255),
    amount              NUMERIC(10, 3),
    unit_price          NUMERIC(10, 2),
    discount            NUMERIC(10, 2),
    total_price         NUMERIC(10, 2),
    position            INTEGER,
    receipt_revision_id UUID,
    product_id          UUID,
    parent_item_id      UUID,
    CONSTRAINT pk_receipt_item PRIMARY KEY (id),
    CONSTRAINT fk_receipt_item_receipt_revision FOREIGN KEY (receipt_revision_id) REFERENCES receipt_revision (id),
    CONSTRAINT fk_receipt_item_product FOREIGN KEY (product_id) REFERENCES product (id),
    CONSTRAINT fk_receipt_item_parent FOREIGN KEY (parent_item_id) REFERENCES receipt_item (id)
);

CREATE INDEX ix_wallet_user_id ON wallet (user_id);
CREATE INDEX ix_receipt_wallet_id ON receipt (wallet_id);
CREATE INDEX ix_receipt_file_group_receipt_id ON receipt_file_group (receipt_id);
CREATE INDEX ix_receipt_file_file_group_id ON receipt_file (file_group_id);
CREATE INDEX ix_receipt_revision_receipt_id ON receipt_revision (receipt_id);
CREATE INDEX ix_receipt_revision_source_file_group_id ON receipt_revision (source_file_group_id);
CREATE INDEX ix_receipt_item_receipt_revision_id ON receipt_item (receipt_revision_id);
CREATE INDEX ix_receipt_item_product_id ON receipt_item (product_id);
CREATE INDEX ix_product_alias_product_id ON product_alias (product_id);
