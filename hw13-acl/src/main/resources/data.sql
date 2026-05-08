insert into authors(full_name)
values ('Author_1'), ('Author_2'), ('Author_3');

insert into genres(name)
values ('Genre_1'), ('Genre_2'), ('Genre_3'),
       ('Genre_4'), ('Genre_5'), ('Genre_6');

insert into books(title, author_id)
values ('BookTitle_1', 1), ('BookTitle_2', 2), ('BookTitle_3', 3);

insert into books_genres(book_id, genre_id)
values (1, 1), (1, 2),
       (2, 3), (2, 4),
       (3, 5), (3, 6);

insert into comments(text, book_id)
values ('Comment_1_for_book_1', 1),
       ('Comment_2_for_book_1', 1),
       ('Comment_1_for_book_2', 2),
       ('Comment_1_for_book_3', 3);

INSERT INTO users (username, password, role) VALUES
    ('admin', '$2a$10$2EJo9VZzK64v6/gidt5/3e5SWIAseuzmCm2bPYMw77o7q34ihiYTy', 'ROLE_ADMIN'),
    ('user',  '$2a$10$2EJo9VZzK64v6/gidt5/3e5SWIAseuzmCm2bPYMw77o7q34ihiYTy', 'ROLE_USER');
