package ru.otus.hw.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.exceptions.EntityNotFoundException;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Тест сервиса книг")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class BookServiceTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private CommentService commentService;

    @BeforeEach
    void setUp() {
        bookService.findAll().forEach(book -> bookService.deleteById(book.getId()));

        bookService.insert(new BookCreateDto("Book A", 1L, Set.of(1L)));
        bookService.insert(new BookCreateDto("Book B", 2L, Set.of(2L)));
        bookService.insert(new BookCreateDto("Book C", 1L, Set.of(3L)));
    }

    @DisplayName("findAll должен возвращать BookDto с инициализированными автором и жанрами")
    @Test
    void findAll_shouldReturnDtosWithRelations() {
        List<BookDto> books = bookService.findAll();

        assertThat(books).isNotEmpty();
        assertThat(books).allSatisfy(dto -> {
            assertThat(dto.getAuthor()).isNotNull();
            assertThat(dto.getAuthor().getFullName()).isNotBlank();
            assertThat(dto.getGenres()).isNotNull().isNotEmpty();
        });
    }

    @DisplayName("findById должен возвращать корректный BookDto")
    @ParameterizedTest
    @MethodSource("expectedBookTitles")
    void findById_shouldReturnCorrectDto(String expectedTitle) {
        var result = bookService.findAll().stream()
                .filter(dto -> dto.getTitle().equals(expectedTitle))
                .findFirst();

        assertThat(result)
                .as("Книга '%s' должна существовать", expectedTitle)
                .isPresent();

        BookDto dto = result.get();
        assertThat(dto.getAuthor()).isNotNull();
        assertThat(dto.getGenres()).isNotEmpty();
    }

    @DisplayName("insert должен сохранять книгу и возвращать BookDto с корректными связями")
    @Test
    void insert_shouldPersistAndReturnDto() {
        BookDto actual = bookService.insert(new BookCreateDto("New Book", 1L, Set.of(1L, 2L)));

        assertThat(actual.getId()).isGreaterThan(0);
        assertThat(actual.getTitle()).isEqualTo("New Book");
        assertThat(actual.getAuthor()).isNotNull();
        assertThat(actual.getAuthor().getFullName()).isNotBlank();
        assertThat(actual.getGenres()).hasSize(2);
    }

    @DisplayName("update должен обновлять книгу и возвращать актуальный BookDto")
    @Test
    void update_shouldUpdateAndReturnDto() {
        BookDto toUpdate = bookService.insert(new BookCreateDto("Original Title", 1L, Set.of(1L)));

        BookDto actual = bookService.update(
                new BookUpdateDto(toUpdate.getId(), "Updated Title", 2L, Set.of(2L, 3L)));

        assertThat(actual.getId()).isEqualTo(toUpdate.getId());
        assertThat(actual.getTitle()).isEqualTo("Updated Title");
        assertThat(actual.getAuthor()).isNotNull();
        assertThat(actual.getAuthor().getId()).isEqualTo(2L);
        assertThat(actual.getAuthor().getFullName()).isNotBlank();
        assertThat(actual.getGenres()).hasSize(2);
    }

    @DisplayName("deleteById должен удалять книгу и все связанные комментарии")
    @Test
    void deleteById_shouldDeleteBookAndComments() {
        BookDto bookDto = bookService.insert(new BookCreateDto("Book To Delete", 1L, Set.of(1L)));
        long bookId = bookDto.getId();

        commentService.insert(bookId, "Comment 1");
        commentService.insert(bookId, "Comment 2");

        assertNotNull(bookService.findById(bookId));
        assertThat(commentService.findAllByBookId(bookId)).hasSize(2);

        bookService.deleteById(bookId);

        assertThrows(EntityNotFoundException.class, () -> bookService.findById(bookId));
        assertThat(commentService.findAllByBookId(bookId)).isEmpty();
    }

    private static Stream<String> expectedBookTitles() {
        return Stream.of("Book A", "Book B", "Book C");
    }
}
