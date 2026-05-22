package ru.otus.hw.mongoRepositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.otus.hw.mongoModels.CommentMongo;

public interface CommentMongoRepository extends MongoRepository<CommentMongo, String> {
}
