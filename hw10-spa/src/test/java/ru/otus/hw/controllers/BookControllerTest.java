package ru.otus.hw.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.BookService;

@DisplayName("REST контроллер книг")
@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

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
    void listBooks_shouldReturnAllBooks() throws Exception {
        given(bookService.findAll()).willReturn(List.of(bookDto(), bookDto()));

        mvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Book A"));
    }

    @Test
    @DisplayName("GET /api/books/{id}")
    void viewBook_shouldReturnBookById() throws Exception {
        given(bookService.findById(1L)).willReturn(bookDto());

        mvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Book A"))
                .andExpect(jsonPath("$.author.fullName").value("Author A"));
    }

    @Test
    @DisplayName("GET /api/books/{id}")
    void viewBook_shouldReturn404WhenNotFound() throws Exception {
        given(bookService.findById(anyLong()))
                .willThrow(new EntityNotFoundException("Book not found"));

        mvc.perform(get("/api/books/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/books")
    void createBook_shouldCreateAndReturnCreatedStatus() throws Exception {
        BookCreateDto createDto = new BookCreateDto("New Book", 1L, Set.of(1L, 2L));

        given(bookService.insert(any(BookCreateDto.class))).willReturn(bookDto());

        mvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Book A"));

        verify(bookService).insert(any(BookCreateDto.class));
    }

    @Test
    @DisplayName("POST /api/books")
    void createBook_shouldReturn400OnValidationError() throws Exception {
        BookCreateDto invalidDto = new BookCreateDto("title", 1L, Set.of());

        mvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/books/{id}")
    void updateBook_shouldUpdateAndReturnBook() throws Exception {
        BookUpdateDto updateDto = new BookUpdateDto(1L, "Updated Title", 2L, Set.of(3L));

        given(bookService.update(any(BookUpdateDto.class))).willReturn(bookDto());

        mvc.perform(put("/api/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Book A"));

        verify(bookService).update(any(BookUpdateDto.class));
    }

    @Test
    @DisplayName("DELETE /api/books/{id}")
    void deleteBook_shouldDeleteAndReturnNoContent() throws Exception {
        mvc.perform(delete("/api/books/1"))
                .andExpect(status().isNoContent());

        verify(bookService).deleteById(1L);
    }

}
