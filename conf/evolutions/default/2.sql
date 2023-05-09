
-- !Ups

-- create users table
create table order_v1.users
(
    id         serial       not null constraint users_pk primary key,
    first_name              varchar(64)  not null,
    last_name               varchar(64)  not null,
    password                varchar(128) not null,
    email                   varchar(100) not null,
    role                    varchar(16)  not null,
    birth_date              DATE         not null,
    address                 VARCHAR(64)  not null,
    phone_number            VARCHAR(20)  not null
);

create unique index users_id_uindex
    on order_v1.users (id);

create unique index users_email_uindex
    on order_v1.users (email);

create table order_v1.orders
(
    id         serial       not null constraint orders_pk primary key,
    user_id    serial       not null,
    order_date DATE         not null,
    total_price DECIMAL(22, 4) NOT NULL,
    CONSTRAINT fk_orders_users FOREIGN KEY (user_id) REFERENCES order_v1.users(id)
);

create table order_v1.products
(
    id              serial not null constraint products_pk primary key,
    product_name    VARCHAR(64)    not null,
    exp_date        DATE           not null,
    price           DECIMAL(22, 4) NOT NULL,
    CONSTRAINT channels_product_name_unique UNIQUE (product_name)
);

create table order_v1.order_details
(
    id              serial          not null constraint product_details_pk primary key,
    order_id        serial          not null,
    product_id      serial          not null,
    quantity        INT4            NOT NULL,
    price           DECIMAL(22, 4)  NOT NULL,
    CONSTRAINT fk_order_detail_orders FOREIGN KEY (order_id) REFERENCES order_v1.orders(id),
    CONSTRAINT fk_order_details_products FOREIGN KEY (product_id) REFERENCES order_v1.products(id)
);

-- !Downs

DROP TABLE order_v1.users;
DROP TABLE order_v1.orders;
DROP TABLE order_v1.products;
DROP TABLE order_v1.order_details;
