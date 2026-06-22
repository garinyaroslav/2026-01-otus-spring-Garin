package ru.otus.hw.services;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Comment;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;

@RequiredArgsConstructor
@Service
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    private final BookRepository bookRepository;

    @Override
    @Transactional(readOnly = true)
    @Retry(name = "db")
    @CircuitBreaker(name = "db")
    public Optional<CommentDto> findById(long id) {
        return commentRepository.findById(id).map(CommentDto::of);
    }

    @Override
    @Transactional(readOnly = true)
    @Retry(name = "db")
    @CircuitBreaker(name = "db")
    public List<CommentDto> findAllByBookId(long bookId) {
        return CommentDto.fromList(commentRepository.findAllByBookId(bookId));
    }

    @Override
    @Transactional
    @Retry(name = "db")
    @CircuitBreaker(name = "db")
    public CommentDto insert(long bookId, String text) {
        var book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book with id %d not found".formatted(bookId)));
        var comment = new Comment(0, text, book);
        return CommentDto.of(commentRepository.save(comment));
    }

    @Override
    @Transactional
    @Retry(name = "db")
    @CircuitBreaker(name = "db")
    public CommentDto update(long id, String text) {
        var comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comment with id %d not found".formatted(id)));
        comment.setText(text);
        return CommentDto.of(commentRepository.save(comment));
    }

    @Override
    @Transactional
    @Retry(name = "db")
    @CircuitBreaker(name = "db")
    public void deleteById(long id) {
        commentRepository.deleteById(id);
    }
}
