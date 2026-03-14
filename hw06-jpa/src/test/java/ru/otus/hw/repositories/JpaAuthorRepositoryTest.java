package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Author;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Репозиторий авторов")
@DataJpaTest
@Import(JpaAuthorRepository.class)
class JpaAuthorRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private JpaAuthorRepository authorRepository;

    @DisplayName("должен загружать список всех авторов")
    @Test
    void shouldFindAll() {
        em.persistAndFlush(new Author(0, "Author A", null));
        em.persistAndFlush(new Author(0, "Author B", null));

        List<Author> authors = authorRepository.findAll();

        assertThat(authors).hasSizeGreaterThanOrEqualTo(2);
        assertThat(authors).extracting(Author::getFullName)
                .contains("Author A", "Author B");
    }

    @DisplayName("должен загружать автора по id")
    @Test
    void shouldFindById() {
        var author = em.persistAndFlush(new Author(0, "Test Author", null));

        var found = authorRepository.findById(author.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getFullName()).isEqualTo("Test Author");
    }

}
