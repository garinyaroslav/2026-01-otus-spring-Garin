package ru.otus.hw.services;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;

public interface BookService {

    Flux<BookDto> findAll();

    Mono<BookDto> findById(long id);

    Mono<BookDto> insert(BookCreateDto dto);

    Mono<BookDto> update(BookUpdateDto dto);

    Mono<Void> deleteById(long id);

}
