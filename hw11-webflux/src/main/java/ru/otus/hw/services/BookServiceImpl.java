package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;

import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final R2dbcEntityOperations ops;

    @Override
    public Flux<BookDto> findAll() {
        return bookRepository.findAllWithRelations()
                .map(BookDto::of);
    }

    @Override
    public Mono<BookDto> findById(long id) {
        return bookRepository.findByIdWithRelations(id)
                .switchIfEmpty(Mono.error(
                        new EntityNotFoundException("Book with id %d not found".formatted(id))))
                .map(BookDto::of);
    }

    @Override
    public Mono<BookDto> insert(BookCreateDto dto) {
        return validateAndFetchRelations(dto.getAuthorId(), dto.getGenreIds())
                .flatMap(relations -> {
                    var book = new Book(null, dto.getTitle(), relations.authorId());
                    return bookRepository.save(book)
                            .flatMap(saved -> saveGenreLinks(saved.getId(), relations.genreIds())
                                    .thenReturn(saved))
                            .flatMap(saved -> bookRepository.findByIdWithRelations(saved.getId()))
                            .map(BookDto::of);
                });
    }

    @Override
    public Mono<BookDto> update(BookUpdateDto dto) {
        return bookRepository.findById(dto.getId())
                .switchIfEmpty(Mono.error(
                        new EntityNotFoundException("Book with id %d not found".formatted(dto.getId()))))
                .flatMap(book -> validateAndFetchRelations(dto.getAuthorId(), dto.getGenreIds())
                        .flatMap(relations -> {
                            book.setTitle(dto.getTitle());
                            book.setAuthorId(relations.authorId());
                            return bookRepository.save(book)
                                    .flatMap(saved ->

                                    deleteGenreLinks(saved.getId())
                                            .then(saveGenreLinks(saved.getId(), relations.genreIds()))
                                            .thenReturn(saved))
                                    .flatMap(saved -> bookRepository.findByIdWithRelations(saved.getId()))
                                    .map(BookDto::of);
                        }));
    }

    @Override
    public Mono<Void> deleteById(long id) {
        return deleteGenreLinks(id)
                .then(bookRepository.deleteById(id));
    }

    private record Relations(long authorId, List<Long> genreIds) {
    }

    private Mono<Relations> validateAndFetchRelations(Long authorId, java.util.Set<Long> genreIds) {
        Mono<Long> authorMono = authorRepository.findById(authorId)
                .switchIfEmpty(Mono.error(
                        new EntityNotFoundException("Author with id %d not found".formatted(authorId))))
                .map(a -> a.getId());

        Mono<List<Long>> genresMono = genreRepository.findAllByIdIn(genreIds)
                .collectList()
                .flatMap(found -> {
                    if (found.isEmpty() || found.size() != genreIds.size()) {
                        return Mono.error(new EntityNotFoundException(
                                "One or all genres with ids %s not found".formatted(genreIds)));
                    }
                    return Mono.just(found.stream().map(g -> g.getId()).toList());
                });

        return Mono.zip(authorMono, genresMono)
                .map(t -> new Relations(t.getT1(), t.getT2()));
    }

    private Mono<Void> saveGenreLinks(long bookId, List<Long> genreIds) {
        if (genreIds.isEmpty()) {
            return Mono.empty();
        }
        return Flux.fromIterable(genreIds)
                .flatMap(genreId -> ops.getDatabaseClient()
                        .sql("INSERT INTO books_genres (book_id, genre_id) VALUES (:bookId, :genreId)")
                        .bind("bookId", bookId)
                        .bind("genreId", genreId)
                        .fetch()
                        .rowsUpdated())
                .then();
    }

    private Mono<Void> deleteGenreLinks(long bookId) {
        return ops.getDatabaseClient() // ← changed here
                .sql("DELETE FROM books_genres WHERE book_id = :bookId")
                .bind("bookId", bookId)
                .fetch()
                .rowsUpdated()
                .then();
    }
}
