package ru.otus.hw.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.GenreService;

@DisplayName("Контроллер книг")
@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private AuthorService authorService;

    @MockBean
    private GenreService genreService;

    private static AuthorDto authorDto() {
        return new AuthorDto(1L, "Author A");
    }

    private static List<AuthorDto> authorDtos() {
        return List.of(authorDto(), new AuthorDto(2L, "Author B"));
    }

    private static GenreDto genreDto() {
        return new GenreDto(1L, "Genre1");
    }

    private static List<GenreDto> genreDtos() {
        return List.of(genreDto(), new GenreDto(2L, "Genre2"));
    }

    private static BookDto bookDto() {
        return new BookDto(1L, "Book A", authorDto(), List.of(genreDto()), List.of());
    }

    @DisplayName("GET /books")
    @Test
    void listBooks_shouldReturnListView() throws Exception {
        given(bookService.findAll()).willReturn(List.of(bookDto()));

        mvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/list"))
                .andExpect(model().attributeExists("books"));
    }

    @DisplayName("GET /books/{id}")
    @Test
    void viewBook_shouldReturnViewPage() throws Exception {
        given(bookService.findById(1L)).willReturn(bookDto());

        mvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/view"))
                .andExpect(model().attributeExists("book"));
    }

    @DisplayName("GET /books/{id}")
    @Test
    void viewBook_shouldReturn404WhenNotFound() throws Exception {
        given(bookService.findById(anyLong()))
                .willThrow(new EntityNotFoundException("Book not found"));

        mvc.perform(get("/books/99"))
                .andExpect(status().isNotFound());
    }

    @DisplayName("GET /books/create")
    @Test
    void createBookForm_shouldReturnForm() throws Exception {
        given(authorService.findAll()).willReturn(authorDtos());
        given(genreService.findAll()).willReturn(genreDtos());

        mvc.perform(get("/books/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/form-create"))
                .andExpect(model().attributeExists("bookCreateDto", "authors", "genres"));
    }

    @DisplayName("POST /books/create")
    @Test
    void createBook_shouldInsertAndRedirect() throws Exception {
        given(bookService.insert(any(BookCreateDto.class))).willReturn(bookDto());

        mvc.perform(post("/books/create")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", "New Book")
                .param("authorId", "1")
                .param("genreIds", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        verify(bookService).insert(any(BookCreateDto.class));
    }

    @DisplayName("POST /books/create")
    @Test
    void createBook_shouldReturn400OnValidationError() throws Exception {
        given(authorService.findAll()).willReturn(authorDtos());
        given(genreService.findAll()).willReturn(genreDtos());

        mvc.perform(post("/books/create")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", "")
                .param("authorId", "1")
                .param("genreIds", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/form-create"))
                .andExpect(model().attributeHasFieldErrors("bookCreateDto", "title"));
    }

    @DisplayName("POST /books/create")
    @Test
    void createBook_shouldReturn400WhenNoGenres() throws Exception {
        given(authorService.findAll()).willReturn(authorDtos());
        given(genreService.findAll()).willReturn(genreDtos());

        mvc.perform(post("/books/create")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", "Some Title")
                .param("authorId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/form-create"))
                .andExpect(model().attributeHasFieldErrors("bookCreateDto", "genreIds"));
    }

    @DisplayName("GET /books/{id}/edit")
    @Test
    void editBookForm_shouldReturnFormWithBook() throws Exception {
        given(bookService.findById(1L)).willReturn(bookDto());
        given(authorService.findAll()).willReturn(authorDtos());
        given(genreService.findAll()).willReturn(genreDtos());

        mvc.perform(get("/books/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/form-edit"))
                .andExpect(model().attributeExists("bookUpdateDto", "authors", "genres"));
    }

    @DisplayName("GET /books/{id}/edit")
    @Test
    void editBookForm_shouldReturn404WhenNotFound() throws Exception {
        given(bookService.findById(anyLong()))
                .willThrow(new EntityNotFoundException("Book not found"));

        mvc.perform(get("/books/99/edit"))
                .andExpect(status().isNotFound());
    }

    @DisplayName("POST /books/{id}/edit")
    @Test
    void editBook_shouldUpdateAndRedirect() throws Exception {
        given(bookService.update(any())).willReturn(bookDto());

        mvc.perform(post("/books/1/edit")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id", "1")
                .param("title", "Updated Title")
                .param("authorId", "1")
                .param("genreIds", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        verify(bookService).update(any());
    }

    @DisplayName("POST /books/{id}/edit")
    @Test
    void editBook_shouldReturnFormOnValidationError() throws Exception {
        given(authorService.findAll()).willReturn(authorDtos());
        given(genreService.findAll()).willReturn(genreDtos());

        mvc.perform(post("/books/1/edit")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("id", "1")
                .param("title", "")
                .param("authorId", "1")
                .param("genreIds", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/form-edit"))
                .andExpect(model().attributeHasFieldErrors("bookUpdateDto", "title"));
    }

    @DisplayName("GET /books/{id}/delete")
    @Test
    void deleteBookForm_shouldReturnConfirmPage() throws Exception {
        given(bookService.findById(1L)).willReturn(bookDto());

        mvc.perform(get("/books/1/delete"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/delete"))
                .andExpect(model().attributeExists("book"));
    }

    @DisplayName("GET /books/{id}/delete")
    @Test
    void deleteBookForm_shouldReturn404WhenNotFound() throws Exception {
        given(bookService.findById(anyLong()))
                .willThrow(new EntityNotFoundException("Book not found"));

        mvc.perform(get("/books/99/delete"))
                .andExpect(status().isNotFound());
    }

    @DisplayName("DELETE /books/{id}/delete")
    @Test
    void deleteBook_shouldDeleteAndRedirect() throws Exception {
        mvc.perform(delete("/books/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        verify(bookService).deleteById(1L);
    }

    @DisplayName("GET /books")
    @Test
    void listBooks_shouldReturn500OnUnexpectedException() throws Exception {
        given(bookService.findAll()).willThrow(new RuntimeException("Unexpected DB error"));

        mvc.perform(get("/books"))
                .andExpect(status().isInternalServerError());
    }
}
