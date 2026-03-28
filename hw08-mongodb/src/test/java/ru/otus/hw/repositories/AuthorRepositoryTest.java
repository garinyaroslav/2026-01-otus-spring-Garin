package ru.otus.hw.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;

import ru.otus.hw.models.Author;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий авторов")
@ActiveProfiles("test")
@DataMongoTest
class AuthorRepositoryTest {

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
