package ru.otus.hw.services;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import reactor.test.StepVerifier;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.exceptions.EntityNotFoundException;

@DisplayName("Тест сервиса книг")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookServiceTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private CommentService commentService;

    @BeforeEach
    void setUp() {
        bookService.findAll()
                .flatMap(book -> bookService.deleteById(book.getId()))
                .then()
                .block();

        bookService.insert(new BookCreateDto("Book A", 1L, Set.of(1L))).block();
        bookService.insert(new BookCreateDto("Book B", 2L, Set.of(2L))).block();
        bookService.insert(new BookCreateDto("Book C", 1L, Set.of(3L))).block();
    }

    @DisplayName("findAll должен возвращать BookDto с инициализированными автором и жанрами")
    @Test
    void findAll_shouldReturnDtosWithRelations() {
        StepVerifier.create(bookService.findAll())
                .assertNext(dto -> {
                    assertThat(dto.getAuthor()).isNotNull();
                    assertThat(dto.getAuthor().getFullName()).isNotBlank();
                    assertThat(dto.getGenres()).isNotNull().isNotEmpty();
                })
                .assertNext(dto -> {
                    assertThat(dto.getAuthor()).isNotNull();
                    assertThat(dto.getAuthor().getFullName()).isNotBlank();
                    assertThat(dto.getGenres()).isNotNull().isNotEmpty();
                })
                .assertNext(dto -> {
                    assertThat(dto.getAuthor()).isNotNull();
                    assertThat(dto.getAuthor().getFullName()).isNotBlank();
                    assertThat(dto.getGenres()).isNotNull().isNotEmpty();
                })
                .verifyComplete();
    }

    @DisplayName("findById должен возвращать корректный BookDto")
    @Test
    void findById_shouldReturnCorrectDto() {
        StepVerifier.create(bookService.findAll()
                .filter(dto -> dto.getTitle().equals("Book A"))
                .next()
                .flatMap(dto -> bookService.findById(dto.getId())))
                .assertNext(dto -> {
                    assertThat(dto.getTitle()).isEqualTo("Book A");
                    assertThat(dto.getAuthor()).isNotNull();
                    assertThat(dto.getGenres()).isNotEmpty();
                })
                .verifyComplete();
    }

    @DisplayName("insert должен сохранять книгу и возвращать BookDto с корректными связями")
    @Test
    void insert_shouldPersistAndReturnDto() {
        StepVerifier.create(bookService.insert(new BookCreateDto("New Book", 1L, Set.of(1L, 2L))))
                .assertNext(actual -> {
                    assertThat(actual.getId()).isGreaterThan(0);
                    assertThat(actual.getTitle()).isEqualTo("New Book");
                    assertThat(actual.getAuthor()).isNotNull();
                    assertThat(actual.getAuthor().getFullName()).isNotBlank();
                    assertThat(actual.getGenres()).hasSize(2);
                })
                .verifyComplete();
    }

    @DisplayName("update должен обновлять книгу и возвращать актуальный BookDto")
    @Test
    void update_shouldUpdateAndReturnDto() {
        StepVerifier.create(
                bookService.insert(new BookCreateDto("Original Title", 1L, Set.of(1L)))
                        .flatMap(toUpdate -> bookService.update(
                                new BookUpdateDto(toUpdate.getId(), "Updated Title", 2L, Set.of(2L, 3L)))))
                .assertNext(actual -> {
                    assertThat(actual.getTitle()).isEqualTo("Updated Title");
                    assertThat(actual.getAuthor()).isNotNull();
                    assertThat(actual.getAuthor().getId()).isEqualTo(2L);
                    assertThat(actual.getAuthor().getFullName()).isNotBlank();
                    assertThat(actual.getGenres()).hasSize(2);
                })
                .verifyComplete();
    }

    @DisplayName("deleteById должен удалять книгу и все связанные комментарии")
    @Test
    void deleteById_shouldDeleteBookAndComments() {
        StepVerifier.create(
                bookService.insert(new BookCreateDto("Book To Delete", 1L, Set.of(1L)))
                        .flatMap(bookDto -> {
                            long bookId = bookDto.getId();
                            return commentService.insert(bookId, "Comment 1")
                                    .then(commentService.insert(bookId, "Comment 2"))
                                    .then(bookService.deleteById(bookId))
                                    .then(bookService.findById(bookId));
                        }))
                .expectError(EntityNotFoundException.class)
                .verify();
    }

    @DisplayName("удаленная книга не должна быть найдена")
    @Test
    void deletedBook_shouldNotBeFound() {
        var bookId = bookService.insert(new BookCreateDto("Book To Delete", 1L, Set.of(1L)))
                .map(BookDto::getId)
                .block();

        StepVerifier.create(
                bookService.deleteById(bookId)
                        .then(bookService.findById(bookId)))
                .expectError(EntityNotFoundException.class)
                .verify();
    }
}
