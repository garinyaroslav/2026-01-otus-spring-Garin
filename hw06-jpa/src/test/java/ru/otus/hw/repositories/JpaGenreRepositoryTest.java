package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
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
    private TestEntityManager em;

    @Autowired
    private JpaGenreRepository genreRepository;

    @DisplayName("должен загружать все жанры")
    @Test
    void shouldFindAll() {
        em.persistAndFlush(new Genre(0, "genre1"));
        em.persistAndFlush(new Genre(0, "genre2"));

        List<Genre> genres = genreRepository.findAll();

        assertThat(genres).hasSizeGreaterThanOrEqualTo(2);
        assertThat(genres).extracting(Genre::getName)
                .contains("genre1", "genre2");
    }

    @DisplayName("должен загружать жанры по списку id")
    @Test
    void shouldFindAllByIds() {
        var g1 = em.persistAndFlush(new Genre(0, "Drama"));
        var g2 = em.persistAndFlush(new Genre(0, "Comedy"));
        em.persistAndFlush(new Genre(0, "Thriller"));

        List<Genre> found = genreRepository.findAllByIds(Set.of(g1.getId(), g2.getId()));

        assertThat(found).hasSize(2);
        assertThat(found).extracting(Genre::getName)
                .containsExactlyInAnyOrder("Drama", "Comedy");
    }

}
