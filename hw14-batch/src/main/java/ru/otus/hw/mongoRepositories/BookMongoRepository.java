package ru.otus.hw.mongoRepositories;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import ru.otus.hw.mongoModels.BookMongo;

public interface BookMongoRepository extends MongoRepository<BookMongo, String> {

    Optional<BookMongo> findBySourceId(Long sourceId);

}
