package ru.otus.hw.handlers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.validation.Validator;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.routers.BookRoutesConfig;
import ru.otus.hw.services.BookService;

@DisplayName("Handler книг")
@WebFluxTest(BookHandler.class)
@Import(BookRoutesConfig.class)
class BookHandlerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private BookService bookService;

    @MockBean
    private Validator validator;

    private static AuthorDto authorDto() {
        return new AuthorDto(1L, "Author A");
    }

    private static GenreDto genreDto() {
        return new GenreDto(1L, "Genre1");
    }

    private static BookDto bookDto() {
        return new BookDto(1L, "Book A", authorDto(), List.of(genreDto()), List.of());
    }

    @Test
    @DisplayName("GET /api/books")
    void list_shouldReturnAllBooks() {
        List<BookDto> books = List.of(bookDto(), bookDto());
        given(bookService.findAll()).willReturn(Flux.fromIterable(books));

        webTestClient.get().uri("/api/books")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(BookDto.class)
                .hasSize(2)
                .contains(bookDto(), bookDto());
    }

    @Test
    @DisplayName("GET /api/books/{id}")
    void getById_shouldReturnBook() {
        given(bookService.findById(1L)).willReturn(Mono.just(bookDto()));

        webTestClient.get().uri("/api/books/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(BookDto.class)
                .isEqualTo(bookDto());
    }

    @Test
    @DisplayName("GET /api/books/{id}")
    void getById_shouldReturn404WhenNotFound() {
        given(bookService.findById(99L))
                .willReturn(Mono.error(new EntityNotFoundException("Book not found")));

        webTestClient.get().uri("/api/books/99")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("POST /api/books")
    void create_shouldCreateBook() {
        BookCreateDto createDto = new BookCreateDto("New Book", 1L, Set.of(1L, 2L));
        doAnswer(invocation -> null).when(validator).validate(any(), any());
        given(bookService.insert(any(BookCreateDto.class))).willReturn(Mono.just(bookDto()));

        webTestClient.post().uri("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(BookDto.class)
                .isEqualTo(bookDto());

        verify(bookService).insert(any(BookCreateDto.class));
    }

    @Test
    @DisplayName("POST /api/books")
    void create_shouldReturn400OnValidationError() {
        BookCreateDto invalidDto = new BookCreateDto("t", 1L, Set.of());

        doAnswer(invocation -> {
            org.springframework.validation.Errors errors = invocation.getArgument(1);
            errors.rejectValue("title", "Size", "Title must be at least 3 characters");
            return null;
        }).when(validator).validate(any(BookCreateDto.class), any(org.springframework.validation.Errors.class));

        given(bookService.insert(any(BookCreateDto.class))).willReturn(Mono.just(bookDto()));

        webTestClient.post().uri("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidDto)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Validation failed");
    }

    @Test
    @DisplayName("PUT /api/books/{id}")
    void update_shouldUpdateBook() {
        BookUpdateDto updateDto = new BookUpdateDto(1L, "Updated Title", 2L, Set.of(3L));
        doAnswer(invocation -> null).when(validator).validate(any(), any());
        given(bookService.update(any(BookUpdateDto.class))).willReturn(Mono.just(bookDto()));

        webTestClient.put().uri("/api/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateDto)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BookDto.class)
                .isEqualTo(bookDto());

        verify(bookService).update(any(BookUpdateDto.class));
    }

    @Test
    @DisplayName("PUT /api/books/{id}")
    void update_shouldReturn404WhenNotFound() {
        BookUpdateDto updateDto = new BookUpdateDto(99L, "Updated Title", 2L, Set.of(3L));
        doAnswer(invocation -> null).when(validator).validate(any(), any());
        given(bookService.update(any(BookUpdateDto.class)))
                .willReturn(Mono.error(new EntityNotFoundException("Book not found")));

        webTestClient.put().uri("/api/books/99")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateDto)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("DELETE /api/books/{id}")
    void delete_shouldDeleteBook() {
        given(bookService.deleteById(1L)).willReturn(Mono.empty());

        webTestClient.delete().uri("/api/books/1")
                .exchange()
                .expectStatus().isNoContent();

        verify(bookService).deleteById(1L);
    }
}
