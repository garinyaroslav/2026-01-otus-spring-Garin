package ru.otus.hw.services;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.CommentDto;

public interface CommentService {

    Mono<CommentDto> insert(long bookId, String text);

    Mono<Void> deleteById(long id);

    Flux<CommentDto> findAllByBookId(long bookId);

}
