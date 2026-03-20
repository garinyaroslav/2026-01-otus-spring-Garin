package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий книг")
@DataJpaTest
class BookRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private BookRepository bookRepository;

    @DisplayName("должен находить книгу по id с автором и жанрами")
    @Test
    void shouldFindById() {
        var expected = new Book(1L, "Book A",
                new Author(1L, "Author A", null),
                List.of(new Genre(1L, "Genre1")));

        var found = bookRepository.findById(1L);

        assertThat(found).isPresent();
        assertThat(found.get())
                .usingRecursiveComparison()
                .ignoringFields("author.books", "genres.books")
                .isEqualTo(expected);
    }

    @DisplayName("должен загружать все книги с авторами и жанрами")
    @Test
    void shouldFindAll() {
        var books = bookRepository.findAll();

        assertThat(books).hasSize(2);
        assertThat(books)
                .usingRecursiveComparison()
                .ignoringFields("author.books", "genres.books")
                .isEqualTo(List.of(
                        new Book(1L, "Book A", new Author(1L, "Author A", null),
                                List.of(new Genre(1L, "Genre1"))),
                        new Book(2L, "Book B", new Author(2L, "Author B", null),
                                List.of(new Genre(2L, "Genre2")))));
    }

    @DisplayName("должен сохранять новую книгу")
    @Test
    void shouldInsertBook() {
        var author = em.find(Author.class, 1L);
        var genre = em.find(Genre.class, 1L);
        var newBook = new Book(0, "New Book", author, List.of(genre));
        var saved = bookRepository.save(newBook);
        em.flush();
        em.clear();

        var found = em.find(Book.class, saved.getId());

        assertThat(found)
                .usingRecursiveComparison()
                .ignoringFields("id", "author.books", "genres.books")
                .withEqualsForType((a, b) -> a.getId() == b.getId(), Author.class)
                .withEqualsForType((a, b) -> a.getId() == b.getId(), Genre.class)
                .isEqualTo(newBook);
    }

    @DisplayName("должен обновлять существующую книгу")
    @Test
    void shouldUpdateBook() {
        var author = em.find(Author.class, 1L);
        var genre1 = em.find(Genre.class, 1L);
        var genre2 = em.find(Genre.class, 2L);
        var updated = new Book(1L, "Updated Title", author,
                new ArrayList<>(List.of(genre1, genre2)));
        bookRepository.save(updated);
        em.flush();
        em.clear();

        var found = em.find(Book.class, 1L);

        assertThat(found)
                .usingRecursiveComparison()
                .ignoringFields("author.books", "genres.books")
                .withEqualsForType((a, b) -> a.getId() == b.getId(), Author.class)
                .withEqualsForType((a, b) -> a.getId() == b.getId(), Genre.class)
                .isEqualTo(updated);
    }

    @DisplayName("должен удалять книгу по id")
    @Test
    void shouldDeleteById() {
        bookRepository.deleteById(1L);
        em.flush();

        assertThat(em.find(Book.class, 1L)).isNull();
    }

}
