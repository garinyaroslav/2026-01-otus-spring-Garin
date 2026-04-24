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
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.routers.GenreRoutesConfig;
import ru.otus.hw.services.GenreService;

@DisplayName("Handler жанров")
@WebFluxTest(GenreHandler.class)
@Import(GenreRoutesConfig.class)
class GenreHandlerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private GenreService genreService;

    @Test
    @DisplayName("GET /api/genres")
    void list_shouldReturnAllGenres() {
        List<GenreDto> genres = List.of(
                new GenreDto(1L, "Роман"),
                new GenreDto(2L, "Фантастика"),
                new GenreDto(3L, "Детектив"));

        given(genreService.findAll()).willReturn(Flux.fromIterable(genres));

        webTestClient.get().uri("/api/genres")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(GenreDto.class)
                .hasSize(3)
                .contains(genres.toArray(new GenreDto[0]));
    }

    @Test
    @DisplayName("GET /api/genres")
    void list_shouldReturnEmptyList() {
        given(genreService.findAll()).willReturn(Flux.empty());

        webTestClient.get().uri("/api/genres")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(GenreDto.class)
                .hasSize(0);
    }
}
