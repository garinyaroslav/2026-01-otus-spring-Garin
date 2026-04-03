package ru.otus.hw.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;

@DisplayName("Репозиторий книг")
@ActiveProfiles("test")
@Testcontainers
@DataMongoTest
class BookRepositoryTest {

    @Container
    static final MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:7.0.14"));

    @DynamicPropertySource
    static void setMongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        mongoTemplate.dropCollection(Comment.class);
        mongoTemplate.dropCollection(Book.class);
        mongoTemplate.dropCollection(Author.class);
        mongoTemplate.dropCollection(Genre.class);
    }

    @DisplayName("должен находить книгу по id с автором и жанрами")
    @Test
    void shouldFindById() {
        Author author = mongoTemplate.save(new Author(null, "Author A"));
        Genre genre = mongoTemplate.save(new Genre(null, "Genre1"));

        Book book = new Book(null, "Book A", author, List.of(genre));
        Book savedBook = mongoTemplate.save(book);

        var found = bookRepository.findById(savedBook.getId());

        assertThat(found).isPresent();

        Book expected = new Book(null, "Book A",
                new Author(null, "Author A"),
                List.of(new Genre(null, "Genre1")));

        assertThat(found.get())
                .usingRecursiveComparison()
                .ignoringFields("id", "author.id", "genres.id")
                .ignoringExpectedNullFields()
                .isEqualTo(expected);
    }

    @DisplayName("должен загружать все книги с авторами и жанрами")
    @Test
    void shouldFindAll() {
        Author author1 = mongoTemplate.save(new Author(null, "Author A"));
        Author author2 = mongoTemplate.save(new Author(null, "Author B"));
        Genre genre1 = mongoTemplate.save(new Genre(null, "Genre1"));
        Genre genre2 = mongoTemplate.save(new Genre(null, "Genre2"));

        mongoTemplate.save(new Book(null, "Book A", author1, List.of(genre1)));
        mongoTemplate.save(new Book(null, "Book B", author2, List.of(genre2)));

        var books = bookRepository.findAll();

        assertThat(books).hasSize(2);

        List<Book> expected = List.of(
                new Book(null, "Book A", new Author(null, "Author A"), List.of(new Genre(null, "Genre1"))),
                new Book(null, "Book B", new Author(null, "Author B"), List.of(new Genre(null, "Genre2"))));

        assertThat(books)
                .usingRecursiveComparison()
                .ignoringFields("id", "author.id", "genres.id")
                .ignoringExpectedNullFields()
                .isEqualTo(expected);
    }

    @DisplayName("должен сохранять новую книгу")
    @Test
    void shouldInsertBook() {
        Author author = mongoTemplate.save(new Author(null, "Author A"));
        Genre genre = mongoTemplate.save(new Genre(null, "Genre1"));

        Book newBook = new Book(null, "New Book", author, List.of(genre));
        Book saved = bookRepository.save(newBook);

        assertThat(saved.getId()).isNotNull();

        Book expected = new Book(null, "New Book", new Author(null, "Author A"), List.of(new Genre(null, "Genre1")));

        assertThat(saved)
                .usingRecursiveComparison()
                .ignoringFields("id", "author.id", "genres.id")
                .isEqualTo(expected);
    }

    @DisplayName("должен обновлять существующую книгу")
    @Test
    void shouldUpdateBook() {
        Author author = mongoTemplate.save(new Author(null, "Author A"));
        Genre genre1 = mongoTemplate.save(new Genre(null, "Genre1"));
        Genre genre2 = mongoTemplate.save(new Genre(null, "Genre2"));

        Book book = new Book(null, "Old Title", author, List.of(genre1));
        Book saved = mongoTemplate.save(book);

        saved.setTitle("Updated Title");
        saved.setGenres(List.of(genre1, genre2));
        bookRepository.save(saved);

        var updated = bookRepository.findById(saved.getId()).orElseThrow();

        Book expected = new Book(null, "Updated Title", new Author(null, "Author A"), List.of(
                new Genre(null, "Genre1"),
                new Genre(null, "Genre2")));

        assertThat(updated)
                .usingRecursiveComparison()
                .ignoringFields("id", "author.id", "genres.id")
                .isEqualTo(expected);
    }

    @DisplayName("должен удалять книгу по id вместе с её комментариями")
    @Test
    void shouldDeleteBookByIdWithCascade() {
        Author author = mongoTemplate.save(new Author(null, "Author A"));
        Genre genre = mongoTemplate.save(new Genre(null, "Genre1"));

        Book savedBook = mongoTemplate.save(new Book(null, "Book to delete", author, List.of(genre)));

        mongoTemplate.save(new Comment(null, "Comment 1 about book", savedBook));
        mongoTemplate.save(new Comment(null, "Comment 2 about book", savedBook));

        assertThat(mongoTemplate.findAll(Comment.class)).hasSize(2);

        bookRepository.deleteByIdWithCascade(savedBook.getId());

        assertThat(bookRepository.findById(savedBook.getId())).isEmpty();

        assertThat(mongoTemplate.findAll(Comment.class)).isEmpty();
    }
}
