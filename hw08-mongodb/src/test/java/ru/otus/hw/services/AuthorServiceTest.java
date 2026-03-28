package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.models.Author;
import ru.otus.hw.repositories.AuthorRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Сервис авторов")
@SpringBootTest
@ActiveProfiles("test")
@Transactional(propagation = Propagation.NEVER)
class AuthorServiceTest {

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

    @DisplayName("findAll должен возвращать непустой список (seed-данные присутствуют)")
    @Test
    void findAll_shouldReturnNonEmptyList() {
        List<Author> actual = authorService.findAll();

        assertThat(actual).isNotEmpty();
        assertThat(actual).allSatisfy(author -> {
            assertThat(author.getId()).isNotNull();
            assertThat(author.getFullName()).isNotBlank();
        });
    }
}
