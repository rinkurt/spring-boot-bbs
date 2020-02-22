create table users
(
    id serial,
    account_id varchar(100),
    name varchar(50),
    token varchar(36),
    gmt_create bigint,
    gmt_modified bigint,
    bio varchar(256),
    avatar_url varchar(100),
    username varchar(50),
    password varchar(20)
);

create unique index users_id_uindex
    on users (id);

create unique index users_username_uindex
    on users (username);

alter table users
    add constraint users_pk
        primary key (id);

