create table comment
(
    id bigint auto_increment,
    parent_id bigint not null,
    type int not null,
    user_id int not null,
    content text,
    gmt_create bigint,
    gmt_modified bigint,
    like_count bigint default 0,
    constraint comment_pk
        primary key (id)
);
