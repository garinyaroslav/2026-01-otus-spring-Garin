package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тест сервиса книг")
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Transactional(propagation = Propagation.NEVER)
class BookServiceTest {

    @Container
    static final MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:7.0.14"));

    @DynamicPropertySource
    static void setMongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private BookService bookService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private GenreRepository genreRepository;

    private List<Author> testAuthors;
    private List<Genre> testGenres;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        bookRepository.deleteAll();

        testAuthors = List.of(
                authorRepository.save(new Author(null, "Test Author 1")),
                authorRepository.save(new Author(null, "Test Author 2")));

        testGenres = List.of(
                genreRepository.save(new Genre(null, "Test Genre 1")),
                genreRepository.save(new Genre(null, "Test Genre 2")),
                genreRepository.save(new Genre(null, "Test Genre 3")));

        bookService.insert("Book A", testAuthors.get(0).getId(), Set.of(testGenres.get(0).getId()));
        bookService.insert("Book B", testAuthors.get(1).getId(), Set.of(testGenres.get(1).getId()));
        bookService.insert("Book C", testAuthors.get(0).getId(), Set.of(testGenres.get(2).getId()));
    }

    @DisplayName("findAll должен возвращать книги с инициализированными жанрами и автором")
    @Test
    void findAll_shouldReturnBooksWithInitializedRelations() {
        List<Book> books = bookService.findAll();

        assertThat(books).hasSize(3);
        assertThat(books).allSatisfy(book -> {
            assertThat(book.getAuthor()).isNotNull();
            assertThat(book.getAuthor().getFullName()).isNotBlank();
            assertThat(book.getGenres()).isNotNull().isNotEmpty();
        });
    }

    @DisplayName("findById должен возвращать книгу с инициализированными связями")
    @ParameterizedTest
    @MethodSource("getTestBookTitles")
    void shouldReturnCorrectBookById(String expectedTitle) {
        Optional<Book> result = bookService.findAll().stream()
                .filter(b -> b.getTitle().equals(expectedTitle))
                .findFirst();

        assertThat(result)
                .as("Книга '%s' должна существовать", expectedTitle)
                .isPresent();

        assertThat(result.get().getTitle()).isEqualTo(expectedTitle);
        assertThat(result.get().getAuthor()).isNotNull();
        assertThat(result.get().getAuthor().getFullName()).isNotBlank();
        assertThat(result.get().getGenres()).isNotEmpty();
    }

    @DisplayName("insert должен сохранять книгу и возвращать её с корректными связями")
    @Test
    void insert_shouldPersistAndReturnBookWithRelations() {
        Book actual = bookService.insert(
                "New Book",
                testAuthors.get(0).getId(),
                Set.of(testGenres.get(0).getId(), testGenres.get(1).getId()));

        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getTitle()).isEqualTo("New Book");
        assertThat(actual.getAuthor()).isNotNull();
        assertThat(actual.getAuthor().getFullName()).isNotBlank();
        assertThat(actual.getGenres()).hasSize(2);
    }

    @DisplayName("update должен обновлять книгу и возвращать актуальные данные")
    @Test
    void update_shouldUpdateAndReturnBook() {
        Book toUpdate = bookService.insert(
                "Original Title",
                testAuthors.get(0).getId(),
                Set.of(testGenres.get(0).getId()));

        Book actual = bookService.update(
                toUpdate.getId(),
                "Updated Title",
                testAuthors.get(1).getId(),
                Set.of(testGenres.get(1).getId(), testGenres.get(2).getId()));

        assertThat(actual.getTitle()).isEqualTo("Updated Title");
        assertThat(actual.getAuthor().getId()).isEqualTo(testAuthors.get(1).getId());
        assertThat(actual.getGenres()).hasSize(2);
        assertThat(actual.getAuthor().getFullName()).isNotBlank();
    }

    @DisplayName("deleteById должен удалять книгу и все связанные с ней комментарии")
    @Test
    void deleteById_shouldDeleteBookAndRelatedComments() {
        Book bookToDelete = bookService.insert(
                "Book To Delete",
                testAuthors.get(0).getId(),
                Set.of(testGenres.get(0).getId()));

        String bookId = bookToDelete.getId();
        commentService.insert(bookId, "Comment 1");
        commentService.insert(bookId, "Comment 2");

        assertThat(bookService.findById(bookId)).isPresent();
        assertThat(commentService.findAllByBookId(bookId)).hasSize(2);

        bookService.deleteById(bookId);

        assertThat(bookService.findById(bookId)).isEmpty();
        assertThat(commentService.findAllByBookId(bookId)).isEmpty();
    }

    private static Stream<String> getTestBookTitles() {
        return Stream.of("Book A", "Book B", "Book C");
    }
}
