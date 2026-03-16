package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий жанров")
@DataJpaTest
@Import(JpaGenreRepository.class)
class JpaGenreRepositoryTest {

    @Autowired
    private JpaGenreRepository genreRepository;

    @DisplayName("должен загружать все жанры")
    @Test
    void shouldFindAll() {
        var genres = genreRepository.findAll();

        assertThat(genres)
                .usingRecursiveComparison()
                .isEqualTo(List.of(
                        new Genre(1L, "Genre1"),
                        new Genre(2L, "Genre2"),
                        new Genre(3L, "Genre3")));
    }

    @DisplayName("должен загружать жанры по списку id")
    @Test
    void shouldFindAllByIds() {
        var found = genreRepository.findAllByIds(Set.of(1L, 2L));

        assertThat(found)
                .usingRecursiveComparison()
                .isEqualTo(List.of(
                        new Genre(1L, "Genre1"),
                        new Genre(2L, "Genre2")));
    }
}
