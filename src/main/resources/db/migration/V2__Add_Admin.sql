insert into usr (active, id, email, password, username)
values (true, 1, 'example@mail.ru', '123', 'admin');

insert into user_role (user_id, roles)
values (1, 'ADMIN'),
       (1, 'USER');