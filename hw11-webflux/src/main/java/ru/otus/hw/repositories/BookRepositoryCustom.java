package ru.otus.hw.repositories;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.Book;

import java.util.List;

public interface BookRepositoryCustom {

    Flux<Book> findAllWithGenreIds();

    Mono<Book> findByIdWithGenreIds(long id);

    Mono<Void> saveGenreLinks(long bookId, List<Long> genreIds);

    Mono<Void> deleteGenreLinks(long bookId);

}
