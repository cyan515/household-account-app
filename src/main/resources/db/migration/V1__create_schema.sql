CREATE TABLE users (
    id UUID NOT NULL,
    name VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    CONSTRAINT users_id_unique UNIQUE (id),
    CONSTRAINT users_name_unique UNIQUE (name)
);

CREATE TABLE categories (
    id UUID NOT NULL,
    name VARCHAR(50) NOT NULL,
    CONSTRAINT categories_id_unique UNIQUE (id)
);

CREATE TABLE receipts (
    id UUID NOT NULL,
    user_id UUID NOT NULL,
    date_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT receipts_id_unique UNIQUE (id),
    CONSTRAINT receipts_user_id_fk FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE receipt_details (
    id SERIAL PRIMARY KEY,
    receipt_id UUID NOT NULL,
    category_id UUID NOT NULL,
    item_name VARCHAR(50) NOT NULL,
    amount INTEGER NOT NULL,
    CONSTRAINT receipt_details_receipt_id_fk FOREIGN KEY (receipt_id) REFERENCES receipts (id),
    CONSTRAINT receipt_details_category_id_fk FOREIGN KEY (category_id) REFERENCES categories (id)
);
