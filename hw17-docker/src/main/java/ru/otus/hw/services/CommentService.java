package ru.otus.hw.services;

import java.util.List;
import java.util.Optional;

import ru.otus.hw.dto.CommentDto;

public interface CommentService {

    Optional<CommentDto> findById(long id);

    List<CommentDto> findAllByBookId(long bookId);

    CommentDto insert(long bookId, String text);

    CommentDto update(long id, String text);

    void deleteById(long id);

}
