package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import ru.otus.hw.models.Genre;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий жанров")
@ActiveProfiles("test")
@DataMongoTest
class GenreRepositoryTest {

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @DisplayName("должен загружать все жанры")
    @Test
    void shouldFindAll() {
        mongoTemplate.save(new Genre(null, "Genre1"));
        mongoTemplate.save(new Genre(null, "Genre2"));
        mongoTemplate.save(new Genre(null, "Genre3"));

        List<Genre> genres = genreRepository.findAll();

        assertThat(genres).hasSizeGreaterThanOrEqualTo(3);
        assertThat(genres)
                .extracting(Genre::getName)
                .contains("Genre1", "Genre2", "Genre3");
    }

    @DisplayName("должен загружать жанры по списку id")
    @Test
    void shouldFindAllByIdIn() {
        Genre g1 = mongoTemplate.save(new Genre(null, "Genre1"));
        Genre g2 = mongoTemplate.save(new Genre(null, "Genre2"));

        var found = genreRepository.findAllByIdIn(Set.of(g1.getId(), g2.getId()));

        assertThat(found).hasSize(2);
        assertThat(found)
                .extracting(Genre::getName)
                .containsExactlyInAnyOrder("Genre1", "Genre2");
    }
}
