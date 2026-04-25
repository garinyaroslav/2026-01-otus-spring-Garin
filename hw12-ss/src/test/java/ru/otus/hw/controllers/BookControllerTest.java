package ru.otus.hw.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.otus.hw.config.SecurityConfig;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.UserDetailsServiceImpl;

@DisplayName("REST контроллер книг")
@WebMvcTest(BookController.class)
@Import(SecurityConfig.class)
class BookControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    private static BookDto bookDto() {
        return new BookDto(1L, "Book A",
                new AuthorDto(1L, "Author A"),
                List.of(new GenreDto(1L, "Genre1")),
                List.of());
    }

    @Test
    @DisplayName("GET /api/books — без аутентификации возвращает 401")
    void listBooks_shouldReturn401WhenNotAuthenticated() throws Exception {
        mvc.perform(get("/api/books"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/books/{id} — без аутентификации возвращает 401")
    void viewBook_shouldReturn401WhenNotAuthenticated() throws Exception {
        mvc.perform(get("/api/books/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/books — без аутентификации возвращает 401")
    void createBook_shouldReturn401WhenNotAuthenticated() throws Exception {
        mvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new BookCreateDto("Title", 1L, Set.of(1L)))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/books/{id} — без аутентификации возвращает 401")
    void updateBook_shouldReturn401WhenNotAuthenticated() throws Exception {
        mvc.perform(put("/api/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new BookUpdateDto(1L, "Title", 1L, Set.of(1L)))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/books/{id} — без аутентификации возвращает 401")
    void deleteBook_shouldReturn401WhenNotAuthenticated() throws Exception {
        mvc.perform(delete("/api/books/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/books")
    void listBooks_shouldReturnAllBooks() throws Exception {
        given(bookService.findAll()).willReturn(List.of(bookDto(), bookDto()));

        mvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].title").value("Book A"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/books/{id}")
    void viewBook_shouldReturnBookById() throws Exception {
        given(bookService.findById(1L)).willReturn(bookDto());

        mvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.author.fullName").value("Author A"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/books/{id} — не найдена")
    void viewBook_shouldReturn404WhenNotFound() throws Exception {
        given(bookService.findById(anyLong()))
                .willThrow(new EntityNotFoundException("Book not found"));

        mvc.perform(get("/api/books/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/books")
    void createBook_shouldCreateAndReturnCreatedStatus() throws Exception {
        given(bookService.insert(any())).willReturn(bookDto());

        mvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new BookCreateDto("New Book", 1L, Set.of(1L)))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(bookService).insert(any());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/books — валидация")
    void createBook_shouldReturn400OnValidationError() throws Exception {
        mvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new BookCreateDto("title", 1L, Set.of()))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /api/books/{id}")
    void updateBook_shouldUpdateAndReturnBook() throws Exception {
        given(bookService.update(any())).willReturn(bookDto());

        mvc.perform(put("/api/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new BookUpdateDto(1L, "Updated", 2L, Set.of(3L)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Book A"));
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/books/{id}")
    void deleteBook_shouldDeleteAndReturnNoContent() throws Exception {
        mvc.perform(delete("/api/books/1"))
                .andExpect(status().isNoContent());

        verify(bookService).deleteById(1L);
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /api/books/{id} — не найдена")
    void updateBook_shouldReturn404WhenNotFound() throws Exception {
        given(bookService.update(any()))
                .willThrow(new EntityNotFoundException("Book not found"));

        mvc.perform(put("/api/books/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new BookUpdateDto(99L, "Title", 2L, Set.of(3L)))))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/books/{id} — не найдена")
    void deleteBook_shouldReturn404WhenNotFound() throws Exception {
        willThrow(new EntityNotFoundException("Book not found"))
                .given(bookService).deleteById(99L);

        mvc.perform(delete("/api/books/99"))
                .andExpect(status().isNotFound());
    }
}
