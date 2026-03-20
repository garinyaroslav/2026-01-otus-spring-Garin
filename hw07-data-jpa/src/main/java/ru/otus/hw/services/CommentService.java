package ru.otus.hw.services;

import java.util.List;
import java.util.Optional;

import ru.otus.hw.models.Comment;

public interface CommentService {

    Optional<Comment> findById(long id);

    List<Comment> findAllByBookId(long bookId);

    Comment insert(long bookId, String text);

    Comment update(long id, String text);

    void deleteById(long id);

}
