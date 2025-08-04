--liquibase formatted sql
--changeset Reso11er:init-tables

create table images
(
    id         bigint       not null generated always as identity,
    image_name varchar(100) not null,
    image_size bigint       not null,
    image_data bytea        not null,

    constraint images_pk primary key (id)
);

create table products
(
    id                  bigint        not null generated always as identity,
    product_name        varchar(100)  not null,
    product_description text          not null,
    product_price       decimal(7, 2) not null,
    product_image       bigint        not null,

    constraint positive_product_price check ( product_price > 0 ),

    constraint products_pk primary key (id),
    constraint products_images_fk foreign key (product_image)
        references images (id) on delete set null
);

create table buckets
(
    id      bigint not null generated always as identity,
    user_id bigint not null,

    constraint buckets_pk primary key (id)
);

create table buckets_products
(
    bucket_id      bigint not null,
    product_id     bigint not null,
    product_amount int4   not null,

    constraint positive_product_amount check ( product_amount > 0 ),

    constraint buckets_products_pk primary key (bucket_id, product_id),
    constraint buckets_fk foreign key (bucket_id)
        references buckets (id) on delete cascade,
    constraint products_fk foreign key (product_id)
        references products on delete cascade
);

create table orders
(
    id               bigint         not null generated always as identity,
    order_price      decimal(10, 2) not null,
    order_created_at timestamp      not null,

    constraint positive_order_price check ( order_price > 0 ),
    constraint orders_pk primary key (id)
);

create table orders_products
(
    id                        bigint        not null generated always as identity,
    order_id                  bigint        not null,
    order_product_name        varchar(100)  not null,
    order_product_description text          not null,
    order_product_price       decimal(7, 2) not null,
    order_product_image       bigint        not null,
    order_product_amount      int4          not null,

    constraint order_products_pk primary key (id),
    constraint order_products_fk foreign key (order_id)
        references orders (id) on delete cascade,
    constraint order_products_images_fk foreign key (order_product_image)
        references images (id) on delete set null
)
