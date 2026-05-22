package ru.otus.hw.mongoRepositories;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import ru.otus.hw.mongoModels.AuthorMongo;

public interface AuthorMongoRepository extends MongoRepository<AuthorMongo, String> {

    Optional<AuthorMongo> findBySourceId(Long sourceId);

}
