create table comment
(
    id serial
        constraint comment_pk
            primary key,
    parent_id int,
    type int,
    user_id int,
    content text,
    gmt_create bigint,
    gmt_modified bigint,
    like_count bigint default 0,
    comment_count bigint default 0
);

