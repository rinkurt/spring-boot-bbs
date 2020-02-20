alter table NOTIFICATION alter column ID int auto_increment;

alter table NOTIFICATION alter column NOTIFIER int not null;

alter table NOTIFICATION alter column RECEIVER int not null;

alter table NOTIFICATION alter column OUTERID int not null;

