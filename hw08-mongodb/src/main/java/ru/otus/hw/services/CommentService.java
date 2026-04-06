package ru.otus.hw.services;

import java.util.List;
import java.util.Optional;

import ru.otus.hw.models.Comment;

public interface CommentService {

    Optional<Comment> findById(String id);

    List<Comment> findAllByBookId(String bookId);

    Comment insert(String bookId, String text);

    Comment update(String id, String text);

    void deleteById(String id);

}
