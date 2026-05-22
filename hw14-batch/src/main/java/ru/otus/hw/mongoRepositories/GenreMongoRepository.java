package ru.otus.hw.mongoRepositories;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import ru.otus.hw.mongoModels.GenreMongo;

public interface GenreMongoRepository extends MongoRepository<GenreMongo, String> {

    Optional<GenreMongo> findBySourceId(Long sourceId);

}
