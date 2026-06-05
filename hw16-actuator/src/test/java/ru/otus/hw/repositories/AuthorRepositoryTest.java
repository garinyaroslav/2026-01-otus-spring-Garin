package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.otus.hw.models.Author;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий авторов")
@DataJpaTest
class AuthorRepositoryTest {

    @Autowired
    private AuthorRepository authorRepository;

    @DisplayName("должен загружать список всех авторов")
    @Test
    void shouldFindAll() {
        List<Author> authors = authorRepository.findAll();
        List<Author> expected = List.of(
                new Author(1, "Author A", null),
                new Author(2, "Author B", null));

        assertThat(authors)
                .usingRecursiveComparison()
                .ignoringFields("books")
                .isEqualTo(expected);
    }

    @DisplayName("должен загружать автора по id")
    @Test
    void shouldFindById() {
        var expected = new Author(1, "Author A", null);

        var found = authorRepository.findById(1L);

        assertThat(found).isPresent();
        assertThat(found.get())
                .usingRecursiveComparison()
                .ignoringFields("books")
                .isEqualTo(expected);
    }

}
