package ru.otus.hw.controllers;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.UserDetailsServiceImpl;

@DisplayName("REST контроллер комментариев")
@WebMvcTest(CommentController.class)
@Import(SecurityConfig.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CommentService commentService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/comments — USER создаёт комментарий")
    void addComment_shouldCreateCommentAndReturnCreated() throws Exception {
        given(commentService.insert(1L, "Хорошая книга"))
                .willReturn(new CommentDto(10L, "Хорошая книга", 1L));

        mvc.perform(post("/api/comments")
                .param("bookId", "1")
                .param("text", "Хорошая книга"))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.bookId").value(1));

        verify(commentService).insert(1L, "Хорошая книга");
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/comments — пустой текст возвращает 400")
    void addComment_shouldReturnBadRequestWhenTextIsEmpty() throws Exception {
        mvc.perform(post("/api/comments")
                .param("bookId", "1")
                .param("text", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/comments — книга не найдена возвращает 404")
    void addComment_shouldReturnNotFoundWhenBookNotExists() throws Exception {
        given(commentService.insert(anyLong(), anyString()))
                .willThrow(new EntityNotFoundException("Book not found"));

        mvc.perform(post("/api/comments")
                .param("bookId", "999")
                .param("text", "Комментарий"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/comments/{id} — ADMIN удаляет комментарий")
    void deleteComment_shouldDeleteAndReturnNoContent() throws Exception {
        mvc.perform(delete("/api/comments/5"))
                .andExpect(status().isNoContent());

        verify(commentService).deleteById(5L);
    }

}
