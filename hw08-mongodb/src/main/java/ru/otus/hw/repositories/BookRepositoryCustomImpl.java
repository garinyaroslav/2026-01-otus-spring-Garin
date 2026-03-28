package ru.otus.hw.repositories;

import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import ru.otus.hw.models.Book;

@RequiredArgsConstructor
@Component
public class BookRepositoryCustomImpl implements BookRepositoryCustom {

    private final MongoOperations mongoOperations;

    private final CommentRepository commentRepository;

    @Override
    public void deleteByIdWithCascade(String id) {
        commentRepository.deleteAllByBookId(id);

        mongoOperations.remove(new Query(Criteria.where("_id").is(id)), Book.class);
    }

}
