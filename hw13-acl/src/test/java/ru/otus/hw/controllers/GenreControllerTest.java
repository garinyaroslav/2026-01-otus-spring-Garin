package ru.otus.hw.controllers;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import ru.otus.hw.config.SecurityConfig;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.services.GenreService;
import ru.otus.hw.services.UserDetailsServiceImpl;

@DisplayName("REST контроллер жанров")
@WebMvcTest(GenreController.class)
@Import(SecurityConfig.class)
class GenreControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private GenreService genreService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @WithMockUser
    @DisplayName("GET /api/genres — аутентифицированный пользователь получает список")
    void listGenres_shouldReturnAllGenres() throws Exception {
        given(genreService.findAll()).willReturn(List.of(
                new GenreDto(1L, "Роман"),
                new GenreDto(2L, "Фантастика"),
                new GenreDto(3L, "Детектив")));

        mvc.perform(get("/api/genres"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(3))
                .andExpect(jsonPath("$[0].name").value("Роман"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/genres — пустой список")
    void listGenres_shouldReturnEmptyList() throws Exception {
        given(genreService.findAll()).willReturn(List.of());

        mvc.perform(get("/api/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
    }
}
