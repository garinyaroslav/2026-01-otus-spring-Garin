package ru.otus.hw.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.annotation.DirtiesContext;

import reactor.test.StepVerifier;
import ru.otus.hw.models.Author;

@DisplayName("Репозиторий авторов")
@DataR2dbcTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthorRepositoryTest {

    @Autowired
    private AuthorRepository authorRepository;

    @DisplayName("должен загружать список всех авторов")
    @Test
    void shouldFindAll() {
        StepVerifier.create(authorRepository.findAll())
                .expectNext(new Author(1L, "Author A"))
                .expectNext(new Author(2L, "Author B"))
                .expectNext(new Author(3L, "Author C"))
                .verifyComplete();
    }

    @DisplayName("должен загружать автора по id")
    @Test
    void shouldFindById() {
        StepVerifier.create(authorRepository.findById(1L))
                .assertNext(author -> {
                    assertThat(author).isNotNull();
                    assertThat(author.getId()).isEqualTo(1L);
                    assertThat(author.getFullName()).isEqualTo("Author A");
                })
                .verifyComplete();
    }
}
