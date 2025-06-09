CREATE TABLE card_transaction
(
    id                 BIGINT       NOT NULL,
    created_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    approved_at        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    cancelled_at       TIMESTAMP WITHOUT TIME ZONE,
    payment_id         BIGINT,
    card_number_masked VARCHAR(255) NOT NULL,
    card_company       VARCHAR(255) NOT NULL,
    approve_no         VARCHAR(255) NOT NULL,
    installment_months INTEGER      NOT NULL,
    status             SMALLINT     NOT NULL,
    _is_new            BOOLEAN      NOT NULL,
    CONSTRAINT pk_card_transaction PRIMARY KEY (id)
);

CREATE TABLE payment
(
    id           BIGINT       NOT NULL,
    created_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    approved_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    cancelled_at TIMESTAMP WITHOUT TIME ZONE,
    merchant_id  VARCHAR(255) NOT NULL,
    order_id     VARCHAR(255) NOT NULL,
    amount       DECIMAL      NOT NULL,
    currency     VARCHAR(255) NOT NULL,
    status       SMALLINT     NOT NULL,
    method       SMALLINT     NOT NULL,
    user_data    JSONB        NOT NULL,
    CONSTRAINT pk_payment PRIMARY KEY (id)
);

CREATE TABLE payment_logs
(
    id            BIGINT       NOT NULL,
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    approved_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    cancelled_at  TIMESTAMP WITHOUT TIME ZONE,
    payment_id    BIGINT,
    payment_event VARCHAR(255) NOT NULL,
    event_data    JSONB,
    description   VARCHAR(255) NOT NULL,
    _is_new       BOOLEAN      NOT NULL,
    CONSTRAINT pk_payment_logs PRIMARY KEY (id)
);

ALTER TABLE card_transaction
    ADD CONSTRAINT FK_CARD_TRANSACTION_ON_PAYMENT FOREIGN KEY (payment_id) REFERENCES payment (id);

ALTER TABLE payment_logs
    ADD CONSTRAINT FK_PAYMENT_LOGS_ON_PAYMENT FOREIGN KEY (payment_id) REFERENCES payment (id);