INSERT INTO authors (full_name) VALUES ('Author A');
INSERT INTO authors (full_name) VALUES ('Author B');

INSERT INTO genres (name) VALUES ('Genre1');
INSERT INTO genres (name) VALUES ('Genre2');
INSERT INTO genres (name) VALUES ('Genre3');

INSERT INTO books (title, author_id) VALUES ('Book A', 1);
INSERT INTO books (title, author_id) VALUES ('Book B', 2);

INSERT INTO books_genres (book_id, genre_id) VALUES (1, 1);
INSERT INTO books_genres (book_id, genre_id) VALUES (2, 2);

INSERT INTO comments (text, book_id) VALUES ('Comment 1', 1);
INSERT INTO comments (text, book_id) VALUES ('Comment 2', 1);
