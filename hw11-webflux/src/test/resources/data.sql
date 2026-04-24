INSERT INTO authors(id, full_name)
VALUES (1, 'Author A'), (2, 'Author B'), (3, 'Author C');

INSERT INTO genres(id, name)
VALUES (1, 'Genre1'), (2, 'Genre2'), (3, 'Genre3'),
       (4, 'Genre4'), (5, 'Genre5'), (6, 'Genre6');

INSERT INTO books(id, title, author_id)
VALUES (1, 'Book A', 1), (2, 'Book B', 2), (3, 'Book C', 1);

INSERT INTO books_genres(book_id, genre_id)
VALUES (1, 1), (1, 2),
       (2, 3), (2, 4),
       (3, 5), (3, 6);

INSERT INTO comments(id, text, book_id)
VALUES (1, 'Comment 1', 1),
       (2, 'Comment 2', 1),
       (3, 'Comment 1', 2),
       (4, 'Comment 1', 3);

ALTER TABLE authors ALTER COLUMN id RESTART WITH 4;
ALTER TABLE genres ALTER COLUMN id RESTART WITH 7;
ALTER TABLE books ALTER COLUMN id RESTART WITH 4;
ALTER TABLE comments ALTER COLUMN id RESTART WITH 5;
