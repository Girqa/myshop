--liquibase formatted sql
--changeset Reso11er:users-table

create table users
(
    id       bigint       not null generated always as identity,
    username varchar(10)  not null unique,
    password varchar(255) not null,
    role     varchar(10)  not null,

    constraint anonymous_username check (username != 'anonymousUser'),
    constraint users_pk primary key (id)
);

--changeset Reso11er:initial-users
insert into users(username, password, role)
values ('user', '$2a$12$n5nuS71T/PHG6mra.GDjve5OoAbhwwZv.1vxOkiYHLnrpTzWt809O', 'USER'),
       ('admin', '$2a$12$8pQRbdFGptX4gvmyCgGogu0GjMEuYjqaopiFYbPRDK1cDCH5N5VxO', 'ADMIN');
