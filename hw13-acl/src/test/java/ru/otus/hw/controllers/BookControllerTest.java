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
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/books — USER получает список книг")
    void listBooks_shouldReturnAllBooks() throws Exception {
        given(bookService.findAll()).willReturn(List.of(bookDto(), bookDto()));

        mvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].title").value("Book A"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/books/{id} — USER получает книгу по id")
    void viewBook_shouldReturnBookById() throws Exception {
        given(bookService.findById(1L)).willReturn(bookDto());

        mvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.author.fullName").value("Author A"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/books/{id} — книга не найдена возвращает 404")
    void viewBook_shouldReturn404WhenNotFound() throws Exception {
        given(bookService.findById(anyLong()))
                .willThrow(new EntityNotFoundException("Book not found"));

        mvc.perform(get("/api/books/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/books — ADMIN создаёт книгу")
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
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/books — ошибка валидации возвращает 400")
    void createBook_shouldReturn400OnValidationError() throws Exception {
        mvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new BookCreateDto("title", 1L, Set.of()))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/books/{id} — ADMIN обновляет книгу")
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
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/books/{id} — ADMIN удаляет книгу")
    void deleteBook_shouldDeleteAndReturnNoContent() throws Exception {
        mvc.perform(delete("/api/books/1"))
                .andExpect(status().isNoContent());

        verify(bookService).deleteById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/books/{id} — книга не найдена возвращает 404")
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
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/books/{id} — книга не найдена возвращает 404")
    void deleteBook_shouldReturn404WhenNotFound() throws Exception {
        willThrow(new EntityNotFoundException("Book not found"))
                .given(bookService).deleteById(99L);

        mvc.perform(delete("/api/books/99"))
                .andExpect(status().isNotFound());
    }

}
