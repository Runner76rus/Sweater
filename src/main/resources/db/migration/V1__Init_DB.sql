create sequence message_seq start with 1 increment by 50;

create sequence usr_seq start with 1 increment by 50;

create table message
(
    id        bigint        not null,
    user_id   bigint,
    file_name varchar(255),
    message   varchar(2048) not null,
    tag       varchar(255),
    primary key (id)
);

create table user_role
(
    user_id bigint not null,
    roles   varchar(255) check (roles in ('USER', 'ADMIN'))
);

create table usr
(
    active          boolean      not null,
    id              bigint       not null,
    activation_code varchar(255),
    email           varchar(255),
    password        varchar(255) not null,
    username        varchar(255) not null,
    primary key (id)
);

alter table if exists message
    add constraint message_usr_fk
    foreign key (user_id) references usr;

alter table if exists user_role
    add constraint user_role_usr_fk
    foreign key (user_id) references usr;