package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class BookRepositoryCustomImpl implements BookRepositoryCustom {

    private final R2dbcEntityOperations ops;

    @Override
    public Flux<Book> findAllWithRelations() {
        return ops.select(Query.empty(), Book.class)
                .collectList()
                .flatMapMany(books -> {
                    if (books.isEmpty()) {
                        return Flux.empty();
                    }

                    Set<Long> authorIds = books.stream()
                            .map(Book::getAuthorId)
                            .collect(Collectors.toSet());

                    Set<Long> bookIds = books.stream()
                            .map(Book::getId)
                            .collect(Collectors.toSet());

                    Mono<Map<Long, Author>> authorMapMono = ops
                            .select(Query.query(Criteria.where("id").in(authorIds)), Author.class)
                            .collectMap(Author::getId);

                    Mono<Map<Long, List<Genre>>> genreMapMono = fetchGenresForBooks(bookIds);

                    return Mono.zip(authorMapMono, genreMapMono)
                            .flatMapMany(tuple -> {
                                Map<Long, Author> authorMap = tuple.getT1();
                                Map<Long, List<Genre>> genreMap = tuple.getT2();

                                return Flux.fromIterable(books).map(book -> {
                                    book.setAuthor(authorMap.get(book.getAuthorId()));
                                    book.setGenres(genreMap.getOrDefault(book.getId(), List.of()));
                                    book.setComments(List.of());
                                    return book;
                                });
                            });
                });
    }

    @Override
    public Mono<Book> findByIdWithRelations(long id) {
        return ops.selectOne(Query.query(Criteria.where("id").is(id)), Book.class)
                .flatMap(book -> {
                    Mono<Author> authorMono = ops
                            .selectOne(Query.query(Criteria.where("id").is(book.getAuthorId())),
                                    Author.class);

                    Mono<List<Genre>> genresMono = fetchGenresForBooks(Set.of(book.getId()))
                            .map(m -> m.getOrDefault(book.getId(), List.of()));

                    Mono<List<Comment>> commentsMono = ops
                            .select(Query.query(Criteria.where("book_id").is(book.getId())),
                                    Comment.class)
                            .collectList();

                    return Mono.zip(authorMono, genresMono, commentsMono)
                            .map(tuple -> {
                                book.setAuthor(tuple.getT1());
                                book.setGenres(tuple.getT2());
                                book.setComments(tuple.getT3());
                                return book;
                            });
                });
    }

    private Mono<Map<Long, List<Genre>>> fetchGenresForBooks(Set<Long> bookIds) {
        return ops.getDatabaseClient()
                .sql("SELECT bg.book_id, bg.genre_id FROM books_genres bg WHERE bg.book_id IN (:ids)")
                .bind("ids", bookIds)
                .fetch()
                .all()
                .collectList()
                .flatMap(rows -> {
                    if (rows.isEmpty()) {
                        return Mono.just(Map.of());
                    }

                    Map<Long, List<Long>> bookToGenreIds = rows.stream()
                            .collect(Collectors.groupingBy(
                                    r -> ((Number) r.get("book_id")).longValue(),
                                    Collectors.mapping(
                                            r -> ((Number) r.get("genre_id")).longValue(),
                                            Collectors.toList())));

                    Set<Long> allGenreIds = rows.stream()
                            .map(r -> ((Number) r.get("genre_id")).longValue())
                            .collect(Collectors.toSet());

                    return ops.select(
                            Query.query(Criteria.where("id").in(allGenreIds)), Genre.class)
                            .collectMap(Genre::getId)
                            .map(genreById -> bookToGenreIds.entrySet().stream()
                                    .collect(Collectors.toMap(
                                            Map.Entry::getKey,
                                            e -> e.getValue().stream()
                                                    .map(genreById::get)
                                                    .collect(Collectors.toList()))));
                });
    }
}
