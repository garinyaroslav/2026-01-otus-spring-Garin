package ru.otus.hw.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.test.annotation.DirtiesContext;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.otus.hw.models.Book;

@DisplayName("Репозиторий книг")
@DataR2dbcTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private R2dbcEntityOperations r2dbcEntityOperations;

    @DisplayName("должен находить книгу по id с автором и жанрами")
    @Test
    void shouldFindById() {
        StepVerifier.create(bookRepository.findByIdWithRelations(1L))
                .assertNext(found -> {
                    assertThat(found).isNotNull();
                    assertThat(found.getId()).isEqualTo(1L);
                    assertThat(found.getTitle()).isEqualTo("Book A");
                    assertThat(found.getAuthor()).isNotNull();
                    assertThat(found.getAuthor().getId()).isEqualTo(1L);
                    assertThat(found.getAuthor().getFullName()).isEqualTo("Author A");
                    assertThat(found.getGenres()).hasSize(2);
                    assertThat(found.getGenres().get(0).getId()).isEqualTo(1L);
                    assertThat(found.getGenres().get(1).getId()).isEqualTo(2L);
                })
                .verifyComplete();
    }

    @DisplayName("должен загружать все книги с авторами и жанрами")
    @Test
    void shouldFindAll() {
        StepVerifier.create(bookRepository.findAllWithRelations())
                .expectNextCount(3)
                .verifyComplete();
    }

    @DisplayName("должен сохранять новую книгу")
    @Test
    void shouldInsertBook() {
        var newBook = new Book(null, "New Book", 1L);

        StepVerifier.create(
                authorRepository.findById(1L)
                        .zipWith(genreRepository.findById(1L))
                        .flatMap(tuple -> {
                            var author = tuple.getT1();
                            var genre = tuple.getT2();
                            return bookRepository.save(newBook)
                                    .flatMap(saved -> insertGenreLink(saved.getId(), genre.getId())
                                            .thenReturn(saved));
                        })
                        .flatMap(saved -> bookRepository.findByIdWithRelations(saved.getId())))
                .assertNext(found -> {
                    assertThat(found).isNotNull();
                    assertThat(found.getId()).isGreaterThan(0);
                    assertThat(found.getTitle()).isEqualTo("New Book");
                    assertThat(found.getAuthor().getId()).isEqualTo(1L);
                    assertThat(found.getGenres()).hasSize(1);
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
                                    .flatMap(saved -> deleteGenreLinks(saved.getId())
                                            .then(insertGenreLink(saved.getId(), 2L))
                                            .then(insertGenreLink(saved.getId(), 3L))
                                            .thenReturn(saved))
                                    .flatMap(saved -> bookRepository.findByIdWithRelations(saved.getId()));
                        }))
                .assertNext(found -> {
                    assertThat(found.getId()).isEqualTo(1L);
                    assertThat(found.getTitle()).isEqualTo("Updated Title");
                    assertThat(found.getAuthor().getId()).isEqualTo(2L);
                    assertThat(found.getGenres()).hasSize(2);
                })
                .verifyComplete();
    }

    @DisplayName("должен удалять книгу по id")
    @Test
    void shouldDeleteById() {
        StepVerifier.create(
                bookRepository.deleteById(1L)
                        .then(bookRepository.findByIdWithRelations(1L)))
                .verifyComplete();
    }

    private Mono<Void> insertGenreLink(long bookId, long genreId) {
        return r2dbcEntityOperations.getDatabaseClient()
                .sql("INSERT INTO books_genres (book_id, genre_id) VALUES (:book_id, :genre_id)")
                .bind("book_id", bookId)
                .bind("genre_id", genreId)
                .fetch()
                .rowsUpdated()
                .then();
    }

    private Mono<Void> deleteGenreLinks(long bookId) {
        return r2dbcEntityOperations.getDatabaseClient()
                .sql("DELETE FROM books_genres WHERE book_id = :book_id")
                .bind("book_id", bookId)
                .fetch()
                .rowsUpdated()
                .then();
    }
}
