package ru.otus.hw.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.annotation.DirtiesContext;

import reactor.test.StepVerifier;
import ru.otus.hw.models.Book;

@DisplayName("Репозиторий книг")
@DataR2dbcTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @DisplayName("должен находить книгу по id с genreIds")
    @Test
    void shouldFindById() {
        StepVerifier.create(bookRepository.findByIdWithGenreIds(1L))
                .assertNext(found -> {
                    assertThat(found).isNotNull();
                    assertThat(found.getId()).isEqualTo(1L);
                    assertThat(found.getTitle()).isEqualTo("Book A");
                    assertThat(found.getAuthorId()).isEqualTo(1L);
                    assertThat(found.getGenreIds()).hasSize(2);
                    assertThat(found.getGenreIds()).containsExactlyInAnyOrder(1L, 2L);
                })
                .verifyComplete();
    }

    @DisplayName("должен загружать все книги с genreIds")
    @Test
    void shouldFindAll() {
        StepVerifier.create(bookRepository.findAllWithGenreIds())
                .expectNextCount(3)
                .verifyComplete();
    }

    @DisplayName("должен сохранять новую книгу")
    @Test
    void shouldInsertBook() {
        var newBook = new Book(null, "New Book", 1L);

        StepVerifier.create(
                bookRepository.save(newBook)
                        .flatMap(saved -> bookRepository.saveGenreLinks(saved.getId(), List.of(1L))
                                .thenReturn(saved))
                        .flatMap(saved -> bookRepository.findByIdWithGenreIds(saved.getId())))
                .assertNext(found -> {
                    assertThat(found).isNotNull();
                    assertThat(found.getId()).isGreaterThan(0);
                    assertThat(found.getTitle()).isEqualTo("New Book");
                    assertThat(found.getAuthorId()).isEqualTo(1L);
                    assertThat(found.getGenreIds()).hasSize(1);
                    assertThat(found.getGenreIds()).containsExactly(1L);
                })
                .verifyComplete();
    }

    @DisplayName("должен обновлять существующую книгу")
    @Test
    void shouldUpdateBook() {
        StepVerifier.create(
                bookRepository.findById(1L)
                        .flatMap(book -> {
                            book.setTitle("Updated Title");
                            book.setAuthorId(2L);
                            return bookRepository.save(book)
                                    .flatMap(saved -> bookRepository.deleteGenreLinks(saved.getId())
                                            .then(bookRepository.saveGenreLinks(saved.getId(), List.of(2L, 3L)))
                                            .thenReturn(saved))
                                    .flatMap(saved -> bookRepository.findByIdWithGenreIds(saved.getId()));
                        }))
                .assertNext(found -> {
                    assertThat(found.getId()).isEqualTo(1L);
                    assertThat(found.getTitle()).isEqualTo("Updated Title");
                    assertThat(found.getAuthorId()).isEqualTo(2L);
                    assertThat(found.getGenreIds()).hasSize(2);
                    assertThat(found.getGenreIds()).containsExactlyInAnyOrder(2L, 3L);
                })
                .verifyComplete();
    }

    @DisplayName("должен удалять книгу по id")
    @Test
    void shouldDeleteById() {
        StepVerifier.create(
                bookRepository.deleteById(1L)
                        .then(bookRepository.findByIdWithGenreIds(1L)))
                .verifyComplete();
    }
}
