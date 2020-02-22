create table question
(
    id serial,
    title varchar(50),
    description text,
    gmt_create bigint,
    gmt_modified bigint,
    creator int,
    comment_count int default 0,
    view_count int default 0,
    like_count int default 0,
    tag varchar(256)
);

create unique index question_id_uindex
    on question (id);

alter table question
    add constraint question_pk
        primary key (id);

