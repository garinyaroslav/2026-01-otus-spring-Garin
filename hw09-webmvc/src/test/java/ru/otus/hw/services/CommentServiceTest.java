package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.models.Comment;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Тест сервиса комментариев")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional(propagation = Propagation.NEVER)
class CommentServiceTest {

    @Autowired
    private CommentService commentService;

    @DisplayName("findById должен возвращать комментарий с доступной книгой")
    @Test
    void findById_shouldReturnCommentWithAccessibleBook() {
        var inserted = commentService.insert(1L, "Test comment");

        var found = commentService.findById(inserted.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getText()).isEqualTo("Test comment");
        assertThat(found.get().getBook()).isNotNull();
        assertThat(found.get().getBook().getId()).isEqualTo(1L);

        commentService.deleteById(inserted.getId());
    }

    @DisplayName("findAllByBookId должен возвращать все комментарии книги")
    @Test
    void findAllByBookId_shouldReturnComments() {
        var c1 = commentService.insert(1L, "Comment Alpha");
        var c2 = commentService.insert(1L, "Comment Beta");

        List<Comment> comments = commentService.findAllByBookId(1L);

        assertThat(comments)
                .extracting(Comment::getText)
                .contains("Comment Alpha", "Comment Beta");

        commentService.deleteById(c1.getId());
        commentService.deleteById(c2.getId());
    }

    @DisplayName("insert должен сохранять комментарий")
    @Test
    void insert_shouldPersistComment() {
        var saved = commentService.insert(1L, "New comment");

        assertThat(saved.getId()).isGreaterThan(0);
        assertThat(saved.getText()).isEqualTo("New comment");
        assertThat(saved.getBook().getId()).isEqualTo(1L);

        commentService.deleteById(saved.getId());
    }

    @DisplayName("update должен обновлять текст комментария")
    @Test
    void update_shouldUpdateCommentText() {
        var saved = commentService.insert(1L, "Old text");
        var updated = commentService.update(saved.getId(), "New text");

        assertThat(updated.getText()).isEqualTo("New text");

        commentService.deleteById(saved.getId());
    }

    @DisplayName("deleteById должен удалять комментарий")
    @Test
    void deleteById_shouldDeleteComment() {
        var saved = commentService.insert(1L, "To be deleted");

        commentService.deleteById(saved.getId());

        assertThat(commentService.findById(saved.getId())).isEmpty();
    }
}
