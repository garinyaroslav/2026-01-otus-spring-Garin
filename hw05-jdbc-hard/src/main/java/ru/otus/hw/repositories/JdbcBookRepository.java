package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JdbcBookRepository implements BookRepository {

    private final NamedParameterJdbcOperations jdbc;

    private final GenreRepository genreRepository;

    @Override
    public Optional<Book> findById(long id) {
        Book res;
        try {
            res = jdbc.query(
                    """
                            select
                                b.id,
                                b.title,
                                a.id as author_id,
                                a.full_name as author_full_name,
                                g.id as genre_id,
                                g.name as genre_name
                                from books b
                                join authors a on b.author_id = a.id
                                join books_genres bg on bg.book_id = b.id
                                join genres g on g.id = bg.genre_id
                                where b.id = :id
                            """,
                    new MapSqlParameterSource().addValue("id", id), new BookResultSetExtractor());
        } catch (DataAccessException ex) {
            throw new EntityNotFoundException("Exceptions while getting book");
        }
        return Optional.ofNullable(res);
    }

    @Override
    public List<Book> findAll() {
        var genres = genreRepository.findAll();
        var books = getAllBooksWithoutGenres();
        var relations = getAllGenreRelations();
        mergeBooksInfo(books, genres, relations);
        return books;
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == 0) {
            return insert(book);
        }
        return update(book);
    }

    @Override
    public void deleteById(long id) {
        try {
            jdbc.update("delete from books where id = :id", new MapSqlParameterSource().addValue("id", id));
        } catch (DataAccessException ex) {
            throw new EntityNotFoundException("Exceptions while deliting book with id = " + id);
        }
    }

    private List<Book> getAllBooksWithoutGenres() {
        List<Book> res;

        try {
            res = jdbc.query(
                    """
                            select
                                b.id,
                                b.title,
                                a.id as author_id,
                                a.full_name as author_full_name
                                from books b
                                join authors a on b.author_id = a.id
                            """, new BookRowMapper());
        } catch (DataAccessException ex) {
            throw new RuntimeException("Exceptions while getting books");
        }

        return res != null ? res : new ArrayList<>();
    }

    private List<BookGenreRelation> getAllGenreRelations() {
        List<BookGenreRelation> res;

        try {
            res = jdbc.query("select book_id, genre_id from books_genres",
                    (rs, rowNum) -> new BookGenreRelation(rs.getLong("book_id"), rs.getLong("genre_id")));
        } catch (DataAccessException ex) {
            throw new RuntimeException("Exceptions while getting book relations");
        }

        return res != null ? res : new ArrayList<>();
    }

    private void mergeBooksInfo(List<Book> booksWithoutGenres, List<Genre> genres,
            List<BookGenreRelation> relations) {
        Map<Long, Book> bookById = booksWithoutGenres.stream().collect(Collectors.toMap(Book::getId, b -> b));
        Map<Long, Genre> genreById = genres.stream().collect(Collectors.toMap(Genre::getId, g -> g));

        relations.forEach(relation -> {
            Book book = bookById.get(relation.bookId());
            Genre genre = genreById.get(relation.genreId());

            if (book != null && genre != null) {
                book.getGenres().add(genre);
            }
        });
    }

    private Book insert(Book book) {
        if (book == null) {
            throw new RuntimeException("book cannot be null");
        }

        var keyHolder = new GeneratedKeyHolder();

        Author author = book.getAuthor();

        try {
            jdbc.update("insert into books (title, author_id) values (:title, :authorId)",
                    new MapSqlParameterSource()
                            .addValue("title", book.getTitle(), Types.VARCHAR)
                            .addValue("authorId", author == null ? null : author.getId(), Types.BIGINT),
                    keyHolder);
        } catch (DataAccessException ex) {
            throw new RuntimeException("Exceptions while inserting book: " + book);
        }

        book.setId(keyHolder.getKeyAs(Long.class));
        batchInsertGenresRelationsFor(book);
        return book;
    }

    private Book update(Book book) {
        if (book == null) {
            throw new RuntimeException("book cannot be null");
        }

        Author author = book.getAuthor();
        int updatingCount;
        try {
            updatingCount = jdbc.update("update books set title = :title, author_id = :authorId where id = :id",
                    new MapSqlParameterSource().addValue("id", book.getId(), Types.BIGINT)
                            .addValue("title", book.getTitle(), Types.VARCHAR)
                            .addValue("authorId", author == null ? null : author.getId(), Types.BIGINT));
        } catch (DataAccessException ex) {
            throw new RuntimeException("Exceptions while updating book: " + book);
        }

        if (updatingCount < 1) {
            throw new EntityNotFoundException("Not found book with id = " + book.getId());
        }

        removeGenresRelationsFor(book);
        batchInsertGenresRelationsFor(book);

        return book;
    }

    private void batchInsertGenresRelationsFor(Book book) {
        if (book == null || book.getGenres() == null) {
            return;
        }

        List<Genre> genres = book.getGenres();

        List<MapSqlParameterSource> paramList = book.getGenres().stream()
                .map(g -> new MapSqlParameterSource()
                        .addValue("bookId", book.getId(), Types.BIGINT)
                        .addValue("genreId", g.getId(), Types.BIGINT))
                .toList();

        try {
            jdbc.batchUpdate("insert into books_genres (book_id, genre_id) values (:bookId, :genreId)",
                    paramList.toArray(new MapSqlParameterSource[genres.size()]));
        } catch (DataAccessException ex) {
            throw new RuntimeException("Exceptions while inserting genres relations: " + book);
        }
    }

    private void removeGenresRelationsFor(Book book) {
        if (book == null || book.getGenres() == null) {
            return;
        }

        long bookId = book.getId();

        try {
            jdbc.update("delete from books_genres where book_id = :bookId",
                    new MapSqlParameterSource().addValue("bookId", bookId));
        } catch (DataAccessException ex) {
            throw new EntityNotFoundException(
                    "Exceptions while deliting genres relations for book with id = " + bookId);
        }
    }

    private static class BookRowMapper implements RowMapper<Book> {

        @Override
        public Book mapRow(ResultSet rs, int rowNum) throws SQLException {
            Author author = new Author(rs.getLong("author_id"), rs.getString("author_full_name"));

            return new Book(rs.getLong("id"), rs.getString("title"), author, new ArrayList<>());
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    @RequiredArgsConstructor
    private static class BookResultSetExtractor implements ResultSetExtractor<Book> {

        @Override
        public Book extractData(ResultSet rs) throws SQLException, DataAccessException {
            Book ent = null;

            Map<Long, Genre> genres = new HashMap<>();

            while (rs.next()) {
                if (ent == null) {
                    ent = new Book();
                    ent.setId(rs.getLong("id"));
                    ent.setTitle(rs.getString("title"));
                    ent.setAuthor(new Author(rs.getLong("author_id"), rs.getString("author_full_name")));
                }

                Long genreId = rs.getLong("genre_id");
                if (!genres.containsKey(genreId)) {
                    genres.put(genreId, new Genre(genreId, rs.getString("genre_name")));
                }
            }

            if (ent != null) {
                ent.setGenres(new ArrayList<>(genres.values()));
            }

            return ent;
        }
    }

    private record BookGenreRelation(long bookId, long genreId) {
    }
}
