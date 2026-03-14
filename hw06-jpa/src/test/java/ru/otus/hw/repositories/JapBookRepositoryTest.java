package ru.otus.hw.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий книг")
@DataJpaTest
@Import(JpaBookRepository.class)
class JpaBookRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private JpaBookRepository bookRepository;

    private Author author;
    private Genre genre1;
    private Genre genre2;

    @BeforeEach
    void setUp() {
        author = em.persistAndFlush(new Author(0, "Test Author", null));
        genre1 = em.persistAndFlush(new Genre(0, "Genre1"));
        genre2 = em.persistAndFlush(new Genre(0, "Genre2"));
    }

    @DisplayName("должен находить книгу по id с автором и жанрами")
    @Test
    void shouldFindById() {
        var book = em.persistAndFlush(new Book(0, "Book1", author, List.of(genre1, genre2), List.of()));

        var found = bookRepository.findById(book.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Book1");
        assertThat(found.get().getAuthor().getFullName()).isEqualTo("Test Author");
        assertThat(found.get().getGenres()).hasSize(2);
    }

    @DisplayName("должен загружать все книги с авторами и жанрами")
    @Test
    void shouldFindAll() {
        em.persistAndFlush(new Book(0, "Book A", author, List.of(genre1), List.of()));
        em.persistAndFlush(new Book(0, "Book B", author, List.of(genre2), List.of()));

        var books = bookRepository.findAll();

        assertThat(books).hasSizeGreaterThanOrEqualTo(2);
        books.forEach(b -> {
            assertThat(b.getAuthor()).isNotNull();
            assertThat(b.getGenres()).isNotNull();
        });
    }

    @DisplayName("должен сохранять новую книгу")
    @Test
    void shouldInsertBook() {
        var book = new Book(0, "New Book", author, List.of(genre1), List.of());
        var saved = bookRepository.save(book);

        assertThat(saved.getId()).isGreaterThan(0);

        var found = em.find(Book.class, saved.getId());
        assertThat(found).isNotNull();
        assertThat(found.getTitle()).isEqualTo("New Book");
    }

    @DisplayName("должен обновлять существующую книгу")
    @Test
    void shouldUpdateBook() {
        var book = em.persistAndFlush(
                new Book(0, "Old Title", author, new ArrayList<>(List.of(genre1)), new ArrayList<>(List.of())));

        var updated = new Book(book.getId(), "New Title", author, new ArrayList<>(List.of(genre1, genre2)),
                new ArrayList<>(List.of()));
        bookRepository.save(updated);
        em.flush();
        em.clear();

        var found = em.find(Book.class, book.getId());
        assertThat(found.getTitle()).isEqualTo("New Title");
    }

    @DisplayName("должен удалять книгу по id")
    @Test
    void shouldDeleteById() {
        var book = em.persistAndFlush(new Book(0, "To Delete", author, List.of(genre1), List.of()));
        long id = book.getId();

        bookRepository.deleteById(id);
        em.flush();

        assertThat(em.find(Book.class, id)).isNull();
    }

}
