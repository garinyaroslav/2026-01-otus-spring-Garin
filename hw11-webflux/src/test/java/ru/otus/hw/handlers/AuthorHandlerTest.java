package ru.otus.hw.handlers;

import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Flux;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.routers.AuthorRoutesConfig;
import ru.otus.hw.services.AuthorService;

@DisplayName("Handler авторов")
@WebFluxTest(AuthorHandler.class)
@Import(AuthorRoutesConfig.class)
class AuthorHandlerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AuthorService authorService;

    @Test
    @DisplayName("GET /api/authors")
    void list_shouldReturnAllAuthors() {
        List<AuthorDto> authors = List.of(
                new AuthorDto(1L, "Лев Толстой"),
                new AuthorDto(2L, "Фёдор Достоевский"),
                new AuthorDto(3L, "Антон Чехов"));

        given(authorService.findAll()).willReturn(Flux.fromIterable(authors));

        webTestClient.get().uri("/api/authors")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AuthorDto.class)
                .hasSize(3)
                .contains(authors.get(0), authors.get(1), authors.get(2));
    }

    @Test
    @DisplayName("GET /api/authors")
    void list_shouldReturnEmptyList() {
        given(authorService.findAll()).willReturn(Flux.empty());

        webTestClient.get().uri("/api/authors")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AuthorDto.class)
                .hasSize(0);
    }
}
