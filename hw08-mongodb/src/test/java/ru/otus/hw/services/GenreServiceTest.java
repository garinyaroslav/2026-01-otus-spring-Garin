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

import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Сервис жанров")
@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@Transactional(propagation = Propagation.NEVER)
class GenreServiceTest {

    @Container
    static final MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:7.0.14"));

    @DynamicPropertySource
    static void setMongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private GenreService genreService;

    @Autowired
    private GenreRepository genreRepository;

    @DisplayName("findAll должен возвращать все жанры, включая добавленные")
    @Test
    void findAll_shouldReturnAllGenres() {
        Genre saved1 = genreRepository.save(new Genre(null, "Test Genre X"));
        Genre saved2 = genreRepository.save(new Genre(null, "Test Genre Y"));

        try {
            List<Genre> actual = genreService.findAll();

            assertThat(actual).isNotEmpty();
            assertThat(actual)
                    .extracting(Genre::getName)
                    .contains("Test Genre X", "Test Genre Y");
        } finally {
            genreRepository.deleteById(saved1.getId());
            genreRepository.deleteById(saved2.getId());
        }
    }

    @DisplayName("findAll должен возвращать непустой список")
    @Test
    void findAll_shouldReturnNonEmptyList() {
        genreRepository.save(new Genre(null, "Genre from test"));

        List<Genre> actual = genreService.findAll();

        assertThat(actual).isNotEmpty();
        assertThat(actual).allSatisfy(genre -> {
            assertThat(genre.getId()).isNotNull();
            assertThat(genre.getName()).isNotBlank();
        });
    }
}
