package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.Book;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class BookRepositoryCustomImpl implements BookRepositoryCustom {

    private final R2dbcEntityOperations ops;

    @Override
    public Flux<Book> findAllWithGenreIds() {
        return ops.select(Query.empty(), Book.class)
                .collectList()
                .flatMapMany(books -> {
                    if (books.isEmpty()) {
                        return Flux.empty();
                    }
                    Set<Long> bookIds = books.stream()
                            .map(Book::getId)
                            .collect(Collectors.toSet());

                    return loadGenreIdMap(bookIds)
                            .flatMapMany(genreIdMap -> Flux.fromIterable(books).map(book -> {
                                book.setGenreIds(
                                        genreIdMap.getOrDefault(book.getId(), List.of()));
                                return book;
                            }));
                });
    }

    @Override
    public Mono<Book> findByIdWithGenreIds(long id) {
        return ops.selectOne(Query.query(Criteria.where("id").is(id)), Book.class)
                .flatMap(book -> loadGenreIdMap(Set.of(book.getId()))
                        .map(genreIdMap -> {
                            book.setGenreIds(
                                    genreIdMap.getOrDefault(book.getId(), List.of()));
                            return book;
                        }));
    }

    @Override
    public Mono<Void> saveGenreLinks(long bookId, List<Long> genreIds) {
        if (genreIds.isEmpty()) {
            return Mono.empty();
        }
        return Flux.fromIterable(genreIds)
                .flatMap(genreId -> ops.getDatabaseClient()
                        .sql("INSERT INTO books_genres (book_id, genre_id) VALUES (:bookId, :genreId)")
                        .bind("bookId", bookId)
                        .bind("genreId", genreId)
                        .fetch()
                        .rowsUpdated())
                .then();
    }

    @Override
    public Mono<Void> deleteGenreLinks(long bookId) {
        return ops.getDatabaseClient()
                .sql("DELETE FROM books_genres WHERE book_id = :bookId")
                .bind("bookId", bookId)
                .fetch()
                .rowsUpdated()
                .then();
    }

    private Mono<Map<Long, List<Long>>> loadGenreIdMap(Set<Long> bookIds) {
        return ops.getDatabaseClient()
                .sql("SELECT book_id, genre_id FROM books_genres WHERE book_id IN (:ids)")
                .bind("ids", bookIds)
                .fetch()
                .all()
                .collectList()
                .map(rows -> rows.stream()
                        .collect(Collectors.groupingBy(
                                r -> ((Number) r.get("book_id")).longValue(),
                                Collectors.mapping(
                                        r -> ((Number) r.get("genre_id")).longValue(),
                                        Collectors.toList()))));
    }

}
