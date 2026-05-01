package ru.otus.hw.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.GenreService;
import ru.otus.hw.services.UserDetailsServiceImpl;

@DisplayName("Тесты безопасности контроллеров (только доступ)")
@WebMvcTest(controllers = {
        BookController.class,
        AuthorController.class,
        GenreController.class,
        CommentController.class
}, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class,
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class
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
    void listBooks_401() throws Exception {
        mvc.perform(get("/api/books")).andExpect(status().isUnauthorized());
    }

    @Test
    void viewBook_401() throws Exception {
        mvc.perform(get("/api/books/1")).andExpect(status().isUnauthorized());
    }

    @Test
    void createBook_401() throws Exception {
        mvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new BookCreateDto("Title", 1L, Set.of(1L)))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateBook_401() throws Exception {
        mvc.perform(put("/api/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new BookUpdateDto(1L, "Title", 1L, Set.of(1L)))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteBook_401() throws Exception {
        mvc.perform(delete("/api/books/1")).andExpect(status().isUnauthorized());
    }

    @Test
    void listAuthors_401() throws Exception {
        mvc.perform(get("/api/authors")).andExpect(status().isUnauthorized());
    }

    @Test
    void listGenres_401() throws Exception {
        mvc.perform(get("/api/genres")).andExpect(status().isUnauthorized());
    }

    @Test
    void addComment_401() throws Exception {
        mvc.perform(post("/api/comments")
                .param("bookId", "1")
                .param("text", "Текст"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteComment_401() throws Exception {
        mvc.perform(delete("/api/comments/1")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createBook_403_user() throws Exception {
        mvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new BookCreateDto("Title", 1L, Set.of(1L)))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateBook_403_user() throws Exception {
        mvc.perform(put("/api/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new BookUpdateDto(1L, "Title", 1L, Set.of(1L)))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteBook_403_user() throws Exception {
        mvc.perform(delete("/api/books/1")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteComment_403_user() throws Exception {
        mvc.perform(delete("/api/comments/1")).andExpect(status().isForbidden());
    }
}
