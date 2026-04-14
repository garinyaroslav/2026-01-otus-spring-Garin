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

import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.services.AuthorService;

@DisplayName("REST контроллер авторов")
@WebMvcTest(AuthorController.class)
class AuthorControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private AuthorService authorService;

    private static AuthorDto authorDto(Long id, String fullName) {
        return new AuthorDto(id, fullName);
    }

    @Test
    @DisplayName("GET /api/authors")
    void listAuthors_shouldReturnAllAuthors() throws Exception {
        List<AuthorDto> authors = List.of(
                authorDto(1L, "Лев Толстой"),
                authorDto(2L, "Фёдор Достоевский"),
                authorDto(3L, "Антон Чехов"));

        given(authorService.findAll()).willReturn(authors);

        mvc.perform(get("/api/authors"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(3))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].fullName").value("Лев Толстой"))
                .andExpect(jsonPath("$[1].fullName").value("Фёдор Достоевский"));
    }

    @Test
    @DisplayName("GET /api/authors")
    void listAuthors_shouldReturnEmptyList() throws Exception {
        given(authorService.findAll()).willReturn(List.of());

        mvc.perform(get("/api/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
    }
}
