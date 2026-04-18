package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Comment;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    private final BookRepository bookRepository;

    @Override
    public Mono<CommentDto> insert(long bookId, String text) {
        return bookRepository.findById(bookId)
                .switchIfEmpty(Mono.error(
                        new EntityNotFoundException("Book with id %d not found".formatted(bookId))))
                .flatMap(book -> commentRepository.save(new Comment(null, text, book.getId())))
                .map(CommentDto::of);
    }

    @Override
    public Mono<Void> deleteById(long id) {
        return commentRepository.deleteById(id);
    }

    @Override
    public Flux<CommentDto> findAllByBookId(long bookId) {
        return CommentDto.fromList(commentRepository.findAllByBookId(bookId));
    }

}
