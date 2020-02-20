create table likes
(
    parent_id int not null,
    type int not null,
    user_id int not null,
    gmt_create bigint,
    constraint like_pk
        primary key (parent_id, type, user_id)
);
