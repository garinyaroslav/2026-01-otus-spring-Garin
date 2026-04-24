package ru.otus.hw.handlers;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Mono;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.routers.CommentRoutesConfig;
import ru.otus.hw.services.CommentService;

@DisplayName("Handler комментариев")
@WebFluxTest(CommentHandler.class)
@Import(CommentRoutesConfig.class)
class CommentHandlerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private CommentService commentService;

    private static CommentDto commentDto() {
        return new CommentDto(10L, "Отличная книга, очень рекомендую!", 1L);
    }

    @Test
    @DisplayName("POST /api/comments?bookId=1&text=...")
    void add_shouldCreateComment() {
        given(commentService.insert(1L, "Хорошая книга")).willReturn(Mono.just(commentDto()));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path("/api/comments")
                        .queryParam("bookId", "1")
                        .queryParam("text", "Хорошая книга")
                        .build())
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CommentDto.class)
                .isEqualTo(commentDto());

        verify(commentService).insert(1L, "Хорошая книга");
    }

    @Test
    @DisplayName("POST /api/comments")
    void add_shouldReturn400WhenMissingParams() {
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path("/api/comments")
                        .queryParam("text", "some text")
                        .build())
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("bookId and text are required");

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path("/api/comments")
                        .queryParam("bookId", "1")
                        .queryParam("text", "   ")
                        .build())
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("bookId and text are required");
    }

    @Test
    @DisplayName("POST /api/comments")
    void add_shouldReturn400WhenBookIdNotNumber() {
        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path("/api/comments")
                        .queryParam("bookId", "abc")
                        .queryParam("text", "good")
                        .build())
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("bookId must be a number");
    }

    @Test
    @DisplayName("POST /api/comments")
    void add_shouldReturn404WhenBookNotFound() {
        given(commentService.insert(eq(999L), anyString()))
                .willReturn(Mono.error(new EntityNotFoundException("Book not found")));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path("/api/comments")
                        .queryParam("bookId", "999")
                        .queryParam("text", "comment")
                        .build())
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("DELETE /api/comments/{id}")
    void delete_shouldDeleteComment() {
        given(commentService.deleteById(5L)).willReturn(Mono.empty());

        webTestClient.delete().uri("/api/comments/5")
                .exchange()
                .expectStatus().isNoContent();

        verify(commentService).deleteById(5L);
    }
}
