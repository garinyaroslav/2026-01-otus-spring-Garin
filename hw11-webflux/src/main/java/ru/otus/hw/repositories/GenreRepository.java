package ru.otus.hw.repositories;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.otus.hw.models.Genre;

import java.util.Set;

public interface GenreRepository extends ReactiveCrudRepository<Genre, Long> {

    Flux<Genre> findAllByIdIn(Set<Long> ids);

}
