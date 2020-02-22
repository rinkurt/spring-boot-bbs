create table likes
(
    parent_id int,
    type int,
    user_id int,
    gmt_create bigint,
    primary key (parent_id, type, user_id)
);

