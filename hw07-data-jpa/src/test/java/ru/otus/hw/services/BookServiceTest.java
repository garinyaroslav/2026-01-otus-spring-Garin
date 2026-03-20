package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.models.Book;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("тест сервиса книг")
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

        assertThatCode(() -> books.forEach(book -> {
            assertThat(book.getAuthor()).isNotNull();
            assertThat(book.getAuthor().getFullName()).isNotBlank();
            assertThat(book.getGenres()).isNotNull();
        })).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("findById возвращает книгу с инициализированными автором и жанрами")
    void findById_shouldReturnBookWithInitializedRelations() {
        Optional<Book> result = bookService.findById(1L);

        assertThat(result).isPresent();

        assertThatCode(() -> {
            Book book = result.get();
            assertThat(book.getAuthor()).isNotNull();
            assertThat(book.getAuthor().getFullName()).isNotBlank();
            assertThat(book.getGenres()).isNotEmpty();
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("insert сохраняет и возвращает книгу с доступными связями")
    void insert_shouldPersistAndReturnBookWithRelations() {
        Book book = bookService.insert("New Book", 1L, Set.of(1L, 2L));

        assertThat(book).isNotNull();
        assertThat(book.getId()).isGreaterThan(0);

        assertThatCode(() -> {
            assertThat(book.getAuthor().getFullName()).isNotBlank();
            assertThat(book.getGenres()).hasSize(2);
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("update обновляет книгу и возвращает актуальные данные")
    void update_shouldUpdateAndReturnBook() {
        Book book = bookService.update(1L, "Updated Title", 1L, Set.of(1L));

        assertThat(book.getTitle()).isEqualTo("Updated Title");

        assertThatCode(() -> {
            assertThat(book.getAuthor()).isNotNull();
            assertThat(book.getGenres()).hasSize(1);
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("deleteById удаляет книгу")
    void deleteById_shouldDeleteBook() {
        bookService.insert("To Delete", 1L, Set.of(1L));
        List<Book> before = bookService.findAll();
        long idToDelete = before.get(before.size() - 1).getId();

        bookService.deleteById(idToDelete);

        assertThat(bookService.findById(idToDelete)).isEmpty();
    }
}
