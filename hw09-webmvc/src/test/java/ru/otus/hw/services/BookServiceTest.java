package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тест сервиса книг")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional(propagation = Propagation.NEVER)
class BookServiceTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private CommentService commentService;

    private List<Author> dbAuthors;
    private List<Genre> dbGenres;

    @BeforeEach
    void setUp() {
        deleteAllBooksAndRelations();

        dbAuthors = getDbAuthors();
        dbGenres = getDbGenres();

        recreateTestBooks();
    }

    private void deleteAllBooksAndRelations() {
        bookService.findAll().forEach(book -> bookService.deleteById(book.getId()));
    }

    private void recreateTestBooks() {
        bookService.insert("Book A", 1L, Set.of(1L));
        bookService.insert("Book B", 2L, Set.of(2L));
        bookService.insert("Book C", 1L, Set.of(3L));
    }

    @DisplayName("findAll должен возвращать книги с инициализированными жанрами и автором")
    @Test
    void findAll_shouldReturnBooksWithInitializedRelations() {
        List<Book> books = bookService.findAll();

        assertThat(books).isNotEmpty();
        assertThat(books).allSatisfy(book -> {
            assertThat(book.getAuthor()).isNotNull();
            assertThat(book.getAuthor().getFullName()).isNotBlank();
            assertThat(book.getGenres()).isNotNull().isNotEmpty();
        });
    }

    @DisplayName("findById должен возвращать книгу с инициализированными связями")
    @ParameterizedTest
    @MethodSource("getDbBooks")
    void shouldReturnCorrectBookById(Book expectedBook) {
        Optional<Book> result = bookService.findAll().stream()
                .filter(b -> b.getTitle().equals(expectedBook.getTitle()))
                .findFirst();

        assertThat(result)
                .as("Книга '%s' должна существовать", expectedBook.getTitle())
                .isPresent()
                .get()
                .usingRecursiveComparison()
                .ignoringFields("id", "genres", "author.fullName", "author.books", "comments")
                .isEqualTo(expectedBook);

        Book actual = result.get();
        assertThat(actual.getAuthor().getFullName()).isNotBlank();
        assertThat(actual.getGenres()).isNotEmpty();
    }

    @DisplayName("insert должен сохранять книгу и возвращать её с корректными связями")
    @Test
    void insert_shouldPersistAndReturnBookWithRelations() {
        Book actual = bookService.insert("New Book", 1L, Set.of(1L, 2L));

        Book expected = new Book(0L, "New Book", dbAuthors.get(0),
                List.of(dbGenres.get(0), dbGenres.get(1)), List.of());

        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("id", "author.fullName", "author.books", "genres", "comments")
                .isEqualTo(expected);
        assertThat(actual.getId()).isGreaterThan(0);
        assertThat(actual.getGenres()).hasSize(2);
        assertThat(actual.getAuthor().getFullName()).isNotBlank();
    }

    @DisplayName("update должен обновлять книгу и возвращать актуальные данные")
    @Test
    void update_shouldUpdateAndReturnBook() {
        Book toUpdate = bookService.insert("Original Title", 1L, Set.of(1L));

        Book actual = bookService.update(toUpdate.getId(), "Updated Title", 2L, Set.of(2L, 3L));

        Book expected = new Book(toUpdate.getId(), "Updated Title",
                dbAuthors.get(1), List.of(dbGenres.get(1), dbGenres.get(2)), List.of());

        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("author.fullName", "author.books", "genres", "comments")
                .isEqualTo(expected);
        assertThat(actual.getAuthor()).isNotNull();
        assertThat(actual.getGenres()).hasSize(2);
        assertThat(actual.getAuthor().getFullName()).isNotBlank();
    }

    @DisplayName("deleteById должен удалять книгу и все связанные с ней комментарии")
    @Test
    void deleteById_shouldDeleteBook() {
        Book bookToDelete = bookService.insert("Book To Delete", 1L, Set.of(1L));
        long bookId = bookToDelete.getId();

        commentService.insert(bookId, "Comment 1");
        commentService.insert(bookId, "Comment 2");

        assertThat(bookService.findById(bookId)).isPresent();
        assertThat(commentService.findAllByBookId(bookId)).hasSize(2);

        bookService.deleteById(bookId);

        assertThat(bookService.findById(bookId)).isEmpty();
        assertThat(commentService.findAllByBookId(bookId)).isEmpty();
    }

    private static List<Author> getDbAuthors() {
        return List.of(
                new Author(1L, "Author A", null),
                new Author(2L, "Author B", null));
    }

    private static List<Genre> getDbGenres() {
        return List.of(
                new Genre(1L, "Genre1"),
                new Genre(2L, "Genre2"),
                new Genre(3L, "Genre3"));
    }

    private static List<Book> getDbBooks(List<Author> authors, List<Genre> genres) {
        return List.of(
                new Book(0L, "Book A", authors.get(0), List.of(genres.get(0)), List.of()),
                new Book(0L, "Book B", authors.get(1), List.of(genres.get(1)), List.of()),
                new Book(0L, "Book C", authors.get(0), List.of(genres.get(2)), List.of()));
    }

    private static Stream<Book> getDbBooks() {
        return getDbBooks(getDbAuthors(), getDbGenres()).stream();
    }
}
