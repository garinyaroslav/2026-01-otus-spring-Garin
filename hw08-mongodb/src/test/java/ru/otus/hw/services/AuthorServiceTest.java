package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import ru.otus.hw.repositories.AuthorRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Сервис авторов")
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Transactional(propagation = Propagation.NEVER)
class AuthorServiceTest {

    @Container
    static final MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:7.0.14"));

    @DynamicPropertySource
    static void setMongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private AuthorService authorService;

    @Autowired
    private AuthorRepository authorRepository;

    @DisplayName("findAll должен возвращать всех авторов, включая добавленных")
    @Test
    void findAll_shouldReturnAllAuthors() {
        Author saved1 = authorRepository.save(new Author(null, "Test Author X"));
        Author saved2 = authorRepository.save(new Author(null, "Test Author Y"));

        try {
            List<Author> actual = authorService.findAll();

            assertThat(actual).isNotEmpty();
            assertThat(actual)
                    .extracting(Author::getFullName)
                    .contains("Test Author X", "Test Author Y");
        } finally {
            authorRepository.deleteById(saved1.getId());
            authorRepository.deleteById(saved2.getId());
        }
    }

    @DisplayName("findAll должен возвращать непустой список")
    @Test
    void findAll_shouldReturnNonEmptyList() {
        authorRepository.save(new Author(null, "Test Author X"));

        List<Author> actual = authorService.findAll();

        assertThat(actual).isNotEmpty();
        assertThat(actual).allSatisfy(author -> {
            assertThat(author.getId()).isNotNull();
            assertThat(author.getFullName()).isNotBlank();
        });
    }
}
