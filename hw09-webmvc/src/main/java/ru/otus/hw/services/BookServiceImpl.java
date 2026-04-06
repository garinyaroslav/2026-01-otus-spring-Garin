package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {
    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    @Override
    public Optional<Book> findById(long id) {
        return bookRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Book> findAll() {
        var books = bookRepository.findAll();
        books.forEach(b -> b.getGenres().size());

        return books;
    }

    @Transactional
    @Override
    public Book insert(BookCreateDto dto) {
        Long authorId = dto.getAuthorId();
        Set<Long> genresIds = dto.getGenreIds();

        if (isEmpty(genresIds)) {
            throw new IllegalArgumentException("Genres ids must not be null");
        }

        var author = authorRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("Author with id %d not found".formatted(authorId)));

        var genres = genreRepository.findAllByIdIn(genresIds);

        if (isEmpty(genres) || genresIds.size() != genres.size()) {
            throw new EntityNotFoundException("One or all genres with ids %s not found".formatted(genresIds));
        }

        return bookRepository.save(new Book(0L, dto.getTitle(), author, genres, List.of()));
    }

    @Transactional
    @Override
    public Book update(BookUpdateDto dto) {
        Long id = dto.getId();
        Set<Long> genresIds = dto.getGenreIds();
        Long authorId = dto.getAuthorId();

        var book = bookRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Book with id %d not found".formatted(id)));

        var author = authorRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("Author with id %d not found".formatted(authorId)));

        var genres = genreRepository.findAllByIdIn(genresIds);
        if (isEmpty(genres) || genresIds.size() != genres.size()) {
            throw new EntityNotFoundException("One or all genres with ids %s not found".formatted(genresIds));
        }

        book.setTitle(dto.getTitle());
        book.setAuthor(author);
        book.setGenres(genres);

        return bookRepository.save(book);
    }

    @Transactional
    @Override
    public void deleteById(long id) {
        bookRepository.deleteById(id);
    }

}
