package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тест сервиса книг")
@SpringBootTest
@Transactional(propagation = Propagation.NEVER)
class BookServiceTest {

    @Autowired
    private BookService bookService;

    @Test
    @DisplayName("findAll возвращает книги с инициализированными жанрами и авторами")
    void findAll_shouldReturnBooksWithInitializedRelations() {
        List<Book> books = bookService.findAll();

        assertThat(books).isNotEmpty();
        assertThat(books).allSatisfy(book -> {
            assertThat(book.getAuthor()).isNotNull();
            assertThat(book.getAuthor().getFullName()).isNotBlank();
            assertThat(book.getGenres()).isNotNull().isNotEmpty();
        });
    }

    @Test
    @DisplayName("findById возвращает книгу с инициализированными автором и жанрами")
    void findById_shouldReturnBookWithInitializedRelations() {
        Optional<Book> result = bookService.findById(2L);
        assertThat(result).isPresent();

        Book expected = new Book();
        expected.setId(2L);
        expected.setTitle("Book B");

        Author expectedAuthor = new Author();
        expectedAuthor.setId(2L);
        expected.setAuthor(expectedAuthor);

        assertThat(result.get())
                .usingRecursiveComparison()
                .ignoringFields("genres", "author.fullName", "author.books")
                .isEqualTo(expected);
        assertThat(result.get().getAuthor().getFullName()).isNotBlank();
        assertThat(result.get().getGenres()).isNotEmpty();
    }

    @Test
    @DisplayName("insert сохраняет и возвращает книгу с доступными связями")
    void insert_shouldPersistAndReturnBookWithRelations() {
        Book actual = bookService.insert("New Book", 1L, Set.of(1L, 2L));

        Book expected = new Book();
        expected.setTitle("New Book");

        Author expectedAuthor = new Author();
        expectedAuthor.setId(1L);
        expected.setAuthor(expectedAuthor);

        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("id", "author.fullName", "author.books", "genres")
                .isEqualTo(expected);
        assertThat(actual.getId()).isGreaterThan(0);
        assertThat(actual.getGenres()).hasSize(2);
        assertThat(actual.getAuthor().getFullName()).isNotBlank();
    }

    @Test
    @DisplayName("update обновляет книгу и возвращает актуальные данные")
    void update_shouldUpdateAndReturnBook() {
        Book toUpdate = bookService.insert("Original Title", 1L, Set.of(1L));
        Book actual = bookService.update(toUpdate.getId(), "Updated Title", 1L, Set.of(1L));

        Book expected = new Book();
        expected.setId(toUpdate.getId());
        expected.setTitle("Updated Title");

        Author expectedAuthor = new Author();
        expectedAuthor.setId(1L);
        expected.setAuthor(expectedAuthor);

        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("author.fullName", "author.books", "genres")
                .isEqualTo(expected);
        assertThat(actual.getAuthor()).isNotNull();
        assertThat(actual.getGenres()).hasSize(1);
    }

    @Test
    @DisplayName("deleteById удаляет книгу")
    void deleteById_shouldDeleteBook() {
        Book inserted = bookService.insert("To Delete", 1L, Set.of(1L));

        bookService.deleteById(inserted.getId());

        assertThat(bookService.findById(inserted.getId())).isEmpty();
    }

}
