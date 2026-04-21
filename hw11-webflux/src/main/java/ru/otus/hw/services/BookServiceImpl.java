package ru.otus.hw.services;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;
import ru.otus.hw.models.Relations;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final CommentRepository commentRepository;

    @Override
    public Flux<BookDto> findAll() {
        return bookRepository.findAllWithGenreIds()
                .collectList()
                .flatMapMany(this::toBookDtos);
    }

    @Override
    public Mono<BookDto> findById(long id) {
        return bookRepository.findByIdWithGenreIds(id)
                .switchIfEmpty(Mono.error(
                        new EntityNotFoundException("Book with id %d not found".formatted(id))))
                .flatMap(this::toBookDto);
    }

    @Override
    public Mono<BookDto> insert(BookCreateDto dto) {
        return validateAndFetchRelations(dto.getAuthorId(), dto.getGenreIds())
                .flatMap(relations -> {
                    var book = new Book(null, dto.getTitle(), relations.authorId());
                    return bookRepository.save(book)
                            .flatMap(saved -> bookRepository.saveGenreLinks(saved.getId(), relations.genreIds())
                                    .thenReturn(saved))
                            .flatMap(saved -> bookRepository.findByIdWithGenreIds(saved.getId())
                                    .flatMap(this::toBookDto));
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
                                    .flatMap(saved -> bookRepository.deleteGenreLinks(saved.getId())
                                            .then(bookRepository.saveGenreLinks(saved.getId(), relations.genreIds()))
                                            .thenReturn(saved))
                                    .flatMap(saved -> bookRepository.findByIdWithGenreIds(saved.getId())
                                            .flatMap(this::toBookDto));
                        }));
    }

    @Override
    public Mono<Void> deleteById(long id) {
        return bookRepository.deleteGenreLinks(id)
                .then(bookRepository.deleteById(id));
    }

    private Mono<Relations> validateAndFetchRelations(Long authorId, Set<Long> genreIds) {
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

    private Mono<BookDto> toBookDto(Book book) {
        Mono<Author> authorMono = authorRepository.findById(book.getAuthorId())
                .switchIfEmpty(Mono.error(
                        new EntityNotFoundException("Author with id %d not found".formatted(book.getAuthorId()))));

        Mono<List<Genre>> genresMono = (book.getGenreIds() == null || book.getGenreIds().isEmpty())
                ? Mono.just(List.of())
                : genreRepository.findAllById(book.getGenreIds()).collectList();

        Mono<List<Comment>> commentsMono = commentRepository.findAllByBookId(book.getId()).collectList();

        return Mono.zip(authorMono, genresMono, commentsMono)
                .map(t -> BookDto.of(book, t.getT1(), t.getT2(), t.getT3()));
    }

    private Flux<BookDto> toBookDtos(List<Book> books) {
        if (books.isEmpty()) {
            return Flux.empty();
        }

        return Mono.zip(
                loadAuthorMap(books),
                loadGenreMap(books),
                loadCommentMap(books)).flatMapMany(
                        maps -> Flux.fromIterable(books)
                                .map(book -> mapToBookDto(book, maps.getT1(), maps.getT2(), maps.getT3())));
    }

    private Mono<Map<Long, Author>> loadAuthorMap(List<Book> books) {
        Set<Long> authorIds = books.stream()
                .map(Book::getAuthorId)
                .collect(Collectors.toSet());
        return authorRepository.findAllById(authorIds)
                .collectMap(Author::getId);
    }

    private Mono<Map<Long, Genre>> loadGenreMap(List<Book> books) {
        Set<Long> genreIdsSet = books.stream()
                .filter(b -> b.getGenreIds() != null)
                .flatMap(b -> b.getGenreIds().stream())
                .collect(Collectors.toSet());

        return genreIdsSet.isEmpty()
                ? Mono.just(Map.of())
                : genreRepository.findAllById(genreIdsSet)
                        .collectMap(Genre::getId);
    }

    private Mono<Map<Long, List<Comment>>> loadCommentMap(List<Book> books) {
        Set<Long> bookIds = books.stream()
                .map(Book::getId)
                .collect(Collectors.toSet());

        return commentRepository.findAllByBookIdIn(bookIds)
                .collectList()
                .map(comments -> comments.stream()
                        .collect(Collectors.groupingBy(
                                Comment::getBookId,
                                Collectors.toList())));
    }

    private BookDto mapToBookDto(Book book,
            Map<Long, Author> authorMap,
            Map<Long, Genre> genreMap,
            Map<Long, List<Comment>> commentMap) {
        Author author = authorMap.get(book.getAuthorId());

        List<Genre> genres = book.getGenreIds() != null
                ? book.getGenreIds().stream()
                        .map(genreMap::get)
                        .toList()
                : List.of();

        List<Comment> comments = commentMap.getOrDefault(book.getId(), List.of());

        return BookDto.of(book, author, genres, comments);
    }
}
