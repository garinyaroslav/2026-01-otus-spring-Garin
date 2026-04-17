package ru.otus.hw.controllers;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.services.GenreService;

@DisplayName("REST контроллер жанров")
@WebMvcTest(GenreController.class)
class GenreControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private GenreService genreService;

    private static GenreDto genreDto(Long id, String name) {
        return new GenreDto(id, name);
    }

    @Test
    @DisplayName("GET /api/genres")
    void listGenres_shouldReturnAllGenres() throws Exception {
        List<GenreDto> genres = List.of(
                genreDto(1L, "Роман"),
                genreDto(2L, "Фантастика"),
                genreDto(3L, "Детектив"));

        given(genreService.findAll()).willReturn(genres);

        mvc.perform(get("/api/genres"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(3))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Роман"))
                .andExpect(jsonPath("$[2].name").value("Детектив"));
    }

    @Test
    @DisplayName("GET /api/genres")
    void listGenres_shouldReturnEmptyList() throws Exception {
        given(genreService.findAll()).willReturn(List.of());

        mvc.perform(get("/api/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
    }
}
