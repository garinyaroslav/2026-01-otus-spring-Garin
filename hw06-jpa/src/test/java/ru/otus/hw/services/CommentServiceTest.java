package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.models.Comment;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("Тест сервиса комментариев")
@SpringBootTest
@Transactional(propagation = Propagation.NEVER)
class CommentServiceTest {

    @Autowired
    private CommentService commentService;

    @Test
    @DisplayName("findById возвращает комментарий с доступной книгой снаружи транзакции")
    void findById_shouldReturnCommentWithAccessibleBook() {
        Optional<Comment> result = commentService.findById(1L);

        assertThat(result).isPresent();

        assertThatCode(() -> {
            assertThat(result.get().getText()).isNotBlank();
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("findAllByBookId возвращает комментарии для книги")
    void findAllByBookId_shouldReturnComments() {
        List<Comment> comments = commentService.findAllByBookId(1L);

        assertThat(comments).isNotEmpty();

        assertThatCode(() -> comments.forEach(c -> assertThat(c.getText()).isNotBlank())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("insert сохраняет комментарий")
    void insert_shouldPersistComment() {
        Comment comment = commentService.insert(1L, "Great book!");

        assertThat(comment.getId()).isGreaterThan(0);
        assertThat(comment.getText()).isEqualTo("Great book!");
    }

    @Test
    @DisplayName("update обновляет текст комментария")
    void update_shouldUpdateCommentText() {
        commentService.insert(1L, "Initial text");
        List<Comment> all = commentService.findAllByBookId(1L);
        long id = all.get(all.size() - 1).getId();

        Comment updated = commentService.update(id, "Updated text");

        assertThat(updated.getText()).isEqualTo("Updated text");
    }

    @Test
    @DisplayName("deleteById удаляет комментарий")
    void deleteById_shouldDeleteComment() {
        Comment inserted = commentService.insert(1L, "To be deleted");

        commentService.deleteById(inserted.getId());

        assertThat(commentService.findById(inserted.getId())).isEmpty();
    }
}
