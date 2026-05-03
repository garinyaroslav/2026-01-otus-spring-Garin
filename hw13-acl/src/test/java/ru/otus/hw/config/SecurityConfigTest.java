package ru.otus.hw.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

import ru.otus.hw.controllers.AuthorController;
import ru.otus.hw.controllers.BookController;
import ru.otus.hw.controllers.CommentController;
import ru.otus.hw.controllers.GenreController;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.GenreService;
import ru.otus.hw.services.UserDetailsServiceImpl;

@DisplayName("Тесты безопасности контроллеров")
@WebMvcTest(controllers = {
        BookController.class,
        AuthorController.class,
        GenreController.class,
        CommentController.class
})
@Import(SecurityConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private BookService bookService;

    @MockBean
    private AuthorService authorService;

    @MockBean
    private GenreService genreService;

    @MockBean
    private CommentService commentService;

    @Test
    @DisplayName("GET /api/books — 401 без аутентификации")
    void listBooks_401() throws Exception {
        mvc.perform(get("/api/books")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/books/{id} — 401 без аутентификации")
    void viewBook_401() throws Exception {
        mvc.perform(get("/api/books/1")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/books — 401 без аутентификации")
    void createBook_401() throws Exception {
        mvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new BookCreateDto("Title", 1L, Set.of(1L)))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /api/books/{id} — 401 без аутентификации")
    void updateBook_401() throws Exception {
        mvc.perform(put("/api/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new BookUpdateDto(1L, "Title", 1L, Set.of(1L)))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/books/{id} — 401 без аутентификации")
    void deleteBook_401() throws Exception {
        mvc.perform(delete("/api/books/1")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/authors — 401 без аутентификации")
    void listAuthors_401() throws Exception {
        mvc.perform(get("/api/authors")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/genres — 401 без аутентификации")
    void listGenres_401() throws Exception {
        mvc.perform(get("/api/genres")).andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/comments — 401 без аутентификации")
    void addComment_401() throws Exception {
        mvc.perform(post("/api/comments")
                .param("bookId", "1")
                .param("text", "Текст"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/comments/{id} — 401 без аутентификации")
    void deleteComment_401() throws Exception {
        mvc.perform(delete("/api/comments/1")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/books — 403 для USER")
    void createBook_403_user() throws Exception {
        mvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new BookCreateDto("Title", 1L, Set.of(1L)))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("PUT /api/books/{id} — 403 для USER")
    void updateBook_403_user() throws Exception {
        mvc.perform(put("/api/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new BookUpdateDto(1L, "Title", 1L, Set.of(1L)))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /api/books/{id} — 403 для USER")
    void deleteBook_403_user() throws Exception {
        mvc.perform(delete("/api/books/1")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /api/comments/{id} — 403 для USER")
    void deleteComment_403_user() throws Exception {
        mvc.perform(delete("/api/comments/1")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/books — 200 для USER")
    void listBooks_200_user() throws Exception {
        given(bookService.findAll()).willReturn(List.of(
                new BookDto(1L, "Book A", new AuthorDto(1L, "Author"), List.of(), List.of())));

        mvc.perform(get("/api/books")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/books/{id} — 200 для USER")
    void viewBook_200_user() throws Exception {
        given(bookService.findById(1L))
                .willReturn(new BookDto(1L, "Book A", new AuthorDto(1L, "Author"), List.of(), List.of()));

        mvc.perform(get("/api/books/1")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/authors — 200 для USER")
    void listAuthors_200_user() throws Exception {
        given(authorService.findAll()).willReturn(List.of(new AuthorDto(1L, "Author")));

        mvc.perform(get("/api/authors")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/genres — 200 для USER")
    void listGenres_200_user() throws Exception {
        given(genreService.findAll()).willReturn(List.of(new GenreDto(1L, "Genre")));

        mvc.perform(get("/api/genres")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/comments — 201 для USER")
    void addComment_201_user() throws Exception {
        given(commentService.insert(anyLong(), anyString()))
                .willReturn(new CommentDto(1L, "Текст", 1L));

        mvc.perform(post("/api/comments")
                .param("bookId", "1")
                .param("text", "Текст"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/books — 201 для ADMIN")
    void createBook_201_admin() throws Exception {
        given(bookService.insert(any()))
                .willReturn(new BookDto(1L, "Book A", new AuthorDto(1L, "Author"), List.of(), List.of()));

        mvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new BookCreateDto("Book A", 1L, Set.of(1L)))))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /api/books/{id} — 200 для ADMIN")
    void updateBook_200_admin() throws Exception {
        given(bookService.update(any()))
                .willReturn(new BookDto(1L, "Updated", new AuthorDto(1L, "Author"), List.of(), List.of()));

        mvc.perform(put("/api/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new BookUpdateDto(1L, "Updated", 1L, Set.of(1L)))))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/books/{id} — 204 для ADMIN")
    void deleteBook_204_admin() throws Exception {
        mvc.perform(delete("/api/books/1")).andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/comments/{id} — 204 для ADMIN")
    void deleteComment_204_admin() throws Exception {
        mvc.perform(delete("/api/comments/1")).andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/comments — 201 для ADMIN")
    void addComment_201_admin() throws Exception {
        given(commentService.insert(anyLong(), anyString()))
                .willReturn(new CommentDto(1L, "Текст", 1L));

        mvc.perform(post("/api/comments")
                .param("bookId", "1")
                .param("text", "Текст"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/books — 200 для ADMIN")
    void listBooks_200_admin() throws Exception {
        given(bookService.findAll()).willReturn(List.of(
                new BookDto(1L, "Book A", new AuthorDto(1L, "Author"), List.of(), List.of())));

        mvc.perform(get("/api/books")).andExpect(status().isOk());
    }
}
