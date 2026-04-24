package ru.otus.hw.repositories;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.Comment;

import java.util.Collection;

public interface CommentRepository extends ReactiveCrudRepository<Comment, Long> {

    Flux<Comment> findAllByBookId(long bookId);

    Flux<Comment> findAllByBookIdIn(Collection<Long> bookIds);

    Mono<Void> deleteAllByBookId(long bookId);

}
