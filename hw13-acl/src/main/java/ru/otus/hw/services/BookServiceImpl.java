package ru.otus.hw.services;

import static org.springframework.util.CollectionUtils.isEmpty;

import java.util.List;
import java.util.Set;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    private final BookRepository bookRepository;

    @Override
    @PreAuthorize("isAuthenticated()")
    public BookDto findById(long id) {
        return bookRepository.findById(id).map(BookDto::of)
                .orElseThrow(() -> new EntityNotFoundException("Book with id %d not found".formatted(id)));
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public List<BookDto> findAll() {
        var books = bookRepository.findAll();
        books.forEach(b -> b.getGenres().size());
        return BookDto.fromList(books);
    }

    @Transactional
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public BookDto insert(BookCreateDto dto) {
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

        var saved = bookRepository.save(new Book(0L, dto.getTitle(), author, genres, List.of()));
        return BookDto.of(saved);
    }

    @Transactional
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public BookDto update(BookUpdateDto dto) {
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

        var saved = bookRepository.save(book);
        return BookDto.of(saved);
    }

    @Transactional
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteById(long id) {
        bookRepository.deleteById(id);
    }

}
