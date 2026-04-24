package ru.otus.hw.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.annotation.DirtiesContext;

import reactor.test.StepVerifier;

@DisplayName("Репозиторий жанров")
@DataR2dbcTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class GenreRepositoryTest {

    @Autowired
    private GenreRepository genreRepository;

    @DisplayName("должен загружать все жанры")
    @Test
    void shouldFindAll() {
        StepVerifier.create(genreRepository.findAll())
                .expectNextCount(6)
                .verifyComplete();
    }

    @DisplayName("должен загружать жанры по списку id")
    @Test
    void shouldFindAllByIdIn() {
        StepVerifier.create(genreRepository.findAllByIdIn(Set.of(1L, 2L)))
                .assertNext(genre -> {
                    assertThat(genre.getId()).isIn(1L, 2L);
                    assertThat(genre.getName()).isIn("Genre1", "Genre2");
                })
                .assertNext(genre -> {
                    assertThat(genre.getId()).isIn(1L, 2L);
                    assertThat(genre.getName()).isIn("Genre1", "Genre2");
                })
                .verifyComplete();
    }
}
