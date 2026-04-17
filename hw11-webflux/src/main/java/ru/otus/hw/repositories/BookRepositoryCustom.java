package ru.otus.hw.repositories;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.Book;

public interface BookRepositoryCustom {

    Flux<Book> findAllWithRelations();

    Mono<Book> findByIdWithRelations(long id);

}
