create table notification
(
    id serial
        constraint notification_pk
            primary key,
    notifier int,
    receiver int,
    outerid int,
    type int,
    gmt_create bigint,
    status int default 0
);

