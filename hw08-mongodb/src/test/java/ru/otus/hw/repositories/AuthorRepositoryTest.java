package ru.otus.hw.repositories;

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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий авторов")
@ActiveProfiles("test")
@Testcontainers
@DataMongoTest
class AuthorRepositoryTest {

    @Container
    static final MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:7.0.14"));

    @DynamicPropertySource
    static void setMongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        mongoTemplate.dropCollection(Author.class);
    }

    @DisplayName("должен загружать список всех авторов")
    @Test
    void shouldFindAll() {
        mongoTemplate.save(new Author(null, "Author A"));
        mongoTemplate.save(new Author(null, "Author B"));

        List<Author> authors = authorRepository.findAll();

        assertThat(authors).hasSize(2);
        assertThat(authors)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(List.of(
                        new Author(null, "Author A"),
                        new Author(null, "Author B")));
    }

    @DisplayName("должен загружать автора по id")
    @Test
    void shouldFindById() {
        Author savedAuthor = mongoTemplate.save(new Author(null, "Author A"));

        var found = authorRepository.findById(savedAuthor.getId());

        assertThat(found).isPresent();
        assertThat(found.get())
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(new Author(null, "Author A"));
    }
}
