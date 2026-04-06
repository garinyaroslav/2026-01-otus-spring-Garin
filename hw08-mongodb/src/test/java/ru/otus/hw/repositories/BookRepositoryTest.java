package ru.otus.hw.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import ru.otus.hw.events.BookCascadeDeleteEventListener;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Comment;
import ru.otus.hw.models.Genre;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий книг")
@ActiveProfiles("test")
@Testcontainers
@DataMongoTest
@Import(BookCascadeDeleteEventListener.class)
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

    @DisplayName("должен находить книгу по id")
    @Test
    void shouldFindById() {
        Author author = mongoTemplate.save(new Author(null, "Author A"));
        Genre genre = mongoTemplate.save(new Genre(null, "Genre1"));

        Book book = new Book(null, "Book A", author.getId(), List.of(genre.getId()));
        Book savedBook = mongoTemplate.save(book);

        var found = bookRepository.findById(savedBook.getId());

        assertThat(found).isPresent();
        Book actual = found.get();

        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getTitle()).isEqualTo("Book A");
        assertThat(actual.getAuthorId()).isEqualTo(author.getId());
        assertThat(actual.getGenreIds()).containsExactly(genre.getId());
    }

    @DisplayName("должен загружать все книги")
    @Test
    void shouldFindAll() {
        Author author1 = mongoTemplate.save(new Author(null, "Author A"));
        Author author2 = mongoTemplate.save(new Author(null, "Author B"));
        Genre genre1 = mongoTemplate.save(new Genre(null, "Genre1"));
        Genre genre2 = mongoTemplate.save(new Genre(null, "Genre2"));

        mongoTemplate.save(new Book(null, "Book A", author1.getId(), List.of(genre1.getId())));
        mongoTemplate.save(new Book(null, "Book B", author2.getId(), List.of(genre2.getId())));

        var books = bookRepository.findAll();

        assertThat(books).hasSize(2);

        assertThat(books)
                .extracting(Book::getTitle, Book::getAuthorId, Book::getGenreIds)
                .containsExactlyInAnyOrder(
                        org.assertj.core.api.Assertions.tuple("Book A", author1.getId(), List.of(genre1.getId())),
                        org.assertj.core.api.Assertions.tuple("Book B", author2.getId(), List.of(genre2.getId())));
    }

    @DisplayName("должен сохранять новую книгу")
    @Test
    void shouldInsertBook() {
        Author author = mongoTemplate.save(new Author(null, "Author A"));
        Genre genre = mongoTemplate.save(new Genre(null, "Genre1"));

        Book newBook = new Book(null, "New Book", author.getId(), List.of(genre.getId()));
        Book saved = bookRepository.save(newBook);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("New Book");
        assertThat(saved.getAuthorId()).isEqualTo(author.getId());
        assertThat(saved.getGenreIds()).containsExactly(genre.getId());
    }

    @DisplayName("должен обновлять существующую книгу")
    @Test
    void shouldUpdateBook() {
        Author author1 = mongoTemplate.save(new Author(null, "Author A"));
        Author author2 = mongoTemplate.save(new Author(null, "Author B"));
        Genre genre1 = mongoTemplate.save(new Genre(null, "Genre1"));
        Genre genre2 = mongoTemplate.save(new Genre(null, "Genre2"));

        Book book = new Book(null, "Old Title", author1.getId(), List.of(genre1.getId()));
        Book saved = mongoTemplate.save(book);

        saved.setTitle("Updated Title");
        saved.setAuthorId(author2.getId());
        saved.setGenreIds(List.of(genre1.getId(), genre2.getId()));

        bookRepository.save(saved);

        var updated = bookRepository.findById(saved.getId()).orElseThrow();

        assertThat(updated.getTitle()).isEqualTo("Updated Title");
        assertThat(updated.getAuthorId()).isEqualTo(author2.getId());
        assertThat(updated.getGenreIds()).containsExactlyInAnyOrder(genre1.getId(), genre2.getId());
    }

    @DisplayName("должен удалять книгу по id вместе с её комментариями")
    @Test
    void shouldDeleteBookByIdWithCascade() {
        Author author = mongoTemplate.save(new Author(null, "Author A"));
        Genre genre = mongoTemplate.save(new Genre(null, "Genre1"));

        Book savedBook = mongoTemplate.save(
                new Book(null, "Book to delete", author.getId(), List.of(genre.getId())));

        mongoTemplate.save(new Comment(null, "Comment 1 about book", savedBook));
        mongoTemplate.save(new Comment(null, "Comment 2 about book", savedBook));

        assertThat(mongoTemplate.findAll(Comment.class)).hasSize(2);

        bookRepository.deleteById(savedBook.getId());

        assertThat(bookRepository.findById(savedBook.getId())).isEmpty();
        assertThat(mongoTemplate.findAll(Comment.class)).isEmpty();
    }
}
