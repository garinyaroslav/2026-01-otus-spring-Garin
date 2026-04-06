package ru.otus.hw.controllers;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
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

    private static Author author() {
        return new Author(1L, "Author A", null);
    }

    private static List<Author> authors() {
        return List.of(author(), new Author(2L, "Author B", null));
    }

    private static Genre genre() {
        return new Genre(1L, "Genre1");
    }

    private static List<Genre> genres() {
        return List.of(genre(), new Genre(2L, "Genre2"));
    }

    private static Book book() {
        return new Book(1L, "Book A", author(), List.of(genre()), List.of());
    }

    @DisplayName("GET /books")
    @Test
    void listBooks_shouldReturnListView() throws Exception {
        given(bookService.findAll()).willReturn(List.of(book()));

        mvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/list"))
                .andExpect(model().attributeExists("books"));
    }

    @DisplayName("GET /books/{id}")
    @Test
    void viewBook_shouldReturnViewPage() throws Exception {
        given(bookService.findById(1L)).willReturn(Optional.of(book()));

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
        given(authorService.findAll()).willReturn(authors());
        given(genreService.findAll()).willReturn(genres());

        mvc.perform(get("/books/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/form-create"))
                .andExpect(model().attributeExists("bookCreateDto", "authors", "genres"));
    }

    @DisplayName("POST /books/create")
    @Test
    void createBook_shouldInsertAndRedirect() throws Exception {
        given(authorService.findAll()).willReturn(authors());
        given(genreService.findAll()).willReturn(genres());

        mvc.perform(get("/books/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/form-create"))
                .andExpect(model().attributeExists("bookCreateDto", "authors", "genres"));
    }

    @DisplayName("GET /books/{id}/edit")
    @Test
    void editBookForm_shouldReturnFormWithBook() throws Exception {
        given(bookService.findById(1L)).willReturn(Optional.of(book()));
        given(authorService.findAll()).willReturn(authors());
        given(genreService.findAll()).willReturn(genres());

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
        given(bookService.findById(1L)).willReturn(Optional.of(book()));
        given(authorService.findAll()).willReturn(authors());
        given(genreService.findAll()).willReturn(genres());

        mvc.perform(get("/books/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/form-edit"))
                .andExpect(model().attributeExists("bookUpdateDto", "authors", "genres"));
    }

    @DisplayName("GET /books/{id}/delete")
    @Test
    void deleteBookForm_shouldReturnConfirmPage() throws Exception {
        given(bookService.findById(1L)).willReturn(Optional.of(book()));

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
}
