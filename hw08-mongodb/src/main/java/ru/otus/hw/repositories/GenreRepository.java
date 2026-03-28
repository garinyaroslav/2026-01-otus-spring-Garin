package ru.otus.hw.repositories;

import java.util.List;
import java.util.Set;

import org.springframework.data.mongodb.repository.MongoRepository;

import ru.otus.hw.models.Genre;

public interface GenreRepository extends MongoRepository<Genre, String> {

    List<Genre> findAllByIdIn(Set<String> ids);

}
